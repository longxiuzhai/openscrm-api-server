package cn.openscrm.api.staff.controller;

import cn.openscrm.api.auth.annotation.RequirePermission;
import cn.openscrm.api.auth.service.CurrentStaffService;
import cn.openscrm.api.common.api.ApiResponse;
import cn.openscrm.api.common.api.PageResponse;
import cn.openscrm.api.common.constant.BizIdentity;
import cn.openscrm.api.common.constant.OperationType;
import cn.openscrm.api.persistence.entity.StaffPo;
import cn.openscrm.api.staff.dto.EnableStaffsRequest;
import cn.openscrm.api.staff.service.StaffService;
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
@RequestMapping({"/api/v1/staff-admin", "/api/v1/staff_admin"})
public class StaffController {

    private final StaffService staffService;
    private final CurrentStaffService currentStaffService;

    @PostMapping("/staff")
    @RequirePermission(biz = BizIdentity.BIZ_STAFF_INFO, operation = OperationType.FULL)
    public ApiResponse<Void> sync(HttpSession session) {
        StaffPo current = currentStaffService.requireStaffAdmin(session);
        staffService.sync(current.getExtCorpId());
        return ApiResponse.ok(null);
    }

    @GetMapping("/staffs")
    @RequirePermission(biz = BizIdentity.BIZ_STAFF_INFO, operation = OperationType.READ)
    public ApiResponse<PageResponse<StaffPo>> query(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String roleType,
            @RequestParam(required = false) Long roleId,
            @RequestParam(defaultValue = "1") @Min(1) long page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(200) long pageSize,
            HttpSession session) {
        StaffPo current = currentStaffService.requireStaffAdmin(session);
        return ApiResponse.ok(staffService.query(current.getExtCorpId(), name, roleType, roleId, page, pageSize));
    }

    @GetMapping("/staff/{extStaffId}")
    @RequirePermission(biz = BizIdentity.BIZ_STAFF_INFO, operation = OperationType.READ)
    public ApiResponse<StaffPo> get(@PathVariable String extStaffId, HttpSession session) {
        StaffPo current = currentStaffService.requireStaffAdmin(session);
        return ApiResponse.ok(staffService.get(current.getExtCorpId(), extStaffId));
    }

    @PutMapping("/staff")
    @RequirePermission(biz = BizIdentity.BIZ_STAFF_INFO, operation = OperationType.FULL)
    public ApiResponse<Void> enable(@RequestBody EnableStaffsRequest request, HttpSession session) {
        StaffPo current = currentStaffService.requireStaffAdmin(session);
        staffService.enable(current.getExtCorpId(), request);
        return ApiResponse.ok(null);
    }

    @PostMapping("/staff/action/update-msg-arch-status")
    @RequirePermission(biz = BizIdentity.BIZ_STAFF_INFO, operation = OperationType.FULL)
    public ApiResponse<Void> updateMsgArchStatus(HttpSession session) {
        StaffPo current = currentStaffService.requireStaffAdmin(session);
        staffService.updateMsgArchStatus(current.getExtCorpId());
        return ApiResponse.ok(null);
    }

    @GetMapping("/action/get-current-staff")
    public ApiResponse<StaffPo> current(HttpSession session) {
        return ApiResponse.ok(currentStaffService.requireStaffAdmin(session));
    }
}
