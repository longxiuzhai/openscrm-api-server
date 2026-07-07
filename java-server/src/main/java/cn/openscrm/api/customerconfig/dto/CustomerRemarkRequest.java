package cn.openscrm.api.customerconfig.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomerRemarkRequest {

    private Long id;

    private String name;

    @JsonProperty("field_name")
    private String fieldName;

    @JsonProperty("field_type")
    private String fieldType;

    @JsonProperty("option_name_list")
    private List<String> optionNameList;

    private List<Long> ids;

    @JsonProperty("exchange_order_id")
    private Long exchangeOrderId;
}
