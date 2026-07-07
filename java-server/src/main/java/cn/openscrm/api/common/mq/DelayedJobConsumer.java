package cn.openscrm.api.common.mq;

import cn.openscrm.api.config.RabbitMqConfig;
import cn.openscrm.api.contactway.service.ContactWayService;
import cn.openscrm.api.customer.service.CustomerSyncService;
import cn.openscrm.api.massmsg.service.MassMsgService;
import cn.openscrm.api.remainder.service.RemainderService;
import cn.openscrm.api.wework.callback.DeleteCustomerNotificationService;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DelayedJobConsumer {

    private final ContactWayService contactWayService;
    private final CustomerSyncService customerSyncService;
    private final MassMsgService massMsgService;
    private final DeleteCustomerNotificationService deleteCustomerNotificationService;
    private final RemainderService remainderService;

    public DelayedJobConsumer(ContactWayService contactWayService,
                              CustomerSyncService customerSyncService,
                              MassMsgService massMsgService,
                              DeleteCustomerNotificationService deleteCustomerNotificationService,
                              RemainderService remainderService) {
        this.contactWayService = contactWayService;
        this.customerSyncService = customerSyncService;
        this.massMsgService = massMsgService;
        this.deleteCustomerNotificationService = deleteCustomerNotificationService;
        this.remainderService = remainderService;
    }

    @RabbitListener(queues = RabbitMqConfig.DELAYED_JOB_QUEUE)
    public void consume(Map<String, Object> payload) {
        String topic = string(payload.get("topic"));
        if (DelayedJobTopics.REFRESH_CONTACT_WAY.equals(topic)) {
            contactWayService.refresh(longValue(payload.get("id")), string(payload.get("extCorpId")));
            return;
        }
        if (DelayedJobTopics.SYNC_CUSTOMER_DATA.equals(topic)) {
            customerSyncService.syncSingle(
                    string(payload.get("extStaffId")),
                    string(payload.get("extCustomerId")),
                    string(payload.get("welcomeCode")),
                    string(payload.get("state")));
            return;
        }
        if (DelayedJobTopics.MASS_MSG.equals(topic)) {
            massMsgService.sendMassMsgToWeWork(longValue(payload.get("id")), string(payload.get("extCorpId")));
            return;
        }
        if (DelayedJobTopics.GROUP_CHAT_MASS_MSG.equals(topic)) {
            massMsgService.sendGroupChatMassMsgToWeWork(longValue(payload.get("id")), string(payload.get("extCorpId")));
            return;
        }
        if (DelayedJobTopics.DELETE_CUSTOMER_ADMIN_NOTIFY.equals(topic)) {
            deleteCustomerNotificationService.sendTimedAdminNotification(payload);
            return;
        }
        if (DelayedJobTopics.REMAINDER.equals(topic)) {
            remainderService.sendRemainder(payload);
            return;
        }
        log.warn("unknown delayed job topic={}, payload={}", topic, payload);
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
