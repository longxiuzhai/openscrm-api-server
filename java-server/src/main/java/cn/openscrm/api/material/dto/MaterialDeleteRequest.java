package cn.openscrm.api.material.dto;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MaterialDeleteRequest {

    private List<Long> ids;
}
