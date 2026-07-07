package cn.openscrm.api.msgarch.dto;

import cn.openscrm.api.persistence.entity.ChatMsgPo;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ChatSessionResponse {

    private final ChatMsgPo lastMsg;
    private final String peerExtId;
    private final String peerName;
    private final String peerAvatar;
    private final String groupChatName;
}
