package cn.openscrm.api.wework;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DepartmentInfo {

    private Long id;

    private String name;

    @JsonProperty("parentid")
    private Long parentId;

    private Integer order;
}
