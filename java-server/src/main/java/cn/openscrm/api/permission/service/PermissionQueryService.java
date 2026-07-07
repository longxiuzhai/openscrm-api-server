package cn.openscrm.api.permission.service;

import cn.openscrm.api.common.api.PageResponse;
import cn.openscrm.api.common.exception.BizException;
import cn.openscrm.api.common.exception.ErrorCode;
import cn.openscrm.api.persistence.entity.PermissionPo;
import cn.openscrm.api.persistence.mapper.PermissionPoMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class PermissionQueryService {

    private final PermissionPoMapper permissionMapper;

    public PermissionQueryService(PermissionPoMapper permissionMapper) {
        this.permissionMapper = permissionMapper;
    }

    public PageResponse<PermissionPo> query(Long id,
                                            String name,
                                            String bizIdentity,
                                            String operation,
                                            String identity,
                                            long page,
                                            long pageSize) {
        LambdaQueryWrapper<PermissionPo> wrapper = new LambdaQueryWrapper<PermissionPo>()
                .eq(id != null, PermissionPo::getId, id)
                .likeRight(StringUtils.hasText(name), PermissionPo::getName, name)
                .eq(StringUtils.hasText(bizIdentity), PermissionPo::getBizIdentity, bizIdentity)
                .eq(StringUtils.hasText(operation), PermissionPo::getOperation, operation)
                .eq(StringUtils.hasText(identity), PermissionPo::getIdentity, identity)
                .orderByAsc(PermissionPo::getSortWeight)
                .orderByDesc(PermissionPo::getCreatedAt);
        IPage<PermissionPo> result = permissionMapper.selectPage(new Page<>(page, pageSize), wrapper);
        return new PageResponse<>(result.getRecords(), result.getTotal(), page, pageSize);
    }

    public PermissionPo get(Long id) {
        PermissionPo item = permissionMapper.selectById(id);
        if (item == null) {
            throw new BizException(ErrorCode.ITEM_NOT_FOUND);
        }
        return item;
    }
}
