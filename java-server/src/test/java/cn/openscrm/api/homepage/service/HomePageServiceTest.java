package cn.openscrm.api.homepage.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import cn.openscrm.api.homepage.dto.CustomerSummaryResponse;
import cn.openscrm.api.persistence.mapper.CustomerPoMapper;
import cn.openscrm.api.persistence.mapper.CustomerStaffRelationHistoryPoMapper;
import cn.openscrm.api.persistence.mapper.GroupChatPoMapper;
import cn.openscrm.api.persistence.mapper.StaffPoMapper;
import java.math.BigDecimal;
import java.util.Collections;
import org.junit.jupiter.api.Test;

class HomePageServiceTest {

    @Test
    void summaryAggregatesCountsAndGroupJoinQuitTotals() {
        StaffPoMapper staffMapper = mock(StaffPoMapper.class);
        CustomerPoMapper customerMapper = mock(CustomerPoMapper.class);
        CustomerStaffRelationHistoryPoMapper historyMapper = mock(CustomerStaffRelationHistoryPoMapper.class);
        GroupChatPoMapper groupChatMapper = mock(GroupChatPoMapper.class);
        when(staffMapper.selectCount(any())).thenReturn(3L);
        when(customerMapper.selectCount(any())).thenReturn(100L, 5L);
        when(historyMapper.selectCount(any())).thenReturn(2L);
        when(groupChatMapper.selectCount(any())).thenReturn(8L);
        when(groupChatMapper.selectObjs(any()))
                .thenReturn(Collections.singletonList(new BigDecimal("12")))
                .thenReturn(Collections.singletonList(new BigDecimal("4")));
        HomePageService service = new HomePageService(staffMapper, customerMapper, historyMapper, groupChatMapper);

        CustomerSummaryResponse response = service.summary("ww-corp");

        assertThat(response.getTotalStaffsNum()).isEqualTo(3);
        assertThat(response.getTotalCustomersNum()).isEqualTo(100);
        assertThat(response.getTodayCustomersIncrease()).isEqualTo(5);
        assertThat(response.getTodayCustomersDecrease()).isEqualTo(2);
        assertThat(response.getTotalGroupsNum()).isEqualTo(8);
        assertThat(response.getTodayGroupsIncrease()).isEqualTo(12);
        assertThat(response.getTodayGroupsDecrease()).isEqualTo(4);
    }
}
