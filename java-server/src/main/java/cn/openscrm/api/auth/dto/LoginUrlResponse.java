package cn.openscrm.api.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginUrlResponse {

    @JsonProperty("app_id")
    private String appId;

    @JsonProperty("agent_id")
    private Long agentId;

    @JsonProperty("redirect_uri")
    private String redirectUri;

    @JsonProperty("source_url")
    private String sourceUrl;

    private String state;

    @JsonProperty("location_url")
    private String locationUrl;
}
