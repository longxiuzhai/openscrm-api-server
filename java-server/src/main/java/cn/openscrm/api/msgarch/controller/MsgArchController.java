package cn.openscrm.api.msgarch.controller;

import cn.openscrm.api.auth.annotation.RequirePermission;
import cn.openscrm.api.auth.service.CurrentStaffService;
import cn.openscrm.api.common.api.ApiResponse;
import cn.openscrm.api.common.api.PageResponse;
import cn.openscrm.api.common.constant.BizIdentity;
import cn.openscrm.api.common.constant.OperationType;
import cn.openscrm.api.msgarch.dto.ChatMessageResponse;
import cn.openscrm.api.msgarch.dto.ChatSessionResponse;
import cn.openscrm.api.msgarch.dto.MsgArchSyncResponse;
import cn.openscrm.api.msgarch.service.MsgArchService;
import cn.openscrm.api.persistence.entity.StaffPo;
import javax.servlet.http.HttpSession;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping({"/api/v1/staff-admin", "/api/v1/staff_admin"})
public class MsgArchController {

    private final CurrentStaffService currentStaffService;
    private final MsgArchService msgArchService;

    @GetMapping({"/customers/chat-sessions", "/customer/chat-sessions"})
    @RequirePermission(biz = BizIdentity.BIZ_MSG_ARCH, operation = OperationType.READ)
    public ApiResponse<PageResponse<ChatSessionResponse>> querySessions(
            @RequestParam(required = false) String name,
            @RequestParam(value = "ext_staff_id") String extStaffId,
            @RequestParam(value = "session_type", required = false) String sessionType,
            @RequestParam(defaultValue = "1") @Min(1) long page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(200) long pageSize,
            HttpSession session) {
        StaffPo current = currentStaffService.requireStaffAdmin(session);
        return ApiResponse.ok(msgArchService.querySessions(
                current.getExtCorpId(), extStaffId, sessionType, name, page, pageSize));
    }

    @PostMapping("/chat-msg/sync")
    @RequirePermission(biz = BizIdentity.BIZ_MSG_ARCH, operation = OperationType.FULL)
    public ApiResponse<MsgArchSyncResponse> sync(HttpSession session) {
        StaffPo current = currentStaffService.requireStaffAdmin(session);
        return ApiResponse.ok(msgArchService.sync(current.getExtCorpId()));
    }

    @GetMapping({"/customer/sessions-msgs", "/customer/session-msgs"})
    @RequirePermission(biz = BizIdentity.BIZ_MSG_ARCH, operation = OperationType.READ)
    public ApiResponse<PageResponse<ChatMessageResponse>> querySessionMsgs(
            @RequestParam(value = "ext_staff_id") String extStaffId,
            @RequestParam(value = "receiver_id") String receiverId,
            @RequestParam(value = "msg_type", required = false) String msgType,
            @RequestParam(value = "send_at_start", required = false) Long sendAtStart,
            @RequestParam(value = "send_at_end", required = false) Long sendAtEnd,
            @RequestParam(value = "min_id", required = false) Long minId,
            @RequestParam(value = "max_id", required = false) Long maxId,
            @RequestParam(defaultValue = "1") @Min(1) long page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(200) long pageSize,
            HttpSession session) {
        StaffPo current = currentStaffService.requireStaffAdmin(session);
        return ApiResponse.ok(msgArchService.queryMsgs(
                current.getExtCorpId(), extStaffId, receiverId, msgType,
                sendAtStart, sendAtEnd, minId, maxId, page, pageSize));
    }

    @PostMapping({"/customer/sessions-msg/action/search", "/customer/session-msg/action/search"})
    @RequirePermission(biz = BizIdentity.BIZ_MSG_ARCH, operation = OperationType.READ)
    public ApiResponse<PageResponse<ChatMessageResponse>> searchMsgs(
            @RequestParam String keyword,
            @RequestParam(value = "ext_staff_id") String extStaffId,
            @RequestParam(value = "ext_peer_id") String extPeerId,
            @RequestParam(defaultValue = "1") @Min(1) long page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(200) long pageSize,
            HttpSession session) {
        StaffPo current = currentStaffService.requireStaffAdmin(session);
        return ApiResponse.ok(msgArchService.searchMsgs(
                current.getExtCorpId(), extStaffId, extPeerId, keyword, page, pageSize));
    }
}
