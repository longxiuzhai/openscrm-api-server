package cn.openscrm.api.persistence.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * 自动拉群码绑定的员工
 */
@Getter
@Setter
@TableName("group_chat_auto_join_code_staff")
public class GroupChatAutoJoinCodeStaffPo {

    /**
     * ID
     */
    @TableId("id")
    private Long id;


    /**
     * 外部企业ID
     */
    @TableField("ext_corp_id")
    private String extCorpId;


    /**
     * 创建者外部员工ID
     */
    @TableField("ext_creator_id")
    private String extCreatorId;


    /**
     * 自动拉群码id
     */
    @TableField("group_chat_auto_join_code_id")
    private Long groupChatAutoJoinCodeId;


    /**
     * 员工每日添加客户计数
     */
    @TableField("daily_add_customer_count")
    private Long dailyAddCustomerCount;


    /**
     * 员工累计添加客户计数
     */
    @TableField("add_customer_count")
    private Long addCustomerCount;


    /**
     * 员工每日添加客户上限
     */
    @TableField("daily_add_customer_limit")
    private Long dailyAddCustomerLimit;


    /**
     * 员工头像
     */
    @TableField("avatar")
    private String avatar;


    /**
     * 员工ID
     */
    @TableField("staff_id")
    private Long staffId;


    /**
     * 外部员工ID
     */
    @TableField("ext_staff_id")
    private String extStaffId;


    /**
     * 员工名称
     */
    @TableField("name")
    private String name;


    /**
     * 创建时间
     */
    @TableField("created_at")
    private LocalDateTime createdAt;


    /**
     * 更新时间
     */
    @TableField("updated_at")
    private LocalDateTime updatedAt;


    /**
     * 删除时间
     */
    @TableField("deleted_at")
    private LocalDateTime deletedAt;

}