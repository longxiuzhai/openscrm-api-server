package cn.openscrm.api.storage.service;

import cn.openscrm.api.common.exception.BizException;
import cn.openscrm.api.common.exception.ErrorCode;
import cn.openscrm.api.config.OpenScrmProperties;
import com.aliyun.oss.HttpMethod;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.GeneratePresignedUrlRequest;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.Instant;
import java.util.Date;
import org.springframework.http.MediaTypeFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class OssStorageService implements FileStorageAdapter {

    private final OpenScrmProperties properties;

    public OssStorageService(OpenScrmProperties properties) {
        this.properties = properties;
    }

    @Override
    public String type() {
        return "oss";
    }

    @Override
    public String signedUrl(String objectKey, String method, long ttlSeconds) {
        OpenScrmProperties.Storage storage = properties.getStorage();
        validateConfig(storage);
        OSS oss = new OSSClientBuilder().build(
                storage.getEndpoint(),
                storage.getAccessKeyId(),
                storage.getAccessKeySecret());
        try {
            HttpMethod httpMethod = HttpMethod.valueOf(method.toUpperCase());
            Date expiration = Date.from(Instant.now().plusSeconds(Math.max(1, ttlSeconds)));
            GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(
                    storage.getBucket(), objectKey, httpMethod);
            request.setExpiration(expiration);
            if (HttpMethod.PUT.equals(httpMethod)) {
                request.setContentType(MediaTypeFactory.getMediaType(objectKey)
                        .orElse(org.springframework.http.MediaType.APPLICATION_OCTET_STREAM)
                        .toString());
            }
            URL url = oss.generatePresignedUrl(request);
            return rewriteCdnUrl(url.toString(), storage.getCdnUrl());
        } finally {
            oss.shutdown();
        }
    }

    private void validateConfig(OpenScrmProperties.Storage storage) {
        if (!StringUtils.hasText(storage.getEndpoint())
                || !StringUtils.hasText(storage.getBucket())
                || !StringUtils.hasText(storage.getAccessKeyId())
                || !StringUtils.hasText(storage.getAccessKeySecret())) {
            throw new BizException(ErrorCode.BAD_REQUEST, "OSS 配置不完整");
        }
    }

    private String rewriteCdnUrl(String signedUrl, String cdnUrl) {
        if (!StringUtils.hasText(cdnUrl)) {
            return signedUrl;
        }
        try {
            URI signed = new URI(signedUrl);
            URI cdn = new URI(cdnUrl);
            return new URI(cdn.getScheme(), signed.getUserInfo(), cdn.getHost(), cdn.getPort(),
                    signed.getPath(), signed.getQuery(), signed.getFragment()).toString();
        } catch (URISyntaxException e) {
            throw new BizException(ErrorCode.ILLEGAL_URL, "CDN URL 不正确");
        }
    }
}
