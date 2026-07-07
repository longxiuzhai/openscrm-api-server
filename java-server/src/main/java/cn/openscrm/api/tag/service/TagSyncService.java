package cn.openscrm.api.tag.service;

import cn.openscrm.api.common.id.SnowflakeIdGenerator;
import cn.openscrm.api.config.OpenScrmProperties;
import cn.openscrm.api.persistence.entity.CustomerStaffTagPo;
import cn.openscrm.api.persistence.entity.TagGroupPo;
import cn.openscrm.api.persistence.entity.TagPo;
import cn.openscrm.api.persistence.mapper.CustomerStaffTagPoMapper;
import cn.openscrm.api.persistence.mapper.TagGroupPoMapper;
import cn.openscrm.api.persistence.mapper.TagPoMapper;
import cn.openscrm.api.wework.ExternalContactCorpTag;
import cn.openscrm.api.wework.ExternalContactCorpTagGroup;
import cn.openscrm.api.wework.ExternalContactCorpTagListResponse;
import cn.openscrm.api.wework.WeWorkClient;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class TagSyncService {

    private final OpenScrmProperties properties;
    private final WeWorkClient weWorkClient;
    private final TagGroupPoMapper tagGroupMapper;
    private final TagPoMapper tagMapper;
    private final CustomerStaffTagPoMapper customerStaffTagMapper;
    private final SnowflakeIdGenerator idGenerator;

    public TagSyncService(OpenScrmProperties properties,
                          WeWorkClient weWorkClient,
                          TagGroupPoMapper tagGroupMapper,
                          TagPoMapper tagMapper,
                          CustomerStaffTagPoMapper customerStaffTagMapper,
                          SnowflakeIdGenerator idGenerator) {
        this.properties = properties;
        this.weWorkClient = weWorkClient;
        this.tagGroupMapper = tagGroupMapper;
        this.tagMapper = tagMapper;
        this.customerStaffTagMapper = customerStaffTagMapper;
        this.idGenerator = idGenerator;
    }

    @Transactional(rollbackFor = Exception.class)
    public void syncAll(String extCorpId) {
        ExternalContactCorpTagListResponse response = weWorkClient.listExternalContactCorpTags(
                extCorpId, properties.getWeWork().getCustomerSecret(), Collections.emptyList());
        upsertGroups(extCorpId, response.getTagGroup());
    }

    @Transactional(rollbackFor = Exception.class)
    public void syncTag(String extCorpId, String extTagId) {
        if (!StringUtils.hasText(extTagId)) {
            return;
        }
        ExternalContactCorpTagListResponse response = weWorkClient.listExternalContactCorpTags(
                extCorpId, properties.getWeWork().getCustomerSecret(), Collections.singletonList(extTagId));
        upsertGroups(extCorpId, response.getTagGroup());
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteTag(String extTagId) {
        if (!StringUtils.hasText(extTagId)) {
            return;
        }
        tagMapper.delete(new LambdaQueryWrapper<TagPo>().eq(TagPo::getExtId, extTagId));
        deleteCustomerStaffTags(Collections.singletonList(extTagId));
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteTagGroup(String extGroupId) {
        if (!StringUtils.hasText(extGroupId)) {
            return;
        }
        List<TagPo> tags = tagMapper.selectList(new LambdaQueryWrapper<TagPo>()
                .eq(TagPo::getExtGroupId, extGroupId));
        List<String> tagIds = new ArrayList<>();
        for (TagPo tag : tags) {
            tagIds.add(tag.getExtId());
        }
        tagGroupMapper.delete(new LambdaQueryWrapper<TagGroupPo>().eq(TagGroupPo::getExtId, extGroupId));
        tagMapper.delete(new LambdaQueryWrapper<TagPo>().eq(TagPo::getExtGroupId, extGroupId));
        deleteCustomerStaffTags(tagIds);
    }

    private void upsertGroups(String extCorpId, List<ExternalContactCorpTagGroup> groups) {
        if (groups == null) {
            return;
        }
        for (ExternalContactCorpTagGroup group : groups) {
            if (Boolean.TRUE.equals(group.getDeleted())) {
                deleteTagGroup(group.getGroupId());
                continue;
            }
            TagGroupPo groupPo = findGroup(group.getGroupId());
            if (groupPo == null) {
                groupPo = new TagGroupPo();
                groupPo.setId(idGenerator.nextId());
            }
            groupPo.setExtCorpId(extCorpId);
            groupPo.setExtId(group.getGroupId());
            groupPo.setName(group.getGroupName());
            groupPo.setCreateTime(group.getCreateTime());
            groupPo.setOrder(group.getOrder());
            groupPo.setDepartmentList(defaultDepartmentList(groupPo.getDepartmentList()));
            if (tagGroupMapper.selectById(groupPo.getId()) == null) {
                tagGroupMapper.insert(groupPo);
            } else {
                tagGroupMapper.updateById(groupPo);
            }
            upsertTags(extCorpId, group);
        }
    }

    private void upsertTags(String extCorpId, ExternalContactCorpTagGroup group) {
        if (group.getTag() == null) {
            return;
        }
        for (ExternalContactCorpTag source : group.getTag()) {
            if (Boolean.TRUE.equals(source.getDeleted())) {
                deleteTag(source.getId());
                continue;
            }
            TagPo tag = findTag(source.getId());
            if (tag == null) {
                tag = new TagPo();
                tag.setId(idGenerator.nextId());
            }
            tag.setExtCorpId(extCorpId);
            tag.setExtId(source.getId());
            tag.setExtGroupId(group.getGroupId());
            tag.setName(source.getName());
            tag.setGroupName(group.getGroupName());
            tag.setCreateTime(source.getCreateTime());
            tag.setOrder(source.getOrder());
            tag.setType(1);
            if (tagMapper.selectById(tag.getId()) == null) {
                tagMapper.insert(tag);
            } else {
                tagMapper.updateById(tag);
            }
        }
    }

    private TagGroupPo findGroup(String extGroupId) {
        return tagGroupMapper.selectOne(new LambdaQueryWrapper<TagGroupPo>()
                .eq(TagGroupPo::getExtId, extGroupId)
                .last("limit 1"));
    }

    private TagPo findTag(String extTagId) {
        return tagMapper.selectOne(new LambdaQueryWrapper<TagPo>()
                .eq(TagPo::getExtId, extTagId)
                .last("limit 1"));
    }

    private void deleteCustomerStaffTags(List<String> extTagIds) {
        if (extTagIds == null || extTagIds.isEmpty()) {
            return;
        }
        customerStaffTagMapper.update(null, new LambdaUpdateWrapper<CustomerStaffTagPo>()
                .in(CustomerStaffTagPo::getExtTagId, extTagIds)
                .isNull(CustomerStaffTagPo::getDeletedAt)
                .set(CustomerStaffTagPo::getDeletedAt, LocalDateTime.now()));
    }

    private String defaultDepartmentList(String current) {
        return StringUtils.hasText(current) ? current : "[0]";
    }
}
