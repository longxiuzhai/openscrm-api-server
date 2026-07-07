package cn.openscrm.api.wework;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MsgAuditPermitUserListRequest {

    @JsonProperty("type")
    private Integer type;
}
