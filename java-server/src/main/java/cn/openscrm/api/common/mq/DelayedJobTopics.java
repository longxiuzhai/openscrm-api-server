package cn.openscrm.api.common.mq;

public final class DelayedJobTopics {

    public static final String REFRESH_CONTACT_WAY = "topic:RefreshContactWayTopic";
    public static final String SYNC_CUSTOMER_DATA = "topic:SyncCustomerDataTopic";
    public static final String MASS_MSG = "topic:MassMsgTopic";
    public static final String GROUP_CHAT_MASS_MSG = "topic:GroupChatMassMsgTopic";
    public static final String DELETE_CUSTOMER_ADMIN_NOTIFY = "topic:StaffDeleteCustomerTopic";
    public static final String REMAINDER = "topic:RemainderTopic";

    private DelayedJobTopics() {
    }
}
