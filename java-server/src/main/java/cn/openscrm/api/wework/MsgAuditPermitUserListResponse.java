package cn.openscrm.api.wework;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MsgAuditPermitUserListResponse extends CommonResponse {

    @JsonProperty("ids")
    private List<String> ids = new ArrayList<>();
}
