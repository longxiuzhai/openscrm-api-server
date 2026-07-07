package cn.openscrm.api.persistence.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * 客户群群发消息内容 消息内容不可修改
 */
@Getter
@Setter
@TableName("group_chat_mass_msg")
public class GroupChatMassMsgPo {

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
     * 1-立即发送,2-定时发送
     */
    @TableField("send_type")
    private Integer sendType;


    @TableField("ext_staff_ids")
    private String extStaffIds;


    /**
     * 消息内容
     */
    @TableField("msg")
    private String msg;


    /**
     * 微信消息ID
     */
    @TableField("ext_msg_id")
    private String extMsgId;


    /**
     * 创建企业群发消息的状态,1-预约发送,2-发送中,3-发送成功,4-发送失败,5-已取消
     */
    @TableField("mission_status")
    private Integer missionStatus;


    /**
     * 已发送群主计数
     */
    @TableField("delivered_num")
    private Integer deliveredNum;


    /**
     * 已送达群聊数
     */
    @TableField("success_num")
    private Integer successNum;


    /**
     * 未发送群主计数
     */
    @TableField("un_delivered_num")
    private Integer unDeliveredNum;


    /**
     * 未送达群聊计数
     */
    @TableField("failed_num")
    private Integer failedNum;


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