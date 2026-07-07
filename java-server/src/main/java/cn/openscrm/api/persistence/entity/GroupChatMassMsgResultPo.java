package cn.openscrm.api.persistence.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * 客户群群发发送结果明细
 */
@Getter
@Setter
@TableName("group_chat_mass_msg_result")
public class GroupChatMassMsgResultPo {

    @TableId("id")
    private Long id;

    @TableField("ext_corp_id")
    private String extCorpId;

    @TableField("ext_creator_id")
    private String extCreatorId;

    @TableField("group_chat_mass_msg_id")
    private Long groupChatMassMsgId;

    @TableField("ext_staff_id")
    private String extStaffId;

    @TableField("ext_msg_id")
    private String extMsgId;

    @TableField("ext_chat_id")
    private String extChatId;

    @TableField("send_time")
    private Integer sendTime;

    @TableField("is_sent")
    private Integer isSent;

    @TableField("is_delivered")
    private Integer isDelivered;

    @TableField("failed_reason")
    private Integer failedReason;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;

    @TableField("deleted_at")
    private LocalDateTime deletedAt;
}
