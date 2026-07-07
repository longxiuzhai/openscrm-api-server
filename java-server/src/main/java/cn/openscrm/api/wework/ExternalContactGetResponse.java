package cn.openscrm.api.wework;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExternalContactGetResponse extends CommonResponse {

    @JsonProperty("external_contact")
    private ExternalContactInfo externalContact;

    @JsonProperty("follow_user")
    private List<ExternalContactFollowUser> followUser = new ArrayList<>();
}
