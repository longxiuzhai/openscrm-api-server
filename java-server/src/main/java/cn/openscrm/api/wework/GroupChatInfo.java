package cn.openscrm.api.wework;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GroupChatInfo {

    @JsonProperty("chat_id")
    private String chatId;

    private String name;

    private String owner;

    private String notice;

    @JsonProperty("create_time")
    private Integer createTime;

    @JsonProperty("admin_list")
    private List<AdminInfo> adminList = new ArrayList<>();

    @JsonProperty("member_list")
    private List<GroupChatMemberInfo> memberList = new ArrayList<>();

    @Getter
    @Setter
    public static class AdminInfo {
        private String userid;
    }
}
