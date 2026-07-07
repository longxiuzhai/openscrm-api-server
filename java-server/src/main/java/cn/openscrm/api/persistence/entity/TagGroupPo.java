package cn.openscrm.api.persistence.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * 企微客户标签组
 */
@Getter
@Setter
@TableName("tag_group")
public class TagGroupPo {

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
     * 外部标签分组ID
     */
    @TableField("ext_id")
    private String extId;


    /**
     * 组名字
     */
    @TableField("name")
    private String name;


    @TableField("create_time")
    private Integer createTime;


    /**
     * order值大的排序靠前
     */
    @TableField("order")
    private Integer order;


    /**
     * 该标签组可用部门列表,默认0全部可用
     */
    @TableField("department_list")
    private String departmentList;


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