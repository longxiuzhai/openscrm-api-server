package cn.openscrm.api.customerconfig.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomerInfoRequest {

    @JsonProperty("ext_customer_id")
    private String extCustomerId;

    @JsonProperty("ext_staff_id")
    private String extStaffId;

    private Integer age;

    private String description;

    private String email;

    @JsonProperty("phone_number")
    private String phoneNumber;

    private String qq;

    private String address;

    private String birthday;

    private String weibo;

    @JsonProperty("remark_field")
    private String remarkField;

    @JsonProperty("remark_values")
    private String remarkValues;
}
