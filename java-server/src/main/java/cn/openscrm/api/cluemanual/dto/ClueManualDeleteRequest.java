package cn.openscrm.api.cluemanual.dto;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClueManualDeleteRequest {

    private List<Long> ids;
}
