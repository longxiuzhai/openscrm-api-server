package cn.openscrm.api.deletenotify.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeleteCustomerNotifyRuleRequest {

    @JsonProperty("is_notify_staff")
    private Integer isNotifyStaff;

    @JsonProperty("notify_type")
    private Integer notifyType;

    @JsonProperty("ext_staff_ids")
    private List<String> extStaffIds;
}
