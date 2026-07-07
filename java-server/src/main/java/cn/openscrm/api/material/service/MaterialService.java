package cn.openscrm.api.material.service;

import cn.openscrm.api.common.api.PageResponse;
import cn.openscrm.api.common.exception.BizException;
import cn.openscrm.api.common.exception.ErrorCode;
import cn.openscrm.api.common.id.SnowflakeIdGenerator;
import cn.openscrm.api.material.dto.MaterialDeleteRequest;
import cn.openscrm.api.material.dto.MaterialRequest;
import cn.openscrm.api.material.dto.MaterialResponse;
import cn.openscrm.api.material.dto.SidebarStatusResponse;
import cn.openscrm.api.persistence.entity.CorpSettingPo;
import cn.openscrm.api.persistence.entity.MaterialLibTagPo;
import cn.openscrm.api.persistence.entity.MaterialPo;
import cn.openscrm.api.persistence.mapper.CorpSettingPoMapper;
import cn.openscrm.api.persistence.mapper.MaterialLibTagPoMapper;
import cn.openscrm.api.persistence.mapper.MaterialPoMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
public class MaterialService {

    private final MaterialPoMapper materialMapper;
    private final MaterialLibTagPoMapper materialLibTagMapper;
    private final CorpSettingPoMapper corpSettingMapper;
    private final SnowflakeIdGenerator idGenerator;
    private final ObjectMapper objectMapper;

    @Transactional
    public MaterialPo create(MaterialRequest request, String extCorpId, String extCreatorId) {
        LocalDateTime now = LocalDateTime.now();
        MaterialPo item = new MaterialPo();
        item.setId(idGenerator.nextId());
        item.setExtCorpId(extCorpId);
        item.setExtCreatorId(extCreatorId);
        item.setMaterialType(request.getMaterialType());
        item.setTitle(request.getTitle());
        item.setFileUrl(request.getFileUrl());
        item.setFileSize(request.getFileSize());
        item.setLink(request.getLink());
        item.setDigest(request.getDigest());
        item.setMaterialTagList(writeTagList(request.getMaterialTagList()));
        item.setCreatedAt(now);
        item.setUpdatedAt(now);
        materialMapper.insert(item);
        return item;
    }

    @Transactional
    public MaterialPo update(Long id, MaterialRequest request, String extCorpId) {
        MaterialPo item = materialMapper.selectOne(new LambdaQueryWrapper<MaterialPo>()
                .eq(MaterialPo::getId, id)
                .eq(MaterialPo::getExtCorpId, extCorpId)
                .isNull(MaterialPo::getDeletedAt));
        if (item == null) {
            throw new BizException(ErrorCode.ITEM_NOT_FOUND);
        }
        if (StringUtils.hasText(request.getTitle())) {
            item.setTitle(request.getTitle());
        }
        if (StringUtils.hasText(request.getMaterialType())) {
            item.setMaterialType(request.getMaterialType());
        }
        if (StringUtils.hasText(request.getFileUrl())) {
            item.setFileUrl(request.getFileUrl());
        }
        if (request.getFileSize() != null) {
            item.setFileSize(request.getFileSize());
        }
        if (StringUtils.hasText(request.getLink())) {
            item.setLink(request.getLink());
        }
        if (StringUtils.hasText(request.getDigest())) {
            item.setDigest(request.getDigest());
        }
        if (request.getMaterialTagList() != null) {
            item.setMaterialTagList(writeTagList(request.getMaterialTagList()));
        }
        item.setUpdatedAt(LocalDateTime.now());
        materialMapper.updateById(item);
        return item;
    }

    public PageResponse<MaterialResponse> query(String extCorpId,
                                                String title,
                                                String materialType,
                                                List<String> materialTagList,
                                                long page,
                                                long pageSize) {
        LambdaQueryWrapper<MaterialPo> wrapper = new LambdaQueryWrapper<MaterialPo>()
                .eq(MaterialPo::getExtCorpId, extCorpId)
                .isNull(MaterialPo::getDeletedAt)
                .eq(StringUtils.hasText(materialType), MaterialPo::getMaterialType, materialType)
                .likeRight(StringUtils.hasText(title), MaterialPo::getTitle, title)
                .orderByDesc(MaterialPo::getCreatedAt);
        if (!CollectionUtils.isEmpty(materialTagList)) {
            wrapper.and(w -> {
                for (String tagId : materialTagList) {
                    w.or().apply("JSON_CONTAINS(material_tag_list, JSON_ARRAY({0}))", tagId);
                }
            });
        }
        Page<MaterialPo> result = materialMapper.selectPage(new Page<>(page, pageSize), wrapper);
        return new PageResponse<>(attachTags(result.getRecords()), result.getTotal(), page, pageSize);
    }

