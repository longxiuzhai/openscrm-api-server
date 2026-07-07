package cn.openscrm.api.wework;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExternalContactRemarkRequest {

    private String userid;

    @JsonProperty("external_userid")
    private String externalUserId;

    private String remark;

    private String description;

    @JsonProperty("remark_company")
    private String remarkCompany;
}
