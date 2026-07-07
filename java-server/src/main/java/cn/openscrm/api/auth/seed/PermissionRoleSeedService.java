package cn.openscrm.api.auth.seed;

import cn.openscrm.api.common.constant.BooleanFlag;
import cn.openscrm.api.common.constant.RoleType;
import cn.openscrm.api.common.constant.SeedIds;
import cn.openscrm.api.common.id.SnowflakeIdGenerator;
import cn.openscrm.api.config.OpenScrmProperties;
import cn.openscrm.api.persistence.entity.PermissionPo;
import cn.openscrm.api.persistence.entity.RolePo;
import cn.openscrm.api.persistence.mapper.PermissionPoMapper;
import cn.openscrm.api.persistence.mapper.RolePoMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class PermissionRoleSeedService {

    private final OpenScrmProperties properties;
    private final PermissionPoMapper permissionMapper;
    private final RolePoMapper roleMapper;
    private final SnowflakeIdGenerator idGenerator;
    private final ObjectMapper objectMapper;

    public PermissionRoleSeedService(OpenScrmProperties properties,
                                     PermissionPoMapper permissionMapper,
                                     RolePoMapper roleMapper,
                                     SnowflakeIdGenerator idGenerator,
                                     ObjectMapper objectMapper) {
        this.properties = properties;
        this.permissionMapper = permissionMapper;
        this.roleMapper = roleMapper;
        this.idGenerator = idGenerator;
        this.objectMapper = objectMapper;
    }

    public void seed() {
        seedPermissions();
        seedRoles();
    }

    private void seedPermissions() {
        for (PermissionDefinition definition : PermissionDefinitions.allPermissions()) {
            PermissionPo existing = permissionMapper.selectOne(new LambdaQueryWrapper<PermissionPo>()
                    .eq(PermissionPo::getIdentity, definition.identity())
                    .last("limit 1"));
            PermissionPo item = existing == null ? new PermissionPo() : existing;
            if (item.getId() == null) {
                item.setId(idGenerator.nextId());
            }
            item.setName(definition.getName());
            item.setDescription(definition.getName());
            item.setBizIdentity(definition.getBiz());
            item.setOperation(definition.getOperation());
            item.setIdentity(definition.identity());
            item.setSortWeight(1000L);
            if (existing == null) {
                permissionMapper.insert(item);
            } else {
                permissionMapper.updateById(item);
            }
        }
    }

    private void seedRoles() {
        upsertRole(SeedIds.DEFAULT_CORP_STAFF_ROLE_ID, "员工", RoleType.STAFF,
                PermissionDefinitions.identities(PermissionDefinitions.staffPermissions()), 10000L);
        upsertRole(SeedIds.DEFAULT_CORP_DEPARTMENT_ADMIN_ROLE_ID, "部门管理员", RoleType.DEPARTMENT_ADMIN,
                PermissionDefinitions.identities(PermissionDefinitions.departmentAdminPermissions()), 10001L);
        upsertRole(SeedIds.DEFAULT_CORP_ADMIN_ROLE_ID, "管理员", RoleType.ADMIN,
                PermissionDefinitions.identities(PermissionDefinitions.adminPermissions()), 10002L);
        upsertRole(SeedIds.DEFAULT_CORP_SUPER_ADMIN_ROLE_ID, "超级管理员", RoleType.SUPER_ADMIN,
                PermissionDefinitions.identities(PermissionDefinitions.superAdminPermissions()), 10003L);
    }

    private void upsertRole(Long id, String name, String type, List<String> permissionIds, Long sortWeight) {
        RolePo item = roleMapper.selectById(id);
        if (item == null) {
            item = new RolePo();
            item.setId(id);
        }
        item.setExtCorpId(properties.getWeWork().getExtCorpId());
        item.setName(name);
        item.setDescription(name);
        item.setType(type);
        item.setIsDefault(BooleanFlag.TRUE);
        item.setSortWeight(sortWeight);
        item.setPermissionIds(toJson(permissionIds));
        if (roleMapper.selectById(id) == null) {
            roleMapper.insert(item);
        } else {
            roleMapper.updateById(item);
        }
    }

    private String toJson(List<String> values) {
        try {
            return objectMapper.writeValueAsString(values);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }
}
