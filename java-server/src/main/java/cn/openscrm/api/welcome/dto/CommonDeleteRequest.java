package cn.openscrm.api.welcome.dto;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommonDeleteRequest {

    private List<Long> ids;
}
