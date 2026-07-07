package cn.openscrm.api.quickreply.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuickReplyRequest {

    private Long id;

    private String name;

    @JsonProperty("group_id")
    private String groupId;

    @JsonProperty("reply_details")
    private List<QuickReplyDetailRequest> replyDetails;

    @JsonProperty("deleted_ids")
    private List<Long> deletedIds;
}
