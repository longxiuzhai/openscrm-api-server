package cn.openscrm.api.wework;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommonResponse {

    @JsonProperty("errcode")
    private Integer errCode;

    @JsonProperty("errmsg")
    private String errMsg;

    public void throwIfError() {
        if (errCode != null && errCode != 0) {
            throw new WeWorkApiException(errCode, errMsg);
        }
    }
}
