package cn.openscrm.api.stafffrontend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UploadMediaResponse {

    private String type;

    @JsonProperty("media_id")
    private String mediaId;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;
}
