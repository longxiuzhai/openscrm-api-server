package cn.openscrm.api.customer.service;

import cn.openscrm.api.common.id.SnowflakeIdGenerator;
import cn.openscrm.api.config.OpenScrmProperties;
import cn.openscrm.api.contactway.service.ContactWayService;
import cn.openscrm.api.groupchatautojoin.service.GroupChatAutoJoinService;
import cn.openscrm.api.persistence.entity.CustomerInfoPo;
import cn.openscrm.api.persistence.entity.CustomerPo;
import cn.openscrm.api.persistence.entity.CustomerStaffPo;
import cn.openscrm.api.persistence.entity.CustomerStaffTagPo;
import cn.openscrm.api.persistence.entity.StaffPo;
import cn.openscrm.api.persistence.entity.WelcomeMsgPo;
import cn.openscrm.api.persistence.mapper.CustomerInfoPoMapper;
import cn.openscrm.api.persistence.mapper.CustomerPoMapper;
import cn.openscrm.api.persistence.mapper.CustomerStaffPoMapper;
import cn.openscrm.api.persistence.mapper.CustomerStaffTagPoMapper;
import cn.openscrm.api.persistence.mapper.StaffPoMapper;
import cn.openscrm.api.persistence.mapper.WelcomeMsgPoMapper;
import cn.openscrm.api.wework.ExternalContactFollowUser;
import cn.openscrm.api.wework.ExternalContactGetResponse;
import cn.openscrm.api.wework.ExternalContactInfo;
import cn.openscrm.api.wework.WeWorkApiException;
import cn.openscrm.api.wework.WeWorkClient;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerSyncService {

    private static final int ERR_EXTERNAL_CONTACT_NOT_FOUND = 84061;

    private final OpenScrmProperties properties;
    private final WeWorkClient weWorkClient;
    private final CustomerPoMapper customerMapper;
    private final CustomerStaffPoMapper customerStaffMapper;
    private final CustomerStaffTagPoMapper customerStaffTagMapper;
    private final CustomerInfoPoMapper customerInfoMapper;
    private final StaffPoMapper staffMapper;
    private final WelcomeMsgPoMapper welcomeMsgMapper;
    private final ContactWayService contactWayService;
    private final GroupChatAutoJoinService groupChatAutoJoinService;
    private final SnowflakeIdGenerator idGenerator;
    private final ObjectMapper objectMapper;

    @Transactional(rollbackFor = Exception.class)
    public void syncSingle(String extStaffId, String extCustomerId, String welcomeCode, String state) {
        if (!StringUtils.hasText(extStaffId) || !StringUtils.hasText(extCustomerId)) {
            log.warn("skip sync customer job because staff/customer is blank extStaffId={}, extCustomerId={}",
                    extStaffId, extCustomerId);
            return;
        }
        String extCorpId = properties.getWeWork().getExtCorpId();
        ExternalContactGetResponse response;
        try {
            response = weWorkClient.getExternalContact(
                    extCorpId, properties.getWeWork().getCustomerSecret(), extCustomerId);
        } catch (WeWorkApiException e) {
            if (e.getErrCode() == ERR_EXTERNAL_CONTACT_NOT_FOUND) {
                log.info("{} has no external contact", extCustomerId);
                return;
            }
            throw e;
        }
        ExternalContactInfo contact = response.getExternalContact();
        if (contact == null || !StringUtils.hasText(contact.getExternalUserId())) {
            log.warn("empty external contact info extCustomerId={}", extCustomerId);
            return;
        }
        upsertCustomer(extCorpId, contact);
        for (ExternalContactFollowUser followUser : defaultList(response.getFollowUser())) {
            if (extStaffId.equals(followUser.getUserid())) {
                CustomerStaffPo relation = upsertCustomerStaff(extCorpId, extCustomerId, followUser);
                refreshCustomerTags(extCorpId, relation, followUser.getTags());
                upsertCustomerPortrait(extCorpId, extCustomerId, extStaffId);
                boolean shouldSendDefault = contactWayService.dealAddCustomerEvent(
                        extStaffId,
                        extCustomerId,
                        StringUtils.hasText(state) ? state : followUser.getState(),
                        welcomeCode,
                        contact.getName());
                groupChatAutoJoinService.dealAddCustomerEvent(
                        extCorpId,
                        extStaffId,
                        extCustomerId,
                        StringUtils.hasText(state) ? state : followUser.getState());
                if (shouldSendDefault) {
                    sendDefaultWelcomeMessage(extCorpId, extStaffId, welcomeCode);
                }
                return;
            }
        }
        log.info("external contact has no follow user relation extStaffId={}, extCustomerId={}",
                extStaffId, extCustomerId);
    }

    private void upsertCustomer(String extCorpId, ExternalContactInfo source) {
        CustomerPo existing = customerMapper.selectOne(new LambdaQueryWrapper<CustomerPo>()
                .eq(CustomerPo::getExtId, source.getExternalUserId())
                .last("limit 1"));
        CustomerPo item = existing == null ? new CustomerPo() : existing;
        if (item.getId() == null) {
            item.setId(idGenerator.nextId());
            item.setCreatedAt(LocalDateTime.now());
        }
        item.setExtCorpId(extCorpId);
        item.setExtCreatorId(source.getExternalUserId());
        item.setExtId(source.getExternalUserId());
        item.setName(source.getName());
        item.setPosition(source.getPosition());
        item.setCorpName(source.getCorpName());
        item.setAvatar(thumbAvatar(source.getAvatar()));
        item.setType(source.getType());
        item.setGender(source.getGender());
        item.setUnionid(source.getUnionid());
        item.setExternalProfile(toJson(source.getExternalProfile()));
        item.setDeletedAt(null);
        item.setUpdatedAt(LocalDateTime.now());
        if (existing == null) {
            customerMapper.insert(item);
        } else {
            customerMapper.updateById(item);
        }
    }

    private void sendDefaultWelcomeMessage(String extCorpId, String extStaffId, String welcomeCode) {
        if (!StringUtils.hasText(welcomeCode)) {
            return;
        }
        StaffPo staff = staffMapper.selectOne(new LambdaQueryWrapper<StaffPo>()
                .eq(StaffPo::getExtCorpId, extCorpId)
                .eq(StaffPo::getExtId, extStaffId)
                .last("limit 1"));
        if (staff == null || staff.getWelcomeMsgId() == null) {
            return;
        }
        WelcomeMsgPo welcomeMsg = welcomeMsgMapper.selectById(staff.getWelcomeMsgId());
        if (welcomeMsg == null || !StringUtils.hasText(welcomeMsg.getWelcomeMsg())) {
            return;
        }
        contactWayService.sendWelcomeMessage(extCorpId, welcomeCode, welcomeMsg.getWelcomeMsg());
    }

    private CustomerStaffPo upsertCustomerStaff(String extCorpId,
                                                String extCustomerId,
                                                ExternalContactFollowUser source) {
        CustomerStaffPo existing = customerStaffMapper.selectOne(new LambdaQueryWrapper<CustomerStaffPo>()
                .eq(CustomerStaffPo::getExtStaffId, source.getUserid())
                .eq(CustomerStaffPo::getExtCustomerId, extCustomerId)
                .last("limit 1"));
        CustomerStaffPo item = existing == null ? new CustomerStaffPo() : existing;
        if (item.getId() == null) {
            item.setId(idGenerator.nextId());
            item.setCreatedAt(LocalDateTime.now());
        }
        item.setExtCorpId(extCorpId);
        item.setExtCreatorId(source.getUserid());
        item.setExtStaffId(source.getUserid());
        item.setExtCustomerId(extCustomerId);
        item.setRemark(source.getRemark());
        item.setDescription(source.getDescription());
        item.setCreatetime(toLocalDateTime(source.getCreatetime()));
        item.setRemarkCorpName(source.getRemarkCorpName());
        item.setRemarkMobiles(toJson(defaultList(source.getRemarkMobiles())));
        item.setAddWay(source.getAddWay());
        item.setOperUserId(source.getOperUserId());
        item.setState(source.getState());
        item.setSignature(md5(extCustomerId + source.getUserid()));
        item.setDeletedAt(null);
        item.setUpdatedAt(LocalDateTime.now());
        if (existing == null) {
            customerStaffMapper.insert(item);
        } else {
            customerStaffMapper.updateById(item);
        }
        return item;
    }

    private void refreshCustomerTags(String extCorpId,
                                     CustomerStaffPo relation,
                                     List<ExternalContactFollowUser.Tag> tags) {
        List<ExternalContactFollowUser.Tag> safeTags = defaultList(tags);
        for (ExternalContactFollowUser.Tag source : safeTags) {
            if (!StringUtils.hasText(source.getTagId())) {
                continue;
            }
            CustomerStaffTagPo existing = customerStaffTagMapper.selectOne(new LambdaQueryWrapper<CustomerStaffTagPo>()
                    .eq(CustomerStaffTagPo::getCustomerStaffId, relation.getId())
                    .eq(CustomerStaffTagPo::getExtTagId, source.getTagId())
                    .last("limit 1"));
            CustomerStaffTagPo item = existing == null ? new CustomerStaffTagPo() : existing;
            if (item.getId() == null) {
                item.setId(idGenerator.nextId());
                item.setCreatedAt(LocalDateTime.now());
            }
            item.setExtCorpId(extCorpId);
            item.setExtCreatorId(relation.getExtStaffId());
            item.setCustomerStaffId(relation.getId());
            item.setExtTagId(source.getTagId());
            item.setGroupName(source.getGroupName());
            item.setTagName(source.getTagName());
            item.setType(source.getType());
            item.setDeletedAt(null);
            item.setUpdatedAt(LocalDateTime.now());
            if (existing == null) {
                customerStaffTagMapper.insert(item);
            } else {
                customerStaffTagMapper.updateById(item);
            }
        }
        if (safeTags.isEmpty()) {
            customerStaffTagMapper.update(null, new LambdaUpdateWrapper<CustomerStaffTagPo>()
                    .eq(CustomerStaffTagPo::getCustomerStaffId, relation.getId())
                    .isNull(CustomerStaffTagPo::getDeletedAt)
                    .set(CustomerStaffTagPo::getDeletedAt, LocalDateTime.now()));
            return;
        }
        List<String> extTagIds = tagIds(safeTags);
        if (extTagIds.isEmpty()) {
            return;
        }
        customerStaffTagMapper.update(null, new LambdaUpdateWrapper<CustomerStaffTagPo>()
                .eq(CustomerStaffTagPo::getCustomerStaffId, relation.getId())
                .notIn(CustomerStaffTagPo::getExtTagId, extTagIds)
                .isNull(CustomerStaffTagPo::getDeletedAt)
                .set(CustomerStaffTagPo::getDeletedAt, LocalDateTime.now()));
    }

    private void upsertCustomerPortrait(String extCorpId, String extCustomerId, String extStaffId) {
        CustomerInfoPo existing = customerInfoMapper.selectOne(new LambdaQueryWrapper<CustomerInfoPo>()
                .eq(CustomerInfoPo::getExtCustomerId, extCustomerId)
                .eq(CustomerInfoPo::getExtStaffId, extStaffId)
                .last("limit 1"));
        CustomerInfoPo item = existing == null ? new CustomerInfoPo() : existing;
        if (item.getId() == null) {
            item.setId(idGenerator.nextId());
            item.setCreatedAt(LocalDateTime.now());
        }
        item.setExtCorpId(extCorpId);
        item.setExtCreatorId(extStaffId);
        item.setExtCustomerId(extCustomerId);
        item.setExtStaffId(extStaffId);
        item.setDeletedAt(null);
        item.setUpdatedAt(LocalDateTime.now());
        if (existing == null) {
            customerInfoMapper.insert(item);
        } else {
            customerInfoMapper.updateById(item);
        }
    }

    private List<String> tagIds(List<ExternalContactFollowUser.Tag> tags) {
        return tags.stream()
                .map(ExternalContactFollowUser.Tag::getTagId)
                .filter(StringUtils::hasText)
                .collect(Collectors.toList());
    }

    private LocalDateTime toLocalDateTime(Integer epochSeconds) {
        if (epochSeconds == null || epochSeconds <= 0) {
            return null;
        }
        return LocalDateTime.ofInstant(Instant.ofEpochSecond(epochSeconds), ZoneId.systemDefault());
    }

    private String thumbAvatar(String avatar) {
        if (!StringUtils.hasText(avatar)) {
            return avatar;
        }
        try {
            String host = new URI(avatar).getHost();
            if ("wework.qpic.cn".equals(host)) {
                return stripTrailingSize(avatar) + "/60";
            }
            if ("wx.qlogo.cn".equals(host)) {
                return stripTrailingSize(avatar) + "/64";
            }
        } catch (URISyntaxException ignored) {
            return avatar;
        }
        return avatar;
    }

    private String stripTrailingSize(String avatar) {
        return avatar.replaceFirst("/(?:0|46|64|96|132)$", "");
    }

    private String md5(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] bytes = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) {
                builder.append(String.format("%02x", b));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    private <T> List<T> defaultList(List<T> values) {
        return values == null ? Collections.emptyList() : values;
    }
}
