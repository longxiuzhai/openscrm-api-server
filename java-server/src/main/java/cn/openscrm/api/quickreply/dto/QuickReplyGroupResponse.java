package cn.openscrm.api.quickreply.dto;

import cn.openscrm.api.persistence.entity.QuickReplyGroupPo;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class QuickReplyGroupResponse {

    private final QuickReplyGroupPo group;
    private final List<QuickReplyGroupResponse> subGroups;
    private final List<QuickReplyResponse> quickReplies;
}
