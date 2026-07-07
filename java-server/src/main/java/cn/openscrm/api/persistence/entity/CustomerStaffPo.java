package cn.openscrm.api.persistence.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * 客户-员工关系 员工客户关系的历史数据（流水记录）也在此表中。 员工删除客户/客户删除员工时 新增一条数据，写入 customer_delete_staff_at/staff_delete_customer_at, 同时软删除原有记录。
 */
@Getter
@Setter
@TableName("customer_staff")
public class CustomerStaffPo {

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
     * 员工ID
     */
    @TableField("ext_staff_id")
    private String extStaffId;


    /**
     * 客户ID
     */
    @TableField("ext_customer_id")
    private String extCustomerId;


    /**
     * 员工对客户的备注
     */
    @TableField("remark")
    private String remark;


    /**
     * 员工对此客户的描述
     */
    @TableField("description")
    private String description;


    /**
     * 员工添加客户的时间
     */
    @TableField("createtime")
    private LocalDateTime createtime;


    /**
     * 员工对客户备注的企业名称
     */
    @TableField("remark_corp_name")
    private String remarkCorpName;


    /**
     * 对此客户备注的手机号码
     */
    @TableField("remark_mobiles")
    private String remarkMobiles;


    /**
     * 添加此客户的来源,0-未知来源 1-扫描二维码 2-搜索手机号 3-名片分享 4-群聊 5-手机通讯录 6-微信联系人 7-来自微信的添加好友申请 8-安装第三方应用时自动添加的客服人员 9-搜索邮箱 201-内部成员共享 202-管理员/负责人分配
     */
    @TableField("add_way")
    private Integer addWay;


    /**
     * 发起添加的userid
     */
    @TableField("oper_user_id")
    private String operUserId;


    /**
     * 区分客户具体是通过哪个「联系我」添加，由企业通过创建「联系我」方式指定
     */
    @TableField("state")
    private String state;


    /**
     * 是否已发送通知 1-是 2-否
     */
    @TableField("is_notified")
    private Integer isNotified;


    @TableField("internal_tag_ids")
    private String internalTagIds;


    @TableField("signature")
    private String signature;


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