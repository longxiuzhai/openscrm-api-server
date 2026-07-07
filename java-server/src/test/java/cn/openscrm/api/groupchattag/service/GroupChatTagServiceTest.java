package cn.openscrm.api.groupchattag.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cn.openscrm.api.common.id.SnowflakeIdGenerator;
import cn.openscrm.api.groupchattag.dto.CommonIdsRequest;
import cn.openscrm.api.groupchattag.dto.GroupChatTagCreateRequest;
import cn.openscrm.api.groupchattag.dto.GroupChatTagUpdateRequest;
import cn.openscrm.api.persistence.entity.GroupChatTagPo;
import cn.openscrm.api.persistence.mapper.GroupChatTagPoMapper;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class GroupChatTagServiceTest {

    @Test
    void createInsertsNonBlankNames() {
        GroupChatTagPoMapper mapper = mock(GroupChatTagPoMapper.class);
        GroupChatTagService service = service(mapper);
        GroupChatTagCreateRequest request = new GroupChatTagCreateRequest();
        request.setGroupId(99L);
        request.setNames(Arrays.asList("重点群", " ", "新客群"));

        java.util.List<GroupChatTagPo> tags = service.create(request, "ww-corp", "admin-a");

        assertThat(tags).hasSize(2);
        assertThat(tags).extracting(GroupChatTagPo::getName).containsExactly("重点群", "新客群");
        assertThat(tags).allSatisfy(tag -> {
            assertThat(tag.getExtCorpId()).isEqualTo("ww-corp");
            assertThat(tag.getExtCreatorId()).isEqualTo("admin-a");
            assertThat(tag.getGroupChatTagGroupId()).isEqualTo("99");
        });
        verify(mapper).insert(tags.get(0));
        verify(mapper).insert(tags.get(1));
    }

    @Test
    void updateUsesCorpScopedWrapperAndReturnsStoredTag() {
        GroupChatTagPoMapper mapper = mock(GroupChatTagPoMapper.class);
        GroupChatTagPo stored = new GroupChatTagPo();
        stored.setId(10001L);
        stored.setName("改名");
        when(mapper.selectById(10001L)).thenReturn(stored);
        GroupChatTagService service = service(mapper);
        GroupChatTagUpdateRequest request = new GroupChatTagUpdateRequest();
        request.setId(10001L);
        request.setName("改名");

        GroupChatTagPo tag = service.update(request, "ww-corp");

        assertThat(tag).isSameAs(stored);
        @SuppressWarnings({"unchecked", "rawtypes"})
        ArgumentCaptor<Wrapper<GroupChatTagPo>> wrapperCaptor = ArgumentCaptor.forClass((Class) Wrapper.class);
        verify(mapper).update(any(), wrapperCaptor.capture());
        assertThat(wrapperCaptor.getValue()).isNotNull();
    }

    @Test
    void deleteUsesBatchIds() {
        GroupChatTagPoMapper mapper = mock(GroupChatTagPoMapper.class);
        when(mapper.deleteBatchIds(Arrays.asList(1L, 2L))).thenReturn(2);
        GroupChatTagService service = service(mapper);
        CommonIdsRequest request = new CommonIdsRequest();
        request.setIds(Arrays.asList(1L, 2L));

        long rows = service.delete(request);

        assertThat(rows).isEqualTo(2L);
        verify(mapper).deleteBatchIds(Arrays.asList(1L, 2L));
    }

    private GroupChatTagService service(GroupChatTagPoMapper mapper) {
        SnowflakeIdGenerator idGenerator = mock(SnowflakeIdGenerator.class);
        when(idGenerator.nextId()).thenReturn(10001L, 10002L, 10003L);
        return new GroupChatTagService(mapper, idGenerator);
    }
}
