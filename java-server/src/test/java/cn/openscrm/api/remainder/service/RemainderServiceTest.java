package cn.openscrm.api.remainder.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cn.openscrm.api.common.id.SnowflakeIdGenerator;
import cn.openscrm.api.common.mq.DelayedJobPublisher;
import cn.openscrm.api.common.mq.DelayedJobTopics;
import cn.openscrm.api.config.OpenScrmProperties;
import cn.openscrm.api.persistence.entity.CustomerEventPo;
import cn.openscrm.api.persistence.mapper.CustomerEventPoMapper;
import cn.openscrm.api.remainder.dto.RemainderRequest;
import cn.openscrm.api.wework.SendTextMessageRequest;
import cn.openscrm.api.wework.WeWorkClient;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class RemainderServiceTest {

    @Test
    void createStoresCustomerEventAndPublishesDelayedJob() {
        CustomerEventPoMapper eventMapper = mock(CustomerEventPoMapper.class);
        DelayedJobPublisher publisher = mock(DelayedJobPublisher.class);
        RemainderService service = service(eventMapper, publisher, mock(WeWorkClient.class));

        RemainderRequest request = new RemainderRequest();
        request.setSendAt(LocalDateTime.now().plusMinutes(5));
        request.setCustomerName("李四");
        request.setContent("回访");
        request.setExtStaffId("staff-a");
        request.setExtCustomerId("customer-a");

        CustomerEventPo event = service.create(request, "ww-corp", "admin-a");

        assertThat(event.getId()).isEqualTo(10001L);
        assertThat(event.getEventType()).isEqualTo("reminder_event");
        verify(eventMapper).insert(event);
        ArgumentCaptor<Object> payloadCaptor = ArgumentCaptor.forClass(Object.class);
        verify(publisher).publish(payloadCaptor.capture(), any(Duration.class));
        @SuppressWarnings("unchecked")
        Map<String, Object> payload = (Map<String, Object>) payloadCaptor.getValue();
        assertThat(payload).containsEntry("topic", DelayedJobTopics.REMAINDER);
        assertThat(payload).containsEntry("id", 10001L);
        assertThat(payload).containsEntry("customerName", "李四");
    }

    @Test
    void sendRemainderSendsCurrentEventContent() {
        CustomerEventPoMapper eventMapper = mock(CustomerEventPoMapper.class);
        WeWorkClient weWorkClient = mock(WeWorkClient.class);
        RemainderService service = service(eventMapper, mock(DelayedJobPublisher.class), weWorkClient);
        CustomerEventPo event = new CustomerEventPo();
        event.setId(10001L);
        event.setExtCorpId("ww-corp");
        event.setExtStaffId("staff-a");
        event.setExtCustomerId("customer-a");
        event.setContent("现在联系");
        when(eventMapper.selectById(10001L)).thenReturn(event);

        service.sendRemainder(payload());

        ArgumentCaptor<SendTextMessageRequest> requestCaptor = ArgumentCaptor.forClass(SendTextMessageRequest.class);
        verify(weWorkClient).sendTextMessage(eq("ww-corp"), eq("main-secret"), requestCaptor.capture());
        assertThat(requestCaptor.getValue().getTouser()).isEqualTo("staff-a");
        assertThat(requestCaptor.getValue().getText().getContent()).contains("李四", "现在联系");
    }

    @Test
    void sendRemainderSkipsDeletedEvent() {
        CustomerEventPoMapper eventMapper = mock(CustomerEventPoMapper.class);
        WeWorkClient weWorkClient = mock(WeWorkClient.class);
        RemainderService service = service(eventMapper, mock(DelayedJobPublisher.class), weWorkClient);
        CustomerEventPo event = new CustomerEventPo();
        event.setExtCorpId("ww-corp");
        event.setDeletedAt(LocalDateTime.now());
        when(eventMapper.selectById(10001L)).thenReturn(event);

        service.sendRemainder(payload());

        verify(weWorkClient, never()).sendTextMessage(any(), any(), any());
    }

    private RemainderService service(CustomerEventPoMapper eventMapper,
                                     DelayedJobPublisher publisher,
                                     WeWorkClient weWorkClient) {
        SnowflakeIdGenerator idGenerator = mock(SnowflakeIdGenerator.class);
        when(idGenerator.nextId()).thenReturn(10001L);
        OpenScrmProperties properties = new OpenScrmProperties();
        properties.getWeWork().setMainAgentId(100L);
        properties.getWeWork().setMainAgentSecret("main-secret");
        return new RemainderService(eventMapper, publisher, idGenerator, weWorkClient, properties);
    }

    private Map<String, Object> payload() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("id", 10001L);
        payload.put("extCorpId", "ww-corp");
        payload.put("customerName", "李四");
        return payload;
    }
}
