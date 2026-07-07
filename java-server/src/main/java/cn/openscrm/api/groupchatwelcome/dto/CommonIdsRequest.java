package cn.openscrm.api.groupchatwelcome.dto;

import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommonIdsRequest {

    @NotEmpty
    private List<Long> ids = new ArrayList<>();
}
