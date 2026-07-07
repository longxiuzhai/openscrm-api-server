package cn.openscrm.api.customerconfig.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomerInfoDisplayRuleRequest {

    @JsonProperty("display_field_list")
    private List<String> displayFieldList;

    @JsonProperty("cancel_display_field_list")
    private List<String> cancelDisplayFieldList;
}
