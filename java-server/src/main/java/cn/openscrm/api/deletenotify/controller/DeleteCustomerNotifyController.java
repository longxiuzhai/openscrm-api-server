package cn.openscrm.api.deletenotify.controller;

import cn.openscrm.api.auth.annotation.RequirePermission;
import cn.openscrm.api.auth.service.CurrentStaffService;
import cn.openscrm.api.common.api.ApiResponse;
import cn.openscrm.api.common.api.PageResponse;
import cn.openscrm.api.common.constant.BizIdentity;
import cn.openscrm.api.common.constant.OperationType;
import cn.openscrm.api.deletenotify.dto.DeleteCustomerNotifyRuleRequest;
import cn.openscrm.api.deletenotify.dto.StaffDeleteCustomerResponse;
import cn.openscrm.api.deletenotify.service.DeleteCustomerNotifyService;
import cn.openscrm.api.persistence.entity.EventNotifyPo;
import cn.openscrm.api.persistence.entity.StaffPo;
import java.time.LocalDate;
import java.util.List;
import javax.servlet.http.HttpSession;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
public class DeleteCustomerNotifyController {

    private final DeleteCustomerNotifyService notifyService;
    private final CurrentStaffService currentStaffService;

    @GetMapping({"/notify/delete-customer/status", "/notify/delete_customer/status",
            "/customer/action/get-loss-notify-rule"})
    @RequirePermission(biz = BizIdentity.BIZ_DELETE_CUSTOMER, operation = OperationType.READ)
    public ApiResponse<EventNotifyPo> getRule(HttpSession session) {
        StaffPo current = currentStaffService.requireStaffAdmin(session);
        return ApiResponse.ok(notifyService.getRule(current.getExtCorpId()));
    }

    @PutMapping({"/notify/delete-customer/status", "/notify/delete_customer/status"})
    @RequirePermission(biz = BizIdentity.BIZ_DELETE_CUSTOMER, operation = OperationType.FULL)
    public ApiResponse<Void> updateRule(@RequestBody DeleteCustomerNotifyRuleRequest request, HttpSession session) {
        StaffPo current = currentStaffService.requireStaffAdmin(session);
        notifyService.upsertRule(current.getExtCorpId(), request);
        return ApiResponse.ok(null);
    }

    @PostMapping("/customer/action/update-loss-notify-rule")
    @RequirePermission(biz = BizIdentity.BIZ_DELETE_CUSTOMER, operation = OperationType.FULL)
    public ApiResponse<Void> updateRulePostAlias(@RequestBody DeleteCustomerNotifyRuleRequest request,
                                                 HttpSession session) {
        return updateRule(request, session);
    }

    @GetMapping({"/notify/delete-customers", "/action/notify/delete-customer"})
    @RequirePermission(biz = BizIdentity.BIZ_DELETE_CUSTOMER, operation = OperationType.READ)
    public ApiResponse<PageResponse<StaffDeleteCustomerResponse>> queryRecords(
            @RequestParam(value = "ext_department_id", required = false) Long extDepartmentId,
            @RequestParam(value = "department_id", required = false) Long departmentId,
            @RequestParam(value = "ext_staff_id", required = false) List<String> extStaffIds,
            @RequestParam(value = "connection_create_start", required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate connectionCreateStart,
            @RequestParam(value = "connection_create_end", required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate connectionCreateEnd,
            @RequestParam(value = "delete_customer_start", required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate deleteCustomerStart,
            @RequestParam(value = "delete_customer_end", required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate deleteCustomerEnd,
            @RequestParam(value = "sort_field", required = false) String sortField,
            @RequestParam(value = "sort_type", required = false) String sortType,
            @RequestParam(defaultValue = "1") @Min(1) long page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(200) long pageSize,
            HttpSession session) {
        StaffPo current = currentStaffService.requireStaffAdmin(session);
        return ApiResponse.ok(notifyService.queryRecords(
                current.getExtCorpId(),
                extDepartmentId != null ? extDepartmentId : departmentId,
                extStaffIds,
                connectionCreateStart,
                connectionCreateEnd,
                deleteCustomerStart,
                deleteCustomerEnd,
                sortField,
                sortType,
                page,
                pageSize));
    }

    @GetMapping("/staff/action/delete-customers-data-export")
    @RequirePermission(biz = BizIdentity.BIZ_DELETE_CUSTOMER, operation = OperationType.READ)
    public ResponseEntity<byte[]> exportRecords(
            @RequestParam(value = "ext_department_id", required = false) Long extDepartmentId,
            @RequestParam(value = "department_id", required = false) Long departmentId,
            @RequestParam(value = "ext_staff_id", required = false) List<String> extStaffIds,
            @RequestParam(value = "connection_create_start", required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate connectionCreateStart,
            @RequestParam(value = "connection_create_end", required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate connectionCreateEnd,
            @RequestParam(value = "delete_customer_start", required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate deleteCustomerStart,
            @RequestParam(value = "delete_customer_end", required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate deleteCustomerEnd,
            @RequestParam(value = "sort_field", required = false) String sortField,
            @RequestParam(value = "sort_type", required = false) String sortType,
            HttpSession session) {
        StaffPo current = currentStaffService.requireStaffAdmin(session);
        byte[] content = notifyService.exportXlsx(
                current.getExtCorpId(),
                extDepartmentId != null ? extDepartmentId : departmentId,
                extStaffIds,
                connectionCreateStart,
                connectionCreateEnd,
                deleteCustomerStart,
                deleteCustomerEnd,
                sortField,
                sortType);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename("delete_customers.xlsx").build().toString())
                .body(content);
    }
}
