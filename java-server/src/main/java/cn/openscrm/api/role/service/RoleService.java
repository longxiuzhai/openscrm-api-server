package cn.openscrm.api.role.service;

import cn.openscrm.api.common.api.PageResponse;
import cn.openscrm.api.common.constant.AppEnv;
import cn.openscrm.api.common.constant.BooleanFlag;
import cn.openscrm.api.common.constant.RoleType;
import cn.openscrm.api.common.exception.BizException;
import cn.openscrm.api.common.exception.ErrorCode;
import cn.openscrm.api.common.id.SnowflakeIdGenerator;
import cn.openscrm.api.config.OpenScrmProperties;
import cn.openscrm.api.persistence.entity.PermissionPo;
import cn.openscrm.api.persistence.entity.RolePo;
import cn.openscrm.api.persistence.entity.StaffPo;
import cn.openscrm.api.persistence.mapper.PermissionPoMapper;
import cn.openscrm.api.persistence.mapper.RolePoMapper;
import cn.openscrm.api.persistence.mapper.StaffPoMapper;
import cn.openscrm.api.role.dto.AssignRoleRequest;
import cn.openscrm.api.role.dto.RoleRequest;
import cn.openscrm.api.role.dto.RoleResponse;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

@Service
public class RoleService {

    private final OpenScrmProperties properties;
    private final RolePoMapper roleMapper;
    private final PermissionPoMapper permissionMapper;
    private final StaffPoMapper staffMapper;
    private final SnowflakeIdGenerator idGenerator;
    private final ObjectMapper objectMapper;

    public RoleService(OpenScrmProperties properties,
                       RolePoMapper roleMapper,
                       PermissionPoMapper permissionMapper,
                       StaffPoMapper staffMapper,
                       SnowflakeIdGenerator idGenerator,
                       ObjectMapper objectMapper) {
        this.properties = properties;
        this.roleMapper = roleMapper;
        this.permissionMapper = permissionMapper;
        this.staffMapper = staffMapper;
        this.idGenerator = idGenerator;
        this.objectMapper = objectMapper;
    }

    public PageResponse<RoleResponse> query(String extCorpId,
                                            Long id,
                                            String name,
                                            String type,
                                            Integer isDefault,
                                            long page,
                                            long pageSize) {
        LambdaQueryWrapper<RolePo> wrapper = new LambdaQueryWrapper<RolePo>()
                .eq(RolePo::getExtCorpId, extCorpId)
                .eq(id != null, RolePo::getId, id)
                .eq(StringUtils.hasText(type), RolePo::getType, type)
                .eq(isDefault != null, RolePo::getIsDefault, isDefault)
                .likeRight(StringUtils.hasText(name), RolePo::getName, name)
                .orderByAsc(RolePo::getSortWeight)
                .orderByDesc(RolePo::getCreatedAt);
        IPage<RolePo> result = roleMapper.selectPage(new Page<>(page, pageSize), wrapper);
        Map<Long, Long> counts = countStaffs(extCorpId);
        List<RoleResponse> records = new ArrayList<>();
        for (RolePo role : result.getRecords()) {
            RoleResponse response = new RoleResponse();
            response.setRole(role);
            response.setCount(counts.getOrDefault(role.getId(), 0L));
            records.add(response);
        }
        return new PageResponse<>(records, result.getTotal(), page, pageSize);
    }

    public RoleResponse get(Long id) {
        RolePo role = roleMapper.selectById(id);
        if (role == null) {
            throw new BizException(ErrorCode.ITEM_NOT_FOUND);
        }
        RoleResponse response = new RoleResponse();
        response.setRole(role);
        response.setPermissions(getPermissions(parsePermissionIds(role.getPermissionIds())));
        return response;
    }

    public RolePo create(String extCorpId, RoleRequest request) {
        assertMutableEnv();
        if (!StringUtils.hasText(request.getName())) {
            throw new BizException(ErrorCode.BAD_REQUEST);
        }
        RolePo item = new RolePo();
        item.setId(idGenerator.nextId());
        item.setExtCorpId(extCorpId);
        item.setName(request.getName());
        item.setDescription(request.getDescription());
        item.setType(RoleType.STAFF);
        item.setSortWeight(request.getSortWeight() == null ? 1000L : request.getSortWeight());
        item.setIsDefault(BooleanFlag.FALSE);
        item.setPermissionIds(toJson(request.getPermissionIds()));
        roleMapper.insert(item);
        return item;
    }

