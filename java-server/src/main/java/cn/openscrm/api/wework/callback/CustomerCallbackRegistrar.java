package cn.openscrm.api.wework.callback;

import cn.openscrm.api.common.id.SnowflakeIdGenerator;
import cn.openscrm.api.common.mq.DelayedJobPublisher;
import cn.openscrm.api.common.mq.DelayedJobTopics;
import cn.openscrm.api.persistence.entity.CustomerEventPo;
import cn.openscrm.api.persistence.entity.CustomerStaffPo;
import cn.openscrm.api.persistence.entity.CustomerStaffRelationHistoryPo;
import cn.openscrm.api.persistence.entity.CustomerStatisticPo;
import cn.openscrm.api.persistence.mapper.CustomerEventPoMapper;
import cn.openscrm.api.persistence.mapper.CustomerStaffPoMapper;
import cn.openscrm.api.persistence.mapper.CustomerStaffRelationHistoryPoMapper;
import cn.openscrm.api.persistence.mapper.CustomerStatisticPoMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomerCallbackRegistrar implements WeWorkCallbackRegistrar {

    private static final String EVENT_TYPE_CUSTOMER_ACTION = "customer_action";
    private static final String EVENT_NAME_DELETE_EXTERNAL_USER = "delete_external_user";
    private static final String EVENT_NAME_ADD_EXTERNAL_USER = "add_external_user";
    private static final String EVENT_NAME_TRANSFER_FAIL = "transfer_fail";

    private final CustomerEventPoMapper customerEventMapper;
    private final CustomerStatisticPoMapper customerStatisticMapper;
    private final CustomerStaffPoMapper customerStaffMapper;
    private final CustomerStaffRelationHistoryPoMapper relationHistoryMapper;
    private final DelayedJobPublisher delayedJobPublisher;
    private final SnowflakeIdGenerator idGenerator;
    private final DeleteCustomerNotificationService deleteCustomerNotificationService;

    @Override
    public void register(WeWorkCallbackService service) {
        service.register("event", "change_external_contact", "add_external_contact", this::addExternalContact);
        service.register("event", "change_external_contact", "add_half_external_contact", this::addExternalContact);
        service.register("event", "change_external_contact", "edit_external_contact", this::editExternalContact);
        service.register("event", "change_external_contact", "del_external_contact", this::staffDeleteCustomer);
        service.register("event", "change_external_contact", "del_follow_user", this::customerDeleteStaff);
        service.register("event", "change_external_contact", "transfer_fail", this::transferFail);
    }

    @Transactional(rollbackFor = Exception.class)
    public void addExternalContact(WeWorkCallbackMessage message) {
        String extStaffId = field(message, "UserID");
        String extCustomerId = field(message, "ExternalUserID");
        if (!hasStaffAndCustomer(extStaffId, extCustomerId, message)) {
            return;
        }
        publishSyncCustomerJob(extStaffId, extCustomerId, field(message, "WelcomeCode"), field(message, "State"));
        upsertStatistic(message.getToUserName(), extStaffId, 1);
        createEvent(message.getToUserName(), extStaffId, extCustomerId,
                "员工 [" + extStaffId + "] 添加了客户 [" + extCustomerId + "] ",
                EVENT_NAME_ADD_EXTERNAL_USER);
    }

    public void editExternalContact(WeWorkCallbackMessage message) {
        String extStaffId = field(message, "UserID");
        String extCustomerId = field(message, "ExternalUserID");
        if (!hasStaffAndCustomer(extStaffId, extCustomerId, message)) {
            return;
        }
        publishSyncCustomerJob(extStaffId, extCustomerId, null, field(message, "State"));
    }

    @Transactional(rollbackFor = Exception.class)
    public void staffDeleteCustomer(WeWorkCallbackMessage message) {
        handleDeleteRelation(message, false);
    }

    @Transactional(rollbackFor = Exception.class)
    public void customerDeleteStaff(WeWorkCallbackMessage message) {
        handleDeleteRelation(message, true);
    }

    public void transferFail(WeWorkCallbackMessage message) {
        String extStaffId = field(message, "UserID");
        String extCustomerId = field(message, "ExternalUserID");
        if (!hasStaffAndCustomer(extStaffId, extCustomerId, message)) {
            return;
        }
        String failReason = field(message, "FailReason");
        createEvent(message.getToUserName(), extStaffId, extCustomerId,
                "客户 [" + extCustomerId + "] 接替失败，原因 [" + failReason + "] ",
                EVENT_NAME_TRANSFER_FAIL);
    }

    private void handleDeleteRelation(WeWorkCallbackMessage message, boolean customerDeleteStaff) {
        String extStaffId = field(message, "UserID");
        String extCustomerId = field(message, "ExternalUserID");
        if (!hasStaffAndCustomer(extStaffId, extCustomerId, message)) {
            return;
        }
        String content = customerDeleteStaff
                ? "客户 [" + extCustomerId + "] 删除了员工 [" + extStaffId + "] "
                : "员工 [" + extStaffId + "] 删除了客户 [" + extCustomerId + "] ";
        if (customerDeleteStaff) {
            deleteCustomerNotificationService.notifyCustomerDeleteStaff(
                    message.getToUserName(), extStaffId, extCustomerId);
        } else {
            content = deleteCustomerNotificationService.notifyStaffDeleteCustomer(
                    message.getToUserName(), extStaffId, extCustomerId);
        }
        createEvent(message.getToUserName(), extStaffId, extCustomerId, content, EVENT_NAME_DELETE_EXTERNAL_USER);
        boolean hadRelation = hasActiveRelation(message.getToUserName(), extStaffId, extCustomerId);
        softDeleteRelation(message.getToUserName(), extStaffId, extCustomerId);
        createRelationHistory(message.getToUserName(), extStaffId, extCustomerId, customerDeleteStaff);
        if (hadRelation) {
            upsertStatistic(message.getToUserName(), extStaffId, -1);
        }
    }

    private boolean hasStaffAndCustomer(String extStaffId, String extCustomerId, WeWorkCallbackMessage message) {
        if (!StringUtils.hasText(extStaffId) || !StringUtils.hasText(extCustomerId)) {
            log.warn("missing staff/customer in callback changeType={}, fields={}",
                    message.getChangeType(), message.getFields());
            return false;
        }
        return true;
    }

    private void publishSyncCustomerJob(String extStaffId, String extCustomerId, String welcomeCode, String state) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("topic", DelayedJobTopics.SYNC_CUSTOMER_DATA);
        payload.put("extStaffId", extStaffId);
        payload.put("extCustomerId", extCustomerId);
        payload.put("welcomeCode", welcomeCode);
        payload.put("state", state);
        delayedJobPublisher.publish(payload, Duration.ZERO);
    }

    private void createEvent(String extCorpId, String extStaffId, String extCustomerId, String content, String eventName) {
        CustomerEventPo event = new CustomerEventPo();
        event.setId(idGenerator.nextId());
        event.setExtCorpId(extCorpId);
        event.setExtCreatorId(extStaffId);
        event.setContent(content);
        event.setEventType(EVENT_TYPE_CUSTOMER_ACTION);
        event.setEventName(eventName);
        event.setExtCustomerId(extCustomerId);
        event.setExtStaffId(extStaffId);
        customerEventMapper.insert(event);
    }

    private boolean hasActiveRelation(String extCorpId, String extStaffId, String extCustomerId) {
        return customerStaffMapper.selectCount(new LambdaQueryWrapper<CustomerStaffPo>()
                .eq(CustomerStaffPo::getExtCorpId, extCorpId)
                .eq(CustomerStaffPo::getExtStaffId, extStaffId)
                .eq(CustomerStaffPo::getExtCustomerId, extCustomerId)
                .isNull(CustomerStaffPo::getDeletedAt)) > 0;
    }

    private void softDeleteRelation(String extCorpId, String extStaffId, String extCustomerId) {
        customerStaffMapper.update(null, new LambdaUpdateWrapper<CustomerStaffPo>()
                .eq(CustomerStaffPo::getExtCorpId, extCorpId)
                .eq(CustomerStaffPo::getExtStaffId, extStaffId)
                .eq(CustomerStaffPo::getExtCustomerId, extCustomerId)
                .isNull(CustomerStaffPo::getDeletedAt)
                .set(CustomerStaffPo::getDeletedAt, LocalDateTime.now()));
    }

    private void createRelationHistory(String extCorpId,
                                       String extStaffId,
                                       String extCustomerId,
                                       boolean customerDeleteStaff) {
        CustomerStaffRelationHistoryPo history = new CustomerStaffRelationHistoryPo();
        history.setId(idGenerator.nextId());
        history.setExtCorpId(extCorpId);
        history.setExtCreatorId(extStaffId);
        history.setExtStaffId(extStaffId);
        history.setExtCustomerId(extCustomerId);
        LocalDateTime now = LocalDateTime.now();
        if (customerDeleteStaff) {
            history.setCustomerDeleteStaffAt(now);
        } else {
            history.setStaffDeleteCustomerAt(now);
        }
        relationHistoryMapper.insert(history);
    }

    private void upsertStatistic(String extCorpId, String extStaffId, long delta) {
        LocalDate today = LocalDate.now();
        CustomerStatisticPo statistic = customerStatisticMapper.selectOne(new LambdaQueryWrapper<CustomerStatisticPo>()
                .eq(CustomerStatisticPo::getExtCorpId, extCorpId)
                .eq(CustomerStatisticPo::getExtStaffId, extStaffId)
                .eq(CustomerStatisticPo::getDate, today)
                .last("limit 1"));
        if (statistic == null) {
            statistic = new CustomerStatisticPo();
            statistic.setId(idGenerator.nextId());
            statistic.setExtCorpId(extCorpId);
            statistic.setExtCreatorId(extStaffId);
            statistic.setExtStaffId(extStaffId);
            statistic.setDate(today);
            statistic.setIncreaseCustomerNum(0L);
            statistic.setDecreaseCustomerNum(0L);
        }
        statistic.setTotalCustomerNum(countActiveRelations(extCorpId, extStaffId));
        if (delta > 0) {
            statistic.setIncreaseCustomerNum(defaultLong(statistic.getIncreaseCustomerNum()) + delta);
        } else if (delta < 0) {
            statistic.setDecreaseCustomerNum(defaultLong(statistic.getDecreaseCustomerNum()) + Math.abs(delta));
        }
        if (customerStatisticMapper.selectById(statistic.getId()) == null) {
            customerStatisticMapper.insert(statistic);
        } else {
            customerStatisticMapper.updateById(statistic);
        }
    }

    private long countActiveRelations(String extCorpId, String extStaffId) {
        return customerStaffMapper.selectCount(new LambdaQueryWrapper<CustomerStaffPo>()
                .eq(CustomerStaffPo::getExtCorpId, extCorpId)
                .eq(CustomerStaffPo::getExtStaffId, extStaffId)
                .isNull(CustomerStaffPo::getDeletedAt));
    }

    private long defaultLong(Long value) {
        return value == null ? 0L : value;
    }

    private String field(WeWorkCallbackMessage message, String key) {
        return message.getFields().get(key);
    }
}
