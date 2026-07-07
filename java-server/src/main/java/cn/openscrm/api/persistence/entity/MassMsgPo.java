package cn.openscrm.api.persistence.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * 企业群发消息内容 消息内容不可修改
 */
@Getter
@Setter
@TableName("mass_msg")
public class MassMsgPo {

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


    @TableField("ext_department_ids")
    private String extDepartmentIds;


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
     * 是否有筛选条件
     */
    @TableField("ext_customer_filter_enable")
    private Integer extCustomerFilterEnable;


    /**
     * 发送客户的筛选条件
     */
    @TableField("ext_customer_filter")
    private String extCustomerFilter;


    /**
     * 已发送消息的员工数
     */
    @TableField("delivered_num")
    private Integer deliveredNum;


    /**
     * 成功送达消息的员工数
     */
    @TableField("success_num")
    private Integer successNum;


    /**
     * 需要发送消息的员工总数
     */
    @TableField("un_delivered_num")
    private Integer unDeliveredNum;


    /**
     * 未送达客户计数
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