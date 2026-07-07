package cn.openscrm.api.wework;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class UserIdListResponse extends CommonResponse {

    @JsonProperty("next_cursor")
    private String nextCursor;

    @JsonProperty("dept_user")
    private List<UserIdInfo> deptUser = new ArrayList<>();
}
