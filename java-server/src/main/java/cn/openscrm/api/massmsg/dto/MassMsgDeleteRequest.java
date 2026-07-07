package cn.openscrm.api.massmsg.dto;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MassMsgDeleteRequest {

    private List<Long> ids;
}
