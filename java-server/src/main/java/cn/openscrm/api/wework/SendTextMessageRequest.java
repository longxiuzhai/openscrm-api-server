package cn.openscrm.api.wework;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SendTextMessageRequest {

    private String touser;

    private String msgtype = "text";

    private Long agentid;

    private Text text;

    private Integer safe = 0;

    @JsonProperty("enable_id_trans")
    private Integer enableIdTrans = 0;

    @JsonProperty("enable_duplicate_check")
    private Integer enableDuplicateCheck = 0;

    @Getter
    @Setter
    public static class Text {
        private String content;
    }
}
