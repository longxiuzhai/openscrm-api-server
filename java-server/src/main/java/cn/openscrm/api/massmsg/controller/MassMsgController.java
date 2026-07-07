package cn.openscrm.api.massmsg.controller;

import cn.openscrm.api.auth.annotation.RequirePermission;
import cn.openscrm.api.auth.service.CurrentStaffService;
import cn.openscrm.api.common.api.ApiResponse;
import cn.openscrm.api.common.api.PageResponse;
import cn.openscrm.api.common.constant.BizIdentity;
import cn.openscrm.api.common.constant.OperationType;
import cn.openscrm.api.massmsg.dto.CustomerFilterCountResponse;
import cn.openscrm.api.massmsg.dto.MassMsgDeleteRequest;
import cn.openscrm.api.massmsg.dto.MassMsgDetailResponse;
import cn.openscrm.api.massmsg.dto.MassMsgNotifyRequest;
import cn.openscrm.api.massmsg.dto.MassMsgRequest;
import cn.openscrm.api.massmsg.dto.MassMsgResultResponse;
import cn.openscrm.api.massmsg.service.MassMsgService;
import cn.openscrm.api.persistence.entity.MassMsgPo;
import cn.openscrm.api.persistence.entity.StaffPo;
import cn.openscrm.api.storage.dto.UploadUrlRequest;
import cn.openscrm.api.storage.dto.UploadUrlResponse;
import cn.openscrm.api.storage.service.FileStorageService;
import com.fasterxml.jackson.databind.JsonNode;
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
@RequestMapping({"/api/v1/staff-admin/customer", "/api/v1/staff_admin/customer"})
public class MassMsgController {

    private final CurrentStaffService currentStaffService;
    private final MassMsgService massMsgService;
    private final FileStorageService fileStorageService;

    @PostMapping("/mass-msg")
    @RequirePermission(biz = BizIdentity.BIZ_MASS_MSG, operation = OperationType.FULL)
    public ApiResponse<MassMsgPo> create(@RequestBody MassMsgRequest request, HttpSession session) {
        StaffPo current = currentStaffService.requireStaffAdmin(session);
        return ApiResponse.ok(massMsgService.create(request, current));
    }

    @GetMapping("/mass-msg/{id}")
    @RequirePermission(biz = BizIdentity.BIZ_MASS_MSG, operation = OperationType.READ)
    public ApiResponse<MassMsgDetailResponse> get(@PathVariable Long id, HttpSession session) {
        StaffPo current = currentStaffService.requireStaffAdmin(session);
        return ApiResponse.ok(massMsgService.get(id, current.getExtCorpId()));
    }

    @GetMapping("/mass-msg/result/{id}")
    @RequirePermission(biz = BizIdentity.BIZ_MASS_MSG, operation = OperationType.READ)
    public ApiResponse<MassMsgResultResponse> result(@PathVariable Long id, HttpSession session) {
        StaffPo current = currentStaffService.requireStaffAdmin(session);
        return ApiResponse.ok(massMsgService.result(id, current.getExtCorpId()));
    }

    @PostMapping("/mass-msg/action/refresh-result")
    @RequirePermission(biz = BizIdentity.BIZ_MASS_MSG, operation = OperationType.FULL)
    public ApiResponse<Void> refreshResult(@RequestBody MassMsgDeleteRequest request, HttpSession session) {
        StaffPo current = currentStaffService.requireStaffAdmin(session);
        massMsgService.refreshResults(request == null ? null : request.getIds(), current.getExtCorpId());
        return ApiResponse.ok(null);
    }

    @PostMapping("/mass-msg/action/delete")
    @RequirePermission(biz = BizIdentity.BIZ_MASS_MSG, operation = OperationType.FULL)
    public ApiResponse<Void> delete(@RequestBody MassMsgDeleteRequest request, HttpSession session) {
        StaffPo current = currentStaffService.requireStaffAdmin(session);
        massMsgService.deleteTimed(request, current.getExtCorpId());
        return ApiResponse.ok(null);
    }

    @PostMapping({"/mass-msg/notify", "/mass-msg/action/notify"})
    @RequirePermission(biz = BizIdentity.BIZ_MASS_MSG, operation = OperationType.FULL)
    public ApiResponse<Void> notifyStaff(@RequestBody MassMsgNotifyRequest request, HttpSession session) {
        StaffPo current = currentStaffService.requireStaffAdmin(session);
        massMsgService.notifyStaff(request == null ? null : request.getIds(), current.getExtCorpId());
        return ApiResponse.ok(null);
    }

    @GetMapping("/mass-msgs")
    @RequirePermission(biz = BizIdentity.BIZ_MASS_MSG, operation = OperationType.READ)
    public ApiResponse<PageResponse<MassMsgPo>> query(
            @RequestParam(defaultValue = "1") @Min(1) long page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(200) long pageSize,
            HttpSession session) {
        StaffPo current = currentStaffService.requireStaffAdmin(session);
        return ApiResponse.ok(massMsgService.query(current.getExtCorpId(), page, pageSize));
    }

    @PostMapping("/mass-msg/customer-filter")
    @RequirePermission(biz = BizIdentity.BIZ_MASS_MSG, operation = OperationType.READ)
    public ApiResponse<CustomerFilterCountResponse> customerFilter(@RequestBody MassMsgRequest request,
                                                                   HttpSession session) {
        StaffPo current = currentStaffService.requireStaffAdmin(session);
        return ApiResponse.ok(massMsgService.countCustomers(
                current.getExtCorpId(),
                request == null ? null : request.getExtCustomerFilterEnable(),
                request == null ? null : request.getExtCustomerFilter()));
    }

    @GetMapping("/mass-msg/customer-filter")
    @RequirePermission(biz = BizIdentity.BIZ_MASS_MSG, operation = OperationType.READ)
    public ApiResponse<CustomerFilterCountResponse> customerFilterGet(JsonNode ignored, HttpSession session) {
        StaffPo current = currentStaffService.requireStaffAdmin(session);
        return ApiResponse.ok(massMsgService.countCustomers(current.getExtCorpId(), 0, null));
    }

    @PutMapping("/mass-msg/{id}")
    @RequirePermission(biz = BizIdentity.BIZ_MASS_MSG, operation = OperationType.FULL)
    public ApiResponse<MassMsgPo> update(@PathVariable Long id,
                                         @RequestBody MassMsgRequest request,
                                         HttpSession session) {
        StaffPo current = currentStaffService.requireStaffAdmin(session);
        return ApiResponse.ok(massMsgService.update(id, request, current));
    }

    @PostMapping("/mass-msg/action/get-upload-url")
    @RequirePermission(biz = BizIdentity.BIZ_MASS_MSG, operation = OperationType.FULL)
    public ApiResponse<UploadUrlResponse> getUploadUrl(@RequestBody UploadUrlRequest request, HttpSession session) {
        StaffPo current = currentStaffService.requireStaffAdmin(session);
        String fileName = fileStorageService.safeFileName(request == null ? null : request.getFileName());
        String objectKey = current.getExtCorpId() + "/quick_reply/" + current.getName() + "/" + fileName;
        return ApiResponse.ok(new UploadUrlResponse(
                fileStorageService.signedUrl(objectKey, "PUT", 3600),
                fileStorageService.signedUrl(objectKey, "GET", 3600)));
    }
}
