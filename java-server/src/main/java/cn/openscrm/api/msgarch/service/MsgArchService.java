package cn.openscrm.api.msgarch.service;

import cn.openscrm.api.common.api.PageResponse;
import cn.openscrm.api.common.exception.BizException;
import cn.openscrm.api.common.exception.ErrorCode;
import cn.openscrm.api.config.OpenScrmProperties;
import cn.openscrm.api.msgarch.dto.ChatMessageResponse;
import cn.openscrm.api.msgarch.dto.ChatSessionResponse;
import cn.openscrm.api.msgarch.dto.MsgArchSyncResponse;
import cn.openscrm.api.persistence.entity.ChatMsgContentPo;
import cn.openscrm.api.persistence.entity.ChatMsgPo;
import cn.openscrm.api.persistence.entity.CustomerPo;
import cn.openscrm.api.persistence.entity.GroupChatPo;
import cn.openscrm.api.persistence.entity.StaffPo;
import cn.openscrm.api.persistence.mapper.ChatMsgContentPoMapper;
import cn.openscrm.api.persistence.mapper.ChatMsgPoMapper;
import cn.openscrm.api.persistence.mapper.CustomerPoMapper;
import cn.openscrm.api.persistence.mapper.GroupChatPoMapper;
import cn.openscrm.api.persistence.mapper.StaffPoMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
public class MsgArchService {

    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final String SYNC_PATH = "/api/v1/chat-msg/sync";

    private final ChatMsgPoMapper chatMsgMapper;
    private final ChatMsgContentPoMapper chatMsgContentMapper;
    private final StaffPoMapper staffMapper;
    private final CustomerPoMapper customerMapper;
    private final GroupChatPoMapper groupChatMapper;
    private final ObjectMapper objectMapper;
    private final OpenScrmProperties properties;
    private final RestTemplate restTemplate;

    public PageResponse<ChatSessionResponse> querySessions(String extCorpId,
                                                           String extStaffId,
                                                           String sessionType,
                                                           String name,
                                                           long page,
                                                           long pageSize) {
        Page<ChatMsgPo> result = chatMsgMapper.selectPage(new Page<>(page, pageSize),
                new LambdaQueryWrapper<ChatMsgPo>()
                        .eq(ChatMsgPo::getExtCorpId, extCorpId)
                        .eq(StringUtils.hasText(sessionType), ChatMsgPo::getSessionType, sessionType)
                        .and(w -> w.eq(ChatMsgPo::getFrom, extStaffId)
                                .or()
                                .apply("JSON_CONTAINS(to_list, JSON_ARRAY({0}))", extStaffId))
                        .orderByDesc(ChatMsgPo::getMsgTime));
        List<ChatMsgPo> latestBySession = latestBySession(result.getRecords());
        List<ChatSessionResponse> sessions = latestBySession.stream()
                .map(item -> toSessionResponse(item, extStaffId))
                .filter(item -> !StringUtils.hasText(name)
                        || (StringUtils.hasText(item.getPeerName()) && item.getPeerName().startsWith(name))
                        || (StringUtils.hasText(item.getGroupChatName()) && item.getGroupChatName().startsWith(name)))
                .collect(Collectors.toList());
        return new PageResponse<>(sessions, result.getTotal(), page, pageSize);
    }

    public PageResponse<ChatMessageResponse> queryMsgs(String extCorpId,
                                                       String extStaffId,
                                                       String receiverId,
                                                       String msgType,
                                                       Long sendAtStart,
                                                       Long sendAtEnd,
                                                       Long minId,
                                                       Long maxId,
                                                       long page,
                                                       long pageSize) {
        LambdaQueryWrapper<ChatMsgPo> wrapper = baseConversationWrapper(extCorpId, extStaffId, receiverId)
                .eq(StringUtils.hasText(msgType), ChatMsgPo::getMsgType, msgType)
                .ge(sendAtStart != null, ChatMsgPo::getMsgTime, secondsToMillis(sendAtStart))
                .le(sendAtEnd != null, ChatMsgPo::getMsgTime, secondsToMillis(sendAtEnd))
                .gt(minId != null, ChatMsgPo::getId, minId)
                .lt(maxId != null, ChatMsgPo::getId, maxId)
                .orderByDesc(ChatMsgPo::getMsgTime);
        Page<ChatMsgPo> result = chatMsgMapper.selectPage(new Page<>(page, pageSize), wrapper);
        return new PageResponse<>(attachContent(result.getRecords()), result.getTotal(), page, pageSize);
    }

