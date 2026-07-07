package cn.openscrm.api.materialtag.dto;

import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MaterialTagDeleteRequest {

    @NotEmpty
    private List<Long> ids = new ArrayList<>();
}
