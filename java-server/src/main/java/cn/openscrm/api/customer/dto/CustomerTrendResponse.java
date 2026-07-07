package cn.openscrm.api.customer.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CustomerTrendResponse {

    private final long number;

    private final String date;
}
