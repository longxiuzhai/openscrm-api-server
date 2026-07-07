package cn.openscrm.api.groupchatwelcome.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import javax.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GroupChatWelcomeMsgRequest {

    private String content;

    @Size(max = 64)
    @JsonProperty("attachment_type")
    private String attachmentType;

    private JsonNode attachment;

    @JsonProperty("notify_staffs_enable")
    private Boolean notifyStaffsEnable;
}
