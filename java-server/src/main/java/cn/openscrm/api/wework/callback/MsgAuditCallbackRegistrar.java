package cn.openscrm.api.wework.callback;

import cn.openscrm.api.config.OpenScrmProperties;
import cn.openscrm.api.staff.service.StaffService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Slf4j
@Component
@RequiredArgsConstructor
public class MsgAuditCallbackRegistrar implements WeWorkCallbackRegistrar {

    private final StaffService staffService;
    private final OpenScrmProperties properties;

    @Override
    public void register(WeWorkCallbackService service) {
        service.register("event", "change_external_contact", "msg_audit_approved", this::msgAuditApproved);
    }

    private void msgAuditApproved(WeWorkCallbackMessage message) {
        String extCorpId = StringUtils.hasText(message.getToUserName())
                ? message.getToUserName()
                : properties.getWeWork().getExtCorpId();
        log.info("received msg_audit_approved callback extCorpId={}, userId={}, externalUserId={}",
                extCorpId, message.getFields().get("UserID"), message.getFields().get("ExternalUserID"));
        staffService.updateMsgArchStatus(extCorpId);
    }
}
