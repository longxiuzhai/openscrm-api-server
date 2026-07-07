package cn.openscrm.api.wework.callback;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import cn.openscrm.api.config.OpenScrmProperties;
import cn.openscrm.api.staff.service.StaffService;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class MsgAuditCallbackRegistrarTest {

    @Test
    void registersMsgAuditApprovedCallback() {
        MsgAuditCallbackRegistrar registrar = new MsgAuditCallbackRegistrar(
                mock(StaffService.class), new OpenScrmProperties());
        WeWorkCallbackService service = new WeWorkCallbackService(
                mock(WeWorkCallbackCrypto.class), Collections.singletonList(registrar));

        service.registerDefaultHandlers();

        @SuppressWarnings("unchecked")
        Map<WeWorkCallbackEventKey, WeWorkCallbackHandler> handlers =
                (Map<WeWorkCallbackEventKey, WeWorkCallbackHandler>) ReflectionTestUtils.getField(service, "handlers");
        assertThat(handlers).containsKey(
                new WeWorkCallbackEventKey("event", "change_external_contact", "msg_audit_approved"));
    }

    @Test
    void refreshesStaffMsgArchStatusWhenApproved() {
        StaffService staffService = mock(StaffService.class);
        MsgAuditCallbackRegistrar registrar = new MsgAuditCallbackRegistrar(staffService, new OpenScrmProperties());
        WeWorkCallbackService service = new WeWorkCallbackService(
                mock(WeWorkCallbackCrypto.class), Collections.singletonList(registrar));
        service.registerDefaultHandlers();
        @SuppressWarnings("unchecked")
        Map<WeWorkCallbackEventKey, WeWorkCallbackHandler> handlers =
                (Map<WeWorkCallbackEventKey, WeWorkCallbackHandler>) ReflectionTestUtils.getField(service, "handlers");

        WeWorkCallbackMessage message = new WeWorkCallbackMessage();
        message.setToUserName("ww-corp");
        message.setMsgType("event");
        message.setEvent("change_external_contact");
        message.setChangeType("msg_audit_approved");
        Map<String, String> fields = new HashMap<>();
        fields.put("UserID", "staff-a");
        fields.put("ExternalUserID", "customer-a");
        message.setFields(fields);

        handlers.get(new WeWorkCallbackEventKey("event", "change_external_contact", "msg_audit_approved"))
                .handle(message);

        verify(staffService).updateMsgArchStatus("ww-corp");
    }
}
