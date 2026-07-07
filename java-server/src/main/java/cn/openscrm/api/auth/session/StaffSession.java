package cn.openscrm.api.auth.session;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StaffSession implements Serializable {

    private Long id;
    private String extCorpId;
    private String extStaffId;
    private Long roleId;
    private String roleType;
    private String name;
}
