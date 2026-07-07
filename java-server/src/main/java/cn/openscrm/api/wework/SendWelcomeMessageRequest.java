package cn.openscrm.api.wework;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class SendWelcomeMessageRequest {

    @JsonProperty("welcome_code")
    private String welcomeCode;

    private Text text;

    private List<JsonNode> attachments = new ArrayList<>();

    @Getter
    @Setter
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public static class Text {
        private String content;
    }
}
