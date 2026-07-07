package cn.openscrm.api.taggroup.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TagGroupTagRequest {

    @Size(max = 255)
    private String name;

    @JsonProperty("ext_id")
    private String extId;

    private Integer order;
}
