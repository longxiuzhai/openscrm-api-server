package cn.openscrm.api.groupchattag.dto;

import java.util.ArrayList;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GroupChatTagGroupCreateRequest {

    @NotBlank
    @Size(max = 64)
    private String name;

    @Valid
    private List<GroupChatTagItemRequest> tags = new ArrayList<>();
}
