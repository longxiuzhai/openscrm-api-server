package cn.openscrm.api.internaltag.service;

import cn.openscrm.api.common.api.PageResponse;
import cn.openscrm.api.common.exception.BizException;
import cn.openscrm.api.common.exception.ErrorCode;
import cn.openscrm.api.common.id.SnowflakeIdGenerator;
import cn.openscrm.api.internaltag.dto.InternalTagCreateRequest;
import cn.openscrm.api.internaltag.dto.InternalTagDeleteRequest;
import cn.openscrm.api.persistence.entity.InternalTagPo;
import cn.openscrm.api.persistence.mapper.InternalTagPoMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class InternalTagService {

    private final InternalTagPoMapper internalTagMapper;
    private final SnowflakeIdGenerator idGenerator;

    public List<InternalTagPo> create(InternalTagCreateRequest request, String extCorpId, String extCreatorId) {
        if (request == null || CollectionUtils.isEmpty(request.getNames())) {
            throw new BizException(ErrorCode.BAD_REQUEST);
        }
        List<InternalTagPo> tags = new ArrayList<>();
        for (String name : request.getNames()) {
            if (!StringUtils.hasText(name)) {
                continue;
            }
            InternalTagPo tag = new InternalTagPo();
            tag.setId(idGenerator.nextId());
            tag.setExtCorpId(extCorpId);
            tag.setExtCreatorId(extCreatorId);
            tag.setName(name.trim());
            internalTagMapper.insert(tag);
            tags.add(tag);
        }
        if (tags.isEmpty()) {
            throw new BizException(ErrorCode.BAD_REQUEST);
        }
        return tags;
    }

    public PageResponse<InternalTagPo> query(String extCorpId, long page, long pageSize) {
        IPage<InternalTagPo> result = internalTagMapper.selectPage(Page.of(page, pageSize),
                new LambdaQueryWrapper<InternalTagPo>()
                        .eq(InternalTagPo::getExtCorpId, extCorpId)
                        .isNull(InternalTagPo::getDeletedAt)
                        .orderByDesc(InternalTagPo::getCreatedAt));
        return new PageResponse<>(result.getRecords(), result.getTotal(), page, pageSize);
    }

    public long delete(InternalTagDeleteRequest request, String extCorpId) {
        if (request == null || CollectionUtils.isEmpty(request.getIds())) {
            throw new BizException(ErrorCode.BAD_REQUEST);
        }
        return internalTagMapper.update(null, new UpdateWrapper<InternalTagPo>()
                .eq("ext_corp_id", extCorpId)
                .in("id", request.getIds())
                .isNull("deleted_at")
                .set("deleted_at", LocalDateTime.now()));
    }
}
