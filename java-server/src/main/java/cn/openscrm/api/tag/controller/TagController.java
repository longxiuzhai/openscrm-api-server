package cn.openscrm.api.tag.controller;

import cn.openscrm.api.auth.annotation.RequirePermission;
import cn.openscrm.api.auth.service.CurrentStaffService;
import cn.openscrm.api.common.api.ApiResponse;
import cn.openscrm.api.common.constant.BizIdentity;
import cn.openscrm.api.common.constant.OperationType;
import cn.openscrm.api.persistence.entity.StaffPo;
import cn.openscrm.api.tag.service.TagSyncService;
import javax.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping({"/api/v1/staff-admin/customer", "/api/v1/staff_admin/customer"})
public class TagController {

    private final CurrentStaffService currentStaffService;
    private final TagSyncService tagSyncService;

    @PostMapping("/tag/action/sync")
    @RequirePermission(biz = BizIdentity.BIZ_CUSTOMER_TAG, operation = OperationType.FULL)
    public ApiResponse<Void> sync(HttpSession session) {
        StaffPo current = currentStaffService.requireStaffAdmin(session);
        tagSyncService.syncAll(current.getExtCorpId());
        return ApiResponse.ok(null);
    }
}
