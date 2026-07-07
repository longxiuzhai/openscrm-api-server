package cn.openscrm.api.wework;

public interface WeWorkClient {

    String getAccessToken(String corpId, String secret);

    DepartmentListResponse listDepartments(String corpId, String secret);

    UserIdListResponse listUserIds(String corpId, String secret);

    UserDetailResponse getUser(String corpId, String secret, String userId);

    UserIdentityResponse getUserInfo(String corpId, String secret, String code);

    ExternalContactCorpTagListResponse listExternalContactCorpTags(String corpId, String secret, java.util.List<String> tagIds);

    ExternalContactCorpTagGroup addExternalContactCorpTag(String corpId, String secret, ExternalContactCorpTagGroup request);

    void editExternalContactCorpTag(String corpId, String secret, String id, String name, Integer order);

    void deleteExternalContactCorpTag(String corpId, String secret, java.util.List<String> tagIds, java.util.List<String> groupIds);

    ExternalContactGetResponse getExternalContact(String corpId, String secret, String externalUserId);

    void remarkExternalContact(String corpId, String secret, ExternalContactRemarkRequest request);

    void markExternalContactTag(String corpId, String secret, ExternalContactMarkTagRequest request);

    void sendWelcomeMessage(String corpId, String secret, SendWelcomeMessageRequest request);

    void sendTextMessage(String corpId, String secret, SendTextMessageRequest request);

    AddMsgTemplateResponse addMsgTemplate(String corpId, String secret, AddMsgTemplateRequest request);

    GroupMsgSendResultResponse getGroupMsgSendResult(String corpId, String secret, GroupMsgSendResultRequest request);

    MsgAuditPermitUserListResponse listMsgAuditPermitUsers(String corpId, String secret, Integer edition);

    String uploadPermanentImage(String corpId, String secret, String filename, byte[] content);

    JsApiTicketResponse getJsApiTicket(String corpId, String secret);

    JsApiTicketResponse getJsApiAgentTicket(String corpId, String secret);

    MediaUploadResponse uploadTemporaryMedia(String corpId, String secret, String type, String filename, byte[] content);

    GroupChatGetResponse getGroupChat(String corpId, String secret, String chatId);

    GroupChatListResponse listGroupChats(String corpId, String secret, GroupChatListRequest request);

    ContactWayAddResponse addContactWay(String corpId, String secret, ContactWayRequest request);

    ContactWayGetResponse getContactWay(String corpId, String secret, String configId);

    void updateContactWay(String corpId, String secret, ContactWayRequest request);

    void deleteContactWay(String corpId, String secret, String configId);

    void updateUserEnable(String corpId, String secret, String userId, int enable);
}
