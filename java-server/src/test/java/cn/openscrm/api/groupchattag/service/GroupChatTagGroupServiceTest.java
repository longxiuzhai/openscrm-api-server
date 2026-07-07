package cn.openscrm.api.groupchattag.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cn.openscrm.api.common.id.SnowflakeIdGenerator;
import cn.openscrm.api.groupchattag.dto.GroupChatTagGroupCreateRequest;
import cn.openscrm.api.groupchattag.dto.GroupChatTagGroupResponse;
import cn.openscrm.api.groupchattag.dto.GroupChatTagGroupUpdateRequest;
import cn.openscrm.api.groupchattag.dto.GroupChatTagItemRequest;
import cn.openscrm.api.persistence.entity.GroupChatTagGroupPo;
import cn.openscrm.api.persistence.entity.GroupChatTagPo;
import cn.openscrm.api.persistence.mapper.GroupChatTagGroupPoMapper;
import cn.openscrm.api.persistence.mapper.GroupChatTagPoMapper;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Test;

class GroupChatTagGroupServiceTest {

    @Test
    void createInsertsGroupAndInlineTags() {
        Fixtures fixtures = fixtures();
        GroupChatTagGroupCreateRequest request = new GroupChatTagGroupCreateRequest();
        request.setName("群阶段");
        GroupChatTagItemRequest tag = new GroupChatTagItemRequest();
        tag.setName("高活跃");
        request.setTags(Collections.singletonList(tag));

        GroupChatTagGroupResponse response = fixtures.service.create(request, "ww-corp", "admin-a");

        assertThat(response.getId()).isEqualTo(10001L);
        assertThat(response.getTags()).hasSize(1);
        verify(fixtures.groupMapper).insert(any(GroupChatTagGroupPo.class));
        verify(fixtures.tagMapper).insert(any(GroupChatTagPo.class));
    }

    @Test
    void updateCreatesUpdatesAndDeletesInlineTags() {
        Fixtures fixtures = fixtures();
        when(fixtures.groupMapper.selectById(10001L)).thenReturn(group(10001L, "群阶段"));
        when(fixtures.tagMapper.selectList(any())).thenReturn(Collections.singletonList(tag(10002L, "10001", "新标签")));
        GroupChatTagGroupUpdateRequest request = new GroupChatTagGroupUpdateRequest();
        request.setId(10001L);
        request.setName("群阶段");
        request.setDeleteTagIds(Collections.singletonList(10004L));
        GroupChatTagItemRequest newTag = new GroupChatTagItemRequest();
        newTag.setName("新标签");
        GroupChatTagItemRequest updateTag = new GroupChatTagItemRequest();
        updateTag.setId(10003L);
        updateTag.setName("改名");
        request.setTags(Arrays.asList(newTag, updateTag));

        GroupChatTagGroupResponse response = fixtures.service.update(request, "ww-corp");

        assertThat(response.getId()).isEqualTo(10001L);
        verify(fixtures.groupMapper).update(any(), any());
        verify(fixtures.tagMapper).insert(any(GroupChatTagPo.class));
        verify(fixtures.tagMapper).update(any(), any());
        verify(fixtures.tagMapper).deleteBatchIds(Collections.singletonList(10004L));
    }

    @Test
    void queryHydratesTags() {
        Fixtures fixtures = fixtures();
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<GroupChatTagGroupPo> page =
                com.baomidou.mybatisplus.extension.plugins.pagination.Page.of(1, 20, 1);
        page.setRecords(Collections.singletonList(group(10001L, "群阶段")));
        when(fixtures.groupMapper.selectPage(any(), any())).thenReturn(page);
        when(fixtures.tagMapper.selectList(any())).thenReturn(Collections.singletonList(tag(10002L, "10001", "高活跃")));

        cn.openscrm.api.common.api.PageResponse<GroupChatTagGroupResponse> response =
                fixtures.service.query("ww-corp", "群", 1, 20);

        assertThat(response.getTotalRows()).isEqualTo(1L);
        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getItems().get(0).getTags()).hasSize(1);
    }

    private Fixtures fixtures() {
        GroupChatTagGroupPoMapper groupMapper = mock(GroupChatTagGroupPoMapper.class);
        GroupChatTagPoMapper tagMapper = mock(GroupChatTagPoMapper.class);
        SnowflakeIdGenerator idGenerator = mock(SnowflakeIdGenerator.class);
        when(idGenerator.nextId()).thenReturn(10001L, 10002L, 10003L);
        GroupChatTagService tagService = new GroupChatTagService(tagMapper, idGenerator);
        GroupChatTagGroupService service = new GroupChatTagGroupService(groupMapper, tagMapper, tagService, idGenerator);
        return new Fixtures(service, groupMapper, tagMapper);
    }

    private GroupChatTagGroupPo group(Long id, String name) {
        GroupChatTagGroupPo group = new GroupChatTagGroupPo();
        group.setId(id);
        group.setExtCorpId("ww-corp");
        group.setName(name);
        return group;
    }

    private GroupChatTagPo tag(Long id, String groupId, String name) {
        GroupChatTagPo tag = new GroupChatTagPo();
        tag.setId(id);
        tag.setGroupChatTagGroupId(groupId);
        tag.setName(name);
        return tag;
    }

    private static class Fixtures {
        private final GroupChatTagGroupService service;
        private final GroupChatTagGroupPoMapper groupMapper;
        private final GroupChatTagPoMapper tagMapper;

        private Fixtures(GroupChatTagGroupService service,
                         GroupChatTagGroupPoMapper groupMapper,
                         GroupChatTagPoMapper tagMapper) {
            this.service = service;
            this.groupMapper = groupMapper;
            this.tagMapper = tagMapper;
        }
    }
}
