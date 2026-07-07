package cn.openscrm.api.groupchatautojoin.dto;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GroupChatAutoJoinDeleteRequest {

    private List<Long> ids;
}
