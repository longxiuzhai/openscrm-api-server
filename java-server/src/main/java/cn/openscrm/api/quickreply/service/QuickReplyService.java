package cn.openscrm.api.quickreply.service;

import cn.openscrm.api.common.api.PageResponse;
import cn.openscrm.api.common.exception.BizException;
import cn.openscrm.api.common.exception.ErrorCode;
import cn.openscrm.api.common.id.SnowflakeIdGenerator;
import cn.openscrm.api.persistence.entity.QuickReplyDetailPo;
import cn.openscrm.api.persistence.entity.QuickReplyGroupPo;
import cn.openscrm.api.persistence.entity.QuickReplyPo;
import cn.openscrm.api.persistence.entity.StaffPo;
import cn.openscrm.api.persistence.mapper.QuickReplyDetailPoMapper;
import cn.openscrm.api.persistence.mapper.QuickReplyGroupPoMapper;
import cn.openscrm.api.persistence.mapper.QuickReplyPoMapper;
import cn.openscrm.api.persistence.mapper.StaffPoMapper;
import cn.openscrm.api.quickreply.dto.QuickReplyDeleteRequest;
import cn.openscrm.api.quickreply.dto.QuickReplyDetailRequest;
import cn.openscrm.api.quickreply.dto.QuickReplyRequest;
import cn.openscrm.api.quickreply.dto.QuickReplyResponse;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class QuickReplyService {

    private static final int TYPE_COLLECTION = 1;

    private final QuickReplyPoMapper quickReplyMapper;
    private final QuickReplyDetailPoMapper detailMapper;
    private final QuickReplyGroupPoMapper groupMapper;
    private final StaffPoMapper staffMapper;
    private final SnowflakeIdGenerator idGenerator;
    private final ObjectMapper objectMapper;

    @Transactional
    public QuickReplyPo create(QuickReplyRequest request, StaffPo staff) {
        LocalDateTime now = LocalDateTime.now();
        Long id = idGenerator.nextId();
        List<QuickReplyDetailRequest> details = request.getReplyDetails() == null
                ? Collections.emptyList()
                : request.getReplyDetails();
        QuickReplyPo item = new QuickReplyPo();
        item.setId(id);
        item.setExtCorpId(staff.getExtCorpId());
        item.setExtCreatorId(staff.getExtId());
        item.setName(request.getName());
        item.setGroupId(request.getGroupId());
        item.setExtStaffId(staff.getExtId());
        item.setStaffName(staff.getName());
        item.setSendCount(0);
        item.setQuickReplyType(details.size() > 1 ? TYPE_COLLECTION : firstType(details));
        item.setSearchableText(writeJson(buildSearchableText(request.getName(), details)));
        item.setCreatedAt(now);
        item.setUpdatedAt(now);
        quickReplyMapper.insert(item);
        upsertDetails(String.valueOf(id), details, staff.getExtCorpId(), staff.getExtId(), now);
        return item;
    }

    public PageResponse<QuickReplyResponse> query(String extCorpId,
                                                  String keyword,
                                                  String groupId,
                                                  List<Long> departmentIds,
                                                  long page,
                                                  long pageSize) {
        LambdaQueryWrapper<QuickReplyPo> wrapper = new LambdaQueryWrapper<QuickReplyPo>()
                .eq(QuickReplyPo::getExtCorpId, extCorpId)
                .isNull(QuickReplyPo::getDeletedAt)
                .likeRight(StringUtils.hasText(keyword), QuickReplyPo::getName, keyword)
                .eq(StringUtils.hasText(groupId), QuickReplyPo::getGroupId, groupId)
                .orderByDesc(QuickReplyPo::getCreatedAt);
        if (!CollectionUtils.isEmpty(departmentIds)) {
            List<QuickReplyGroupPo> groups = groupMapper.selectList(new LambdaQueryWrapper<QuickReplyGroupPo>()
                    .eq(QuickReplyGroupPo::getExtCorpId, extCorpId)
                    .isNull(QuickReplyGroupPo::getDeletedAt)
                    .and(w -> {
                        for (Long departmentId : departmentIds) {
                            w.or().apply("JSON_CONTAINS(departments, JSON_ARRAY({0}))", departmentId);
                        }
                    }));
            List<String> groupIds = groups.stream().map(QuickReplyGroupPo::getId).map(String::valueOf).collect(Collectors.toList());
            if (groupIds.isEmpty()) {
                return new PageResponse<>(Collections.emptyList(), 0, page, pageSize);
            }
            wrapper.in(QuickReplyPo::getGroupId, groupIds);
        }
        Page<QuickReplyPo> result = quickReplyMapper.selectPage(new Page<>(page, pageSize), wrapper);
        return new PageResponse<>(attachDetailsAndAvatar(result.getRecords()), result.getTotal(), page, pageSize);
    }

    public List<QuickReplyResponse> queryByGroup(String extCorpId, String groupId) {
        if (!StringUtils.hasText(groupId)) {
            return Collections.emptyList();
        }
        List<QuickReplyPo> replies = quickReplyMapper.selectList(new LambdaQueryWrapper<QuickReplyPo>()
                .eq(QuickReplyPo::getExtCorpId, extCorpId)
                .eq(QuickReplyPo::getGroupId, groupId)
                .isNull(QuickReplyPo::getDeletedAt)
                .orderByDesc(QuickReplyPo::getCreatedAt));
        return attachDetailsAndAvatar(replies);
    }

    @Transactional
    public int delete(QuickReplyDeleteRequest request, String extCorpId) {
        if (request == null || CollectionUtils.isEmpty(request.getIds())) {
            return 0;
        }
        QuickReplyPo update = new QuickReplyPo();
        update.setDeletedAt(LocalDateTime.now());
        update.setUpdatedAt(LocalDateTime.now());
        return quickReplyMapper.update(update, new LambdaQueryWrapper<QuickReplyPo>()
                .eq(QuickReplyPo::getExtCorpId, extCorpId)
                .in(QuickReplyPo::getId, request.getIds())
                .isNull(QuickReplyPo::getDeletedAt));
    }

    @Transactional
    public QuickReplyResponse update(QuickReplyRequest request, StaffPo staff) {
        QuickReplyPo item = quickReplyMapper.selectOne(new LambdaQueryWrapper<QuickReplyPo>()
                .eq(QuickReplyPo::getId, request.getId())
                .eq(QuickReplyPo::getExtCorpId, staff.getExtCorpId())
                .isNull(QuickReplyPo::getDeletedAt));
        if (item == null) {
            throw new BizException(ErrorCode.ITEM_NOT_FOUND);
        }
        LocalDateTime now = LocalDateTime.now();
        if (!CollectionUtils.isEmpty(request.getDeletedIds())) {
            QuickReplyDetailPo detailUpdate = new QuickReplyDetailPo();
            detailUpdate.setDeletedAt(now);
            detailUpdate.setUpdatedAt(now);
            detailMapper.update(detailUpdate, new LambdaQueryWrapper<QuickReplyDetailPo>()
                    .in(QuickReplyDetailPo::getId, request.getDeletedIds())
                    .eq(QuickReplyDetailPo::getExtCorpId, staff.getExtCorpId())
                    .isNull(QuickReplyDetailPo::getDeletedAt));
        }
        List<QuickReplyDetailRequest> details = request.getReplyDetails() == null
                ? Collections.emptyList()
                : request.getReplyDetails();
        item.setName(request.getName());
        item.setGroupId(request.getGroupId());
        item.setQuickReplyType(details.size() > 1 ? TYPE_COLLECTION : firstType(details));
        item.setSearchableText(writeJson(buildSearchableText(request.getName(), details)));
        item.setUpdatedAt(now);
        quickReplyMapper.updateById(item);
        upsertDetails(String.valueOf(item.getId()), details, staff.getExtCorpId(), staff.getExtId(), now);
        return attachDetailsAndAvatar(Collections.singletonList(item)).get(0);
    }

    private void upsertDetails(String quickReplyId,
                               List<QuickReplyDetailRequest> details,
                               String extCorpId,
                               String extCreatorId,
                               LocalDateTime now) {
        for (QuickReplyDetailRequest detail : details) {
            boolean insert = false;
            QuickReplyDetailPo item = detail.getId() == null ? null : detailMapper.selectOne(
                    new LambdaQueryWrapper<QuickReplyDetailPo>()
                            .eq(QuickReplyDetailPo::getId, detail.getId())
                            .eq(QuickReplyDetailPo::getExtCorpId, extCorpId)
                            .isNull(QuickReplyDetailPo::getDeletedAt));
            if (item == null) {
                item = new QuickReplyDetailPo();
                item.setId(idGenerator.nextId());
                item.setExtCorpId(extCorpId);
                item.setExtCreatorId(extCreatorId);
                item.setQuickReplyId(quickReplyId);
                item.setSendCount(0L);
                item.setCreatedAt(now);
                insert = true;
            }
            item.setQuickReplyId(quickReplyId);
            item.setContentType(detail.getContentType());
            item.setQuickReplyContent(writeJson(withMsgType(detail.getQuickReplyContent(), detail.getContentType())));
            item.setUpdatedAt(now);
            if (insert) {
                detailMapper.insert(item);
            } else {
                detailMapper.updateById(item);
            }
        }
    }

    private List<QuickReplyResponse> attachDetailsAndAvatar(List<QuickReplyPo> replies) {
        if (CollectionUtils.isEmpty(replies)) {
            return Collections.emptyList();
        }
        List<String> ids = replies.stream().map(QuickReplyPo::getId).map(String::valueOf).collect(Collectors.toList());
        List<QuickReplyDetailPo> details = detailMapper.selectList(new LambdaQueryWrapper<QuickReplyDetailPo>()
                .in(QuickReplyDetailPo::getQuickReplyId, ids)
                .isNull(QuickReplyDetailPo::getDeletedAt)
                .orderByAsc(QuickReplyDetailPo::getCreatedAt));
        Map<String, List<QuickReplyDetailPo>> detailsByReply = details.stream()
                .collect(Collectors.groupingBy(QuickReplyDetailPo::getQuickReplyId, LinkedHashMap::new, Collectors.toList()));
        List<String> creatorIds = replies.stream()
                .map(QuickReplyPo::getExtCreatorId)
                .filter(StringUtils::hasText)
                .distinct()
                .collect(Collectors.toList());
        Map<String, String> avatarByExtId = new LinkedHashMap<>();
        if (!CollectionUtils.isEmpty(creatorIds)) {
            for (StaffPo staff : staffMapper.selectList(new LambdaQueryWrapper<StaffPo>().in(StaffPo::getExtId, creatorIds))) {
                avatarByExtId.put(staff.getExtId(), staff.getAvatarUrl());
            }
        }
        return replies.stream()
                .map(item -> new QuickReplyResponse(
                        item,
                        avatarByExtId.get(item.getExtCreatorId()),
                        detailsByReply.getOrDefault(String.valueOf(item.getId()), Collections.emptyList())))
                .collect(Collectors.toList());
    }

    private int firstType(List<QuickReplyDetailRequest> details) {
        if (CollectionUtils.isEmpty(details) || details.get(0).getContentType() == null) {
            return TYPE_COLLECTION;
        }
        return details.get(0).getContentType();
    }

    private List<String> buildSearchableText(String name, List<QuickReplyDetailRequest> details) {
        List<String> values = new ArrayList<>();
        if (StringUtils.hasText(name)) {
            values.add(name);
        }
        if (details != null) {
            details.stream()
                    .map(this::keyword)
                    .filter(StringUtils::hasText)
                    .forEach(values::add);
        }
        return values;
    }

    private String keyword(QuickReplyDetailRequest detail) {
        JsonNode content = detail.getQuickReplyContent();
        if (content == null) {
            return null;
        }
        switch (Objects.requireNonNullElse(detail.getContentType(), 0)) {
            case 2:
                return text(content, "text", "content");
            case 3:
                return text(content, "image", "title");
            case 4:
                return text(content, "link", "title");
            case 5:
                return text(content, "pdf", "title");
            case 6:
                return text(content, "video", "title");
            default:
                return null;
        }
    }

    private String text(JsonNode root, String objectName, String fieldName) {
        JsonNode nested = root.get(objectName);
        if (nested != null && nested.get(fieldName) != null) {
            return nested.get(fieldName).asText();
        }
        JsonNode direct = root.get(fieldName);
        return direct == null ? null : direct.asText();
    }

    private JsonNode withMsgType(JsonNode content, Integer contentType) {
        ObjectNode copy = content == null || !content.isObject()
                ? objectMapper.createObjectNode()
                : ((ObjectNode) content).deepCopy();
        copy.put("msgtype", msgType(contentType));
        return copy;
    }

    private String msgType(Integer contentType) {
        switch (Objects.requireNonNullElse(contentType, 0)) {
            case 2:
                return "text";
            case 3:
                return "image";
            case 4:
                return "link";
            case 5:
                return "pdf";
            case 6:
                return "video";
            default:
                throw new BizException(ErrorCode.UNSUPPORTED_MSG);
        }
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new BizException(ErrorCode.INTERNAL_ERROR, "序列化话术内容失败");
        }
    }
}
