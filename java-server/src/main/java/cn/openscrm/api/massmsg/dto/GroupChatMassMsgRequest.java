package cn.openscrm.api.massmsg.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GroupChatMassMsgRequest {

    @JsonProperty("ext_staff_ids")
    private List<String> extStaffIds;

    @JsonProperty("send_type")
    private Integer sendType;

    @JsonProperty("send_at")
    private Long sendAt;

    private JsonNode msg;
}
