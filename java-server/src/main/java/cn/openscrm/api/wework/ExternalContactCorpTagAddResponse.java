package cn.openscrm.api.wework;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExternalContactCorpTagAddResponse extends CommonResponse {

    @JsonProperty("tag_group")
    private ExternalContactCorpTagGroup tagGroup;
}
