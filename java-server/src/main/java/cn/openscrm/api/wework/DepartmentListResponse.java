package cn.openscrm.api.wework;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class DepartmentListResponse extends CommonResponse {

    private List<DepartmentInfo> department = new ArrayList<>();
}
