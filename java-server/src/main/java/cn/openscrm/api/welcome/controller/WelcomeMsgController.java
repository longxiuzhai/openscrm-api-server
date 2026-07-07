package cn.openscrm.api.welcome.controller;

import cn.openscrm.api.auth.annotation.RequirePermission;
import cn.openscrm.api.auth.service.CurrentStaffService;
import cn.openscrm.api.common.api.ApiResponse;
import cn.openscrm.api.common.api.PageResponse;
import cn.openscrm.api.common.constant.AppEnv;
import cn.openscrm.api.common.constant.BizIdentity;
import cn.openscrm.api.common.constant.OperationType;
import cn.openscrm.api.common.exception.BizException;
import cn.openscrm.api.common.exception.ErrorCode;
import cn.openscrm.api.config.OpenScrmProperties;
import cn.openscrm.api.persistence.entity.StaffPo;
import cn.openscrm.api.persistence.entity.WelcomeMsgPo;
import cn.openscrm.api.welcome.dto.CommonDeleteRequest;
import cn.openscrm.api.welcome.dto.WelcomeMsgRequest;
import cn.openscrm.api.welcome.dto.WelcomeMsgResponse;
import cn.openscrm.api.welcome.service.WelcomeMsgService;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletRequest;
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
@RequestMapping("/api/v1")
public class WelcomeMsgController {

    private final WelcomeMsgService welcomeMsgService;
    private final CurrentStaffService currentStaffService;
    private final OpenScrmProperties properties;

    @PostMapping({"/staff-admin/customer/welcome-msg", "/staff_admin/customer/welcome-msg", "/customer/welcome-msg"})
    @RequirePermission(biz = BizIdentity.BIZ_WELCOME_MSG, operation = OperationType.FULL)
    public ApiResponse<WelcomeMsgPo> create(@RequestBody WelcomeMsgRequest request, HttpSession session) {
        StaffPo current = currentStaffService.requireStaffAdmin(session);
        return ApiResponse.ok(welcomeMsgService.create(request, current.getExtCorpId(), current.getExtId()));
    }

    @GetMapping({"/staff-admin/customer/welcome-msg", "/staff-admin/customer/welcome-msgs",
            "/staff_admin/customer/welcome-msg", "/staff_admin/customer/welcome-msgs"})
    @RequirePermission(biz = BizIdentity.BIZ_WELCOME_MSG, operation = OperationType.READ)
    public ApiResponse<PageResponse<WelcomeMsgResponse>> query(
            @RequestParam(required = false) String name,
            @RequestParam(value = "ext_staff_ids", required = false) String extStaffIds,
            @RequestParam(defaultValue = "1") @Min(1) long page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(200) long pageSize,
            HttpSession session) {
        StaffPo current = currentStaffService.requireStaffAdmin(session);
        return ApiResponse.ok(welcomeMsgService.query(current.getExtCorpId(), name, extStaffIds, page, pageSize));
    }

    @PutMapping({"/staff-admin/customer/welcome-msg/{id}", "/staff_admin/customer/welcome-msg/{id}"})
    @RequirePermission(biz = BizIdentity.BIZ_WELCOME_MSG, operation = OperationType.FULL)
    public ApiResponse<WelcomeMsgPo> update(@PathVariable Long id,
                                            @RequestBody WelcomeMsgRequest request,
                                            HttpSession session) {
        StaffPo current = currentStaffService.requireStaffAdmin(session);
        return ApiResponse.ok(welcomeMsgService.update(id, request, current.getExtCorpId()));
    }

    @PostMapping({"/staff-admin/customer/welcome-msg/action/delete",
            "/staff_admin/customer/welcome-msg/action/delete"})
    @RequirePermission(biz = BizIdentity.BIZ_WELCOME_MSG, operation = OperationType.FULL)
    public ApiResponse<Void> delete(@RequestBody CommonDeleteRequest request, HttpSession session) {
        StaffPo current = currentStaffService.requireStaffAdmin(session);
        welcomeMsgService.delete(request, current.getExtCorpId());
        return ApiResponse.ok(null);
    }

    @GetMapping({"/staff-admin/welcome-msg/{id}", "/staff-admin/customer/welcome-msg/{id}",
            "/staff_admin/welcome-msg/{id}", "/staff_admin/customer/welcome-msg/{id}"})
    @RequirePermission(biz = BizIdentity.BIZ_WELCOME_MSG, operation = OperationType.READ)
    public ApiResponse<WelcomeMsgResponse> get(@PathVariable Long id, HttpSession session) {
        StaffPo current = currentStaffService.requireStaffAdmin(session);
        return ApiResponse.ok(welcomeMsgService.get(id, current.getExtCorpId()));
    }

    @GetMapping({"/staff-admin/welcome-msg/action/get-upload-url",
            "/staff_admin/welcome-msg/action/get-upload-url"})
    @RequirePermission(biz = BizIdentity.BIZ_WELCOME_MSG, operation = OperationType.FULL)
    public ApiResponse<String> uploadFileUrl(@RequestParam("file_name") String filename,
                                             HttpServletRequest request,
                                             HttpSession session) throws java.io.IOException {
        if (!AppEnv.DEV.name().equalsIgnoreCase(properties.getEnv())
                && !AppEnv.TEST.name().equalsIgnoreCase(properties.getEnv())) {
            throw new BizException(ErrorCode.FORBIDDEN);
        }
        StaffPo current = currentStaffService.requireStaffAdmin(session);
        return ApiResponse.ok(welcomeMsgService.uploadImage(request.getInputStream(), filename, current.getExtCorpId()));
    }

    @PostMapping({"/staff-admin/customer/welcome-msg/action/upload-image",
            "/staff_admin/customer/welcome-msg/action/upload-image"})
    @RequirePermission(biz = BizIdentity.BIZ_WELCOME_MSG, operation = OperationType.FULL)
    public ApiResponse<String> uploadImageCompat(@RequestParam("file_name") String filename,
                                                 HttpServletRequest request,
                                                 HttpSession session) throws java.io.IOException {
        return uploadFileUrl(filename, request, session);
    }
}
