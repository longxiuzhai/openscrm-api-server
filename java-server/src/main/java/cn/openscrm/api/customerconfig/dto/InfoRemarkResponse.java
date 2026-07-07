package cn.openscrm.api.customerconfig.dto;

import cn.openscrm.api.persistence.entity.CustomerInfoDisplayRulePo;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InfoRemarkResponse {

    @JsonProperty("display_rules")
    private CustomerInfoDisplayRulePo displayRules;

    private List<CustomerRemarkResponse> remark = new ArrayList<>();
}
