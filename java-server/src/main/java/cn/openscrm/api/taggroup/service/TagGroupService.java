package cn.openscrm.api.taggroup.service;

import cn.openscrm.api.common.api.PageResponse;
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
import cn.openscrm.api.taggroup.dto.TagResponse;
import cn.openscrm.api.wework.ExternalContactCorpTag;
import cn.openscrm.api.wework.ExternalContactCorpTagGroup;
import cn.openscrm.api.wework.WeWorkClient;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class TagGroupService {

    private static final TypeReference<List<Long>> LONG_LIST = new TypeReference<List<Long>>() {
    };

    private final OpenScrmProperties properties;
    private final WeWorkClient weWorkClient;
    private final TagGroupPoMapper tagGroupMapper;
    private final TagPoMapper tagMapper;
    private final TagSyncService tagSyncService;
    private final SnowflakeIdGenerator idGenerator;
    private final ObjectMapper objectMapper;

    public TagGroupService(OpenScrmProperties properties,
                           WeWorkClient weWorkClient,
                           TagGroupPoMapper tagGroupMapper,
                           TagPoMapper tagMapper,
                           TagSyncService tagSyncService,
                           SnowflakeIdGenerator idGenerator,
                           ObjectMapper objectMapper) {
        this.properties = properties;
        this.weWorkClient = weWorkClient;
        this.tagGroupMapper = tagGroupMapper;
        this.tagMapper = tagMapper;
        this.tagSyncService = tagSyncService;
        this.idGenerator = idGenerator;
        this.objectMapper = objectMapper;
    }

    public PageResponse<TagGroupResponse> query(String extCorpId,
                                                List<Long> extDepartmentIds,
                                                String name,
                                                long page,
                                                long pageSize) {
        QueryWrapper<TagPo> tagQuery = new QueryWrapper<TagPo>()
                .eq("ext_corp_id", extCorpId)
                .isNull("deleted_at");
        if (StringUtils.hasText(name)) {
            tagQuery.likeRight("name", name);
        }
        Set<String> extGroupIdsFromTags = tagMapper.selectList(tagQuery).stream()
                .map(TagPo::getExtGroupId)
                .filter(StringUtils::hasText)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        QueryWrapper<TagGroupPo> groupQuery = new QueryWrapper<TagGroupPo>()
                .eq("ext_corp_id", extCorpId)
                .isNull("deleted_at")
                .orderByDesc("order")
                .orderByDesc("updated_at");
        if (StringUtils.hasText(name)) {
            if (extGroupIdsFromTags.isEmpty()) {
                groupQuery.likeRight("name", name);
            } else {
                groupQuery.and(wrapper -> wrapper.likeRight("name", name).or().in("ext_id", extGroupIdsFromTags));
            }
        } else {
            if (extGroupIdsFromTags.isEmpty()) {
                return new PageResponse<>(Collections.emptyList(), 0, page, pageSize);
            }
            groupQuery.in("ext_id", extGroupIdsFromTags);
        }

        List<TagGroupPo> matched = tagGroupMapper.selectList(groupQuery).stream()
                .filter(group -> matchesDepartment(group, extDepartmentIds))
                .collect(Collectors.toList());
        long total = matched.size();
        int from = Math.toIntExact(Math.min((page - 1) * pageSize, total));
        int to = Math.toIntExact(Math.min(from + pageSize, total));
        List<TagGroupPo> pageGroups = matched.subList(from, to);
        return new PageResponse<>(toResponses(pageGroups), total, page, pageSize);
    }

    @Transactional(rollbackFor = Exception.class)
    public TagGroupResponse create(TagGroupCreateRequest request, String extCorpId) {
        long duplicate = tagGroupMapper.selectCount(new QueryWrapper<TagGroupPo>()
                .eq("ext_corp_id", extCorpId)
                .eq("name", request.getName())
                .isNull("deleted_at"));
        if (duplicate > 0) {
            throw new IllegalArgumentException("duplicate tag group name");
        }
        ExternalContactCorpTagGroup corpRequest = new ExternalContactCorpTagGroup();
        corpRequest.setGroupName(request.getName());
        corpRequest.setOrder(request.getOrder());
        corpRequest.setTag(toCorpTags(request.getTags()));

        ExternalContactCorpTagGroup corpGroup = weWorkClient.addExternalContactCorpTag(
                extCorpId, properties.getWeWork().getCustomerSecret(), corpRequest);
        TagGroupPo group = upsertGroup(extCorpId, corpGroup, request.getDepartmentList());
        upsertTags(extCorpId, corpGroup);
        return get(group.getExtId(), extCorpId);
    }

    @Transactional(rollbackFor = Exception.class)
    public long delete(TagGroupDeleteRequest request, String extCorpId) {
        weWorkClient.deleteExternalContactCorpTag(
                extCorpId, properties.getWeWork().getCustomerSecret(), Collections.emptyList(), request.getExtIds());
        for (String extId : request.getExtIds()) {
            tagSyncService.deleteTagGroup(extId);
        }
        return request.getExtIds().size();
    }

    @Transactional(rollbackFor = Exception.class)
    public TagGroupResponse update(String extGroupId, TagGroupUpdateRequest request, String extCorpId) {
        List<ExternalContactCorpTag> newTags = new ArrayList<>();
        int index = 0;
        for (TagGroupTagRequest tag : safeTags(request.getTags())) {
            int order = tag.getOrder() == null ? 10000 - index : tag.getOrder();
            index++;
            if (!StringUtils.hasText(tag.getName())) {
                continue;
            }
            if (StringUtils.hasText(tag.getExtId())) {
                weWorkClient.editExternalContactCorpTag(
                        extCorpId, properties.getWeWork().getCustomerSecret(), tag.getExtId(), tag.getName(), order);
                updateLocalTag(extGroupId, tag.getExtId(), tag.getName(), order);
            } else {
                ExternalContactCorpTag corpTag = new ExternalContactCorpTag();
                corpTag.setName(tag.getName());
                corpTag.setOrder(order);
                newTags.add(corpTag);
            }
        }

        if (!newTags.isEmpty()) {
            ExternalContactCorpTagGroup addRequest = new ExternalContactCorpTagGroup();
            addRequest.setGroupId(extGroupId);
            addRequest.setTag(newTags);
            ExternalContactCorpTagGroup added = weWorkClient.addExternalContactCorpTag(
                    extCorpId, properties.getWeWork().getCustomerSecret(), addRequest);
            upsertTags(extCorpId, added);
        }

        if (request.getRemoveExtTagIds() != null && !request.getRemoveExtTagIds().isEmpty()) {
            weWorkClient.deleteExternalContactCorpTag(
                    extCorpId, properties.getWeWork().getCustomerSecret(), request.getRemoveExtTagIds(), Collections.emptyList());
            for (String extTagId : request.getRemoveExtTagIds()) {
                tagSyncService.deleteTag(extTagId);
            }
        }

        weWorkClient.editExternalContactCorpTag(
                extCorpId, properties.getWeWork().getCustomerSecret(), extGroupId, request.getName(), request.getOrder());
        TagGroupPo group = findGroup(extGroupId);
        if (group == null) {
            group = new TagGroupPo();
            group.setId(idGenerator.nextId());
            group.setExtCorpId(extCorpId);
            group.setExtId(extGroupId);
            group.setCreateTime(0);
        }
        group.setName(request.getName());
        group.setOrder(request.getOrder());
        group.setDepartmentList(writeDepartments(request.getDepartmentList()));
        group.setUpdatedAt(LocalDateTime.now());
        if (tagGroupMapper.selectById(group.getId()) == null) {
            tagGroupMapper.insert(group);
        } else {
            tagGroupMapper.updateById(group);
        }
        return get(extGroupId, extCorpId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void exchangeOrder(TagGroupExchangeOrderRequest request) {
        TagGroupPo one = tagGroupMapper.selectById(request.getId());
        TagGroupPo other = tagGroupMapper.selectById(request.getExchangeOrderId());
        if (one == null || other == null) {
            throw new IllegalArgumentException("tag group not found");
        }
        Integer order = one.getOrder();
        one.setOrder(other.getOrder());
        other.setOrder(order);
        tagGroupMapper.updateById(one);
        tagGroupMapper.updateById(other);
    }

    private TagGroupResponse get(String extGroupId, String extCorpId) {
        TagGroupPo group = tagGroupMapper.selectOne(new QueryWrapper<TagGroupPo>()
                .eq("ext_corp_id", extCorpId)
                .eq("ext_id", extGroupId)
                .isNull("deleted_at")
                .last("limit 1"));
        if (group == null) {
            throw new IllegalArgumentException("tag group not found");
        }
        return toResponses(Collections.singletonList(group)).get(0);
    }

    private List<TagGroupResponse> toResponses(List<TagGroupPo> groups) {
        if (groups.isEmpty()) {
            return Collections.emptyList();
        }
        Set<String> extGroupIds = groups.stream().map(TagGroupPo::getExtId).collect(Collectors.toSet());
        List<TagPo> tags = tagMapper.selectList(new QueryWrapper<TagPo>()
                .in("ext_group_id", extGroupIds)
                .isNull("deleted_at")
                .orderByDesc("order"));
        Map<String, List<TagResponse>> groupedTags = new LinkedHashMap<>();
        for (TagPo tag : tags) {
            groupedTags.computeIfAbsent(tag.getExtGroupId(), key -> new ArrayList<>()).add(TagResponse.from(tag));
        }
        return groups.stream()
                .map(group -> TagGroupResponse.from(
                        group,
                        readDepartments(group.getDepartmentList()),
                        groupedTags.getOrDefault(group.getExtId(), Collections.emptyList())))
                .collect(Collectors.toList());
    }

    private TagGroupPo upsertGroup(String extCorpId, ExternalContactCorpTagGroup corpGroup, List<Long> departments) {
        TagGroupPo group = findGroup(corpGroup.getGroupId());
        if (group == null) {
            group = new TagGroupPo();
            group.setId(idGenerator.nextId());
            group.setCreatedAt(LocalDateTime.now());
        }
        group.setExtCorpId(extCorpId);
        group.setExtId(corpGroup.getGroupId());
        group.setName(corpGroup.getGroupName());
        group.setCreateTime(corpGroup.getCreateTime());
        group.setOrder(corpGroup.getOrder());
        group.setDepartmentList(writeDepartments(departments));
        group.setUpdatedAt(LocalDateTime.now());
        if (tagGroupMapper.selectById(group.getId()) == null) {
            tagGroupMapper.insert(group);
        } else {
            tagGroupMapper.updateById(group);
        }
        return group;
    }

    private void upsertTags(String extCorpId, ExternalContactCorpTagGroup corpGroup) {
        if (corpGroup.getTag() == null) {
            return;
        }
        for (ExternalContactCorpTag corpTag : corpGroup.getTag()) {
            TagPo tag = tagMapper.selectOne(new QueryWrapper<TagPo>()
                    .eq("ext_id", corpTag.getId())
                    .last("limit 1"));
            if (tag == null) {
                tag = new TagPo();
                tag.setId(idGenerator.nextId());
                tag.setCreatedAt(LocalDateTime.now());
            }
            tag.setExtCorpId(extCorpId);
            tag.setExtId(corpTag.getId());
            tag.setExtGroupId(corpGroup.getGroupId());
            tag.setName(corpTag.getName());
            tag.setGroupName(corpGroup.getGroupName());
            tag.setCreateTime(corpTag.getCreateTime());
            tag.setOrder(corpTag.getOrder());
            tag.setType(1);
            tag.setUpdatedAt(LocalDateTime.now());
            if (tagMapper.selectById(tag.getId()) == null) {
                tagMapper.insert(tag);
            } else {
                tagMapper.updateById(tag);
            }
        }
    }

    private void updateLocalTag(String extGroupId, String extTagId, String name, Integer order) {
        TagPo tag = tagMapper.selectOne(new QueryWrapper<TagPo>()
                .eq("ext_id", extTagId)
                .last("limit 1"));
        if (tag == null) {
            return;
        }
        tag.setExtGroupId(extGroupId);
        tag.setName(name);
        tag.setOrder(order);
        tag.setUpdatedAt(LocalDateTime.now());
        tagMapper.updateById(tag);
    }

    private TagGroupPo findGroup(String extGroupId) {
        return tagGroupMapper.selectOne(new QueryWrapper<TagGroupPo>()
                .eq("ext_id", extGroupId)
                .last("limit 1"));
    }

    private List<ExternalContactCorpTag> toCorpTags(List<TagGroupTagRequest> requests) {
        List<ExternalContactCorpTag> tags = new ArrayList<>();
        int index = 0;
        for (TagGroupTagRequest request : safeTags(requests)) {
            if (!StringUtils.hasText(request.getName())) {
                continue;
            }
            ExternalContactCorpTag tag = new ExternalContactCorpTag();
            tag.setName(request.getName());
            tag.setOrder(request.getOrder() == null ? 10000 - index : request.getOrder());
            tags.add(tag);
            index++;
        }
        return tags;
    }

    private List<TagGroupTagRequest> safeTags(List<TagGroupTagRequest> tags) {
        return tags == null ? Collections.emptyList() : tags;
    }

    private boolean matchesDepartment(TagGroupPo group, List<Long> extDepartmentIds) {
        if (extDepartmentIds == null || extDepartmentIds.isEmpty() || extDepartmentIds.contains(0L)) {
            return true;
        }
        List<Long> departments = readDepartments(group.getDepartmentList());
        if (departments.isEmpty() || departments.contains(0L)) {
            return false;
        }
        Set<Long> requested = new HashSet<>(extDepartmentIds);
        for (Long department : departments) {
            if (requested.contains(department)) {
                return true;
            }
        }
        return false;
    }

    private String writeDepartments(List<Long> departments) {
        List<Long> values = departments == null || departments.isEmpty() ? Collections.singletonList(0L) : departments;
        try {
            return objectMapper.writeValueAsString(values);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("invalid department list", e);
        }
    }

    private List<Long> readDepartments(String source) {
        if (!StringUtils.hasText(source)) {
            return Collections.singletonList(0L);
        }
        try {
            List<Long> values = objectMapper.readValue(source, LONG_LIST);
            return values == null || values.isEmpty() ? Collections.singletonList(0L) : values;
        } catch (JsonProcessingException e) {
            return Collections.singletonList(0L);
        }
    }
}
