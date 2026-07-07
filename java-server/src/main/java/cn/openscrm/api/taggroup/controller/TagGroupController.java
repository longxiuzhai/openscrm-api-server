package cn.openscrm.api.taggroup.controller;

import cn.openscrm.api.auth.annotation.RequirePermission;
import cn.openscrm.api.auth.service.CurrentStaffService;
import cn.openscrm.api.common.api.ApiResponse;
import cn.openscrm.api.common.api.PageResponse;
import cn.openscrm.api.common.constant.BizIdentity;
import cn.openscrm.api.common.constant.OperationType;
import cn.openscrm.api.persistence.entity.StaffPo;
import cn.openscrm.api.taggroup.dto.TagGroupCreateRequest;
import cn.openscrm.api.taggroup.dto.TagGroupDeleteRequest;
import cn.openscrm.api.taggroup.dto.TagGroupExchangeOrderRequest;
import cn.openscrm.api.taggroup.dto.TagGroupResponse;
import cn.openscrm.api.taggroup.dto.TagGroupUpdateRequest;
import cn.openscrm.api.taggroup.service.TagGroupService;
import java.util.List;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
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
@RequestMapping({"/api/v1/staff-admin/customer", "/api/v1/staff_admin/customer"})
public class TagGroupController {

    private final CurrentStaffService currentStaffService;
    private final TagGroupService tagGroupService;

    @GetMapping({"/tag-group", "/tag-groups"})
    @RequirePermission(biz = BizIdentity.BIZ_CUSTOMER_TAG, operation = OperationType.READ)
    public ApiResponse<PageResponse<TagGroupResponse>> query(
            @RequestParam(name = "ext_department_ids", required = false) List<Long> extDepartmentIds,
            @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "1") @Min(1) long page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(200) long pageSize,
            HttpSession session) {
        StaffPo current = currentStaffService.requireStaffAdmin(session);
        return ApiResponse.ok(tagGroupService.query(current.getExtCorpId(), extDepartmentIds, name, page, pageSize));
    }

    @PostMapping("/tag-group")
    @RequirePermission(biz = BizIdentity.BIZ_CUSTOMER_TAG, operation = OperationType.FULL)
    public ApiResponse<TagGroupResponse> create(@Valid @RequestBody TagGroupCreateRequest request,
                                                HttpSession session) {
        StaffPo current = currentStaffService.requireStaffAdmin(session);
        return ApiResponse.ok(tagGroupService.create(request, current.getExtCorpId()));
    }

    @PutMapping("/tag-group/{extId}")
    @RequirePermission(biz = BizIdentity.BIZ_CUSTOMER_TAG, operation = OperationType.FULL)
    public ApiResponse<TagGroupResponse> update(@PathVariable String extId,
                                                @Valid @RequestBody TagGroupUpdateRequest request,
                                                HttpSession session) {
        StaffPo current = currentStaffService.requireStaffAdmin(session);
        return ApiResponse.ok(tagGroupService.update(extId, request, current.getExtCorpId()));
    }

    @PostMapping("/tag-group/{extId}")
    @RequirePermission(biz = BizIdentity.BIZ_CUSTOMER_TAG, operation = OperationType.FULL)
    public ApiResponse<TagGroupResponse> updatePostAlias(@PathVariable String extId,
                                                         @Valid @RequestBody TagGroupUpdateRequest request,
                                                         HttpSession session) {
        return update(extId, request, session);
    }

    @PostMapping("/tag-group/action/delete")
    @RequirePermission(biz = BizIdentity.BIZ_CUSTOMER_TAG, operation = OperationType.FULL)
    public ApiResponse<Long> delete(@Valid @RequestBody TagGroupDeleteRequest request, HttpSession session) {
        StaffPo current = currentStaffService.requireStaffAdmin(session);
        return ApiResponse.ok(tagGroupService.delete(request, current.getExtCorpId()));
    }

    @PostMapping("/tag-group/action/exchange-order")
    @RequirePermission(biz = BizIdentity.BIZ_CUSTOMER_TAG, operation = OperationType.FULL)
    public ApiResponse<Object> exchangeOrder(@Valid @RequestBody TagGroupExchangeOrderRequest request) {
        tagGroupService.exchangeOrder(request);
        return ApiResponse.ok(null);
    }
}
