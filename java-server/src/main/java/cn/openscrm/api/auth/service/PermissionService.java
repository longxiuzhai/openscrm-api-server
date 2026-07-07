package cn.openscrm.api.auth.service;

import cn.openscrm.api.common.exception.BizException;
import cn.openscrm.api.common.exception.ErrorCode;
import cn.openscrm.api.persistence.entity.RolePo;
import cn.openscrm.api.persistence.entity.StaffPo;
import cn.openscrm.api.persistence.mapper.RolePoMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class PermissionService {

    private final RolePoMapper roleMapper;
    private final ObjectMapper objectMapper;

    public PermissionService(RolePoMapper roleMapper, ObjectMapper objectMapper) {
        this.roleMapper = roleMapper;
        this.objectMapper = objectMapper;
    }

    public void require(StaffPo staff, String biz, String operation) {
        if (staff == null || staff.getRoleId() == null) {
            throw new BizException(ErrorCode.FORBIDDEN);
        }
        RolePo role = roleMapper.selectById(staff.getRoleId());
        if (role == null) {
            throw new BizException(ErrorCode.FORBIDDEN);
        }
        String identity = biz + "_" + operation;
        if (!permissionIds(role).contains(identity)) {
            throw new BizException(ErrorCode.FORBIDDEN);
        }
    }

    private List<String> permissionIds(RolePo role) {
        if (!StringUtils.hasText(role.getPermissionIds())) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(role.getPermissionIds(), new TypeReference<List<String>>() {
            });
        } catch (Exception e) {
            throw new BizException(ErrorCode.INTERNAL_ERROR, "invalid role permission ids");
        }
    }
}
