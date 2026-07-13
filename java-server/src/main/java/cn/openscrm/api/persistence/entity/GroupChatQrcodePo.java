package cn.openscrm.api.persistence.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

/**
 * 自动拉群码中的群二维码
 */
@Getter
@Setter
@TableName("group_chat_qrcode")
public class GroupChatQrcodePo {

    /**
     * 自动拉群码id
     */
    @TableField("group_chat_auto_join_id")
    private Long groupChatAutoJoinId;


    @TableField("`order`")
    private Integer order;


    /**
     * 群二维码pic media id
     */
    @TableField("qr_media_id")
    private String qrMediaId;


    /**
     * 群二维码的pic url
     */
    @TableField("qr_url")
    private String qrUrl;


    /**
     * 群二维码添加好友数上限
     */
    @TableField("user_limit")
    private Integer userLimit;


    /**
     * 群二维码状态,1- 使用中 2-已停用
     */
    @TableField("status")
    private Integer status;

}