    public PageResponse<ChatMessageResponse> searchMsgs(String extCorpId,
                                                        String extStaffId,
                                                        String extPeerId,
                                                        String keyword,
                                                        long page,
                                                        long pageSize) {
        LambdaQueryWrapper<ChatMsgPo> wrapper = baseConversationWrapper(extCorpId, extStaffId, extPeerId)
                .like(StringUtils.hasText(keyword), ChatMsgPo::getContentText, keyword)
                .orderByDesc(ChatMsgPo::getMsgTime);
        Page<ChatMsgPo> result = chatMsgMapper.selectPage(new Page<>(page, pageSize), wrapper);
        return new PageResponse<>(attachContent(result.getRecords()), result.getTotal(), page, pageSize);
    }

    public MsgArchSyncResponse sync(String extCorpId) {
        if (!msgArchProxyEnabled()) {
            return new MsgArchSyncResponse(true, "local sync fallback, latest_seq=" + latestSeq(extCorpId));
        }
        String url = UriComponentsBuilder.fromHttpUrl(properties.getMsgArch().getServerUrl())
                .path(SYNC_PATH)
                .toUriString();
        Map<String, String> body = new LinkedHashMap<>();
        body.put("ext_corp_id", extCorpId);
        body.put("signature", hmacSha256Hex(properties.getMsgArch().getAppCode(), extCorpId));
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, body, String.class);
            if (!HttpStatus.OK.equals(response.getStatusCode())) {
                return new MsgArchSyncResponse(false, "msg-archive sync failed, status=" + response.getStatusCodeValue());
            }
            return new MsgArchSyncResponse(true, "msg-archive sync proxied");
        } catch (HttpStatusCodeException e) {
            return new MsgArchSyncResponse(false, "msg-archive sync failed, status=" + e.getRawStatusCode());
        } catch (RestClientException e) {
            throw new BizException(ErrorCode.INTERNAL_ERROR, "调用会话存档同步服务失败: " + e.getMessage());
        }
    }

    private boolean msgArchProxyEnabled() {
        return properties.getMsgArch() != null
                && StringUtils.hasText(properties.getMsgArch().getServerUrl())
                && StringUtils.hasText(properties.getMsgArch().getAppCode());
    }

    private Long latestSeq(String extCorpId) {
        return chatMsgMapper.selectList(new LambdaQueryWrapper<ChatMsgPo>()
                        .eq(ChatMsgPo::getExtCorpId, extCorpId)
                        .orderByDesc(ChatMsgPo::getSeq)
                        .last("limit 1"))
                .stream()
                .map(ChatMsgPo::getSeq)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(0L);
    }

    private String hmacSha256Hex(String secret, String value) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
            byte[] bytes = mac.doFinal(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new BizException(ErrorCode.INTERNAL_ERROR, "生成会话存档同步签名失败");
        }
    }

    private LambdaQueryWrapper<ChatMsgPo> baseConversationWrapper(String extCorpId, String extStaffId, String receiverId) {
        return new LambdaQueryWrapper<ChatMsgPo>()
                .eq(ChatMsgPo::getExtCorpId, extCorpId)
                .and(w -> w.eq(ChatMsgPo::getFrom, extStaffId)
                        .apply("JSON_CONTAINS(to_list, JSON_ARRAY({0}))", receiverId)
                        .or()
                        .eq(ChatMsgPo::getFrom, receiverId)
                        .apply("JSON_CONTAINS(to_list, JSON_ARRAY({0}))", extStaffId));
    }

    private List<ChatMsgPo> latestBySession(List<ChatMsgPo> rows) {
        Map<String, ChatMsgPo> map = new LinkedHashMap<>();
        for (ChatMsgPo row : rows) {
            map.putIfAbsent(row.getSessionId(), row);
        }
        return new ArrayList<>(map.values());
    }

    private ChatSessionResponse toSessionResponse(ChatMsgPo msg, String extStaffId) {
        if ("room".equals(msg.getSessionType()) || StringUtils.hasText(msg.getRoomId())) {
            GroupChatPo chat = groupChatMapper.selectOne(new LambdaQueryWrapper<GroupChatPo>()
                    .eq(GroupChatPo::getExtChatId, msg.getRoomId())
                    .eq(GroupChatPo::getExtCorpId, msg.getExtCorpId())
                    .last("limit 1"));
            return new ChatSessionResponse(msg, msg.getRoomId(), null, null, chat == null ? null : chat.getName());
        }
        String peer = peerId(msg, extStaffId);
        Person person = findPerson(msg.getExtCorpId(), peer);
        return new ChatSessionResponse(msg, peer, person.name, person.avatar, null);
    }

    private List<ChatMessageResponse> attachContent(List<ChatMsgPo> messages) {
        if (CollectionUtils.isEmpty(messages)) {
            return Collections.emptyList();
        }
        List<String> ids = messages.stream().map(ChatMsgPo::getId).map(String::valueOf).collect(Collectors.toList());
        Map<String, ChatMsgContentPo> contentByMsg = chatMsgContentMapper.selectList(new LambdaQueryWrapper<ChatMsgContentPo>()
                        .in(ChatMsgContentPo::getChatMsgId, ids)
                        .isNull(ChatMsgContentPo::getDeletedAt))
                .stream()
                .collect(Collectors.toMap(ChatMsgContentPo::getChatMsgId, item -> item, (a, b) -> a));
        return messages.stream().map(msg -> {
            Person sender = findPerson(msg.getExtCorpId(), msg.getFrom());
            String to = readFirst(msg.getToList());
            Person receiver = findPerson(msg.getExtCorpId(), to);
            return new ChatMessageResponse(msg, contentByMsg.get(String.valueOf(msg.getId())),
                    sender.name, sender.avatar, receiver.name, receiver.avatar);
        }).collect(Collectors.toList());
    }

    private String peerId(ChatMsgPo msg, String extStaffId) {
        if (!extStaffId.equals(msg.getFrom())) {
            return msg.getFrom();
        }
        for (String to : readList(msg.getToList())) {
            if (!extStaffId.equals(to)) {
                return to;
            }
        }
        return readFirst(msg.getToList());
    }

    private Person findPerson(String extCorpId, String extId) {
        if (!StringUtils.hasText(extId)) {
            return new Person(null, null);
        }
        StaffPo staff = staffMapper.selectOne(new LambdaQueryWrapper<StaffPo>()
                .eq(StaffPo::getExtCorpId, extCorpId)
                .eq(StaffPo::getExtId, extId)
                .last("limit 1"));
        if (staff != null) {
            return new Person(staff.getName(), staff.getAvatarUrl());
        }
        CustomerPo customer = customerMapper.selectOne(new LambdaQueryWrapper<CustomerPo>()
                .eq(CustomerPo::getExtCorpId, extCorpId)
                .eq(CustomerPo::getExtId, extId)
                .last("limit 1"));
        if (customer != null) {
            return new Person(customer.getName(), customer.getAvatar());
        }
        return new Person(null, null);
    }

    private String readFirst(String json) {
        List<String> values = readList(json);
        return values.isEmpty() ? null : values.get(0);
    }

    private List<String> readList(String json) {
        if (!StringUtils.hasText(json)) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {
            });
        } catch (JsonProcessingException e) {
            return Collections.emptyList();
        }
    }

    private Long secondsToMillis(Long value) {
        if (value == null) {
            return null;
        }
        return value < 100000000000L ? value * 1000 : value;
    }

    private static class Person {
        private final String name;
        private final String avatar;

        private Person(String name, String avatar) {
            this.name = name;
            this.avatar = avatar;
        }
    }
}
