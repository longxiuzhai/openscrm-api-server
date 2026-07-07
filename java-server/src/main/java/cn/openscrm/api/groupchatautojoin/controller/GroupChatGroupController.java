package cn.openscrm.api.groupchatautojoin.controller;

import cn.openscrm.api.auth.annotation.RequirePermission;
import cn.openscrm.api.auth.service.CurrentStaffService;
import cn.openscrm.api.common.api.ApiResponse;
import cn.openscrm.api.common.api.PageResponse;
import cn.openscrm.api.common.constant.BizIdentity;
import cn.openscrm.api.common.constant.OperationType;
import cn.openscrm.api.groupchatautojoin.dto.GroupChatGroupDeleteRequest;
import cn.openscrm.api.groupchatautojoin.dto.GroupChatGroupRequest;
import cn.openscrm.api.groupchatautojoin.service.GroupChatGroupService;
import cn.openscrm.api.persistence.entity.GroupChatGroupPo;
import cn.openscrm.api.persistence.entity.StaffPo;
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
@RequestMapping({
        "/api/v1/staff-admin/customer-group/auto-join",
        "/api/v1/staff-admin/group-chat/auto-join",
        "/api/v1/staff_admin/customer-group/auto-join",
        "/api/v1/staff_admin/group-chat/auto-join"})
public class GroupChatGroupController {

    private final CurrentStaffService currentStaffService;
    private final GroupChatGroupService groupChatGroupService;

    @GetMapping({"/group", "/groups"})
    @RequirePermission(biz = BizIdentity.BIZ_CUSTOMER_GROUP_CHAT, operation = OperationType.READ)
    public ApiResponse<PageResponse<GroupChatGroupPo>> query(
            @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "1") @Min(1) long page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(200) long pageSize,
            HttpSession session) {
        StaffPo current = currentStaffService.requireStaffAdmin(session);
        return ApiResponse.ok(groupChatGroupService.query(current.getExtCorpId(), name, page, pageSize));
    }

    @PostMapping("/group")
    @RequirePermission(biz = BizIdentity.BIZ_CUSTOMER_GROUP_CHAT, operation = OperationType.FULL)
    public ApiResponse<GroupChatGroupPo> create(@RequestBody GroupChatGroupRequest request, HttpSession session) {
        StaffPo current = currentStaffService.requireStaffAdmin(session);
        return ApiResponse.ok(groupChatGroupService.create(request, current));
    }

    @PutMapping("/group/{id}")
    @RequirePermission(biz = BizIdentity.BIZ_CUSTOMER_GROUP_CHAT, operation = OperationType.FULL)
    public ApiResponse<GroupChatGroupPo> update(@PathVariable Long id,
                                                @RequestBody GroupChatGroupRequest request,
                                                HttpSession session) {
        StaffPo current = currentStaffService.requireStaffAdmin(session);
        return ApiResponse.ok(groupChatGroupService.update(id, request, current.getExtCorpId()));
    }

    @PostMapping("/action/delete")
    @RequirePermission(biz = BizIdentity.BIZ_CUSTOMER_GROUP_CHAT, operation = OperationType.FULL)
    public ApiResponse<Integer> delete(@RequestBody GroupChatGroupDeleteRequest request, HttpSession session) {
        StaffPo current = currentStaffService.requireStaffAdmin(session);
        return ApiResponse.ok(groupChatGroupService.delete(request, current.getExtCorpId()));
    }
}
