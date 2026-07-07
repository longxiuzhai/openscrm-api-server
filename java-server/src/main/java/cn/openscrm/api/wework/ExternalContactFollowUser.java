package cn.openscrm.api.wework;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExternalContactFollowUser {

    private String userid;

    private String remark;

    private String description;

    private Integer createtime;

    @JsonProperty("remark_corp_name")
    private String remarkCorpName;

    @JsonProperty("remark_mobiles")
    private List<String> remarkMobiles = new ArrayList<>();

    @JsonProperty("add_way")
    private Integer addWay;

    @JsonProperty("oper_userid")
    private String operUserId;

    private String state;

    private List<Tag> tags = new ArrayList<>();

    @Getter
    @Setter
    public static class Tag {
        @JsonProperty("group_name")
        private String groupName;

        @JsonProperty("tag_name")
        private String tagName;

        private Integer type;

        @JsonProperty("tag_id")
        private String tagId;
    }
}
