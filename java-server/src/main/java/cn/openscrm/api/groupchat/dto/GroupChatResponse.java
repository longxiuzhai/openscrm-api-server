package cn.openscrm.api.groupchat.dto;

import cn.openscrm.api.persistence.entity.GroupChatMemberPo;
import cn.openscrm.api.persistence.entity.GroupChatPo;
import cn.openscrm.api.persistence.entity.GroupChatTagPo;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GroupChatResponse {

    private GroupChatPo groupChat;

    private List<GroupChatMemberPo> memberList = new ArrayList<>();

    private List<GroupChatTagPo> tags = new ArrayList<>();
}
