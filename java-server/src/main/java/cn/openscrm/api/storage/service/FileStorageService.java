package cn.openscrm.api.storage.service;

import cn.openscrm.api.common.exception.BizException;
import cn.openscrm.api.common.exception.ErrorCode;
import cn.openscrm.api.config.OpenScrmProperties;
import java.nio.file.Paths;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class FileStorageService {

    private final OpenScrmProperties properties;
    private final List<FileStorageAdapter> adapters;

    public FileStorageService(OpenScrmProperties properties, List<FileStorageAdapter> adapters) {
        this.properties = properties;
        this.adapters = adapters;
    }

    public String signedUrl(String objectKey, String method, long ttlSeconds) {
        return adapter().signedUrl(objectKey, method, ttlSeconds);
    }

    public String safeFileName(String fileName) {
        if (!StringUtils.hasText(fileName)) {
            throw new BizException(ErrorCode.BAD_REQUEST);
        }
        String safe = Paths.get(fileName).getFileName().toString();
        if (!StringUtils.hasText(safe) || safe.contains("..")) {
            throw new BizException(ErrorCode.INVALID_PATH);
        }
        return safe;
    }

    public long signedUrlTtlSeconds() {
        Long ttl = properties.getStorage().getSignedUrlTtlSeconds();
        return ttl == null || ttl <= 0 ? 3600L : ttl;
    }

    private FileStorageAdapter adapter() {
        String type = properties.getStorage().getType();
        for (FileStorageAdapter adapter : adapters) {
            if (adapter.type().equalsIgnoreCase(type)) {
                return adapter;
            }
        }
        throw new BizException(ErrorCode.BAD_REQUEST, "未配置可用文件存储: " + type);
    }
}
