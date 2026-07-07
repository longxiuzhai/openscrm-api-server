package cn.openscrm.api.deletenotify.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cn.openscrm.api.common.api.PageResponse;
import cn.openscrm.api.deletenotify.dto.DeleteCustomerNotifyRuleRequest;
import cn.openscrm.api.deletenotify.dto.StaffDeleteCustomerResponse;
import cn.openscrm.api.persistence.entity.EventNotifyPo;
import cn.openscrm.api.persistence.mapper.EventNotifyPoMapper;
import cn.openscrm.api.persistence.mapper.StaffDeleteCustomerQueryMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Test;

class DeleteCustomerNotifyServiceTest {

    @Test
    void getRuleReturnsDefaultClosedWhenMissing() {
        EventNotifyPoMapper eventNotifyMapper = mock(EventNotifyPoMapper.class);
        StaffDeleteCustomerQueryMapper queryMapper = mock(StaffDeleteCustomerQueryMapper.class);
        when(eventNotifyMapper.selectOne(any())).thenReturn(null);
        DeleteCustomerNotifyService service = new DeleteCustomerNotifyService(
                eventNotifyMapper, queryMapper, new ObjectMapper());

        EventNotifyPo rule = service.getRule("ww-corp");

        assertThat(rule.getExtCorpId()).isEqualTo("ww-corp");
        assertThat(rule.getIsNotifyAdmins()).isEqualTo(2);
        assertThat(rule.getIsNotifyStaff()).isEqualTo(2);
        assertThat(rule.getNotifyType()).isEqualTo(1);
        assertThat(rule.getExtStaffIds()).isEqualTo("[]");
    }

    @Test
    void upsertRuleWritesAdminAndStaffFlagsForCallbackCompatibility() {
        EventNotifyPoMapper eventNotifyMapper = mock(EventNotifyPoMapper.class);
        StaffDeleteCustomerQueryMapper queryMapper = mock(StaffDeleteCustomerQueryMapper.class);
        when(eventNotifyMapper.selectOne(any())).thenReturn(null);
        DeleteCustomerNotifyService service = new DeleteCustomerNotifyService(
                eventNotifyMapper, queryMapper, new ObjectMapper());
        DeleteCustomerNotifyRuleRequest request = new DeleteCustomerNotifyRuleRequest();
        request.setIsNotifyStaff(1);
        request.setNotifyType(2);
        request.setExtStaffIds(Arrays.asList("admin-a", "admin-b"));

        service.upsertRule("ww-corp", request);

        verify(eventNotifyMapper).insert(any(EventNotifyPo.class));
    }

    @Test
    void queryRecordsReturnsEmptyPageWhenNoRows() {
        EventNotifyPoMapper eventNotifyMapper = mock(EventNotifyPoMapper.class);
        StaffDeleteCustomerQueryMapper queryMapper = mock(StaffDeleteCustomerQueryMapper.class);
        when(queryMapper.countStaffDeleteCustomers(eq("ww-corp"), any(), any(), any(), any(), any(), any()))
                .thenReturn(0L);
        DeleteCustomerNotifyService service = new DeleteCustomerNotifyService(
                eventNotifyMapper, queryMapper, new ObjectMapper());

        PageResponse<StaffDeleteCustomerResponse> response = service.queryRecords(
                "ww-corp", null, Collections.emptyList(), null, null, null, null,
                null, null, 1, 20);

        assertThat(response.getItems()).isEmpty();
        assertThat(response.getTotalRows()).isZero();
        verify(queryMapper, never()).selectStaffDeleteCustomers(any(), any(), any(), any(), any(), any(), any(), any(), any(Long.class), any(Long.class));
    }

    @Test
    void queryRecordsWhitelistsSortFieldAndDirection() {
        EventNotifyPoMapper eventNotifyMapper = mock(EventNotifyPoMapper.class);
        StaffDeleteCustomerQueryMapper queryMapper = mock(StaffDeleteCustomerQueryMapper.class);
        when(queryMapper.countStaffDeleteCustomers(eq("ww-corp"), any(), any(), any(), any(), any(), any()))
                .thenReturn(1L);
        when(queryMapper.selectStaffDeleteCustomers(any(), any(), any(), any(), any(), any(), any(), any(), any(Long.class), any(Long.class)))
                .thenReturn(Collections.emptyList());
        DeleteCustomerNotifyService service = new DeleteCustomerNotifyService(
                eventNotifyMapper, queryMapper, new ObjectMapper());

        service.queryRecords("ww-corp", 1L, Arrays.asList("staff-a"),
                LocalDate.parse("2026-07-01"), LocalDate.parse("2026-07-05"),
                null, null, "staff_name", "asc", 2, 10);

        verify(queryMapper).selectStaffDeleteCustomers(eq("ww-corp"), eq(1L), any(), any(), any(), any(), any(),
                eq("staff.name asc"), eq(10L), eq(10L));
    }

    @Test
    void exportXlsxReturnsWorkbookBytes() {
        EventNotifyPoMapper eventNotifyMapper = mock(EventNotifyPoMapper.class);
        StaffDeleteCustomerQueryMapper queryMapper = mock(StaffDeleteCustomerQueryMapper.class);
        StaffDeleteCustomerResponse item = new StaffDeleteCustomerResponse();
        item.setExtCustomerName("客户A");
        item.setStaffName("员工A");
        item.setRelationDeleteAt(LocalDateTime.parse("2026-07-05T10:00:00"));
        item.setRelationCreateAt(LocalDateTime.parse("2026-07-01T09:00:00"));
        when(queryMapper.countStaffDeleteCustomers(eq("ww-corp"), any(), any(), any(), any(), any(), any()))
                .thenReturn(1L);
        when(queryMapper.selectStaffDeleteCustomers(any(), any(), any(), any(), any(), any(), any(), any(), any(Long.class), any(Long.class)))
                .thenReturn(Collections.singletonList(item));
        DeleteCustomerNotifyService service = new DeleteCustomerNotifyService(
                eventNotifyMapper, queryMapper, new ObjectMapper());

        byte[] bytes = service.exportXlsx("ww-corp", null, null, null, null, null, null, null, null);

        assertThat(bytes).isNotEmpty();
        assertThat(bytes[0]).isEqualTo((byte) 'P');
        assertThat(bytes[1]).isEqualTo((byte) 'K');
    }
}
