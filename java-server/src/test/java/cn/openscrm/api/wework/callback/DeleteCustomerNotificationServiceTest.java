package cn.openscrm.api.wework.callback;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class DeleteCustomerNotificationServiceTest {

    @Test
    void notifyStaffDeleteCustomerSendsRealtimeAdminMessage() {
        EventNotifyPoMapper eventNotifyMapper = mock(EventNotifyPoMapper.class);
        StaffPoMapper staffMapper = mock(StaffPoMapper.class);
        CustomerPoMapper customerMapper = mock(CustomerPoMapper.class);
        WeWorkClient weWorkClient = mock(WeWorkClient.class);
        DelayedJobPublisher delayedJobPublisher = mock(DelayedJobPublisher.class);
        OpenScrmProperties properties = properties();
        DeleteCustomerNotificationService service = new DeleteCustomerNotificationService(
                eventNotifyMapper, staffMapper, customerMapper, weWorkClient, properties, new ObjectMapper(),
                delayedJobPublisher);

        EventNotifyPo rule = new EventNotifyPo();
        rule.setIsNotifyAdmins(1);
        rule.setNotifyType(1);
        rule.setExtStaffIds("[\"admin-a\",\"admin-b\"]");
        StaffPo staff = new StaffPo();
        staff.setName("张三");
        CustomerPo customer = new CustomerPo();
        customer.setName("李四");
        when(eventNotifyMapper.selectOne(any())).thenReturn(rule);
        when(staffMapper.selectOne(any())).thenReturn(staff);
        when(customerMapper.selectOne(any())).thenReturn(customer);

        String content = service.notifyStaffDeleteCustomer("ww-corp", "staff-a", "customer-a");

        assertThat(content).isEqualTo("员工 [张三] 删除了客户 [李四] ");
        ArgumentCaptor<SendTextMessageRequest> captor = ArgumentCaptor.forClass(SendTextMessageRequest.class);
        verify(weWorkClient).sendTextMessage(eq("ww-corp"), eq("main-secret"), captor.capture());
        assertThat(captor.getValue().getTouser()).isEqualTo("admin-a|admin-b");
        assertThat(captor.getValue().getAgentid()).isEqualTo(10001L);
        assertThat(captor.getValue().getText().getContent()).isEqualTo(content);
        verify(delayedJobPublisher, never()).publish(any(), any());
    }

    @Test
    void notifyStaffDeleteCustomerDoesNotSendWhenAdminNotifyIsDisabled() {
        EventNotifyPoMapper eventNotifyMapper = mock(EventNotifyPoMapper.class);
        StaffPoMapper staffMapper = mock(StaffPoMapper.class);
        CustomerPoMapper customerMapper = mock(CustomerPoMapper.class);
        WeWorkClient weWorkClient = mock(WeWorkClient.class);
        DelayedJobPublisher delayedJobPublisher = mock(DelayedJobPublisher.class);
        DeleteCustomerNotificationService service = new DeleteCustomerNotificationService(
                eventNotifyMapper, staffMapper, customerMapper, weWorkClient, properties(), new ObjectMapper(),
                delayedJobPublisher);

        EventNotifyPo rule = new EventNotifyPo();
        rule.setIsNotifyAdmins(2);
        StaffPo staff = new StaffPo();
        staff.setName("张三");
        CustomerPo customer = new CustomerPo();
        customer.setName("李四");
        when(eventNotifyMapper.selectOne(any())).thenReturn(rule);
        when(staffMapper.selectOne(any())).thenReturn(staff);
        when(customerMapper.selectOne(any())).thenReturn(customer);

        String content = service.notifyStaffDeleteCustomer("ww-corp", "staff-a", "customer-a");

        assertThat(content).isEqualTo("员工 [张三] 删除了客户 [李四] ");
        verify(weWorkClient, never()).sendTextMessage(any(), any(), any());
        verify(delayedJobPublisher, never()).publish(any(), any());
    }

    @Test
    void notifyStaffDeleteCustomerPublishesTimedAdminMessage() {
        EventNotifyPoMapper eventNotifyMapper = mock(EventNotifyPoMapper.class);
        StaffPoMapper staffMapper = mock(StaffPoMapper.class);
        CustomerPoMapper customerMapper = mock(CustomerPoMapper.class);
        WeWorkClient weWorkClient = mock(WeWorkClient.class);
        DelayedJobPublisher delayedJobPublisher = mock(DelayedJobPublisher.class);
        DeleteCustomerNotificationService service = new DeleteCustomerNotificationService(
                eventNotifyMapper, staffMapper, customerMapper, weWorkClient, properties(), new ObjectMapper(),
                delayedJobPublisher);

        EventNotifyPo rule = new EventNotifyPo();
        rule.setIsNotifyAdmins(1);
        rule.setNotifyType(2);
        rule.setExtStaffIds("[\"admin-a\"]");
        StaffPo staff = new StaffPo();
        staff.setName("张三");
        CustomerPo customer = new CustomerPo();
        customer.setName("李四");
        when(eventNotifyMapper.selectOne(any())).thenReturn(rule);
        when(staffMapper.selectOne(any())).thenReturn(staff);
        when(customerMapper.selectOne(any())).thenReturn(customer);

        String content = service.notifyStaffDeleteCustomer("ww-corp", "staff-a", "customer-a");

        assertThat(content).isEqualTo("员工 [张三] 删除了客户 [李四] ");
        verify(weWorkClient, never()).sendTextMessage(any(), any(), any());
        ArgumentCaptor<Object> payloadCaptor = ArgumentCaptor.forClass(Object.class);
        verify(delayedJobPublisher).publish(payloadCaptor.capture(), any(Duration.class));
        @SuppressWarnings("unchecked")
        Map<String, Object> payload = (Map<String, Object>) payloadCaptor.getValue();
        assertThat(payload).containsEntry("topic", DelayedJobTopics.DELETE_CUSTOMER_ADMIN_NOTIFY);
        assertThat(payload).containsEntry("extCorpId", "ww-corp");
        assertThat(payload).containsEntry("content", content);
        assertThat(payload.get("adminIds")).asList().containsExactly("admin-a");
    }

    @Test
    void delayUntilNextEightUsesTodayWhenBeforeEightAndTomorrowOtherwise() {
        DeleteCustomerNotificationService service = new DeleteCustomerNotificationService(
                mock(EventNotifyPoMapper.class),
                mock(StaffPoMapper.class),
                mock(CustomerPoMapper.class),
                mock(WeWorkClient.class),
                properties(),
                new ObjectMapper(),
                mock(DelayedJobPublisher.class));

        assertThat(service.delayUntilNextEight(LocalDateTime.of(2026, 7, 5, 7, 30)))
                .isEqualTo(Duration.ofMinutes(30));
        assertThat(service.delayUntilNextEight(LocalDateTime.of(2026, 7, 5, 8, 0)))
                .isEqualTo(Duration.ofHours(24));
    }

    private OpenScrmProperties properties() {
        OpenScrmProperties properties = new OpenScrmProperties();
        properties.getWeWork().setMainAgentId(10001L);
        properties.getWeWork().setMainAgentSecret("main-secret");
        return properties;
    }
}
