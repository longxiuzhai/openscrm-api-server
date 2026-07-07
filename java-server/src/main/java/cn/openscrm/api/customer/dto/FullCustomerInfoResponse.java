package cn.openscrm.api.customer.dto;

import cn.openscrm.api.persistence.entity.CustomerInfoPo;
import cn.openscrm.api.persistence.entity.CustomerPo;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FullCustomerInfoResponse {

    private CustomerPo customer;

    private CustomerInfoPo customerInfo;

    private List<CustomerStaffRelationResponse> staffRelations = new ArrayList<>();

    private CustomerEventsResponse customerEvents = new CustomerEventsResponse();
}
