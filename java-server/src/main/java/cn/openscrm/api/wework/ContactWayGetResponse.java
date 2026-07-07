package cn.openscrm.api.wework;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ContactWayGetResponse extends CommonResponse {

    @JsonProperty("contact_way")
    private ContactWayInfo contactWay;
}
