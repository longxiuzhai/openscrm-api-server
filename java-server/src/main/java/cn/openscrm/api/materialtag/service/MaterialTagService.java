package cn.openscrm.api.materialtag.service;

import cn.openscrm.api.common.api.PageResponse;
import cn.openscrm.api.common.id.SnowflakeIdGenerator;
import cn.openscrm.api.materialtag.dto.MaterialTagCreateRequest;
import cn.openscrm.api.materialtag.dto.MaterialTagDeleteRequest;
import cn.openscrm.api.persistence.entity.MaterialLibTagPo;
import cn.openscrm.api.persistence.mapper.MaterialLibTagPoMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class MaterialTagService {

    private final MaterialLibTagPoMapper mapper;
    private final SnowflakeIdGenerator idGenerator;

    public MaterialTagService(MaterialLibTagPoMapper mapper, SnowflakeIdGenerator idGenerator) {
        this.mapper = mapper;
        this.idGenerator = idGenerator;
    }

    @Transactional(rollbackFor = Exception.class)
    public List<MaterialLibTagPo> create(MaterialTagCreateRequest request, String extCorpId, String extCreatorId) {
        List<MaterialLibTagPo> tags = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        for (String name : request.getNames()) {
            if (!StringUtils.hasText(name)) {
                continue;
            }
            MaterialLibTagPo tag = new MaterialLibTagPo();
            tag.setId(idGenerator.nextId());
            tag.setExtCorpId(extCorpId);
            tag.setExtCreatorId(extCreatorId);
            tag.setName(name.trim());
            tag.setCreatedAt(now);
            tag.setUpdatedAt(now);
            mapper.insert(tag);
            tags.add(tag);
        }
        return tags;
    }

    public PageResponse<MaterialLibTagPo> query(String extCorpId, String name, long page, long pageSize) {
        QueryWrapper<MaterialLibTagPo> wrapper = new QueryWrapper<MaterialLibTagPo>()
                .eq("ext_corp_id", extCorpId)
                .isNull("deleted_at")
                .orderByDesc("created_at");
        if (StringUtils.hasText(name)) {
            wrapper.likeRight("name", name);
        }
        Page<MaterialLibTagPo> result = mapper.selectPage(Page.of(page, pageSize), wrapper);
        return new PageResponse<>(result.getRecords(), result.getTotal(), page, pageSize);
    }

    @Transactional(rollbackFor = Exception.class)
    public long delete(MaterialTagDeleteRequest request) {
        if (request.getIds() == null || request.getIds().isEmpty()) {
            return 0;
        }
        return mapper.update(null, new UpdateWrapper<MaterialLibTagPo>()
                .in("id", request.getIds())
                .isNull("deleted_at")
                .set("deleted_at", LocalDateTime.now())
                .set("updated_at", LocalDateTime.now()));
    }
}
