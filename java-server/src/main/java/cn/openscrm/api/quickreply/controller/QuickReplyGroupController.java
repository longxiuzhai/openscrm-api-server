package cn.openscrm.api.quickreply.controller;

import cn.openscrm.api.auth.annotation.RequirePermission;
import cn.openscrm.api.auth.service.CurrentStaffService;
import cn.openscrm.api.common.api.ApiResponse;
import cn.openscrm.api.common.api.PageResponse;
import cn.openscrm.api.common.constant.BizIdentity;
import cn.openscrm.api.common.constant.OperationType;
import cn.openscrm.api.persistence.entity.QuickReplyGroupPo;
import cn.openscrm.api.persistence.entity.StaffPo;
import cn.openscrm.api.quickreply.dto.QuickReplyGroupDeleteRequest;
import cn.openscrm.api.quickreply.dto.QuickReplyGroupRequest;
import cn.openscrm.api.quickreply.dto.QuickReplyGroupResponse;
import cn.openscrm.api.quickreply.service.QuickReplyGroupService;
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
public class QuickReplyGroupController {

    private final CurrentStaffService currentStaffService;
    private final QuickReplyGroupService groupService;

    @PostMapping({"/staff-admin/quick-reply-group", "/staff_admin/quick-reply-group"})
    @RequirePermission(biz = BizIdentity.BIZ_QUICK_REPLY_GROUP, operation = OperationType.FULL)
    public ApiResponse<QuickReplyGroupPo> create(@RequestBody QuickReplyGroupRequest request, HttpSession session) {
        StaffPo current = currentStaffService.requireStaffAdmin(session);
        return ApiResponse.ok(groupService.create(request, current.getExtCorpId(), current.getExtId()));
    }

    @PostMapping({"/staff-frontend/quick-reply-group", "/staff_frontend/quick-reply-group"})
    public ApiResponse<QuickReplyGroupPo> createFrontend(@RequestBody QuickReplyGroupRequest request, HttpSession session) {
        StaffPo current = currentStaffService.requireStaff(session);
        return ApiResponse.ok(groupService.create(request, current.getExtCorpId(), current.getExtId()));
    }

    @GetMapping({"/staff-admin/quick-reply-groups", "/staff_admin/quick-reply-groups"})
    @RequirePermission(biz = BizIdentity.BIZ_QUICK_REPLY_GROUP, operation = OperationType.READ)
    public ApiResponse<PageResponse<QuickReplyGroupResponse>> query(
            @RequestParam(defaultValue = "1") @Min(1) long page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(200) long pageSize,
            HttpSession session) {
        StaffPo current = currentStaffService.requireStaffAdmin(session);
        return ApiResponse.ok(groupService.query(current.getExtCorpId(), page, pageSize));
    }

    @GetMapping({"/staff-frontend/quick-reply-groups", "/staff_frontend/quick-reply-groups"})
    public ApiResponse<PageResponse<QuickReplyGroupResponse>> queryFrontend(
            @RequestParam(defaultValue = "1") @Min(1) long page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(200) long pageSize,
            HttpSession session) {
        StaffPo current = currentStaffService.requireStaff(session);
        return ApiResponse.ok(groupService.query(current.getExtCorpId(), page, pageSize));
    }

    @PostMapping({"/staff-frontend/quick-reply-group/action/search",
            "/staff_frontend/quick-reply-group/action/search"})
    public ApiResponse<PageResponse<QuickReplyGroupResponse>> searchFrontend(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") @Min(1) long page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(200) long pageSize,
            HttpSession session) {
        StaffPo current = currentStaffService.requireStaff(session);
        return ApiResponse.ok(groupService.searchByQuickReply(current.getExtCorpId(), keyword, page, pageSize));
    }

    @PutMapping({"/staff-admin/quick-reply-group/{id}", "/staff_admin/quick-reply-group/{id}"})
    @RequirePermission(biz = BizIdentity.BIZ_QUICK_REPLY_GROUP, operation = OperationType.FULL)
    public ApiResponse<QuickReplyGroupResponse> update(@PathVariable Long id,
                                                       @RequestBody QuickReplyGroupRequest request,
                                                       HttpSession session) {
        StaffPo current = currentStaffService.requireStaffAdmin(session);
        return ApiResponse.ok(groupService.update(id, request, current.getExtCorpId()));
    }

    @PutMapping({"/staff-frontend/quick-reply-group", "/staff_frontend/quick-reply-group"})
    public ApiResponse<QuickReplyGroupResponse> updateFrontend(@RequestBody QuickReplyGroupRequest request,
                                                               HttpSession session) {
        StaffPo current = currentStaffService.requireStaff(session);
        return ApiResponse.ok(groupService.update(request.getId(), request, current.getExtCorpId()));
    }

    @PostMapping({"/staff-admin/quick-reply-group/action/delete",
            "/staff_admin/quick-reply-group/action/delete"})
    @RequirePermission(biz = BizIdentity.BIZ_QUICK_REPLY_GROUP, operation = OperationType.FULL)
    public ApiResponse<Void> delete(@RequestBody QuickReplyGroupDeleteRequest request, HttpSession session) {
        StaffPo current = currentStaffService.requireStaffAdmin(session);
        groupService.delete(request, current.getExtCorpId());
        return ApiResponse.ok(null);
    }

    @PostMapping({"/staff-frontend/quick-reply-group/action/delete",
            "/staff_frontend/quick-reply-group/action/delete"})
    public ApiResponse<Void> deleteFrontend(@RequestBody QuickReplyGroupDeleteRequest request, HttpSession session) {
        StaffPo current = currentStaffService.requireStaff(session);
        groupService.delete(request, current.getExtCorpId());
        return ApiResponse.ok(null);
    }
}
