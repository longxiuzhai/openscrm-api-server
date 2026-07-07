package cn.openscrm.api.cluemanual.controller;

import cn.openscrm.api.auth.annotation.RequirePermission;
import cn.openscrm.api.auth.service.CurrentStaffService;
import cn.openscrm.api.cluemanual.dto.ClueManualDeleteRequest;
import cn.openscrm.api.cluemanual.dto.ClueManualRequest;
import cn.openscrm.api.cluemanual.dto.ClueManualUpdateRequest;
import cn.openscrm.api.cluemanual.service.ClueManualService;
import cn.openscrm.api.common.api.ApiResponse;
import cn.openscrm.api.common.constant.BizIdentity;
import cn.openscrm.api.common.constant.OperationType;
import cn.openscrm.api.persistence.entity.CustomerEventPo;
import cn.openscrm.api.persistence.entity.StaffPo;
import javax.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class ClueManualController {

    private final CurrentStaffService currentStaffService;
    private final ClueManualService clueManualService;

    @PostMapping({"/staff-admin/customer/clue-manual", "/staff_admin/customer/clue-manual"})
    @RequirePermission(biz = BizIdentity.BIZ_CUSTOMER_INFO, operation = OperationType.FULL)
    public ApiResponse<CustomerEventPo> createAdmin(@RequestBody ClueManualRequest request, HttpSession session) {
        StaffPo current = currentStaffService.requireStaffAdmin(session);
        return ApiResponse.ok(clueManualService.create(request, current.getExtCorpId(), current.getExtId()));
    }

    @PostMapping({"/staff-frontend/customer/clue-manual", "/staff_frontend/customer/clue-manual"})
    public ApiResponse<CustomerEventPo> createFrontend(@RequestBody ClueManualRequest request, HttpSession session) {
        StaffPo current = currentStaffService.requireStaff(session);
        return ApiResponse.ok(clueManualService.create(request, current.getExtCorpId(), current.getExtId()));
    }

    @PostMapping({"/staff-admin/customer/clue-manual/action/delete",
            "/staff_admin/customer/clue-manual/action/delete"})
    @RequirePermission(biz = BizIdentity.BIZ_CUSTOMER_INFO, operation = OperationType.FULL)
    public ApiResponse<Long> deleteAdmin(@RequestBody ClueManualDeleteRequest request, HttpSession session) {
        StaffPo current = currentStaffService.requireStaffAdmin(session);
        return ApiResponse.ok(clueManualService.delete(request, current.getExtCorpId()));
    }

    @PostMapping({"/staff-frontend/customer/clue-manual/action/delete",
            "/staff_frontend/customer/clue-manual/action/delete"})
    public ApiResponse<Long> deleteFrontend(@RequestBody ClueManualDeleteRequest request, HttpSession session) {
        StaffPo current = currentStaffService.requireStaff(session);
        return ApiResponse.ok(clueManualService.delete(request, current.getExtCorpId()));
    }

    @PutMapping({"/staff-admin/customer/clue-manual/{id}", "/staff_admin/customer/clue-manual/{id}",
            "/staff-admin/clue-manual/{id}", "/staff_admin/clue-manual/{id}"})
    @RequirePermission(biz = BizIdentity.BIZ_CUSTOMER_INFO, operation = OperationType.FULL)
    public ApiResponse<CustomerEventPo> updateAdmin(@PathVariable Long id,
                                                    @RequestBody ClueManualUpdateRequest request,
                                                    HttpSession session) {
        StaffPo current = currentStaffService.requireStaffAdmin(session);
        return ApiResponse.ok(clueManualService.update(id, request, current.getExtCorpId()));
    }

    @PutMapping({"/staff-frontend/customer/clue-manual/{id}", "/staff_frontend/customer/clue-manual/{id}",
            "/staff-frontend/clue-manual/{id}", "/staff_frontend/clue-manual/{id}"})
    public ApiResponse<CustomerEventPo> updateFrontend(@PathVariable Long id,
                                                       @RequestBody ClueManualUpdateRequest request,
                                                       HttpSession session) {
        StaffPo current = currentStaffService.requireStaff(session);
        return ApiResponse.ok(clueManualService.update(id, request, current.getExtCorpId()));
    }
}
