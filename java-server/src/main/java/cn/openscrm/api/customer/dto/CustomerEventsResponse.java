package cn.openscrm.api.customer.dto;

import cn.openscrm.api.persistence.entity.CustomerEventPo;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomerEventsResponse {

    private List<CustomerEventPo> events = new ArrayList<>();

    private Long total = 0L;
}
