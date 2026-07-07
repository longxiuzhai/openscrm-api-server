package cn.openscrm.api.wework.callback;

import cn.openscrm.api.tag.service.TagSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Slf4j
@Component
@RequiredArgsConstructor
public class TagCallbackRegistrar implements WeWorkCallbackRegistrar {

    private static final String TAG = "tag";
    private static final String TAG_GROUP = "tag_group";

    private final TagSyncService tagSyncService;

    @Override
    public void register(WeWorkCallbackService service) {
        service.register("event", "change_external_tag", "create", this::createTag);
        service.register("event", "change_external_tag", "update", this::updateTag);
        service.register("event", "change_external_tag", "delete", this::deleteTag);
    }

    private void createTag(WeWorkCallbackMessage message) {
        tagSyncService.syncAll(message.getToUserName());
    }

    private void updateTag(WeWorkCallbackMessage message) {
        String id = message.getFields().get("Id");
        String tagType = message.getFields().get("TagType");
        if (!StringUtils.hasText(id)) {
            log.warn("missing tag id in update callback fields={}", message.getFields());
            return;
        }
        if (TAG.equals(tagType)) {
            tagSyncService.syncTag(message.getToUserName(), id);
        } else if (TAG_GROUP.equals(tagType)) {
            tagSyncService.syncAll(message.getToUserName());
        } else {
            log.warn("unknown tag type in update callback tagType={}, fields={}", tagType, message.getFields());
        }
    }

    private void deleteTag(WeWorkCallbackMessage message) {
        String id = message.getFields().get("Id");
        String tagType = message.getFields().get("TagType");
        if (!StringUtils.hasText(id)) {
            log.warn("missing tag id in delete callback fields={}", message.getFields());
            return;
        }
        if (TAG.equals(tagType)) {
            tagSyncService.deleteTag(id);
        } else if (TAG_GROUP.equals(tagType)) {
            tagSyncService.deleteTagGroup(id);
        } else {
            log.warn("unknown tag type in delete callback tagType={}, fields={}", tagType, message.getFields());
        }
    }
}
