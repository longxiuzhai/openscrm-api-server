package cn.openscrm.api.contactway.service;

import cn.openscrm.api.common.api.PageResponse;
import cn.openscrm.api.common.constant.BooleanFlag;
import cn.openscrm.api.common.exception.BizException;
import cn.openscrm.api.common.exception.ErrorCode;
import cn.openscrm.api.common.id.SnowflakeIdGenerator;
import cn.openscrm.api.common.mq.DelayedJobPublisher;
import cn.openscrm.api.common.mq.DelayedJobTopics;
import cn.openscrm.api.config.OpenScrmProperties;
import cn.openscrm.api.contactway.dto.ContactWayBatchUpdateRequest;
import cn.openscrm.api.contactway.dto.ContactWayDeleteRequest;
import cn.openscrm.api.contactway.dto.ContactWayRequest;
import cn.openscrm.api.contactway.dto.ContactWayResponse;
import cn.openscrm.api.contactway.dto.ContactWayScheduleRequest;
import cn.openscrm.api.contactway.dto.ContactWayStaffRequest;
import cn.openscrm.api.persistence.entity.ContactWayBackupStaffPo;
import cn.openscrm.api.persistence.entity.ContactWayGroupPo;
import cn.openscrm.api.persistence.entity.ContactWayPo;
import cn.openscrm.api.persistence.entity.ContactWaySchedulePo;
import cn.openscrm.api.persistence.entity.ContactWayScheduleStaffPo;
import cn.openscrm.api.persistence.entity.ContactWayStaffPo;
import cn.openscrm.api.persistence.entity.StaffPo;
import cn.openscrm.api.persistence.entity.TagPo;
import cn.openscrm.api.persistence.mapper.ContactWayBackupStaffPoMapper;
import cn.openscrm.api.persistence.mapper.ContactWayGroupPoMapper;
import cn.openscrm.api.persistence.mapper.ContactWayPoMapper;
import cn.openscrm.api.persistence.mapper.ContactWaySchedulePoMapper;
import cn.openscrm.api.persistence.mapper.ContactWayScheduleStaffPoMapper;
import cn.openscrm.api.persistence.mapper.ContactWayStaffPoMapper;
import cn.openscrm.api.persistence.mapper.StaffPoMapper;
import cn.openscrm.api.persistence.mapper.TagPoMapper;
import cn.openscrm.api.wework.ContactWayAddResponse;
import cn.openscrm.api.wework.ContactWayGetResponse;
import cn.openscrm.api.wework.ExternalContactMarkTagRequest;
import cn.openscrm.api.wework.ExternalContactRemarkRequest;
import cn.openscrm.api.wework.SendWelcomeMessageRequest;
import cn.openscrm.api.wework.WeWorkClient;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

@Service
@Slf4j
@RequiredArgsConstructor
public class ContactWayService {

    private static final String STATE_PREFIX = "ixj:";
    private static final int AUTO_REPLY_CUSTOM = 1;
    private static final int AUTO_REPLY_DEFAULT = 2;
    private static final int AUTO_REPLY_DISABLE = 3;

    private final OpenScrmProperties properties;
    private final WeWorkClient weWorkClient;
    private final ContactWayPoMapper contactWayMapper;
    private final ContactWayGroupPoMapper contactWayGroupMapper;
    private final ContactWayStaffPoMapper contactWayStaffMapper;
    private final ContactWayBackupStaffPoMapper backupStaffMapper;
    private final ContactWaySchedulePoMapper scheduleMapper;
    private final ContactWayScheduleStaffPoMapper scheduleStaffMapper;
    private final StaffPoMapper staffMapper;
    private final TagPoMapper tagMapper;
    private final SnowflakeIdGenerator idGenerator;
    private final ObjectMapper objectMapper;
    private final DelayedJobPublisher delayedJobPublisher;

    public PageResponse<ContactWayResponse> query(String extCorpId,
                                                  Long id,
                                                  List<String> extStaffIds,
                                                  String name,
                                                  String configId,
                                                  Long groupId,
                                                  LocalDateTime createdAtStart,
                                                  LocalDateTime createdAtEnd,
                                                  long page,
                                                  long pageSize) {
        List<Long> filterIds = findContactWayIdsByStaff(extStaffIds);
        if (!CollectionUtils.isEmpty(extStaffIds) && filterIds.isEmpty()) {
            return new PageResponse<>(Collections.emptyList(), 0, page, pageSize);
        }
        IPage<ContactWayPo> result = contactWayMapper.selectPage(new Page<>(page, pageSize),
                new LambdaQueryWrapper<ContactWayPo>()
                        .eq(ContactWayPo::getExtCorpId, extCorpId)
                        .eq(id != null, ContactWayPo::getId, id)
                        .in(!filterIds.isEmpty(), ContactWayPo::getId, filterIds)
                        .likeRight(StringUtils.hasText(name), ContactWayPo::getName, name)
                        .eq(StringUtils.hasText(configId), ContactWayPo::getConfigId, configId)
                        .eq(groupId != null, ContactWayPo::getGroupId, groupId)
                        .gt(createdAtStart != null, ContactWayPo::getCreatedAt, createdAtStart)
                        .lt(createdAtEnd != null, ContactWayPo::getCreatedAt, createdAtEnd)
                        .orderByDesc(ContactWayPo::getCreatedAt));
        List<ContactWayResponse> items = new ArrayList<>();
        for (ContactWayPo item : result.getRecords()) {
            items.add(toResponse(item));
        }
        return new PageResponse<>(items, result.getTotal(), page, pageSize);
    }

    public ContactWayResponse get(Long id, String extCorpId) {
        return toResponse(getRaw(id, extCorpId));
    }

