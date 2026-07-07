package cn.openscrm.api.msgarch.dto;

import cn.openscrm.api.persistence.entity.ChatMsgContentPo;
import cn.openscrm.api.persistence.entity.ChatMsgPo;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ChatMessageResponse {

    private final ChatMsgPo chatMsg;
    private final ChatMsgContentPo chatMsgContent;
    private final String senderName;
    private final String senderAvatar;
    private final String toUserName;
    private final String toUserAvatar;
}
