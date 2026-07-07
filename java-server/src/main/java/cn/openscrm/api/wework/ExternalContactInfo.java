package cn.openscrm.api.wework;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExternalContactInfo {

    @JsonProperty("external_userid")
    private String externalUserId;

    private String name;

    private String position;

    private String avatar;

    @JsonProperty("corp_name")
    private String corpName;

    private Integer type;

    private Integer gender;

    private String unionid;

    @JsonProperty("external_profile")
    private ExternalProfile externalProfile;

    @Getter
    @Setter
    public static class ExternalProfile {
        @JsonProperty("external_corp_name")
        private String externalCorpName;

        @JsonProperty("external_attr")
        private List<ExternalAttr> externalAttr = new ArrayList<>();
    }

    @Getter
    @Setter
    public static class ExternalAttr {
        private Integer type;
        private String name;
        private Text text;
        private Web web;
        private Miniprogram miniprogram;
    }

    @Getter
    @Setter
    public static class Text {
        private String value;
    }

    @Getter
    @Setter
    public static class Web {
        private String url;
        private String title;
    }

    @Getter
    @Setter
    public static class Miniprogram {
        private String appid;
        private String pagepath;
        private String title;
    }
}
