package cn.openscrm.api.welcome.dto;

import cn.openscrm.api.persistence.entity.WelcomeMsgPo;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WelcomeMsgResponse {

    @JsonUnwrapped
    private WelcomeMsgPo welcomeMsg;

    private List<DepartmentMainInfo> department = new ArrayList<>();

    private List<StaffMainInfo> staffs = new ArrayList<>();

    @JsonProperty("time_period_msg")
    private List<WelcomeMsgPo> timePeriodMsg = new ArrayList<>();
}
