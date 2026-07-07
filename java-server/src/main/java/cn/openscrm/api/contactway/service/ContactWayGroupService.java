package cn.openscrm.api.contactway.service;

import cn.openscrm.api.common.api.PageResponse;
import cn.openscrm.api.common.exception.BizException;
import cn.openscrm.api.common.exception.ErrorCode;
import cn.openscrm.api.common.id.SnowflakeIdGenerator;
import cn.openscrm.api.contactway.dto.ContactWayGroupDeleteRequest;
import cn.openscrm.api.contactway.dto.ContactWayGroupRequest;
import cn.openscrm.api.contactway.dto.ContactWayGroupResponse;
import cn.openscrm.api.persistence.entity.ContactWayGroupPo;
import cn.openscrm.api.persistence.entity.ContactWayPo;
import cn.openscrm.api.persistence.mapper.ContactWayGroupPoMapper;
import cn.openscrm.api.persistence.mapper.ContactWayPoMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class ContactWayGroupService {

    private static final int FALSE = 2;

    private final ContactWayGroupPoMapper contactWayGroupMapper;
    private final ContactWayPoMapper contactWayMapper;
    private final SnowflakeIdGenerator idGenerator;

    public PageResponse<ContactWayGroupResponse> query(String extCorpId,
                                                       Long id,
                                                       String name,
                                                       Integer isDefault,
                                                       long page,
                                                       long pageSize) {
        IPage<ContactWayGroupPo> result = contactWayGroupMapper.selectPage(new Page<>(page, pageSize),
                new LambdaQueryWrapper<ContactWayGroupPo>()
                        .eq(ContactWayGroupPo::getExtCorpId, extCorpId)
                        .eq(id != null, ContactWayGroupPo::getId, id)
                        .likeRight(StringUtils.hasText(name), ContactWayGroupPo::getName, name)
                        .eq(isDefault != null, ContactWayGroupPo::getIsDefault, isDefault)
                        .orderByAsc(ContactWayGroupPo::getSortWeight)
                        .orderByDesc(ContactWayGroupPo::getCreatedAt));
        List<ContactWayGroupResponse> items = new ArrayList<>();
        for (ContactWayGroupPo group : result.getRecords()) {
            items.add(toResponse(group));
        }
        return new PageResponse<>(items, result.getTotal(), page, pageSize);
    }

    public ContactWayGroupResponse get(Long id, String extCorpId) {
        return toResponse(getRaw(id, extCorpId));
    }

    @Transactional(rollbackFor = Exception.class)
    public ContactWayGroupPo create(ContactWayGroupRequest request, String extCorpId, String extCreatorId) {
        if (!StringUtils.hasText(request.getName())) {
            throw new BizException(ErrorCode.BAD_REQUEST);
        }
        ContactWayGroupPo item = new ContactWayGroupPo();
        item.setId(idGenerator.nextId());
        item.setExtCorpId(extCorpId);
        item.setExtCreatorId(extCreatorId);
        item.setName(request.getName());
        item.setSortWeight(request.getSortWeight() == null ? 1000L : request.getSortWeight());
        item.setCount(0L);
        item.setIsDefault(FALSE);
        item.setCreatedAt(LocalDateTime.now());
        item.setUpdatedAt(LocalDateTime.now());
        contactWayGroupMapper.insert(item);
        return item;
    }

    @Transactional(rollbackFor = Exception.class)
    public ContactWayGroupPo update(Long id, ContactWayGroupRequest request, String extCorpId) {
        ContactWayGroupPo item = getRaw(id, extCorpId);
        if (StringUtils.hasText(request.getName())) {
            item.setName(request.getName());
        }
        if (request.getSortWeight() != null) {
            item.setSortWeight(request.getSortWeight());
        }
        item.setUpdatedAt(LocalDateTime.now());
        contactWayGroupMapper.updateById(item);
        return item;
    }

    @Transactional(rollbackFor = Exception.class)
    public long delete(ContactWayGroupDeleteRequest request, String extCorpId) {
        if (request == null || CollectionUtils.isEmpty(request.getIds())) {
            throw new BizException(ErrorCode.BAD_REQUEST);
        }
        return contactWayGroupMapper.delete(new LambdaQueryWrapper<ContactWayGroupPo>()
                .eq(ContactWayGroupPo::getExtCorpId, extCorpId)
                .in(ContactWayGroupPo::getId, request.getIds()));
    }

    private ContactWayGroupPo getRaw(Long id, String extCorpId) {
        ContactWayGroupPo item = contactWayGroupMapper.selectOne(new LambdaQueryWrapper<ContactWayGroupPo>()
                .eq(ContactWayGroupPo::getExtCorpId, extCorpId)
                .eq(ContactWayGroupPo::getId, id)
                .last("limit 1"));
        if (item == null) {
            throw new BizException(ErrorCode.ITEM_NOT_FOUND);
        }
        return item;
    }

    private ContactWayGroupResponse toResponse(ContactWayGroupPo group) {
        Long count = contactWayMapper.selectCount(new LambdaQueryWrapper<ContactWayPo>()
                .eq(ContactWayPo::getExtCorpId, group.getExtCorpId())
                .eq(ContactWayPo::getGroupId, group.getId()));
        return new ContactWayGroupResponse(group, count);
    }
}
