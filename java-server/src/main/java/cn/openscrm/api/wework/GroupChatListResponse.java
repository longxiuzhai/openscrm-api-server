package cn.openscrm.api.wework;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GroupChatListResponse extends CommonResponse {

    @JsonProperty("group_chat_list")
    private List<Item> groupChatList = new ArrayList<>();

    @JsonProperty("next_cursor")
    private String nextCursor;

    @Getter
    @Setter
    public static class Item {
        @JsonProperty("chat_id")
        private String chatId;

        private Integer status;
    }
}
