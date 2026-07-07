package cn.openscrm.api.material.controller;

import cn.openscrm.api.auth.annotation.RequirePermission;
import cn.openscrm.api.auth.service.CurrentStaffService;
import cn.openscrm.api.common.api.ApiResponse;
import cn.openscrm.api.common.constant.BizIdentity;
import cn.openscrm.api.common.constant.OperationType;
import cn.openscrm.api.persistence.entity.StaffPo;
import cn.openscrm.api.stafffrontend.dto.UploadMediaResponse;
import cn.openscrm.api.stafffrontend.service.StaffFrontendUtilityService;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.servlet.http.HttpSession;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TempMaterialController {

    private final CurrentStaffService currentStaffService;
    private final StaffFrontendUtilityService utilityService;

    @PostMapping({"/api/v1/staff-admin/material/temp", "/api/v1/staff_admin/material/temp"})
    @RequirePermission(biz = BizIdentity.BIZ_MEDIA_MGR, operation = OperationType.FULL)
    public ApiResponse<UploadMediaResponse> upload(@RequestBody TempMaterialRequest request, HttpSession session) {
        StaffPo current = currentStaffService.requireStaffAdmin(session);
        return ApiResponse.ok(utilityService.uploadMaterialTemp(current.getExtCorpId(), request.getFileUrl()));
    }

    @Getter
    @Setter
    public static class TempMaterialRequest {
        @JsonProperty("file_type")
        private String fileType;

        @JsonProperty("file_url")
        private String fileUrl;
    }
}
