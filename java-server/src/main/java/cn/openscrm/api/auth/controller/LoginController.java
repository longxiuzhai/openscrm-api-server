package cn.openscrm.api.auth.controller;

import cn.openscrm.api.auth.dto.CustomerLoginSuccessResponse;
import cn.openscrm.api.auth.dto.ForceLoginRequest;
import cn.openscrm.api.auth.dto.LoginRequest;
import cn.openscrm.api.auth.dto.LoginSuccessResponse;
import cn.openscrm.api.auth.dto.LoginUrlResponse;
import cn.openscrm.api.auth.service.LoginService;
import cn.openscrm.api.common.api.ApiResponse;
import cn.openscrm.api.common.exception.BizException;
import cn.openscrm.api.common.exception.ErrorCode;
import cn.openscrm.api.persistence.entity.StaffPo;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class LoginController {

    private final LoginService loginService;

    @GetMapping({"/staff-admin/action/login", "/staff_admin/action/login"})
    public void staffAdminLoginRedirect(@RequestParam(value = "ext_corp_id", required = false) String extCorpId,
                                        @RequestParam("source_url") String sourceUrl,
                                        HttpSession session,
                                        HttpServletResponse response) throws IOException {
        LoginRequest request = new LoginRequest();
        request.setExtCorpId(extCorpId);
        request.setSourceUrl(sourceUrl);
        response.sendRedirect(loginService.staffAdminLogin(request, session).getLocationUrl());
    }

    @PostMapping({"/staff-admin/action/login", "/staff_admin/action/login"})
    public ApiResponse<LoginUrlResponse> staffAdminLogin(@RequestBody LoginRequest request, HttpSession session) {
        return ApiResponse.ok(loginService.staffAdminLogin(request, session));
    }

    @GetMapping({"/staff-admin/action/login-callback", "/staff_admin/action/login-callback",
            "/staff_admin/action/login_callback"})
    public void staffAdminLoginCallbackRedirect(@RequestParam("appid") String appId,
                                                @RequestParam("code") String code,
                                                @RequestParam("state") String state,
                                                HttpSession session,
                                                HttpServletResponse response) throws IOException {
        loginService.staffAdminLoginCallback(appId, code, state, session);
        response.sendRedirect("/staff-admin/login-callback");
    }

    @PostMapping({"/staff-admin/action/login-callback", "/staff_admin/action/login-callback",
            "/staff_admin/action/login_callback"})
    public ApiResponse<LoginSuccessResponse> staffAdminLoginCallback(@RequestParam("appid") String appId,
                                                                     @RequestParam("code") String code,
                                                                     @RequestParam("state") String state,
                                                                     HttpSession session) {
        return ApiResponse.ok(loginService.staffAdminLoginCallback(appId, code, state, session));
    }

    @PostMapping({"/staff-admin/action/force-login", "/staff_admin/action/force-login"})
    public ApiResponse<StaffPo> staffAdminForceLogin(@RequestBody ForceLoginRequest request, HttpSession session) {
        return ApiResponse.ok(loginService.forceStaffAdminLogin(request, session));
    }

    @GetMapping({"/staff-frontend/action/login", "/staff_frontend/action/login"})
    public void staffLoginRedirect(@RequestParam(value = "ext_corp_id", required = false) String extCorpId,
                                   @RequestParam("source_url") String sourceUrl,
                                   HttpSession session,
                                   HttpServletResponse response) throws IOException {
        LoginRequest request = new LoginRequest();
        request.setExtCorpId(extCorpId);
        request.setSourceUrl(sourceUrl);
        response.sendRedirect(loginService.staffLogin(request, session).getLocationUrl());
    }

    @PostMapping({"/staff-frontend/action/login", "/staff_frontend/action/login"})
    public ApiResponse<LoginUrlResponse> staffLogin(@RequestBody LoginRequest request, HttpSession session) {
        return ApiResponse.ok(loginService.staffLogin(request, session));
    }

    @GetMapping({"/staff-frontend/action/login-callback", "/staff_frontend/action/login-callback",
            "/staff_frontend/action/login_callback"})
    public void staffLoginCallbackRedirect(@RequestParam("appid") String appId,
                                           @RequestParam("code") String code,
                                           @RequestParam("source_url") String sourceUrl,
                                           HttpSession session,
                                           HttpServletResponse response) throws IOException {
        loginService.staffLoginCallback(appId, code, session);
        response.sendRedirect(decodeSourceUrl(sourceUrl));
    }

    @PostMapping({"/staff-frontend/action/login-callback", "/staff_frontend/action/login-callback",
            "/staff_frontend/action/login_callback"})
    public ApiResponse<LoginSuccessResponse> staffLoginCallback(@RequestParam("appid") String appId,
                                                                @RequestParam("code") String code,
                                                                HttpSession session) {
        return ApiResponse.ok(loginService.staffLoginCallback(appId, code, session));
    }

    @PostMapping({"/staff-frontend/action/force-login", "/staff_frontend/action/force-login"})
    public ApiResponse<StaffPo> staffForceLogin(@RequestBody ForceLoginRequest request, HttpSession session) {
        return ApiResponse.ok(loginService.forceStaffLogin(request, session));
    }

    @GetMapping({"/staff-frontend/action/force-login", "/staff_frontend/action/force-login"})
    public ApiResponse<StaffPo> staffForceLoginGet(@RequestParam(value = "ext_corp_id", required = false) String extCorpId,
                                                   @RequestParam(value = "ext_staff_id", required = false) String extStaffId,
                                                   HttpSession session) {
        ForceLoginRequest request = new ForceLoginRequest();
        request.setExtCorpId(extCorpId);
        request.setExtStaffId(extStaffId);
        return ApiResponse.ok(loginService.forceStaffLogin(request, session));
    }

    @GetMapping({"/customer-frontend/action/login-callback", "/customer_frontend/action/login-callback",
            "/customer_frontend/action/login_callback"})
    public void customerLoginCallbackRedirect(@RequestParam("code") String code,
                                              @RequestParam("source_url") String sourceUrl,
                                              HttpSession session,
                                              HttpServletResponse response) throws IOException {
        loginService.customerLoginCallback(code, session);
        response.sendRedirect(decodeSourceUrl(sourceUrl));
    }

    @PostMapping({"/customer-frontend/action/login-callback", "/customer_frontend/action/login-callback",
            "/customer_frontend/action/login_callback"})
    public ApiResponse<CustomerLoginSuccessResponse> customerLoginCallback(@RequestParam("code") String code,
                                                                           HttpSession session) {
        return ApiResponse.ok(loginService.customerLoginCallback(code, session));
    }

    private String decodeSourceUrl(String sourceUrl) {
        if (!StringUtils.hasText(sourceUrl)) {
            throw new BizException(ErrorCode.BAD_REQUEST);
        }
        try {
            return URLDecoder.decode(sourceUrl, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            throw new BizException(ErrorCode.ILLEGAL_URL);
        }
    }
}
