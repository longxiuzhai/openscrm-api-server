package cn.openscrm.api.taggroup.dto;

import cn.openscrm.api.persistence.entity.TagPo;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TagResponse {

    private Long id;

    @JsonProperty("ext_id")
    private String extId;

    @JsonProperty("ext_group_id")
    private String extGroupId;

    private String name;

    @JsonProperty("group_name")
    private String groupName;

    @JsonProperty("create_time")
    private Integer createTime;

    private Integer order;

    private Integer type;

    public static TagResponse from(TagPo tag) {
        TagResponse response = new TagResponse();
        response.setId(tag.getId());
        response.setExtId(tag.getExtId());
        response.setExtGroupId(tag.getExtGroupId());
        response.setName(tag.getName());
        response.setGroupName(tag.getGroupName());
        response.setCreateTime(tag.getCreateTime());
        response.setOrder(tag.getOrder());
        response.setType(tag.getType());
        return response;
    }
}
