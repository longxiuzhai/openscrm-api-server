package cn.openscrm.api.groupchatautojoin.dto;

import cn.openscrm.api.persistence.entity.GroupChatAutoJoinCodePo;
import cn.openscrm.api.persistence.entity.GroupChatAutoJoinBackupStaffPo;
import cn.openscrm.api.persistence.entity.GroupChatAutoJoinCodeStaffPo;
import cn.openscrm.api.persistence.entity.GroupChatQrcodePo;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GroupChatAutoJoinResponse {

    private final GroupChatAutoJoinCodePo autoJoinCode;
    private final List<GroupChatQrcodePo> groupChatQrCode;
    private final List<GroupChatAutoJoinCodeStaffPo> staffs;
    private final List<GroupChatAutoJoinBackupStaffPo> backupStaffs;
}
