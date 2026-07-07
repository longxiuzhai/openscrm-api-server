package cn.openscrm.api.persistence.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * 是否显示信息
 */
@Getter
@Setter
@TableName("customer_info_display_rule")
public class CustomerInfoDisplayRulePo {

    /**
     * ID
     */
    @TableId("id")
    private Long id;


    /**
     * 企业ID
     */
    @TableField("ext_corp_id")
    private String extCorpId;


    /**
     * 是否展示年龄
     */
    @TableField("age")
    private Integer age;


    /**
     * 是否展示描述
     */
    @TableField("description")
    private Integer description;


    /**
     * 是否展示邮箱
     */
    @TableField("email")
    private Integer email;


    /**
     * 是否展示电话
     */
    @TableField("phone_number")
    private Integer phoneNumber;


    /**
     * 是否展示mqq
     */
    @TableField("qq")
    private Integer qq;


    /**
     * 是否展示地址
     */
    @TableField("address")
    private Integer address;


    /**
     * 是否展示生日
     */
    @TableField("birthday")
    private Integer birthday;


    /**
     * 是否展示微博
     */
    @TableField("weibo")
    private Integer weibo;


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