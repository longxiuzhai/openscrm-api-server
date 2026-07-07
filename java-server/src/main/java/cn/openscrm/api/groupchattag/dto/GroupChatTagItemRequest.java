package cn.openscrm.api.groupchattag.dto;

import javax.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GroupChatTagItemRequest {

    private Long id;

    @Size(max = 64)
    private String name;
}
