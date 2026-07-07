package cn.openscrm.api.customerconfig.dto;

import cn.openscrm.api.persistence.entity.CustomerRemarkPo;
import cn.openscrm.api.persistence.entity.RemarkOptionPo;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomerRemarkResponse extends CustomerRemarkPo {

    @JsonProperty("info_option")
    private List<RemarkOptionPo> infoOption = new ArrayList<>();
}
