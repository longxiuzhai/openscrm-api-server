package cn.openscrm.api.homepage.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomerSummaryResponse {

    @JsonProperty("corp_name")
    private String corpName;

    @JsonProperty("total_staffs_num")
    private long totalStaffsNum;

    @JsonProperty("total_customers_num")
    private long totalCustomersNum;

    @JsonProperty("today_customers_increase")
    private long todayCustomersIncrease;

    @JsonProperty("today_customers_decrease")
    private long todayCustomersDecrease;

    @JsonProperty("total_groups_num")
    private long totalGroupsNum;

    @JsonProperty("today_groups_increase")
    private long todayGroupsIncrease;

    @JsonProperty("today_groups_decrease")
    private long todayGroupsDecrease;
}
