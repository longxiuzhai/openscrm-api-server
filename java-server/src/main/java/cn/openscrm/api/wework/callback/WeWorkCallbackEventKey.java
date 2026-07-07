package cn.openscrm.api.wework.callback;

import java.util.Objects;

public class WeWorkCallbackEventKey {

    private final String msgType;
    private final String event;
    private final String changeType;

    public WeWorkCallbackEventKey(String msgType, String event, String changeType) {
        this.msgType = msgType;
        this.event = event;
        this.changeType = changeType;
    }

    public String getMsgType() {
        return msgType;
    }

    public String getEvent() {
        return event;
    }

    public String getChangeType() {
        return changeType;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof WeWorkCallbackEventKey)) {
            return false;
        }
        WeWorkCallbackEventKey that = (WeWorkCallbackEventKey) other;
        return Objects.equals(msgType, that.msgType)
                && Objects.equals(event, that.event)
                && Objects.equals(changeType, that.changeType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(msgType, event, changeType);
    }
}
