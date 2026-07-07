package cn.openscrm.api.stafffrontend.service;

import cn.openscrm.api.common.exception.BizException;
import cn.openscrm.api.common.exception.ErrorCode;
import cn.openscrm.api.config.OpenScrmProperties;
import cn.openscrm.api.stafffrontend.dto.JsAgentConfigResponse;
import cn.openscrm.api.stafffrontend.dto.JsConfigResponse;
import cn.openscrm.api.stafffrontend.dto.UploadMediaRequest;
import cn.openscrm.api.stafffrontend.dto.UploadMediaResponse;
import cn.openscrm.api.wework.JsApiTicketResponse;
import cn.openscrm.api.wework.MediaUploadResponse;
import cn.openscrm.api.wework.WeWorkClient;
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
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
public class StaffFrontendUtilityService {

    private static final java.time.Duration MEDIA_CACHE_TTL = java.time.Duration.ofDays(2);

    private final OpenScrmProperties properties;
    private final WeWorkClient weWorkClient;
    private final RestTemplate restTemplate;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public StaffFrontendUtilityService(OpenScrmProperties properties,
                                       WeWorkClient weWorkClient,
                                       RestTemplateBuilder restTemplateBuilder,
                                       StringRedisTemplate redisTemplate,
                                       ObjectMapper objectMapper) {
        this.properties = properties;
        this.weWorkClient = weWorkClient;
        this.restTemplate = restTemplateBuilder.build();
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public JsConfigResponse getJsConfig(String extCorpId, String url) {
        String cleanUrl = cleanUrl(url);
        JsApiTicketResponse ticket = weWorkClient.getJsApiTicket(extCorpId, properties.getWeWork().getCustomerSecret());
        long timestamp = Instant.now().getEpochSecond();
        String nonce = nonce();
        JsConfigResponse response = new JsConfigResponse();
        response.setTimestamp(timestamp);
        response.setNonceStr(nonce);
        response.setAppId(properties.getWeWork().getExtCorpId());
        response.setUrl(cleanUrl);
        response.setSignature(signature(ticket.getTicket(), nonce, timestamp, cleanUrl));
        return response;
    }

    public JsAgentConfigResponse getJsAgentConfig(String extCorpId, String url) {
        String cleanUrl = cleanUrl(url);
        JsApiTicketResponse ticket = weWorkClient.getJsApiAgentTicket(extCorpId, properties.getWeWork().getMainAgentSecret());
        long timestamp = Instant.now().getEpochSecond();
        String nonce = nonce();
        JsAgentConfigResponse response = new JsAgentConfigResponse();
        response.setCorpId(properties.getWeWork().getExtCorpId());
        response.setAgentId(properties.getWeWork().getMainAgentId());
        response.setTimestamp(timestamp);
        response.setNonceStr(nonce);
        response.setUrl(cleanUrl);
        response.setSignature(signature(ticket.getTicket(), nonce, timestamp, cleanUrl));
        return response;
    }

    public UploadMediaResponse uploadMedia(String extCorpId, UploadMediaRequest request) {
        validateMediaRequest(request);
        String cacheKey = "openscrm:staff-frontend:upload-media:" + sha1(request.getUrl());
        String cached = redisTemplate.opsForValue().get(cacheKey);
        if (StringUtils.hasText(cached)) {
            try {
                return objectMapper.readValue(cached, UploadMediaResponse.class);
            } catch (JsonProcessingException ignored) {
                // Fall through and refresh the cache.
            }
        }
        byte[] content = download(request.getUrl());
        MediaUploadResponse uploaded = weWorkClient.uploadTemporaryMedia(
                extCorpId,
                properties.getWeWork().getCustomerSecret(),
                request.getType(),
                filename(request.getUrl()),
                content);
        UploadMediaResponse response = new UploadMediaResponse();
        response.setType(uploaded.getType());
        response.setMediaId(uploaded.getMediaId());
        response.setCreatedAt(LocalDateTime.ofInstant(
                Instant.ofEpochSecond(uploaded.getCreatedAt() == null ? Instant.now().getEpochSecond() : uploaded.getCreatedAt()),
                ZoneId.systemDefault()));
        try {
            redisTemplate.opsForValue().set(cacheKey, objectMapper.writeValueAsString(response), MEDIA_CACHE_TTL);
        } catch (JsonProcessingException ignored) {
            // Upload succeeded; cache serialization should not fail the request.
        }
        return response;
    }

    public UploadMediaResponse uploadMaterialTemp(String extCorpId, String fileUrl) {
        UploadMediaRequest request = new UploadMediaRequest();
        request.setType("image");
        request.setUrl(fileUrl);
        return uploadMedia(extCorpId, request);
    }

    private void validateMediaRequest(UploadMediaRequest request) {
        if (request == null || !StringUtils.hasText(request.getUrl()) || !StringUtils.hasText(request.getType())) {
            throw new BizException(ErrorCode.BAD_REQUEST);
        }
        String type = request.getType().toLowerCase(Locale.ROOT);
        if (!("image".equals(type) || "voice".equals(type) || "video".equals(type) || "file".equals(type))) {
            throw new BizException(ErrorCode.BAD_REQUEST);
        }
        request.setType(type);
        parseUri(request.getUrl());
    }

    private byte[] download(String url) {
        try {
            ResponseEntity<byte[]> response = restTemplate.getForEntity(url, byte[].class);
            return response.getBody() == null ? new byte[0] : response.getBody();
        } catch (RestClientException e) {
            throw new BizException(ErrorCode.ILLEGAL_URL);
        }
    }

    private String cleanUrl(String url) {
        if (!StringUtils.hasText(url)) {
            throw new BizException(ErrorCode.BAD_REQUEST);
        }
        return url.split("#", 2)[0];
    }

    private URI parseUri(String value) {
        try {
            URI uri = new URI(value);
            if (!StringUtils.hasText(uri.getScheme()) || !StringUtils.hasText(uri.getHost())) {
                throw new BizException(ErrorCode.ILLEGAL_URL);
            }
            return uri;
        } catch (URISyntaxException e) {
            throw new BizException(ErrorCode.ILLEGAL_URL);
        }
    }

    private String filename(String url) {
        URI uri = parseUri(url);
        String path = uri.getPath();
        String ext = "";
        int index = path == null ? -1 : path.lastIndexOf('.');
        if (index >= 0) {
            ext = path.substring(index);
        }
        return sha1(System.nanoTime() + ":" + ThreadLocalRandom.current().nextInt()) + ext;
    }

    private String nonce() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 20);
    }

    private String signature(String ticket, String nonce, long timestamp, String url) {
        String raw = "jsapi_ticket=" + ticket + "&noncestr=" + nonce + "&timestamp=" + timestamp + "&url=" + url;
        return sha1(raw);
    }

    private String sha1(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] bytes = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) {
                builder.append(String.format("%02x", b));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new BizException(ErrorCode.INTERNAL_ERROR);
        }
    }
}
