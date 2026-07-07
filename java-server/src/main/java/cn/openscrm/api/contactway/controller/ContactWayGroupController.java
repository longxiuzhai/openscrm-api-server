package cn.openscrm.api.contactway.controller;

import cn.openscrm.api.auth.annotation.RequirePermission;
import cn.openscrm.api.auth.service.CurrentStaffService;
import cn.openscrm.api.common.api.ApiResponse;
import cn.openscrm.api.common.api.PageResponse;
import cn.openscrm.api.common.constant.BizIdentity;
import cn.openscrm.api.common.constant.OperationType;
import cn.openscrm.api.contactway.dto.ContactWayGroupDeleteRequest;
import cn.openscrm.api.contactway.dto.ContactWayGroupRequest;
import cn.openscrm.api.contactway.dto.ContactWayGroupResponse;
import cn.openscrm.api.contactway.service.ContactWayGroupService;
import cn.openscrm.api.persistence.entity.ContactWayGroupPo;
import cn.openscrm.api.persistence.entity.StaffPo;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
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
public class ContactWayGroupController {

    private final ContactWayGroupService contactWayGroupService;
    private final CurrentStaffService currentStaffService;

    @GetMapping({
            "/staff_admin/contact_way_groups", "/staff-admin/contact_way_groups",
            "/staff_admin/contact-way-groups", "/staff-admin/contact-way-groups"})
    @RequirePermission(biz = BizIdentity.BIZ_CONTACT_WAY, operation = OperationType.READ)
    public ApiResponse<PageResponse<ContactWayGroupResponse>> query(
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) String name,
            @RequestParam(value = "is_default", required = false) Integer isDefault,
            @RequestParam(defaultValue = "1") @Min(1) long page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(200) long pageSize,
            HttpSession session) {
        StaffPo current = currentStaffService.requireStaffAdmin(session);
        return ApiResponse.ok(contactWayGroupService.query(
                current.getExtCorpId(), id, name, isDefault, page, pageSize));
    }

    @GetMapping({
            "/staff_admin/contact_way_group/{id}", "/staff-admin/contact_way_group/{id}",
            "/staff_admin/contact-way-group/{id}", "/staff-admin/contact-way-group/{id}"})
    @RequirePermission(biz = BizIdentity.BIZ_CONTACT_WAY, operation = OperationType.READ)
    public ApiResponse<ContactWayGroupResponse> get(@PathVariable Long id, HttpSession session) {
        StaffPo current = currentStaffService.requireStaffAdmin(session);
        return ApiResponse.ok(contactWayGroupService.get(id, current.getExtCorpId()));
    }

    @PostMapping({
            "/staff_admin/contact_way_group", "/staff-admin/contact_way_group",
            "/staff_admin/contact-way-group", "/staff-admin/contact-way-group"})
    @RequirePermission(biz = BizIdentity.BIZ_CONTACT_WAY, operation = OperationType.FULL)
    public ApiResponse<ContactWayGroupPo> create(@Valid @RequestBody ContactWayGroupRequest request,
                                                 HttpSession session) {
        StaffPo current = currentStaffService.requireStaffAdmin(session);
        return ApiResponse.ok(contactWayGroupService.create(request, current.getExtCorpId(), current.getExtId()));
    }

    @PutMapping({
            "/staff_admin/contact_way_group/{id}", "/staff-admin/contact_way_group/{id}",
            "/staff_admin/contact-way-group/{id}", "/staff-admin/contact-way-group/{id}"})
    @RequirePermission(biz = BizIdentity.BIZ_CONTACT_WAY, operation = OperationType.FULL)
    public ApiResponse<ContactWayGroupPo> update(@PathVariable Long id,
                                                 @Valid @RequestBody ContactWayGroupRequest request,
                                                 HttpSession session) {
        StaffPo current = currentStaffService.requireStaffAdmin(session);
        return ApiResponse.ok(contactWayGroupService.update(id, request, current.getExtCorpId()));
    }

    @PostMapping({
            "/staff_admin/contact_way_group/action/delete", "/staff-admin/contact_way_group/action/delete",
            "/staff_admin/contact-way-group/action/delete", "/staff-admin/contact-way-group/action/delete"})
    @RequirePermission(biz = BizIdentity.BIZ_CONTACT_WAY, operation = OperationType.FULL)
    public ApiResponse<Long> delete(@RequestBody ContactWayGroupDeleteRequest request, HttpSession session) {
        StaffPo current = currentStaffService.requireStaffAdmin(session);
        return ApiResponse.ok(contactWayGroupService.delete(request, current.getExtCorpId()));
    }
}
