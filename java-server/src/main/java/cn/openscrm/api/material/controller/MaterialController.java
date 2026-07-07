package cn.openscrm.api.material.controller;

import cn.openscrm.api.auth.annotation.RequirePermission;
import cn.openscrm.api.auth.service.CurrentStaffService;
import cn.openscrm.api.common.api.ApiResponse;
import cn.openscrm.api.common.api.PageResponse;
import cn.openscrm.api.common.constant.BizIdentity;
import cn.openscrm.api.common.constant.OperationType;
import cn.openscrm.api.material.dto.MaterialDeleteRequest;
import cn.openscrm.api.material.dto.MaterialRequest;
import cn.openscrm.api.material.dto.MaterialResponse;
import cn.openscrm.api.material.dto.SidebarStatusRequest;
import cn.openscrm.api.material.dto.SidebarStatusResponse;
import cn.openscrm.api.material.service.MaterialService;
import cn.openscrm.api.persistence.entity.MaterialPo;
import cn.openscrm.api.persistence.entity.StaffPo;
import java.util.List;
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
@RequestMapping({"/api/v1/staff-admin/material", "/api/v1/staff_admin/material"})
public class MaterialController {

    private final CurrentStaffService currentStaffService;
    private final MaterialService materialService;

    @PostMapping("/lib")
    @RequirePermission(biz = BizIdentity.BIZ_MEDIA_MGR, operation = OperationType.FULL)
    public ApiResponse<MaterialPo> create(@RequestBody MaterialRequest request, HttpSession session) {
        StaffPo current = currentStaffService.requireStaffAdmin(session);
        return ApiResponse.ok(materialService.create(request, current.getExtCorpId(), current.getExtId()));
    }

    @GetMapping({"/lib", "/libs"})
    @RequirePermission(biz = BizIdentity.BIZ_MEDIA_MGR, operation = OperationType.READ)
    public ApiResponse<PageResponse<MaterialResponse>> query(
            @RequestParam(required = false) String title,
            @RequestParam(value = "material_type", required = false) String materialType,
            @RequestParam(value = "material_tag_list", required = false) List<String> materialTagList,
            @RequestParam(defaultValue = "1") @Min(1) long page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(200) long pageSize,
            HttpSession session) {
        StaffPo current = currentStaffService.requireStaffAdmin(session);
        return ApiResponse.ok(materialService.query(
                current.getExtCorpId(),
                title,
                materialType,
                materialService.split(materialTagList),
                page,
                pageSize));
    }

    @PutMapping("/lib/{id}")
    @RequirePermission(biz = BizIdentity.BIZ_MEDIA_MGR, operation = OperationType.FULL)
    public ApiResponse<MaterialPo> update(@PathVariable Long id,
                                          @RequestBody MaterialRequest request,
                                          HttpSession session) {
        StaffPo current = currentStaffService.requireStaffAdmin(session);
        return ApiResponse.ok(materialService.update(id, request, current.getExtCorpId()));
    }

    @PostMapping("/lib/action/delete")
    @RequirePermission(biz = BizIdentity.BIZ_MEDIA_MGR, operation = OperationType.FULL)
    public ApiResponse<Integer> delete(@RequestBody MaterialDeleteRequest request, HttpSession session) {
        StaffPo current = currentStaffService.requireStaffAdmin(session);
        return ApiResponse.ok(materialService.delete(request, current.getExtCorpId()));
    }

    @GetMapping("/lib/sidebar-status")
    @RequirePermission(biz = BizIdentity.BIZ_MEDIA_MGR, operation = OperationType.READ)
    public ApiResponse<SidebarStatusResponse> getSidebarStatus(HttpSession session) {
        StaffPo current = currentStaffService.requireStaffAdmin(session);
        return ApiResponse.ok(materialService.getSidebarStatus(current.getExtCorpId()));
    }

    @PutMapping("/lib/sidebar-status")
    @RequirePermission(biz = BizIdentity.BIZ_MEDIA_MGR, operation = OperationType.FULL)
    public ApiResponse<SidebarStatusResponse> updateSidebarStatus(@RequestBody SidebarStatusRequest request,
                                                                  HttpSession session) {
        StaffPo current = currentStaffService.requireStaffAdmin(session);
        return ApiResponse.ok(materialService.updateSidebarStatus(
                current.getExtCorpId(),
                current.getExtId(),
                request == null ? null : request.getStatus()));
    }
}
