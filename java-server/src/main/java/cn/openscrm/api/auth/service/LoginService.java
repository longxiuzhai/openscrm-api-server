package cn.openscrm.api.auth.service;

import cn.openscrm.api.auth.dto.CustomerLoginSuccessResponse;
import cn.openscrm.api.auth.dto.ForceLoginRequest;
import cn.openscrm.api.auth.dto.LoginRequest;
import cn.openscrm.api.auth.dto.LoginSuccessResponse;
import cn.openscrm.api.auth.dto.LoginUrlResponse;
import cn.openscrm.api.auth.session.SessionKeys;
import cn.openscrm.api.common.constant.AppEnv;
import cn.openscrm.api.common.exception.BizException;
import cn.openscrm.api.common.exception.ErrorCode;
import cn.openscrm.api.common.security.JwtTokenService;
import cn.openscrm.api.config.OpenScrmProperties;
import cn.openscrm.api.persistence.entity.CustomerPo;
import cn.openscrm.api.persistence.entity.StaffPo;
import cn.openscrm.api.persistence.mapper.CustomerPoMapper;
import cn.openscrm.api.persistence.mapper.StaffPoMapper;
import cn.openscrm.api.wework.UserIdentityResponse;
import cn.openscrm.api.wework.WeWorkClient;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpSession;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class LoginService {

    private final OpenScrmProperties properties;
    private final StaffPoMapper staffMapper;
    private final CustomerPoMapper customerMapper;
    private final CurrentStaffService currentStaffService;
    private final JwtTokenService jwtTokenService;
    private final WeWorkClient weWorkClient;
    private final SecureRandom secureRandom = new SecureRandom();

    public LoginService(OpenScrmProperties properties,
                        StaffPoMapper staffMapper,
                        CustomerPoMapper customerMapper,
                        CurrentStaffService currentStaffService,
                        JwtTokenService jwtTokenService,
                        WeWorkClient weWorkClient) {
        this.properties = properties;
        this.staffMapper = staffMapper;
        this.customerMapper = customerMapper;
        this.currentStaffService = currentStaffService;
        this.jwtTokenService = jwtTokenService;
        this.weWorkClient = weWorkClient;
    }

    public LoginUrlResponse staffAdminLogin(LoginRequest request, HttpSession session) {
        String extCorpId = resolveExtCorpId(request.getExtCorpId());
        String state = randomState();
        String sourceUrl = decodeRequiredUrl(request.getSourceUrl());
        URI sourceUri = parseRequiredAbsoluteUri(sourceUrl);
        String redirectUri = sourceUri.getScheme() + "://" + sourceUri.getHost()
                + (sourceUri.getPort() >= 0 ? ":" + sourceUri.getPort() : "")
                + "/api/v1/staff-admin/action/login-callback";

        LoginUrlResponse response = new LoginUrlResponse();
        response.setAppId(extCorpId);
        response.setAgentId(properties.getWeWork().getMainAgentId());
        response.setRedirectUri(redirectUri);
        response.setState(state);
        response.setLocationUrl("https://open.work.weixin.qq.com/wwopen/sso/qrConnect"
                + "?appid=" + encode(extCorpId)
                + "&agentid=" + properties.getWeWork().getMainAgentId()
                + "&redirect_uri=" + encode(redirectUri)
                + "&state=" + encode(state));
        session.setAttribute(SessionKeys.QRCODE_AUTH_STATE, state);
        return response;
    }

    public LoginSuccessResponse staffAdminLoginCallback(String appId, String code, String state, HttpSession session) {
        String savedState = (String) session.getAttribute(SessionKeys.QRCODE_AUTH_STATE);
        if (!StringUtils.hasText(savedState) || !savedState.equals(state)) {
            throw new BizException(ErrorCode.BAD_REQUEST);
        }
        StaffPo staff = loginByWeWorkCode(appId, code);
        session.setAttribute(SessionKeys.STAFF_ADMIN_INFO, currentStaffService.toSession(staff));
        return loginSuccess(staff);
    }

    public LoginUrlResponse staffLogin(LoginRequest request, HttpSession session) {
        String extCorpId = resolveExtCorpId(request.getExtCorpId());
        String state = randomState();
        String sourceUrl = decodeRequiredUrl(request.getSourceUrl());
        URI sourceUri = parseRequiredAbsoluteUri(sourceUrl);
        String redirectUri = sourceUri.getScheme() + "://" + sourceUri.getHost()
                + (sourceUri.getPort() >= 0 ? ":" + sourceUri.getPort() : "")
                + "/api/v1/staff-frontend/action/login-callback"
                + "?appid=" + encode(extCorpId)
                + "&source_url=" + encode(sourceUrl);

        LoginUrlResponse response = new LoginUrlResponse();
        response.setAppId(extCorpId);
        response.setRedirectUri(redirectUri);
        response.setSourceUrl(sourceUrl);
        response.setState(state);
        response.setLocationUrl("https://open.weixin.qq.com/connect/oauth2/authorize"
                + "?appid=" + encode(extCorpId)
                + "&redirect_uri=" + encode(redirectUri)
                + "&response_type=code"
                + "&scope=snsapi_base"
                + "&state=" + encode(state)
                + "#wechat_redirect");
        session.setAttribute(SessionKeys.QRCODE_AUTH_STATE, state);
        return response;
    }

    public LoginSuccessResponse staffLoginCallback(String appId, String code, HttpSession session) {
        StaffPo staff = loginByWeWorkCode(appId, code);
        session.setAttribute(SessionKeys.STAFF_INFO, currentStaffService.toSession(staff));
        return loginSuccess(staff);
    }

    public CustomerLoginSuccessResponse customerLoginCallback(String code, HttpSession session) {
        if (!StringUtils.hasText(code)) {
            throw new BizException(ErrorCode.BAD_REQUEST);
        }
        UserIdentityResponse userInfo = weWorkClient.getUserInfo(
                properties.getWeWork().getExtCorpId(),
                properties.getWeWork().getMainAgentSecret(),
                code);
        if (!StringUtils.hasText(userInfo.getUserId()) || !StringUtils.hasText(userInfo.getExternalUserId())) {
            throw new BizException(ErrorCode.FORBIDDEN);
        }
        CustomerPo customer = customerMapper.selectOne(new LambdaQueryWrapper<CustomerPo>()
                .eq(CustomerPo::getExtCorpId, properties.getWeWork().getExtCorpId())
                .eq(CustomerPo::getExtId, userInfo.getExternalUserId())
                .last("limit 1"));
        if (customer == null) {
            throw new BizException(ErrorCode.ITEM_NOT_FOUND);
        }
        session.setAttribute(SessionKeys.CUSTOMER_INFO, customer);
        return customerLoginSuccess(customer);
    }

    public StaffPo forceStaffAdminLogin(ForceLoginRequest request, HttpSession session) {
        StaffPo staff = forceLogin(request);
        session.setAttribute(SessionKeys.STAFF_ADMIN_INFO, currentStaffService.toSession(staff));
        return staff;
    }

    public StaffPo forceStaffLogin(ForceLoginRequest request, HttpSession session) {
        StaffPo staff = forceLogin(request);
        session.setAttribute(SessionKeys.STAFF_INFO, currentStaffService.toSession(staff));
        return staff;
    }

    private StaffPo loginByWeWorkCode(String extCorpId, String code) {
        if (!StringUtils.hasText(extCorpId) || !StringUtils.hasText(code)) {
            throw new BizException(ErrorCode.BAD_REQUEST);
        }
        UserIdentityResponse userInfo = weWorkClient.getUserInfo(
                properties.getWeWork().getExtCorpId(),
                properties.getWeWork().getMainAgentSecret(),
                code);
        if (!StringUtils.hasText(userInfo.getUserId())) {
            throw new BizException(ErrorCode.FORBIDDEN);
        }
        StaffPo staff = staffMapper.selectOne(new LambdaQueryWrapper<StaffPo>()
                .eq(StaffPo::getExtId, userInfo.getUserId())
                .last("limit 1"));
        if (staff == null) {
            throw new BizException(ErrorCode.ITEM_NOT_FOUND);
        }
        if (!extCorpId.equals(staff.getExtCorpId())) {
            throw new BizException(ErrorCode.FORBIDDEN);
        }
        return staff;
    }

    private LoginSuccessResponse loginSuccess(StaffPo staff) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("extCorpId", staff.getExtCorpId());
        claims.put("extStaffId", staff.getExtId());
        claims.put("roleId", staff.getRoleId());
        claims.put("roleType", staff.getRoleType());
        String token = jwtTokenService.issue(String.valueOf(staff.getId()), claims);
        return new LoginSuccessResponse(staff, token);
    }

    private CustomerLoginSuccessResponse customerLoginSuccess(CustomerPo customer) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("extCorpId", customer.getExtCorpId());
        claims.put("extCustomerId", customer.getExtId());
        String token = jwtTokenService.issue(String.valueOf(customer.getId()), claims);
        return new CustomerLoginSuccessResponse(customer, token);
    }

    private StaffPo forceLogin(ForceLoginRequest request) {
        if (AppEnv.PROD.name().equalsIgnoreCase(properties.getEnv())) {
            throw new BizException(ErrorCode.FORBIDDEN);
        }
        String extCorpId = request.getExtCorpId();
        if (!StringUtils.hasText(extCorpId)) {
            extCorpId = properties.getWeWork().getExtCorpId();
        }
        StaffPo staff = staffMapper.selectOne(new LambdaQueryWrapper<StaffPo>()
                .eq(StringUtils.hasText(extCorpId), StaffPo::getExtCorpId, extCorpId)
                .eq(StringUtils.hasText(request.getExtStaffId()), StaffPo::getExtId, request.getExtStaffId())
                .last("limit 1"));
        if (staff == null) {
            throw new BizException(ErrorCode.ITEM_NOT_FOUND);
        }
        return staff;
    }

    private String resolveExtCorpId(String extCorpId) {
        if (StringUtils.hasText(extCorpId)) {
            return extCorpId;
        }
        return properties.getWeWork().getExtCorpId();
    }

    private String decodeRequiredUrl(String sourceUrl) {
        if (!StringUtils.hasText(sourceUrl)) {
            throw new BizException(ErrorCode.BAD_REQUEST);
        }
        String decoded;
        try {
            decoded = URLDecoder.decode(sourceUrl, StandardCharsets.UTF_8.name());
            parseRequiredAbsoluteUri(decoded);
        } catch (IllegalArgumentException | UnsupportedEncodingException e) {
            throw new BizException(ErrorCode.ILLEGAL_URL);
        }
        return decoded;
    }

    private URI parseRequiredAbsoluteUri(String url) {
        URI uri = URI.create(url);
        if (!StringUtils.hasText(uri.getScheme()) || !StringUtils.hasText(uri.getHost())) {
            throw new BizException(ErrorCode.ILLEGAL_URL);
        }
        return uri;
    }

    private String randomState() {
        String alphabet = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        StringBuilder builder = new StringBuilder(10);
        for (int i = 0; i < 10; i++) {
            builder.append(alphabet.charAt(secureRandom.nextInt(alphabet.length())));
        }
        return builder.toString();
    }

    private String encode(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            throw new BizException(ErrorCode.INTERNAL_ERROR);
        }
    }
}
