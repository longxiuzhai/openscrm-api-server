package cn.openscrm.api.stafffrontend.controller;

import cn.openscrm.api.auth.service.CurrentStaffService;
import cn.openscrm.api.common.api.ApiResponse;
import cn.openscrm.api.persistence.entity.StaffPo;
import cn.openscrm.api.stafffrontend.dto.JsAgentConfigResponse;
import cn.openscrm.api.stafffrontend.dto.JsConfigRequest;
import cn.openscrm.api.stafffrontend.dto.JsConfigResponse;
import cn.openscrm.api.stafffrontend.dto.UploadMediaRequest;
import cn.openscrm.api.stafffrontend.dto.UploadMediaResponse;
import cn.openscrm.api.stafffrontend.service.StaffFrontendUtilityService;
import javax.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class StaffFrontendUtilityController {

    private final CurrentStaffService currentStaffService;
    private final StaffFrontendUtilityService utilityService;

    @GetMapping({"/staff-frontend/action/get-current-staff", "/staff_frontend/action/get-current-staff"})
    public ApiResponse<StaffPo> currentStaff(HttpSession session) {
        return ApiResponse.ok(currentStaffService.requireStaff(session));
    }

    @PostMapping({"/staff-frontend/action/upload-media", "/staff_frontend/action/upload-media"})
    public ApiResponse<UploadMediaResponse> uploadMedia(@RequestBody UploadMediaRequest request, HttpSession session) {
        StaffPo staff = currentStaffService.requireStaff(session);
        return ApiResponse.ok(utilityService.uploadMedia(staff.getExtCorpId(), request));
    }

    @PostMapping({"/staff-frontend/action/get-js-config", "/staff_frontend/action/get-js-config"})
    public ApiResponse<JsConfigResponse> getJsConfig(@RequestBody JsConfigRequest request, HttpSession session) {
        StaffPo staff = currentStaffService.requireStaff(session);
        return ApiResponse.ok(utilityService.getJsConfig(staff.getExtCorpId(), request.getUrl()));
    }

    @GetMapping({"/staff-frontend/action/get-js-config", "/staff_frontend/action/get-js-config"})
    public ApiResponse<JsConfigResponse> getJsConfigByQuery(@RequestParam("url") String url, HttpSession session) {
        StaffPo staff = currentStaffService.requireStaff(session);
        return ApiResponse.ok(utilityService.getJsConfig(staff.getExtCorpId(), url));
    }

    @PostMapping({"/staff-frontend/action/get-js-agent-config", "/staff_frontend/action/get-js-agent-config"})
    public ApiResponse<JsAgentConfigResponse> getJsAgentConfig(@RequestBody JsConfigRequest request,
                                                               HttpSession session) {
        StaffPo staff = currentStaffService.requireStaff(session);
        return ApiResponse.ok(utilityService.getJsAgentConfig(staff.getExtCorpId(), request.getUrl()));
    }

    @GetMapping({"/staff-frontend/action/get-js-agent-config", "/staff_frontend/action/get-js-agent-config"})
    public ApiResponse<JsAgentConfigResponse> getJsAgentConfigByQuery(@RequestParam("url") String url,
                                                                      HttpSession session) {
        StaffPo staff = currentStaffService.requireStaff(session);
        return ApiResponse.ok(utilityService.getJsAgentConfig(staff.getExtCorpId(), url));
    }
}
