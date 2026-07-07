package cn.openscrm.api.customer.entity;

import cn.openscrm.api.common.entity.ExtCorpEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("customer")
public class CustomerEntity extends ExtCorpEntity {

    @TableField("ext_id")
    private String extId;

    @TableField("name")
    private String name;

    @TableField("position")
    private String position;

    @TableField("corp_name")
    private String corpName;

    @TableField("avatar")
    private String avatar;

    @TableField("type")
    private Integer type;

    @TableField("gender")
    private Integer gender;

    @TableField("unionid")
    private String unionid;

    @TableField("external_profile")
    private String externalProfile;
}
