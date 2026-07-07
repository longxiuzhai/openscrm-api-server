package cn.openscrm.api.persistence.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

/**
 * 客户群成员
 */
@Getter
@Setter
@TableName("group_chat_member")
public class GroupChatMemberPo {

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
     * 群聊id
     */
    @TableField("ext_chat_id")
    private String extChatId;


    /**
     * 群成员id
     */
    @TableField("userid")
    private String userid;


    /**
     * 群成员类型
     */
    @TableField("type")
    private Integer type;


    /**
     * 入群时间
     */
    @TableField("join_time")
    private Long joinTime;


    /**
     * 入群方式
     */
    @TableField("join_scene")
    private Integer joinScene;


    /**
     * 邀请者。目前仅当是由本企业内部成员邀请入群时会返回该值
     */
    @TableField("invitor")
    private String invitor;


    /**
     * 外部联系人在微信开放平台的唯一身份标识（微信unionid）
     */
    @TableField("unionid")
    private String unionid;

}