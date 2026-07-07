package cn.openscrm.api.contactway.dto;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ContactWayGroupDeleteRequest {

    private List<Long> ids;
}
