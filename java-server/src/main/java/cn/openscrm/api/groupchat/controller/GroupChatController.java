package cn.openscrm.api.groupchat.controller;

import cn.openscrm.api.auth.annotation.RequirePermission;
import cn.openscrm.api.auth.service.CurrentStaffService;
import cn.openscrm.api.common.api.ApiResponse;
import cn.openscrm.api.common.api.PageResponse;
import cn.openscrm.api.common.constant.BizIdentity;
import cn.openscrm.api.common.constant.OperationType;
import cn.openscrm.api.groupchat.dto.GroupChatMainInfo;
import cn.openscrm.api.groupchat.dto.GroupChatResponse;
import cn.openscrm.api.groupchat.dto.GroupChatTagUpdateRequest;
import cn.openscrm.api.groupchat.service.GroupChatService;
import cn.openscrm.api.persistence.entity.StaffPo;
import java.time.LocalDate;
import java.util.List;
import javax.servlet.http.HttpSession;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
public class GroupChatController {

    private final CurrentStaffService currentStaffService;
    private final GroupChatService groupChatService;

    @GetMapping({"/customer-group", "/group-chats"})
    @RequirePermission(biz = BizIdentity.BIZ_CUSTOMER_INFO, operation = OperationType.READ)
    public ApiResponse<PageResponse<GroupChatResponse>> query(
            @RequestParam(required = false) String owners,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer status,
            @RequestParam(value = "create_time_start", required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate createTimeStart,
            @RequestParam(value = "create_time_end", required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate createTimeEnd,
            @RequestParam(value = "group_tag_ids", required = false) String groupTagIds,
            @RequestParam(value = "tags_union_type", required = false) String tagsUnionType,
            @RequestParam(defaultValue = "1") @Min(1) long page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(200) long pageSize,
            HttpSession session) {
        StaffPo current = currentStaffService.requireStaffAdmin(session);
        return ApiResponse.ok(groupChatService.query(
                current.getExtCorpId(),
                groupChatService.split(owners),
                name,
                status,
                createTimeStart,
                createTimeEnd,
                groupChatService.splitLong(groupTagIds),
                tagsUnionType,
                page,
                pageSize));
    }

    @GetMapping({"/customer-group/{extChatId}", "/group-chat/{extChatId}"})
    @RequirePermission(biz = BizIdentity.BIZ_CUSTOMER_INFO, operation = OperationType.READ)
    public ApiResponse<GroupChatResponse> get(@PathVariable String extChatId, HttpSession session) {
        StaffPo current = currentStaffService.requireStaffAdmin(session);
        return ApiResponse.ok(groupChatService.get(current.getExtCorpId(), extChatId));
    }

    @PostMapping({"/customer-group/action/get-all", "/group-chat/action/get-all"})
    @RequirePermission(biz = BizIdentity.BIZ_CUSTOMER_INFO, operation = OperationType.READ)
    public ApiResponse<PageResponse<GroupChatMainInfo>> getAll(
            @RequestParam(defaultValue = "1") @Min(1) long page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(200) long pageSize,
            HttpSession session) {
        StaffPo current = currentStaffService.requireStaffAdmin(session);
        return ApiResponse.ok(groupChatService.getAll(current.getExtCorpId(), page, pageSize));
    }

    @GetMapping({"/customer-group/owners", "/customer-group/tags", "/group-chat/owners"})
    @RequirePermission(biz = BizIdentity.BIZ_CUSTOMER_INFO, operation = OperationType.READ)
    public ApiResponse<List<StaffPo>> getAllOwners(HttpSession session) {
        StaffPo current = currentStaffService.requireStaffAdmin(session);
        return ApiResponse.ok(groupChatService.getAllOwners(current.getExtCorpId()));
    }

    @GetMapping({"/customer-group/action/export", "/customer-groups/action/export", "/group-chat/action/export"})
    @RequirePermission(biz = BizIdentity.BIZ_CUSTOMER_INFO, operation = OperationType.READ)
    public ResponseEntity<byte[]> export(
            @RequestParam(required = false) String owners,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer status,
            @RequestParam(value = "create_time_start", required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate createTimeStart,
            @RequestParam(value = "create_time_end", required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate createTimeEnd,
            @RequestParam(value = "group_tag_ids", required = false) String groupTagIds,
            @RequestParam(value = "tags_union_type", required = false) String tagsUnionType,
            HttpSession session) {
        StaffPo current = currentStaffService.requireStaffAdmin(session);
        byte[] content = groupChatService.exportXlsx(
                current.getExtCorpId(),
                groupChatService.split(owners),
                name,
                status,
                createTimeStart,
                createTimeEnd,
                groupChatService.splitLong(groupTagIds),
                tagsUnionType);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename("group_chats.xlsx").build().toString())
                .body(content);
    }

    @PostMapping({"/customer-group/action/update-tags", "/group-chat/action/update-tags"})
    @RequirePermission(biz = BizIdentity.BIZ_CUSTOMER_INFO, operation = OperationType.FULL)
    public ApiResponse<Void> updateTags(@RequestBody GroupChatTagUpdateRequest request) {
        groupChatService.updateTags(request);
        return ApiResponse.ok(null);
    }

    @PostMapping("/customer-group/{extChatId}/action/sync")
    @RequirePermission(biz = BizIdentity.BIZ_CUSTOMER_INFO, operation = OperationType.FULL)
    public ApiResponse<Void> syncOne(@PathVariable String extChatId, HttpSession session) {
        StaffPo current = currentStaffService.requireStaffAdmin(session);
        groupChatService.syncOne(current.getExtCorpId(), extChatId);
        return ApiResponse.ok(null);
    }

    @PostMapping("/customer-group/action/sync")
    @RequirePermission(biz = BizIdentity.BIZ_CUSTOMER_INFO, operation = OperationType.FULL)
    public ApiResponse<Void> syncAll(HttpSession session) {
        StaffPo current = currentStaffService.requireStaffAdmin(session);
        groupChatService.syncAll(current.getExtCorpId());
        return ApiResponse.ok(null);
    }
}
