package cn.openscrm.api.wework;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class GroupChatListRequest {

    @JsonProperty("status_filter")
    private Integer statusFilter;

    @JsonProperty("owner_filter")
    private OwnerFilter ownerFilter;

    private String cursor;

    private Integer limit = 100;

    @Getter
    @Setter
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public static class OwnerFilter {
        @JsonProperty("userid_list")
        private List<String> userIdList = new ArrayList<>();
    }
}
