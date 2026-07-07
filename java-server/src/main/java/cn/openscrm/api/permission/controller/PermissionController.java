package cn.openscrm.api.permission.controller;

import cn.openscrm.api.auth.annotation.RequirePermission;
import cn.openscrm.api.common.api.ApiResponse;
import cn.openscrm.api.common.api.PageResponse;
import cn.openscrm.api.common.constant.BizIdentity;
import cn.openscrm.api.common.constant.OperationType;
import cn.openscrm.api.permission.service.PermissionQueryService;
import cn.openscrm.api.persistence.entity.PermissionPo;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class PermissionController {

    private final PermissionQueryService permissionQueryService;

    @GetMapping({"/corp-admin/permissions", "/corp_admin/permissions",
            "/staff-admin/permissions", "/staff_admin/permissions"})
    @RequirePermission(biz = BizIdentity.BIZ_ROLE, operation = OperationType.READ)
    public ApiResponse<PageResponse<PermissionPo>> query(
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) String name,
            @RequestParam(value = "biz_identity", required = false) String bizIdentity,
            @RequestParam(required = false) String operation,
            @RequestParam(required = false) String identity,
            @RequestParam(defaultValue = "1") @Min(1) long page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(200) long pageSize) {
        return ApiResponse.ok(permissionQueryService.query(id, name, bizIdentity, operation, identity, page, pageSize));
    }

    @GetMapping({"/corp-admin/permission/{id}", "/corp_admin/permission/{id}",
            "/staff-admin/permission/{id}", "/staff_admin/permission/{id}"})
    @RequirePermission(biz = BizIdentity.BIZ_ROLE, operation = OperationType.READ)
    public ApiResponse<PermissionPo> get(@PathVariable Long id) {
        return ApiResponse.ok(permissionQueryService.get(id));
    }
}
