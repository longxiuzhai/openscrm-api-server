package cn.openscrm.api.wework;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AddMsgTemplateRequest {

    @JsonProperty("chat_type")
    private String chatType;

    @JsonProperty("external_userid")
    private List<String> externalUserId;

    private String sender;

    private Text text;

    private List<JsonNode> attachments;

    @Getter
    @Setter
    public static class Text {
        private String content;
    }
}
