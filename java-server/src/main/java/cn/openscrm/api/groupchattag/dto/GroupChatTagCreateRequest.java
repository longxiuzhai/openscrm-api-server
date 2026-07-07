package cn.openscrm.api.groupchattag.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GroupChatTagCreateRequest {

    @NotNull
    @JsonProperty("group_id")
    private Long groupId;

    @NotEmpty
    private List<String> names = new ArrayList<>();
}
