package cn.openscrm.api.department.controller;

import cn.openscrm.api.auth.annotation.RequirePermission;
import cn.openscrm.api.auth.service.CurrentStaffService;
import cn.openscrm.api.common.api.ApiResponse;
import cn.openscrm.api.common.api.PageResponse;
import cn.openscrm.api.common.constant.BizIdentity;
import cn.openscrm.api.common.constant.OperationType;
import cn.openscrm.api.department.service.DepartmentService;
import cn.openscrm.api.persistence.entity.DepartmentPo;
import cn.openscrm.api.persistence.entity.StaffPo;
import javax.servlet.http.HttpSession;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping({"/api/v1/staff-admin", "/api/v1/staff_admin"})
public class DepartmentController {

    private final DepartmentService departmentService;
    private final CurrentStaffService currentStaffService;

    @PostMapping("/department")
    @RequirePermission(biz = BizIdentity.BIZ_DEPARTMENT, operation = OperationType.FULL)
    public ApiResponse<Void> sync(HttpSession session) {
        StaffPo staff = currentStaffService.requireStaffAdmin(session);
        departmentService.sync(staff.getExtCorpId());
        return ApiResponse.ok(null);
    }

    @GetMapping("/departments")
    @RequirePermission(biz = BizIdentity.BIZ_DEPARTMENT, operation = OperationType.READ)
    public ApiResponse<PageResponse<DepartmentPo>> query(
            @RequestParam(required = false) Integer extParentId,
            @RequestParam(defaultValue = "1") @Min(1) long page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(200) long pageSize,
            HttpSession session) {
        StaffPo staff = currentStaffService.requireStaffAdmin(session);
        return ApiResponse.ok(departmentService.query(staff.getExtCorpId(), extParentId, page, pageSize));
    }

    @GetMapping("/department")
    @RequirePermission(biz = BizIdentity.BIZ_DEPARTMENT, operation = OperationType.READ)
    public ApiResponse<DepartmentService.DepartmentTreeNode> get(
            @RequestParam(required = false) Integer extId,
            HttpSession session) {
        StaffPo staff = currentStaffService.requireStaffAdmin(session);
        return ApiResponse.ok(departmentService.tree(staff.getExtCorpId(), extId));
    }
}
