package cn.openscrm.api.groupchatautojoin.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GroupChatAutoJoinBatchRegroupRequest {

    private List<Long> ids;

    @JsonProperty("new_group_id")
    private Long newGroupId;
}
