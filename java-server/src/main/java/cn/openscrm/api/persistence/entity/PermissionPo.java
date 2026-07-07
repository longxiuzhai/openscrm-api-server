package cn.openscrm.api.persistence.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * 权限
 */
@Getter
@Setter
@TableName("permission")
public class PermissionPo {

    /**
     * ID
     */
    @TableId("id")
    private Long id;


    /**
     * 权限名称
     */
    @TableField("name")
    private String name;


    /**
     * 权限描述
     */
    @TableField("description")
    private String description;


    /**
     * 业务名称
     */
    @TableField("biz_name")
    private String bizName;


    /**
     * 业务标识
     */
    @TableField("biz_identity")
    private String bizIdentity;


    /**
     * 操作
     */
    @TableField("operation")
    private String operation;


    /**
     * 权限标识
     */
    @TableField("identity")
    private String identity;


    /**
     * 权限排序权重
     */
    @TableField("sort_weight")
    private Long sortWeight;


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