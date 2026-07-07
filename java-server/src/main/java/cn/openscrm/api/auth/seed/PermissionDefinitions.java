package cn.openscrm.api.auth.seed;

import cn.openscrm.api.common.constant.BizIdentity;
import cn.openscrm.api.common.constant.OperationType;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class PermissionDefinitions {

    private PermissionDefinitions() {
    }

    public static List<PermissionDefinition> adminPermissions() {
        List<PermissionDefinition> items = new ArrayList<>();
        addReadFull(items, BizIdentity.BIZ_ROLE, "角色权限");
        addReadFull(items, BizIdentity.BIZ_MSG_ARCH, "会话存档");
        addReadFull(items, BizIdentity.BIZ_MEDIA_MGR, "素材管理");
        addReadFull(items, BizIdentity.BIZ_CUSTOMER_GROUP_CHAT, "群聊");
        addReadFull(items, BizIdentity.BIZ_WELCOME_MSG, "欢迎语");
        addReadFull(items, BizIdentity.BIZ_DELETE_CUSTOMER, "删人提醒");
        addReadFull(items, BizIdentity.BIZ_CUSTOMER_LOSS, "流失提醒");
        addReadFull(items, BizIdentity.BIZ_QUICK_REPLY, "快捷回复");
        addReadFull(items, BizIdentity.BIZ_QUICK_REPLY_GROUP, "话术库");
        addReadFull(items, BizIdentity.BIZ_DEPARTMENT, "部门管理");
        addReadFull(items, BizIdentity.BIZ_MASS_MSG, "群发消息");
        addReadFull(items, BizIdentity.BIZ_CUSTOMER_REMARK, "客户自定义信息");
        addReadFull(items, BizIdentity.BIZ_CUSTOMER_TAG, "客户标签");
        addReadFull(items, BizIdentity.BIZ_CUSTOMER_INFO, "客户管理");
        addReadFull(items, BizIdentity.BIZ_STAFF_INFO, "员工管理");
        addReadFull(items, BizIdentity.BIZ_CONTACT_WAY, "渠道码");
        return unique(items);
    }

    public static List<PermissionDefinition> staffPermissions() {
        List<PermissionDefinition> items = new ArrayList<>();
        addReadFull(items, BizIdentity.BIZ_MEDIA_MGR, "素材管理");
        addRead(items, BizIdentity.BIZ_CUSTOMER_GROUP_CHAT, "群聊");
        addRead(items, BizIdentity.BIZ_WELCOME_MSG, "欢迎语");
        addRead(items, BizIdentity.BIZ_QUICK_REPLY, "快捷回复");
        addRead(items, BizIdentity.BIZ_QUICK_REPLY_GROUP, "话术库");
        addRead(items, BizIdentity.BIZ_MASS_MSG, "群发消息");
        addReadFull(items, BizIdentity.BIZ_CUSTOMER_REMARK, "客户自定义信息");
        addReadFull(items, BizIdentity.BIZ_CUSTOMER_TAG, "客户标签");
        addReadFull(items, BizIdentity.BIZ_CUSTOMER_INFO, "客户管理");
        addRead(items, BizIdentity.BIZ_STAFF_INFO, "员工管理");
        addRead(items, BizIdentity.BIZ_CONTACT_WAY, "渠道码");
        return unique(items);
    }

    public static List<PermissionDefinition> departmentAdminPermissions() {
        return adminPermissions();
    }

    public static List<PermissionDefinition> superAdminPermissions() {
        return adminPermissions();
    }

    public static List<PermissionDefinition> allPermissions() {
        List<PermissionDefinition> items = new ArrayList<>();
        items.addAll(adminPermissions());
        items.addAll(superAdminPermissions());
        return unique(items);
    }

    public static List<String> identities(List<PermissionDefinition> permissions) {
        List<String> ids = new ArrayList<>();
        for (PermissionDefinition permission : permissions) {
            ids.add(permission.identity());
        }
        return ids;
    }

    private static void addReadFull(List<PermissionDefinition> items, String biz, String name) {
        addRead(items, biz, name);
        items.add(new PermissionDefinition(biz, OperationType.FULL, name + "-完全"));
    }

    private static void addRead(List<PermissionDefinition> items, String biz, String name) {
        items.add(new PermissionDefinition(biz, OperationType.READ, name + "-查看"));
    }

    private static List<PermissionDefinition> unique(List<PermissionDefinition> source) {
        Map<String, PermissionDefinition> byIdentity = new LinkedHashMap<>();
        for (PermissionDefinition item : source) {
            byIdentity.put(item.identity(), item);
        }
        return new ArrayList<>(byIdentity.values());
    }
}
