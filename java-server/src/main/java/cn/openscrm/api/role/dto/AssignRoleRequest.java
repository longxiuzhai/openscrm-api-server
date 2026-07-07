package cn.openscrm.api.role.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AssignRoleRequest {

    @JsonProperty("ext_staff_ids")
    private List<String> extStaffIds;

    @JsonProperty("role_id")
    private Long roleId;
}
