package cn.openscrm.api.wework;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExternalContactCorpTagGroup {

    @JsonProperty("group_id")
    private String groupId;

    @JsonProperty("group_name")
    private String groupName;

    @JsonProperty("create_time")
    private Integer createTime;

    private Integer order;

    private Boolean deleted;

    private List<ExternalContactCorpTag> tag = new ArrayList<>();
}
