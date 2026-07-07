package cn.openscrm.api.wework;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserIdentityResponse extends CommonResponse {

    @JsonProperty("UserId")
    private String userId;

    @JsonProperty("OpenId")
    private String openId;

    @JsonProperty("DeviceId")
    private String deviceId;

    @JsonProperty("external_userid")
    private String externalUserId;
}
