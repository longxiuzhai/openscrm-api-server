package cn.openscrm.api.auth.dto;

import cn.openscrm.api.persistence.entity.StaffPo;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginSuccessResponse {

    private StaffPo staff;

    @JsonProperty("access_token")
    private String accessToken;
}
