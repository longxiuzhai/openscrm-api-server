package cn.openscrm.api.wework.callback;

import cn.openscrm.api.groupchat.service.GroupChatSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Slf4j
@Component
@RequiredArgsConstructor
public class GroupChatCallbackRegistrar implements WeWorkCallbackRegistrar {

    private static final String UPDATE_ADD_MEMBER = "add_member";

    private final GroupChatSyncService groupChatSyncService;

    @Override
    public void register(WeWorkCallbackService service) {
        service.register("event", "change_external_chat", "create", this::createGroupChat);
        service.register("event", "change_external_chat", "update", this::updateGroupChat);
        service.register("event", "change_external_chat", "dismiss", this::dismissGroupChat);
    }

    private void createGroupChat(WeWorkCallbackMessage message) {
        String chatId = chatId(message);
        if (!StringUtils.hasText(chatId)) {
            log.warn("missing chat id in group chat create callback fields={}", message.getFields());
            return;
        }
        groupChatSyncService.syncOne(message.getToUserName(), chatId, UPDATE_ADD_MEMBER, null);
    }

    private void updateGroupChat(WeWorkCallbackMessage message) {
        String chatId = chatId(message);
        if (!StringUtils.hasText(chatId)) {
            log.warn("missing chat id in group chat update callback fields={}", message.getFields());
            return;
        }
        groupChatSyncService.syncOne(
                message.getToUserName(),
                chatId,
                message.getFields().get("UpdateDetail"),
                parseLong(message.getFields().get("MemChangeCnt")));
    }

    private void dismissGroupChat(WeWorkCallbackMessage message) {
        String chatId = chatId(message);
        if (!StringUtils.hasText(chatId)) {
            log.warn("missing chat id in group chat dismiss callback fields={}", message.getFields());
            return;
        }
        groupChatSyncService.dismiss(message.getToUserName(), chatId);
    }

    private String chatId(WeWorkCallbackMessage message) {
        String chatId = message.getFields().get("ChatId");
        return StringUtils.hasText(chatId) ? chatId : message.getFields().get("ChatID");
    }

    private Long parseLong(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
