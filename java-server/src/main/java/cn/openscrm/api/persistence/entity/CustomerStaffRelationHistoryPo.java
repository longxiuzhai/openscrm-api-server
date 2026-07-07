package cn.openscrm.api.persistence.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * 员工客户关系的历史数据（流水记录）。 员工删除客户/客户删除员工时 新增一条数据，写入 customer_delete_staff_at/staff_delete_customer_at, 同时软删除原有记录。
 */
@Getter
@Setter
@TableName("customer_staff_relation_history")
public class CustomerStaffRelationHistoryPo {

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
     * 员工ID
     */
    @TableField("ext_staff_id")
    private String extStaffId;


    /**
     * 客户ID
     */
    @TableField("ext_customer_id")
    private String extCustomerId;


    /**
     * 员工添加客户的时间
     */
    @TableField("createtime")
    private LocalDateTime createtime;


    /**
     * 客户删除员工的时间
     */
    @TableField("customer_delete_staff_at")
    private LocalDateTime customerDeleteStaffAt;


    /**
     * 员工删除客户的时间
     */
    @TableField("staff_delete_customer_at")
    private LocalDateTime staffDeleteCustomerAt;


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
