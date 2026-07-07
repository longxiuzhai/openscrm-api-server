package cn.openscrm.api.stafffrontend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JsConfigResponse {

    private Long timestamp;

    private String signature;

    @JsonProperty("nonce_str")
    private String nonceStr;

    @JsonProperty("app_id")
    private String appId;

    private String url;
}
