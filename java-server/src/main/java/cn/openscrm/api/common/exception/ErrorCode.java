package cn.openscrm.api.common.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    OK(0, "成功", HttpStatus.OK),
    INTERNAL_ERROR(500, "内部错误", HttpStatus.INTERNAL_SERVER_ERROR),
    UNKNOWN_ERROR(404, "未知错误", HttpStatus.INTERNAL_SERVER_ERROR),

    INTERNAL_SERVICE_INVALID_SIGN(10000001, "内部服务验签失败", HttpStatus.OK),
    MISSING_USER_ID(10000002, "缺失用户ID", HttpStatus.OK),
    USER_BUSY(10000003, "此用户正忙", HttpStatus.OK),
    ACCOUNT_NOT_FOUND(10000004, "账户未找到", HttpStatus.OK),
    INVALID_PARAMS(10000005, "非法参数", HttpStatus.BAD_REQUEST),
    BAD_REQUEST(10000400, "非法请求", HttpStatus.OK),
    NO_PERMISSION(10000401, "无权访问", HttpStatus.UNAUTHORIZED),
    TOKEN_EXPIRED(10000402, "TokDetail过期", HttpStatus.UNAUTHORIZED),
    INVALID_TOKEN(10000403, "无效TokDetail", HttpStatus.UNAUTHORIZED),
    TOKEN_REQUIRED(10000404, "无效token", HttpStatus.UNAUTHORIZED),
    TOO_MANY_REQUESTS(10000429, "请求过多", HttpStatus.TOO_MANY_REQUESTS),
    SESSION_EXPIRED(10000501, "会话已过期", HttpStatus.OK),
    INVALID_CIPHER(10000502, "无效密文", HttpStatus.OK),
    NO_FOLLOW(10000503, "请先关注公众号", HttpStatus.OK),
    TOO_MANY_REQUESTS_ERROR(10000504, "您的请求过于频繁，请休息一会儿", HttpStatus.OK),
    ITEM_NOT_FOUND(10000886, "未找到指定条目", HttpStatus.OK),
    INVALID_SESSION(10000887, "无效会话", HttpStatus.OK),

    DUPLICATED_PHONE(20000001, "重复手机号", HttpStatus.OK),
    INVALID_LOGIN(20000002, "账号或密码错误", HttpStatus.OK),
    DISABLED_USER(20000003, "用户被禁用", HttpStatus.OK),
    FORBIDDEN(20000004, "无权访问", HttpStatus.OK),
    DUPLICATED_CORP_ID(20000005, "系统已存在此CorpID", HttpStatus.OK),
    DO_NOT_DELETE_YOURSELF(20000006, "不要删除自己", HttpStatus.OK),
    INVALID_SIGN(20000007, "非法签名", HttpStatus.OK),
    EXPIRED_SIGN(20000008, "签名已过期", HttpStatus.OK),
    INVALID_PATH(20000009, "非法路径", HttpStatus.OK),
    DO_NOT_UPDATE_DEFAULT_ROLE(20000010, "禁止修改默认角色", HttpStatus.OK),
    INVALID_CORP_CONFIG(20000011, "不正确的企业配置信息", HttpStatus.OK),
    DUPLICATE_QUICK_REPLY_GROUP_NAME(20000100, "话术库组名重复", HttpStatus.OK),
    NOTIFY_TYPE(20000200, "通知时间类型错误", HttpStatus.OK),
    DUPLICATE_TAG(20000300, "标签重复", HttpStatus.OK),
    DUPLICATE_TAG_GROUP(20000301, "标签组重复", HttpStatus.OK),
    UNSUPPORTED_MSG(20000400, "不支持的消息类型", HttpStatus.OK),
    EARLIER_THAN_NOW(20000401, "延迟发送时间不能比当前时间早", HttpStatus.OK),
    TIMED_MSG_UNCHANGEABLE(20000402, "立即发送的消息不支持修改", HttpStatus.OK),
    NO_MASS_MSG_RECEIVERS(20000403, "群发消息未找到有效接收人", HttpStatus.OK),
    UNSUPPORTED_FILE_TYPE(20000404, "不支持的上传文件类型", HttpStatus.OK),
    INFO_FIELD_DUPLICATE(20000500, "取消展示/确认展示 包含重复字段", HttpStatus.OK),
    DUPLICATE_REMARK_NAME(20000600, "自定义字段名重复", HttpStatus.OK),
    GROUP_CHAT_NOT_EXISTS(20000700, "自动拉群分组不存在", HttpStatus.OK),
    CHECK_SIGN_FAILED(20000800, "验签失败", HttpStatus.OK),
    NO_STAFF(20000900, "员工列表或者员工分组列表至少需要一个不为空", HttpStatus.OK),
    TIMED_MSG_EARLIER_THAN_NOW(20000901, "定时发送消息不能比当前时间早", HttpStatus.OK),
    ILLEGAL_URL(20001000, "URL 不正确", HttpStatus.OK),
    PARSE_FILE_URL(20001001, "上传url解析错误", HttpStatus.OK),
    FILE_NOT_EXISTS(20001002, "文件不存在", HttpStatus.OK),
    NOT_IMAGE_FILE(20002000, "文件不是图片格式", HttpStatus.OK),
    CUSTOMER_NUM(20003001, "客户数量错误", HttpStatus.OK),
    QUICK_REPLY_GROUP_NOT_FOUND(20004001, "未找到话术分组", HttpStatus.OK),
    UPDATE_OTHER_RECORD_NOT_ALLOWED(20004002, "不能更新别人的话术分组", HttpStatus.OK),
    DELETE_OTHER_RECORD_NOT_ALLOWED(20004003, "不能删除别人的话术分组", HttpStatus.OK),
    EMPTY_EXTERNAL_CONTACT_INFO(20005001, "空员工数据", HttpStatus.OK),
    UNKNOWN_EVENT_TYPE(20006001, "未知事件类型错误", HttpStatus.OK);

    private final int code;
    private final String message;
    private final HttpStatus status;

    ErrorCode(int code, String message, HttpStatus status) {
        this.code = code;
        this.message = message;
        this.status = status;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
