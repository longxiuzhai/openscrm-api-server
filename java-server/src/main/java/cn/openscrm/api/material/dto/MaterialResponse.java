package cn.openscrm.api.material.dto;

import cn.openscrm.api.persistence.entity.MaterialLibTagPo;
import cn.openscrm.api.persistence.entity.MaterialPo;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MaterialResponse {

    private final MaterialPo material;
    private final List<MaterialLibTagPo> tags;
}
