package cn.openscrm.api.wework;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ContactWayRequest {

    @JsonProperty("config_id")
    private String configId;

    @JsonProperty("is_temp")
    private Boolean temp = false;

    private String remark;

    private Integer scene = 2;

    @JsonProperty("skip_verify")
    private Boolean skipVerify = true;

    private String state;

    private Integer type = 2;

    private List<String> user = new ArrayList<>();
}
