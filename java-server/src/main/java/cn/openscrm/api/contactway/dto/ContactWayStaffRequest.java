package cn.openscrm.api.contactway.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ContactWayStaffRequest {

    private Long id;
    private Long dailyAddCustomerLimit;
    private String extStaffId;
}
