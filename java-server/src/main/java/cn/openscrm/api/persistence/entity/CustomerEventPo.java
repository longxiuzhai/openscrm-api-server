package cn.openscrm.api.persistence.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * 事件类型
 */
@Getter
@Setter
@TableName("customer_event")
public class CustomerEventPo {

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
     * 事件内容
     */
    @TableField("content")
    private String content;


    /**
     * 事件类型
     */
    @TableField("event_type")
    private String eventType;


    /**
     * 事件名称
     */
    @TableField("event_name")
    private String eventName;


    /**
     * 企微定义的客户ID
     */
    @TableField("ext_customer_id")
    private String extCustomerId;


    /**
     * 微信定义的员工ID
     */
    @TableField("ext_staff_id")
    private String extStaffId;


    /**
     * 员工头像
     */
    @TableField("relate_staff_avatar")
    private String relateStaffAvatar;


    /**
     * 员工名字
     */
    @TableField("relate_staff_name")
    private String relateStaffName;


    /**
     * 提醒类型事件的发送时间
     */
    @TableField("send_at")
    private Integer sendAt;


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