package cn.openscrm.api.persistence.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * 渠道码绑定的员工
 */
@Getter
@Setter
@TableName("contact_way_staff")
public class ContactWayStaffPo {

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
     * 员工累计添加客户计数
     */
    @TableField("add_customer_count")
    private Long addCustomerCount;


    /**
     * 员工每日添加客户计数
     */
    @TableField("daily_add_customer_count")
    private Long dailyAddCustomerCount;


    /**
     * 员工每日添加客户上限
     */
    @TableField("daily_add_customer_limit")
    private Long dailyAddCustomerLimit;


    /**
     * 外部员工ID
     */
    @TableField("ext_staff_id")
    private String extStaffId;


    /**
     * 员工名
     */
    @TableField("name")
    private String name;


    /**
     * 头像地址
     */
    @TableField("avatar_url")
    private String avatarUrl;


    /**
     * 员工是否在线
     */
    @TableField("online")
    private Integer online;


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