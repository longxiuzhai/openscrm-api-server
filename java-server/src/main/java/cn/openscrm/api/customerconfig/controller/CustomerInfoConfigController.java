package cn.openscrm.api.customerconfig.controller;

import cn.openscrm.api.auth.annotation.RequirePermission;
import cn.openscrm.api.auth.service.CurrentStaffService;
import cn.openscrm.api.common.api.ApiResponse;
import cn.openscrm.api.common.api.PageResponse;
import cn.openscrm.api.common.constant.BizIdentity;
import cn.openscrm.api.common.constant.OperationType;
import cn.openscrm.api.customerconfig.dto.CustomerInfoDisplayRuleRequest;
import cn.openscrm.api.customerconfig.dto.CustomerInfoRequest;
import cn.openscrm.api.customerconfig.dto.CustomerRemarkRequest;
import cn.openscrm.api.customerconfig.dto.CustomerRemarkResponse;
import cn.openscrm.api.customerconfig.dto.InfoRemarkResponse;
import cn.openscrm.api.customerconfig.dto.RemarkOptionRequest;
import cn.openscrm.api.customerconfig.service.CustomerEventQueryService;
import cn.openscrm.api.customerconfig.service.CustomerInfoConfigService;
import cn.openscrm.api.customerconfig.service.CustomerRemarkConfigService;
import cn.openscrm.api.persistence.entity.CustomerEventPo;
import cn.openscrm.api.persistence.entity.CustomerInfoDisplayRulePo;
import cn.openscrm.api.persistence.entity.CustomerInfoPo;
import cn.openscrm.api.persistence.entity.CustomerRemarkPo;
import cn.openscrm.api.persistence.entity.StaffPo;
import javax.servlet.http.HttpSession;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping({"/api/v1/staff-admin", "/api/v1/staff_admin"})
public class CustomerInfoConfigController {

    private final CustomerInfoConfigService customerInfoConfigService;
    private final CustomerRemarkConfigService customerRemarkConfigService;
    private final CustomerEventQueryService customerEventQueryService;
    private final CurrentStaffService currentStaffService;

    @GetMapping("/customer/info")
    @RequirePermission(biz = BizIdentity.BIZ_CUSTOMER_INFO, operation = OperationType.READ)
    public ApiResponse<CustomerInfoPo> getCustomerInfo(@RequestParam("ext_customer_id") String extCustomerId,
                                                       @RequestParam("ext_staff_id") String extStaffId,
                                                       HttpSession session) {
        StaffPo current = currentStaffService.requireStaffAdmin(session);
        return ApiResponse.ok(customerInfoConfigService.getCustomerInfo(
                current.getExtCorpId(), extCustomerId, extStaffId));
    }

    @PutMapping("/customer/info")
    @RequirePermission(biz = BizIdentity.BIZ_CUSTOMER_INFO, operation = OperationType.FULL)
    public ApiResponse<Void> updateCustomerInfo(@RequestBody CustomerInfoRequest request, HttpSession session) {
        StaffPo current = currentStaffService.requireStaffAdmin(session);
        customerInfoConfigService.updateCustomerInfo(current.getExtCorpId(), request);
        return ApiResponse.ok(null);
    }

    @GetMapping("/customer/info/displays")
    @RequirePermission(biz = BizIdentity.BIZ_CUSTOMER_INFO, operation = OperationType.READ)
    public ApiResponse<CustomerInfoDisplayRulePo> getDisplayRule(HttpSession session) {
        StaffPo current = currentStaffService.requireStaffAdmin(session);
        return ApiResponse.ok(customerInfoConfigService.getDisplayRule(current.getExtCorpId()));
    }

    @PutMapping("/customer/info/displays")
    @RequirePermission(biz = BizIdentity.BIZ_CUSTOMER_INFO, operation = OperationType.FULL)
    public ApiResponse<Void> updateDisplayRule(@RequestBody CustomerInfoDisplayRuleRequest request,
                                               HttpSession session) {
        StaffPo current = currentStaffService.requireStaffAdmin(session);
        customerInfoConfigService.updateDisplayRule(current.getExtCorpId(), request);
        return ApiResponse.ok(null);
    }

