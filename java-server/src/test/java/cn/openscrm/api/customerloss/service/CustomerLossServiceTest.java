package cn.openscrm.api.customerloss.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cn.openscrm.api.common.api.PageResponse;
import cn.openscrm.api.customerloss.dto.CustomerLossResponse;
import cn.openscrm.api.persistence.entity.CustomerStaffTagPo;
import cn.openscrm.api.persistence.mapper.CustomerLossQueryMapper;
import cn.openscrm.api.persistence.mapper.CustomerStaffTagPoMapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Test;

class CustomerLossServiceTest {

    @Test
    void queryReturnsEmptyPageWhenNoRows() {
        CustomerLossQueryMapper lossMapper = mock(CustomerLossQueryMapper.class);
        CustomerStaffTagPoMapper tagMapper = mock(CustomerStaffTagPoMapper.class);
        when(lossMapper.countLosses(eq("ww-corp"), any(), any(), any(), any(), any(), any(), any())).thenReturn(0L);
        CustomerLossService service = new CustomerLossService(lossMapper, tagMapper);

        PageResponse<CustomerLossResponse> response = service.query(
                "ww-corp", Collections.emptyList(), null, null, null, null,
                null, null, null, null, 1, 20);

        assertThat(response.getItems()).isEmpty();
        assertThat(response.getTotalRows()).isZero();
        verify(lossMapper, never()).selectLosses(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(Long.class), any(Long.class));
        verify(tagMapper, never()).selectList(any());
    }

    @Test
    void queryHydratesTagsByCustomerStaffId() {
        CustomerLossQueryMapper lossMapper = mock(CustomerLossQueryMapper.class);
        CustomerStaffTagPoMapper tagMapper = mock(CustomerStaffTagPoMapper.class);
        CustomerLossResponse item = new CustomerLossResponse();
        item.setId(10001L);
        item.setExtCustomerId("customer-a");
        when(lossMapper.countLosses(eq("ww-corp"), any(), any(), any(), any(), any(), any(), any())).thenReturn(1L);
        when(lossMapper.selectLosses(eq("ww-corp"), any(), any(), any(), any(), any(), any(), any(),
                eq("customer_staff_relation_history.customer_delete_staff_at desc"), eq(20L), eq(0L)))
                .thenReturn(Collections.singletonList(item));
        CustomerStaffTagPo tag = new CustomerStaffTagPo();
        tag.setCustomerStaffId(10001L);
        tag.setTagName("高意向");
        when(tagMapper.selectList(any())).thenReturn(Collections.singletonList(tag));
        CustomerLossService service = new CustomerLossService(lossMapper, tagMapper);

        PageResponse<CustomerLossResponse> response = service.query(
                "ww-corp", Arrays.asList("staff-a"),
                LocalDate.parse("2026-07-01"), LocalDate.parse("2026-07-05"),
                null, null, 1L, 10L, null, null, 1, 20);

        assertThat(response.getTotalRows()).isEqualTo(1L);
        assertThat(response.getItems()).containsExactly(item);
        assertThat(response.getItems().get(0).getTags()).containsExactly(tag);
    }

    @Test
    void queryWhitelistsSortFieldAndDirection() {
        CustomerLossQueryMapper lossMapper = mock(CustomerLossQueryMapper.class);
        CustomerStaffTagPoMapper tagMapper = mock(CustomerStaffTagPoMapper.class);
        when(lossMapper.countLosses(eq("ww-corp"), any(), any(), any(), any(), any(), any(), any())).thenReturn(1L);
        when(lossMapper.selectLosses(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(Long.class), any(Long.class)))
                .thenReturn(Collections.emptyList());
        CustomerLossService service = new CustomerLossService(lossMapper, tagMapper);

        service.query("ww-corp", null, null, null, null, null,
                null, null, "staff_name", "asc", 2, 10);

        verify(lossMapper).selectLosses(eq("ww-corp"), any(), any(), any(), any(), any(), any(), any(),
                eq("s.name asc"), eq(10L), eq(10L));
    }

    @Test
    void exportXlsxReturnsWorkbookBytes() {
        CustomerLossQueryMapper lossMapper = mock(CustomerLossQueryMapper.class);
        CustomerStaffTagPoMapper tagMapper = mock(CustomerStaffTagPoMapper.class);
        CustomerLossResponse item = new CustomerLossResponse();
        item.setId(10001L);
        item.setExtCustomerName("张三");
        item.setStaffName("客服A");
        item.setCustomerDeleteStaffAt(LocalDateTime.parse("2026-07-05T10:00:00"));
        item.setRelationCreateAt(LocalDateTime.parse("2026-07-01T09:00:00"));
        item.setInConnectionTimeRange(4L);
        when(lossMapper.countLosses(eq("ww-corp"), any(), any(), any(), any(), any(), any(), any())).thenReturn(1L);
        when(lossMapper.selectLosses(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(Long.class), any(Long.class)))
                .thenReturn(Collections.singletonList(item));
        CustomerStaffTagPo tag = new CustomerStaffTagPo();
        tag.setCustomerStaffId(10001L);
        tag.setTagName("高意向");
        when(tagMapper.selectList(any())).thenReturn(Collections.singletonList(tag));
        CustomerLossService service = new CustomerLossService(lossMapper, tagMapper);

        byte[] bytes = service.exportXlsx("ww-corp", null, null, null, null, null, null, null, null, null);

        assertThat(bytes).isNotEmpty();
        assertThat(bytes[0]).isEqualTo((byte) 'P');
        assertThat(bytes[1]).isEqualTo((byte) 'K');
    }
}
