package cn.openscrm.api.quickreply.controller;

import cn.openscrm.api.auth.annotation.RequirePermission;
import cn.openscrm.api.auth.service.CurrentStaffService;
import cn.openscrm.api.common.api.ApiResponse;
import cn.openscrm.api.common.api.PageResponse;
import cn.openscrm.api.common.constant.BizIdentity;
import cn.openscrm.api.common.constant.OperationType;
import cn.openscrm.api.persistence.entity.QuickReplyPo;
import cn.openscrm.api.persistence.entity.StaffPo;
import cn.openscrm.api.quickreply.dto.QuickReplyDeleteRequest;
import cn.openscrm.api.quickreply.dto.QuickReplyRequest;
import cn.openscrm.api.quickreply.dto.QuickReplyResponse;
import cn.openscrm.api.quickreply.service.QuickReplyService;
import cn.openscrm.api.storage.dto.UploadUrlRequest;
import cn.openscrm.api.storage.dto.UploadUrlResponse;
import cn.openscrm.api.storage.service.FileStorageService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.servlet.http.HttpSession;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.util.StringUtils;
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
@RequestMapping("/api/v1")
public class QuickReplyController {

    private final CurrentStaffService currentStaffService;
    private final QuickReplyService quickReplyService;
    private final FileStorageService fileStorageService;

    @PostMapping({"/staff-admin/quick-reply", "/staff_admin/quick-reply"})
    @RequirePermission(biz = BizIdentity.BIZ_QUICK_REPLY, operation = OperationType.FULL)
    public ApiResponse<QuickReplyPo> create(@RequestBody QuickReplyRequest request, HttpSession session) {
        StaffPo current = currentStaffService.requireStaffAdmin(session);
        return ApiResponse.ok(quickReplyService.create(request, current));
    }

    @GetMapping({"/staff-admin/quick-replies", "/staff_admin/quick-replies"})
    @RequirePermission(biz = BizIdentity.BIZ_QUICK_REPLY, operation = OperationType.READ)
    public ApiResponse<PageResponse<QuickReplyResponse>> query(
            @RequestParam(required = false) String keyword,
            @RequestParam(value = "group_id", required = false) String groupId,
            @RequestParam(value = "department_ids", required = false) String departmentIds,
            @RequestParam(defaultValue = "1") @Min(1) long page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(200) long pageSize,
            HttpSession session) {
        StaffPo current = currentStaffService.requireStaffAdmin(session);
        return ApiResponse.ok(quickReplyService.query(
                current.getExtCorpId(),
                keyword,
                groupId,
                splitLong(departmentIds),
                page,
                pageSize));
    }

    @GetMapping({"/staff-frontend/quick-replies", "/staff_frontend/quick-replies"})
    public ApiResponse<PageResponse<QuickReplyResponse>> queryFrontend(
            @RequestParam(required = false) String keyword,
            @RequestParam(value = "group_id", required = false) String groupId,
            @RequestParam(value = "department_ids", required = false) String departmentIds,
            @RequestParam(defaultValue = "1") @Min(1) long page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(200) long pageSize,
            HttpSession session) {
        StaffPo current = currentStaffService.requireStaff(session);
        return ApiResponse.ok(quickReplyService.query(
                current.getExtCorpId(),
                keyword,
                groupId,
                splitLong(departmentIds),
                page,
                pageSize));
    }

    @PostMapping({"/staff-admin/quick-reply/action/delete", "/staff_admin/quick-reply/action/delete"})
    @RequirePermission(biz = BizIdentity.BIZ_QUICK_REPLY, operation = OperationType.FULL)
    public ApiResponse<Integer> delete(@RequestBody QuickReplyDeleteRequest request, HttpSession session) {
        StaffPo current = currentStaffService.requireStaffAdmin(session);
        return ApiResponse.ok(quickReplyService.delete(request, current.getExtCorpId()));
    }

    @PutMapping({"/staff-admin/quick-reply", "/staff_admin/quick-reply"})
    @RequirePermission(biz = BizIdentity.BIZ_QUICK_REPLY, operation = OperationType.FULL)
    public ApiResponse<QuickReplyResponse> update(@RequestBody QuickReplyRequest request, HttpSession session) {
        StaffPo current = currentStaffService.requireStaffAdmin(session);
        return ApiResponse.ok(quickReplyService.update(request, current));
    }

    @PostMapping({"/staff-admin/quick-reply/action/get-upload-url",
            "/staff_admin/quick-reply/action/get-upload-url"})
    @RequirePermission(biz = BizIdentity.BIZ_QUICK_REPLY, operation = OperationType.FULL)
    public ApiResponse<UploadUrlResponse> getUploadUrl(@RequestBody UploadUrlRequest request, HttpSession session) {
        StaffPo current = currentStaffService.requireStaffAdmin(session);
        String fileName = fileStorageService.safeFileName(request == null ? null : request.getFileName());
        String staffName = StringUtils.hasText(current.getName()) ? current.getName() : current.getExtId();
        String objectKey = current.getExtCorpId() + "/quick_reply/" + staffName + "/" + fileName;
        long ttl = fileStorageService.signedUrlTtlSeconds();
        return ApiResponse.ok(new UploadUrlResponse(
                fileStorageService.signedUrl(objectKey, "PUT", ttl),
                fileStorageService.signedUrl(objectKey, "GET", ttl)));
    }

    private List<Long> splitLong(String value) {
        if (!StringUtils.hasText(value)) {
            return Collections.emptyList();
        }
        List<Long> values = new ArrayList<>();
        for (String item : value.split(",")) {
            if (StringUtils.hasText(item)) {
                values.add(Long.valueOf(item.trim()));
            }
        }
        return values;
    }
}
