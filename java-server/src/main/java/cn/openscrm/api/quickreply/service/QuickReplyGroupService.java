package cn.openscrm.api.quickreply.service;

import cn.openscrm.api.common.api.PageResponse;
import cn.openscrm.api.common.exception.BizException;
import cn.openscrm.api.common.exception.ErrorCode;
import cn.openscrm.api.common.id.SnowflakeIdGenerator;
import cn.openscrm.api.persistence.entity.QuickReplyGroupPo;
import cn.openscrm.api.persistence.entity.QuickReplyPo;
import cn.openscrm.api.persistence.mapper.QuickReplyGroupPoMapper;
import cn.openscrm.api.persistence.mapper.QuickReplyPoMapper;
import cn.openscrm.api.quickreply.dto.QuickReplyGroupDeleteRequest;
import cn.openscrm.api.quickreply.dto.QuickReplyGroupRequest;
import cn.openscrm.api.quickreply.dto.QuickReplyGroupResponse;
import cn.openscrm.api.quickreply.dto.QuickReplySubGroupRequest;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

@Service
@RequiredArgsConstructor
public class QuickReplyGroupService {

    private final QuickReplyGroupPoMapper groupMapper;
    private final QuickReplyPoMapper quickReplyMapper;
    private final QuickReplyService quickReplyService;
    private final SnowflakeIdGenerator idGenerator;
    private final ObjectMapper objectMapper;

    @Transactional
    public QuickReplyGroupPo create(QuickReplyGroupRequest request, String extCorpId, String extCreatorId) {
        LocalDateTime now = LocalDateTime.now();
        Long topGroupId = idGenerator.nextId();
        QuickReplyGroupPo top = new QuickReplyGroupPo();
        top.setId(topGroupId);
        top.setExtCorpId(extCorpId);
        top.setExtCreatorId(extCreatorId);
        top.setName(request.getName());
        top.setDepartments(writeDepartments(request.getDepartments()));
        top.setIsTopGroup(1);
        top.setCreatedAt(now);
        top.setUpdatedAt(now);
        groupMapper.insert(top);

        List<QuickReplySubGroupRequest> subGroups = request.getSubGroups();
        if (!CollectionUtils.isEmpty(subGroups)) {
            for (int i = 0; i < subGroups.size(); i++) {
                QuickReplySubGroupRequest sub = subGroups.get(i);
                if (sub.getName() == null || sub.getName().isEmpty()) {
                    continue;
                }
                QuickReplyGroupPo item = new QuickReplyGroupPo();
                item.setId(idGenerator.nextId());
                item.setExtCorpId(extCorpId);
                item.setExtCreatorId(extCreatorId);
                item.setName(sub.getName());
                item.setParentId(topGroupId);
                item.setDepartments(writeDepartments(request.getDepartments()));
                item.setIsTopGroup(0);
                item.setOrder(10000 - i);
                item.setCreatedAt(now);
                item.setUpdatedAt(now);
                groupMapper.insert(item);
            }
        }
        return top;
    }

    public PageResponse<QuickReplyGroupResponse> query(String extCorpId, long page, long pageSize) {
        Page<QuickReplyGroupPo> result = groupMapper.selectPage(new Page<>(page, pageSize),
                new LambdaQueryWrapper<QuickReplyGroupPo>()
                        .eq(QuickReplyGroupPo::getExtCorpId, extCorpId)
                        .isNull(QuickReplyGroupPo::getDeletedAt)
                        .orderByDesc(QuickReplyGroupPo::getCreatedAt));
        List<QuickReplyGroupResponse> responses = attachChildrenAndReplies(extCorpId, result.getRecords());
        return new PageResponse<>(responses, result.getTotal(), page, pageSize);
    }

    public PageResponse<QuickReplyGroupResponse> searchByQuickReply(String extCorpId,
                                                                    String keyword,
                                                                    long page,
                                                                    long pageSize) {
        Page<QuickReplyPo> replies = quickReplyMapper.selectPage(new Page<>(page, pageSize),
                new LambdaQueryWrapper<QuickReplyPo>()
                        .eq(QuickReplyPo::getExtCorpId, extCorpId)
                        .likeRight(org.springframework.util.StringUtils.hasText(keyword), QuickReplyPo::getName, keyword)
                        .isNull(QuickReplyPo::getDeletedAt)
                        .orderByDesc(QuickReplyPo::getCreatedAt));
        List<Long> groupIds = replies.getRecords().stream()
                .map(QuickReplyPo::getGroupId)
                .filter(org.springframework.util.StringUtils::hasText)
                .map(this::parseLong)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (groupIds.isEmpty()) {
            return new PageResponse<>(Collections.emptyList(), replies.getTotal(), page, pageSize);
        }
        List<QuickReplyGroupPo> groups = groupMapper.selectList(new LambdaQueryWrapper<QuickReplyGroupPo>()
                .eq(QuickReplyGroupPo::getExtCorpId, extCorpId)
                .in(QuickReplyGroupPo::getId, groupIds)
                .isNull(QuickReplyGroupPo::getDeletedAt));
        return new PageResponse<>(attachChildrenAndReplies(extCorpId, groups), replies.getTotal(), page, pageSize);
    }

