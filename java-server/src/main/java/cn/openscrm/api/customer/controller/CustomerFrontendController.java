package cn.openscrm.api.customer.controller;

import cn.openscrm.api.auth.service.CurrentCustomerService;
import cn.openscrm.api.auth.service.CurrentStaffService;
import cn.openscrm.api.common.api.ApiResponse;
import cn.openscrm.api.customer.dto.FullCustomerInfoResponse;
import cn.openscrm.api.customer.service.CustomerFrontendService;
import cn.openscrm.api.persistence.entity.CustomerPo;
import cn.openscrm.api.persistence.entity.StaffPo;
import javax.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class CustomerFrontendController {

    private final CurrentStaffService currentStaffService;
    private final CurrentCustomerService currentCustomerService;
    private final CustomerFrontendService customerFrontendService;

    @GetMapping({"/staff-frontend/customer/{extId}", "/staff_frontend/customer/{extId}"})
    public ApiResponse<FullCustomerInfoResponse> get(@PathVariable("extId") String extCustomerId,
                                                     HttpSession session) {
        StaffPo staff = currentStaffService.requireStaff(session);
        return ApiResponse.ok(customerFrontendService.getFullCustomerInfo(extCustomerId, staff));
    }

    @GetMapping({"/customer-frontend/customer/current", "/customer_frontend/customer/current"})
    public ApiResponse<CustomerPo> currentCustomer(HttpSession session) {
        return ApiResponse.ok(currentCustomerService.requireCustomer(session));
    }
}
