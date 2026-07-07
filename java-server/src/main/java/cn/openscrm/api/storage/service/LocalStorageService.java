package cn.openscrm.api.storage.service;

import cn.openscrm.api.common.exception.BizException;
import cn.openscrm.api.common.exception.ErrorCode;
import cn.openscrm.api.config.OpenScrmProperties;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Service
public class LocalStorageService implements FileStorageAdapter {

    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private final OpenScrmProperties properties;

    public LocalStorageService(OpenScrmProperties properties) {
        this.properties = properties;
    }

    @Override
    public String type() {
        return "local";
    }

    @Override
    public String signedUrl(String objectKey, String method, long ttlSeconds) {
        long expiresAt = Instant.now().getEpochSecond() + Math.max(1, ttlSeconds);
        String token = sign(method, objectKey, expiresAt);
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/v1/storage/local")
                .queryParam("key", objectKey)
                .queryParam("method", method)
                .queryParam("expires_at", expiresAt)
                .queryParam("token", token)
                .build()
                .encode()
                .toUriString();
    }

    public void put(String objectKey, String method, Long expiresAt, String token, byte[] content) {
        validate(objectKey, method, expiresAt, token, "PUT");
        try {
            Path path = pathFor(objectKey);
            Files.createDirectories(path.getParent());
            Files.write(path, content == null ? new byte[0] : content);
        } catch (IOException e) {
            throw new BizException(ErrorCode.INTERNAL_ERROR, "写入本地文件失败");
        }
    }

    public byte[] get(String objectKey, String method, Long expiresAt, String token) {
        validate(objectKey, method, expiresAt, token, "GET");
        try {
            Path path = pathFor(objectKey);
            if (!Files.exists(path)) {
                throw new BizException(ErrorCode.FILE_NOT_EXISTS);
            }
            return Files.readAllBytes(path);
        } catch (IOException e) {
            throw new BizException(ErrorCode.INTERNAL_ERROR, "读取本地文件失败");
        }
    }

    public void putGoStyle(String objectKey, String method, Long expiresAt, String signature, byte[] content) {
        validateGoStyle(objectKey, method, expiresAt, signature, "PUT");
        try {
            Path path = pathFor(objectKey);
            Files.createDirectories(path.getParent());
            Files.write(path, content == null ? new byte[0] : content);
        } catch (IOException e) {
            throw new BizException(ErrorCode.INTERNAL_ERROR, "写入本地文件失败");
        }
    }

    public byte[] getGoStyle(String objectKey, String method, Long expiresAt, String signature) {
        validateGoStyle(objectKey, method, expiresAt, signature, "GET");
        try {
            Path path = pathFor(objectKey);
            if (!Files.exists(path)) {
                throw new BizException(ErrorCode.FILE_NOT_EXISTS);
            }
            return Files.readAllBytes(path);
        } catch (IOException e) {
            throw new BizException(ErrorCode.INTERNAL_ERROR, "读取本地文件失败");
        }
    }

    private void validate(String objectKey, String method, Long expiresAt, String token, String expectedMethod) {
        if (!expectedMethod.equalsIgnoreCase(method)
                || !StringUtils.hasText(objectKey)
                || expiresAt == null
                || !StringUtils.hasText(token)
                || expiresAt < Instant.now().getEpochSecond()) {
            throw new BizException(ErrorCode.EXPIRED_SIGN);
        }
        String expected = sign(expectedMethod, objectKey, expiresAt);
        if (!expected.equals(token)) {
            throw new BizException(ErrorCode.INVALID_SIGN);
        }
    }

    private void validateGoStyle(String objectKey, String method, Long expiresAt, String signature, String expectedMethod) {
        if (!expectedMethod.equalsIgnoreCase(method)
                || !StringUtils.hasText(objectKey)
                || expiresAt == null
                || !StringUtils.hasText(signature)
                || expiresAt < Instant.now().getEpochSecond()) {
            throw new BizException(ErrorCode.EXPIRED_SIGN);
        }
        String expected = sign(expectedMethod, objectKey, expiresAt);
        if (!expected.equals(signature)) {
            throw new BizException(ErrorCode.INVALID_SIGN);
        }
    }

    private Path pathFor(String objectKey) {
        Path root = Paths.get(properties.getStorage().getLocalRoot()).toAbsolutePath().normalize();
        Path path = root.resolve(objectKey).normalize();
        if (!path.startsWith(root)) {
            throw new BizException(ErrorCode.INVALID_PATH);
        }
        return path;
    }

    private String sign(String method, String objectKey, long expiresAt) {
        try {
            String payload = method.toUpperCase() + "\n" + objectKey + "\n" + expiresAt;
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(secret().getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
            return Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new BizException(ErrorCode.INTERNAL_ERROR, "生成签名失败");
        }
    }

    private String secret() {
        String secret = properties.getJwt().getSecret();
        return StringUtils.hasText(secret) ? secret : "openscrm-local-storage";
    }
}
