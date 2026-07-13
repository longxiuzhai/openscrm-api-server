SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci;
SET collation_connection = 'utf8mb4_unicode_ci';

START TRANSACTION;

SET @ext_corp_id  = 'ww2d3e2957190c6e4c';
SET @ext_staff_id = 'admin';
SET @staff_name   = '本地超级管理员';
SET @role_id      = 1310832952200000106;

-- 1. 超级管理员角色
INSERT INTO `role` (
    `id`,
    `ext_corp_id`,
    `name`,
    `description`,
    `type`,
    `sort_weight`,
    `is_default`,
    `permission_ids`,
    `created_at`,
    `updated_at`,
    `deleted_at`
) VALUES (
    @role_id,
    @ext_corp_id,
    '超级管理员',
    '超级管理员',
    'superAdmin',
    10003,
    1,
    JSON_ARRAY(
        'BizRole_Read', 'BizRole_Full',
        'BizMsgArch_Read', 'BizMsgArch_Full',
        'BizMediaMgr_Read', 'BizMediaMgr_Full',
        'BizCustomerGroupChat_Read', 'BizCustomerGroupChat_Full',
        'BizWelcomeMsg_Read', 'BizWelcomeMsg_Full',
        'BizDeleteCustomer_Read', 'BizDeleteCustomer_Full',
        'BizCustomerLoss_Read', 'BizCustomerLoss_Full',
        'BizQuickReply_Read', 'BizQuickReply_Full',
        'BizQuickReplyGroup_Read', 'BizQuickReplyGroup_Full',
        'BizDepartment_Read', 'BizDepartment_Full',
        'BizMassMsg_Read', 'BizMassMsg_Full',
        'BizCustomerRemark_Read', 'BizCustomerRemark_Full',
        'BizCustomerTag_Read', 'BizCustomerTag_Full',
        'BizCustomerInfo_Read', 'BizCustomerInfo_Full',
        'BizStaffInfo_Read', 'BizStaffInfo_Full',
        'BizContactWay_Read', 'BizContactWay_Full'
    ),
    NOW(3),
    NOW(3),
    NULL
)
ON DUPLICATE KEY UPDATE
    `ext_corp_id` = VALUES(`ext_corp_id`),
    `name` = VALUES(`name`),
    `description` = VALUES(`description`),
    `type` = VALUES(`type`),
    `sort_weight` = VALUES(`sort_weight`),
    `is_default` = VALUES(`is_default`),
    `permission_ids` = VALUES(`permission_ids`),
    `updated_at` = NOW(3),
    `deleted_at` = NULL;

-- 2. 可用于 force-login 的员工
INSERT INTO `staff` (
    `ext_corp_id`,
    `ext_id`,
    `role_id`,
    `role_type`,
    `name`,
    `gender`,
    `status`,
    `enable`,
    `customer_count`,
    `dept_ids`,
    `is_authorized`,
    `enable_msg_arch`,
    `created_at`,
    `updated_at`,
    `deleted_at`
) VALUES (
    @ext_corp_id,
    @ext_staff_id,
    @role_id,
    'superAdmin',
    @staff_name,
    0,
    1,
    1,
    0,
    JSON_ARRAY(1),
    1,
    2,
    NOW(3),
    NOW(3),
    NULL
)
ON DUPLICATE KEY UPDATE
    `role_id` = VALUES(`role_id`),
    `role_type` = VALUES(`role_type`),
    `name` = VALUES(`name`),
    `status` = VALUES(`status`),
    `enable` = VALUES(`enable`),
    `dept_ids` = VALUES(`dept_ids`),
    `is_authorized` = VALUES(`is_authorized`),
    `updated_at` = NOW(3),
    `deleted_at` = NULL;

-- 3. 根部门（登录本身不依赖，但部门相关页面会用到）
INSERT INTO `department` (
    `ext_corp_id`,
    `ext_id`,
    `name`,
    `ext_parent_id`,
    `order`,
    `staff_num`,
    `created_at`,
    `updated_at`,
    `deleted_at`
) VALUES (
    @ext_corp_id,
    1,
    '根部门',
    0,
    1,
    1,
    NOW(3),
    NOW(3),
    NULL
)
ON DUPLICATE KEY UPDATE
    `name` = VALUES(`name`),
    `staff_num` = VALUES(`staff_num`),
    `updated_at` = NOW(3),
    `deleted_at` = NULL;

SET @staff_id = (
    SELECT `id`
    FROM `staff`
    WHERE `ext_corp_id` = @ext_corp_id
      AND `ext_id` = @ext_staff_id
    LIMIT 1
);

SET @department_id = (
    SELECT `id`
    FROM `department`
    WHERE `ext_corp_id` = @ext_corp_id
      AND `ext_id` = 1
    LIMIT 1
);

-- 4. 员工部门关系
INSERT INTO `staff_department` (
    `ext_corp_id`,
    `ext_staff_id`,
    `ext_department_id`,
    `staff_id`,
    `department_id`,
    `is_leader`,
    `order`
) VALUES (
    @ext_corp_id,
    @ext_staff_id,
    1,
    @staff_id,
    @department_id,
    1,
    1
)
ON DUPLICATE KEY UPDATE
    `is_leader` = VALUES(`is_leader`),
    `order` = VALUES(`order`);

COMMIT;