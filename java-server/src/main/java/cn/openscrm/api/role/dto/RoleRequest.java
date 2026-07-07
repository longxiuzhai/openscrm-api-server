package cn.openscrm.api.role.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoleRequest {

    private String name;

    private String description;

    @JsonProperty("sort_weight")
    private Long sortWeight;

    @JsonProperty("permission_ids")
    private List<String> permissionIds;
}
