package cn.openscrm.api.wework;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GroupChatMemberInfo {

    private String userid;

    private Integer type;

    @JsonProperty("join_time")
    private Long joinTime;

    @JsonProperty("join_scene")
    private Integer joinScene;

    private String unionid;

    private Invitor invitor;

    @Getter
    @Setter
    public static class Invitor {
        private String userid;
    }
}
