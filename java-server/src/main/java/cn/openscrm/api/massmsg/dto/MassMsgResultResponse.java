package cn.openscrm.api.massmsg.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MassMsgResultResponse {

    @JsonProperty("mission_id")
    private final Long missionId;

    @JsonProperty("mission_status")
    private final Integer missionStatus;

    @JsonProperty("delivered_num")
    private final Integer deliveredNum;

    @JsonProperty("success_num")
    private final Integer successNum;

    @JsonProperty("undelivered_num")
    private final Integer unDeliveredNum;

    @JsonProperty("failed_num")
    private final Integer failedNum;
}
