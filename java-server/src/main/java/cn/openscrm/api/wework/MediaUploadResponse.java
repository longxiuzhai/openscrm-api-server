package cn.openscrm.api.wework;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MediaUploadResponse extends CommonResponse {

    private String type;

    @JsonProperty("media_id")
    private String mediaId;

    @JsonProperty("created_at")
    private Long createdAt;
}
