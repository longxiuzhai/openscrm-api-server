package cn.openscrm.api.massmsg.controller;

import cn.openscrm.api.auth.annotation.RequirePermission;
import cn.openscrm.api.auth.service.CurrentStaffService;
import cn.openscrm.api.common.api.ApiResponse;
import cn.openscrm.api.common.api.PageResponse;
import cn.openscrm.api.common.constant.BizIdentity;
import cn.openscrm.api.common.constant.OperationType;
import cn.openscrm.api.massmsg.dto.GroupChatMassMsgDetailResponse;
import cn.openscrm.api.massmsg.dto.GroupChatMassMsgRequest;
import cn.openscrm.api.massmsg.dto.MassMsgDeleteRequest;
import cn.openscrm.api.massmsg.service.MassMsgService;
import cn.openscrm.api.persistence.entity.GroupChatMassMsgPo;
import cn.openscrm.api.persistence.entity.StaffPo;
import javax.servlet.http.HttpSession;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping({"/api/v1/staff-admin", "/api/v1/staff_admin"})
public class GroupChatMassMsgController {

    private final CurrentStaffService currentStaffService;
    private final MassMsgService massMsgService;

    @PostMapping("/group-chat/mass-msg")
    @RequirePermission(biz = BizIdentity.BIZ_MASS_MSG, operation = OperationType.FULL)
    public ApiResponse<GroupChatMassMsgPo> create(@RequestBody GroupChatMassMsgRequest request,
                                                  HttpSession session) {
        StaffPo current = currentStaffService.requireStaffAdmin(session);
        return ApiResponse.ok(massMsgService.createGroupChat(request, current));
    }

    @GetMapping("/group-chat/mass-msg/{id}")
    @RequirePermission(biz = BizIdentity.BIZ_MASS_MSG, operation = OperationType.READ)
    public ApiResponse<GroupChatMassMsgDetailResponse> get(@PathVariable Long id, HttpSession session) {
        StaffPo current = currentStaffService.requireStaffAdmin(session);
        return ApiResponse.ok(massMsgService.getGroupChat(id, current.getExtCorpId()));
    }

    @PostMapping("/group-chat/mass-msg/action/delete")
    @RequirePermission(biz = BizIdentity.BIZ_MASS_MSG, operation = OperationType.FULL)
    public ApiResponse<Void> delete(@RequestBody MassMsgDeleteRequest request, HttpSession session) {
        StaffPo current = currentStaffService.requireStaffAdmin(session);
        massMsgService.deleteGroupChatTimed(request, current.getExtCorpId());
        return ApiResponse.ok(null);
    }

    @PostMapping("/group-chat/mass-msg/action/refresh-result")
    @RequirePermission(biz = BizIdentity.BIZ_MASS_MSG, operation = OperationType.FULL)
    public ApiResponse<Void> refreshResult(@RequestBody MassMsgDeleteRequest request, HttpSession session) {
        StaffPo current = currentStaffService.requireStaffAdmin(session);
        massMsgService.refreshGroupChatResults(request == null ? null : request.getIds(), current.getExtCorpId());
        return ApiResponse.ok(null);
    }

    @GetMapping("/group-chat/mass-msgs")
    @RequirePermission(biz = BizIdentity.BIZ_MASS_MSG, operation = OperationType.READ)
    public ApiResponse<PageResponse<GroupChatMassMsgPo>> query(
            @RequestParam(defaultValue = "1") @Min(1) long page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(200) long pageSize,
            HttpSession session) {
        StaffPo current = currentStaffService.requireStaffAdmin(session);
        return ApiResponse.ok(massMsgService.queryGroupChat(current.getExtCorpId(), page, pageSize));
    }
}
