package cn.openscrm.api.groupchatautojoin.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GroupChatAutoJoinStaffRequest {

    @JsonProperty("staff_id")
    private String extStaffId;

    @JsonProperty("daily_add_customer_limit")
    private Long dailyAddCustomerLimit;
}
