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
public class StaffCallbackRegistrar implements WeWorkCallbackRegistrar {

    private final StaffService staffService;
    private final OpenScrmProperties properties;

    @Override
    public void register(WeWorkCallbackService service) {
        service.register("event", "change_contact", "create_user", this::createStaff);
        service.register("event", "change_contact", "update_user", this::updateStaff);
        service.register("event", "change_contact", "delete_user", this::deleteStaff);
    }

    private void createStaff(WeWorkCallbackMessage message) {
        syncStaff(message, "create_user");
    }

    private void updateStaff(WeWorkCallbackMessage message) {
        syncStaff(message, "update_user");
    }

    private void syncStaff(WeWorkCallbackMessage message, String changeType) {
        String extCorpId = StringUtils.hasText(message.getToUserName())
                ? message.getToUserName()
                : properties.getWeWork().getExtCorpId();
        String userId = message.getFields().get("UserID");
        if (!StringUtils.hasText(userId)) {
            log.warn("missing UserID in {} callback fields={}", changeType, message.getFields());
            staffService.sync(extCorpId);
            return;
        }
        log.info("sync staff after {} callback userId={}", changeType, userId);
        staffService.syncOne(extCorpId, userId);
    }

    private void deleteStaff(WeWorkCallbackMessage message) {
        String extCorpId = StringUtils.hasText(message.getToUserName())
                ? message.getToUserName()
                : properties.getWeWork().getExtCorpId();
        String userId = message.getFields().get("UserID");
        if (!StringUtils.hasText(userId)) {
            log.warn("missing UserID in delete_user callback fields={}", message.getFields());
            return;
        }
        log.info("delete staff after delete_user callback userId={}", userId);
        staffService.deleteOne(extCorpId, userId);
    }
}
