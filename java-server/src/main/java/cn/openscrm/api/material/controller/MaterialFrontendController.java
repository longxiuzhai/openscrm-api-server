package cn.openscrm.api.material.controller;

import cn.openscrm.api.auth.service.CurrentStaffService;
import cn.openscrm.api.common.api.ApiResponse;
import cn.openscrm.api.common.api.PageResponse;
import cn.openscrm.api.material.dto.MaterialResponse;
import cn.openscrm.api.material.service.MaterialService;
import cn.openscrm.api.persistence.entity.StaffPo;
import java.util.List;
import javax.servlet.http.HttpSession;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping({"/api/v1/staff-frontend/material", "/api/v1/staff_frontend/material"})
public class MaterialFrontendController {

    private final CurrentStaffService currentStaffService;
    private final MaterialService materialService;

    @GetMapping("/lib")
    public ApiResponse<PageResponse<MaterialResponse>> query(
            @RequestParam(required = false) String title,
            @RequestParam(value = "material_type", required = false) String materialType,
            @RequestParam(value = "material_tag_list", required = false) List<String> materialTagList,
            @RequestParam(defaultValue = "1") @Min(1) long page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(200) long pageSize,
            HttpSession session) {
        StaffPo current = currentStaffService.requireStaff(session);
        return ApiResponse.ok(materialService.query(
                current.getExtCorpId(),
                title,
                materialType,
                materialService.split(materialTagList),
                page,
                pageSize));
    }
}
