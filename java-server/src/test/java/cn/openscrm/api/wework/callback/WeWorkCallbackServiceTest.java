package cn.openscrm.api.wework.callback;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import cn.openscrm.api.config.OpenScrmProperties;
import cn.openscrm.api.staff.service.StaffService;
import java.util.Collections;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class WeWorkCallbackServiceTest {

    @Test
    void staffRegistrarRegistersCreateUpdateAndDeleteUserCallbacks() {
        OpenScrmProperties properties = new OpenScrmProperties();
        properties.getWeWork().setExtCorpId("ww-test");
        StaffCallbackRegistrar registrar = new StaffCallbackRegistrar(mock(StaffService.class), properties);
        WeWorkCallbackService service = new WeWorkCallbackService(
                mock(WeWorkCallbackCrypto.class),
                Collections.singletonList(registrar));

        service.registerDefaultHandlers();

        @SuppressWarnings("unchecked")
        Map<WeWorkCallbackEventKey, WeWorkCallbackHandler> handlers =
                (Map<WeWorkCallbackEventKey, WeWorkCallbackHandler>) ReflectionTestUtils.getField(service, "handlers");
        assertThat(handlers).containsKeys(
                new WeWorkCallbackEventKey("event", "change_contact", "create_user"),
                new WeWorkCallbackEventKey("event", "change_contact", "update_user"),
                new WeWorkCallbackEventKey("event", "change_contact", "delete_user"));
    }
}
