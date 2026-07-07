package cn.openscrm.api.wework;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExternalContactCorpTagListResponse extends CommonResponse {

    @JsonProperty("tag_group")
    private List<ExternalContactCorpTagGroup> tagGroup = new ArrayList<>();
}
