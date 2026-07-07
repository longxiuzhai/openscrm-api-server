package cn.openscrm.api.remainder.dto;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RemainderDeleteRequest {

    private List<Long> ids;
}
