package cn.openscrm.api.persistence.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * 客户群
 */
@Getter
@Setter
@TableName("group_chat")
public class GroupChatPo {

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
     * 群名字
     */
    @TableField("name")
    private String name;


    /**
     * 群主ExtID
     */
    @TableField("owner")
    private String owner;


    /**
     * 群主名字
     */
    @TableField("owner_name")
    private String ownerName;


    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;


    /**
     * 群公告
     */
    @TableField("notice")
    private String notice;


    /**
     * 群管理员列表
     */
    @TableField("admin_list")
    private String adminList;


    /**
     * 群状态 1-解散 2-未解散
     */
    @TableField("status")
    private Integer status;


    /**
     * 群人数
     */
    @TableField("total")
    private Integer total;


    /**
     * 今日进群人数
     */
    @TableField("today_join_member_num")
    private Integer todayJoinMemberNum;


    /**
     * 今日退群人数
     */
    @TableField("today_quit_member_num")
    private Integer todayQuitMemberNum;


    @TableField("owner_avatar_url")
    private String ownerAvatarUrl;


    @TableField("owner_role_type")
    private String ownerRoleType;


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