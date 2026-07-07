package cn.openscrm.api.persistence.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * 客户
 */
@Getter
@Setter
@TableName("customer")
public class CustomerPo {

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
     * 微信定义的userID
     */
    @TableField("ext_id")
    private String extId;


    /**
     * 名称，微信用户对应微信昵称；企业微信用户，则为联系人或管理员设置的昵称、认证的实名和账号名称
     */
    @TableField("name")
    private String name;


    /**
     * 职位,客户为企业微信时使用
     */
    @TableField("position")
    private String position;


    /**
     * 客户的公司名称,仅当客户ID为企业微信ID时存在
     */
    @TableField("corp_name")
    private String corpName;


    /**
     * 头像
     */
    @TableField("avatar")
    private String avatar;


    /**
     * 类型,1-微信用户, 2-企业微信用户
     */
    @TableField("type")
    private Integer type;


    /**
     * 性别,0-未知 1-男性 2-女性
     */
    @TableField("gender")
    private Integer gender;


    /**
     * 微信开放平台的唯一身份标识(微信unionID)
     */
    @TableField("unionid")
    private String unionid;


    /**
     * 仅当联系人类型是企业微信用户时有此字段
     */
    @TableField("external_profile")
    private String externalProfile;


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