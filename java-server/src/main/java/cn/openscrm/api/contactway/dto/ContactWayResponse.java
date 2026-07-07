package cn.openscrm.api.contactway.dto;

import cn.openscrm.api.persistence.entity.ContactWayBackupStaffPo;
import cn.openscrm.api.persistence.entity.ContactWayGroupPo;
import cn.openscrm.api.persistence.entity.ContactWayPo;
import cn.openscrm.api.persistence.entity.ContactWaySchedulePo;
import cn.openscrm.api.persistence.entity.ContactWayScheduleStaffPo;
import cn.openscrm.api.persistence.entity.ContactWayStaffPo;
import cn.openscrm.api.persistence.entity.TagPo;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ContactWayResponse {

    @JsonUnwrapped
    private ContactWayPo contactWay;
    private ContactWayGroupPo group;
    @JsonProperty("customer_tags")
    private List<TagPo> customerTags = new ArrayList<>();
    private List<ContactWayStaffPo> staffs = new ArrayList<>();
    private List<ContactWayBackupStaffPo> backupStaffs = new ArrayList<>();
    private List<ScheduleWithStaffs> schedules = new ArrayList<>();

    @Getter
    @Setter
    public static class ScheduleWithStaffs {
        private ContactWaySchedulePo schedule;
        private List<ContactWayScheduleStaffPo> staffs = new ArrayList<>();
    }
}
