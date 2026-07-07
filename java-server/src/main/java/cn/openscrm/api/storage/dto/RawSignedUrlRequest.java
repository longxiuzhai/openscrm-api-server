package cn.openscrm.api.storage.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RawSignedUrlRequest {

    @JsonProperty("object_key")
    private String objectKey;

    private String method;

    @JsonProperty("expired_in_sec")
    private Long expiredInSec;
}
