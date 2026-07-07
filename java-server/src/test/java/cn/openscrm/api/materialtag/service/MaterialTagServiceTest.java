package cn.openscrm.api.materialtag.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cn.openscrm.api.common.api.PageResponse;
import cn.openscrm.api.common.id.SnowflakeIdGenerator;
import cn.openscrm.api.materialtag.dto.MaterialTagCreateRequest;
import cn.openscrm.api.materialtag.dto.MaterialTagDeleteRequest;
import cn.openscrm.api.persistence.entity.MaterialLibTagPo;
import cn.openscrm.api.persistence.mapper.MaterialLibTagPoMapper;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class MaterialTagServiceTest {

    @Test
    void createInsertsNonBlankNames() {
        MaterialLibTagPoMapper mapper = mock(MaterialLibTagPoMapper.class);
        MaterialTagService service = service(mapper);
        MaterialTagCreateRequest request = new MaterialTagCreateRequest();
        request.setNames(Arrays.asList("海报", " ", "案例"));

        java.util.List<MaterialLibTagPo> tags = service.create(request, "ww-corp", "admin-a");

        assertThat(tags).hasSize(2);
        assertThat(tags).extracting(MaterialLibTagPo::getName).containsExactly("海报", "案例");
        verify(mapper).insert(tags.get(0));
        verify(mapper).insert(tags.get(1));
    }

    @Test
    void queryWrapsPagedResult() {
        MaterialLibTagPoMapper mapper = mock(MaterialLibTagPoMapper.class);
        IPage<MaterialLibTagPo> page = Page.of(1, 20, 1);
        MaterialLibTagPo tag = new MaterialLibTagPo();
        tag.setName("海报");
        page.setRecords(Collections.singletonList(tag));
        when(mapper.selectPage(any(), any())).thenReturn(page);
        MaterialTagService service = service(mapper);

        PageResponse<MaterialLibTagPo> response = service.query("ww-corp", "海", 1, 20);

        assertThat(response.getItems()).containsExactly(tag);
        assertThat(response.getTotalRows()).isEqualTo(1L);
    }

    @Test
    void deleteSoftDeletesByIds() {
        MaterialLibTagPoMapper mapper = mock(MaterialLibTagPoMapper.class);
        when(mapper.update(any(), any())).thenReturn(2);
        MaterialTagService service = service(mapper);
        MaterialTagDeleteRequest request = new MaterialTagDeleteRequest();
        request.setIds(Arrays.asList(1L, 2L));

        long rows = service.delete(request);

        assertThat(rows).isEqualTo(2L);
        @SuppressWarnings({"unchecked", "rawtypes"})
        ArgumentCaptor<Wrapper<MaterialLibTagPo>> wrapperCaptor = ArgumentCaptor.forClass((Class) Wrapper.class);
        verify(mapper).update(any(), wrapperCaptor.capture());
        assertThat(wrapperCaptor.getValue()).isNotNull();
    }

    private MaterialTagService service(MaterialLibTagPoMapper mapper) {
        SnowflakeIdGenerator idGenerator = mock(SnowflakeIdGenerator.class);
        when(idGenerator.nextId()).thenReturn(10001L, 10002L, 10003L);
        return new MaterialTagService(mapper, idGenerator);
    }
}
