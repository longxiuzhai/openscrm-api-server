package cn.openscrm.api.staff.service;

import cn.openscrm.api.common.api.PageResponse;
import cn.openscrm.api.common.constant.BooleanFlag;
import cn.openscrm.api.common.constant.RoleType;
import cn.openscrm.api.common.constant.SeedIds;
import cn.openscrm.api.common.id.SnowflakeIdGenerator;
import cn.openscrm.api.config.OpenScrmProperties;
import cn.openscrm.api.persistence.entity.DepartmentPo;
import cn.openscrm.api.persistence.entity.StaffDepartmentPo;
import cn.openscrm.api.persistence.entity.StaffPo;
import cn.openscrm.api.persistence.mapper.DepartmentPoMapper;
import cn.openscrm.api.persistence.mapper.StaffDepartmentPoMapper;
import cn.openscrm.api.persistence.mapper.StaffPoMapper;
import cn.openscrm.api.staff.dto.EnableStaffsRequest;
import cn.openscrm.api.wework.UserDetailResponse;
import cn.openscrm.api.wework.UserIdInfo;
import cn.openscrm.api.wework.UserIdListResponse;
import cn.openscrm.api.wework.WeWorkClient;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.ArrayList;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;

@Service
public class StaffService {

    private final StaffPoMapper staffMapper;
    private final StaffDepartmentPoMapper staffDepartmentMapper;
    private final DepartmentPoMapper departmentMapper;
    private final WeWorkClient weWorkClient;
    private final OpenScrmProperties properties;
    private final SnowflakeIdGenerator idGenerator;
    private final ObjectMapper objectMapper;

    public StaffService(StaffPoMapper staffMapper,
                        StaffDepartmentPoMapper staffDepartmentMapper,
                        DepartmentPoMapper departmentMapper,
                        WeWorkClient weWorkClient,
                        OpenScrmProperties properties,
                        SnowflakeIdGenerator idGenerator,
                        ObjectMapper objectMapper) {
        this.staffMapper = staffMapper;
        this.staffDepartmentMapper = staffDepartmentMapper;
        this.departmentMapper = departmentMapper;
        this.weWorkClient = weWorkClient;
        this.properties = properties;
        this.idGenerator = idGenerator;
        this.objectMapper = objectMapper;
    }

