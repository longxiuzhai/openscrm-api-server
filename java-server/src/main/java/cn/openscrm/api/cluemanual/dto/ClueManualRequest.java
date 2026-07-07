package cn.openscrm.api.cluemanual.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClueManualRequest {

    @JsonProperty("ext_staff_id")
    private String extStaffId;

    @JsonProperty("ext_customer_id")
    private String extCustomerId;

    private String content;
}
