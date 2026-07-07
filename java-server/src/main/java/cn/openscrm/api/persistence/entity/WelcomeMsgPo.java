package cn.openscrm.api.persistence.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * 主欢迎语-多个分时欢迎语 主欢迎语维护可用员工和部门id列表
 */
@Getter
@Setter
@TableName("welcome_msg")
public class WelcomeMsgPo {

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
     * 标题
     */
    @TableField("name")
    private String name;


    @TableField("welcome_msg")
    private String welcomeMsg;


    @TableField("main_welcome_msg_id")
    private Long mainWelcomeMsgId;


    @TableField("enable_time_period_msg")
    private Integer enableTimePeriodMsg;


    @TableField("effective_at")
    private String effectiveAt;


    /**
     * 分时段欢迎语-开始时间
     */
    @TableField("start_time")
    private Long startTime;


    /**
     * 分时段欢迎语-结束时间
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