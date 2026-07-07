package cn.openscrm.api.wework.callback;

import cn.openscrm.api.common.mq.DelayedJobPublisher;
import cn.openscrm.api.common.mq.DelayedJobTopics;
import cn.openscrm.api.config.OpenScrmProperties;
import cn.openscrm.api.persistence.entity.CustomerPo;
import cn.openscrm.api.persistence.entity.EventNotifyPo;
import cn.openscrm.api.persistence.entity.StaffPo;
import cn.openscrm.api.persistence.mapper.CustomerPoMapper;
import cn.openscrm.api.persistence.mapper.EventNotifyPoMapper;
import cn.openscrm.api.persistence.mapper.StaffPoMapper;
import cn.openscrm.api.wework.SendTextMessageRequest;
import cn.openscrm.api.wework.WeWorkClient;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeleteCustomerNotificationService {

    private static final int NOTIFY_ON = 1;
    private static final int NOTIFY_REAL_TIME = 1;

    private final EventNotifyPoMapper eventNotifyMapper;
    private final StaffPoMapper staffMapper;
    private final CustomerPoMapper customerMapper;
    private final WeWorkClient weWorkClient;
    private final OpenScrmProperties properties;
    private final ObjectMapper objectMapper;
    private final DelayedJobPublisher delayedJobPublisher;

    public String notifyStaffDeleteCustomer(String extCorpId, String extStaffId, String extCustomerId) {
        EventNotifyPo rule = findRule(extCorpId);
        String content = "员工 [" + staffName(extCorpId, extStaffId) + "] 删除了客户 ["
                + customerName(extCorpId, extCustomerId) + "] ";
        if (rule == null || !Integer.valueOf(NOTIFY_ON).equals(rule.getIsNotifyAdmins())) {
            return content;
        }
        List<String> adminIds = parseStaffIds(rule.getExtStaffIds());
        if (adminIds.isEmpty()) {
            log.warn("delete-customer admin notification is enabled but ext_staff_ids is empty extCorpId={}", extCorpId);
            return content;
        }
        if (!Integer.valueOf(NOTIFY_REAL_TIME).equals(rule.getNotifyType())) {
            publishTimedAdminNotification(extCorpId, adminIds, content);
            return content;
        }
        sendText(extCorpId, String.join("|", adminIds), content);
        return content;
    }

    public void notifyCustomerDeleteStaff(String extCorpId, String extStaffId, String extCustomerId) {
        EventNotifyPo rule = findRule(extCorpId);
        if (rule == null || !Integer.valueOf(NOTIFY_ON).equals(rule.getIsNotifyStaff())) {
            return;
        }
        String content = "您已被客户 [" + customerName(extCorpId, extCustomerId) + "] 删除";
        sendText(extCorpId, extStaffId, content);
    }

    public void sendTimedAdminNotification(Map<String, Object> payload) {
        String extCorpId = string(payload.get("extCorpId"));
        String content = string(payload.get("content"));
        List<String> adminIds = objectListToStrings(payload.get("adminIds"));
        if (!StringUtils.hasText(extCorpId) || !StringUtils.hasText(content) || adminIds.isEmpty()) {
            log.warn("invalid timed delete-customer notification payload={}", payload);
            return;
        }
        sendText(extCorpId, String.join("|", adminIds), content);
    }

    private EventNotifyPo findRule(String extCorpId) {
        return eventNotifyMapper.selectOne(new LambdaQueryWrapper<EventNotifyPo>()
                .eq(EventNotifyPo::getExtCorpId, extCorpId)
                .last("limit 1"));
    }

    private String staffName(String extCorpId, String extStaffId) {
        StaffPo staff = staffMapper.selectOne(new LambdaQueryWrapper<StaffPo>()
                .eq(StaffPo::getExtCorpId, extCorpId)
                .eq(StaffPo::getExtId, extStaffId)
                .last("limit 1"));
        return staff != null && StringUtils.hasText(staff.getName()) ? staff.getName() : extStaffId;
    }

    private String customerName(String extCorpId, String extCustomerId) {
        CustomerPo customer = customerMapper.selectOne(new LambdaQueryWrapper<CustomerPo>()
                .eq(CustomerPo::getExtCorpId, extCorpId)
                .eq(CustomerPo::getExtId, extCustomerId)
                .last("limit 1"));
        return customer != null && StringUtils.hasText(customer.getName()) ? customer.getName() : extCustomerId;
    }

    private List<String> parseStaffIds(String value) {
        if (!StringUtils.hasText(value)) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(value, new TypeReference<List<String>>() {
            });
        } catch (Exception e) {
            log.warn("failed to parse event_notify.ext_staff_ids={}", value);
            return Collections.emptyList();
        }
    }

    private void publishTimedAdminNotification(String extCorpId, List<String> adminIds, String content) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("topic", DelayedJobTopics.DELETE_CUSTOMER_ADMIN_NOTIFY);
        payload.put("extCorpId", extCorpId);
        payload.put("adminIds", adminIds);
        payload.put("content", content);
        delayedJobPublisher.publish(payload, delayUntilNextEight(LocalDateTime.now()));
    }

    Duration delayUntilNextEight(LocalDateTime now) {
        LocalDateTime target = now.toLocalDate().atTime(LocalTime.of(8, 0));
        if (!now.isBefore(target)) {
            target = target.plusDays(1);
        }
        return Duration.between(now, target);
    }

    private List<String> objectListToStrings(Object value) {
        if (!(value instanceof Iterable)) {
            return Collections.emptyList();
        }
        List<String> values = new ArrayList<>();
        for (Object item : (Iterable<?>) value) {
            if (item != null && StringUtils.hasText(String.valueOf(item))) {
                values.add(String.valueOf(item));
            }
        }
        return values;
    }

    private String string(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private void sendText(String extCorpId, String toUser, String content) {
        SendTextMessageRequest request = new SendTextMessageRequest();
        request.setTouser(toUser);
        request.setAgentid(properties.getWeWork().getMainAgentId());
        SendTextMessageRequest.Text text = new SendTextMessageRequest.Text();
        text.setContent(content);
        request.setText(text);
        weWorkClient.sendTextMessage(extCorpId, properties.getWeWork().getMainAgentSecret(), request);
    }
}
