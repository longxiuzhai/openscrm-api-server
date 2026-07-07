package cn.openscrm.api.persistence.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * 渠道码
 */
@Getter
@Setter
@TableName("contact_way")
public class ContactWayPo {

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
     * 渠道码名称
     */
    @TableField("name")
    private String name;


    /**
     * 渠道码配置ID
     */
    @TableField("config_id")
    private String configId;


    /**
     * 活码分组ID
     */
    @TableField("group_id")
    private Long groupId;


    /**
     * 联系二维码的URL
     */
    @TableField("qr_code")
    private String qrCode;


    /**
     * 渠道码的备注信息
     */
    @TableField("remark")
    private String remark;


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
     * 欢迎语类型：1，渠道欢迎语；2, 渠道默认欢迎语；3，不送欢迎语；
     */
    @TableField("auto_reply_type")
    private Integer autoReplyType;


    /**
     * 欢迎语策略
     */
    @TableField("auto_reply")
    private String autoReply;


    /**
     * 客户描述
     */
    @TableField("customer_desc")
    private String customerDesc;


    /**
     * 是否开启客户描述
     */
    @TableField("customer_desc_enable")
    private Integer customerDescEnable;


    /**
     * 客户备注
     */
    @TableField("customer_remark")
    private String customerRemark;


    /**
     * 是否开启客户备注
     */
    @TableField("customer_remark_enable")
    private Integer customerRemarkEnable;


    /**
     * 是否开启员工每日添加上限
     */
    @TableField("daily_add_customer_limit_enable")
    private Integer dailyAddCustomerLimitEnable;


    /**
     * 员工每日添加上限
     */
    @TableField("daily_add_customer_limit")
    private Long dailyAddCustomerLimit;


    /**
     * 是否开启工作日调度
     */
    @TableField("schedule_enable")
    private Integer scheduleEnable;


    /**
     * 是否开启员工自行上下线
     */
    @TableField("staff_control_enable")
    private Integer staffControlEnable;


    /**
     * 是否自动打标签
     */
    @TableField("auto_tag_enable")
    private Integer autoTagEnable;


    /**
     * 自动打标签绑定的标签ExtID数组
     */
    @TableField("customer_tag_ext_ids")
    private String customerTagExtIds;


    /**
     * 是否开启自动通过好友时段控制
     */
    @TableField("auto_skip_verify_enable")
    private Integer autoSkipVerifyEnable;


    /**
     * 自动通过好友开启时刻
     */
    @TableField("skip_verify_start_time")
    private Long skipVerifyStartTime;


    /**
     * 自动通过好友结束时刻
     */
    @TableField("skip_verify_end_time")
    private Long skipVerifyEndTime;


    /**
     * 实时关联的外部员工ID
     */
    @TableField("ext_staff_ids")
    private String extStaffIds;


    /**
     * 是否开启客户昵称屏蔽欢迎语
     */
    @TableField("nickname_block_enable")
    private Integer nicknameBlockEnable;


    /**
     * 客户昵称屏蔽欢迎语列表
     */
    @TableField("nickname_block_list")
    private String nicknameBlockList;


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