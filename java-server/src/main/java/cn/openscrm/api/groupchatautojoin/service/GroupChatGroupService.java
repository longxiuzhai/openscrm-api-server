package cn.openscrm.api.groupchatautojoin.service;

import cn.openscrm.api.common.api.PageResponse;
import cn.openscrm.api.common.id.SnowflakeIdGenerator;
import cn.openscrm.api.groupchatautojoin.dto.GroupChatGroupDeleteRequest;
import cn.openscrm.api.groupchatautojoin.dto.GroupChatGroupRequest;
import cn.openscrm.api.persistence.entity.GroupChatGroupPo;
import cn.openscrm.api.persistence.entity.StaffPo;
import cn.openscrm.api.persistence.mapper.GroupChatGroupPoMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class GroupChatGroupService {

    private final GroupChatGroupPoMapper groupMapper;
    private final SnowflakeIdGenerator idGenerator;

    @Transactional
    public GroupChatGroupPo create(GroupChatGroupRequest request, StaffPo current) {
        LocalDateTime now = LocalDateTime.now();
        GroupChatGroupPo item = new GroupChatGroupPo();
        item.setId(idGenerator.nextId());
        item.setExtCorpId(current.getExtCorpId());
        item.setExtCreatorId(current.getExtId());
        item.setName(request.getName());
        item.setIsDefault(2);
        item.setCreatedAt(now);
        item.setUpdatedAt(now);
        groupMapper.insert(item);
        return item;
    }

    public PageResponse<GroupChatGroupPo> query(String extCorpId, String name, long page, long pageSize) {
        Page<GroupChatGroupPo> result = groupMapper.selectPage(new Page<>(page, pageSize),
                new LambdaQueryWrapper<GroupChatGroupPo>()
                        .eq(GroupChatGroupPo::getExtCorpId, extCorpId)
                        .likeRight(StringUtils.hasText(name), GroupChatGroupPo::getName, name)
                        .isNull(GroupChatGroupPo::getDeletedAt)
                        .orderByDesc(GroupChatGroupPo::getCreatedAt));
        return new PageResponse<>(result.getRecords(), result.getTotal(), page, pageSize);
    }

    @Transactional
    public GroupChatGroupPo update(Long id, GroupChatGroupRequest request, String extCorpId) {
        GroupChatGroupPo item = new GroupChatGroupPo();
        item.setId(id);
        item.setName(request.getName());
        item.setUpdatedAt(LocalDateTime.now());
        groupMapper.update(item, new LambdaQueryWrapper<GroupChatGroupPo>()
                .eq(GroupChatGroupPo::getId, id)
                .eq(GroupChatGroupPo::getExtCorpId, extCorpId)
                .isNull(GroupChatGroupPo::getDeletedAt));
        return groupMapper.selectById(id);
    }

    @Transactional
    public int delete(GroupChatGroupDeleteRequest request, String extCorpId) {
        if (request == null || CollectionUtils.isEmpty(request.getIds())) {
            return 0;
        }
        GroupChatGroupPo update = new GroupChatGroupPo();
        update.setDeletedAt(LocalDateTime.now());
        update.setUpdatedAt(LocalDateTime.now());
        return groupMapper.update(update, new LambdaQueryWrapper<GroupChatGroupPo>()
                .eq(GroupChatGroupPo::getExtCorpId, extCorpId)
                .in(GroupChatGroupPo::getId, request.getIds())
                .isNull(GroupChatGroupPo::getDeletedAt));
    }
}
