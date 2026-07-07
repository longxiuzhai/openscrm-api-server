package cn.openscrm.api.wework;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddMsgTemplateResponse extends CommonResponse {

    @JsonProperty("fail_list")
    private List<String> failList;

    @JsonProperty("msgid")
    private String msgId;
}
