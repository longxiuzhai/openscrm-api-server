package cn.openscrm.api.groupchatwelcome.service;

import cn.openscrm.api.common.api.PageResponse;
import cn.openscrm.api.common.id.SnowflakeIdGenerator;
import cn.openscrm.api.groupchatwelcome.dto.CommonIdsRequest;
import cn.openscrm.api.groupchatwelcome.dto.GroupChatWelcomeMsgRequest;
import cn.openscrm.api.persistence.entity.GroupChatWelcomeMsgPo;
import cn.openscrm.api.persistence.mapper.GroupChatWelcomeMsgPoMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class GroupChatWelcomeMsgService {

    private final GroupChatWelcomeMsgPoMapper mapper;
    private final SnowflakeIdGenerator idGenerator;
    private final ObjectMapper objectMapper;

    public GroupChatWelcomeMsgService(GroupChatWelcomeMsgPoMapper mapper,
                                      SnowflakeIdGenerator idGenerator,
                                      ObjectMapper objectMapper) {
        this.mapper = mapper;
        this.idGenerator = idGenerator;
        this.objectMapper = objectMapper;
    }

    @Transactional(rollbackFor = Exception.class)
    public GroupChatWelcomeMsgPo create(GroupChatWelcomeMsgRequest request, String extCorpId, String extCreatorId) {
        GroupChatWelcomeMsgPo msg = new GroupChatWelcomeMsgPo();
        msg.setId(idGenerator.nextId());
        msg.setExtCorpId(extCorpId);
        msg.setExtCreatorId(extCreatorId);
        msg.setContent(request.getContent());
        msg.setAttachmentType(request.getAttachmentType());
        msg.setAttachment(writeAttachment(request.getAttachment()));
        msg.setCreatedAt(LocalDateTime.now());
        msg.setUpdatedAt(LocalDateTime.now());
        mapper.insert(msg);
        return msg;
    }

    @Transactional(rollbackFor = Exception.class)
    public GroupChatWelcomeMsgPo update(Long id, GroupChatWelcomeMsgRequest request, String extCorpId) {
        UpdateWrapper<GroupChatWelcomeMsgPo> update = new UpdateWrapper<GroupChatWelcomeMsgPo>()
                .eq("id", id)
                .eq("ext_corp_id", extCorpId)
                .isNull("deleted_at")
                .set("updated_at", LocalDateTime.now());
        if (request.getContent() != null) {
            update.set("content", request.getContent());
        }
        if (request.getAttachmentType() != null) {
            update.set("attachment_type", request.getAttachmentType());
        }
        if (request.getAttachment() != null) {
            update.set("attachment", writeAttachment(request.getAttachment()));
        }
        mapper.update(null, update);
        GroupChatWelcomeMsgPo msg = mapper.selectById(id);
        if (msg == null) {
            throw new IllegalArgumentException("group chat welcome msg not found");
        }
        return msg;
    }

    @Transactional(rollbackFor = Exception.class)
    public long delete(CommonIdsRequest request) {
        if (request.getIds() == null || request.getIds().isEmpty()) {
            return 0;
        }
        return mapper.deleteBatchIds(request.getIds());
    }

    public PageResponse<GroupChatWelcomeMsgPo> query(String extCorpId, String content, long page, long pageSize) {
        QueryWrapper<GroupChatWelcomeMsgPo> query = new QueryWrapper<GroupChatWelcomeMsgPo>()
                .eq("ext_corp_id", extCorpId)
                .isNull("deleted_at")
                .orderByDesc("updated_at");
        if (StringUtils.hasText(content)) {
            query.likeRight("content", content);
        }
        Page<GroupChatWelcomeMsgPo> result = mapper.selectPage(Page.of(page, pageSize), query);
        return new PageResponse<>(result.getRecords(), result.getTotal(), page, pageSize);
    }

    private String writeAttachment(JsonNode attachment) {
        if (attachment == null || attachment.isNull()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(attachment);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("invalid attachment", e);
        }
    }
}
