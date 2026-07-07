package cn.openscrm.api.persistence.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * 自动拉群码绑定的备份员工
 */
@Getter
@Setter
@TableName("group_chat_auto_join_backup_staff")
public class GroupChatAutoJoinBackupStaffPo {

    @TableId("id")
    private Long id;

    @TableField("ext_corp_id")
    private String extCorpId;

    @TableField("ext_creator_id")
    private String extCreatorId;

    @TableField("group_chat_auto_join_code_id")
    private Long groupChatAutoJoinCodeId;

    @TableField("daily_add_customer_count")
    private Long dailyAddCustomerCount;

    @TableField("add_customer_count")
    private Long addCustomerCount;

    @TableField("daily_add_customer_limit")
    private Long dailyAddCustomerLimit;

    @TableField("avatar")
    private String avatar;

    @TableField("staff_id")
    private Long staffId;

    @TableField("ext_staff_id")
    private String extStaffId;

    @TableField("name")
    private String name;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;

    @TableField("deleted_at")
    private LocalDateTime deletedAt;
}
