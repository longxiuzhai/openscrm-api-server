package cn.openscrm.api.groupchattag.service;

import cn.openscrm.api.common.api.PageResponse;
import cn.openscrm.api.common.id.SnowflakeIdGenerator;
import cn.openscrm.api.groupchattag.dto.CommonIdsRequest;
import cn.openscrm.api.groupchattag.dto.GroupChatTagGroupCreateRequest;
import cn.openscrm.api.groupchattag.dto.GroupChatTagGroupResponse;
import cn.openscrm.api.groupchattag.dto.GroupChatTagGroupUpdateRequest;
import cn.openscrm.api.groupchattag.dto.GroupChatTagItemRequest;
import cn.openscrm.api.persistence.entity.GroupChatTagGroupPo;
import cn.openscrm.api.persistence.entity.GroupChatTagPo;
import cn.openscrm.api.persistence.mapper.GroupChatTagGroupPoMapper;
import cn.openscrm.api.persistence.mapper.GroupChatTagPoMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class GroupChatTagGroupService {

    private final GroupChatTagGroupPoMapper groupMapper;
    private final GroupChatTagPoMapper tagMapper;
    private final GroupChatTagService tagService;
    private final SnowflakeIdGenerator idGenerator;

    public GroupChatTagGroupService(GroupChatTagGroupPoMapper groupMapper,
                                    GroupChatTagPoMapper tagMapper,
                                    GroupChatTagService tagService,
                                    SnowflakeIdGenerator idGenerator) {
        this.groupMapper = groupMapper;
        this.tagMapper = tagMapper;
        this.tagService = tagService;
        this.idGenerator = idGenerator;
    }

    @Transactional(rollbackFor = Exception.class)
    public GroupChatTagGroupResponse create(GroupChatTagGroupCreateRequest request, String extCorpId, String extCreatorId) {
        GroupChatTagGroupPo group = new GroupChatTagGroupPo();
        group.setId(idGenerator.nextId());
        group.setExtCorpId(extCorpId);
        group.setExtCreatorId(extCreatorId);
        group.setName(request.getName());
        groupMapper.insert(group);

        List<GroupChatTagPo> tags = new ArrayList<>();
        if (request.getTags() != null) {
            for (GroupChatTagItemRequest tagRequest : request.getTags()) {
                if (StringUtils.hasText(tagRequest.getName())) {
                    GroupChatTagPo tag = tagService.newTag(extCorpId, extCreatorId, group.getId(), tagRequest.getName());
                    tags.add(tag);
                    tagMapper.insert(tag);
                }
            }
        }
        return GroupChatTagGroupResponse.from(group, tags);
    }

    @Transactional(rollbackFor = Exception.class)
    public GroupChatTagGroupResponse update(GroupChatTagGroupUpdateRequest request, String extCorpId) {
        if (StringUtils.hasText(request.getName())) {
            groupMapper.update(null, new UpdateWrapper<GroupChatTagGroupPo>()
                    .eq("id", request.getId())
                    .eq("ext_corp_id", extCorpId)
                    .set("name", request.getName()));
        }

        if (request.getTags() != null) {
            for (GroupChatTagItemRequest tagRequest : request.getTags()) {
                if (!StringUtils.hasText(tagRequest.getName())) {
                    continue;
                }
                if (tagRequest.getId() == null) {
                    tagMapper.insert(tagService.newTag(extCorpId, null, request.getId(), tagRequest.getName()));
                } else {
                    tagMapper.update(null, new UpdateWrapper<GroupChatTagPo>()
                            .eq("id", tagRequest.getId())
                            .eq("ext_corp_id", extCorpId)
                            .isNull("deleted_at")
                            .set("name", tagRequest.getName())
                            .set("group_chat_tag_group_id", String.valueOf(request.getId()))
                            .set("updated_at", LocalDateTime.now()));
                }
            }
        }

        if (request.getDeleteTagIds() != null && !request.getDeleteTagIds().isEmpty()) {
            tagMapper.deleteBatchIds(request.getDeleteTagIds());
        }
        return get(request.getId());
    }

    @Transactional(rollbackFor = Exception.class)
    public long delete(CommonIdsRequest request) {
        if (request.getIds() == null || request.getIds().isEmpty()) {
            return 0;
        }
        return groupMapper.deleteBatchIds(request.getIds());
    }

    public PageResponse<GroupChatTagGroupResponse> query(String extCorpId, String name, long page, long pageSize) {
        QueryWrapper<GroupChatTagGroupPo> query = new QueryWrapper<GroupChatTagGroupPo>()
                .eq("ext_corp_id", extCorpId);
        if (StringUtils.hasText(name)) {
            query.likeRight("name", name);
        }
        Page<GroupChatTagGroupPo> result = groupMapper.selectPage(Page.of(page, pageSize), query);
        return new PageResponse<>(toResponses(result.getRecords()), result.getTotal(), page, pageSize);
    }

    private GroupChatTagGroupResponse get(Long id) {
        GroupChatTagGroupPo group = groupMapper.selectById(id);
        if (group == null) {
            throw new IllegalArgumentException("group chat tag group not found");
        }
        List<GroupChatTagPo> tags = tagMapper.selectList(new QueryWrapper<GroupChatTagPo>()
                .eq("group_chat_tag_group_id", String.valueOf(id))
                .isNull("deleted_at"));
        return GroupChatTagGroupResponse.from(group, tags);
    }

    private List<GroupChatTagGroupResponse> toResponses(List<GroupChatTagGroupPo> groups) {
        if (groups == null || groups.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> groupIds = groups.stream().map(group -> String.valueOf(group.getId())).collect(Collectors.toList());
        List<GroupChatTagPo> tags = tagMapper.selectList(new QueryWrapper<GroupChatTagPo>()
                .in("group_chat_tag_group_id", groupIds)
                .isNull("deleted_at"));
        Map<String, List<GroupChatTagPo>> tagsByGroup = tags.stream()
                .collect(Collectors.groupingBy(GroupChatTagPo::getGroupChatTagGroupId));
        return groups.stream()
                .map(group -> GroupChatTagGroupResponse.from(
                        group, tagsByGroup.getOrDefault(String.valueOf(group.getId()), Collections.emptyList())))
                .collect(Collectors.toList());
    }
}
