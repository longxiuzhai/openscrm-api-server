package cn.openscrm.api.welcome.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WelcomeMsgRequest {

    private String name;

    @JsonProperty("welcome_msg")
    private JsonNode welcomeMsg;

    @JsonProperty("ext_department_ids")
    private List<Integer> extDepartmentIds;

    @JsonProperty("ext_staff_ids")
    private List<String> extStaffIds;

    @JsonProperty("enable_time_period_msg")
    private Integer enableTimePeriodMsg;

    @JsonProperty("time_period_msg")
    private List<TimePeriodWelcomeMsgRequest> timePeriodMsg;
}