    @Transactional
    public QuickReplyGroupResponse update(Long id, QuickReplyGroupRequest request, String extCorpId) {
        QuickReplyGroupPo existing = getRequired(id, extCorpId);
        LocalDateTime now = LocalDateTime.now();
        existing.setName(request.getName());
        existing.setDepartments(writeDepartments(request.getDepartments()));
        existing.setUpdatedAt(now);
        groupMapper.updateById(existing);

        if (!CollectionUtils.isEmpty(request.getSubGroups())) {
            for (int i = 0; i < request.getSubGroups().size(); i++) {
                QuickReplySubGroupRequest sub = request.getSubGroups().get(i);
                if (sub.getName() == null || sub.getName().isEmpty()) {
                    continue;
                }
                QuickReplyGroupPo item = sub.getId() == null ? null : groupMapper.selectOne(
                        new LambdaQueryWrapper<QuickReplyGroupPo>()
                                .eq(QuickReplyGroupPo::getId, sub.getId())
                                .eq(QuickReplyGroupPo::getExtCorpId, extCorpId)
                                .isNull(QuickReplyGroupPo::getDeletedAt));
                if (item == null) {
                    item = new QuickReplyGroupPo();
                    item.setId(idGenerator.nextId());
                    item.setExtCorpId(extCorpId);
                    item.setParentId(id);
                    item.setIsTopGroup(0);
                    item.setCreatedAt(now);
                }
                item.setName(sub.getName());
                item.setParentId(id);
                item.setOrder(10000 - i);
                item.setUpdatedAt(now);
                if (item.getCreatedAt() == null) {
                    item.setCreatedAt(now);
                }
                if (sub.getId() == null) {
                    groupMapper.insert(item);
                } else {
                    groupMapper.updateById(item);
                }
            }
        }
        if (!CollectionUtils.isEmpty(request.getDeleteGroupIds())) {
            softDeleteGroups(request.getDeleteGroupIds(), extCorpId);
        }
        return attachChildrenAndReplies(extCorpId, Collections.singletonList(getRequired(id, extCorpId))).get(0);
    }

    @Transactional
    public void delete(QuickReplyGroupDeleteRequest request, String extCorpId) {
        if (request == null || CollectionUtils.isEmpty(request.getIds())) {
            return;
        }
        List<String> groupIds = request.getIds().stream().map(String::valueOf).collect(Collectors.toList());
        QuickReplyPo replyUpdate = new QuickReplyPo();
        replyUpdate.setDeletedAt(LocalDateTime.now());
        replyUpdate.setUpdatedAt(LocalDateTime.now());
        quickReplyMapper.update(replyUpdate, new LambdaQueryWrapper<QuickReplyPo>()
                .eq(QuickReplyPo::getExtCorpId, extCorpId)
                .in(QuickReplyPo::getGroupId, groupIds)
                .isNull(QuickReplyPo::getDeletedAt));
        softDeleteGroups(request.getIds(), extCorpId);
    }

    private QuickReplyGroupPo getRequired(Long id, String extCorpId) {
        QuickReplyGroupPo item = groupMapper.selectOne(new LambdaQueryWrapper<QuickReplyGroupPo>()
                .eq(QuickReplyGroupPo::getId, id)
                .eq(QuickReplyGroupPo::getExtCorpId, extCorpId)
                .isNull(QuickReplyGroupPo::getDeletedAt));
        if (item == null) {
            throw new BizException(ErrorCode.QUICK_REPLY_GROUP_NOT_FOUND);
        }
        return item;
    }

    private void softDeleteGroups(List<Long> ids, String extCorpId) {
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        QuickReplyGroupPo update = new QuickReplyGroupPo();
        update.setDeletedAt(now);
        update.setUpdatedAt(now);
        groupMapper.update(update, new LambdaQueryWrapper<QuickReplyGroupPo>()
                .eq(QuickReplyGroupPo::getExtCorpId, extCorpId)
                .and(w -> w.in(QuickReplyGroupPo::getId, ids).or().in(QuickReplyGroupPo::getParentId, ids))
                .isNull(QuickReplyGroupPo::getDeletedAt));
    }

    private List<QuickReplyGroupResponse> attachChildrenAndReplies(String extCorpId, List<QuickReplyGroupPo> groups) {
        if (CollectionUtils.isEmpty(groups)) {
            return Collections.emptyList();
        }
        List<Long> ids = groups.stream().map(QuickReplyGroupPo::getId).collect(Collectors.toList());
        List<QuickReplyGroupPo> children = groupMapper.selectList(new LambdaQueryWrapper<QuickReplyGroupPo>()
                .eq(QuickReplyGroupPo::getExtCorpId, extCorpId)
                .in(QuickReplyGroupPo::getParentId, ids)
                .isNull(QuickReplyGroupPo::getDeletedAt)
                .orderByDesc(QuickReplyGroupPo::getOrder));
        Map<Long, List<QuickReplyGroupResponse>> childrenByParent = children.stream()
                .collect(Collectors.groupingBy(QuickReplyGroupPo::getParentId,
                        Collectors.mapping(item -> new QuickReplyGroupResponse(
                                item,
                                Collections.emptyList(),
                                quickReplyService.queryByGroup(extCorpId, String.valueOf(item.getId()))),
                                Collectors.toList())));
        return groups.stream()
                .map(item -> new QuickReplyGroupResponse(
                        item,
                        childrenByParent.getOrDefault(item.getId(), Collections.emptyList()),
                        quickReplyService.queryByGroup(extCorpId, String.valueOf(item.getId()))))
                .collect(Collectors.toList());
    }

    private String writeDepartments(List<Long> values) {
        try {
            return objectMapper.writeValueAsString(values == null ? Collections.emptyList() : values);
        } catch (JsonProcessingException e) {
            throw new BizException(ErrorCode.INTERNAL_ERROR, "序列化话术分组部门失败");
        }
    }

    private Long parseLong(String value) {
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
