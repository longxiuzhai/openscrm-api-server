package cn.openscrm.api.wework;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ContactWayInfo {

    @JsonProperty("config_id")
    private String configId;

    @JsonProperty("qr_code")
    private String qrCode;

    private String remark;

    @JsonProperty("skip_verify")
    private Boolean skipVerify;

    private String state;

    private List<String> user = new ArrayList<>();
}