    public RolePo update(Long id, RoleRequest request) {
        assertMutableEnv();
        RolePo item = roleMapper.selectById(id);
        if (item == null) {
            throw new BizException(ErrorCode.ITEM_NOT_FOUND);
        }
        if (item.getIsDefault() != null && item.getIsDefault() == BooleanFlag.TRUE) {
            throw new BizException(ErrorCode.DO_NOT_UPDATE_DEFAULT_ROLE);
        }
        if (StringUtils.hasText(request.getName())) {
            item.setName(request.getName());
        }
        item.setDescription(request.getDescription());
        if (request.getSortWeight() != null) {
            item.setSortWeight(request.getSortWeight());
        }
        if (request.getPermissionIds() != null) {
            item.setPermissionIds(toJson(request.getPermissionIds()));
        }
        roleMapper.updateById(item);
        return item;
    }

    @Transactional(rollbackFor = Exception.class)
    public long assignToStaffs(AssignRoleRequest request) {
        assertMutableEnv();
        if (request.getRoleId() == null || CollectionUtils.isEmpty(request.getExtStaffIds())) {
            throw new BizException(ErrorCode.BAD_REQUEST);
        }
        RolePo role = roleMapper.selectById(request.getRoleId());
        if (role == null) {
            throw new BizException(ErrorCode.ITEM_NOT_FOUND);
        }
        return staffMapper.update(null, new LambdaUpdateWrapper<StaffPo>()
                .eq(StaffPo::getExtCorpId, role.getExtCorpId())
                .in(StaffPo::getExtId, request.getExtStaffIds())
                .set(StaffPo::getRoleId, role.getId())
                .set(StaffPo::getRoleType, role.getType()));
    }

    public PageResponse<StaffPo> queryStaffs(String extCorpId,
                                             Long staffId,
                                             String extStaffId,
                                             String name,
                                             Long roleId,
                                             String roleType,
                                             long page,
                                             long pageSize) {
        LambdaQueryWrapper<StaffPo> wrapper = new LambdaQueryWrapper<StaffPo>()
                .eq(StaffPo::getExtCorpId, extCorpId)
                .eq(staffId != null, StaffPo::getId, staffId)
                .eq(StringUtils.hasText(extStaffId), StaffPo::getExtId, extStaffId)
                .eq(roleId != null, StaffPo::getRoleId, roleId)
                .eq(StringUtils.hasText(roleType), StaffPo::getRoleType, roleType)
                .likeRight(StringUtils.hasText(name), StaffPo::getName, name)
                .orderByDesc(StaffPo::getCreatedAt);
        IPage<StaffPo> result = staffMapper.selectPage(new Page<>(page, pageSize), wrapper);
        return new PageResponse<>(result.getRecords(), result.getTotal(), page, pageSize);
    }

    private Map<Long, Long> countStaffs(String extCorpId) {
        List<StaffPo> staffs = staffMapper.selectList(new LambdaQueryWrapper<StaffPo>()
                .eq(StaffPo::getExtCorpId, extCorpId)
                .isNotNull(StaffPo::getRoleId));
        Map<Long, Long> counts = new HashMap<>();
        for (StaffPo staff : staffs) {
            counts.put(staff.getRoleId(), counts.getOrDefault(staff.getRoleId(), 0L) + 1);
        }
        return counts;
    }

    private List<PermissionPo> getPermissions(List<String> identities) {
        if (identities.isEmpty()) {
            return Collections.emptyList();
        }
        return permissionMapper.selectList(new LambdaQueryWrapper<PermissionPo>()
                .in(PermissionPo::getIdentity, identities));
    }

    private List<String> parsePermissionIds(String json) {
        if (!StringUtils.hasText(json)) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {
            });
        } catch (JsonProcessingException e) {
            throw new BizException(ErrorCode.INTERNAL_ERROR, "invalid role permission ids");
        }
    }

    private String toJson(List<String> values) {
        try {
            return objectMapper.writeValueAsString(values == null ? Collections.emptyList() : values);
        } catch (JsonProcessingException e) {
            throw new BizException(ErrorCode.INTERNAL_ERROR);
        }
    }

    private void assertMutableEnv() {
        if (AppEnv.DEMO.name().equalsIgnoreCase(properties.getEnv())) {
            throw new BizException(ErrorCode.FORBIDDEN);
        }
    }
}
