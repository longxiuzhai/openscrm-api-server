package cn.openscrm.api.remainder.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RemainderRequest {

    @JsonProperty("send_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime sendAt;

    @JsonProperty("customer_name")
    private String customerName;

    private String content;

    @JsonProperty("ext_staff_id")
    private String extStaffId;

    @JsonProperty("ext_customer_id")
    private String extCustomerId;
}
