package cn.openscrm.api.persistence.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * 删人提醒事件通知设置
 */
@Getter
@Setter
@TableName("event_notify")
public class EventNotifyPo {

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
     * 时间名称
     */
    @TableField("event_name")
    private String eventName;


    /**
     * 1-打开 2-关闭
     */
    @TableField("is_notify_admins")
    private Integer isNotifyAdmins;


    /**
     * 1-打开 2-关闭
     */
    @TableField("is_notify_staff")
    private Integer isNotifyStaff;


    /**
     * 通知类型 1-实时 2-定时
     */
    @TableField("notify_type")
    private Integer notifyType;


    /**
     * 接收通知的管理员
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