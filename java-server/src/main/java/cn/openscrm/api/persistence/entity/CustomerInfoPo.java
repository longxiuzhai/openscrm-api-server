package cn.openscrm.api.persistence.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * 对客户编辑的用户画像，不同于企业微信员工对客户的备注和描述，后者记录在staff_customer中
 */
@Getter
@Setter
@TableName("customer_info")
public class CustomerInfoPo {

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
     * 微信客户ID
     */
    @TableField("ext_customer_id")
    private String extCustomerId;


    /**
     * 微信员工ID
     */
    @TableField("ext_staff_id")
    private String extStaffId;


    /**
     * 年龄
     */
    @TableField("age")
    private Integer age;


    /**
     * 描述
     */
    @TableField("description")
    private String description;


    /**
     * 邮箱
     */
    @TableField("email")
    private String email;


    /**
     * 电话
     */
    @TableField("phone_number")
    private String phoneNumber;


    /**
     * qq
     */
    @TableField("qq")
    private String qq;


    /**
     * 地址
     */
    @TableField("address")
    private String address;


    /**
     * 生日
     */
    @TableField("birthday")
    private String birthday;


    /**
     * 微博
     */
    @TableField("weibo")
    private String weibo;


    /**
     * 自定义字段的值
     */
    @TableField("remark_field")
    private String remarkField;


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