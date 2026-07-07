package cn.openscrm.api.groupchattag.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GroupChatTagGroupUpdateRequest {

    @NotNull
    private Long id;

    @Size(max = 64)
    private String name;

    @JsonProperty("delete_tag_ids")
    private List<Long> deleteTagIds = new ArrayList<>();

    @Valid
    private List<GroupChatTagItemRequest> tags = new ArrayList<>();
}
