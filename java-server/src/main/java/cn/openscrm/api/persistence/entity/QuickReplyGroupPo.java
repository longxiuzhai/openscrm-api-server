package cn.openscrm.api.persistence.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * 话术库分组
 */
@Getter
@Setter
@TableName("quick_reply_group")
public class QuickReplyGroupPo {

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
     * 父级id
     */
    @TableField("parent_id")
    private Long parentId;


    /**
     * 可见部门ids
     */
    @TableField("departments")
    private String departments;


    /**
     * 是否是顶级分组
     */
    @TableField("is_top_group")
    private Integer isTopGroup;


    @TableField("`order`")
    private Integer order;


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