package cn.openscrm.api.wework;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ContactWayAddResponse extends CommonResponse {

    @JsonProperty("config_id")
    private String configId;

    @JsonProperty("qr_code")
    private String qrCode;
}
