package cn.openscrm.api.wework;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GroupMsgSendResultRequest {

    private String cursor;

    private Integer limit;

    @JsonProperty("msgid")
    private String msgId;

    @JsonProperty("userid")
    private String userId;
}
