package cn.openscrm.api.customer.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import cn.openscrm.api.customer.dto.CustomerTrendResponse;
import cn.openscrm.api.persistence.entity.CustomerStatisticPo;
import cn.openscrm.api.persistence.mapper.CustomerStatisticPoMapper;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

class CustomerStatisticServiceTest {

    @Test
    void totalCarriesForwardAndUsesFirstKnownValueForLeadingGap() {
        CustomerStatisticPoMapper mapper = mock(CustomerStatisticPoMapper.class);
        when(mapper.selectList(any())).thenReturn(Arrays.asList(
                row("2026-07-02", 5, 2, 0),
                row("2026-07-04", 8, 3, 0)));
        CustomerStatisticService service = new CustomerStatisticService(mapper);

        List<CustomerTrendResponse> result = service.query(
                "ww-corp", CustomerStatisticService.TOTAL, Collections.emptyList(),
                LocalDate.parse("2026-07-01"), LocalDate.parse("2026-07-05"));

        assertThat(result).extracting(CustomerTrendResponse::getNumber)
                .containsExactly(5L, 5L, 5L, 8L, 8L);
        assertThat(result).extracting(CustomerTrendResponse::getDate)
                .containsExactly("2026-07-01", "2026-07-02", "2026-07-03", "2026-07-04", "2026-07-05");
    }

    @Test
    void increaseFillsMissingDatesWithZero() {
        CustomerStatisticPoMapper mapper = mock(CustomerStatisticPoMapper.class);
        when(mapper.selectList(any())).thenReturn(Collections.singletonList(row("2026-07-02", 5, 2, 0)));
        CustomerStatisticService service = new CustomerStatisticService(mapper);

        List<CustomerTrendResponse> result = service.query(
                "ww-corp", CustomerStatisticService.INCREASE, null,
                LocalDate.parse("2026-07-01"), LocalDate.parse("2026-07-03"));

        assertThat(result).extracting(CustomerTrendResponse::getNumber).containsExactly(0L, 2L, 0L);
    }

    @Test
    void aggregatesMultipleRowsOnSameDate() {
        CustomerStatisticPoMapper mapper = mock(CustomerStatisticPoMapper.class);
        when(mapper.selectList(any())).thenReturn(Arrays.asList(
                row("2026-07-02", 5, 4, 1),
                row("2026-07-02", 7, 3, 2)));
        CustomerStatisticService service = new CustomerStatisticService(mapper);

        List<CustomerTrendResponse> result = service.query(
                "ww-corp", CustomerStatisticService.NET_INCREASE, Arrays.asList("staff-a", "staff-b"),
                LocalDate.parse("2026-07-02"), LocalDate.parse("2026-07-02"));

        assertThat(result).extracting(CustomerTrendResponse::getNumber).containsExactly(4L);
    }

    private CustomerStatisticPo row(String date, long total, long increase, long decrease) {
        CustomerStatisticPo row = new CustomerStatisticPo();
        row.setExtCorpId("ww-corp");
        row.setDate(LocalDate.parse(date));
        row.setTotalCustomerNum(total);
        row.setIncreaseCustomerNum(increase);
        row.setDecreaseCustomerNum(decrease);
        return row;
    }
}