    @Transactional
    public int delete(MaterialDeleteRequest request, String extCorpId) {
        if (request == null || CollectionUtils.isEmpty(request.getIds())) {
            return 0;
        }
        MaterialPo update = new MaterialPo();
        update.setDeletedAt(LocalDateTime.now());
        update.setUpdatedAt(LocalDateTime.now());
        return materialMapper.update(update, new LambdaQueryWrapper<MaterialPo>()
                .eq(MaterialPo::getExtCorpId, extCorpId)
                .in(MaterialPo::getId, request.getIds())
                .isNull(MaterialPo::getDeletedAt));
    }

    public SidebarStatusResponse getSidebarStatus(String extCorpId) {
        CorpSettingPo setting = getCorpSetting(extCorpId);
        return new SidebarStatusResponse(setting != null && Integer.valueOf(1).equals(setting.getIsMaterialUsed()));
    }

    @Transactional
    public SidebarStatusResponse updateSidebarStatus(String extCorpId, String extCreatorId, Boolean status) {
        boolean enabled = Boolean.TRUE.equals(status);
        LocalDateTime now = LocalDateTime.now();
        CorpSettingPo setting = getCorpSetting(extCorpId);
        if (setting == null) {
            setting = new CorpSettingPo();
            setting.setId(idGenerator.nextId());
            setting.setExtCorpId(extCorpId);
            setting.setExtCreatorId(extCreatorId);
            setting.setCreatedAt(now);
            setting.setUpdatedAt(now);
            setting.setIsMaterialUsed(enabled ? 1 : 0);
            corpSettingMapper.insert(setting);
        } else {
            setting.setIsMaterialUsed(enabled ? 1 : 0);
            setting.setUpdatedAt(now);
            corpSettingMapper.updateById(setting);
        }
        return new SidebarStatusResponse(enabled);
    }

    public List<String> split(List<String> values) {
        if (CollectionUtils.isEmpty(values)) {
            return Collections.emptyList();
        }
        List<String> result = new ArrayList<>();
        for (String value : values) {
            if (!StringUtils.hasText(value)) {
                continue;
            }
            for (String item : value.split(",")) {
                if (StringUtils.hasText(item)) {
                    result.add(item.trim());
                }
            }
        }
        return result;
    }

    private CorpSettingPo getCorpSetting(String extCorpId) {
        return corpSettingMapper.selectOne(new LambdaQueryWrapper<CorpSettingPo>()
                .eq(CorpSettingPo::getExtCorpId, extCorpId)
                .isNull(CorpSettingPo::getDeletedAt)
                .last("limit 1"));
    }

    private List<MaterialResponse> attachTags(List<MaterialPo> materials) {
        if (CollectionUtils.isEmpty(materials)) {
            return Collections.emptyList();
        }
        List<Long> tagIds = materials.stream()
                .flatMap(item -> readTagList(item.getMaterialTagList()).stream())
                .map(this::parseLong)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, MaterialLibTagPo> tagMap = CollectionUtils.isEmpty(tagIds)
                ? Collections.emptyMap()
                : materialLibTagMapper.selectBatchIds(tagIds).stream()
                        .collect(Collectors.toMap(MaterialLibTagPo::getId, item -> item, (a, b) -> a, LinkedHashMap::new));
        return materials.stream()
                .map(item -> new MaterialResponse(item, readTagList(item.getMaterialTagList()).stream()
                        .map(this::parseLong)
                        .map(tagMap::get)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList())))
                .collect(Collectors.toList());
    }

    private String writeTagList(List<String> value) {
        try {
            return objectMapper.writeValueAsString(value == null ? Collections.emptyList() : value);
        } catch (JsonProcessingException e) {
            throw new BizException(ErrorCode.INTERNAL_ERROR, "序列化素材标签失败");
        }
    }

    private List<String> readTagList(String value) {
        if (!StringUtils.hasText(value)) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(value, new TypeReference<List<String>>() {
            });
        } catch (JsonProcessingException e) {
            return Collections.emptyList();
        }
    }

    private Long parseLong(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
