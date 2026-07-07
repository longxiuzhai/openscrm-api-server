package cn.openscrm.api.commonutil.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ParseLinkResponse {

    private String title;

    private String desc;

    @JsonProperty("img_url")
    private String imgUrl;

    @JsonProperty("link_url")
    private String linkUrl;
}
