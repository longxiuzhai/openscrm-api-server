package cn.openscrm.api.wework;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GroupChatGetResponse extends CommonResponse {

    @JsonProperty("group_chat")
    private GroupChatInfo groupChat;
}
