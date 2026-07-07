package cn.openscrm.api.persistence.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * 员工
 */
@Getter
@Setter
@TableName("staff")
public class StaffPo {

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
     * 外部员工ID
     */
    @TableField("ext_id")
    private String extId;


    /**
     * 角色ID
     */
    @TableField("role_id")
    private Long roleId;


    /**
     * 角色类型
     */
    @TableField("role_type")
    private String roleType;


    /**
     * 员工名
     */
    @TableField("name")
    private String name;


    /**
     * 地址
     */
    @TableField("address")
    private String address;


    /**
     * 别名
     */
    @TableField("alias")
    private String alias;


    /**
     * 头像地址
     */
    @TableField("avatar_url")
    private String avatarUrl;


    @TableField("email")
    private String email;


    /**
     * 0表示未定义，1表示男性，2表示女性
     */
    @TableField("gender")
    private Integer gender;


    /**
     * 激活状态: 1=已激活，2=已禁用，4=未激活，5=退出企业。已激活代表已激活企业微信或已关注微工作台（原企业号）。未激活代表既未激活企业微信又未关注微工作台（原企业号）。
     */
    @TableField("status")
    private Integer status;


    /**
     * 手机号
     */
    @TableField("mobile")
    private String mobile;


    /**
     * 二维码
     */
    @TableField("qr_code_url")
    private String qrCodeUrl;


    /**
     * 电话
     */
    @TableField("telephone")
    private String telephone;


    @TableField("enable")
    private Integer enable;


    /**
     * 微信返回的内容签名
     */
    @TableField("signature")
    private String signature;


    @TableField("external_position")
    private String externalPosition;


    @TableField("external_profile")
    private String externalProfile;


    @TableField("extattr")
    private String extattr;


    @TableField("customer_count")
    private Long customerCount;


    @TableField("dept_ids")
    private String deptIds;


    @TableField("welcome_msg_id")
    private Long welcomeMsgId;


    @TableField("is_authorized")
    private Integer isAuthorized;


    @TableField("enable_msg_arch")
    private Integer enableMsgArch;


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