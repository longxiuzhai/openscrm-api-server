package cn.openscrm.api.wework;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExternalContactCorpTag {

    private String id;

    private String name;

    @JsonProperty("create_time")
    private Integer createTime;

    private Integer order;

    private Boolean deleted;
}
