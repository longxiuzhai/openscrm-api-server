package cn.openscrm.api.contactway.dto;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ContactWayScheduleRequest {

    private Long id;
    private Long dailyAddCustomerLimit;
    private List<String> weekdays = new ArrayList<>();
    private Long startTime;
    private Long endTime;
    private List<ContactWayStaffRequest> staffs = new ArrayList<>();
}
