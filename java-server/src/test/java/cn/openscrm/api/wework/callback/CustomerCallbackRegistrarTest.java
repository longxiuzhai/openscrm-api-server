package cn.openscrm.api.wework.callback;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cn.openscrm.api.common.id.SnowflakeIdGenerator;
import cn.openscrm.api.common.mq.DelayedJobPublisher;
import cn.openscrm.api.persistence.entity.CustomerEventPo;
import cn.openscrm.api.persistence.mapper.CustomerEventPoMapper;
import cn.openscrm.api.persistence.mapper.CustomerStaffPoMapper;
import cn.openscrm.api.persistence.mapper.CustomerStaffRelationHistoryPoMapper;
import cn.openscrm.api.persistence.mapper.CustomerStatisticPoMapper;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

class CustomerCallbackRegistrarTest {

    @Test
    void registersHalfAddAndTransferFailCallbacks() {
        CustomerCallbackRegistrar registrar = registrar(mock(CustomerEventPoMapper.class));
        WeWorkCallbackService service = new WeWorkCallbackService(
                mock(WeWorkCallbackCrypto.class), Collections.singletonList(registrar));

        service.registerDefaultHandlers();

        @SuppressWarnings("unchecked")
        Map<WeWorkCallbackEventKey, WeWorkCallbackHandler> handlers =
                (Map<WeWorkCallbackEventKey, WeWorkCallbackHandler>) ReflectionTestUtils.getField(service, "handlers");
        assertThat(handlers).containsKeys(
                new WeWorkCallbackEventKey("event", "change_external_contact", "add_half_external_contact"),
                new WeWorkCallbackEventKey("event", "change_external_contact", "transfer_fail"));
    }

    @Test
    void transferFailCreatesCustomerEvent() {
        CustomerEventPoMapper eventMapper = mock(CustomerEventPoMapper.class);
        CustomerCallbackRegistrar registrar = registrar(eventMapper);

        WeWorkCallbackMessage message = new WeWorkCallbackMessage();
        message.setToUserName("ww-corp");
        message.setChangeType("transfer_fail");
        Map<String, String> fields = new HashMap<>();
        fields.put("UserID", "staff-a");
        fields.put("ExternalUserID", "customer-a");
        fields.put("FailReason", "customer_refused");
        message.setFields(fields);

        registrar.transferFail(message);

        ArgumentCaptor<CustomerEventPo> eventCaptor = ArgumentCaptor.forClass(CustomerEventPo.class);
        verify(eventMapper).insert(eventCaptor.capture());
        CustomerEventPo event = eventCaptor.getValue();
        assertThat(event.getExtCorpId()).isEqualTo("ww-corp");
        assertThat(event.getExtStaffId()).isEqualTo("staff-a");
        assertThat(event.getExtCustomerId()).isEqualTo("customer-a");
        assertThat(event.getEventName()).isEqualTo("transfer_fail");
        assertThat(event.getContent()).contains("customer_refused");
    }

    private CustomerCallbackRegistrar registrar(CustomerEventPoMapper eventMapper) {
        SnowflakeIdGenerator idGenerator = mock(SnowflakeIdGenerator.class);
        when(idGenerator.nextId()).thenReturn(10001L);
        CustomerStatisticPoMapper statisticMapper = mock(CustomerStatisticPoMapper.class);
        when(statisticMapper.selectCount(any())).thenReturn(0L);
        CustomerStaffPoMapper customerStaffMapper = mock(CustomerStaffPoMapper.class);
        when(customerStaffMapper.selectCount(any())).thenReturn(0L);
        CustomerStaffRelationHistoryPoMapper historyMapper = mock(CustomerStaffRelationHistoryPoMapper.class);
        DelayedJobPublisher publisher = mock(DelayedJobPublisher.class);
        DeleteCustomerNotificationService notificationService = mock(DeleteCustomerNotificationService.class);
        return new CustomerCallbackRegistrar(
                eventMapper,
                statisticMapper,
                customerStaffMapper,
                historyMapper,
                publisher,
                idGenerator,
                notificationService);
    }
}
