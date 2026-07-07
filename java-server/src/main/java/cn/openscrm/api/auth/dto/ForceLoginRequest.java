package cn.openscrm.api.auth.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ForceLoginRequest {

    private String extCorpId;
    private String extStaffId;
}
