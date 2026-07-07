package cn.openscrm.api.massmsg.service;

import cn.openscrm.api.common.api.PageResponse;
import cn.openscrm.api.common.exception.BizException;
import cn.openscrm.api.common.exception.ErrorCode;
import cn.openscrm.api.common.id.SnowflakeIdGenerator;
import cn.openscrm.api.common.mq.DelayedJobPublisher;
import cn.openscrm.api.common.mq.DelayedJobTopics;
import cn.openscrm.api.massmsg.dto.CustomerFilterCountResponse;
import cn.openscrm.api.massmsg.dto.GroupChatMassMsgDetailResponse;
import cn.openscrm.api.massmsg.dto.GroupChatMassMsgRequest;
import cn.openscrm.api.massmsg.dto.MassMsgDeleteRequest;
import cn.openscrm.api.massmsg.dto.MassMsgDetailResponse;
import cn.openscrm.api.massmsg.dto.MassMsgRequest;
import cn.openscrm.api.massmsg.dto.MassMsgResultResponse;
import cn.openscrm.api.persistence.entity.CustomerPo;
import cn.openscrm.api.persistence.entity.CustomerStaffPo;
import cn.openscrm.api.persistence.entity.GroupChatMassMsgPo;
import cn.openscrm.api.persistence.entity.GroupChatMassMsgResultPo;
import cn.openscrm.api.persistence.entity.MassMsgPo;
import cn.openscrm.api.persistence.entity.MassMsgStaffPo;
import cn.openscrm.api.persistence.entity.StaffPo;
import cn.openscrm.api.persistence.mapper.CustomerPoMapper;
import cn.openscrm.api.persistence.mapper.CustomerStaffPoMapper;
import cn.openscrm.api.persistence.mapper.GroupChatMassMsgPoMapper;
import cn.openscrm.api.persistence.mapper.GroupChatMassMsgResultPoMapper;
import cn.openscrm.api.persistence.mapper.MassMsgPoMapper;
import cn.openscrm.api.persistence.mapper.MassMsgStaffPoMapper;
import cn.openscrm.api.persistence.mapper.StaffPoMapper;
import cn.openscrm.api.config.OpenScrmProperties;
import cn.openscrm.api.wework.AddMsgTemplateRequest;
import cn.openscrm.api.wework.AddMsgTemplateResponse;
import cn.openscrm.api.wework.GroupMsgSendResultRequest;
import cn.openscrm.api.wework.GroupMsgSendResultResponse;
import cn.openscrm.api.wework.SendTextMessageRequest;
import cn.openscrm.api.wework.WeWorkClient;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class MassMsgService {

    private static final int SEND_TYPE_INSTANT = 1;
    private static final int SEND_TYPE_TIMED = 2;
    private static final int STATUS_NOT_ACTIVE = 1;
    private static final int STATUS_SENDING = 2;
    private static final int STATUS_SENT = 3;
    private static final int STATUS_FAILED = 5;
    private static final int STATUS_DELETED = 4;
    private static final int SENT_FALSE = 0;
    private static final int SENT_TRUE = 1;
    private static final int DELIVERED_FALSE = 0;
    private static final int DELIVERED_TRUE = 1;
    private static final DateTimeFormatter NOTIFY_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final MassMsgPoMapper massMsgMapper;
    private final MassMsgStaffPoMapper massMsgStaffMapper;
    private final GroupChatMassMsgPoMapper groupChatMassMsgMapper;
    private final GroupChatMassMsgResultPoMapper groupChatMassMsgResultMapper;
    private final CustomerStaffPoMapper customerStaffMapper;
    private final CustomerPoMapper customerMapper;
    private final StaffPoMapper staffMapper;
    private final SnowflakeIdGenerator idGenerator;
    private final ObjectMapper objectMapper;
    private final DelayedJobPublisher delayedJobPublisher;
    private final WeWorkClient weWorkClient;
    private final OpenScrmProperties properties;

    @Transactional
    public MassMsgPo create(MassMsgRequest request, StaffPo creator) {
        int sendAt = resolveSendAt(request.getSendType(), request.getSendAt());
        List<CustomerStaffPo> receivers = queryReceivers(
                creator.getExtCorpId(),
                request.getExtStaffIds(),
                request.getExtCustomerFilterEnable(),
                request.getExtCustomerFilter());
        if (receivers.isEmpty()) {
            throw new BizException(ErrorCode.NO_MASS_MSG_RECEIVERS);
        }
        LocalDateTime now = LocalDateTime.now();
        MassMsgPo item = new MassMsgPo();
        item.setId(idGenerator.nextId());
        item.setExtCorpId(creator.getExtCorpId());
        item.setExtCreatorId(creator.getExtId());
        item.setSendType(request.getSendType());
        item.setExtStaffIds(writeJson(emptyListIfNull(request.getExtStaffIds())));
        item.setExtDepartmentIds(writeJson(emptyListIfNull(request.getExtDepartmentIds())));
        item.setMsg(writeJson(request.getMsg()));
        item.setMissionStatus(STATUS_NOT_ACTIVE);
        item.setExtCustomerFilterEnable(request.getExtCustomerFilterEnable() == null ? 0 : request.getExtCustomerFilterEnable());
        item.setExtCustomerFilter(writeJson(request.getExtCustomerFilter()));
        item.setDeliveredNum(0);
        item.setSuccessNum(0);
        item.setUnDeliveredNum(countDistinctStaff(receivers));
        item.setFailedNum(receivers.size());
        item.setSendAt(sendAt);
        item.setCreatedAt(now);
        item.setUpdatedAt(now);
        massMsgMapper.insert(item);
        insertStaffRows(item.getId(), creator, receivers, now);
        publishMassMsg(item.getId(), creator.getExtCorpId(), request, sendAt);
        return item;
    }

    @Transactional
    public MassMsgPo update(Long id, MassMsgRequest request, StaffPo current) {
        MassMsgPo item = getRequired(id, current.getExtCorpId());
        if (Integer.valueOf(SEND_TYPE_INSTANT).equals(item.getSendType())
                || (item.getMissionStatus() != null && item.getMissionStatus() > STATUS_NOT_ACTIVE)) {
            throw new BizException(ErrorCode.UNSUPPORTED_MSG);
        }
        int sendAt = resolveSendAt(request.getSendType(), request.getSendAt());
        List<CustomerStaffPo> receivers = queryReceivers(
                current.getExtCorpId(),
                request.getExtStaffIds(),
                request.getExtCustomerFilterEnable(),
                request.getExtCustomerFilter());
        if (receivers.isEmpty()) {
            throw new BizException(ErrorCode.NO_MASS_MSG_RECEIVERS);
        }
        LocalDateTime now = LocalDateTime.now();
        item.setSendType(request.getSendType());
        item.setExtStaffIds(writeJson(emptyListIfNull(request.getExtStaffIds())));
        item.setExtDepartmentIds(writeJson(emptyListIfNull(request.getExtDepartmentIds())));
        item.setMsg(writeJson(request.getMsg()));
        item.setMissionStatus(STATUS_NOT_ACTIVE);
        item.setExtCustomerFilterEnable(request.getExtCustomerFilterEnable() == null ? 0 : request.getExtCustomerFilterEnable());
        item.setExtCustomerFilter(writeJson(request.getExtCustomerFilter()));
        item.setDeliveredNum(0);
        item.setSuccessNum(0);
        item.setUnDeliveredNum(countDistinctStaff(receivers));
        item.setFailedNum(receivers.size());
        item.setSendAt(sendAt);
        item.setUpdatedAt(now);
        massMsgMapper.updateById(item);

        MassMsgStaffPo delete = new MassMsgStaffPo();
        delete.setDeletedAt(now);
        delete.setUpdatedAt(now);
        massMsgStaffMapper.update(delete, new LambdaQueryWrapper<MassMsgStaffPo>()
                .eq(MassMsgStaffPo::getMassMsgId, id)
                .eq(MassMsgStaffPo::getExtCorpId, current.getExtCorpId())
                .isNull(MassMsgStaffPo::getDeletedAt));
        insertStaffRows(item.getId(), current, receivers, now);
        publishMassMsg(item.getId(), current.getExtCorpId(), request, sendAt);
        return item;
    }

    public PageResponse<MassMsgPo> query(String extCorpId, long page, long pageSize) {
        Page<MassMsgPo> result = massMsgMapper.selectPage(new Page<>(page, pageSize),
                new LambdaQueryWrapper<MassMsgPo>()
                        .eq(MassMsgPo::getExtCorpId, extCorpId)
                        .isNull(MassMsgPo::getDeletedAt)
                        .orderByDesc(MassMsgPo::getCreatedAt));
        return new PageResponse<>(result.getRecords(), result.getTotal(), page, pageSize);
    }

    public MassMsgDetailResponse get(Long id, String extCorpId) {
        MassMsgPo item = getRequired(id, extCorpId);
        StaffPo creator = staffMapper.selectOne(new LambdaQueryWrapper<StaffPo>()
                .eq(StaffPo::getExtId, item.getExtCreatorId())
                .eq(StaffPo::getExtCorpId, extCorpId)
                .last("limit 1"));
        List<MassMsgStaffPo> staffs = massMsgStaffMapper.selectList(new LambdaQueryWrapper<MassMsgStaffPo>()
                .eq(MassMsgStaffPo::getMassMsgId, id)
                .eq(MassMsgStaffPo::getExtCorpId, extCorpId)
                .isNull(MassMsgStaffPo::getDeletedAt));
        return new MassMsgDetailResponse(item, creator, staffs);
    }

    @Transactional
    public MassMsgResultResponse result(Long id, String extCorpId) {
        MassMsgPo item = refreshSendResult(getRequired(id, extCorpId));
        return new MassMsgResultResponse(
                item.getId(),
                item.getMissionStatus(),
                item.getDeliveredNum(),
                item.getSuccessNum(),
                item.getUnDeliveredNum(),
                item.getFailedNum());
    }

    @Transactional
    public void refreshResults(List<Long> ids, String extCorpId) {
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }
        List<MassMsgPo> items = massMsgMapper.selectList(new LambdaQueryWrapper<MassMsgPo>()
                .eq(MassMsgPo::getExtCorpId, extCorpId)
                .in(MassMsgPo::getId, ids)
                .isNull(MassMsgPo::getDeletedAt));
        for (MassMsgPo item : items) {
            refreshSendResult(item);
        }
    }

    public void notifyStaff(List<Long> ids, String extCorpId) {
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }
        List<MassMsgPo> items = massMsgMapper.selectList(new LambdaQueryWrapper<MassMsgPo>()
                .eq(MassMsgPo::getExtCorpId, extCorpId)
                .in(MassMsgPo::getId, ids)
                .isNull(MassMsgPo::getDeletedAt));
        for (MassMsgPo item : items) {
            notifyStaffForTask(item);
        }
    }

    @Transactional
    public void deleteTimed(MassMsgDeleteRequest request, String extCorpId) {
        if (request == null || CollectionUtils.isEmpty(request.getIds())) {
            return;
        }
        List<MassMsgPo> items = massMsgMapper.selectList(new LambdaQueryWrapper<MassMsgPo>()
                .eq(MassMsgPo::getExtCorpId, extCorpId)
                .in(MassMsgPo::getId, request.getIds())
                .isNull(MassMsgPo::getDeletedAt));
        LocalDateTime now = LocalDateTime.now();
        for (MassMsgPo item : items) {
            if (!Integer.valueOf(SEND_TYPE_TIMED).equals(item.getSendType())) {
                throw new BizException(ErrorCode.UNSUPPORTED_MSG);
            }
            item.setMissionStatus(STATUS_DELETED);
            item.setDeletedAt(now);
            item.setUpdatedAt(now);
            massMsgMapper.updateById(item);
        }
    }

    public CustomerFilterCountResponse countCustomers(String extCorpId, Integer filterEnable, JsonNode filter) {
        return new CustomerFilterCountResponse(queryReceivers(extCorpId, Collections.emptyList(), filterEnable, filter).size());
    }

    @Transactional
    public GroupChatMassMsgPo createGroupChat(GroupChatMassMsgRequest request, StaffPo creator) {
        int sendAt = resolveSendAt(request.getSendType(), request.getSendAt());
        LocalDateTime now = LocalDateTime.now();
        GroupChatMassMsgPo item = new GroupChatMassMsgPo();
        item.setId(idGenerator.nextId());
        item.setExtCorpId(creator.getExtCorpId());
        item.setExtCreatorId(creator.getExtId());
        item.setSendType(request.getSendType());
        item.setExtStaffIds(writeJson(emptyListIfNull(request.getExtStaffIds())));
        item.setMsg(writeJson(request.getMsg()));
        item.setMissionStatus(STATUS_NOT_ACTIVE);
        item.setDeliveredNum(0);
        item.setSuccessNum(0);
        item.setUnDeliveredNum(request.getExtStaffIds() == null ? 0 : request.getExtStaffIds().size());
        item.setFailedNum(0);
        item.setSendAt(sendAt);
        item.setCreatedAt(now);
        item.setUpdatedAt(now);
        groupChatMassMsgMapper.insert(item);
        insertGroupChatResultOwnerRows(item.getId(), creator, emptyListIfNull(request.getExtStaffIds()), now);
        publishGroupChatMassMsg(item.getId(), creator.getExtCorpId(), request, sendAt);
        return item;
    }

    public PageResponse<GroupChatMassMsgPo> queryGroupChat(String extCorpId, long page, long pageSize) {
        Page<GroupChatMassMsgPo> result = groupChatMassMsgMapper.selectPage(new Page<>(page, pageSize),
                new LambdaQueryWrapper<GroupChatMassMsgPo>()
                        .eq(GroupChatMassMsgPo::getExtCorpId, extCorpId)
                        .isNull(GroupChatMassMsgPo::getDeletedAt)
                        .orderByDesc(GroupChatMassMsgPo::getCreatedAt));
        return new PageResponse<>(result.getRecords(), result.getTotal(), page, pageSize);
    }

    public GroupChatMassMsgDetailResponse getGroupChat(Long id, String extCorpId) {
        GroupChatMassMsgPo item = groupChatMassMsgMapper.selectOne(new LambdaQueryWrapper<GroupChatMassMsgPo>()
                .eq(GroupChatMassMsgPo::getId, id)
                .eq(GroupChatMassMsgPo::getExtCorpId, extCorpId)
                .isNull(GroupChatMassMsgPo::getDeletedAt));
        if (item == null) {
            throw new BizException(ErrorCode.ITEM_NOT_FOUND);
        }
        item = refreshGroupChatSendResult(item);
        StaffPo creator = staffMapper.selectOne(new LambdaQueryWrapper<StaffPo>()
                .eq(StaffPo::getExtId, item.getExtCreatorId())
                .eq(StaffPo::getExtCorpId, extCorpId)
                .last("limit 1"));
        List<GroupChatMassMsgResultPo> results = groupChatMassMsgResultMapper.selectList(
                new LambdaQueryWrapper<GroupChatMassMsgResultPo>()
                        .eq(GroupChatMassMsgResultPo::getGroupChatMassMsgId, item.getId())
                        .eq(GroupChatMassMsgResultPo::getExtCorpId, extCorpId)
                        .isNull(GroupChatMassMsgResultPo::getDeletedAt)
                        .orderByAsc(GroupChatMassMsgResultPo::getExtStaffId)
                        .orderByAsc(GroupChatMassMsgResultPo::getExtChatId));
        return new GroupChatMassMsgDetailResponse(item, creator, results);
    }

    @Transactional
    public void deleteGroupChatTimed(MassMsgDeleteRequest request, String extCorpId) {
        if (request == null || CollectionUtils.isEmpty(request.getIds())) {
            return;
        }
        List<GroupChatMassMsgPo> items = groupChatMassMsgMapper.selectList(new LambdaQueryWrapper<GroupChatMassMsgPo>()
                .eq(GroupChatMassMsgPo::getExtCorpId, extCorpId)
                .in(GroupChatMassMsgPo::getId, request.getIds())
                .isNull(GroupChatMassMsgPo::getDeletedAt));
        LocalDateTime now = LocalDateTime.now();
        for (GroupChatMassMsgPo item : items) {
            if (!Integer.valueOf(SEND_TYPE_TIMED).equals(item.getSendType())) {
                throw new BizException(ErrorCode.UNSUPPORTED_MSG);
            }
            item.setMissionStatus(STATUS_DELETED);
            item.setDeletedAt(now);
            item.setUpdatedAt(now);
            groupChatMassMsgMapper.updateById(item);
        }
    }

    @Transactional
    public void refreshGroupChatResults(List<Long> ids, String extCorpId) {
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }
        List<GroupChatMassMsgPo> items = groupChatMassMsgMapper.selectList(new LambdaQueryWrapper<GroupChatMassMsgPo>()
                .eq(GroupChatMassMsgPo::getExtCorpId, extCorpId)
                .in(GroupChatMassMsgPo::getId, ids)
                .isNull(GroupChatMassMsgPo::getDeletedAt));
        for (GroupChatMassMsgPo item : items) {
            refreshGroupChatSendResult(item);
        }
    }

    @Transactional
    public void sendMassMsgToWeWork(Long id, String extCorpId) {
        MassMsgPo item = getRequired(id, extCorpId);
        try {
            Map<String, List<String>> customersByStaff = massMsgStaffMapper.selectList(new LambdaQueryWrapper<MassMsgStaffPo>()
                            .eq(MassMsgStaffPo::getMassMsgId, id)
                            .eq(MassMsgStaffPo::getExtCorpId, extCorpId)
                            .eq(MassMsgStaffPo::getIsSent, SENT_FALSE)
                            .isNull(MassMsgStaffPo::getDeletedAt))
                    .stream()
                    .collect(Collectors.groupingBy(
                            MassMsgStaffPo::getExtStaffId,
                            LinkedHashMap::new,
                            Collectors.mapping(MassMsgStaffPo::getExtCustomerId, Collectors.toList())));
            String lastExtMsgId = null;
            JsonNode msg = readJson(item.getMsg());
            for (Map.Entry<String, List<String>> entry : customersByStaff.entrySet()) {
                AddMsgTemplateResponse response = weWorkClient.addMsgTemplate(
                        item.getExtCorpId(),
                        properties.getWeWork().getCustomerSecret(),
                        buildTemplateRequest("single", entry.getKey(), entry.getValue(), msg));
                lastExtMsgId = response.getMsgId();
            }
            item.setExtMsgId(lastExtMsgId);
            item.setMissionStatus(STATUS_SENDING);
            item.setUpdatedAt(LocalDateTime.now());
            massMsgMapper.updateById(item);
        } catch (RuntimeException e) {
            item.setMissionStatus(STATUS_FAILED);
            item.setUpdatedAt(LocalDateTime.now());
            massMsgMapper.updateById(item);
            throw e;
        }
    }

    @Transactional
    public void sendGroupChatMassMsgToWeWork(Long id, String extCorpId) {
        GroupChatMassMsgPo item = groupChatMassMsgMapper.selectOne(new LambdaQueryWrapper<GroupChatMassMsgPo>()
                .eq(GroupChatMassMsgPo::getId, id)
                .eq(GroupChatMassMsgPo::getExtCorpId, extCorpId)
                .isNull(GroupChatMassMsgPo::getDeletedAt));
        if (item == null) {
            return;
        }
        try {
            String lastExtMsgId = null;
            JsonNode msg = readJson(item.getMsg());
            for (String extStaffId : readStringList(item.getExtStaffIds())) {
                AddMsgTemplateResponse response = weWorkClient.addMsgTemplate(
                        item.getExtCorpId(),
                        properties.getWeWork().getCustomerSecret(),
                        buildTemplateRequest("group", extStaffId, null, msg));
                lastExtMsgId = response.getMsgId();
                updateGroupChatOwnerMsgId(item, extStaffId, lastExtMsgId);
            }
            item.setExtMsgId(lastExtMsgId);
            item.setMissionStatus(STATUS_SENDING);
            item.setUpdatedAt(LocalDateTime.now());
            groupChatMassMsgMapper.updateById(item);
        } catch (RuntimeException e) {
            item.setMissionStatus(STATUS_FAILED);
            item.setUpdatedAt(LocalDateTime.now());
            groupChatMassMsgMapper.updateById(item);
            throw e;
        }
    }

    private GroupChatMassMsgPo refreshGroupChatSendResult(GroupChatMassMsgPo item) {
        if (!Integer.valueOf(STATUS_SENDING).equals(item.getMissionStatus())
                || !StringUtils.hasText(item.getExtMsgId())) {
            return item;
        }
        List<GroupChatMassMsgResultPo> ownerRows = getGroupChatOwnerRows(item);
        for (GroupChatMassMsgResultPo ownerRow : ownerRows) {
            String cursor = null;
            do {
                GroupMsgSendResultRequest request = new GroupMsgSendResultRequest();
                request.setMsgId(ownerRow.getExtMsgId());
                request.setUserId(ownerRow.getExtStaffId());
                request.setCursor(cursor);
                request.setLimit(1000);
                GroupMsgSendResultResponse response = weWorkClient.getGroupMsgSendResult(
                        item.getExtCorpId(),
                        properties.getWeWork().getCustomerSecret(),
                        request);
                applyGroupChatSendResult(item, ownerRow, response.getSendList());
                cursor = response.getNextCursor();
            } while (StringUtils.hasText(cursor));
        }
        recomputeGroupChatMassMsgCounters(item);
        return item;
    }

    private List<GroupChatMassMsgResultPo> getGroupChatOwnerRows(GroupChatMassMsgPo item) {
        List<GroupChatMassMsgResultPo> rows = groupChatMassMsgResultMapper.selectList(
                new LambdaQueryWrapper<GroupChatMassMsgResultPo>()
                        .eq(GroupChatMassMsgResultPo::getGroupChatMassMsgId, item.getId())
                        .eq(GroupChatMassMsgResultPo::getExtCorpId, item.getExtCorpId())
                        .isNull(GroupChatMassMsgResultPo::getExtChatId)
                        .isNotNull(GroupChatMassMsgResultPo::getExtMsgId)
                        .isNull(GroupChatMassMsgResultPo::getDeletedAt));
        if (!rows.isEmpty()) {
            return rows;
        }
        List<String> owners = readStringList(item.getExtStaffIds());
        if (owners.isEmpty() || !StringUtils.hasText(item.getExtMsgId())) {
            return Collections.emptyList();
        }
        LocalDateTime now = LocalDateTime.now();
        List<GroupChatMassMsgResultPo> created = new ArrayList<>();
        for (String extStaffId : owners) {
            GroupChatMassMsgResultPo row = newGroupChatResult(item.getId(), item.getExtCorpId(), item.getExtCreatorId(), extStaffId, now);
            row.setExtMsgId(item.getExtMsgId());
            groupChatMassMsgResultMapper.insert(row);
            created.add(row);
        }
        return created;
    }

    private void updateGroupChatOwnerMsgId(GroupChatMassMsgPo item, String extStaffId, String extMsgId) {
        if (!StringUtils.hasText(extStaffId) || !StringUtils.hasText(extMsgId)) {
            return;
        }
        GroupChatMassMsgResultPo row = groupChatMassMsgResultMapper.selectOne(
                new LambdaQueryWrapper<GroupChatMassMsgResultPo>()
                        .eq(GroupChatMassMsgResultPo::getGroupChatMassMsgId, item.getId())
                        .eq(GroupChatMassMsgResultPo::getExtCorpId, item.getExtCorpId())
                        .eq(GroupChatMassMsgResultPo::getExtStaffId, extStaffId)
                        .isNull(GroupChatMassMsgResultPo::getExtChatId)
                        .isNull(GroupChatMassMsgResultPo::getDeletedAt)
                        .last("limit 1"));
        LocalDateTime now = LocalDateTime.now();
        if (row == null) {
            row = newGroupChatResult(item.getId(), item.getExtCorpId(), item.getExtCreatorId(), extStaffId, now);
        }
        row.setExtMsgId(extMsgId);
        row.setUpdatedAt(now);
        if (row.getId() == null) {
            groupChatMassMsgResultMapper.insert(row);
        } else {
            groupChatMassMsgResultMapper.updateById(row);
        }
    }

    private void applyGroupChatSendResult(GroupChatMassMsgPo item,
                                          GroupChatMassMsgResultPo ownerRow,
                                          List<GroupMsgSendResultResponse.SendResult> sendList) {
        if (CollectionUtils.isEmpty(sendList)) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        boolean ownerDelivered = false;
        for (GroupMsgSendResultResponse.SendResult result : sendList) {
            if (!StringUtils.hasText(result.getChatId()) || result.getStatus() == null) {
                continue;
            }
            GroupChatMassMsgResultPo row = groupChatMassMsgResultMapper.selectOne(
                    new LambdaQueryWrapper<GroupChatMassMsgResultPo>()
                            .eq(GroupChatMassMsgResultPo::getGroupChatMassMsgId, item.getId())
                            .eq(GroupChatMassMsgResultPo::getExtCorpId, item.getExtCorpId())
                            .eq(GroupChatMassMsgResultPo::getExtStaffId, ownerRow.getExtStaffId())
                            .eq(GroupChatMassMsgResultPo::getExtChatId, result.getChatId())
                            .isNull(GroupChatMassMsgResultPo::getDeletedAt)
                            .last("limit 1"));
            if (row == null) {
                row = newGroupChatResult(item.getId(), item.getExtCorpId(), item.getExtCreatorId(), ownerRow.getExtStaffId(), now);
                row.setExtChatId(result.getChatId());
            }
            row.setExtMsgId(ownerRow.getExtMsgId());
            row.setSendTime(result.getSendTime());
            row.setIsSent(Integer.valueOf(1).equals(result.getStatus()) ? SENT_TRUE : SENT_FALSE);
            row.setIsDelivered(Integer.valueOf(1).equals(result.getStatus()) ? DELIVERED_TRUE : DELIVERED_FALSE);
            row.setFailedReason(Integer.valueOf(1).equals(result.getStatus()) ? 0 : result.getStatus());
            row.setUpdatedAt(now);
            if (row.getId() == null) {
                groupChatMassMsgResultMapper.insert(row);
            } else {
                groupChatMassMsgResultMapper.updateById(row);
            }
            ownerDelivered = ownerDelivered || Integer.valueOf(1).equals(result.getStatus());
        }
        if (ownerDelivered) {
            ownerRow.setIsSent(SENT_TRUE);
            ownerRow.setIsDelivered(DELIVERED_TRUE);
            ownerRow.setFailedReason(0);
            ownerRow.setUpdatedAt(now);
            groupChatMassMsgResultMapper.updateById(ownerRow);
        }
    }

    private void recomputeGroupChatMassMsgCounters(GroupChatMassMsgPo item) {
        List<GroupChatMassMsgResultPo> rows = groupChatMassMsgResultMapper.selectList(
                new LambdaQueryWrapper<GroupChatMassMsgResultPo>()
                        .eq(GroupChatMassMsgResultPo::getGroupChatMassMsgId, item.getId())
                        .eq(GroupChatMassMsgResultPo::getExtCorpId, item.getExtCorpId())
                        .isNull(GroupChatMassMsgResultPo::getDeletedAt));
        List<String> owners = readStringList(item.getExtStaffIds());
        int deliveredOwners = (int) rows.stream()
                .filter(row -> StringUtils.hasText(row.getExtChatId()))
                .filter(row -> Integer.valueOf(DELIVERED_TRUE).equals(row.getIsDelivered()))
                .map(GroupChatMassMsgResultPo::getExtStaffId)
                .distinct()
                .count();
        int successChats = (int) rows.stream()
                .filter(row -> StringUtils.hasText(row.getExtChatId()))
                .filter(row -> Integer.valueOf(DELIVERED_TRUE).equals(row.getIsDelivered()))
                .map(GroupChatMassMsgResultPo::getExtChatId)
                .distinct()
                .count();
        int failedChats = (int) rows.stream()
                .filter(row -> StringUtils.hasText(row.getExtChatId()))
                .filter(row -> !Integer.valueOf(DELIVERED_TRUE).equals(row.getIsDelivered()))
                .count();
        item.setDeliveredNum(deliveredOwners);
        item.setUnDeliveredNum(Math.max(0, owners.size() - deliveredOwners));
        item.setSuccessNum(successChats);
        item.setFailedNum(failedChats);
        if (!owners.isEmpty() && deliveredOwners == owners.size()) {
            item.setMissionStatus(STATUS_SENT);
        }
        item.setUpdatedAt(LocalDateTime.now());
        groupChatMassMsgMapper.updateById(item);
    }

    private MassMsgPo refreshSendResult(MassMsgPo item) {
        if (!Integer.valueOf(STATUS_SENDING).equals(item.getMissionStatus())
                || !StringUtils.hasText(item.getExtMsgId())) {
            return item;
        }
        List<MassMsgStaffPo> rows = massMsgStaffMapper.selectList(new LambdaQueryWrapper<MassMsgStaffPo>()
                .eq(MassMsgStaffPo::getMassMsgId, item.getId())
                .eq(MassMsgStaffPo::getExtCorpId, item.getExtCorpId())
                .isNull(MassMsgStaffPo::getDeletedAt));
        Map<String, List<MassMsgStaffPo>> rowsByStaff = rows.stream()
                .collect(Collectors.groupingBy(MassMsgStaffPo::getExtStaffId, LinkedHashMap::new, Collectors.toList()));
        for (String extStaffId : rowsByStaff.keySet()) {
            String cursor = null;
            do {
                GroupMsgSendResultRequest request = new GroupMsgSendResultRequest();
                request.setMsgId(item.getExtMsgId());
                request.setUserId(extStaffId);
                request.setCursor(cursor);
                request.setLimit(1000);
                GroupMsgSendResultResponse response = weWorkClient.getGroupMsgSendResult(
                        item.getExtCorpId(),
                        properties.getWeWork().getCustomerSecret(),
                        request);
                applySendResult(rowsByStaff.get(extStaffId), response.getSendList());
                cursor = response.getNextCursor();
            } while (StringUtils.hasText(cursor));
        }
        recomputeMassMsgCounters(item);
        return item;
    }

    private void applySendResult(List<MassMsgStaffPo> rows, List<GroupMsgSendResultResponse.SendResult> sendList) {
        if (CollectionUtils.isEmpty(rows) || CollectionUtils.isEmpty(sendList)) {
            return;
        }
        Map<String, GroupMsgSendResultResponse.SendResult> resultByCustomer = sendList.stream()
                .filter(item -> StringUtils.hasText(item.getExternalUserId()))
                .collect(Collectors.toMap(GroupMsgSendResultResponse.SendResult::getExternalUserId, item -> item, (a, b) -> a));
        LocalDateTime now = LocalDateTime.now();
        for (MassMsgStaffPo row : rows) {
            GroupMsgSendResultResponse.SendResult result = resultByCustomer.get(row.getExtCustomerId());
            if (result == null || result.getStatus() == null) {
                continue;
            }
            row.setIsSent(result.getStatus() == 1 ? SENT_TRUE : SENT_FALSE);
            row.setIsDelivered(result.getStatus() == 1 ? DELIVERED_TRUE : DELIVERED_FALSE);
            row.setFailedReason(result.getStatus() == 1 ? 0 : result.getStatus());
            row.setUpdatedAt(now);
            massMsgStaffMapper.updateById(row);
        }
    }

    private void recomputeMassMsgCounters(MassMsgPo item) {
        List<MassMsgStaffPo> rows = massMsgStaffMapper.selectList(new LambdaQueryWrapper<MassMsgStaffPo>()
                .eq(MassMsgStaffPo::getMassMsgId, item.getId())
                .eq(MassMsgStaffPo::getExtCorpId, item.getExtCorpId())
                .isNull(MassMsgStaffPo::getDeletedAt));
        int deliveredStaff = (int) rows.stream()
                .filter(row -> Integer.valueOf(SENT_TRUE).equals(row.getIsSent()))
                .map(MassMsgStaffPo::getExtStaffId)
                .distinct()
                .count();
        int totalStaff = (int) rows.stream().map(MassMsgStaffPo::getExtStaffId).distinct().count();
        int success = (int) rows.stream().filter(row -> Integer.valueOf(DELIVERED_TRUE).equals(row.getIsDelivered())).count();
        int failed = rows.size() - success;
        item.setDeliveredNum(deliveredStaff);
        item.setUnDeliveredNum(Math.max(0, totalStaff - deliveredStaff));
        item.setSuccessNum(success);
        item.setFailedNum(failed);
        if (!rows.isEmpty() && failed == 0) {
            item.setMissionStatus(STATUS_SENT);
        }
        item.setUpdatedAt(LocalDateTime.now());
        massMsgMapper.updateById(item);
    }

    private void notifyStaffForTask(MassMsgPo item) {
        List<MassMsgStaffPo> rows = massMsgStaffMapper.selectList(new LambdaQueryWrapper<MassMsgStaffPo>()
                .eq(MassMsgStaffPo::getMassMsgId, item.getId())
                .eq(MassMsgStaffPo::getExtCorpId, item.getExtCorpId())
                .isNull(MassMsgStaffPo::getDeletedAt));
        if (rows.isEmpty()) {
            return;
        }
        Set<String> staffIds = rows.stream()
                .map(MassMsgStaffPo::getExtStaffId)
                .filter(StringUtils::hasText)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (staffIds.isEmpty()) {
            return;
        }

        String createdAt = item.getCreatedAt() == null ? "" : item.getCreatedAt().format(NOTIFY_TIME_FORMATTER);
        String content = String.format("【管理员】提醒你发送群发任务%n任务创建于%s，将群发给%s等%d个客户，可前往【客户联系】中确认发送",
                createdAt, resolveNotifyCustomerName(item.getExtCorpId(), rows), rows.size());

        SendTextMessageRequest request = new SendTextMessageRequest();
        request.setTouser(String.join("|", staffIds));
        request.setAgentid(properties.getWeWork().getMainAgentId());
        SendTextMessageRequest.Text text = new SendTextMessageRequest.Text();
        text.setContent(content);
        request.setText(text);
        weWorkClient.sendTextMessage(item.getExtCorpId(), properties.getWeWork().getMainAgentSecret(), request);
    }

    private String resolveNotifyCustomerName(String extCorpId, List<MassMsgStaffPo> rows) {
        String extCustomerId = rows.stream()
                .map(MassMsgStaffPo::getExtCustomerId)
                .filter(StringUtils::hasText)
                .findFirst()
                .orElse(null);
        if (!StringUtils.hasText(extCustomerId)) {
            return "客户";
        }
        CustomerPo customer = customerMapper.selectOne(new LambdaQueryWrapper<CustomerPo>()
                .eq(CustomerPo::getExtCorpId, extCorpId)
                .eq(CustomerPo::getExtId, extCustomerId)
                .isNull(CustomerPo::getDeletedAt)
                .last("limit 1"));
        if (customer == null || !StringUtils.hasText(customer.getName())) {
            return "客户";
        }
        return customer.getName();
    }

    private MassMsgPo getRequired(Long id, String extCorpId) {
        MassMsgPo item = massMsgMapper.selectOne(new LambdaQueryWrapper<MassMsgPo>()
                .eq(MassMsgPo::getId, id)
                .eq(MassMsgPo::getExtCorpId, extCorpId)
                .isNull(MassMsgPo::getDeletedAt));
        if (item == null) {
            throw new BizException(ErrorCode.ITEM_NOT_FOUND);
        }
        return item;
    }

    private int resolveSendAt(Integer sendType, Long requestedSendAt) {
        long now = Instant.now().getEpochSecond();
        if (Integer.valueOf(SEND_TYPE_TIMED).equals(sendType)) {
            if (requestedSendAt == null || requestedSendAt <= now) {
                throw new BizException(ErrorCode.EARLIER_THAN_NOW);
            }
            return requestedSendAt.intValue();
        }
        return (int) (now + 2);
    }

    private List<CustomerStaffPo> queryReceivers(String extCorpId,
                                                 List<String> extStaffIds,
                                                 Integer filterEnable,
                                                 JsonNode filter) {
        LambdaQueryWrapper<CustomerStaffPo> wrapper = new LambdaQueryWrapper<CustomerStaffPo>()
                .eq(CustomerStaffPo::getExtCorpId, extCorpId)
                .isNull(CustomerStaffPo::getDeletedAt)
                .isNotNull(CustomerStaffPo::getExtStaffId)
                .isNotNull(CustomerStaffPo::getExtCustomerId);
        if (!CollectionUtils.isEmpty(extStaffIds)) {
            wrapper.in(CustomerStaffPo::getExtStaffId, extStaffIds);
        }
        if (filterEnable != null && filterEnable != 0 && filter != null && !filter.isNull()) {
            applyCustomerFilter(wrapper, filter);
        }
        List<CustomerStaffPo> rows = customerStaffMapper.selectList(wrapper);
        Map<String, CustomerStaffPo> dedup = new LinkedHashMap<>();
        for (CustomerStaffPo row : rows) {
            dedup.putIfAbsent(row.getExtStaffId() + "\n" + row.getExtCustomerId(), row);
        }
        return new ArrayList<>(dedup.values());
    }

    private void applyCustomerFilter(LambdaQueryWrapper<CustomerStaffPo> wrapper, JsonNode filter) {
        JsonNode start = filter.get("start_time");
        JsonNode end = filter.get("end_time");
        if (start != null && end != null && StringUtils.hasText(start.asText()) && StringUtils.hasText(end.asText())) {
            wrapper.between(CustomerStaffPo::getCreatetime, start.asText(), end.asText());
        }
        JsonNode gender = filter.get("gender");
        if (gender != null && gender.asInt() != 0) {
            wrapper.exists("select 1 from customer c where c.ext_corp_id = ext_corp_id"
                    + " and c.ext_id = ext_customer_id and c.gender = " + gender.asInt());
        }
        List<String> extGroupChatIds = strings(filter.get("ext_group_chat_ids"));
        if (!extGroupChatIds.isEmpty()) {
            wrapper.exists("select 1 from group_chat_member gcm where gcm.ext_corp_id = ext_corp_id"
                    + " and gcm.userid = ext_customer_id and gcm.ext_chat_id in ("
                    + sqlStringList(extGroupChatIds) + ")");
        }
        List<String> excludeTags = strings(filter.get("exclude_ext_tag_ids"));
        if (!excludeTags.isEmpty()) {
            wrapper.notExists("select 1 from customer_staff_tag cst where cst.customer_staff_id = id and cst.ext_tag_id in ("
                    + sqlStringList(excludeTags) + ") and cst.deleted_at is null");
        }
        String tagLogicalCondition = text(filter.get("tag_logical_condition"));
        if ("none".equalsIgnoreCase(tagLogicalCondition)) {
            wrapper.notExists("select 1 from customer_staff_tag cst where cst.customer_staff_id = id and cst.deleted_at is null");
            return;
        }
        List<String> includeTags = strings(filter.get("ext_tag_ids"));
        if (includeTags.isEmpty()) {
            return;
        }
        if ("or".equalsIgnoreCase(tagLogicalCondition)) {
            wrapper.exists("select 1 from customer_staff_tag cst where cst.customer_staff_id = id and cst.ext_tag_id in ("
                    + sqlStringList(includeTags) + ") and cst.deleted_at is null");
            return;
        }
        for (String tagId : includeTags) {
            wrapper.exists("select 1 from customer_staff_tag cst where cst.customer_staff_id = id"
                    + " and cst.ext_tag_id = " + sqlString(tagId) + " and cst.deleted_at is null");
        }
    }

    private void insertStaffRows(Long massMsgId, StaffPo creator, List<CustomerStaffPo> receivers, LocalDateTime now) {
        for (CustomerStaffPo receiver : receivers) {
            MassMsgStaffPo item = new MassMsgStaffPo();
            item.setId(idGenerator.nextId());
            item.setExtCorpId(creator.getExtCorpId());
            item.setExtCreatorId(creator.getExtId());
            item.setMassMsgId(massMsgId);
            item.setExtStaffId(receiver.getExtStaffId());
            item.setExtCustomerId(receiver.getExtCustomerId());
            item.setIsSent(SENT_FALSE);
            item.setIsDelivered(DELIVERED_FALSE);
            item.setFailedReason(0);
            item.setCreatedAt(now);
            item.setUpdatedAt(now);
            massMsgStaffMapper.insert(item);
        }
    }

    private void insertGroupChatResultOwnerRows(Long groupChatMassMsgId,
                                                StaffPo creator,
                                                List<String> extStaffIds,
                                                LocalDateTime now) {
        Set<String> dedup = extStaffIds.stream()
                .filter(StringUtils::hasText)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        for (String extStaffId : dedup) {
            groupChatMassMsgResultMapper.insert(newGroupChatResult(
                    groupChatMassMsgId,
                    creator.getExtCorpId(),
                    creator.getExtId(),
                    extStaffId,
                    now));
        }
    }

    private GroupChatMassMsgResultPo newGroupChatResult(Long groupChatMassMsgId,
                                                        String extCorpId,
                                                        String extCreatorId,
                                                        String extStaffId,
                                                        LocalDateTime now) {
        GroupChatMassMsgResultPo item = new GroupChatMassMsgResultPo();
        item.setId(idGenerator.nextId());
        item.setExtCorpId(extCorpId);
        item.setExtCreatorId(extCreatorId);
        item.setGroupChatMassMsgId(groupChatMassMsgId);
        item.setExtStaffId(extStaffId);
        item.setIsSent(SENT_FALSE);
        item.setIsDelivered(DELIVERED_FALSE);
        item.setFailedReason(0);
        item.setCreatedAt(now);
        item.setUpdatedAt(now);
        return item;
    }

    private int countDistinctStaff(List<CustomerStaffPo> receivers) {
        return (int) receivers.stream().map(CustomerStaffPo::getExtStaffId).distinct().count();
    }

    private void publishMassMsg(Long id, String extCorpId, MassMsgRequest request, int sendAt) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("topic", DelayedJobTopics.MASS_MSG);
        payload.put("id", id);
        payload.put("extCorpId", extCorpId);
        payload.put("body", request);
        delayedJobPublisher.publish(payload, delayUntil(sendAt));
    }

    private void publishGroupChatMassMsg(Long id, String extCorpId, GroupChatMassMsgRequest request, int sendAt) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("topic", DelayedJobTopics.GROUP_CHAT_MASS_MSG);
        payload.put("id", id);
        payload.put("extCorpId", extCorpId);
        payload.put("body", request);
        delayedJobPublisher.publish(payload, delayUntil(sendAt));
    }

    private Duration delayUntil(int sendAt) {
        long seconds = Math.max(0, sendAt - Instant.now().getEpochSecond());
        return Duration.ofSeconds(seconds);
    }

    private List<String> strings(JsonNode node) {
        if (node == null || !node.isArray()) {
            return Collections.emptyList();
        }
        List<String> values = new ArrayList<>();
        node.forEach(item -> {
            if (StringUtils.hasText(item.asText())) {
                values.add(item.asText());
            }
        });
        return values;
    }

    private String sqlStringList(List<String> values) {
        return values.stream()
                .map(this::sqlString)
                .collect(Collectors.joining(","));
    }

    private String sqlString(String value) {
        return "'" + value.replace("'", "''") + "'";
    }

    private String text(JsonNode node) {
        return node == null || node.isNull() ? "" : node.asText("");
    }

    private <T> List<T> emptyListIfNull(List<T> values) {
        return values == null ? Collections.emptyList() : values;
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value == null ? objectMapper.createObjectNode() : value);
        } catch (JsonProcessingException e) {
            throw new BizException(ErrorCode.INTERNAL_ERROR, "序列化群发消息失败");
        }
    }

    private JsonNode readJson(String value) {
        try {
            return StringUtils.hasText(value) ? objectMapper.readTree(value) : objectMapper.createObjectNode();
        } catch (JsonProcessingException e) {
            throw new BizException(ErrorCode.INTERNAL_ERROR, "解析群发消息失败");
        }
    }

    private List<String> readStringList(String value) {
        if (!StringUtils.hasText(value)) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(value, new com.fasterxml.jackson.core.type.TypeReference<List<String>>() {
            });
        } catch (JsonProcessingException e) {
            return Collections.emptyList();
        }
    }

    private AddMsgTemplateRequest buildTemplateRequest(String chatType,
                                                       String sender,
                                                       List<String> externalUserIds,
                                                       JsonNode msg) {
        AddMsgTemplateRequest request = new AddMsgTemplateRequest();
        request.setChatType(chatType);
        request.setSender(sender);
        request.setExternalUserId(externalUserIds);
        AddMsgTemplateRequest.Text text = new AddMsgTemplateRequest.Text();
        JsonNode textNode = msg == null ? null : msg.get("text");
        text.setContent(textNode == null ? "" : textNode.asText(""));
        request.setText(text);
        JsonNode attachments = msg == null ? null : msg.get("attachments");
        if (attachments != null && attachments.isArray()) {
            List<JsonNode> values = new ArrayList<>();
            attachments.forEach(values::add);
            request.setAttachments(values);
        }
        return request;
    }
}
