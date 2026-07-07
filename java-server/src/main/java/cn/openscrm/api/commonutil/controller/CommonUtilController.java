package cn.openscrm.api.commonutil.controller;

import cn.openscrm.api.auth.annotation.RequirePermission;
import cn.openscrm.api.auth.service.CurrentStaffService;
import cn.openscrm.api.common.api.ApiResponse;
import cn.openscrm.api.common.constant.BizIdentity;
import cn.openscrm.api.common.constant.OperationType;
import cn.openscrm.api.common.exception.BizException;
import cn.openscrm.api.common.exception.ErrorCode;
import cn.openscrm.api.commonutil.dto.ParseLinkRequest;
import cn.openscrm.api.commonutil.dto.ParseLinkResponse;
import cn.openscrm.api.commonutil.service.CommonUtilService;
import cn.openscrm.api.persistence.entity.StaffPo;
import cn.openscrm.api.storage.dto.UploadUrlRequest;
import cn.openscrm.api.storage.dto.UploadUrlResponse;
import cn.openscrm.api.storage.service.FileStorageService;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CommonUtilController {

    private static final Set<String> ALLOWED_EXTENSIONS = new HashSet<>(Arrays.asList(
            "jpg", "jpeg", "png", "bmp", "gif", "doc", "docx", "txt", "xls", "xlsx", "pdf", "ppt", "pptx",
            "mp4", "rm", "rmvb", "mkv", "avi"));

    private final CurrentStaffService currentStaffService;
    private final FileStorageService fileStorageService;
    private final CommonUtilService commonUtilService;

    @PostMapping({"/api/v1/staff-admin/common/action/parse-link", "/api/v1/staff_admin/common/action/parse-link"})
    @RequirePermission(biz = BizIdentity.BIZ_QUICK_REPLY, operation = OperationType.FULL)
    public ApiResponse<ParseLinkResponse> parseLink(@Valid @RequestBody ParseLinkRequest request) {
        return ApiResponse.ok(commonUtilService.parseLink(request.getUrl()));
    }

    @PostMapping({
            "/api/v1/staff-admin/common/action/get-signed-url",
            "/api/v1/staff_admin/common/action/get-signed-url",
            "/api/v1/staff-admin/storage/action/get-signed-url",
            "/api/v1/staff_admin/storage/action/get-signed-url"
    })
    @RequirePermission(biz = BizIdentity.BIZ_MEDIA_MGR, operation = OperationType.FULL)
    public ApiResponse<UploadUrlResponse> getSignedUrl(@RequestBody UploadUrlRequest request, HttpSession session) {
        StaffPo current = currentStaffService.requireStaffAdmin(session);
        String fileName = fileStorageService.safeFileName(request == null ? null : request.getFileName());
        String extension = extension(fileName);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BizException(ErrorCode.UNSUPPORTED_FILE_TYPE);
        }
        String objectKey = current.getExtCorpId() + "/public/" + fileName;
        return ApiResponse.ok(new UploadUrlResponse(
                fileStorageService.signedUrl(objectKey, "PUT", 3600),
                fileStorageService.signedUrl(objectKey, "GET", 10L * 356 * 24 * 3600)));
    }

    private String extension(String fileName) {
        int index = fileName.lastIndexOf('.');
        return index < 0 ? "" : fileName.substring(index + 1).toLowerCase(Locale.ROOT);
    }
}
