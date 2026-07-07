package cn.openscrm.api.groupchattag.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GroupChatTagUpdateRequest {

    @NotNull
    private Long id;

    @NotBlank
    @Size(max = 64)
    private String name;
}
