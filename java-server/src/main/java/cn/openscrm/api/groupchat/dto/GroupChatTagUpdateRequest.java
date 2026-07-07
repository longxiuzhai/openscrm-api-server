package cn.openscrm.api.groupchat.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GroupChatTagUpdateRequest {

    @JsonProperty("group_chat_ids")
    private List<String> groupChatIds = new ArrayList<>();

    @JsonProperty("add_tag_ids")
    private List<Long> addTagIds = new ArrayList<>();

    @JsonProperty("remove_tag_ids")
    private List<Long> removeTagIds = new ArrayList<>();
}
