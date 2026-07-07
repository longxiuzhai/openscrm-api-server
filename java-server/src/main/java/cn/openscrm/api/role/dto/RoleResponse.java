package cn.openscrm.api.role.dto;

import cn.openscrm.api.persistence.entity.PermissionPo;
import cn.openscrm.api.persistence.entity.RolePo;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoleResponse {

    @JsonUnwrapped
    private RolePo role;

    private Long count = 0L;

    private List<PermissionPo> permissions = new ArrayList<>();
}
