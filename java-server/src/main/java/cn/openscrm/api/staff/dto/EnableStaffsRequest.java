package cn.openscrm.api.staff.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class EnableStaffsRequest {

    private List<String> extStaffIds = new ArrayList<>();

    private List<String> excludeExtStaffIds = new ArrayList<>();
}
