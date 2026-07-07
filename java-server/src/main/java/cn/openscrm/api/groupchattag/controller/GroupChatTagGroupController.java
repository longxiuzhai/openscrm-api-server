package cn.openscrm.api.groupchattag.controller;

import cn.openscrm.api.auth.annotation.RequirePermission;
import cn.openscrm.api.auth.service.CurrentStaffService;
import cn.openscrm.api.common.api.ApiResponse;
import cn.openscrm.api.common.api.PageResponse;
import cn.openscrm.api.common.constant.BizIdentity;
import cn.openscrm.api.common.constant.OperationType;
import cn.openscrm.api.groupchattag.dto.CommonIdsRequest;
import cn.openscrm.api.groupchattag.dto.GroupChatTagGroupCreateRequest;
import cn.openscrm.api.groupchattag.dto.GroupChatTagGroupResponse;
import cn.openscrm.api.groupchattag.dto.GroupChatTagGroupUpdateRequest;
import cn.openscrm.api.groupchattag.service.GroupChatTagGroupService;
import cn.openscrm.api.persistence.entity.StaffPo;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
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
public class GroupChatTagGroupController {

    private final CurrentStaffService currentStaffService;
    private final GroupChatTagGroupService groupService;

    @PostMapping({"/group-chat/tag-group", "/customer-group/tag-group"})
    @RequirePermission(biz = BizIdentity.BIZ_CUSTOMER_GROUP_CHAT, operation = OperationType.FULL)
    public ApiResponse<GroupChatTagGroupResponse> create(
            @Valid @RequestBody GroupChatTagGroupCreateRequest request,
            HttpSession session) {
        StaffPo current = currentStaffService.requireStaffAdmin(session);
        return ApiResponse.ok(groupService.create(request, current.getExtCorpId(), current.getExtId()));
    }

    @PutMapping({"/group-chat/tag-group", "/customer-group/tag-group"})
    @RequirePermission(biz = BizIdentity.BIZ_CUSTOMER_GROUP_CHAT, operation = OperationType.FULL)
    public ApiResponse<GroupChatTagGroupResponse> update(
            @Valid @RequestBody GroupChatTagGroupUpdateRequest request,
            HttpSession session) {
        StaffPo current = currentStaffService.requireStaffAdmin(session);
        return ApiResponse.ok(groupService.update(request, current.getExtCorpId()));
    }

    @GetMapping({"/group-chat/tag-groups", "/customer-group/tag-groups", "/customer-group/tag-group"})
    @RequirePermission(biz = BizIdentity.BIZ_CUSTOMER_GROUP_CHAT, operation = OperationType.READ)
    public ApiResponse<PageResponse<GroupChatTagGroupResponse>> query(
            @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "1") @Min(1) long page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(200) long pageSize,
            HttpSession session) {
        StaffPo current = currentStaffService.requireStaffAdmin(session);
        return ApiResponse.ok(groupService.query(current.getExtCorpId(), name, page, pageSize));
    }

    @PostMapping({"/group-chat/tag-group/action/delete", "/customer-group/tag-group/action/delete"})
    @RequirePermission(biz = BizIdentity.BIZ_CUSTOMER_GROUP_CHAT, operation = OperationType.FULL)
    public ApiResponse<Long> delete(@Valid @RequestBody CommonIdsRequest request) {
        return ApiResponse.ok(groupService.delete(request));
    }
}
