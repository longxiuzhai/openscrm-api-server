package cn.openscrm.api.persistence.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * 素材
 */
@Getter
@Setter
@TableName("material")
public class MaterialPo {

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


    @TableField("material_type")
    private String materialType;


    /**
     * 素材标题
     */
    @TableField("title")
    private String title;


    /**
     * 素材文件大小
     */
    @TableField("file_size")
    private Long fileSize;


    /**
     * 素材下载地址
     */
    @TableField("file_url")
    private String fileUrl;


    @TableField("link")
    private String link;


    /**
     * 链接类型素材的摘要
     */
    @TableField("digest")
    private String digest;


    @TableField("material_tag_list")
    private String materialTagList;


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