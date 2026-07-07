package cn.openscrm.api.contactway.dto;

import cn.openscrm.api.persistence.entity.ContactWayGroupPo;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ContactWayGroupResponse {

    @JsonUnwrapped
    private ContactWayGroupPo contactWayGroup;
    private Long count;

    public ContactWayGroupResponse(ContactWayGroupPo contactWayGroup, Long count) {
        this.contactWayGroup = contactWayGroup;
        this.count = count;
    }
}
