package cn.openscrm.api.taggroup.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TagGroupCreateRequest {

    @NotBlank
    @Size(max = 255)
    private String name;

    @JsonProperty("department_list")
    private List<Long> departmentList = new ArrayList<>();

    private Integer order;

    @Valid
    private List<TagGroupTagRequest> tags = new ArrayList<>();
}
