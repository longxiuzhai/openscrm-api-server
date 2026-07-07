package cn.openscrm.api.groupchatautojoin.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GroupChatAutoJoinRequest {

    @JsonProperty("create_type")
    private Integer createType;

    private String remark;

    @JsonProperty("group_id")
    private Long groupId;

    @JsonProperty("ext_staff_ids")
    private List<String> extStaffIds;

    @JsonProperty("backup_ext_staff_ids")
    private List<String> backupExtStaffIds;

    private List<GroupChatAutoJoinStaffRequest> staffs;

    @JsonProperty("backup_staffs")
    private List<GroupChatAutoJoinStaffRequest> backupStaffs;

    @JsonProperty("skip_verify")
    private Integer skipVerify;

    @JsonProperty("auto_tag_enable")
    private Integer autoTagEnable;

    @JsonProperty("ext_tag_ids")
    private List<String> extTagIds;

    @JsonProperty("daily_add_customer_limit_enable")
    private Integer dailyAddCustomerLimitEnable;

    @JsonProperty("day_add_user_limit_enable")
    private Integer dayAddUserLimitEnable;

    @JsonProperty("auto_reply")
    private String autoReply;

    @JsonProperty("group_chat_qr_code")
    private List<GroupChatQrCodeRequest> groupChatQrCode;
}
