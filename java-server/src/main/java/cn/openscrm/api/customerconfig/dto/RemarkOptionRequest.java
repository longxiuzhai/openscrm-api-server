package cn.openscrm.api.customerconfig.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RemarkOptionRequest {

    @JsonProperty("remark_id")
    private Long remarkId;

    @JsonProperty("remark_option_id")
    private Long remarkOptionId;

    private String name;

    private List<Long> ids;
}
