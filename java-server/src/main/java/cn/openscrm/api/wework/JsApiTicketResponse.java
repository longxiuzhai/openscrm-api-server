package cn.openscrm.api.wework;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JsApiTicketResponse extends CommonResponse {

    private String ticket;

    @JsonProperty("expires_in")
    private Integer expiresIn;
}