    @Transactional(rollbackFor = Exception.class)
    public ContactWayPo create(ContactWayRequest request, String extCorpId, String extCreatorId) {
        validate(request);
        ContactWayPo item = new ContactWayPo();
        item.setId(idGenerator.nextId());
        item.setExtCorpId(extCorpId);
        item.setExtCreatorId(extCreatorId);
        item.setState(STATE_PREFIX + item.getId());
        applyRequest(item, request);
        item.setCreatedAt(LocalDateTime.now());
        item.setUpdatedAt(LocalDateTime.now());
        saveAssociations(item, request, extCreatorId);
        refreshLocalStaffIds(item);
        scheduleRefreshJobs(item);

        ContactWayAddResponse wx = weWorkClient.addContactWay(
                extCorpId, properties.getWeWork().getCustomerSecret(), toWxRequest(item, false));
        item.setConfigId(wx.getConfigId());
        if (StringUtils.hasText(wx.getQrCode())) {
            item.setQrCode(wx.getQrCode());
        } else {
            ContactWayGetResponse detail = weWorkClient.getContactWay(
                    extCorpId, properties.getWeWork().getCustomerSecret(), item.getConfigId());
            if (detail.getContactWay() != null) {
                item.setQrCode(detail.getContactWay().getQrCode());
            }
        }
        contactWayMapper.insert(item);
        return item;
    }

    @Transactional(rollbackFor = Exception.class)
    public ContactWayPo update(Long id, ContactWayRequest request, String extCorpId) {
        validate(request);
        ContactWayPo item = getRaw(id, extCorpId);
        applyRequest(item, request);
        item.setUpdatedAt(LocalDateTime.now());
        replaceAssociations(item, request, item.getExtCreatorId());
        refreshLocalStaffIds(item);
        scheduleRefreshJobs(item);
        weWorkClient.updateContactWay(extCorpId, properties.getWeWork().getCustomerSecret(), toWxRequest(item, true));
        contactWayMapper.updateById(item);
        return item;
    }

    @Transactional(rollbackFor = Exception.class)
    public long delete(ContactWayDeleteRequest request, String extCorpId) {
        if (request == null || CollectionUtils.isEmpty(request.getIds())) {
            throw new BizException(ErrorCode.BAD_REQUEST);
        }
        List<ContactWayPo> records = contactWayMapper.selectList(new LambdaQueryWrapper<ContactWayPo>()
                .eq(ContactWayPo::getExtCorpId, extCorpId)
                .in(ContactWayPo::getId, request.getIds()));
        for (ContactWayPo record : records) {
            if (StringUtils.hasText(record.getConfigId())) {
                weWorkClient.deleteContactWay(extCorpId, properties.getWeWork().getCustomerSecret(), record.getConfigId());
            }
            deleteAssociations(record.getId());
        }
        return contactWayMapper.delete(new LambdaQueryWrapper<ContactWayPo>()
                .eq(ContactWayPo::getExtCorpId, extCorpId)
                .in(ContactWayPo::getId, request.getIds()));
    }

    @Transactional(rollbackFor = Exception.class)
    public long batchUpdate(ContactWayBatchUpdateRequest request, String extCorpId) {
        if (request == null || CollectionUtils.isEmpty(request.getIds()) || request.getGroupId() == null) {
            throw new BizException(ErrorCode.BAD_REQUEST);
        }
        return contactWayMapper.update(null, new LambdaUpdateWrapper<ContactWayPo>()
                .eq(ContactWayPo::getExtCorpId, extCorpId)
                .in(ContactWayPo::getId, request.getIds())
                .set(ContactWayPo::getGroupId, request.getGroupId())
                .set(ContactWayPo::getUpdatedAt, LocalDateTime.now()));
    }

