package cn.openscrm.api.quickreply.dto;

import cn.openscrm.api.persistence.entity.QuickReplyDetailPo;
import cn.openscrm.api.persistence.entity.QuickReplyPo;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class QuickReplyResponse {

    private final QuickReplyPo quickReply;
    private final String avatar;
    private final List<QuickReplyDetailPo> replyDetails;
}
