package cn.openscrm.api.staff.dto;

import cn.openscrm.api.persistence.entity.DepartmentPo;
import cn.openscrm.api.persistence.entity.StaffPo;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * Compact staff projection used by dashboard selectors.
 */
@Getter
@Setter
public class SimpleStaffResponse {

    private Long id;

    @JsonProperty("ext_id")
    private String extId;

    @JsonProperty("avatar_url")
    private String avatarUrl;

    @JsonProperty("role_id")
    private Long roleId;

    @JsonProperty("role_type")
    private String roleType;

    private String name;

    private List<DepartmentInfo> departments = new ArrayList<>();

    public static SimpleStaffResponse from(StaffPo staff, List<DepartmentPo> departments) {
        SimpleStaffResponse response = new SimpleStaffResponse();
        response.setId(staff.getId());
        response.setExtId(staff.getExtId());
        response.setAvatarUrl(staff.getAvatarUrl());
        response.setRoleId(staff.getRoleId());
        response.setRoleType(staff.getRoleType());
        response.setName(staff.getName());
        if (departments != null) {
            for (DepartmentPo department : departments) {
                response.getDepartments().add(DepartmentInfo.from(department));
            }
        }
        return response;
    }

    @Getter
    @Setter
    public static class DepartmentInfo {

        @JsonProperty("ext_id")
        private Integer extId;

        private String name;

        @JsonProperty("ext_parent_id")
        private Integer extParentId;

        private static DepartmentInfo from(DepartmentPo department) {
            DepartmentInfo response = new DepartmentInfo();
            response.setExtId(department.getExtId());
            response.setName(department.getName());
            response.setExtParentId(department.getExtParentId());
            return response;
        }
    }
}
