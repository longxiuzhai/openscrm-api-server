package cn.openscrm.api.storage.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UploadUrlResponse {

    @JsonProperty("upload_url")
    private final String uploadUrl;

    @JsonProperty("download_url")
    private final String downloadUrl;
}
