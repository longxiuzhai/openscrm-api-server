package cn.openscrm.api.internaltag.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cn.openscrm.api.common.api.PageResponse;
import cn.openscrm.api.common.id.SnowflakeIdGenerator;
import cn.openscrm.api.internaltag.dto.InternalTagCreateRequest;
import cn.openscrm.api.internaltag.dto.InternalTagDeleteRequest;
import cn.openscrm.api.persistence.entity.InternalTagPo;
import cn.openscrm.api.persistence.mapper.InternalTagPoMapper;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class InternalTagServiceTest {

    @Test
    void createInsertsNonBlankNames() {
        InternalTagPoMapper mapper = mock(InternalTagPoMapper.class);
        InternalTagService service = service(mapper);
        InternalTagCreateRequest request = new InternalTagCreateRequest();
        request.setNames(Arrays.asList("重点", " ", "高意向"));

        List<InternalTagPo> tags = service.create(request, "ww-corp", "admin-a");

        assertThat(tags).hasSize(2);
        assertThat(tags).extracting(InternalTagPo::getName).containsExactly("重点", "高意向");
        assertThat(tags).allSatisfy(tag -> {
            assertThat(tag.getExtCorpId()).isEqualTo("ww-corp");
            assertThat(tag.getExtCreatorId()).isEqualTo("admin-a");
        });
        verify(mapper).insert(tags.get(0));
        verify(mapper).insert(tags.get(1));
    }

    @Test
    void queryWrapsPagedResult() {
        InternalTagPoMapper mapper = mock(InternalTagPoMapper.class);
        IPage<InternalTagPo> page = Page.of(2, 10, 1);
        InternalTagPo tag = new InternalTagPo();
        tag.setName("重点");
        page.setRecords(Arrays.asList(tag));
        when(mapper.selectPage(any(), any())).thenReturn(page);
        InternalTagService service = service(mapper);

        PageResponse<InternalTagPo> response = service.query("ww-corp", 2, 10);

        assertThat(response.getItems()).containsExactly(tag);
        assertThat(response.getTotalRows()).isEqualTo(1L);
        assertThat(response.getPage()).isEqualTo(2L);
        assertThat(response.getPageSize()).isEqualTo(10L);
    }

    @Test
    void deleteSoftDeletesByIdsAndCorp() {
        InternalTagPoMapper mapper = mock(InternalTagPoMapper.class);
        when(mapper.update(any(), any())).thenReturn(2);
        InternalTagService service = service(mapper);
        InternalTagDeleteRequest request = new InternalTagDeleteRequest();
        request.setIds(Arrays.asList(1L, 2L));

        long rows = service.delete(request, "ww-corp");

        assertThat(rows).isEqualTo(2L);
        @SuppressWarnings({"unchecked", "rawtypes"})
        ArgumentCaptor<Wrapper<InternalTagPo>> wrapperCaptor = ArgumentCaptor.forClass((Class) Wrapper.class);
        verify(mapper).update(any(), wrapperCaptor.capture());
        assertThat(wrapperCaptor.getValue()).isNotNull();
    }

    private InternalTagService service(InternalTagPoMapper mapper) {
        SnowflakeIdGenerator idGenerator = mock(SnowflakeIdGenerator.class);
        when(idGenerator.nextId()).thenReturn(10001L, 10002L, 10003L);
        return new InternalTagService(mapper, idGenerator);
    }
}
