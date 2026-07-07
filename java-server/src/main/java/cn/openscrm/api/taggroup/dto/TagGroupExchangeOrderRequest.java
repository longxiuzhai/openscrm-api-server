package cn.openscrm.api.taggroup.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TagGroupExchangeOrderRequest {

    @NotNull
    private Long id;

    @NotNull
    @JsonProperty("exchange_order_id")
    private Long exchangeOrderId;
}
