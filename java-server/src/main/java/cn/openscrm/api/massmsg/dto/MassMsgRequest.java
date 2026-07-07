package cn.openscrm.api.massmsg.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MassMsgRequest {

    @JsonProperty("ext_staff_ids")
    private List<String> extStaffIds;

    @JsonProperty("send_type")
    private Integer sendType;

    @JsonProperty("send_at")
    private Long sendAt;

    @JsonProperty("chat_type")
    private String chatType;

    @JsonProperty("ext_department_ids")
    private List<Long> extDepartmentIds;

    @JsonProperty("ext_customer_filter_enable")
    private Integer extCustomerFilterEnable;

    @JsonProperty("ext_customer_filter")
    private JsonNode extCustomerFilter;

    private JsonNode msg;
}
