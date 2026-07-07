package cn.openscrm.api.internaltag.dto;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InternalTagCreateRequest {

    private List<String> names;
}
