package cn.openscrm.api.quickreply.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuickReplyDetailRequest {

    private Long id;

    @JsonProperty("content_type")
    private Integer contentType;

    @JsonProperty("quick_reply_content")
    private JsonNode quickReplyContent;
}
