package cn.openscrm.api.wework;

import cn.openscrm.api.config.OpenScrmProperties;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class DefaultWeWorkClient implements WeWorkClient {

    private final OpenScrmProperties properties;
    private final RestTemplate restTemplate;
    private final StringRedisTemplate redisTemplate;

    public DefaultWeWorkClient(OpenScrmProperties properties, RestTemplate restTemplate, StringRedisTemplate redisTemplate) {
        this.properties = properties;
        this.restTemplate = restTemplate;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public String getAccessToken(String corpId, String secret) {
        if (!StringUtils.hasText(corpId) || !StringUtils.hasText(secret)) {
            throw new WeWorkApiException(-1, "missing WeWork corp id or secret");
        }
        String cacheKey = "openscrm:wework:access-token:" + corpId + ":" + secret.hashCode();
        String cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null && !cached.isEmpty()) {
            return cached;
        }
        String url = UriComponentsBuilder.fromHttpUrl(properties.getWeWork().getApiHost())
                .path("/cgi-bin/gettoken")
                .queryParam("corpid", corpId)
                .queryParam("corpsecret", secret)
                .toUriString();
        AccessTokenResponse response = getForObject(url, AccessTokenResponse.class);
        response.throwIfError();
        redisTemplate.opsForValue().set(cacheKey, response.getAccessToken(), ttl(response.getExpiresIn()));
        return response.getAccessToken();
    }

    @Override
    public DepartmentListResponse listDepartments(String corpId, String secret) {
        String token = getAccessToken(corpId, secret);
        String url = UriComponentsBuilder.fromHttpUrl(properties.getWeWork().getApiHost())
                .path("/cgi-bin/department/list")
                .queryParam("access_token", token)
                .toUriString();
        DepartmentListResponse response = getForObject(url, DepartmentListResponse.class);
        response.throwIfError();
        return response;
    }

    @Override
    public UserIdListResponse listUserIds(String corpId, String secret) {
        String token = getAccessToken(corpId, secret);
        String cursor = null;
        UserIdListResponse aggregate = new UserIdListResponse();
        do {
            String url = UriComponentsBuilder.fromHttpUrl(properties.getWeWork().getApiHost())
                    .path("/cgi-bin/user/list_id")
                    .queryParam("access_token", token)
                    .queryParam("limit", 10000)
                    .queryParamIfPresent("cursor", java.util.Optional.ofNullable(cursor))
                    .toUriString();
            UserIdListResponse page = getForObject(url, UserIdListResponse.class);
            page.throwIfError();
            aggregate.getDeptUser().addAll(page.getDeptUser());
            cursor = page.getNextCursor();
        } while (cursor != null && !cursor.isEmpty());
        return aggregate;
    }

    @Override
    public UserDetailResponse getUser(String corpId, String secret, String userId) {
        String token = getAccessToken(corpId, secret);
        String url = UriComponentsBuilder.fromHttpUrl(properties.getWeWork().getApiHost())
                .path("/cgi-bin/user/get")
                .queryParam("access_token", token)
                .queryParam("userid", userId)
                .toUriString();
        UserDetailResponse response = getForObject(url, UserDetailResponse.class);
        response.throwIfError();
        return response;
    }

    @Override
    public UserIdentityResponse getUserInfo(String corpId, String secret, String code) {
        String token = getAccessToken(corpId, secret);
        String url = UriComponentsBuilder.fromHttpUrl(properties.getWeWork().getApiHost())
                .path("/cgi-bin/user/getuserinfo")
                .queryParam("access_token", token)
                .queryParam("code", code)
                .toUriString();
        UserIdentityResponse response = getForObject(url, UserIdentityResponse.class);
        response.throwIfError();
        return response;
    }

    @Override
    public ExternalContactCorpTagListResponse listExternalContactCorpTags(String corpId, String secret, List<String> tagIds) {
        String token = getAccessToken(corpId, secret);
        String url = UriComponentsBuilder.fromHttpUrl(properties.getWeWork().getApiHost())
                .path("/cgi-bin/externalcontact/get_corp_tag_list")
                .queryParam("access_token", token)
                .toUriString();
        Map<String, Object> body = new HashMap<>();
        if (tagIds != null && !tagIds.isEmpty()) {
            body.put("tag_id", tagIds);
        }
        ExternalContactCorpTagListResponse response = postForObject(url, body, ExternalContactCorpTagListResponse.class);
        response.throwIfError();
        return response;
    }

    @Override
    public ExternalContactCorpTagGroup addExternalContactCorpTag(String corpId, String secret, ExternalContactCorpTagGroup request) {
        String token = getAccessToken(corpId, secret);
        String url = UriComponentsBuilder.fromHttpUrl(properties.getWeWork().getApiHost())
                .path("/cgi-bin/externalcontact/add_corp_tag")
                .queryParam("access_token", token)
                .toUriString();
        Map<String, Object> body = new HashMap<>();
        body.put("group_id", request.getGroupId());
        body.put("group_name", request.getGroupName());
        body.put("order", request.getOrder());
        body.put("tag", request.getTag());
        ExternalContactCorpTagAddResponse response = postForObject(url, body, ExternalContactCorpTagAddResponse.class);
        response.throwIfError();
        return response.getTagGroup();
    }

    @Override
    public void editExternalContactCorpTag(String corpId, String secret, String id, String name, Integer order) {
        String token = getAccessToken(corpId, secret);
        String url = UriComponentsBuilder.fromHttpUrl(properties.getWeWork().getApiHost())
                .path("/cgi-bin/externalcontact/edit_corp_tag")
                .queryParam("access_token", token)
                .toUriString();
        Map<String, Object> body = new HashMap<>();
        body.put("id", id);
        body.put("name", name);
        body.put("order", order);
        CommonResponse response = postForObject(url, body, CommonResponse.class);
        response.throwIfError();
    }

    @Override
    public void deleteExternalContactCorpTag(String corpId, String secret, List<String> tagIds, List<String> groupIds) {
        String token = getAccessToken(corpId, secret);
        String url = UriComponentsBuilder.fromHttpUrl(properties.getWeWork().getApiHost())
                .path("/cgi-bin/externalcontact/del_corp_tag")
                .queryParam("access_token", token)
                .toUriString();
        Map<String, Object> body = new HashMap<>();
        body.put("tag_id", tagIds);
        body.put("group_id", groupIds);
        CommonResponse response = postForObject(url, body, CommonResponse.class);
        response.throwIfError();
    }

    @Override
    public ExternalContactGetResponse getExternalContact(String corpId, String secret, String externalUserId) {
        String token = getAccessToken(corpId, secret);
        String url = UriComponentsBuilder.fromHttpUrl(properties.getWeWork().getApiHost())
                .path("/cgi-bin/externalcontact/get")
                .queryParam("access_token", token)
                .queryParam("external_userid", externalUserId)
                .toUriString();
        ExternalContactGetResponse response = getForObject(url, ExternalContactGetResponse.class);
        response.throwIfError();
        return response;
    }

    @Override
    public void remarkExternalContact(String corpId, String secret, ExternalContactRemarkRequest request) {
        String token = getAccessToken(corpId, secret);
        String url = UriComponentsBuilder.fromHttpUrl(properties.getWeWork().getApiHost())
                .path("/cgi-bin/externalcontact/remark")
                .queryParam("access_token", token)
                .toUriString();
        CommonResponse response = postForObject(url, request, CommonResponse.class);
        response.throwIfError();
    }

    @Override
    public void markExternalContactTag(String corpId, String secret, ExternalContactMarkTagRequest request) {
        String token = getAccessToken(corpId, secret);
        String url = UriComponentsBuilder.fromHttpUrl(properties.getWeWork().getApiHost())
                .path("/cgi-bin/externalcontact/mark_tag")
                .queryParam("access_token", token)
                .toUriString();
        CommonResponse response = postForObject(url, request, CommonResponse.class);
        response.throwIfError();
    }

    @Override
    public void sendWelcomeMessage(String corpId, String secret, SendWelcomeMessageRequest request) {
        String token = getAccessToken(corpId, secret);
        String url = UriComponentsBuilder.fromHttpUrl(properties.getWeWork().getApiHost())
                .path("/cgi-bin/externalcontact/send_welcome_msg")
                .queryParam("access_token", token)
                .toUriString();
        CommonResponse response = postForObject(url, request, CommonResponse.class);
        response.throwIfError();
    }

    @Override
    public void sendTextMessage(String corpId, String secret, SendTextMessageRequest request) {
        String token = getAccessToken(corpId, secret);
        String url = UriComponentsBuilder.fromHttpUrl(properties.getWeWork().getApiHost())
                .path("/cgi-bin/message/send")
                .queryParam("access_token", token)
                .toUriString();
        CommonResponse response = postForObject(url, request, CommonResponse.class);
        response.throwIfError();
    }

    @Override
    public AddMsgTemplateResponse addMsgTemplate(String corpId, String secret, AddMsgTemplateRequest request) {
        String token = getAccessToken(corpId, secret);
        String url = UriComponentsBuilder.fromHttpUrl(properties.getWeWork().getApiHost())
                .path("/cgi-bin/externalcontact/add_msg_template")
                .queryParam("access_token", token)
                .toUriString();
        AddMsgTemplateResponse response = postForObject(url, request, AddMsgTemplateResponse.class);
        response.throwIfError();
        return response;
    }

    @Override
    public GroupMsgSendResultResponse getGroupMsgSendResult(String corpId, String secret, GroupMsgSendResultRequest request) {
        String token = getAccessToken(corpId, secret);
        String url = UriComponentsBuilder.fromHttpUrl(properties.getWeWork().getApiHost())
                .path("/cgi-bin/externalcontact/get_groupmsg_send_result")
                .queryParam("access_token", token)
                .toUriString();
        GroupMsgSendResultResponse response = postForObject(url, request, GroupMsgSendResultResponse.class);
        response.throwIfError();
        return response;
    }

    @Override
    public MsgAuditPermitUserListResponse listMsgAuditPermitUsers(String corpId, String secret, Integer edition) {
        String token = getAccessToken(corpId, secret);
        String url = UriComponentsBuilder.fromHttpUrl(properties.getWeWork().getApiHost())
                .path("/cgi-bin/msgaudit/get_permit_user_list")
                .queryParam("access_token", token)
                .toUriString();
        MsgAuditPermitUserListRequest request = new MsgAuditPermitUserListRequest();
        request.setType(edition);
        MsgAuditPermitUserListResponse response = postForObject(url, request, MsgAuditPermitUserListResponse.class);
        response.throwIfError();
        return response;
    }

    @Override
    public String uploadPermanentImage(String corpId, String secret, String filename, byte[] content) {
        String token = getAccessToken(corpId, secret);
        String url = UriComponentsBuilder.fromHttpUrl(properties.getWeWork().getApiHost())
                .path("/cgi-bin/media/uploadimg")
                .queryParam("access_token", token)
                .toUriString();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("media", new NamedByteArrayResource(content, filename));
        MediaUploadImageResponse response = postForObject(url, new HttpEntity<>(body, headers), MediaUploadImageResponse.class);
        response.throwIfError();
        return response.getUrl();
    }

    @Override
    public JsApiTicketResponse getJsApiTicket(String corpId, String secret) {
        String token = getAccessToken(corpId, secret);
        String url = UriComponentsBuilder.fromHttpUrl(properties.getWeWork().getApiHost())
                .path("/cgi-bin/get_jsapi_ticket")
                .queryParam("access_token", token)
                .toUriString();
        JsApiTicketResponse response = getForObject(url, JsApiTicketResponse.class);
        response.throwIfError();
        return response;
    }

    @Override
    public JsApiTicketResponse getJsApiAgentTicket(String corpId, String secret) {
        String token = getAccessToken(corpId, secret);
        String url = UriComponentsBuilder.fromHttpUrl(properties.getWeWork().getApiHost())
                .path("/cgi-bin/ticket/get")
                .queryParam("access_token", token)
                .queryParam("type", "agent_config")
                .toUriString();
        JsApiTicketResponse response = getForObject(url, JsApiTicketResponse.class);
        response.throwIfError();
        return response;
    }

    @Override
    public MediaUploadResponse uploadTemporaryMedia(String corpId, String secret, String type, String filename, byte[] content) {
        String token = getAccessToken(corpId, secret);
        String url = UriComponentsBuilder.fromHttpUrl(properties.getWeWork().getApiHost())
                .path("/cgi-bin/media/upload")
                .queryParam("access_token", token)
                .queryParam("type", type)
                .toUriString();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("media", new NamedByteArrayResource(content, filename));
        MediaUploadResponse response = postForObject(url, new HttpEntity<>(body, headers), MediaUploadResponse.class);
        response.throwIfError();
        return response;
    }

    @Override
    public GroupChatGetResponse getGroupChat(String corpId, String secret, String chatId) {
        String token = getAccessToken(corpId, secret);
        String url = UriComponentsBuilder.fromHttpUrl(properties.getWeWork().getApiHost())
                .path("/cgi-bin/externalcontact/groupchat/get")
                .queryParam("access_token", token)
                .toUriString();
        Map<String, Object> body = new HashMap<>();
        body.put("chat_id", chatId);
        GroupChatGetResponse response = postForObject(url, body, GroupChatGetResponse.class);
        response.throwIfError();
        return response;
    }

    @Override
    public GroupChatListResponse listGroupChats(String corpId, String secret, GroupChatListRequest request) {
        String token = getAccessToken(corpId, secret);
        String url = UriComponentsBuilder.fromHttpUrl(properties.getWeWork().getApiHost())
                .path("/cgi-bin/externalcontact/groupchat/list")
                .queryParam("access_token", token)
                .toUriString();
        GroupChatListResponse response = postForObject(url, request, GroupChatListResponse.class);
        response.throwIfError();
        return response;
    }

    @Override
    public ContactWayAddResponse addContactWay(String corpId, String secret, ContactWayRequest request) {
        String token = getAccessToken(corpId, secret);
        String url = UriComponentsBuilder.fromHttpUrl(properties.getWeWork().getApiHost())
                .path("/cgi-bin/externalcontact/add_contact_way")
                .queryParam("access_token", token)
                .toUriString();
        ContactWayAddResponse response = postForObject(url, request, ContactWayAddResponse.class);
        response.throwIfError();
        return response;
    }

    @Override
    public ContactWayGetResponse getContactWay(String corpId, String secret, String configId) {
        String token = getAccessToken(corpId, secret);
        String url = UriComponentsBuilder.fromHttpUrl(properties.getWeWork().getApiHost())
                .path("/cgi-bin/externalcontact/get_contact_way")
                .queryParam("access_token", token)
                .toUriString();
        Map<String, Object> body = new HashMap<>();
        body.put("config_id", configId);
        ContactWayGetResponse response = postForObject(url, body, ContactWayGetResponse.class);
        response.throwIfError();
        return response;
    }

    @Override
    public void updateContactWay(String corpId, String secret, ContactWayRequest request) {
        String token = getAccessToken(corpId, secret);
        String url = UriComponentsBuilder.fromHttpUrl(properties.getWeWork().getApiHost())
                .path("/cgi-bin/externalcontact/update_contact_way")
                .queryParam("access_token", token)
                .toUriString();
        CommonResponse response = postForObject(url, request, CommonResponse.class);
        response.throwIfError();
    }

    @Override
    public void deleteContactWay(String corpId, String secret, String configId) {
        String token = getAccessToken(corpId, secret);
        String url = UriComponentsBuilder.fromHttpUrl(properties.getWeWork().getApiHost())
                .path("/cgi-bin/externalcontact/del_contact_way")
                .queryParam("access_token", token)
                .toUriString();
        Map<String, Object> body = new HashMap<>();
        body.put("config_id", configId);
        CommonResponse response = postForObject(url, body, CommonResponse.class);
        response.throwIfError();
    }

    @Override
    public void updateUserEnable(String corpId, String secret, String userId, int enable) {
        String token = getAccessToken(corpId, secret);
        String url = UriComponentsBuilder.fromHttpUrl(properties.getWeWork().getApiHost())
                .path("/cgi-bin/user/update")
                .queryParam("access_token", token)
                .toUriString();
        Map<String, Object> body = new HashMap<>();
        body.put("userid", userId);
        body.put("enable", enable);
        CommonResponse response = postForObject(url, body, CommonResponse.class);
        response.throwIfError();
    }

    public OpenScrmProperties.WeWork config() {
        return properties.getWeWork();
    }

    private Duration ttl(Integer expiresIn) {
        int seconds = expiresIn == null ? 7200 : expiresIn;
        return Duration.ofSeconds(Math.max(60, seconds - 300));
    }

    private <T> T getForObject(String url, Class<T> clazz) {
        try {
            return restTemplate.getForObject(url, clazz);
        } catch (RestClientException e) {
            throw new WeWorkApiException(-1, e.getMessage());
        }
    }

    private <T> T postForObject(String url, Object body, Class<T> clazz) {
        try {
            return restTemplate.postForObject(url, body, clazz);
        } catch (RestClientException e) {
            throw new WeWorkApiException(-1, e.getMessage());
        }
    }

    private static class NamedByteArrayResource extends ByteArrayResource {
        private final String filename;

        NamedByteArrayResource(byte[] byteArray, String filename) {
            super(byteArray);
            this.filename = filename;
        }

        @Override
        public String getFilename() {
            return filename;
        }
    }
}
