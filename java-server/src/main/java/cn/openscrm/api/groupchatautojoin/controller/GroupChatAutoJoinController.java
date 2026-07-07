package cn.openscrm.api.groupchatautojoin.controller;

import cn.openscrm.api.auth.annotation.RequirePermission;
import cn.openscrm.api.auth.service.CurrentStaffService;
import cn.openscrm.api.common.api.ApiResponse;
import cn.openscrm.api.common.api.PageResponse;
import cn.openscrm.api.common.constant.BizIdentity;
import cn.openscrm.api.common.constant.OperationType;
import cn.openscrm.api.groupchatautojoin.dto.GroupChatAutoJoinBatchRegroupRequest;
import cn.openscrm.api.groupchatautojoin.dto.GroupChatAutoJoinDeleteRequest;
import cn.openscrm.api.groupchatautojoin.dto.GroupChatAutoJoinRequest;
import cn.openscrm.api.groupchatautojoin.dto.GroupChatAutoJoinResponse;
import cn.openscrm.api.groupchatautojoin.service.GroupChatAutoJoinService;
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
@RequestMapping({"/api/v1/staff-admin", "/api/v1/staff_admin"})
public class GroupChatAutoJoinController {

    private final CurrentStaffService currentStaffService;
    private final GroupChatAutoJoinService groupChatAutoJoinService;

    @GetMapping({
            "/auto-join-code", "/customer-group/auto-join-code",
            "/group-chat/auto-join-code", "/group-chat/auto-join-codes"})
    @RequirePermission(biz = BizIdentity.BIZ_CUSTOMER_GROUP_CHAT, operation = OperationType.READ)
    public ApiResponse<PageResponse<GroupChatAutoJoinResponse>> query(
            @RequestParam(value = "group_id", required = false) Long groupId,
            @RequestParam(required = false) String remark,
            @RequestParam(defaultValue = "1") @Min(1) long page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(200) long pageSize,
            HttpSession session) {
        StaffPo current = currentStaffService.requireStaffAdmin(session);
        return ApiResponse.ok(groupChatAutoJoinService.query(current.getExtCorpId(), groupId, remark, page, pageSize));
    }

    @PostMapping({"/customer-group/auto-join-code", "/group-chat/auto-join-code"})
    @RequirePermission(biz = BizIdentity.BIZ_CUSTOMER_GROUP_CHAT, operation = OperationType.FULL)
    public ApiResponse<GroupChatAutoJoinResponse> create(@RequestBody GroupChatAutoJoinRequest request,
                                                         HttpSession session) {
        StaffPo current = currentStaffService.requireStaffAdmin(session);
        return ApiResponse.ok(groupChatAutoJoinService.create(request, current));
    }

    @PutMapping({"/customer-group/auto-join-code/{id}", "/group-chat/auto-join-code/{id}"})
    @RequirePermission(biz = BizIdentity.BIZ_CUSTOMER_GROUP_CHAT, operation = OperationType.FULL)
    public ApiResponse<GroupChatAutoJoinResponse> update(@PathVariable Long id,
                                                         @RequestBody GroupChatAutoJoinRequest request,
                                                         HttpSession session) {
        StaffPo current = currentStaffService.requireStaffAdmin(session);
        return ApiResponse.ok(groupChatAutoJoinService.update(id, request, current));
    }

    @PostMapping({"/customer-group/auto-join-code/action/delete", "/group-chat/auto-join-code/action/delete"})
    @RequirePermission(biz = BizIdentity.BIZ_CUSTOMER_GROUP_CHAT, operation = OperationType.FULL)
    public ApiResponse<Integer> delete(@RequestBody GroupChatAutoJoinDeleteRequest request,
                                       HttpSession session) {
        StaffPo current = currentStaffService.requireStaffAdmin(session);
        return ApiResponse.ok(groupChatAutoJoinService.delete(request, current.getExtCorpId()));
    }

    @PostMapping({
            "/customer-group/auto-join-code/action/batch-regroup",
            "/group-chat/auto-join-code/action/batch-regroup"})
    @RequirePermission(biz = BizIdentity.BIZ_CUSTOMER_GROUP_CHAT, operation = OperationType.FULL)
    public ApiResponse<Void> batchRegroup(@RequestBody GroupChatAutoJoinBatchRegroupRequest request,
                                          HttpSession session) {
        StaffPo current = currentStaffService.requireStaffAdmin(session);
        groupChatAutoJoinService.batchRegroup(request, current.getExtCorpId());
        return ApiResponse.ok(null);
    }
}
