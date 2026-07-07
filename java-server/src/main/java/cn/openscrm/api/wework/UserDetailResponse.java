package cn.openscrm.api.wework;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class UserDetailResponse extends CommonResponse {

    @JsonProperty("userid")
    private String userId;

    private String name;

    @JsonProperty("department")
    private List<Long> departments = new ArrayList<>();

    @JsonProperty("order")
    private List<Integer> orders = new ArrayList<>();

    @JsonProperty("is_leader_in_dept")
    private List<Integer> isLeaderInDept = new ArrayList<>();

    private String position;

    private String mobile;

    private String gender;

    private String email;

    @JsonProperty("avatar")
    private String avatarUrl;

    private String telephone;

    @JsonProperty("enable")
    private Integer enable;

    private String alias;

    private Integer status;

    @JsonProperty("qr_code")
    private String qrCodeUrl;

    private Object extattr;

    @JsonProperty("external_profile")
    private Object externalProfile;

    @JsonProperty("external_position")
    private String externalPosition;
}
