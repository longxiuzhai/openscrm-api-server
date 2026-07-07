package cn.openscrm.api.taggroup.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cn.openscrm.api.common.id.SnowflakeIdGenerator;
import cn.openscrm.api.config.OpenScrmProperties;
import cn.openscrm.api.persistence.entity.TagGroupPo;
import cn.openscrm.api.persistence.entity.TagPo;
import cn.openscrm.api.persistence.mapper.TagGroupPoMapper;
import cn.openscrm.api.persistence.mapper.TagPoMapper;
import cn.openscrm.api.tag.service.TagSyncService;
import cn.openscrm.api.taggroup.dto.TagGroupCreateRequest;
import cn.openscrm.api.taggroup.dto.TagGroupDeleteRequest;
import cn.openscrm.api.taggroup.dto.TagGroupExchangeOrderRequest;
import cn.openscrm.api.taggroup.dto.TagGroupResponse;
import cn.openscrm.api.taggroup.dto.TagGroupTagRequest;
import cn.openscrm.api.taggroup.dto.TagGroupUpdateRequest;
import cn.openscrm.api.wework.ExternalContactCorpTag;
import cn.openscrm.api.wework.ExternalContactCorpTagGroup;
import cn.openscrm.api.wework.WeWorkClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class TagGroupServiceTest {

    @Test
    void createCallsWeWorkAndPersistsReturnedGroupAndTags() {
        Fixtures fixtures = fixtures();
        when(fixtures.groupMapper.selectCount(any())).thenReturn(0L);
        when(fixtures.idGenerator.nextId()).thenReturn(10001L, 10002L);
        ExternalContactCorpTagGroup corpGroup = corpGroup("etg-a", "成交阶段", "tag-a");
        when(fixtures.weWorkClient.addExternalContactCorpTag(eq("ww-corp"), eq("customer-secret"), any()))
                .thenReturn(corpGroup);
        TagGroupPo storedGroup = groupPo(10001L, "etg-a", "成交阶段", 12);
        when(fixtures.groupMapper.selectOne(any())).thenReturn(null, storedGroup);
        when(fixtures.groupMapper.selectById(10001L)).thenReturn(null);
        when(fixtures.tagMapper.selectOne(any())).thenReturn(null);
        when(fixtures.tagMapper.selectById(10002L)).thenReturn(null);
        when(fixtures.tagMapper.selectList(any())).thenReturn(Collections.singletonList(tagPo(10002L, "tag-a", "etg-a", "已成交", 10000)));

        TagGroupCreateRequest request = new TagGroupCreateRequest();
        request.setName("成交阶段");
        request.setDepartmentList(Arrays.asList(2L, 3L));
        TagGroupTagRequest tag = new TagGroupTagRequest();
        tag.setName("已成交");
        request.setTags(Collections.singletonList(tag));

        TagGroupResponse response = fixtures.service.create(request, "ww-corp");

        assertThat(response.getExtId()).isEqualTo("etg-a");
        assertThat(response.getTags()).hasSize(1);
        ArgumentCaptor<ExternalContactCorpTagGroup> requestCaptor = ArgumentCaptor.forClass(ExternalContactCorpTagGroup.class);
        verify(fixtures.weWorkClient).addExternalContactCorpTag(eq("ww-corp"), eq("customer-secret"), requestCaptor.capture());
        assertThat(requestCaptor.getValue().getGroupName()).isEqualTo("成交阶段");
        assertThat(requestCaptor.getValue().getTag()).hasSize(1);
        verify(fixtures.groupMapper).insert(any(TagGroupPo.class));
        verify(fixtures.tagMapper).insert(any(TagPo.class));
    }

    @Test
    void updateHandlesExistingNewAndRemovedTags() {
        Fixtures fixtures = fixtures();
        TagPo existing = tagPo(20001L, "tag-old", "etg-a", "原名称", 9);
        when(fixtures.tagMapper.selectOne(any())).thenReturn(existing, null);
        when(fixtures.tagMapper.selectById(10002L)).thenReturn(null);
        when(fixtures.idGenerator.nextId()).thenReturn(10002L);
        when(fixtures.groupMapper.selectOne(any())).thenReturn(groupPo(10001L, "etg-a", "旧组", 1), groupPo(10001L, "etg-a", "新组", 30));
        when(fixtures.groupMapper.selectById(10001L)).thenReturn(groupPo(10001L, "etg-a", "旧组", 1));
        when(fixtures.tagMapper.selectList(any())).thenReturn(Collections.singletonList(existing));
        when(fixtures.weWorkClient.addExternalContactCorpTag(eq("ww-corp"), eq("customer-secret"), any()))
                .thenReturn(corpGroup("etg-a", "新组", "tag-new"));

        TagGroupUpdateRequest request = new TagGroupUpdateRequest();
        request.setName("新组");
        request.setOrder(30);
        request.setRemoveExtTagIds(Collections.singletonList("tag-remove"));
        TagGroupTagRequest updateTag = new TagGroupTagRequest();
        updateTag.setExtId("tag-old");
        updateTag.setName("新名称");
        TagGroupTagRequest newTag = new TagGroupTagRequest();
        newTag.setName("新标签");
        request.setTags(Arrays.asList(updateTag, newTag));

        fixtures.service.update("etg-a", request, "ww-corp");

        verify(fixtures.weWorkClient).editExternalContactCorpTag("ww-corp", "customer-secret", "tag-old", "新名称", 10000);
        verify(fixtures.weWorkClient).editExternalContactCorpTag("ww-corp", "customer-secret", "etg-a", "新组", 30);
        verify(fixtures.weWorkClient).deleteExternalContactCorpTag(
                "ww-corp", "customer-secret", Collections.singletonList("tag-remove"), Collections.emptyList());
        verify(fixtures.tagSyncService).deleteTag("tag-remove");
    }

    @Test
    void deleteRemovesRemoteGroupsAndSyncsLocalDelete() {
        Fixtures fixtures = fixtures();
        TagGroupDeleteRequest request = new TagGroupDeleteRequest();
        request.setExtIds(Arrays.asList("etg-a", "etg-b"));

        long rows = fixtures.service.delete(request, "ww-corp");

        assertThat(rows).isEqualTo(2L);
        verify(fixtures.weWorkClient).deleteExternalContactCorpTag(
                "ww-corp", "customer-secret", Collections.emptyList(), request.getExtIds());
        verify(fixtures.tagSyncService).deleteTagGroup("etg-a");
        verify(fixtures.tagSyncService).deleteTagGroup("etg-b");
    }

    @Test
    void exchangeOrderSwapsTwoLocalGroups() {
        Fixtures fixtures = fixtures();
        TagGroupPo first = groupPo(1L, "etg-a", "A", 10);
        TagGroupPo second = groupPo(2L, "etg-b", "B", 20);
        when(fixtures.groupMapper.selectById(1L)).thenReturn(first);
        when(fixtures.groupMapper.selectById(2L)).thenReturn(second);
        TagGroupExchangeOrderRequest request = new TagGroupExchangeOrderRequest();
        request.setId(1L);
        request.setExchangeOrderId(2L);

        fixtures.service.exchangeOrder(request);

        assertThat(first.getOrder()).isEqualTo(20);
        assertThat(second.getOrder()).isEqualTo(10);
        verify(fixtures.groupMapper).updateById(first);
        verify(fixtures.groupMapper).updateById(second);
    }

    private Fixtures fixtures() {
        TagGroupPoMapper groupMapper = mock(TagGroupPoMapper.class);
        TagPoMapper tagMapper = mock(TagPoMapper.class);
        WeWorkClient weWorkClient = mock(WeWorkClient.class);
        TagSyncService tagSyncService = mock(TagSyncService.class);
        SnowflakeIdGenerator idGenerator = mock(SnowflakeIdGenerator.class);
        OpenScrmProperties properties = new OpenScrmProperties();
        properties.getWeWork().setCustomerSecret("customer-secret");
        TagGroupService service = new TagGroupService(
                properties, weWorkClient, groupMapper, tagMapper, tagSyncService, idGenerator, new ObjectMapper());
        return new Fixtures(service, groupMapper, tagMapper, weWorkClient, tagSyncService, idGenerator);
    }

    private ExternalContactCorpTagGroup corpGroup(String extGroupId, String name, String extTagId) {
        ExternalContactCorpTag tag = new ExternalContactCorpTag();
        tag.setId(extTagId);
        tag.setName("已成交");
        tag.setOrder(10000);
        tag.setCreateTime(123);
        ExternalContactCorpTagGroup group = new ExternalContactCorpTagGroup();
        group.setGroupId(extGroupId);
        group.setGroupName(name);
        group.setOrder(12);
        group.setCreateTime(120);
        group.setTag(Collections.singletonList(tag));
        return group;
    }

    private TagGroupPo groupPo(Long id, String extId, String name, Integer order) {
        TagGroupPo group = new TagGroupPo();
        group.setId(id);
        group.setExtCorpId("ww-corp");
        group.setExtId(extId);
        group.setName(name);
        group.setOrder(order);
        group.setDepartmentList("[0]");
        return group;
    }

    private TagPo tagPo(Long id, String extId, String extGroupId, String name, Integer order) {
        TagPo tag = new TagPo();
        tag.setId(id);
        tag.setExtCorpId("ww-corp");
        tag.setExtId(extId);
        tag.setExtGroupId(extGroupId);
        tag.setName(name);
        tag.setGroupName("成交阶段");
        tag.setOrder(order);
        tag.setType(1);
        return tag;
    }

    private static class Fixtures {
        private final TagGroupService service;
        private final TagGroupPoMapper groupMapper;
        private final TagPoMapper tagMapper;
        private final WeWorkClient weWorkClient;
        private final TagSyncService tagSyncService;
        private final SnowflakeIdGenerator idGenerator;

        private Fixtures(TagGroupService service,
                         TagGroupPoMapper groupMapper,
                         TagPoMapper tagMapper,
                         WeWorkClient weWorkClient,
                         TagSyncService tagSyncService,
                         SnowflakeIdGenerator idGenerator) {
            this.service = service;
            this.groupMapper = groupMapper;
            this.tagMapper = tagMapper;
            this.weWorkClient = weWorkClient;
            this.tagSyncService = tagSyncService;
            this.idGenerator = idGenerator;
        }
    }
}
