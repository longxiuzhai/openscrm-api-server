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
public class TagGroupUpdateRequest {

    @NotBlank
    @Size(max = 255)
    private String name;

    private Integer order;

    @JsonProperty("remove_ext_tag_ids")
    private List<String> removeExtTagIds = new ArrayList<>();

    @Valid
    private List<TagGroupTagRequest> tags = new ArrayList<>();

    @JsonProperty("department_list")
    private List<Long> departmentList = new ArrayList<>();
}
