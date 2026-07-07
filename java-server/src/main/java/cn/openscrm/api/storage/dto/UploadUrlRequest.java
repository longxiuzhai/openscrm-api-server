package cn.openscrm.api.storage.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UploadUrlRequest {

    @JsonProperty("file_name")
    private String fileName;
}
