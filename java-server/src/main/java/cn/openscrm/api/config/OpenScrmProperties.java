package cn.openscrm.api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "openscrm")
public class OpenScrmProperties {

    private String env;
    private Seed seed = new Seed();
    private Jwt jwt = new Jwt();
    private WeWork weWork = new WeWork();
    private Storage storage = new Storage();
    private MsgArch msgArch = new MsgArch();

    public String getEnv() {
        return env;
    }

    public void setEnv(String env) {
        this.env = env;
    }

    public Seed getSeed() {
        return seed;
    }

    public void setSeed(Seed seed) {
        this.seed = seed;
    }

    public static class Seed {
        private Boolean enabled = false;

        public Boolean getEnabled() {
            return enabled;
        }

        public void setEnabled(Boolean enabled) {
            this.enabled = enabled;
        }
    }

    public Jwt getJwt() {
        return jwt;
    }

    public void setJwt(Jwt jwt) {
        this.jwt = jwt;
    }

    public WeWork getWeWork() {
        return weWork;
    }

    public void setWeWork(WeWork weWork) {
        this.weWork = weWork;
    }

    public Storage getStorage() {
        return storage;
    }

    public void setStorage(Storage storage) {
        this.storage = storage;
    }

    public MsgArch getMsgArch() {
        return msgArch;
    }

    public void setMsgArch(MsgArch msgArch) {
        this.msgArch = msgArch;
    }

    public static class Jwt {
        private String secret;
        private Long ttlSeconds = 604800L;

        public String getSecret() {
            return secret;
        }

        public void setSecret(String secret) {
            this.secret = secret;
        }

        public Long getTtlSeconds() {
            return ttlSeconds;
        }

        public void setTtlSeconds(Long ttlSeconds) {
            this.ttlSeconds = ttlSeconds;
        }
    }

    public static class WeWork {
        private String apiHost = "https://qyapi.weixin.qq.com";
        private String extCorpId;
        private String contactSecret;
        private String customerSecret;
        private Long mainAgentId;
        private String mainAgentSecret;
        private String callbackToken;
        private String callbackAesKey;

        public String getApiHost() {
            return apiHost;
        }

        public void setApiHost(String apiHost) {
            this.apiHost = apiHost;
        }

        public String getExtCorpId() {
            return extCorpId;
        }

        public void setExtCorpId(String extCorpId) {
            this.extCorpId = extCorpId;
        }

        public String getContactSecret() {
            return contactSecret;
        }

        public void setContactSecret(String contactSecret) {
            this.contactSecret = contactSecret;
        }

        public String getCustomerSecret() {
            return customerSecret;
        }

        public void setCustomerSecret(String customerSecret) {
            this.customerSecret = customerSecret;
        }

        public Long getMainAgentId() {
            return mainAgentId;
        }

        public void setMainAgentId(Long mainAgentId) {
            this.mainAgentId = mainAgentId;
        }

        public String getMainAgentSecret() {
            return mainAgentSecret;
        }

        public void setMainAgentSecret(String mainAgentSecret) {
            this.mainAgentSecret = mainAgentSecret;
        }

        public String getCallbackToken() {
            return callbackToken;
        }

        public void setCallbackToken(String callbackToken) {
            this.callbackToken = callbackToken;
        }

        public String getCallbackAesKey() {
            return callbackAesKey;
        }

        public void setCallbackAesKey(String callbackAesKey) {
            this.callbackAesKey = callbackAesKey;
        }
    }

    public static class Storage {
        private String type = "oss";
        private String localRoot = "/tmp/openscrm-api-server/uploads";
        private Long signedUrlTtlSeconds = 3600L;
        private String endpoint;
        private String bucket;
        private String accessKeyId;
        private String accessKeySecret;
        private String cdnUrl;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getLocalRoot() {
            return localRoot;
        }

        public void setLocalRoot(String localRoot) {
            this.localRoot = localRoot;
        }

        public Long getSignedUrlTtlSeconds() {
            return signedUrlTtlSeconds;
        }

        public void setSignedUrlTtlSeconds(Long signedUrlTtlSeconds) {
            this.signedUrlTtlSeconds = signedUrlTtlSeconds;
        }

        public String getEndpoint() {
            return endpoint;
        }

        public void setEndpoint(String endpoint) {
            this.endpoint = endpoint;
        }

        public String getBucket() {
            return bucket;
        }

        public void setBucket(String bucket) {
            this.bucket = bucket;
        }

        public String getAccessKeyId() {
            return accessKeyId;
        }

        public void setAccessKeyId(String accessKeyId) {
            this.accessKeyId = accessKeyId;
        }

        public String getAccessKeySecret() {
            return accessKeySecret;
        }

        public void setAccessKeySecret(String accessKeySecret) {
            this.accessKeySecret = accessKeySecret;
        }

        public String getCdnUrl() {
            return cdnUrl;
        }

        public void setCdnUrl(String cdnUrl) {
            this.cdnUrl = cdnUrl;
        }
    }

    public static class MsgArch {
        private String serverUrl;
        private String appCode;

        public String getServerUrl() {
            return serverUrl;
        }

        public void setServerUrl(String serverUrl) {
            this.serverUrl = serverUrl;
        }

        public String getAppCode() {
            return appCode;
        }

        public void setAppCode(String appCode) {
            this.appCode = appCode;
        }
    }
}
