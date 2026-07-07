package cn.openscrm.api.persistence.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * 按天统计客户数量 unique_index: ext_staff_id - date
 */
@Getter
@Setter
@TableName("customer_statistic")
public class CustomerStatisticPo {

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
     * 外部员工ID
     */
    @TableField("ext_staff_id")
    private String extStaffId;


    /**
     * 客户总数
     */
    @TableField("total_customer_num")
    private Long totalCustomerNum;


    /**
     * 新增客户总数
     */
    @TableField("increase_customer_num")
    private Long increaseCustomerNum;


    /**
     * 流失客户总数
     */
    @TableField("decrease_customer_num")
    private Long decreaseCustomerNum;


    /**
     * 日期
     */
    @TableField("date")
    private LocalDate date;


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