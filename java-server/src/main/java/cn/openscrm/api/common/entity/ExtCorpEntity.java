package cn.openscrm.api.common.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExtCorpEntity extends BaseEntity {

    @TableField("ext_corp_id")
    private String extCorpId;

    @TableField("ext_creator_id")
    private String extCreatorId;
}
