package cn.openscrm.api.groupchatwelcome.controller;

import cn.openscrm.api.auth.annotation.RequirePermission;
import cn.openscrm.api.auth.service.CurrentStaffService;
import cn.openscrm.api.common.api.ApiResponse;
import cn.openscrm.api.common.api.PageResponse;
import cn.openscrm.api.common.constant.BizIdentity;
import cn.openscrm.api.common.constant.OperationType;
import cn.openscrm.api.groupchatwelcome.dto.CommonIdsRequest;
import cn.openscrm.api.groupchatwelcome.dto.GroupChatWelcomeMsgRequest;
import cn.openscrm.api.groupchatwelcome.service.GroupChatWelcomeMsgService;
import cn.openscrm.api.persistence.entity.GroupChatWelcomeMsgPo;
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
@RequestMapping({"/api/v1/staff-admin", "/api/v1/staff_admin"})
public class GroupChatWelcomeMsgController {

    private final CurrentStaffService currentStaffService;
    private final GroupChatWelcomeMsgService service;

    @GetMapping({"/group-chat/welcome-msgs", "/customer-group/welcome-msgs"})
    @RequirePermission(biz = BizIdentity.BIZ_CUSTOMER_GROUP_CHAT, operation = OperationType.READ)
    public ApiResponse<PageResponse<GroupChatWelcomeMsgPo>> query(
            @RequestParam(required = false) String content,
            @RequestParam(defaultValue = "1") @Min(1) long page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(200) long pageSize,
            HttpSession session) {
        StaffPo current = currentStaffService.requireStaffAdmin(session);
        return ApiResponse.ok(service.query(current.getExtCorpId(), content, page, pageSize));
    }

    @PostMapping({"/group-chat/welcome-msg", "/customer-group/welcome-msg"})
    @RequirePermission(biz = BizIdentity.BIZ_CUSTOMER_GROUP_CHAT, operation = OperationType.FULL)
    public ApiResponse<GroupChatWelcomeMsgPo> create(
            @Valid @RequestBody GroupChatWelcomeMsgRequest request,
            HttpSession session) {
        StaffPo current = currentStaffService.requireStaffAdmin(session);
        return ApiResponse.ok(service.create(request, current.getExtCorpId(), current.getExtId()));
    }

    @PutMapping({"/group-chat/welcome-msg/{id}", "/customer-group/welcome-msg/{id}"})
    @RequirePermission(biz = BizIdentity.BIZ_CUSTOMER_GROUP_CHAT, operation = OperationType.FULL)
    public ApiResponse<GroupChatWelcomeMsgPo> update(
            @PathVariable Long id,
            @Valid @RequestBody GroupChatWelcomeMsgRequest request,
            HttpSession session) {
        StaffPo current = currentStaffService.requireStaffAdmin(session);
        return ApiResponse.ok(service.update(id, request, current.getExtCorpId()));
    }

    @PostMapping({"/group-chat/welcome-msg/action/delete", "/customer-group/welcome-msg/action/delete"})
    @RequirePermission(biz = BizIdentity.BIZ_CUSTOMER_GROUP_CHAT, operation = OperationType.FULL)
    public ApiResponse<Long> delete(@Valid @RequestBody CommonIdsRequest request) {
        return ApiResponse.ok(service.delete(request));
    }
}
