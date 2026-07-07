package cn.openscrm.api.internaltag.controller;

import cn.openscrm.api.auth.annotation.RequirePermission;
import cn.openscrm.api.auth.service.CurrentStaffService;
import cn.openscrm.api.common.api.ApiResponse;
import cn.openscrm.api.common.api.PageResponse;
import cn.openscrm.api.common.constant.BizIdentity;
import cn.openscrm.api.common.constant.OperationType;
import cn.openscrm.api.internaltag.dto.InternalTagCreateRequest;
import cn.openscrm.api.internaltag.dto.InternalTagDeleteRequest;
import cn.openscrm.api.internaltag.service.InternalTagService;
import cn.openscrm.api.persistence.entity.InternalTagPo;
import cn.openscrm.api.persistence.entity.StaffPo;
import java.util.List;
import javax.servlet.http.HttpSession;
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
@RequestMapping({"/api/v1/staff-admin/customer", "/api/v1/staff_admin/customer"})
public class InternalTagController {

    private final CurrentStaffService currentStaffService;
    private final InternalTagService internalTagService;

    @PostMapping("/internal-tag")
    @RequirePermission(biz = BizIdentity.BIZ_CUSTOMER_TAG, operation = OperationType.FULL)
    public ApiResponse<List<InternalTagPo>> create(@RequestBody InternalTagCreateRequest request,
                                                   HttpSession session) {
        StaffPo current = currentStaffService.requireStaffAdmin(session);
        return ApiResponse.ok(internalTagService.create(request, current.getExtCorpId(), current.getExtId()));
    }

    @GetMapping("/internal-tags")
    @RequirePermission(biz = BizIdentity.BIZ_CUSTOMER_TAG, operation = OperationType.READ)
    public ApiResponse<PageResponse<InternalTagPo>> query(
            @RequestParam(defaultValue = "1") @Min(1) long page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(200) long pageSize,
            HttpSession session) {
        StaffPo current = currentStaffService.requireStaffAdmin(session);
        return ApiResponse.ok(internalTagService.query(current.getExtCorpId(), page, pageSize));
    }

    @PostMapping("/internal-tag/action/delete")
    @RequirePermission(biz = BizIdentity.BIZ_CUSTOMER_TAG, operation = OperationType.FULL)
    public ApiResponse<Long> delete(@RequestBody InternalTagDeleteRequest request, HttpSession session) {
        StaffPo current = currentStaffService.requireStaffAdmin(session);
        return ApiResponse.ok(internalTagService.delete(request, current.getExtCorpId()));
    }
}
