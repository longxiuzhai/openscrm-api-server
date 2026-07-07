package cn.openscrm.api.groupchattag.service;

import cn.openscrm.api.common.id.SnowflakeIdGenerator;
import cn.openscrm.api.groupchattag.dto.CommonIdsRequest;
import cn.openscrm.api.groupchattag.dto.GroupChatTagCreateRequest;
import cn.openscrm.api.groupchattag.dto.GroupChatTagUpdateRequest;
import cn.openscrm.api.persistence.entity.GroupChatTagPo;
import cn.openscrm.api.persistence.mapper.GroupChatTagPoMapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class GroupChatTagService {

    private final GroupChatTagPoMapper tagMapper;
    private final SnowflakeIdGenerator idGenerator;

    public GroupChatTagService(GroupChatTagPoMapper tagMapper, SnowflakeIdGenerator idGenerator) {
        this.tagMapper = tagMapper;
        this.idGenerator = idGenerator;
    }

    @Transactional(rollbackFor = Exception.class)
    public List<GroupChatTagPo> create(GroupChatTagCreateRequest request, String extCorpId, String extCreatorId) {
        List<GroupChatTagPo> tags = new ArrayList<>();
        for (String name : request.getNames()) {
            if (!StringUtils.hasText(name)) {
                continue;
            }
            GroupChatTagPo tag = newTag(extCorpId, extCreatorId, request.getGroupId(), name);
            tags.add(tag);
            tagMapper.insert(tag);
        }
        return tags;
    }

    @Transactional(rollbackFor = Exception.class)
    public GroupChatTagPo update(GroupChatTagUpdateRequest request, String extCorpId) {
        tagMapper.update(null, new UpdateWrapper<GroupChatTagPo>()
                .eq("id", request.getId())
                .eq("ext_corp_id", extCorpId)
                .isNull("deleted_at")
                .set("name", request.getName())
                .set("updated_at", LocalDateTime.now()));
        GroupChatTagPo tag = tagMapper.selectById(request.getId());
        if (tag == null) {
            tag = new GroupChatTagPo();
            tag.setId(request.getId());
            tag.setExtCorpId(extCorpId);
            tag.setName(request.getName());
        }
        return tag;
    }

    @Transactional(rollbackFor = Exception.class)
    public long delete(CommonIdsRequest request) {
        if (request.getIds() == null || request.getIds().isEmpty()) {
            return 0;
        }
        return tagMapper.deleteBatchIds(request.getIds());
    }

    GroupChatTagPo newTag(String extCorpId, String extCreatorId, Long groupId, String name) {
        GroupChatTagPo tag = new GroupChatTagPo();
        tag.setId(idGenerator.nextId());
        tag.setExtCorpId(extCorpId);
        tag.setExtCreatorId(extCreatorId);
        tag.setGroupChatTagGroupId(String.valueOf(groupId));
        tag.setName(name);
        tag.setCreatedAt(LocalDateTime.now());
        tag.setUpdatedAt(LocalDateTime.now());
        return tag;
    }
}
