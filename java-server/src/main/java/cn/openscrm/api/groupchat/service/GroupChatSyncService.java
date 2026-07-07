package cn.openscrm.api.groupchat.service;

import cn.openscrm.api.common.id.SnowflakeIdGenerator;
import cn.openscrm.api.config.OpenScrmProperties;
import cn.openscrm.api.persistence.entity.GroupChatMemberPo;
import cn.openscrm.api.persistence.entity.GroupChatPo;
import cn.openscrm.api.persistence.entity.StaffPo;
import cn.openscrm.api.persistence.mapper.GroupChatMemberPoMapper;
import cn.openscrm.api.persistence.mapper.GroupChatPoMapper;
import cn.openscrm.api.persistence.mapper.StaffPoMapper;
import cn.openscrm.api.wework.GroupChatGetResponse;
import cn.openscrm.api.wework.GroupChatInfo;
import cn.openscrm.api.wework.GroupChatMemberInfo;
import cn.openscrm.api.wework.WeWorkClient;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class GroupChatSyncService {

    private static final int STATUS_DISMISSED = 1;
    private static final int STATUS_ACTIVE = 2;
    private static final String UPDATE_ADD_MEMBER = "add_member";
    private static final String UPDATE_DEL_MEMBER = "del_member";

    private final OpenScrmProperties properties;
    private final WeWorkClient weWorkClient;
    private final GroupChatPoMapper groupChatMapper;
    private final GroupChatMemberPoMapper groupChatMemberMapper;
    private final StaffPoMapper staffMapper;
    private final SnowflakeIdGenerator idGenerator;
    private final ObjectMapper objectMapper;

    @Transactional(rollbackFor = Exception.class)
    public void syncOne(String extCorpId, String chatId, String updateDetail, Long memberChangeCount) {
        if (!StringUtils.hasText(extCorpId) || !StringUtils.hasText(chatId)) {
            return;
        }
        GroupChatGetResponse response = weWorkClient.getGroupChat(
                extCorpId, properties.getWeWork().getCustomerSecret(), chatId);
        GroupChatInfo source = response.getGroupChat();
        if (source == null || !StringUtils.hasText(source.getChatId())) {
            return;
        }

        GroupChatPo chat = findChat(extCorpId, source.getChatId());
        boolean create = chat == null;
        if (create) {
            chat = new GroupChatPo();
            chat.setId(idGenerator.nextId());
            chat.setCreatedAt(LocalDateTime.now());
            chat.setTodayJoinMemberNum(0);
            chat.setTodayQuitMemberNum(0);
        }

        StaffPo owner = findStaff(extCorpId, source.getOwner());
        chat.setExtCorpId(extCorpId);
        chat.setExtCreatorId(source.getOwner());
        chat.setExtChatId(source.getChatId());
        chat.setName(resolveName(source, owner));
        chat.setOwner(source.getOwner());
        chat.setOwnerName(owner == null ? source.getOwner() : owner.getName());
        chat.setOwnerAvatarUrl(owner == null ? chat.getOwnerAvatarUrl() : owner.getAvatarUrl());
        chat.setOwnerRoleType(owner == null ? chat.getOwnerRoleType() : owner.getRoleType());
        chat.setCreateTime(toLocalDateTime(source.getCreateTime()));
        chat.setNotice(source.getNotice());
        chat.setAdminList(toAdminListJson(source.getAdminList()));
        chat.setStatus(STATUS_ACTIVE);
        chat.setTotal(source.getMemberList() == null ? 0 : source.getMemberList().size());
        applyMemberChangeCount(chat, updateDetail, memberChangeCount, create);
        chat.setUpdatedAt(LocalDateTime.now());

        if (create) {
            groupChatMapper.insert(chat);
        } else {
            groupChatMapper.updateById(chat);
        }
        refreshMembers(extCorpId, source);
    }

    @Transactional(rollbackFor = Exception.class)
    public void dismiss(String extCorpId, String chatId) {
        GroupChatPo chat = findChat(extCorpId, chatId);
        if (chat == null) {
            return;
        }
        chat.setStatus(STATUS_DISMISSED);
        chat.setUpdatedAt(LocalDateTime.now());
        groupChatMapper.updateById(chat);
    }

    private GroupChatPo findChat(String extCorpId, String chatId) {
        return groupChatMapper.selectOne(new LambdaQueryWrapper<GroupChatPo>()
                .eq(GroupChatPo::getExtCorpId, extCorpId)
                .eq(GroupChatPo::getExtChatId, chatId)
                .last("limit 1"));
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

    private String resolveName(GroupChatInfo source, StaffPo owner) {
        if (StringUtils.hasText(source.getName())) {
            return source.getName();
        }
        if (owner != null && StringUtils.hasText(owner.getName())) {
            return owner.getName() + "的客户群";
        }
        return source.getChatId();
    }

    private LocalDateTime toLocalDateTime(Integer epochSeconds) {
        if (epochSeconds == null || epochSeconds <= 0) {
            return null;
        }
        return LocalDateTime.ofInstant(Instant.ofEpochSecond(epochSeconds), ZoneId.systemDefault());
    }

    private String toAdminListJson(List<GroupChatInfo.AdminInfo> adminList) {
        List<String> userIds = new ArrayList<>();
        if (adminList != null) {
            for (GroupChatInfo.AdminInfo admin : adminList) {
                if (StringUtils.hasText(admin.getUserid())) {
                    userIds.add(admin.getUserid());
                }
            }
        }
        try {
            return objectMapper.writeValueAsString(userIds);
        } catch (JsonProcessingException e) {
            return "[]";
        }
    }

    private void applyMemberChangeCount(GroupChatPo chat, String updateDetail, Long memberChangeCount, boolean create) {
        long delta = memberChangeCount == null ? 0L : memberChangeCount;
        if (create && UPDATE_ADD_MEMBER.equals(updateDetail) && delta == 0L) {
            delta = chat.getTotal() == null ? 0L : chat.getTotal();
        }
        if (UPDATE_ADD_MEMBER.equals(updateDetail)) {
            chat.setTodayJoinMemberNum(defaultInt(chat.getTodayJoinMemberNum()) + Math.toIntExact(delta));
        } else if (UPDATE_DEL_MEMBER.equals(updateDetail)) {
            chat.setTodayQuitMemberNum(defaultInt(chat.getTodayQuitMemberNum()) + Math.toIntExact(delta));
        }
    }

    private int defaultInt(Integer value) {
        return value == null ? 0 : value;
    }

    private void refreshMembers(String extCorpId, GroupChatInfo source) {
        groupChatMemberMapper.delete(new LambdaQueryWrapper<GroupChatMemberPo>()
                .eq(GroupChatMemberPo::getExtCorpId, extCorpId)
                .eq(GroupChatMemberPo::getExtChatId, source.getChatId()));
        if (source.getMemberList() == null) {
            return;
        }
        for (GroupChatMemberInfo member : source.getMemberList()) {
            GroupChatMemberPo po = new GroupChatMemberPo();
            po.setId(idGenerator.nextId());
            po.setExtCorpId(extCorpId);
            po.setExtCreatorId(source.getOwner());
            po.setExtChatId(source.getChatId());
            po.setUserid(member.getUserid());
            po.setType(member.getType());
            po.setJoinTime(member.getJoinTime());
            po.setJoinScene(member.getJoinScene());
            po.setInvitor(member.getInvitor() == null ? null : member.getInvitor().getUserid());
            po.setUnionid(member.getUnionid());
            groupChatMemberMapper.insert(po);
        }
    }
}
