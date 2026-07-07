package cn.openscrm.api.persistence.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * 渠道码分组
 */
@Getter
@Setter
@TableName("contact_way_group")
public class ContactWayGroupPo {

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
     * 分组名称
     */
    @TableField("name")
    private String name;


    /**
     * 分组排序权重
     */
    @TableField("sort_weight")
    private Long sortWeight;


    /**
     * 该分组渠道码数量
     */
    @TableField("count")
    private Long count;


    /**
     * 是否为默认分组，1：是；2：否
     */
    @TableField("is_default")
    private Integer isDefault;


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