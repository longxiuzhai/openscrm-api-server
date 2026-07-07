package cn.openscrm.api.wework;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExternalContactMarkTagRequest {

    private String userid;

    @JsonProperty("external_userid")
    private String externalUserId;

    @JsonProperty("add_tag")
    private List<String> addTag = new ArrayList<>();

    @JsonProperty("remove_tag")
    private List<String> removeTag = new ArrayList<>();
}
