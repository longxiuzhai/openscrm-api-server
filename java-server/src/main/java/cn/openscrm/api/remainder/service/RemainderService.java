package cn.openscrm.api.remainder.service;

import cn.openscrm.api.common.exception.BizException;
import cn.openscrm.api.common.exception.ErrorCode;
import cn.openscrm.api.common.id.SnowflakeIdGenerator;
import cn.openscrm.api.common.mq.DelayedJobPublisher;
import cn.openscrm.api.common.mq.DelayedJobTopics;
import cn.openscrm.api.config.OpenScrmProperties;
import cn.openscrm.api.persistence.entity.CustomerEventPo;
import cn.openscrm.api.persistence.mapper.CustomerEventPoMapper;
import cn.openscrm.api.remainder.dto.RemainderDeleteRequest;
import cn.openscrm.api.remainder.dto.RemainderRequest;
import cn.openscrm.api.remainder.dto.RemainderUpdateRequest;
import cn.openscrm.api.wework.SendTextMessageRequest;
import cn.openscrm.api.wework.WeWorkClient;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class RemainderService {

    private static final String EVENT_TYPE_REMAINDER = "reminder_event";
    private static final String EVENT_NAME_REMAINDER = "reminder_event";
    private static final String REMAINDER_CONTENT = "[ %s ], 您收到一条关于客户[ %s ]的提醒事件。 提醒内容: [ %s ] ";

    private final CustomerEventPoMapper customerEventMapper;
    private final DelayedJobPublisher delayedJobPublisher;
    private final SnowflakeIdGenerator idGenerator;
    private final WeWorkClient weWorkClient;
    private final OpenScrmProperties properties;

    @Transactional(rollbackFor = Exception.class)
    public CustomerEventPo create(RemainderRequest request, String extCorpId, String extCreatorId) {
        validateCreate(request);
        CustomerEventPo event = new CustomerEventPo();
        event.setId(idGenerator.nextId());
        event.setExtCorpId(extCorpId);
        event.setExtCreatorId(extCreatorId);
        event.setExtStaffId(request.getExtStaffId());
        event.setExtCustomerId(request.getExtCustomerId());
        event.setContent(request.getContent());
        event.setEventType(EVENT_TYPE_REMAINDER);
        event.setEventName(EVENT_NAME_REMAINDER);
        event.setSendAt((int) request.getSendAt().atZone(ZoneId.systemDefault()).toEpochSecond());
        customerEventMapper.insert(event);
        publishRemainder(event.getId(), extCorpId, request.getCustomerName(), request.getSendAt());
        return event;
    }

    public CustomerEventPo update(Long id, RemainderUpdateRequest request, String extCorpId) {
        if (id == null || request == null || !StringUtils.hasText(request.getContent())) {
            throw new BizException(ErrorCode.BAD_REQUEST);
        }
        CustomerEventPo event = requireEvent(id, extCorpId);
        event.setContent(request.getContent());
        customerEventMapper.updateById(event);
        return event;
    }

    public long delete(RemainderDeleteRequest request, String extCorpId) {
        if (request == null || CollectionUtils.isEmpty(request.getIds())) {
            throw new BizException(ErrorCode.BAD_REQUEST);
        }
        return customerEventMapper.update(null, new UpdateWrapper<CustomerEventPo>()
                .eq("ext_corp_id", extCorpId)
                .in("id", request.getIds())
                .isNull("deleted_at")
                .set("deleted_at", LocalDateTime.now()));
    }

    public void sendRemainder(Map<String, Object> payload) {
        Long id = longValue(payload.get("id"));
        String extCorpId = string(payload.get("extCorpId"));
        String customerName = string(payload.get("customerName"));
        if (id == null || !StringUtils.hasText(extCorpId)) {
            return;
        }
        CustomerEventPo event = customerEventMapper.selectById(id);
        if (event == null || event.getDeletedAt() != null || !extCorpId.equals(event.getExtCorpId())) {
            return;
        }
        String content = String.format(REMAINDER_CONTENT,
                event.getExtStaffId(),
                StringUtils.hasText(customerName) ? customerName : event.getExtCustomerId(),
                event.getContent());
        sendText(extCorpId, event.getExtStaffId(), content);
    }

    private void validateCreate(RemainderRequest request) {
        if (request == null
                || request.getSendAt() == null
                || !StringUtils.hasText(request.getCustomerName())
                || !StringUtils.hasText(request.getContent())
                || !StringUtils.hasText(request.getExtStaffId())
                || !StringUtils.hasText(request.getExtCustomerId())) {
            throw new BizException(ErrorCode.BAD_REQUEST);
        }
    }

    private CustomerEventPo requireEvent(Long id, String extCorpId) {
        CustomerEventPo event = customerEventMapper.selectById(id);
        if (event == null || event.getDeletedAt() != null || !extCorpId.equals(event.getExtCorpId())) {
            throw new BizException(ErrorCode.ITEM_NOT_FOUND);
        }
        return event;
    }

    private void publishRemainder(Long id, String extCorpId, String customerName, LocalDateTime sendAt) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("topic", DelayedJobTopics.REMAINDER);
        payload.put("id", id);
        payload.put("extCorpId", extCorpId);
        payload.put("customerName", customerName);
        delayedJobPublisher.publish(payload, delayUntil(sendAt));
    }

    private Duration delayUntil(LocalDateTime sendAt) {
        Duration delay = Duration.between(LocalDateTime.now(), sendAt);
        return delay.isNegative() ? Duration.ZERO : delay;
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

    private String string(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private Long longValue(Object value) {
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        try {
            return value == null ? null : Long.valueOf(String.valueOf(value));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
