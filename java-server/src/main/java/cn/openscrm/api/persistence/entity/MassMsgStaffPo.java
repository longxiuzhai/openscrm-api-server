package cn.openscrm.api.persistence.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * 客户群发员工执行记录
 */
@Getter
@Setter
@TableName("mass_msg_staff")
public class MassMsgStaffPo {

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


    @TableField("mass_msg_id")
    private Long massMsgId;


    @TableField("ext_staff_id")
    private String extStaffId;


    @TableField("ext_customer_id")
    private String extCustomerId;


    @TableField("ext_chat_id")
    private String extChatId;


    @TableField("is_sent")
    private Integer isSent;


    @TableField("is_delivered")
    private Integer isDelivered;


    @TableField("failed_reason")
    private Integer failedReason;


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