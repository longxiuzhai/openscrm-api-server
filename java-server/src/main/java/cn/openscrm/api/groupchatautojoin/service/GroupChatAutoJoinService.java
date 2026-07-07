package cn.openscrm.api.groupchatautojoin.service;

import cn.openscrm.api.common.api.PageResponse;
import cn.openscrm.api.common.constant.BooleanFlag;
import cn.openscrm.api.common.exception.BizException;
import cn.openscrm.api.common.exception.ErrorCode;
import cn.openscrm.api.common.id.SnowflakeIdGenerator;
import cn.openscrm.api.config.OpenScrmProperties;
import cn.openscrm.api.groupchatautojoin.dto.GroupChatAutoJoinBatchRegroupRequest;
import cn.openscrm.api.groupchatautojoin.dto.GroupChatAutoJoinDeleteRequest;
import cn.openscrm.api.groupchatautojoin.dto.GroupChatAutoJoinRequest;
import cn.openscrm.api.groupchatautojoin.dto.GroupChatAutoJoinResponse;
import cn.openscrm.api.groupchatautojoin.dto.GroupChatAutoJoinStaffRequest;
import cn.openscrm.api.groupchatautojoin.dto.GroupChatQrCodeRequest;
import cn.openscrm.api.persistence.entity.GroupChatAutoJoinBackupStaffPo;
import cn.openscrm.api.persistence.entity.GroupChatAutoJoinCodePo;
import cn.openscrm.api.persistence.entity.GroupChatAutoJoinCodeStaffPo;
import cn.openscrm.api.persistence.entity.GroupChatGroupPo;
import cn.openscrm.api.persistence.entity.GroupChatQrcodePo;
import cn.openscrm.api.persistence.entity.StaffPo;
import cn.openscrm.api.persistence.mapper.GroupChatAutoJoinBackupStaffPoMapper;
import cn.openscrm.api.persistence.mapper.GroupChatAutoJoinCodePoMapper;
import cn.openscrm.api.persistence.mapper.GroupChatAutoJoinCodeStaffPoMapper;
import cn.openscrm.api.persistence.mapper.GroupChatGroupPoMapper;
import cn.openscrm.api.persistence.mapper.GroupChatQrcodePoMapper;
import cn.openscrm.api.persistence.mapper.StaffPoMapper;
import cn.openscrm.api.wework.ContactWayAddResponse;
import cn.openscrm.api.wework.ContactWayGetResponse;
import cn.openscrm.api.wework.ContactWayRequest;
import cn.openscrm.api.wework.ExternalContactMarkTagRequest;
import cn.openscrm.api.wework.WeWorkClient;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class GroupChatAutoJoinService {

    private static final String STATE_PREFIX = "auto_join_code_";
    private static final int CONTACT_WAY_TYPE_MULTIPLE = 2;
    private static final int CONTACT_WAY_SCENE_QR = 2;

    private final GroupChatAutoJoinCodePoMapper autoJoinCodeMapper;
    private final GroupChatQrcodePoMapper qrCodeMapper;
    private final GroupChatAutoJoinCodeStaffPoMapper staffRelationMapper;
    private final GroupChatAutoJoinBackupStaffPoMapper backupStaffMapper;
    private final GroupChatGroupPoMapper groupMapper;
    private final StaffPoMapper staffMapper;
    private final SnowflakeIdGenerator idGenerator;
    private final ObjectMapper objectMapper;
    private final WeWorkClient weWorkClient;
    private final OpenScrmProperties properties;

    @Transactional
    public GroupChatAutoJoinResponse create(GroupChatAutoJoinRequest request, StaffPo current) {
        Long id = idGenerator.nextId();
        String state = STATE_PREFIX + id;
        ContactWayAddResponse wx = weWorkClient.addContactWay(
                current.getExtCorpId(),
                properties.getWeWork().getCustomerSecret(),
                contactWayRequest(null, request, state));
        String qrCode = wx.getQrCode();
        if (!StringUtils.hasText(qrCode)) {
            ContactWayGetResponse info = weWorkClient.getContactWay(
                    current.getExtCorpId(),
                    properties.getWeWork().getCustomerSecret(),
                    wx.getConfigId());
            if (info.getContactWay() != null) {
                qrCode = info.getContactWay().getQrCode();
            }
        }

        LocalDateTime now = LocalDateTime.now();
        GroupChatAutoJoinCodePo item = new GroupChatAutoJoinCodePo();
        item.setId(id);
        item.setExtCorpId(current.getExtCorpId());
        item.setExtCreatorId(current.getExtId());
        fill(item, request, state, wx.getConfigId(), qrCode);
        item.setAddCustomerCount(0L);
        item.setCreatedAt(now);
        item.setUpdatedAt(now);
        autoJoinCodeMapper.insert(item);
        replaceQrCodes(id, request.getGroupChatQrCode());
        replaceStaffs(id, current, request.getStaffs());
        replaceBackupStaffs(id, current, request.getBackupStaffs());
        return get(id, current.getExtCorpId());
    }

    @Transactional
    public GroupChatAutoJoinResponse update(Long id, GroupChatAutoJoinRequest request, StaffPo current) {
        GroupChatAutoJoinCodePo item = getRequired(id, current.getExtCorpId());
        String state = STATE_PREFIX + id;
        String configId = item.getConfigId();
        String qrCode = item.getQrCode();
        if (StringUtils.hasText(configId)) {
            ContactWayRequest wxRequest = contactWayRequest(configId, request, state);
            weWorkClient.updateContactWay(
                    current.getExtCorpId(),
                    properties.getWeWork().getCustomerSecret(),
                    wxRequest);
        } else {
            ContactWayAddResponse wx = weWorkClient.addContactWay(
                    current.getExtCorpId(),
                    properties.getWeWork().getCustomerSecret(),
                    contactWayRequest(null, request, state));
            configId = wx.getConfigId();
            qrCode = wx.getQrCode();
        }
        fill(item, request, state, configId, qrCode);
        item.setUpdatedAt(LocalDateTime.now());
        autoJoinCodeMapper.updateById(item);
        replaceQrCodes(id, request.getGroupChatQrCode());
        replaceStaffs(id, current, request.getStaffs());
        replaceBackupStaffs(id, current, request.getBackupStaffs());
        return get(id, current.getExtCorpId());
    }

    public PageResponse<GroupChatAutoJoinResponse> query(String extCorpId,
                                                         Long groupId,
                                                         String remark,
                                                         long page,
                                                         long pageSize) {
        Page<GroupChatAutoJoinCodePo> result = autoJoinCodeMapper.selectPage(new Page<>(page, pageSize),
                new LambdaQueryWrapper<GroupChatAutoJoinCodePo>()
                        .eq(GroupChatAutoJoinCodePo::getExtCorpId, extCorpId)
                        .eq(groupId != null, GroupChatAutoJoinCodePo::getGroupId, groupId)
                        .likeRight(StringUtils.hasText(remark), GroupChatAutoJoinCodePo::getRemark, remark)
                        .isNull(GroupChatAutoJoinCodePo::getDeletedAt)
                        .orderByDesc(GroupChatAutoJoinCodePo::getCreatedAt));
        return new PageResponse<>(attach(result.getRecords()), result.getTotal(), page, pageSize);
    }

    public GroupChatAutoJoinResponse get(Long id, String extCorpId) {
        return attach(Collections.singletonList(getRequired(id, extCorpId))).get(0);
    }

    @Transactional
    public int delete(GroupChatAutoJoinDeleteRequest request, String extCorpId) {
        if (request == null || CollectionUtils.isEmpty(request.getIds())) {
            return 0;
        }
        List<GroupChatAutoJoinCodePo> items = autoJoinCodeMapper.selectList(new LambdaQueryWrapper<GroupChatAutoJoinCodePo>()
                .eq(GroupChatAutoJoinCodePo::getExtCorpId, extCorpId)
                .in(GroupChatAutoJoinCodePo::getId, request.getIds())
                .isNull(GroupChatAutoJoinCodePo::getDeletedAt));
        for (GroupChatAutoJoinCodePo item : items) {
            if (StringUtils.hasText(item.getConfigId())) {
                weWorkClient.deleteContactWay(
                        extCorpId,
                        properties.getWeWork().getCustomerSecret(),
                        item.getConfigId());
            }
        }
        GroupChatAutoJoinCodePo update = new GroupChatAutoJoinCodePo();
        update.setDeletedAt(LocalDateTime.now());
        update.setUpdatedAt(LocalDateTime.now());
        return autoJoinCodeMapper.update(update, new LambdaQueryWrapper<GroupChatAutoJoinCodePo>()
                .eq(GroupChatAutoJoinCodePo::getExtCorpId, extCorpId)
                .in(GroupChatAutoJoinCodePo::getId, request.getIds())
                .isNull(GroupChatAutoJoinCodePo::getDeletedAt));
    }

    @Transactional
    public void batchRegroup(GroupChatAutoJoinBatchRegroupRequest request, String extCorpId) {
        if (request == null || request.getNewGroupId() == null || CollectionUtils.isEmpty(request.getIds())) {
            return;
        }
        GroupChatGroupPo group = groupMapper.selectOne(new LambdaQueryWrapper<GroupChatGroupPo>()
                .eq(GroupChatGroupPo::getId, request.getNewGroupId())
                .eq(GroupChatGroupPo::getExtCorpId, extCorpId)
                .isNull(GroupChatGroupPo::getDeletedAt));
        if (group == null) {
            throw new BizException(ErrorCode.GROUP_CHAT_NOT_EXISTS);
        }
        GroupChatAutoJoinCodePo update = new GroupChatAutoJoinCodePo();
        update.setGroupId(request.getNewGroupId());
        update.setUpdatedAt(LocalDateTime.now());
        autoJoinCodeMapper.update(update, new LambdaQueryWrapper<GroupChatAutoJoinCodePo>()
                .eq(GroupChatAutoJoinCodePo::getExtCorpId, extCorpId)
                .in(GroupChatAutoJoinCodePo::getId, request.getIds())
                .isNull(GroupChatAutoJoinCodePo::getDeletedAt));
    }

    @Transactional
    public void dealAddCustomerEvent(String extCorpId, String extStaffId, String extCustomerId, String state) {
        if (!StringUtils.hasText(state) || !state.startsWith(STATE_PREFIX)) {
            return;
        }
        Long id = parseId(state.substring(STATE_PREFIX.length()));
        if (id == null) {
            return;
        }
        GroupChatAutoJoinCodePo item = autoJoinCodeMapper.selectOne(new LambdaQueryWrapper<GroupChatAutoJoinCodePo>()
                .eq(GroupChatAutoJoinCodePo::getId, id)
                .eq(GroupChatAutoJoinCodePo::getExtCorpId, extCorpId)
                .isNull(GroupChatAutoJoinCodePo::getDeletedAt)
                .last("limit 1"));
        if (item == null) {
            return;
        }
        if (Integer.valueOf(BooleanFlag.TRUE).equals(item.getAutoTagEnable())) {
            markTags(item, extStaffId, extCustomerId);
        }
        incrementAddCustomerCount(item, extStaffId);
    }

    private GroupChatAutoJoinCodePo getRequired(Long id, String extCorpId) {
        GroupChatAutoJoinCodePo item = autoJoinCodeMapper.selectOne(new LambdaQueryWrapper<GroupChatAutoJoinCodePo>()
                .eq(GroupChatAutoJoinCodePo::getId, id)
                .eq(GroupChatAutoJoinCodePo::getExtCorpId, extCorpId)
                .isNull(GroupChatAutoJoinCodePo::getDeletedAt));
        if (item == null) {
            throw new BizException(ErrorCode.ITEM_NOT_FOUND);
        }
        return item;
    }

    private void incrementAddCustomerCount(GroupChatAutoJoinCodePo item, String extStaffId) {
        LocalDateTime now = LocalDateTime.now();
        autoJoinCodeMapper.update(null, new LambdaUpdateWrapper<GroupChatAutoJoinCodePo>()
                .eq(GroupChatAutoJoinCodePo::getId, item.getId())
                .set(GroupChatAutoJoinCodePo::getAddCustomerCount, defaultLong(item.getAddCustomerCount()) + 1)
                .set(GroupChatAutoJoinCodePo::getUpdatedAt, now));
        GroupChatAutoJoinCodeStaffPo staff = staffRelationMapper.selectOne(
                new LambdaQueryWrapper<GroupChatAutoJoinCodeStaffPo>()
                        .eq(GroupChatAutoJoinCodeStaffPo::getGroupChatAutoJoinCodeId, item.getId())
                        .eq(GroupChatAutoJoinCodeStaffPo::getExtCorpId, item.getExtCorpId())
                        .eq(GroupChatAutoJoinCodeStaffPo::getExtStaffId, extStaffId)
                        .isNull(GroupChatAutoJoinCodeStaffPo::getDeletedAt)
                        .last("limit 1"));
        if (staff == null) {
            incrementBackupAddCustomerCount(item, extStaffId, now);
            return;
        }
        long nextDaily = defaultLong(staff.getDailyAddCustomerCount()) + 1;
        staffRelationMapper.update(null, new LambdaUpdateWrapper<GroupChatAutoJoinCodeStaffPo>()
                .eq(GroupChatAutoJoinCodeStaffPo::getId, staff.getId())
                .set(GroupChatAutoJoinCodeStaffPo::getAddCustomerCount, defaultLong(staff.getAddCustomerCount()) + 1)
                .set(GroupChatAutoJoinCodeStaffPo::getDailyAddCustomerCount, nextDaily)
                .set(GroupChatAutoJoinCodeStaffPo::getUpdatedAt, now));
        if (Integer.valueOf(BooleanFlag.TRUE).equals(item.getDailyAddCustomerLimitEnable())
                && staff.getDailyAddCustomerLimit() != null
                && staff.getDailyAddCustomerLimit() >= 1
                && nextDaily >= staff.getDailyAddCustomerLimit()) {
            refreshContactWayUsers(item);
        }
    }

    private void incrementBackupAddCustomerCount(GroupChatAutoJoinCodePo item, String extStaffId, LocalDateTime now) {
        GroupChatAutoJoinBackupStaffPo backupStaff = backupStaffMapper.selectOne(
                new LambdaQueryWrapper<GroupChatAutoJoinBackupStaffPo>()
                        .eq(GroupChatAutoJoinBackupStaffPo::getGroupChatAutoJoinCodeId, item.getId())
                        .eq(GroupChatAutoJoinBackupStaffPo::getExtCorpId, item.getExtCorpId())
                        .eq(GroupChatAutoJoinBackupStaffPo::getExtStaffId, extStaffId)
                        .isNull(GroupChatAutoJoinBackupStaffPo::getDeletedAt)
                        .last("limit 1"));
        if (backupStaff == null) {
            return;
        }
        long nextDaily = defaultLong(backupStaff.getDailyAddCustomerCount()) + 1;
        backupStaffMapper.update(null, new LambdaUpdateWrapper<GroupChatAutoJoinBackupStaffPo>()
                .eq(GroupChatAutoJoinBackupStaffPo::getId, backupStaff.getId())
                .set(GroupChatAutoJoinBackupStaffPo::getAddCustomerCount, defaultLong(backupStaff.getAddCustomerCount()) + 1)
                .set(GroupChatAutoJoinBackupStaffPo::getDailyAddCustomerCount, nextDaily)
                .set(GroupChatAutoJoinBackupStaffPo::getUpdatedAt, now));
        if (Integer.valueOf(BooleanFlag.TRUE).equals(item.getDailyAddCustomerLimitEnable())
                && backupStaff.getDailyAddCustomerLimit() != null
                && backupStaff.getDailyAddCustomerLimit() >= 1
                && nextDaily >= backupStaff.getDailyAddCustomerLimit()) {
            refreshContactWayUsers(item);
        }
    }

    private void refreshContactWayUsers(GroupChatAutoJoinCodePo item) {
        if (!StringUtils.hasText(item.getConfigId())) {
            return;
        }
        List<String> available = new ArrayList<>();
        List<GroupChatAutoJoinCodeStaffPo> staffs = staffRelationMapper.selectList(
                new LambdaQueryWrapper<GroupChatAutoJoinCodeStaffPo>()
                        .eq(GroupChatAutoJoinCodeStaffPo::getGroupChatAutoJoinCodeId, item.getId())
                        .eq(GroupChatAutoJoinCodeStaffPo::getExtCorpId, item.getExtCorpId())
                        .isNull(GroupChatAutoJoinCodeStaffPo::getDeletedAt));
        for (GroupChatAutoJoinCodeStaffPo staff : staffs) {
            if (canUseStaff(staff)) {
                available.add(staff.getExtStaffId());
            }
        }
        if (available.isEmpty()) {
            List<GroupChatAutoJoinBackupStaffPo> backups = backupStaffMapper.selectList(
                    new LambdaQueryWrapper<GroupChatAutoJoinBackupStaffPo>()
                            .eq(GroupChatAutoJoinBackupStaffPo::getGroupChatAutoJoinCodeId, item.getId())
                            .eq(GroupChatAutoJoinBackupStaffPo::getExtCorpId, item.getExtCorpId())
                            .isNull(GroupChatAutoJoinBackupStaffPo::getDeletedAt));
            for (GroupChatAutoJoinBackupStaffPo staff : backups) {
                if (canUseBackupStaff(staff)) {
                    available.add(staff.getExtStaffId());
                }
            }
        }
        if (available.isEmpty()) {
            available.addAll(readStringList(item.getBackupStaffIds()));
        }
        ContactWayRequest request = new ContactWayRequest();
        request.setConfigId(item.getConfigId());
        request.setTemp(false);
        request.setRemark(item.getRemark());
        request.setScene(CONTACT_WAY_SCENE_QR);
        request.setSkipVerify(item.getSkipVerify() == null || item.getSkipVerify() == 1);
        request.setState(item.getState());
        request.setType(CONTACT_WAY_TYPE_MULTIPLE);
        request.setUser(available);
        weWorkClient.updateContactWay(item.getExtCorpId(), properties.getWeWork().getCustomerSecret(), request);
    }

    private boolean canUseStaff(GroupChatAutoJoinCodeStaffPo staff) {
        if (!StringUtils.hasText(staff.getExtStaffId())) {
            return false;
        }
        Long limit = staff.getDailyAddCustomerLimit();
        return limit == null || limit <= 0 || defaultLong(staff.getDailyAddCustomerCount()) < limit;
    }

    private boolean canUseBackupStaff(GroupChatAutoJoinBackupStaffPo staff) {
        if (!StringUtils.hasText(staff.getExtStaffId())) {
            return false;
        }
        Long limit = staff.getDailyAddCustomerLimit();
        return limit == null || limit <= 0 || defaultLong(staff.getDailyAddCustomerCount()) < limit;
    }

    private void markTags(GroupChatAutoJoinCodePo item, String extStaffId, String extCustomerId) {
        List<String> tagIds = readStringList(item.getExtTagIds());
        if (tagIds.isEmpty()) {
            return;
        }
        ExternalContactMarkTagRequest request = new ExternalContactMarkTagRequest();
        request.setUserid(extStaffId);
        request.setExternalUserId(extCustomerId);
        request.setAddTag(tagIds);
        weWorkClient.markExternalContactTag(item.getExtCorpId(), properties.getWeWork().getCustomerSecret(), request);
    }

    private Long parseId(String value) {
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private long defaultLong(Long value) {
        return value == null ? 0L : value;
    }

    private void fill(GroupChatAutoJoinCodePo item,
                      GroupChatAutoJoinRequest request,
                      String state,
                      String configId,
                      String qrCode) {
        item.setCreateType(request.getCreateType());
        item.setGroupId(request.getGroupId());
        item.setRemark(request.getRemark());
        item.setAutoReply(request.getAutoReply());
        item.setDayAddUserLimitEnable(request.getDayAddUserLimitEnable());
        item.setBackupStaffIds(writeJson(emptyListIfNull(request.getBackupExtStaffIds())));
        item.setConfigId(configId);
        item.setQrCode(qrCode);
        item.setSkipVerify(request.getSkipVerify() == null ? 1 : request.getSkipVerify());
        item.setState(state);
        item.setDailyAddCustomerLimitEnable(request.getDailyAddCustomerLimitEnable());
        item.setAutoTagEnable(request.getAutoTagEnable());
        item.setExtTagIds(writeJson(emptyListIfNull(request.getExtTagIds())));
        item.setExtStaffIds(writeJson(emptyListIfNull(request.getExtStaffIds())));
    }

    private ContactWayRequest contactWayRequest(String configId, GroupChatAutoJoinRequest request, String state) {
        ContactWayRequest wx = new ContactWayRequest();
        wx.setConfigId(configId);
        wx.setTemp(false);
        wx.setRemark(request.getRemark());
        wx.setScene(CONTACT_WAY_SCENE_QR);
        wx.setSkipVerify(request.getSkipVerify() == null || request.getSkipVerify() == 1);
        wx.setState(state);
        wx.setType(CONTACT_WAY_TYPE_MULTIPLE);
        wx.setUser(emptyListIfNull(request.getExtStaffIds()));
        return wx;
    }

    private void replaceQrCodes(Long id, List<GroupChatQrCodeRequest> requests) {
        qrCodeMapper.delete(new LambdaUpdateWrapper<GroupChatQrcodePo>()
                .eq(GroupChatQrcodePo::getGroupChatAutoJoinId, id));
        if (CollectionUtils.isEmpty(requests)) {
            return;
        }
        for (GroupChatQrCodeRequest request : requests) {
            GroupChatQrcodePo item = new GroupChatQrcodePo();
            item.setGroupChatAutoJoinId(id);
            item.setOrder(request.getOrder());
            item.setQrMediaId(request.getQrMediaId());
            item.setQrUrl(request.getQrUrl());
            item.setUserLimit(request.getUserLimit());
            item.setStatus(request.getStatus() == null ? 1 : request.getStatus());
            qrCodeMapper.insert(item);
        }
    }

    private void replaceStaffs(Long id, StaffPo current, List<GroupChatAutoJoinStaffRequest> requests) {
        GroupChatAutoJoinCodeStaffPo delete = new GroupChatAutoJoinCodeStaffPo();
        delete.setDeletedAt(LocalDateTime.now());
        delete.setUpdatedAt(LocalDateTime.now());
        staffRelationMapper.update(delete, new LambdaQueryWrapper<GroupChatAutoJoinCodeStaffPo>()
                .eq(GroupChatAutoJoinCodeStaffPo::getGroupChatAutoJoinCodeId, id)
                .eq(GroupChatAutoJoinCodeStaffPo::getExtCorpId, current.getExtCorpId())
                .isNull(GroupChatAutoJoinCodeStaffPo::getDeletedAt));
        if (CollectionUtils.isEmpty(requests)) {
            return;
        }
        Map<String, GroupChatAutoJoinStaffRequest> paramByExtId = requests.stream()
                .filter(item -> StringUtils.hasText(item.getExtStaffId()))
                .collect(Collectors.toMap(GroupChatAutoJoinStaffRequest::getExtStaffId, item -> item, (a, b) -> a));
        if (paramByExtId.isEmpty()) {
            return;
        }
        List<StaffPo> staffs = staffMapper.selectList(new LambdaQueryWrapper<StaffPo>()
                .eq(StaffPo::getExtCorpId, current.getExtCorpId())
                .in(StaffPo::getExtId, paramByExtId.keySet())
                .isNull(StaffPo::getDeletedAt));
        LocalDateTime now = LocalDateTime.now();
        for (StaffPo staff : staffs) {
            GroupChatAutoJoinStaffRequest param = paramByExtId.get(staff.getExtId());
            GroupChatAutoJoinCodeStaffPo item = new GroupChatAutoJoinCodeStaffPo();
            item.setId(idGenerator.nextId());
            item.setExtCorpId(current.getExtCorpId());
            item.setExtCreatorId(current.getExtId());
            item.setGroupChatAutoJoinCodeId(id);
            item.setDailyAddCustomerCount(0L);
            item.setAddCustomerCount(0L);
            item.setDailyAddCustomerLimit(param.getDailyAddCustomerLimit());
            item.setAvatar(staff.getAvatarUrl());
            item.setStaffId(staff.getId());
            item.setExtStaffId(staff.getExtId());
            item.setName(staff.getName());
            item.setCreatedAt(now);
            item.setUpdatedAt(now);
            staffRelationMapper.insert(item);
        }
    }

    private void replaceBackupStaffs(Long id, StaffPo current, List<GroupChatAutoJoinStaffRequest> requests) {
        GroupChatAutoJoinBackupStaffPo delete = new GroupChatAutoJoinBackupStaffPo();
        delete.setDeletedAt(LocalDateTime.now());
        delete.setUpdatedAt(LocalDateTime.now());
        backupStaffMapper.update(delete, new LambdaQueryWrapper<GroupChatAutoJoinBackupStaffPo>()
                .eq(GroupChatAutoJoinBackupStaffPo::getGroupChatAutoJoinCodeId, id)
                .eq(GroupChatAutoJoinBackupStaffPo::getExtCorpId, current.getExtCorpId())
                .isNull(GroupChatAutoJoinBackupStaffPo::getDeletedAt));
        if (CollectionUtils.isEmpty(requests)) {
            return;
        }
        Map<String, GroupChatAutoJoinStaffRequest> paramByExtId = requests.stream()
                .filter(item -> StringUtils.hasText(item.getExtStaffId()))
                .collect(Collectors.toMap(GroupChatAutoJoinStaffRequest::getExtStaffId, item -> item, (a, b) -> a));
        if (paramByExtId.isEmpty()) {
            return;
        }
        List<StaffPo> staffs = staffMapper.selectList(new LambdaQueryWrapper<StaffPo>()
                .eq(StaffPo::getExtCorpId, current.getExtCorpId())
                .in(StaffPo::getExtId, paramByExtId.keySet())
                .isNull(StaffPo::getDeletedAt));
        LocalDateTime now = LocalDateTime.now();
        for (StaffPo staff : staffs) {
            GroupChatAutoJoinStaffRequest param = paramByExtId.get(staff.getExtId());
            GroupChatAutoJoinBackupStaffPo item = new GroupChatAutoJoinBackupStaffPo();
            item.setId(idGenerator.nextId());
            item.setExtCorpId(current.getExtCorpId());
            item.setExtCreatorId(current.getExtId());
            item.setGroupChatAutoJoinCodeId(id);
            item.setDailyAddCustomerCount(0L);
            item.setAddCustomerCount(0L);
            item.setDailyAddCustomerLimit(param.getDailyAddCustomerLimit());
            item.setAvatar(staff.getAvatarUrl());
            item.setStaffId(staff.getId());
            item.setExtStaffId(staff.getExtId());
            item.setName(staff.getName());
            item.setCreatedAt(now);
            item.setUpdatedAt(now);
            backupStaffMapper.insert(item);
        }
    }

    private List<GroupChatAutoJoinResponse> attach(List<GroupChatAutoJoinCodePo> codes) {
        if (CollectionUtils.isEmpty(codes)) {
            return Collections.emptyList();
        }
        List<Long> ids = codes.stream().map(GroupChatAutoJoinCodePo::getId).collect(Collectors.toList());
        Map<Long, List<GroupChatQrcodePo>> qrByCode = qrCodeMapper.selectList(new LambdaQueryWrapper<GroupChatQrcodePo>()
                        .in(GroupChatQrcodePo::getGroupChatAutoJoinId, ids))
                .stream()
                .collect(Collectors.groupingBy(GroupChatQrcodePo::getGroupChatAutoJoinId, LinkedHashMap::new, Collectors.toList()));
        Map<Long, List<GroupChatAutoJoinCodeStaffPo>> staffByCode = staffRelationMapper.selectList(
                        new LambdaQueryWrapper<GroupChatAutoJoinCodeStaffPo>()
                                .in(GroupChatAutoJoinCodeStaffPo::getGroupChatAutoJoinCodeId, ids)
                                .isNull(GroupChatAutoJoinCodeStaffPo::getDeletedAt))
                .stream()
                .collect(Collectors.groupingBy(GroupChatAutoJoinCodeStaffPo::getGroupChatAutoJoinCodeId, LinkedHashMap::new, Collectors.toList()));
        Map<Long, List<GroupChatAutoJoinBackupStaffPo>> backupStaffByCode = backupStaffMapper.selectList(
                        new LambdaQueryWrapper<GroupChatAutoJoinBackupStaffPo>()
                                .in(GroupChatAutoJoinBackupStaffPo::getGroupChatAutoJoinCodeId, ids)
                                .isNull(GroupChatAutoJoinBackupStaffPo::getDeletedAt))
                .stream()
                .collect(Collectors.groupingBy(GroupChatAutoJoinBackupStaffPo::getGroupChatAutoJoinCodeId, LinkedHashMap::new, Collectors.toList()));
        return codes.stream()
                .map(item -> new GroupChatAutoJoinResponse(
                        item,
                        qrByCode.getOrDefault(item.getId(), Collections.emptyList()),
                        staffByCode.getOrDefault(item.getId(), Collections.emptyList()),
                        backupStaffByCode.getOrDefault(item.getId(), Collections.emptyList())))
                .collect(Collectors.toList());
    }

    private <T> List<T> emptyListIfNull(List<T> values) {
        return values == null ? Collections.emptyList() : values;
    }

    private List<String> readStringList(String json) {
        if (!StringUtils.hasText(json)) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(json,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
        } catch (JsonProcessingException e) {
            return Collections.emptyList();
        }
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new BizException(ErrorCode.INTERNAL_ERROR, "序列化自动拉群码失败");
        }
    }
}