    @GetMapping("/customer/events")
    @RequirePermission(biz = BizIdentity.BIZ_CUSTOMER_INFO, operation = OperationType.READ)
    public ApiResponse<PageResponse<CustomerEventPo>> queryEvents(
            @RequestParam(value = "event_type", required = false) String eventType,
            @RequestParam("ext_staff_id") String extStaffId,
            @RequestParam("ext_customer_id") String extCustomerId,
            @RequestParam(value = "sort_field", required = false) String sortField,
            @RequestParam(value = "sort_type", required = false) String sortType,
            @RequestParam(defaultValue = "1") @Min(1) long page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(200) long pageSize,
            HttpSession session) {
        StaffPo current = currentStaffService.requireStaffAdmin(session);
        return ApiResponse.ok(customerEventQueryService.query(
                current.getExtCorpId(), extStaffId, extCustomerId, eventType, sortField, sortType, page, pageSize));
    }

    @PostMapping("/customer/remark")
    @RequirePermission(biz = BizIdentity.BIZ_CUSTOMER_REMARK, operation = OperationType.FULL)
    public ApiResponse<CustomerRemarkResponse> createRemark(@RequestBody CustomerRemarkRequest request,
                                                            HttpSession session) {
        StaffPo current = currentStaffService.requireStaffAdmin(session);
        return ApiResponse.ok(customerRemarkConfigService.create(current.getExtCorpId(), request));
    }

    @GetMapping("/customer/remark")
    @RequirePermission(biz = BizIdentity.BIZ_CUSTOMER_REMARK, operation = OperationType.READ)
    public ApiResponse<InfoRemarkResponse> getRemark(HttpSession session) {
        StaffPo current = currentStaffService.requireStaffAdmin(session);
        return ApiResponse.ok(customerRemarkConfigService.get(current.getExtCorpId()));
    }

    @PutMapping("/customer/remark")
    @RequirePermission(biz = BizIdentity.BIZ_CUSTOMER_REMARK, operation = OperationType.FULL)
    public ApiResponse<CustomerRemarkPo> updateRemark(@RequestBody CustomerRemarkRequest request) {
        return ApiResponse.ok(customerRemarkConfigService.update(request));
    }

    @PutMapping("/customer/remark/action/exchange-order")
    @RequirePermission(biz = BizIdentity.BIZ_CUSTOMER_REMARK, operation = OperationType.FULL)
    public ApiResponse<Void> exchangeRemarkOrder(@RequestBody CustomerRemarkRequest request) {
        customerRemarkConfigService.exchangeOrder(request);
        return ApiResponse.ok(null);
    }

    @PostMapping("/customer/remark/action/delete")
    @RequirePermission(biz = BizIdentity.BIZ_CUSTOMER_REMARK, operation = OperationType.FULL)
    public ApiResponse<Void> deleteRemark(@RequestBody CustomerRemarkRequest request, HttpSession session) {
        StaffPo current = currentStaffService.requireStaffAdmin(session);
        customerRemarkConfigService.delete(current.getExtCorpId(), request.getIds());
        return ApiResponse.ok(null);
    }

    @PostMapping("/customer/remark/option")
    @RequirePermission(biz = BizIdentity.BIZ_CUSTOMER_REMARK, operation = OperationType.FULL)
    public ApiResponse<Void> addRemarkOption(@RequestBody RemarkOptionRequest request) {
        customerRemarkConfigService.addOption(request);
        return ApiResponse.ok(null);
    }

    @PutMapping("/customer/remark/option")
    @RequirePermission(biz = BizIdentity.BIZ_CUSTOMER_REMARK, operation = OperationType.FULL)
    public ApiResponse<Void> updateRemarkOption(@RequestBody RemarkOptionRequest request) {
        customerRemarkConfigService.updateOption(request);
        return ApiResponse.ok(null);
    }

    @PostMapping("/customer/remark/option/action/delete")
    @RequirePermission(biz = BizIdentity.BIZ_CUSTOMER_REMARK, operation = OperationType.FULL)
    public ApiResponse<Void> deleteRemarkOption(@RequestBody RemarkOptionRequest request) {
        customerRemarkConfigService.deleteOptions(request.getIds());
        return ApiResponse.ok(null);
    }
}
