package cn.openscrm.api.persistence.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * 角色
 */
@Getter
@Setter
@TableName("role")
public class RolePo {

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
     * 角色名称
     */
    @TableField("name")
    private String name;


    /**
     * 角色描述
     */
    @TableField("description")
    private String description;


    /**
     * 角色类型
     */
    @TableField("type")
    private String type;


    /**
     * 角色排序权重
     */
    @TableField("sort_weight")
    private Long sortWeight;


    /**
     * 是否为默认角色，1：是；2：否
     */
    @TableField("is_default")
    private Integer isDefault;


    /**
     * 角色绑定的权限标识数组
     */
    @TableField("permission_ids")
    private String permissionIds;


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