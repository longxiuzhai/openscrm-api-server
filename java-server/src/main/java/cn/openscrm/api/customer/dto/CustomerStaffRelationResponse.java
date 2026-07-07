package cn.openscrm.api.customer.dto;

import cn.openscrm.api.persistence.entity.CustomerStaffPo;
import cn.openscrm.api.persistence.entity.CustomerStaffTagPo;
import cn.openscrm.api.persistence.entity.InternalTagPo;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomerStaffRelationResponse {

    private CustomerStaffPo relation;

    private List<CustomerStaffTagPo> customerStaffTags = new ArrayList<>();

    private List<InternalTagPo> internalTags = new ArrayList<>();
}
