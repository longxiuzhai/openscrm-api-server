package cn.openscrm.api.persistence.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * 渠道码调度设置（根据时间自动上下线员工）
 */
@Getter
@Setter
@TableName("contact_way_schedule")
public class ContactWaySchedulePo {

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
     * 渠道码ID
     */
    @TableField("contact_way_id")
    private Long contactWayId;


    /**
     * 员工每日添加客户上限
     */
    @TableField("daily_add_customer_limit")
    private Long dailyAddCustomerLimit;


    /**
     * 工作日
     */
    @TableField("weekdays")
    private String weekdays;


    /**
     * 开始时间
     */
    @TableField("start_time")
    private Long startTime;


    /**
     * 结束时间
     */
    @TableField("end_time")
    private Long endTime;


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