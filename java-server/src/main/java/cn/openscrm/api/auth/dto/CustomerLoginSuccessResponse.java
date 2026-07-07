package cn.openscrm.api.auth.dto;

import cn.openscrm.api.persistence.entity.CustomerPo;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CustomerLoginSuccessResponse {

    private CustomerPo customer;

    @JsonProperty("access_token")
    private String accessToken;
}
