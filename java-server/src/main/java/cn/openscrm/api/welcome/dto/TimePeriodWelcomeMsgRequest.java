package cn.openscrm.api.welcome.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TimePeriodWelcomeMsgRequest {

    private Long id;

    private JsonNode attachments;

    @JsonProperty("effective_at")
    private List<Long> effectiveAt;

    @JsonProperty("start_time")
    private Long startTime;

    @JsonProperty("end_time")
    private Long endTime;
}
