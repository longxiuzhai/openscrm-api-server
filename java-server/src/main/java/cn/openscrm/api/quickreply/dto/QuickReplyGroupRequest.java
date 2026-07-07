package cn.openscrm.api.quickreply.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuickReplyGroupRequest {

    private Long id;

    private String name;

    private List<Long> departments;

    @JsonProperty("sub_groups")
    private List<QuickReplySubGroupRequest> subGroups;

    @JsonProperty("delete_group_ids")
    private List<Long> deleteGroupIds;
}
