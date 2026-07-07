package cn.openscrm.api.persistence.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * 话术库每条记录内容
 */
@Getter
@Setter
@TableName("quick_reply_detail")
public class QuickReplyDetailPo {

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


    @TableField("quick_reply_id")
    private String quickReplyId;


    @TableField("quick_reply_content")
    private String quickReplyContent;


    @TableField("scope")
    private String scope;


    /**
     * 单项类型
     */
    @TableField("content_type")
    private Integer contentType;


    @TableField("send_count")
    private Long sendCount;


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