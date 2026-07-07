package cn.openscrm.api.customerloss.dto;

import cn.openscrm.api.persistence.entity.CustomerStaffTagPo;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomerLossResponse {

    private Long id;

    @JsonProperty("ext_customer_id")
    private String extCustomerId;

    @JsonProperty("customer_avatar")
    private String customerAvatar;

    @JsonProperty("customer_corp_name")
    private String customerCorpName;

    @JsonProperty("customer_type")
    private Integer customerType;

    @JsonProperty("ext_customer_name")
    private String extCustomerName;

    @JsonProperty("relation_create_at")
    private LocalDateTime relationCreateAt;

    @JsonProperty("customer_delete_staff_at")
    private LocalDateTime customerDeleteStaffAt;

    @JsonProperty("staff_name")
    private String staffName;

    @JsonProperty("staff_id")
    private Long staffId;

    @JsonProperty("ext_staff_id")
    private String extStaffId;

    @JsonProperty("staff_avatar")
    private String staffAvatar;

    @JsonProperty("in_connection_time_range")
    private Long inConnectionTimeRange;

    private List<CustomerStaffTagPo> tags = new ArrayList<>();
}
