package cn.openscrm.api.persistence.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * 话术内容
 */
@Getter
@Setter
@TableName("quick_reply")
public class QuickReplyPo {

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
     * 内部企业id
     */
    @TableField("corp_id")
    private String corpId;


    /**
     * 话术名
     */
    @TableField("name")
    private String name;


    @TableField("quick_reply_type")
    private Integer quickReplyType;


    /**
     * 用于搜索的词语，多为标题
     */
    @TableField("searchable_text")
    private String searchableText;


    /**
     * 已发送次数
     */
    @TableField("send_count")
    private Integer sendCount;


    /**
     * 创建人企微ID
     */
    @TableField("ext_staff_id")
    private String extStaffId;


    /**
     * 创建人名字
     */
    @TableField("staff_name")
    private String staffName;


    @TableField("scope")
    private String scope;


    @TableField("group_id")
    private String groupId;


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