package cn.openscrm.api.deletenotify.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StaffDeleteCustomerResponse {

    private Long id;

    @JsonProperty("ext_customer_id")
    private String extCustomerId;

    @JsonProperty("ext_customer_avatar")
    private String extCustomerAvatar;

    @JsonProperty("ext_customer_name")
    private String extCustomerName;

    @JsonProperty("customer_corp_name")
    private String customerCorpName;

    @JsonProperty("customer_type")
    private Integer customerType;

    @JsonProperty("relation_create_at")
    private LocalDateTime relationCreateAt;

    @JsonProperty("relation_delete_at")
    private LocalDateTime relationDeleteAt;

    @JsonProperty("ext_staff_avatar")
    private String extStaffAvatar;

    @JsonProperty("ext_staff_id")
    private String extStaffId;

    @JsonProperty("staff_id")
    private Long staffId;

    @JsonProperty("staff_name")
    private String staffName;
}
