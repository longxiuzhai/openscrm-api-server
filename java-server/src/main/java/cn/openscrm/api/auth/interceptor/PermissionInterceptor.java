package cn.openscrm.api.auth.interceptor;

import cn.openscrm.api.auth.annotation.RequirePermission;
import cn.openscrm.api.auth.service.CurrentStaffService;
import cn.openscrm.api.auth.service.PermissionService;
import cn.openscrm.api.persistence.entity.StaffPo;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class PermissionInterceptor implements HandlerInterceptor {

    private final CurrentStaffService currentStaffService;
    private final PermissionService permissionService;

    public PermissionInterceptor(CurrentStaffService currentStaffService, PermissionService permissionService) {
        this.currentStaffService = currentStaffService;
        this.permissionService = permissionService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        HandlerMethod method = (HandlerMethod) handler;
        RequirePermission permission = AnnotatedElementUtils.findMergedAnnotation(method.getMethod(), RequirePermission.class);
        if (permission == null) {
            permission = AnnotatedElementUtils.findMergedAnnotation(method.getBeanType(), RequirePermission.class);
        }
        if (permission == null) {
            return true;
        }
        HttpSession session = request.getSession(false);
        StaffPo staff = currentStaffService.requireStaffAdmin(session);
        permissionService.require(staff, permission.biz(), permission.operation());
        return true;
    }
}