    public void sync(String extCorpId) {
        UserIdListResponse response = weWorkClient.listUserIds(extCorpId, properties.getWeWork().getContactSecret());
        for (UserIdInfo item : response.getDeptUser()) {
            UserDetailResponse detail = weWorkClient.getUser(extCorpId, properties.getWeWork().getContactSecret(), item.getUserId());
            upsertStaff(extCorpId, item, detail);
            upsertStaffDepartment(extCorpId, item);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void syncOne(String extCorpId, String extStaffId) {
        UserDetailResponse detail = weWorkClient.getUser(extCorpId, properties.getWeWork().getContactSecret(), extStaffId);
        List<Long> departments = detail.getDepartments() == null || detail.getDepartments().isEmpty()
                ? Collections.singletonList(1L) : detail.getDepartments();
        List<Integer> activeDepartmentIds = new ArrayList<>();
        for (Long departmentId : departments) {
            activeDepartmentIds.add(departmentId.intValue());
            UserIdInfo item = new UserIdInfo();
            item.setUserId(extStaffId);
            item.setDepartmentId(departmentId);
            upsertStaff(extCorpId, item, detail);
            upsertStaffDepartment(extCorpId, item);
        }
        deleteAbsentStaffDepartments(extCorpId, extStaffId, activeDepartmentIds);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteOne(String extCorpId, String extStaffId) {
        staffMapper.update(null, new LambdaUpdateWrapper<StaffPo>()
                .eq(StaffPo::getExtCorpId, extCorpId)
                .eq(StaffPo::getExtId, extStaffId)
                .isNull(StaffPo::getDeletedAt)
                .set(StaffPo::getDeletedAt, LocalDateTime.now())
                .set(StaffPo::getEnable, BooleanFlag.FALSE)
                .set(StaffPo::getStatus, 5));
        staffDepartmentMapper.delete(new LambdaQueryWrapper<StaffDepartmentPo>()
                .eq(StaffDepartmentPo::getExtCorpId, extCorpId)
                .eq(StaffDepartmentPo::getExtStaffId, extStaffId));
    }

    public PageResponse<StaffPo> query(String extCorpId, String name, String roleType, Long roleId, long page, long pageSize) {
        LambdaQueryWrapper<StaffPo> wrapper = new LambdaQueryWrapper<StaffPo>()
                .eq(StaffPo::getExtCorpId, extCorpId)
                .like(StringUtils.hasText(name), StaffPo::getName, name)
                .eq(StringUtils.hasText(roleType), StaffPo::getRoleType, roleType)
                .eq(roleId != null, StaffPo::getRoleId, roleId)
                .orderByDesc(StaffPo::getCreatedAt);
        IPage<StaffPo> result = staffMapper.selectPage(Page.of(page, pageSize), wrapper);
        return new PageResponse<>(result.getRecords(), result.getTotal(), page, pageSize);
    }

    public StaffPo get(String extCorpId, String extStaffId) {
        return staffMapper.selectOne(new LambdaQueryWrapper<StaffPo>()
                .eq(StaffPo::getExtCorpId, extCorpId)
                .eq(StaffPo::getExtId, extStaffId)
                .last("limit 1"));
    }

    public void enable(String extCorpId, EnableStaffsRequest request) {
        for (String extStaffId : request.getExtStaffIds()) {
            weWorkClient.updateUserEnable(extCorpId, properties.getWeWork().getContactSecret(), extStaffId, 1);
            updateEnable(extCorpId, extStaffId, 1);
        }
        for (String extStaffId : request.getExcludeExtStaffIds()) {
            weWorkClient.updateUserEnable(extCorpId, properties.getWeWork().getContactSecret(), extStaffId, 0);
            updateEnable(extCorpId, extStaffId, 0);
        }
    }

    public void updateMsgArchStatus(String extCorpId) {
        staffMapper.update(null, new LambdaUpdateWrapper<StaffPo>()
                .eq(StaffPo::getExtCorpId, extCorpId)
                .set(StaffPo::getEnableMsgArch, BooleanFlag.FALSE));
        for (int edition = 1; edition <= 3; edition++) {
            List<String> permitUsers = weWorkClient
                    .listMsgAuditPermitUsers(extCorpId, properties.getWeWork().getContactSecret(), edition)
                    .getIds();
            if (permitUsers != null && !permitUsers.isEmpty()) {
                staffMapper.update(null, new LambdaUpdateWrapper<StaffPo>()
                        .eq(StaffPo::getExtCorpId, extCorpId)
                        .in(StaffPo::getExtId, permitUsers)
                        .set(StaffPo::getEnableMsgArch, BooleanFlag.TRUE));
            }
        }
    }

    private void updateEnable(String extCorpId, String extStaffId, int enable) {
        StaffPo item = get(extCorpId, extStaffId);
        if (item != null) {
            item.setEnable(enable);
            staffMapper.updateById(item);
        }
    }

    private void upsertStaff(String extCorpId, UserIdInfo source, UserDetailResponse detail) {
        StaffPo existing = staffMapper.selectOne(new LambdaQueryWrapper<StaffPo>()
                .eq(StaffPo::getExtCorpId, extCorpId)
                .eq(StaffPo::getExtId, source.getUserId())
                .last("limit 1"));
        StaffPo item = existing == null ? new StaffPo() : existing;
        if (item.getId() == null) {
            item.setId(idGenerator.nextId());
            item.setRoleId(SeedIds.DEFAULT_CORP_STAFF_ROLE_ID);
            item.setRoleType(RoleType.STAFF);
            item.setName("未知");
            item.setAvatarUrl("https://openscrm.oss-cn-hangzhou.aliyuncs.com/public/avatar.svg");
        }
        item.setExtCorpId(extCorpId);
        item.setExtId(source.getUserId());
        item.setName(StringUtils.hasText(detail.getName()) ? detail.getName() : item.getName());
        item.setAddress(null);
        item.setAlias(detail.getAlias());
        item.setAvatarUrl(StringUtils.hasText(detail.getAvatarUrl()) ? detail.getAvatarUrl() : item.getAvatarUrl());
        item.setEmail(detail.getEmail());
        item.setGender(parseInt(detail.getGender()));
        item.setStatus(detail.getStatus());
        item.setMobile(detail.getMobile());
        item.setQrCodeUrl(detail.getQrCodeUrl());
        item.setTelephone(detail.getTelephone());
        item.setEnable(detail.getEnable());
        item.setExternalPosition(detail.getExternalPosition());
        item.setExternalProfile(toJson(detail.getExternalProfile()));
        item.setExtattr(toJson(detail.getExtattr()));
        item.setDeptIds(toJson(emptyToSingleton(detail.getDepartments(), source.getDepartmentId())));
        item.setIsAuthorized(BooleanFlag.FALSE);
        if (existing == null) {
            staffMapper.insert(item);
        } else {
            staffMapper.updateById(item);
        }
    }

    private List<Long> emptyToSingleton(List<Long> values, Long fallback) {
        if (values != null && !values.isEmpty()) {
            return values;
        }
        return Collections.singletonList(fallback);
    }

    private Integer parseInt(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return Integer.parseInt(value);
    }

    private void upsertStaffDepartment(String extCorpId, UserIdInfo source) {
        StaffPo staff = get(extCorpId, source.getUserId());
        DepartmentPo department = departmentMapper.selectOne(new LambdaQueryWrapper<DepartmentPo>()
                .eq(DepartmentPo::getExtCorpId, extCorpId)
                .eq(DepartmentPo::getExtId, source.getDepartmentId().intValue())
                .last("limit 1"));
        StaffDepartmentPo existing = staffDepartmentMapper.selectOne(new LambdaQueryWrapper<StaffDepartmentPo>()
                .eq(StaffDepartmentPo::getExtCorpId, extCorpId)
                .eq(StaffDepartmentPo::getExtStaffId, source.getUserId())
                .eq(StaffDepartmentPo::getExtDepartmentId, source.getDepartmentId().intValue())
                .last("limit 1"));
        StaffDepartmentPo item = existing == null ? new StaffDepartmentPo() : existing;
        item.setExtCorpId(extCorpId);
        item.setExtStaffId(source.getUserId());
        item.setExtDepartmentId(source.getDepartmentId().intValue());
        item.setStaffId(staff == null ? null : staff.getId());
        item.setDepartmentId(department == null ? null : department.getId());
        item.setIsLeader(BooleanFlag.FALSE);
        if (existing == null) {
            staffDepartmentMapper.insert(item);
        } else {
            staffDepartmentMapper.update(item, new LambdaQueryWrapper<StaffDepartmentPo>()
                    .eq(StaffDepartmentPo::getExtCorpId, extCorpId)
                    .eq(StaffDepartmentPo::getExtStaffId, source.getUserId())
                    .eq(StaffDepartmentPo::getExtDepartmentId, source.getDepartmentId().intValue()));
        }
    }

    private void deleteAbsentStaffDepartments(String extCorpId, String extStaffId, List<Integer> activeDepartmentIds) {
        LambdaQueryWrapper<StaffDepartmentPo> wrapper = new LambdaQueryWrapper<StaffDepartmentPo>()
                .eq(StaffDepartmentPo::getExtCorpId, extCorpId)
                .eq(StaffDepartmentPo::getExtStaffId, extStaffId);
        if (!activeDepartmentIds.isEmpty()) {
            wrapper.notIn(StaffDepartmentPo::getExtDepartmentId, activeDepartmentIds);
        }
        staffDepartmentMapper.delete(wrapper);
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }
}
