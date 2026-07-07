package cn.openscrm.api.massmsg.dto;

import cn.openscrm.api.persistence.entity.GroupChatMassMsgPo;
import cn.openscrm.api.persistence.entity.GroupChatMassMsgResultPo;
import cn.openscrm.api.persistence.entity.StaffPo;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GroupChatMassMsgDetailResponse {

    private final GroupChatMassMsgPo groupChatMassMsg;
    private final StaffPo creator;
    private final List<GroupChatMassMsgResultPo> results;
}
