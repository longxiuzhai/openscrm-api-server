package cn.openscrm.api.homepage.controller;

import cn.openscrm.api.auth.annotation.RequirePermission;
import cn.openscrm.api.auth.service.CurrentStaffService;
import cn.openscrm.api.common.api.ApiResponse;
import cn.openscrm.api.common.constant.BizIdentity;
import cn.openscrm.api.common.constant.OperationType;
import cn.openscrm.api.customer.dto.CustomerTrendResponse;
import cn.openscrm.api.customer.service.CustomerStatisticService;
import cn.openscrm.api.homepage.dto.CustomerSummaryResponse;
import cn.openscrm.api.homepage.service.HomePageService;
import cn.openscrm.api.persistence.entity.StaffPo;
import java.time.LocalDate;
import java.util.List;
import javax.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping({"/api/v1/staff-admin", "/api/v1/staff_admin"})
public class HomePageController {

    private final HomePageService homePageService;
    private final CustomerStatisticService customerStatisticService;
    private final CurrentStaffService currentStaffService;

    @GetMapping("/action/get-summary")
    @RequirePermission(biz = BizIdentity.BIZ_CUSTOMER_INFO, operation = OperationType.FULL)
    public ApiResponse<CustomerSummaryResponse> summary(HttpSession session) {
        StaffPo current = currentStaffService.requireStaffAdmin(session);
        return ApiResponse.ok(homePageService.summary(current.getExtCorpId()));
    }

    @GetMapping("/action/get-trend")
    @RequirePermission(biz = BizIdentity.BIZ_CUSTOMER_INFO, operation = OperationType.FULL)
    public ApiResponse<List<CustomerTrendResponse>> trend(
            @RequestParam("statistic_type") String statisticType,
            @RequestParam(value = "ext_staff_ids", required = false) List<String> extStaffIds,
            @RequestParam("start_time") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startTime,
            @RequestParam("end_time") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endTime,
            HttpSession session) {
        StaffPo current = currentStaffService.requireStaffAdmin(session);
        return ApiResponse.ok(customerStatisticService.query(
                current.getExtCorpId(), statisticType, extStaffIds, startTime, endTime));
    }
}
