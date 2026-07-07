package cn.openscrm.api.remainder.controller;

import cn.openscrm.api.auth.annotation.RequirePermission;
import cn.openscrm.api.auth.service.CurrentStaffService;
import cn.openscrm.api.common.api.ApiResponse;
import cn.openscrm.api.common.constant.BizIdentity;
import cn.openscrm.api.common.constant.OperationType;
import cn.openscrm.api.persistence.entity.CustomerEventPo;
import cn.openscrm.api.persistence.entity.StaffPo;
import cn.openscrm.api.remainder.dto.RemainderDeleteRequest;
import cn.openscrm.api.remainder.dto.RemainderRequest;
import cn.openscrm.api.remainder.dto.RemainderUpdateRequest;
import cn.openscrm.api.remainder.service.RemainderService;
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
public class RemainderController {

    private final CurrentStaffService currentStaffService;
    private final RemainderService remainderService;

    @PostMapping({"/staff-admin/customer/remainder", "/staff_admin/customer/remainder"})
    @RequirePermission(biz = BizIdentity.BIZ_CUSTOMER_INFO, operation = OperationType.FULL)
    public ApiResponse<CustomerEventPo> createAdmin(@RequestBody RemainderRequest request, HttpSession session) {
        StaffPo current = currentStaffService.requireStaffAdmin(session);
        return ApiResponse.ok(remainderService.create(request, current.getExtCorpId(), current.getExtId()));
    }

    @PostMapping({"/staff-frontend/customer/remainder", "/staff_frontend/customer/remainder"})
    public ApiResponse<CustomerEventPo> createFrontend(@RequestBody RemainderRequest request, HttpSession session) {
        StaffPo current = currentStaffService.requireStaff(session);
        return ApiResponse.ok(remainderService.create(request, current.getExtCorpId(), current.getExtId()));
    }

    @PostMapping({"/staff-admin/customer/remainder/action/delete",
            "/staff_admin/customer/remainder/action/delete"})
    @RequirePermission(biz = BizIdentity.BIZ_CUSTOMER_INFO, operation = OperationType.FULL)
    public ApiResponse<Long> deleteAdmin(@RequestBody RemainderDeleteRequest request, HttpSession session) {
        StaffPo current = currentStaffService.requireStaffAdmin(session);
        return ApiResponse.ok(remainderService.delete(request, current.getExtCorpId()));
    }

    @PostMapping({"/staff-frontend/customer/remainder/action/delete",
            "/staff_frontend/customer/remainder/action/delete"})
    public ApiResponse<Long> deleteFrontend(@RequestBody RemainderDeleteRequest request, HttpSession session) {
        StaffPo current = currentStaffService.requireStaff(session);
        return ApiResponse.ok(remainderService.delete(request, current.getExtCorpId()));
    }

    @PutMapping({"/staff-admin/customer/remainder/{id}", "/staff_admin/customer/remainder/{id}"})
    @RequirePermission(biz = BizIdentity.BIZ_CUSTOMER_INFO, operation = OperationType.FULL)
    public ApiResponse<CustomerEventPo> updateAdmin(@PathVariable Long id,
                                                    @RequestBody RemainderUpdateRequest request,
                                                    HttpSession session) {
        StaffPo current = currentStaffService.requireStaffAdmin(session);
        return ApiResponse.ok(remainderService.update(id, request, current.getExtCorpId()));
    }

    @PutMapping({"/staff-frontend/customer/remainder/{id}", "/staff_frontend/customer/remainder/{id}"})
    public ApiResponse<CustomerEventPo> updateFrontend(@PathVariable Long id,
                                                       @RequestBody RemainderUpdateRequest request,
                                                       HttpSession session) {
        StaffPo current = currentStaffService.requireStaff(session);
        return ApiResponse.ok(remainderService.update(id, request, current.getExtCorpId()));
    }
}
