package cn.openscrm.api.persistence.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

/**
 * 员工部门关系
 */
@Getter
@Setter
@TableName("staff_department")
public class StaffDepartmentPo {

    @TableField("ext_corp_id")
    private String extCorpId;


    @TableField("ext_staff_id")
    private String extStaffId;


    @TableField("ext_department_id")
    private Integer extDepartmentId;


    @TableField("staff_id")
    private Long staffId;


    @TableField("department_id")
    private Long departmentId;


    /**
     * 是否是所在部门的领导
     */
    @TableField("is_leader")
    private Integer isLeader;


    /**
     * 所在部门的排序
     */
    @TableField("order")
    private Integer order;

}