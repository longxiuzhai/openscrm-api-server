package cn.openscrm.api.persistence.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

/**
 * 会话存档消息
 */
@Getter
@Setter
@TableName("chat_msg")
public class ChatMsgPo {

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
     * 外部消息ID
     */
    @TableField("msg_id")
    private String msgId;


    /**
     * 消息动作，目前有send(发送消息)/recall(撤回消息)/switch(切换企业日志)三种类型
     */
    @TableField("`action`")
    private String action;


    /**
     * 消息发送方id。同一企业内容为userid，非相同企业为external_userid。消息如果是机器人发出，也为external_userid
     */
    @TableField("`from`")
    private String from;


    /**
     * 消息接收方列表
     */
    @TableField("to_list")
    private String toList;


    /**
     * 群聊消息的群id。如果是单聊则为空
     */
    @TableField("room_id")
    private String roomId;


    /**
     * 消息发送时间戳，utc时间，ms单位。
     */
    @TableField("msg_time")
    private Long msgTime;


    /**
     * 文本消息为：text
     */
    @TableField("msg_type")
    private String msgType;


    /**
     * 聊天的文本内容
     */
    @TableField("content_text")
    private String contentText;


    /**
     * 消息的seq值，标识消息的序号。再次拉取需要带上上次回包中最大的seq。Uint64类型，范围0-pow(2,64)-1
     */
    @TableField("seq")
    private Long seq;


    /**
     * 消息的会话ID,相同收发方的会话ID相同
     */
    @TableField("session_id")
    private String sessionId;


    /**
     * 会话类型
     */
    @TableField("session_type")
    private String sessionType;

}