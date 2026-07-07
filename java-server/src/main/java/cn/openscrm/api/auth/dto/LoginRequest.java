package cn.openscrm.api.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {

    @JsonProperty("ext_corp_id")
    private String extCorpId;

    @JsonProperty("source_url")
    private String sourceUrl;
}
