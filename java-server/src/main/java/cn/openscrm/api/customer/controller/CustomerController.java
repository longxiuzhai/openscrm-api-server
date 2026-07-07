package cn.openscrm.api.customer.controller;

import cn.openscrm.api.auth.annotation.RequirePermission;
import cn.openscrm.api.auth.service.CurrentStaffService;
import cn.openscrm.api.common.api.ApiResponse;
import cn.openscrm.api.common.api.PageResponse;
import cn.openscrm.api.common.constant.BizIdentity;
import cn.openscrm.api.common.constant.OperationType;
import cn.openscrm.api.customer.dto.CustomerTrendResponse;
import cn.openscrm.api.customer.entity.CustomerEntity;
import cn.openscrm.api.customer.service.CustomerService;
import cn.openscrm.api.customer.service.CustomerStatisticService;
import cn.openscrm.api.customerexport.service.CustomerExportService;
import cn.openscrm.api.customerloss.dto.CustomerLossResponse;
import cn.openscrm.api.customerloss.service.CustomerLossService;
import cn.openscrm.api.persistence.entity.StaffPo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import java.time.LocalDate;
import java.util.List;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping({"/api/v1/staff-admin", "/api/v1/staff_admin"})
public class CustomerController {

    private final CustomerService customerService;
    private final CustomerStatisticService customerStatisticService;
    private final CustomerExportService customerExportService;
    private final CustomerLossService customerLossService;
    private final CurrentStaffService currentStaffService;

    @GetMapping("/customers")
    @RequirePermission(biz = BizIdentity.BIZ_CUSTOMER_INFO, operation = OperationType.READ)
    public ApiResponse<PageResponse<CustomerEntity>> list(
            @RequestParam(defaultValue = "1") @Min(1) long page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(200) long pageSize) {
        IPage<CustomerEntity> result = customerService.page(page, pageSize);
        return ApiResponse.ok(new PageResponse<>(result.getRecords(), result.getTotal(), page, pageSize));
    }

    @GetMapping("/customer/{id}")
    @RequirePermission(biz = BizIdentity.BIZ_CUSTOMER_INFO, operation = OperationType.READ)
    public ApiResponse<CustomerEntity> get(@PathVariable Long id) {
        return ApiResponse.ok(customerService.getById(id));
    }

    @GetMapping("/customers/statistic")
    @RequirePermission(biz = BizIdentity.BIZ_CUSTOMER_INFO, operation = OperationType.READ)
    public ApiResponse<List<CustomerTrendResponse>> statistic(
            @RequestParam("statistic_type") String statisticType,
            @RequestParam(value = "ext_staff_ids", required = false) List<String> extStaffIds,
            @RequestParam("start_time") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startTime,
            @RequestParam("end_time") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endTime,
            javax.servlet.http.HttpSession session) {
        StaffPo current = currentStaffService.requireStaffAdmin(session);
        return ApiResponse.ok(customerStatisticService.query(
                current.getExtCorpId(), statisticType, extStaffIds, startTime, endTime));
    }

    @GetMapping("/customer/losses")
    @RequirePermission(biz = BizIdentity.BIZ_CUSTOMER_LOSS, operation = OperationType.READ)
    public ApiResponse<PageResponse<CustomerLossResponse>> losses(
            @RequestParam(value = "ext_staff_ids", required = false) List<String> extStaffIds,
            @RequestParam(value = "loss_start", required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate lossStart,
            @RequestParam(value = "loss_end", required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate lossEnd,
            @RequestParam(value = "connection_create_start", required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate connectionCreateStart,
            @RequestParam(value = "connection_create_end", required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate connectionCreateEnd,
            @RequestParam(value = "time_span_lower_limit", required = false) Long timeSpanLowerLimit,
            @RequestParam(value = "time_span_upper_limit", required = false) Long timeSpanUpperLimit,
            @RequestParam(value = "sort_field", required = false) String sortField,
            @RequestParam(value = "sort_type", required = false) String sortType,
            @RequestParam(defaultValue = "1") @Min(1) long page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(200) long pageSize,
            javax.servlet.http.HttpSession session) {
        StaffPo current = currentStaffService.requireStaffAdmin(session);
        return ApiResponse.ok(customerLossService.query(
                current.getExtCorpId(),
                extStaffIds,
                lossStart,
                lossEnd,
                connectionCreateStart,
                connectionCreateEnd,
                timeSpanLowerLimit,
                timeSpanUpperLimit,
                sortField,
                sortType,
                page,
                pageSize));
    }

    @GetMapping("/customer/action/customers-losses-data-export")
    @RequirePermission(biz = BizIdentity.BIZ_CUSTOMER_LOSS, operation = OperationType.READ)
    public ResponseEntity<byte[]> exportLosses(
            @RequestParam(value = "ext_staff_ids", required = false) List<String> extStaffIds,
            @RequestParam(value = "loss_start", required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate lossStart,
            @RequestParam(value = "loss_end", required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate lossEnd,
            @RequestParam(value = "connection_create_start", required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate connectionCreateStart,
            @RequestParam(value = "connection_create_end", required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate connectionCreateEnd,
            @RequestParam(value = "time_span_lower_limit", required = false) Long timeSpanLowerLimit,
            @RequestParam(value = "time_span_upper_limit", required = false) Long timeSpanUpperLimit,
            @RequestParam(value = "sort_field", required = false) String sortField,
            @RequestParam(value = "sort_type", required = false) String sortType,
            javax.servlet.http.HttpSession session) {
        StaffPo current = currentStaffService.requireStaffAdmin(session);
        byte[] content = customerLossService.exportXlsx(
                current.getExtCorpId(),
                extStaffIds,
                lossStart,
                lossEnd,
                connectionCreateStart,
                connectionCreateEnd,
                timeSpanLowerLimit,
                timeSpanUpperLimit,
                sortField,
                sortType);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename("customer_losses.xlsx").build().toString())
                .body(content);
    }

    @GetMapping("/customers/action/export")
    @RequirePermission(biz = BizIdentity.BIZ_CUSTOMER_INFO, operation = OperationType.READ)
    public ResponseEntity<byte[]> exportCustomers(
            @RequestParam(required = false) String name,
            @RequestParam(value = "ext_staff_ids", required = false) List<String> extStaffIds,
            @RequestParam(value = "ext_tag_ids", required = false) List<String> extTagIds,
            @RequestParam(value = "channel_type", required = false) Integer channelType,
            @RequestParam(required = false) Integer gender,
            @RequestParam(required = false) Integer type,
            @RequestParam(value = "start_time", required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startTime,
            @RequestParam(value = "end_time", required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endTime,
            @RequestParam(value = "sort_field", required = false) String sortField,
            @RequestParam(value = "sort_type", required = false) String sortType,
            javax.servlet.http.HttpSession session) {
        StaffPo current = currentStaffService.requireStaffAdmin(session);
        byte[] content = customerExportService.exportXlsx(
                current.getExtCorpId(),
                name,
                extStaffIds,
                extTagIds,
                channelType,
                gender,
                type,
                startTime,
                endTime,
                sortField,
                sortType);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename("customers.xlsx").build().toString())
                .body(content);
    }
}
