package cn.openscrm.api.persistence.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * 会话存档消息内容
 */
@Getter
@Setter
@TableName("chat_msg_content")
public class ChatMsgContentPo {

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
     * 内部消息ID
     */
    @TableField("chat_msg_id")
    private String chatMsgId;


    /**
     * 消息类型
     */
    @TableField("content_type")
    private String contentType;


    /**
     * 非文字类型的消息内容
     */
    @TableField("content")
    private String content;


    /**
     * 文件下载地址
     */
    @TableField("file_url")
    private String fileUrl;


    /**
     * 文件名
     */
    @TableField("file_name")
    private String fileName;


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