package cn.openscrm.api.role.controller;

import cn.openscrm.api.auth.annotation.RequirePermission;
import cn.openscrm.api.auth.service.CurrentStaffService;
import cn.openscrm.api.common.api.ApiResponse;
import cn.openscrm.api.common.api.PageResponse;
import cn.openscrm.api.common.constant.BizIdentity;
import cn.openscrm.api.common.constant.OperationType;
import cn.openscrm.api.persistence.entity.RolePo;
import cn.openscrm.api.persistence.entity.StaffPo;
import cn.openscrm.api.role.dto.AssignRoleRequest;
import cn.openscrm.api.role.dto.RoleRequest;
import cn.openscrm.api.role.dto.RoleResponse;
import cn.openscrm.api.role.service.RoleService;
import javax.servlet.http.HttpSession;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class RoleController {

    private final RoleService roleService;
    private final CurrentStaffService currentStaffService;

    @GetMapping({"/corp-admin/roles", "/corp_admin/roles",
            "/staff-admin/roles", "/staff_admin/roles"})
    @RequirePermission(biz = BizIdentity.BIZ_ROLE, operation = OperationType.READ)
    public ApiResponse<PageResponse<RoleResponse>> query(
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String type,
            @RequestParam(value = "is_default", required = false) Integer isDefault,
            @RequestParam(defaultValue = "1") @Min(1) long page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(200) long pageSize,
            HttpSession session) {
        StaffPo current = currentStaffService.requireStaffAdmin(session);
        return ApiResponse.ok(roleService.query(current.getExtCorpId(), id, name, type, isDefault, page, pageSize));
    }

    @GetMapping({"/corp-admin/role/{id}", "/corp_admin/role/{id}",
            "/staff-admin/role/{id}", "/staff_admin/role/{id}"})
    @RequirePermission(biz = BizIdentity.BIZ_ROLE, operation = OperationType.READ)
    public ApiResponse<RoleResponse> get(@PathVariable Long id) {
        return ApiResponse.ok(roleService.get(id));
    }

    @PostMapping({"/corp-admin/role", "/corp_admin/role",
            "/staff-admin/role", "/staff_admin/role"})
    @RequirePermission(biz = BizIdentity.BIZ_ROLE, operation = OperationType.FULL)
    public ApiResponse<RolePo> create(@RequestBody RoleRequest request, HttpSession session) {
        StaffPo current = currentStaffService.requireStaffAdmin(session);
        return ApiResponse.ok(roleService.create(current.getExtCorpId(), request));
    }

    @PutMapping({"/corp-admin/role/{id}", "/corp_admin/role/{id}",
            "/staff-admin/role/{id}", "/staff_admin/role/{id}"})
    @RequirePermission(biz = BizIdentity.BIZ_ROLE, operation = OperationType.FULL)
    public ApiResponse<RolePo> update(@PathVariable Long id, @RequestBody RoleRequest request) {
        return ApiResponse.ok(roleService.update(id, request));
    }

    @PostMapping({"/corp-admin/role/action/assign-to-staffs", "/corp_admin/role/action/assign-to-staffs",
            "/staff-admin/role/action/assign-to-staffs", "/staff_admin/role/action/assign-to-staffs"})
    @RequirePermission(biz = BizIdentity.BIZ_ROLE, operation = OperationType.FULL)
    public ApiResponse<Long> assignToStaffs(@RequestBody AssignRoleRequest request) {
        return ApiResponse.ok(roleService.assignToStaffs(request));
    }

    @GetMapping({"/corp-admin/role/action/query-staffs", "/corp_admin/role/action/query-staffs",
            "/staff-admin/role/action/query-staffs", "/staff_admin/role/action/query-staffs"})
    @RequirePermission(biz = BizIdentity.BIZ_ROLE, operation = OperationType.READ)
    public ApiResponse<PageResponse<StaffPo>> queryStaffs(
            @RequestParam(value = "staff_id", required = false) Long staffId,
            @RequestParam(value = "ext_staff_id", required = false) String extStaffId,
            @RequestParam(required = false) String name,
            @RequestParam(value = "role_id", required = false) Long roleId,
            @RequestParam(value = "role_type", required = false) String roleType,
            @RequestParam(defaultValue = "1") @Min(1) long page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(200) long pageSize,
            HttpSession session) {
        StaffPo current = currentStaffService.requireStaffAdmin(session);
        return ApiResponse.ok(roleService.queryStaffs(current.getExtCorpId(), staffId, extStaffId, name,
                roleId, roleType, page, pageSize));
    }
}
