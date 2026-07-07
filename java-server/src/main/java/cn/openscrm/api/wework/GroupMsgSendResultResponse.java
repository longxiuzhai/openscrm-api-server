package cn.openscrm.api.wework;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GroupMsgSendResultResponse extends CommonResponse {

    @JsonProperty("next_cursor")
    private String nextCursor;

    @JsonProperty("send_list")
    private List<SendResult> sendList = new ArrayList<>();

    @Getter
    @Setter
    public static class SendResult {
        @JsonProperty("chat_id")
        private String chatId;

        @JsonProperty("external_userid")
        private String externalUserId;

        @JsonProperty("send_time")
        private Integer sendTime;

        private Integer status;

        @JsonProperty("userid")
        private String userId;
    }
}
