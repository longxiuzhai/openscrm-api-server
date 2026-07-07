package cn.openscrm.api.quickreply.dto;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuickReplyGroupDeleteRequest {

    private List<Long> ids;
}
