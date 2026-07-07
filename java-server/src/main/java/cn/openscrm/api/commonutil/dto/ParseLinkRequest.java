package cn.openscrm.api.commonutil.dto;

import javax.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ParseLinkRequest {

    @NotBlank
    private String url;
}
