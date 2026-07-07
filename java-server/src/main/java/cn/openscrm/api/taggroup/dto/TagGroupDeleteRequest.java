package cn.openscrm.api.taggroup.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TagGroupDeleteRequest {

    @NotEmpty
    @JsonProperty("ext_ids")
    private List<String> extIds = new ArrayList<>();
}
