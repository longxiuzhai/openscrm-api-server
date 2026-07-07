package cn.openscrm.api.wework.callback;

import java.util.Collections;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WeWorkCallbackMessage {

    private String toUserName;
    private String fromUserName;
    private Long createTime;
    private String msgType;
    private String event;
    private String changeType;
    private Long agentId;
    private String rawXml;
    private Map<String, String> fields = Collections.emptyMap();
}
