package cn.openscrm.api.taggroup.dto;

import cn.openscrm.api.persistence.entity.TagGroupPo;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TagGroupResponse {

    private Long id;

    @JsonProperty("ext_id")
    private String extId;

    private String name;

    @JsonProperty("create_time")
    private Integer createTime;

    private Integer order;

    @JsonProperty("department_list")
    private List<Long> departmentList = new ArrayList<>();

    private List<TagResponse> tags = new ArrayList<>();

    public static TagGroupResponse from(TagGroupPo group, List<Long> departmentList, List<TagResponse> tags) {
        TagGroupResponse response = new TagGroupResponse();
        response.setId(group.getId());
        response.setExtId(group.getExtId());
        response.setName(group.getName());
        response.setCreateTime(group.getCreateTime());
        response.setOrder(group.getOrder());
        response.setDepartmentList(departmentList);
        response.setTags(tags);
        return response;
    }
}