    public void refresh(Long id, String extCorpId) {
        ContactWayPo item = getRaw(id, extCorpId);
        refreshLocalStaffIds(item);
        scheduleRefreshJobs(item);
        weWorkClient.updateContactWay(extCorpId, properties.getWeWork().getCustomerSecret(), toWxRequest(item, true));
        contactWayMapper.updateById(item);
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean dealAddCustomerEvent(String extStaffId,
                                        String extCustomerId,
                                        String state,
                                        String welcomeCode,
                                        String customerName) {
        if (!StringUtils.hasText(state) || !state.startsWith(STATE_PREFIX)) {
            return true;
        }
        Long contactWayId = parseContactWayId(state.substring(STATE_PREFIX.length()));
        if (contactWayId == null) {
            return true;
        }
        ContactWayPo contactWay = contactWayMapper.selectOne(new LambdaQueryWrapper<ContactWayPo>()
                .eq(ContactWayPo::getId, contactWayId)
                .last("limit 1"));
        if (contactWay == null) {
            return true;
        }

        if (isTrue(contactWay.getAutoTagEnable())) {
            markTags(contactWay, extStaffId, extCustomerId);
        }
        if (isTrue(contactWay.getCustomerRemarkEnable()) || isTrue(contactWay.getCustomerDescEnable())) {
            remarkCustomer(contactWay, extStaffId, extCustomerId);
        }

        boolean shouldSendWelcomeMsg = true;
        boolean shouldBlockAutoReply = false;
        if (isTrue(contactWay.getNicknameBlockEnable())) {
            shouldBlockAutoReply = matchAny(readStringList(contactWay.getNicknameBlockList()), customerName);
            shouldSendWelcomeMsg = false;
        }
        if (!shouldBlockAutoReply && Integer.valueOf(AUTO_REPLY_CUSTOM).equals(contactWay.getAutoReplyType())) {
            shouldSendWelcomeMsg = false;
            try {
                sendWelcomeMessage(contactWay.getExtCorpId(), welcomeCode, contactWay.getAutoReply());
            } catch (RuntimeException e) {
                // Other apps may consume the short-lived welcome code first; keep Go's tolerant behavior here.
                log.info("send contact way welcome message failed contactWayId={}", contactWay.getId(), e);
            }
        }
        if (!shouldBlockAutoReply && Integer.valueOf(AUTO_REPLY_DEFAULT).equals(contactWay.getAutoReplyType())) {
            shouldSendWelcomeMsg = true;
        }
        if (!shouldBlockAutoReply && Integer.valueOf(AUTO_REPLY_DISABLE).equals(contactWay.getAutoReplyType())) {
            shouldSendWelcomeMsg = false;
        }
        incrementAddCustomerCount(contactWay, extStaffId);
        return shouldSendWelcomeMsg;
    }

    private void validate(ContactWayRequest request) {
        if (request == null || !StringUtils.hasText(request.getName()) || request.getGroupId() == null) {
            throw new BizException(ErrorCode.BAD_REQUEST);
        }
    }

    private ContactWayPo getRaw(Long id, String extCorpId) {
        ContactWayPo item = contactWayMapper.selectOne(new LambdaQueryWrapper<ContactWayPo>()
                .eq(ContactWayPo::getExtCorpId, extCorpId)
                .eq(ContactWayPo::getId, id)
                .last("limit 1"));
        if (item == null) {
            throw new BizException(ErrorCode.ITEM_NOT_FOUND);
        }
        return item;
    }

    private void applyRequest(ContactWayPo item, ContactWayRequest request) {
        item.setName(request.getName());
        item.setGroupId(request.getGroupId());
        item.setAutoReplyType(defaultInt(request.getAutoReplyType(), 1));
        item.setAutoReply(toJson(request.getAutoReply()));
        item.setCustomerDesc(request.getCustomerDesc());
        item.setCustomerDescEnable(defaultInt(request.getCustomerDescEnable(), BooleanFlag.FALSE));
        item.setCustomerRemark(request.getCustomerRemark());
        item.setCustomerRemarkEnable(defaultInt(request.getCustomerRemarkEnable(), BooleanFlag.FALSE));
        item.setDailyAddCustomerLimitEnable(defaultInt(request.getDailyAddCustomerLimitEnable(), BooleanFlag.FALSE));
        item.setDailyAddCustomerLimit(defaultLong(request.getDailyAddCustomerLimit()));
        item.setScheduleEnable(defaultInt(request.getScheduleEnable(), BooleanFlag.FALSE));
        item.setAutoTagEnable(defaultInt(request.getAutoTagEnable(), BooleanFlag.FALSE));
        item.setCustomerTagExtIds(toJson(request.getCustomerTagExtIds()));
        item.setSkipVerify(defaultInt(request.getSkipVerify(), BooleanFlag.TRUE));
        item.setAutoSkipVerifyEnable(defaultInt(request.getAutoSkipVerifyEnable(), BooleanFlag.FALSE));
        item.setSkipVerifyStartTime(defaultLong(request.getSkipVerifyStartTime()));
        item.setSkipVerifyEndTime(defaultLong(request.getSkipVerifyEndTime()));
        item.setRemark(request.getRemark());
        item.setStaffControlEnable(defaultInt(request.getStaffControlEnable(), BooleanFlag.FALSE));
        item.setNicknameBlockEnable(defaultInt(request.getNicknameBlockEnable(), BooleanFlag.FALSE));
        item.setNicknameBlockList(toJson(request.getNicknameBlockList()));
        item.setAddCustomerCount(item.getAddCustomerCount() == null ? 0L : item.getAddCustomerCount());
    }

    private void refreshLocalStaffIds(ContactWayPo item) {
        Set<String> staffIds = new LinkedHashSet<>();
        boolean scheduleEnabled = isTrue(item.getScheduleEnable());
        if (scheduleEnabled) {
            List<ContactWaySchedulePo> schedules = scheduleMapper.selectList(new LambdaQueryWrapper<ContactWaySchedulePo>()
                    .eq(ContactWaySchedulePo::getContactWayId, item.getId()));
            for (ContactWaySchedulePo schedule : schedules) {
                if (!isScheduleActive(schedule)) {
                    continue;
                }
                List<ContactWayScheduleStaffPo> staffs = scheduleStaffMapper.selectList(
                        new LambdaQueryWrapper<ContactWayScheduleStaffPo>()
                                .eq(ContactWayScheduleStaffPo::getContactWayScheduleId, schedule.getId()));
                for (ContactWayScheduleStaffPo staff : staffs) {
                    if (canUseStaff(item, staff.getDailyAddCustomerLimit(), staff.getDailyAddCustomerCount(), staff.getOnline())) {
                        staffIds.add(staff.getExtStaffId());
                    }
                }
            }
        } else {
            List<ContactWayStaffPo> staffs = contactWayStaffMapper.selectList(new LambdaQueryWrapper<ContactWayStaffPo>()
                    .eq(ContactWayStaffPo::getContactWayId, item.getId()));
            for (ContactWayStaffPo staff : staffs) {
                if (canUseStaff(item, staff.getDailyAddCustomerLimit(), staff.getDailyAddCustomerCount(), staff.getOnline())) {
                    staffIds.add(staff.getExtStaffId());
                }
            }
        }
        if (staffIds.isEmpty()) {
            List<ContactWayBackupStaffPo> backups = backupStaffMapper.selectList(new LambdaQueryWrapper<ContactWayBackupStaffPo>()
                    .eq(ContactWayBackupStaffPo::getContactWayId, item.getId()));
            for (ContactWayBackupStaffPo staff : backups) {
                if (StringUtils.hasText(staff.getExtStaffId())) {
                    staffIds.add(staff.getExtStaffId());
                }
            }
        }
        item.setExtStaffIds(toJson(new ArrayList<>(staffIds)));
        item.setSkipVerify(resolveSkipVerify(item));
    }

    private void scheduleRefreshJobs(ContactWayPo item) {
        if (isTrue(item.getAutoSkipVerifyEnable())) {
            scheduleAtTodaySecond(item, item.getSkipVerifyStartTime(), "SkipVerifyStartTime");
            scheduleAtTodaySecond(item, item.getSkipVerifyEndTime(), "SkipVerifyEndTime");
        }
        if (isTrue(item.getScheduleEnable())) {
            scheduleAt(LocalDate.now().plusDays(1).atStartOfDay().plusSeconds(1), item, "ScheduleEnable");
            List<ContactWaySchedulePo> schedules = scheduleMapper.selectList(new LambdaQueryWrapper<ContactWaySchedulePo>()
                    .eq(ContactWaySchedulePo::getContactWayId, item.getId()));
            for (ContactWaySchedulePo schedule : schedules) {
                scheduleAtTodaySecond(item, schedule.getStartTime(), "staff.StartTime");
                scheduleAtTodaySecond(item, schedule.getEndTime(), "staff.EndTime");
            }
        }
    }

    private void scheduleAtTodaySecond(ContactWayPo item, Long secondOfDay, String state) {
        if (secondOfDay == null || secondOfDay < 0) {
            return;
        }
        scheduleAt(LocalDate.now().atStartOfDay().plusSeconds(secondOfDay), item, state);
    }

    private void scheduleAt(LocalDateTime executeAt, ContactWayPo item, String state) {
        Duration delay = Duration.between(LocalDateTime.now(), executeAt);
        if (delay.isNegative() || delay.isZero()) {
            return;
        }
        Map<String, Object> payload = new HashMap<>();
        payload.put("topic", DelayedJobTopics.REFRESH_CONTACT_WAY);
        payload.put("id", item.getId());
        payload.put("extCorpId", item.getExtCorpId());
        long executeAtEpochSecond = executeAt.atZone(ZoneId.systemDefault()).toEpochSecond();
        String dedupeKey = "openscrm:delayed-job:contact-way:" + item.getId() + ":" + executeAtEpochSecond + ":" + state;
        delayedJobPublisher.publishOnce(dedupeKey, payload, delay);
    }

    private void markTags(ContactWayPo contactWay, String extStaffId, String extCustomerId) {
        List<String> tagIds = readStringList(contactWay.getCustomerTagExtIds());
        if (tagIds.isEmpty()) {
            return;
        }
        ExternalContactMarkTagRequest request = new ExternalContactMarkTagRequest();
        request.setUserid(extStaffId);
        request.setExternalUserId(extCustomerId);
        request.setAddTag(tagIds);
        try {
            weWorkClient.markExternalContactTag(
                    contactWay.getExtCorpId(), properties.getWeWork().getCustomerSecret(), request);
        } catch (RuntimeException e) {
            log.info("mark external contact tag failed contactWayId={}", contactWay.getId(), e);
        }
    }

    private void remarkCustomer(ContactWayPo contactWay, String extStaffId, String extCustomerId) {
        ExternalContactRemarkRequest request = new ExternalContactRemarkRequest();
        request.setUserid(extStaffId);
        request.setExternalUserId(extCustomerId);
        if (isTrue(contactWay.getCustomerRemarkEnable())) {
            request.setRemark(contactWay.getCustomerRemark());
        }
        if (isTrue(contactWay.getCustomerDescEnable())) {
            request.setDescription(contactWay.getCustomerDesc());
        }
        weWorkClient.remarkExternalContact(
                contactWay.getExtCorpId(), properties.getWeWork().getCustomerSecret(), request);
    }

    public void sendWelcomeMessage(String extCorpId, String welcomeCode, String welcomeMsgJson) {
        if (!StringUtils.hasText(welcomeCode) || !StringUtils.hasText(welcomeMsgJson)) {
            return;
        }
        SendWelcomeMessageRequest request = toWelcomeRequest(welcomeCode, welcomeMsgJson);
        weWorkClient.sendWelcomeMessage(extCorpId, properties.getWeWork().getCustomerSecret(), request);
    }

    private SendWelcomeMessageRequest toWelcomeRequest(String welcomeCode, String welcomeMsgJson) {
        try {
            JsonNode root = objectMapper.readTree(welcomeMsgJson);
            SendWelcomeMessageRequest request = new SendWelcomeMessageRequest();
            request.setWelcomeCode(welcomeCode);
            JsonNode text = root.get("text");
            if (text != null && !text.isNull() && StringUtils.hasText(text.asText())) {
                SendWelcomeMessageRequest.Text textBody = new SendWelcomeMessageRequest.Text();
                textBody.setContent(text.asText());
                request.setText(textBody);
            }
            JsonNode attachments = root.get("attachments");
            if (attachments != null && attachments.isArray()) {
                for (JsonNode attachment : attachments) {
                    request.getAttachments().add(normalizeAttachment(attachment));
                }
            }
            return request;
        } catch (JsonProcessingException e) {
            throw new BizException(ErrorCode.BAD_REQUEST);
        }
    }

    private JsonNode normalizeAttachment(JsonNode attachment) {
        if (!attachment.isObject()) {
            return attachment;
        }
        ObjectNode copy = attachment.deepCopy();
        JsonNode miniprogram = copy.get("miniprogram");
        if (miniprogram != null && miniprogram.isObject()) {
            ObjectNode miniprogramCopy = (ObjectNode) miniprogram;
            JsonNode appId = miniprogramCopy.get("app_id");
            if (appId != null && !miniprogramCopy.has("appid")) {
                miniprogramCopy.set("appid", appId);
                miniprogramCopy.remove("app_id");
            }
        }
        return copy;
    }

    private void incrementAddCustomerCount(ContactWayPo contactWay, String extStaffId) {
        ContactWayStaffPo staff = contactWayStaffMapper.selectOne(new LambdaQueryWrapper<ContactWayStaffPo>()
                .eq(ContactWayStaffPo::getContactWayId, contactWay.getId())
                .eq(ContactWayStaffPo::getExtStaffId, extStaffId)
                .last("limit 1"));
        if (staff != null) {
            long nextDaily = defaultLong(staff.getDailyAddCustomerCount()) + 1;
            contactWayStaffMapper.update(null, new LambdaUpdateWrapper<ContactWayStaffPo>()
                    .eq(ContactWayStaffPo::getId, staff.getId())
                    .set(ContactWayStaffPo::getAddCustomerCount, defaultLong(staff.getAddCustomerCount()) + 1)
                    .set(ContactWayStaffPo::getDailyAddCustomerCount, nextDaily)
                    .set(ContactWayStaffPo::getUpdatedAt, LocalDateTime.now()));
            refreshIfLimitReached(contactWay, staff.getDailyAddCustomerLimit(), nextDaily);
            return;
        }
        ContactWayScheduleStaffPo scheduleStaff = scheduleStaffMapper.selectOne(
                new LambdaQueryWrapper<ContactWayScheduleStaffPo>()
                        .eq(ContactWayScheduleStaffPo::getContactWayId, contactWay.getId())
                        .eq(ContactWayScheduleStaffPo::getExtStaffId, extStaffId)
                        .last("limit 1"));
        if (scheduleStaff != null) {
            long nextDaily = defaultLong(scheduleStaff.getDailyAddCustomerCount()) + 1;
            scheduleStaffMapper.update(null, new LambdaUpdateWrapper<ContactWayScheduleStaffPo>()
                    .eq(ContactWayScheduleStaffPo::getId, scheduleStaff.getId())
                    .set(ContactWayScheduleStaffPo::getAddCustomerCount, defaultLong(scheduleStaff.getAddCustomerCount()) + 1)
                    .set(ContactWayScheduleStaffPo::getDailyAddCustomerCount, nextDaily)
                    .set(ContactWayScheduleStaffPo::getUpdatedAt, LocalDateTime.now()));
            refreshIfLimitReached(contactWay, scheduleStaff.getDailyAddCustomerLimit(), nextDaily);
            return;
        }
        ContactWayBackupStaffPo backupStaff = backupStaffMapper.selectOne(
                new LambdaQueryWrapper<ContactWayBackupStaffPo>()
                        .eq(ContactWayBackupStaffPo::getContactWayId, contactWay.getId())
                        .eq(ContactWayBackupStaffPo::getExtStaffId, extStaffId)
                        .last("limit 1"));
        if (backupStaff != null) {
            long nextDaily = defaultLong(backupStaff.getDailyAddCustomerCount()) + 1;
            backupStaffMapper.update(null, new LambdaUpdateWrapper<ContactWayBackupStaffPo>()
                    .eq(ContactWayBackupStaffPo::getId, backupStaff.getId())
                    .set(ContactWayBackupStaffPo::getAddCustomerCount, defaultLong(backupStaff.getAddCustomerCount()) + 1)
                    .set(ContactWayBackupStaffPo::getDailyAddCustomerCount, nextDaily)
                    .set(ContactWayBackupStaffPo::getUpdatedAt, LocalDateTime.now()));
            refreshIfLimitReached(contactWay, backupStaff.getDailyAddCustomerLimit(), nextDaily);
        }
    }

    private void refreshIfLimitReached(ContactWayPo contactWay, Long staffDailyLimit, long nextDailyCount) {
        if (isTrue(contactWay.getDailyAddCustomerLimitEnable())
                && staffDailyLimit != null
                && staffDailyLimit >= 1
                && nextDailyCount >= staffDailyLimit) {
            refresh(contactWay.getId(), contactWay.getExtCorpId());
        }
    }

    private boolean canUseStaff(ContactWayPo item, Long limit, Long current, Integer online) {
        if (isTrue(item.getDailyAddCustomerLimitEnable())
                && limit != null && limit > 0 && defaultLong(current) >= limit) {
            return false;
        }
        return !isTrue(item.getStaffControlEnable()) || !Integer.valueOf(BooleanFlag.FALSE).equals(online);
    }

    private Integer resolveSkipVerify(ContactWayPo item) {
        if (!isTrue(item.getAutoSkipVerifyEnable())) {
            return BooleanFlag.TRUE;
        }
        long now = LocalTime.now().toSecondOfDay();
        Long start = item.getSkipVerifyStartTime();
        Long end = item.getSkipVerifyEndTime();
        if (start == null || end == null) {
            return item.getSkipVerify();
        }
        return now > start && now < end ? BooleanFlag.TRUE : BooleanFlag.FALSE;
    }

    private boolean isScheduleActive(ContactWaySchedulePo schedule) {
        long now = LocalTime.now().toSecondOfDay();
        if (schedule.getStartTime() != null && now < schedule.getStartTime()) {
            return false;
        }
        if (schedule.getEndTime() != null && now > schedule.getEndTime()) {
            return false;
        }
        if (!StringUtils.hasText(schedule.getWeekdays())) {
            return true;
        }
        String weekday = weekday();
        return schedule.getWeekdays().contains(weekday);
    }

    private String weekday() {
        DayOfWeek day = LocalDateTime.now().getDayOfWeek();
        return day.getDisplayName(TextStyle.FULL, Locale.ENGLISH).toLowerCase(Locale.ENGLISH);
    }

    private cn.openscrm.api.wework.ContactWayRequest toWxRequest(ContactWayPo item, boolean update) {
        cn.openscrm.api.wework.ContactWayRequest request = new cn.openscrm.api.wework.ContactWayRequest();
        if (update) {
            request.setConfigId(item.getConfigId());
        }
        request.setTemp(false);
        request.setRemark(item.getRemark());
        request.setScene(2);
        request.setSkipVerify(isTrue(item.getSkipVerify()));
        request.setState(item.getState());
        request.setType(2);
        request.setUser(readStringList(item.getExtStaffIds()));
        return request;
    }

    private void replaceAssociations(ContactWayPo item, ContactWayRequest request, String extCreatorId) {
        syncStaffs(item, request.getStaffs(), extCreatorId);
        syncBackupStaffs(item, request.getBackupStaffs(), extCreatorId);
        syncSchedules(item, request.getSchedules(), extCreatorId);
    }

    private void deleteAssociations(Long contactWayId) {
        contactWayStaffMapper.delete(new LambdaQueryWrapper<ContactWayStaffPo>()
                .eq(ContactWayStaffPo::getContactWayId, contactWayId));
        backupStaffMapper.delete(new LambdaQueryWrapper<ContactWayBackupStaffPo>()
                .eq(ContactWayBackupStaffPo::getContactWayId, contactWayId));
        scheduleStaffMapper.delete(new LambdaQueryWrapper<ContactWayScheduleStaffPo>()
                .eq(ContactWayScheduleStaffPo::getContactWayId, contactWayId));
        scheduleMapper.delete(new LambdaQueryWrapper<ContactWaySchedulePo>()
                .eq(ContactWaySchedulePo::getContactWayId, contactWayId));
    }

    private void syncStaffs(ContactWayPo item, List<ContactWayStaffRequest> requests, String extCreatorId) {
        List<ContactWayStaffPo> existing = contactWayStaffMapper.selectList(new LambdaQueryWrapper<ContactWayStaffPo>()
                .eq(ContactWayStaffPo::getContactWayId, item.getId()));
        Map<Long, ContactWayStaffPo> byId = new HashMap<>();
        Map<String, ContactWayStaffPo> byStaff = new HashMap<>();
        for (ContactWayStaffPo row : existing) {
            byId.put(row.getId(), row);
            byStaff.put(row.getExtStaffId(), row);
        }
        Set<Long> keepIds = new LinkedHashSet<>();
        for (ContactWayStaffRequest request : emptyListIfNull(requests)) {
            ContactWayStaffPo old = request.getId() == null ? byStaff.get(request.getExtStaffId()) : byId.get(request.getId());
            ContactWayStaffPo row = toStaffPo(item, request, extCreatorId);
            preserveStaffCounters(row, old);
            if (old == null) {
                contactWayStaffMapper.insert(row);
            } else {
                row.setId(old.getId());
                contactWayStaffMapper.updateById(row);
            }
            keepIds.add(row.getId());
        }
        deleteMissingContactWayStaffs(item.getId(), keepIds);
    }

    private void syncBackupStaffs(ContactWayPo item, List<ContactWayStaffRequest> requests, String extCreatorId) {
        List<ContactWayBackupStaffPo> existing = backupStaffMapper.selectList(new LambdaQueryWrapper<ContactWayBackupStaffPo>()
                .eq(ContactWayBackupStaffPo::getContactWayId, item.getId()));
        Map<Long, ContactWayBackupStaffPo> byId = new HashMap<>();
        Map<String, ContactWayBackupStaffPo> byStaff = new HashMap<>();
        for (ContactWayBackupStaffPo row : existing) {
            byId.put(row.getId(), row);
            byStaff.put(row.getExtStaffId(), row);
        }
        Set<Long> keepIds = new LinkedHashSet<>();
        for (ContactWayStaffRequest request : emptyListIfNull(requests)) {
            ContactWayBackupStaffPo old = request.getId() == null ? byStaff.get(request.getExtStaffId()) : byId.get(request.getId());
            ContactWayBackupStaffPo row = toBackupStaffPo(item, request, extCreatorId);
            preserveBackupCounters(row, old);
            if (old == null) {
                backupStaffMapper.insert(row);
            } else {
                row.setId(old.getId());
                backupStaffMapper.updateById(row);
            }
            keepIds.add(row.getId());
        }
        deleteMissingBackupStaffs(item.getId(), keepIds);
    }

    private void syncSchedules(ContactWayPo item, List<ContactWayScheduleRequest> requests, String extCreatorId) {
        List<ContactWaySchedulePo> existing = scheduleMapper.selectList(new LambdaQueryWrapper<ContactWaySchedulePo>()
                .eq(ContactWaySchedulePo::getContactWayId, item.getId()));
        Map<Long, ContactWaySchedulePo> byId = new HashMap<>();
        for (ContactWaySchedulePo row : existing) {
            byId.put(row.getId(), row);
        }
        Set<Long> keepIds = new LinkedHashSet<>();
        LocalDateTime now = LocalDateTime.now();
        for (ContactWayScheduleRequest request : emptyListIfNull(requests)) {
            ContactWaySchedulePo old = request.getId() == null ? null : byId.get(request.getId());
            ContactWaySchedulePo row = new ContactWaySchedulePo();
            row.setId(old == null ? (request.getId() == null ? idGenerator.nextId() : request.getId()) : old.getId());
            row.setExtCorpId(item.getExtCorpId());
            row.setExtCreatorId(extCreatorId);
            row.setContactWayId(item.getId());
            row.setDailyAddCustomerLimit(defaultLong(request.getDailyAddCustomerLimit()));
            row.setWeekdays(toJson(request.getWeekdays()));
            row.setStartTime(request.getStartTime());
            row.setEndTime(request.getEndTime());
            row.setCreatedAt(old == null ? now : old.getCreatedAt());
            row.setUpdatedAt(now);
            if (old == null) {
                scheduleMapper.insert(row);
            } else {
                scheduleMapper.updateById(row);
            }
            syncScheduleStaffs(item, row.getId(), request.getStaffs(), extCreatorId);
            keepIds.add(row.getId());
        }
        deleteMissingSchedules(item.getId(), keepIds);
    }

    private void syncScheduleStaffs(ContactWayPo item,
                                    Long scheduleId,
                                    List<ContactWayStaffRequest> requests,
                                    String extCreatorId) {
        List<ContactWayScheduleStaffPo> existing = scheduleStaffMapper.selectList(
                new LambdaQueryWrapper<ContactWayScheduleStaffPo>()
                        .eq(ContactWayScheduleStaffPo::getContactWayScheduleId, scheduleId));
        Map<Long, ContactWayScheduleStaffPo> byId = new HashMap<>();
        Map<String, ContactWayScheduleStaffPo> byStaff = new HashMap<>();
        for (ContactWayScheduleStaffPo row : existing) {
            byId.put(row.getId(), row);
            byStaff.put(row.getExtStaffId(), row);
        }
        Set<Long> keepIds = new LinkedHashSet<>();
        ContactWaySchedulePo schedule = new ContactWaySchedulePo();
        schedule.setId(scheduleId);
        for (ContactWayStaffRequest request : emptyListIfNull(requests)) {
            ContactWayScheduleStaffPo old = request.getId() == null ? byStaff.get(request.getExtStaffId()) : byId.get(request.getId());
            ContactWayScheduleStaffPo row = toScheduleStaffPo(item, schedule, request, extCreatorId);
            preserveScheduleStaffCounters(row, old);
            if (old == null) {
                scheduleStaffMapper.insert(row);
            } else {
                row.setId(old.getId());
                scheduleStaffMapper.updateById(row);
            }
            keepIds.add(row.getId());
        }
        deleteMissingScheduleStaffs(scheduleId, keepIds);
    }

    private void preserveStaffCounters(ContactWayStaffPo row, ContactWayStaffPo old) {
        if (old == null) {
            return;
        }
        row.setAddCustomerCount(defaultLong(old.getAddCustomerCount()));
        row.setDailyAddCustomerCount(defaultLong(old.getDailyAddCustomerCount()));
        row.setOnline(old.getOnline());
        row.setCreatedAt(old.getCreatedAt());
    }

    private void preserveBackupCounters(ContactWayBackupStaffPo row, ContactWayBackupStaffPo old) {
        if (old == null) {
            return;
        }
        row.setAddCustomerCount(defaultLong(old.getAddCustomerCount()));
        row.setDailyAddCustomerCount(defaultLong(old.getDailyAddCustomerCount()));
        row.setOnline(old.getOnline());
        row.setCreatedAt(old.getCreatedAt());
    }

    private void preserveScheduleStaffCounters(ContactWayScheduleStaffPo row, ContactWayScheduleStaffPo old) {
        if (old == null) {
            return;
        }
        row.setAddCustomerCount(defaultLong(old.getAddCustomerCount()));
        row.setDailyAddCustomerCount(defaultLong(old.getDailyAddCustomerCount()));
        row.setOnline(old.getOnline());
        row.setCreatedAt(old.getCreatedAt());
    }

    private void deleteMissingContactWayStaffs(Long contactWayId, Set<Long> keepIds) {
        LambdaQueryWrapper<ContactWayStaffPo> wrapper = new LambdaQueryWrapper<ContactWayStaffPo>()
                .eq(ContactWayStaffPo::getContactWayId, contactWayId);
        if (!keepIds.isEmpty()) {
            wrapper.notIn(ContactWayStaffPo::getId, keepIds);
        }
        contactWayStaffMapper.delete(wrapper);
    }

    private void deleteMissingBackupStaffs(Long contactWayId, Set<Long> keepIds) {
        LambdaQueryWrapper<ContactWayBackupStaffPo> wrapper = new LambdaQueryWrapper<ContactWayBackupStaffPo>()
                .eq(ContactWayBackupStaffPo::getContactWayId, contactWayId);
        if (!keepIds.isEmpty()) {
            wrapper.notIn(ContactWayBackupStaffPo::getId, keepIds);
        }
        backupStaffMapper.delete(wrapper);
    }

    private void deleteMissingSchedules(Long contactWayId, Set<Long> keepIds) {
        LambdaQueryWrapper<ContactWaySchedulePo> wrapper = new LambdaQueryWrapper<ContactWaySchedulePo>()
                .eq(ContactWaySchedulePo::getContactWayId, contactWayId);
        if (!keepIds.isEmpty()) {
            wrapper.notIn(ContactWaySchedulePo::getId, keepIds);
        }
        List<ContactWaySchedulePo> removed = scheduleMapper.selectList(wrapper);
        if (removed.isEmpty()) {
            return;
        }
        List<Long> removedIds = new ArrayList<>();
        for (ContactWaySchedulePo row : removed) {
            removedIds.add(row.getId());
        }
        scheduleStaffMapper.delete(new LambdaQueryWrapper<ContactWayScheduleStaffPo>()
                .in(ContactWayScheduleStaffPo::getContactWayScheduleId, removedIds));
        scheduleMapper.deleteBatchIds(removedIds);
    }

    private void deleteMissingScheduleStaffs(Long scheduleId, Set<Long> keepIds) {
        LambdaQueryWrapper<ContactWayScheduleStaffPo> wrapper = new LambdaQueryWrapper<ContactWayScheduleStaffPo>()
                .eq(ContactWayScheduleStaffPo::getContactWayScheduleId, scheduleId);
        if (!keepIds.isEmpty()) {
            wrapper.notIn(ContactWayScheduleStaffPo::getId, keepIds);
        }
        scheduleStaffMapper.delete(wrapper);
    }

    private void saveAssociations(ContactWayPo item, ContactWayRequest request, String extCreatorId) {
        if (request.getStaffs() != null) {
            for (ContactWayStaffRequest staff : request.getStaffs()) {
                contactWayStaffMapper.insert(toStaffPo(item, staff, extCreatorId));
            }
        }
        if (request.getBackupStaffs() != null) {
            for (ContactWayStaffRequest staff : request.getBackupStaffs()) {
                backupStaffMapper.insert(toBackupStaffPo(item, staff, extCreatorId));
            }
        }
        if (request.getSchedules() != null) {
            for (ContactWayScheduleRequest scheduleRequest : request.getSchedules()) {
                ContactWaySchedulePo schedule = new ContactWaySchedulePo();
                schedule.setId(scheduleRequest.getId() == null ? idGenerator.nextId() : scheduleRequest.getId());
                schedule.setExtCorpId(item.getExtCorpId());
                schedule.setExtCreatorId(extCreatorId);
                schedule.setContactWayId(item.getId());
                schedule.setDailyAddCustomerLimit(defaultLong(scheduleRequest.getDailyAddCustomerLimit()));
                schedule.setWeekdays(toJson(scheduleRequest.getWeekdays()));
                schedule.setStartTime(scheduleRequest.getStartTime());
                schedule.setEndTime(scheduleRequest.getEndTime());
                schedule.setCreatedAt(LocalDateTime.now());
                schedule.setUpdatedAt(LocalDateTime.now());
                scheduleMapper.insert(schedule);
                if (scheduleRequest.getStaffs() != null) {
                    for (ContactWayStaffRequest staff : scheduleRequest.getStaffs()) {
                        ContactWayScheduleStaffPo po = toScheduleStaffPo(item, schedule, staff, extCreatorId);
                        scheduleStaffMapper.insert(po);
                    }
                }
            }
        }
    }

    private ContactWayStaffPo toStaffPo(ContactWayPo item, ContactWayStaffRequest request, String extCreatorId) {
        ContactWayStaffPo po = new ContactWayStaffPo();
        fillStaffBase(po, item, request, extCreatorId);
        return po;
    }

    private ContactWayBackupStaffPo toBackupStaffPo(ContactWayPo item, ContactWayStaffRequest request, String extCreatorId) {
        ContactWayBackupStaffPo po = new ContactWayBackupStaffPo();
        fillStaffBase(po, item, request, extCreatorId);
        return po;
    }

    private ContactWayScheduleStaffPo toScheduleStaffPo(ContactWayPo item,
                                                        ContactWaySchedulePo schedule,
                                                        ContactWayStaffRequest request,
                                                        String extCreatorId) {
        ContactWayScheduleStaffPo po = new ContactWayScheduleStaffPo();
        fillStaffBase(po, item, request, extCreatorId);
        po.setContactWayScheduleId(schedule.getId());
        return po;
    }

    private void fillStaffBase(Object target, ContactWayPo item, ContactWayStaffRequest request, String extCreatorId) {
        StaffPo staff = findStaff(item.getExtCorpId(), request.getExtStaffId());
        LocalDateTime now = LocalDateTime.now();
        if (target instanceof ContactWayStaffPo) {
            ContactWayStaffPo po = (ContactWayStaffPo) target;
            po.setId(request.getId() == null ? idGenerator.nextId() : request.getId());
            po.setExtCorpId(item.getExtCorpId());
            po.setExtCreatorId(extCreatorId);
            po.setContactWayId(item.getId());
            po.setDailyAddCustomerLimit(defaultLong(request.getDailyAddCustomerLimit()));
            po.setAddCustomerCount(0L);
            po.setDailyAddCustomerCount(0L);
            po.setExtStaffId(request.getExtStaffId());
            po.setName(staff == null ? null : staff.getName());
            po.setAvatarUrl(staff == null ? null : staff.getAvatarUrl());
            po.setOnline(BooleanFlag.TRUE);
            po.setCreatedAt(now);
            po.setUpdatedAt(now);
        } else if (target instanceof ContactWayBackupStaffPo) {
            ContactWayBackupStaffPo po = (ContactWayBackupStaffPo) target;
            po.setId(request.getId() == null ? idGenerator.nextId() : request.getId());
            po.setExtCorpId(item.getExtCorpId());
            po.setExtCreatorId(extCreatorId);
            po.setContactWayId(item.getId());
            po.setDailyAddCustomerLimit(defaultLong(request.getDailyAddCustomerLimit()));
            po.setAddCustomerCount(0L);
            po.setDailyAddCustomerCount(0L);
            po.setExtStaffId(request.getExtStaffId());
            po.setName(staff == null ? null : staff.getName());
            po.setAvatarUrl(staff == null ? null : staff.getAvatarUrl());
            po.setOnline(BooleanFlag.TRUE);
            po.setCreatedAt(now);
            po.setUpdatedAt(now);
        } else if (target instanceof ContactWayScheduleStaffPo) {
            ContactWayScheduleStaffPo po = (ContactWayScheduleStaffPo) target;
            po.setId(request.getId() == null ? idGenerator.nextId() : request.getId());
            po.setExtCorpId(item.getExtCorpId());
            po.setExtCreatorId(extCreatorId);
            po.setContactWayId(item.getId());
            po.setDailyAddCustomerLimit(defaultLong(request.getDailyAddCustomerLimit()));
            po.setAddCustomerCount(0L);
            po.setDailyAddCustomerCount(0L);
            po.setExtStaffId(request.getExtStaffId());
            po.setName(staff == null ? null : staff.getName());
            po.setAvatarUrl(staff == null ? null : staff.getAvatarUrl());
            po.setOnline(BooleanFlag.TRUE);
            po.setCreatedAt(now);
            po.setUpdatedAt(now);
        }
    }

    private StaffPo findStaff(String extCorpId, String extStaffId) {
        if (!StringUtils.hasText(extStaffId)) {
            return null;
        }
        return staffMapper.selectOne(new LambdaQueryWrapper<StaffPo>()
                .eq(StaffPo::getExtCorpId, extCorpId)
                .eq(StaffPo::getExtId, extStaffId)
                .isNull(StaffPo::getDeletedAt)
                .last("limit 1"));
    }

    private ContactWayResponse toResponse(ContactWayPo item) {
        ContactWayResponse response = new ContactWayResponse();
        response.setContactWay(item);
        response.setGroup(contactWayGroupMapper.selectOne(new LambdaQueryWrapper<ContactWayGroupPo>()
                .eq(ContactWayGroupPo::getId, item.getGroupId())
                .last("limit 1")));
        response.setCustomerTags(findCustomerTags(item));
        response.setStaffs(contactWayStaffMapper.selectList(new LambdaQueryWrapper<ContactWayStaffPo>()
                .eq(ContactWayStaffPo::getContactWayId, item.getId())));
        response.setBackupStaffs(backupStaffMapper.selectList(new LambdaQueryWrapper<ContactWayBackupStaffPo>()
                .eq(ContactWayBackupStaffPo::getContactWayId, item.getId())));
        List<ContactWaySchedulePo> schedules = scheduleMapper.selectList(new LambdaQueryWrapper<ContactWaySchedulePo>()
                .eq(ContactWaySchedulePo::getContactWayId, item.getId()));
        for (ContactWaySchedulePo schedule : schedules) {
            ContactWayResponse.ScheduleWithStaffs scheduleResponse = new ContactWayResponse.ScheduleWithStaffs();
            scheduleResponse.setSchedule(schedule);
            scheduleResponse.setStaffs(scheduleStaffMapper.selectList(new LambdaQueryWrapper<ContactWayScheduleStaffPo>()
                    .eq(ContactWayScheduleStaffPo::getContactWayScheduleId, schedule.getId())));
            response.getSchedules().add(scheduleResponse);
        }
        return response;
    }

    private List<TagPo> findCustomerTags(ContactWayPo item) {
        List<String> tagExtIds = readStringList(item.getCustomerTagExtIds());
        if (tagExtIds.isEmpty()) {
            return Collections.emptyList();
        }
        return tagMapper.selectList(new LambdaQueryWrapper<TagPo>()
                .eq(TagPo::getExtCorpId, item.getExtCorpId())
                .in(TagPo::getExtId, tagExtIds)
                .isNull(TagPo::getDeletedAt));
    }

    private List<Long> findContactWayIdsByStaff(List<String> extStaffIds) {
        if (CollectionUtils.isEmpty(extStaffIds)) {
            return Collections.emptyList();
        }
        Set<Long> ids = new LinkedHashSet<>();
        for (ContactWayStaffPo po : contactWayStaffMapper.selectList(new LambdaQueryWrapper<ContactWayStaffPo>()
                .in(ContactWayStaffPo::getExtStaffId, extStaffIds))) {
            ids.add(po.getContactWayId());
        }
        for (ContactWayBackupStaffPo po : backupStaffMapper.selectList(new LambdaQueryWrapper<ContactWayBackupStaffPo>()
                .in(ContactWayBackupStaffPo::getExtStaffId, extStaffIds))) {
            ids.add(po.getContactWayId());
        }
        for (ContactWayScheduleStaffPo po : scheduleStaffMapper.selectList(
                new LambdaQueryWrapper<ContactWayScheduleStaffPo>()
                        .in(ContactWayScheduleStaffPo::getExtStaffId, extStaffIds))) {
            ids.add(po.getContactWayId());
        }
        return new ArrayList<>(ids);
    }

    private boolean isTrue(Integer value) {
        return value != null && value == BooleanFlag.TRUE;
    }

    private boolean matchAny(List<String> keywords, String text) {
        if (!StringUtils.hasText(text)) {
            return false;
        }
        for (String keyword : keywords) {
            if (StringUtils.hasText(keyword) && text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private Long parseContactWayId(String value) {
        try {
            return StringUtils.hasText(value) ? Long.valueOf(value) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private int defaultInt(Integer value, int defaultValue) {
        return value == null ? defaultValue : value;
    }

    private long defaultLong(Long value) {
        return value == null ? 0L : value;
    }

    private <T> List<T> emptyListIfNull(List<T> values) {
        return values == null ? Collections.emptyList() : values;
    }

    private String toJson(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new BizException(ErrorCode.BAD_REQUEST);
        }
    }

    private List<String> readStringList(String json) {
        if (!StringUtils.hasText(json)) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(json,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
