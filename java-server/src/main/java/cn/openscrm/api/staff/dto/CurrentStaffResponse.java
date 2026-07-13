package cn.openscrm.api.staff.dto;

import cn.openscrm.api.persistence.entity.RolePo;
import cn.openscrm.api.persistence.entity.StaffPo;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRawValue;
import java.util.Collections;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * Compatibility response for the dashboard's current-staff contract.
 */
@Getter
@Setter
public class CurrentStaffResponse {

    private Long id;

    @JsonProperty("ext_corp_id")
    private String extCorpId;

    @JsonProperty("ext_staff_id")
    private String extStaffId;

    @JsonProperty("role_id")
    private Long roleId;

    @JsonProperty("role_type")
    private String roleType;

    private String name;

    private String address;

    private String alias;

    @JsonProperty("avatar_url")
    private String avatarUrl;

    private String email;

    private Integer gender;

    private Integer status;

    private String mobile;

    @JsonProperty("qr_code_url")
    private String qrCodeUrl;

    private String telephone;

    private Integer enable;

    private String signature;

    @JsonProperty("external_position")
    private String externalPosition;

    @JsonProperty("external_profile")
    private String externalProfile;

    private String extattr;

    @JsonProperty("external_user_count")
    private Long externalUserCount;

    @JsonProperty("dept_ids")
    @JsonRawValue
    private String deptIds;

    @JsonProperty("welcome_msg_id")
    private Long welcomeMsgId;

    @JsonProperty("is_authorized")
    private Integer isAuthorized;

    @JsonProperty("enable_msg_arch")
    private Integer enableMsgArch;

    private RoleInfo role;

    public static CurrentStaffResponse from(StaffPo staff, RolePo role, List<String> permissionIds) {
        CurrentStaffResponse response = new CurrentStaffResponse();
        response.setId(staff.getId());
        response.setExtCorpId(staff.getExtCorpId());
        response.setExtStaffId(staff.getExtId());
        response.setRoleId(staff.getRoleId());
        response.setRoleType(staff.getRoleType());
        response.setName(staff.getName());
        response.setAddress(staff.getAddress());
        response.setAlias(staff.getAlias());
        response.setAvatarUrl(staff.getAvatarUrl());
        response.setEmail(staff.getEmail());
        response.setGender(staff.getGender());
        response.setStatus(staff.getStatus());
        response.setMobile(staff.getMobile());
        response.setQrCodeUrl(staff.getQrCodeUrl());
        response.setTelephone(staff.getTelephone());
        response.setEnable(staff.getEnable());
        response.setSignature(staff.getSignature());
        response.setExternalPosition(staff.getExternalPosition());
        response.setExternalProfile(staff.getExternalProfile());
        response.setExtattr(staff.getExtattr());
        response.setExternalUserCount(staff.getCustomerCount());
        response.setDeptIds(staff.getDeptIds());
        response.setWelcomeMsgId(staff.getWelcomeMsgId());
        response.setIsAuthorized(staff.getIsAuthorized());
        response.setEnableMsgArch(staff.getEnableMsgArch());
        response.setRole(role == null ? null : RoleInfo.from(role, permissionIds));
        return response;
    }

    @Getter
    @Setter
    public static class RoleInfo {

        private Long id;

        @JsonProperty("ext_corp_id")
        private String extCorpId;

        private String name;

        private String description;

        private String type;

        @JsonProperty("sort_weight")
        private Long sortWeight;

        @JsonProperty("is_default")
        private Integer isDefault;

        @JsonProperty("permission_ids")
        private List<String> permissionIds = Collections.emptyList();

        private static RoleInfo from(RolePo role, List<String> permissionIds) {
            RoleInfo response = new RoleInfo();
            response.setId(role.getId());
            response.setExtCorpId(role.getExtCorpId());
            response.setName(role.getName());
            response.setDescription(role.getDescription());
            response.setType(role.getType());
            response.setSortWeight(role.getSortWeight());
            response.setIsDefault(role.getIsDefault());
            response.setPermissionIds(permissionIds == null ? Collections.emptyList() : permissionIds);
            return response;
        }
    }
}
