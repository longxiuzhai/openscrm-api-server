package cn.openscrm.api.materialtag.controller;

import cn.openscrm.api.auth.annotation.RequirePermission;
import cn.openscrm.api.auth.service.CurrentStaffService;
import cn.openscrm.api.common.api.ApiResponse;
import cn.openscrm.api.common.api.PageResponse;
import cn.openscrm.api.common.constant.BizIdentity;
import cn.openscrm.api.common.constant.OperationType;
import cn.openscrm.api.materialtag.dto.MaterialTagCreateRequest;
import cn.openscrm.api.materialtag.dto.MaterialTagDeleteRequest;
import cn.openscrm.api.materialtag.service.MaterialTagService;
import cn.openscrm.api.persistence.entity.MaterialLibTagPo;
import cn.openscrm.api.persistence.entity.StaffPo;
import java.util.List;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
public class MaterialTagController {

    private final CurrentStaffService currentStaffService;
    private final MaterialTagService tagService;

    @PostMapping({"/api/v1/staff-admin/material/lib/tag", "/api/v1/staff_admin/material/lib/tag"})
    @RequirePermission(biz = BizIdentity.BIZ_MEDIA_MGR, operation = OperationType.FULL)
    public ApiResponse<List<MaterialLibTagPo>> create(@Valid @RequestBody MaterialTagCreateRequest request,
                                                      HttpSession session) {
        StaffPo current = currentStaffService.requireStaffAdmin(session);
        return ApiResponse.ok(tagService.create(request, current.getExtCorpId(), current.getExtId()));
    }

    @GetMapping({"/api/v1/staff-admin/material/lib/tags", "/api/v1/staff_admin/material/lib/tags"})
    @RequirePermission(biz = BizIdentity.BIZ_MEDIA_MGR, operation = OperationType.READ)
    public ApiResponse<PageResponse<MaterialLibTagPo>> queryAdmin(
            @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "1") @Min(1) long page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(200) long pageSize,
            HttpSession session) {
        StaffPo current = currentStaffService.requireStaffAdmin(session);
        return ApiResponse.ok(tagService.query(current.getExtCorpId(), name, page, pageSize));
    }

    @GetMapping({"/api/v1/staff-frontend/material/lib/tags", "/api/v1/staff_frontend/material/lib/tags"})
    public ApiResponse<PageResponse<MaterialLibTagPo>> queryFrontend(
            @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "1") @Min(1) long page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(200) long pageSize,
            HttpSession session) {
        StaffPo current = currentStaffService.requireStaff(session);
        return ApiResponse.ok(tagService.query(current.getExtCorpId(), name, page, pageSize));
    }

    @PostMapping({"/api/v1/staff-admin/material/lib/tag/action/delete", "/api/v1/staff_admin/material/lib/tag/action/delete"})
    @RequirePermission(biz = BizIdentity.BIZ_MEDIA_MGR, operation = OperationType.FULL)
    public ApiResponse<Long> delete(@Valid @RequestBody MaterialTagDeleteRequest request) {
        return ApiResponse.ok(tagService.delete(request));
    }
}
