package cn.openscrm.api.massmsg.dto;

import cn.openscrm.api.persistence.entity.MassMsgPo;
import cn.openscrm.api.persistence.entity.MassMsgStaffPo;
import cn.openscrm.api.persistence.entity.StaffPo;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MassMsgDetailResponse {

    private final MassMsgPo massMsg;
    private final StaffPo creator;
    private final List<MassMsgStaffPo> staffs;
}
