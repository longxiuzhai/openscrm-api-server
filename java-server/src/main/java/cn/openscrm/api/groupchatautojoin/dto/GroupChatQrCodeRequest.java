package cn.openscrm.api.groupchatautojoin.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GroupChatQrCodeRequest {

    private Integer order;

    @JsonProperty("qr_media_id")
    private String qrMediaId;

    @JsonProperty("qr_url")
    private String qrUrl;

    @JsonProperty("user_limit")
    private Integer userLimit;

    private Integer status;
}
