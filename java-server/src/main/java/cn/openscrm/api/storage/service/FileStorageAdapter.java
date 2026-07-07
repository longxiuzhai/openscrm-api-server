package cn.openscrm.api.storage.service;

public interface FileStorageAdapter {

    String type();

    String signedUrl(String objectKey, String method, long ttlSeconds);
}
