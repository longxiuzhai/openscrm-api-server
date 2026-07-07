package cn.openscrm.api.cluemanual.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cn.openscrm.api.cluemanual.dto.ClueManualDeleteRequest;
import cn.openscrm.api.cluemanual.dto.ClueManualRequest;
import cn.openscrm.api.cluemanual.dto.ClueManualUpdateRequest;
import cn.openscrm.api.common.id.SnowflakeIdGenerator;
import cn.openscrm.api.persistence.entity.CustomerEventPo;
import cn.openscrm.api.persistence.mapper.CustomerEventPoMapper;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class ClueManualServiceTest {

    @Test
    void createStoresClueManualCustomerEvent() {
        CustomerEventPoMapper eventMapper = mock(CustomerEventPoMapper.class);
        ClueManualService service = service(eventMapper);
        ClueManualRequest request = new ClueManualRequest();
        request.setExtStaffId("staff-a");
        request.setExtCustomerId("customer-a");
        request.setContent("已电话沟通");

        CustomerEventPo event = service.create(request, "ww-corp", "admin-a");

        assertThat(event.getId()).isEqualTo(10001L);
        assertThat(event.getExtCorpId()).isEqualTo("ww-corp");
        assertThat(event.getExtCreatorId()).isEqualTo("admin-a");
        assertThat(event.getEventType()).isEqualTo("clue_manual_event");
        assertThat(event.getEventName()).isEqualTo("clue_manual_event");
        verify(eventMapper).insert(event);
    }

    @Test
    void updateChangesContentAndKeepsClueManualEventType() {
        CustomerEventPoMapper eventMapper = mock(CustomerEventPoMapper.class);
        ClueManualService service = service(eventMapper);
        CustomerEventPo existing = new CustomerEventPo();
        existing.setId(10001L);
        existing.setExtCorpId("ww-corp");
        existing.setContent("旧内容");
        when(eventMapper.selectById(10001L)).thenReturn(existing);
        ClueManualUpdateRequest request = new ClueManualUpdateRequest();
        request.setContent("新内容");

        CustomerEventPo event = service.update(10001L, request, "ww-corp");

        assertThat(event.getContent()).isEqualTo("新内容");
        assertThat(event.getEventType()).isEqualTo("clue_manual_event");
        assertThat(event.getEventName()).isEqualTo("clue_manual_event");
        verify(eventMapper).updateById(existing);
    }

    @Test
    void deleteSoftDeletesRequestedEvents() {
        CustomerEventPoMapper eventMapper = mock(CustomerEventPoMapper.class);
        when(eventMapper.update(any(), any())).thenReturn(2);
        ClueManualService service = service(eventMapper);
        ClueManualDeleteRequest request = new ClueManualDeleteRequest();
        request.setIds(Arrays.asList(1L, 2L));

        long rows = service.delete(request, "ww-corp");

        assertThat(rows).isEqualTo(2L);
        @SuppressWarnings({"unchecked", "rawtypes"})
        ArgumentCaptor<Wrapper<CustomerEventPo>> wrapperCaptor = ArgumentCaptor.forClass((Class) Wrapper.class);
        verify(eventMapper).update(any(), wrapperCaptor.capture());
        assertThat(wrapperCaptor.getValue()).isNotNull();
    }

    private ClueManualService service(CustomerEventPoMapper eventMapper) {
        SnowflakeIdGenerator idGenerator = mock(SnowflakeIdGenerator.class);
        when(idGenerator.nextId()).thenReturn(10001L);
        return new ClueManualService(eventMapper, idGenerator);
    }
}
