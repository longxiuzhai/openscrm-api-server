package cn.openscrm.api.stafffrontend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JsAgentConfigResponse {

    @JsonProperty("corp_id")
    private String corpId;

    @JsonProperty("agent_id")
    private Long agentId;

    private Long timestamp;

    @JsonProperty("nonce_str")
    private String nonceStr;

    private String signature;

    private String url;
}
