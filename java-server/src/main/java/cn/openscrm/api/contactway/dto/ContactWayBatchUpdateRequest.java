package cn.openscrm.api.contactway.dto;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ContactWayBatchUpdateRequest {

    private Long groupId;
    private List<Long> ids;
}
