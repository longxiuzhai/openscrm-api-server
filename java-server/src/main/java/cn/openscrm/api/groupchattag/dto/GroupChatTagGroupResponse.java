package cn.openscrm.api.groupchattag.dto;

import cn.openscrm.api.persistence.entity.GroupChatTagGroupPo;
import cn.openscrm.api.persistence.entity.GroupChatTagPo;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GroupChatTagGroupResponse {

    private Long id;

    private String extCorpId;

    private String extCreatorId;

    private String name;

    private List<GroupChatTagPo> tags = new ArrayList<>();

    public static GroupChatTagGroupResponse from(GroupChatTagGroupPo group, List<GroupChatTagPo> tags) {
        GroupChatTagGroupResponse response = new GroupChatTagGroupResponse();
        response.setId(group.getId());
        response.setExtCorpId(group.getExtCorpId());
        response.setExtCreatorId(group.getExtCreatorId());
        response.setName(group.getName());
        response.setTags(tags);
        return response;
    }
}
