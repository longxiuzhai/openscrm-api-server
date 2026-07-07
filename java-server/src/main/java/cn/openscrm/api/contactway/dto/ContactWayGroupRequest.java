package cn.openscrm.api.contactway.dto;

import javax.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ContactWayGroupRequest {

    private String name;

    @Min(0)
    private Long sortWeight;
}
