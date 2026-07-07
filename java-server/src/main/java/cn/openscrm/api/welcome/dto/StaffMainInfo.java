package cn.openscrm.api.welcome.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class StaffMainInfo {

    private Long id;

    @JsonProperty("avatar_url")
    private String avatarUrl;

    @JsonProperty("ext_id")
    private String extId;

    private String name;
}
