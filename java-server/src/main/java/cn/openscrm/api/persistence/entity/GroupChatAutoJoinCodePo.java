package cn.openscrm.api.persistence.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * 自动拉群码
 */
@Getter
@Setter
@TableName("group_chat_auto_join_code")
public class GroupChatAutoJoinCodePo {

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
     * 拉群方式，1-群二维码，2-企微活码
     */
    @TableField("create_type")
    private Integer createType;


    @TableField("group_id")
    private Long groupId;


    @TableField("remark")
    private String remark;


    @TableField("auto_reply")
    private String autoReply;


    @TableField("day_add_user_limit_enable")
    private Integer dayAddUserLimitEnable;


    @TableField("backup_staff_ids")
    private String backupStaffIds;


    /**
     * 自动拉群码配置ID
     */
    @TableField("config_id")
    private String configId;


    /**
     * 联系二维码的URL
     */
    @TableField("qr_code")
    private String qrCode;


    /**
     * 外部客户添加时是否无需验证，假布尔类型
     */
    @TableField("skip_verify")
    private Integer skipVerify;


    /**
     * 企业自定义的state参数
     */
    @TableField("state")
    private String state;


    /**
     * 扫码添加人次
     */
    @TableField("add_customer_count")
    private Long addCustomerCount;


    /**
     * 是否开启员工每日添加上限
     */
    @TableField("daily_add_customer_limit_enable")
    private Integer dailyAddCustomerLimitEnable;


    /**
     * 是否自动打标签
     */
    @TableField("auto_tag_enable")
    private Integer autoTagEnable;


    /**
     * 自动打标签绑定的标签ID数组
     */
    @TableField("ext_tag_ids")
    private String extTagIds;


    /**
     * 关联的外部员工ID
     */
    @TableField("ext_staff_ids")
    private String extStaffIds;


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