package cn.openscrm.api.storage.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import cn.openscrm.api.common.exception.BizException;
import cn.openscrm.api.common.exception.ErrorCode;
import cn.openscrm.api.config.OpenScrmProperties;
import java.util.Collections;
import org.junit.jupiter.api.Test;

class FileStorageServiceTest {

    @Test
    void safeFileNameStripsPathSegments() {
        FileStorageService service = service(new OpenScrmProperties(), new FakeStorageAdapter("oss"));

        assertThat(service.safeFileName("/tmp/uploads/avatar.png")).isEqualTo("avatar.png");
    }

    @Test
    void safeFileNameRejectsBlankName() {
        FileStorageService service = service(new OpenScrmProperties(), new FakeStorageAdapter("oss"));

        assertThatThrownBy(() -> service.safeFileName(" "))
                .isInstanceOf(BizException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.BAD_REQUEST);
    }

    @Test
    void signedUrlUsesConfiguredAdapterAndDefaultTtl() {
        OpenScrmProperties properties = new OpenScrmProperties();
        properties.getStorage().setType("oss");
        properties.getStorage().setSignedUrlTtlSeconds(0L);
        FileStorageService service = service(properties, new FakeStorageAdapter("oss"));

        assertThat(service.signedUrlTtlSeconds()).isEqualTo(3600L);
        assertThat(service.signedUrl("quick-reply/avatar.png", "PUT", service.signedUrlTtlSeconds()))
                .isEqualTo("oss:PUT:quick-reply/avatar.png:3600");
    }

    @Test
    void signedUrlRejectsUnknownStorageType() {
        OpenScrmProperties properties = new OpenScrmProperties();
        properties.getStorage().setType("missing");
        FileStorageService service = service(properties, new FakeStorageAdapter("oss"));

        assertThatThrownBy(() -> service.signedUrl("quick-reply/avatar.png", "PUT", 60L))
                .isInstanceOf(BizException.class)
                .hasMessage("未配置可用文件存储: missing");
    }

    private static FileStorageService service(OpenScrmProperties properties, FileStorageAdapter adapter) {
        return new FileStorageService(properties, Collections.singletonList(adapter));
    }

    private static class FakeStorageAdapter implements FileStorageAdapter {
        private final String type;

        private FakeStorageAdapter(String type) {
            this.type = type;
        }

        @Override
        public String type() {
            return type;
        }

        @Override
        public String signedUrl(String objectKey, String method, long ttlSeconds) {
            return type + ":" + method + ":" + objectKey + ":" + ttlSeconds;
        }
    }
}
