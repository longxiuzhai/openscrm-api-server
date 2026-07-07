package cn.openscrm.api.welcome.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DepartmentMainInfo {

    private Long id;

    @JsonProperty("ext_id")
    private Integer extId;

    private String name;
}
