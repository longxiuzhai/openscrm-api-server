package cn.openscrm.api.contactway.dto;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ContactWayRequest {

    private String name;
    private Long groupId;
    private Integer autoReplyType;
    private Object autoReply;
    private String customerDesc;
    private Integer customerDescEnable;
    private String customerRemark;
    private Integer customerRemarkEnable;
    private Integer dailyAddCustomerLimitEnable;
    private Long dailyAddCustomerLimit;
    private Integer scheduleEnable;
    private List<ContactWayStaffRequest> staffs = new ArrayList<>();
    private List<ContactWayStaffRequest> backupStaffs = new ArrayList<>();
    private List<ContactWayScheduleRequest> schedules = new ArrayList<>();
    private Integer autoTagEnable;
    private List<String> customerTagExtIds = new ArrayList<>();
    private Integer skipVerify;
    private Integer autoSkipVerifyEnable;
    private Long skipVerifyStartTime;
    private Long skipVerifyEndTime;
    private String remark;
    private Integer staffControlEnable;
    private Integer nicknameBlockEnable;
    private List<String> nicknameBlockList = new ArrayList<>();
}
