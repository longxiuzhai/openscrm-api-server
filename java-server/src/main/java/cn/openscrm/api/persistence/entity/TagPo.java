package cn.openscrm.api.persistence.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * 企微客户标签
 */
@Getter
@Setter
@TableName("tag")
public class TagPo {

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
     * 外部标签ID
     */
    @TableField("ext_id")
    private String extId;


    /**
     * 外部标签组ID
     */
    @TableField("ext_group_id")
    private String extGroupId;


    /**
     * 标签名称
     */
    @TableField("name")
    private String name;


    /**
     * 标签组名称
     */
    @TableField("group_name")
    private String groupName;


    /**
     * 创建时间
     */
    @TableField("create_time")
    private Integer createTime;


    /**
     * 标签排序值，值大的在前
     */
    @TableField("order")
    private Integer order;


    /**
     * 所打标签类型, 1-企业设置, 2-用户自定义
     */
    @TableField("type")
    private Integer type;


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