package cn.openscrm.api.wework;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserIdInfo {

    @JsonProperty("userid")
    private String userId;

    @JsonProperty("department")
    private Long departmentId;
}
