package cn.openscrm.api.customerconfig.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cn.openscrm.api.common.api.PageResponse;
import cn.openscrm.api.persistence.entity.CustomerEventPo;
import cn.openscrm.api.persistence.mapper.CustomerEventPoMapper;
import java.util.Collections;
import org.junit.jupiter.api.Test;

class CustomerEventQueryServiceTest {

    @Test
    void queryReturnsEmptyPageWhenNoRows() {
        CustomerEventPoMapper mapper = mock(CustomerEventPoMapper.class);
        when(mapper.selectCount(any())).thenReturn(0L);
        CustomerEventQueryService service = new CustomerEventQueryService(mapper);

        PageResponse<CustomerEventPo> response = service.query(
                "ww-corp", "staff-a", "customer-a", null, null, null, 1, 20);

        assertThat(response.getItems()).isEmpty();
        assertThat(response.getTotalRows()).isZero();
        verify(mapper, never()).selectList(any());
    }

    @Test
    void queryFetchesRowsWhenCountExists() {
        CustomerEventPoMapper mapper = mock(CustomerEventPoMapper.class);
        CustomerEventPo item = new CustomerEventPo();
        item.setEventType("manual_event");
        when(mapper.selectCount(any())).thenReturn(1L);
        when(mapper.selectList(any())).thenReturn(Collections.singletonList(item));
        CustomerEventQueryService service = new CustomerEventQueryService(mapper);

        PageResponse<CustomerEventPo> response = service.query(
                "ww-corp", "staff-a", "customer-a", "manual_event", "created_at", "desc", 2, 10);

        assertThat(response.getItems()).containsExactly(item);
        assertThat(response.getTotalRows()).isEqualTo(1L);
    }
}
