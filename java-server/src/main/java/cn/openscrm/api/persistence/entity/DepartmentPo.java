package cn.openscrm.api.persistence.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * 部门
 */
@Getter
@Setter
@TableName("department")
public class DepartmentPo {

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
     * 企微定义的部门ID
     */
    @TableField("ext_id")
    private Integer extId;


    /**
     * 部门名称
     */
    @TableField("name")
    private String name;


    /**
     * 上级部门ID,根部门为1
     */
    @TableField("ext_parent_id")
    private Integer extParentId;


    /**
     * 在父部门中的次序值
     */
    @TableField("`order`")
    private Integer order;


    /**
     * 部门使用的欢迎语
     */
    @TableField("welcome_msg_id")
    private Long welcomeMsgId;


    /**
     * 成员数量
     */
    @TableField("staff_num")
    private Integer staffNum;


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