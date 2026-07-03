-- Generated from app/models AutoMigrate structs and GORM tags.
-- Review against a live GORM migration before production use.
SET NAMES utf8mb4;

DROP TABLE IF EXISTS `customer`;
CREATE TABLE `customer` (
  `id` bigint NOT NULL COMMENT 'ID',
  `ext_corp_id` char(18) COMMENT '外部企业ID',
  `ext_creator_id` char(64) COMMENT '创建者外部员工ID',
  `ext_id` char(64) COMMENT '微信定义的userID',
  `name` varchar(255) COMMENT '名称，微信用户对应微信昵称；企业微信用户，则为联系人或管理员设置的昵称、认证的实名和账号名称',
  `position` varchar(255) COMMENT '职位,客户为企业微信时使用',
  `corp_name` varchar(255) COMMENT '客户的公司名称,仅当客户ID为企业微信ID时存在',
  `avatar` varchar(255) COMMENT '头像',
  `type` tinyint(1) COMMENT '类型,1-微信用户, 2-企业微信用户',
  `gender` tinyint COMMENT '性别,0-未知 1-男性 2-女性',
  `unionid` varchar(128) COMMENT '微信开放平台的唯一身份标识(微信unionID)',
  `external_profile` json COMMENT '仅当联系人类型是企业微信用户时有此字段',
  `created_at` datetime(3) COMMENT '创建时间',
  `updated_at` datetime(3) COMMENT '更新时间',
  `deleted_at` datetime(3) COMMENT '删除时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_ext_customer_id` (`ext_id`),
  KEY `idx_ext_corp_id` (`ext_corp_id`),
  KEY `idx_ext_creator_id` (`ext_creator_id`),
  KEY `idx_type` (`type`),
  KEY `idx_created_at` (`created_at`),
  KEY `idx_updated_at` (`updated_at`),
  KEY `idx_deleted_at` (`deleted_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='客户';

DROP TABLE IF EXISTS `customer_info`;
CREATE TABLE `customer_info` (
  `id` bigint NOT NULL COMMENT 'ID',
  `ext_corp_id` char(18) COMMENT '外部企业ID',
  `ext_creator_id` char(64) COMMENT '创建者外部员工ID',
  `ext_customer_id` char(64) COMMENT '微信客户ID',
  `ext_staff_id` char(64) COMMENT '微信员工ID',
  `age` tinyint(3) COMMENT '年龄',
  `description` text COMMENT '描述',
  `email` varchar(64) COMMENT '邮箱',
  `phone_number` char(64) COMMENT '电话',
  `qq` varchar(16) COMMENT 'qq',
  `address` varchar(128) COMMENT '地址',
  `birthday` char(10) COMMENT '生日',
  `weibo` varchar(128) COMMENT '微博',
  `remark_field` json COMMENT '自定义字段的值',
  `created_at` datetime(3) COMMENT '创建时间',
  `updated_at` datetime(3) COMMENT '更新时间',
  `deleted_at` datetime(3) COMMENT '删除时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_ext_staff_id_ext_customer_id` (`ext_customer_id`, `ext_staff_id`),
  KEY `idx_ext_corp_id` (`ext_corp_id`),
  KEY `idx_ext_creator_id` (`ext_creator_id`),
  KEY `idx_created_at` (`created_at`),
  KEY `idx_updated_at` (`updated_at`),
  KEY `idx_deleted_at` (`deleted_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='对客户编辑的用户画像，不同于企业微信员工对客户的备注和描述，后者记录在staff_customer中';

DROP TABLE IF EXISTS `customer_info_display_rule`;
CREATE TABLE `customer_info_display_rule` (
  `id` bigint AUTO_INCREMENT NOT NULL COMMENT 'ID',
  `ext_corp_id` varchar(100) COMMENT '企业ID',
  `age` tinyint unsigned COMMENT '是否展示年龄',
  `description` tinyint unsigned COMMENT '是否展示描述',
  `email` tinyint unsigned COMMENT '是否展示邮箱',
  `phone_number` tinyint unsigned COMMENT '是否展示电话',
  `qq` tinyint unsigned COMMENT '是否展示mqq',
  `address` tinyint unsigned COMMENT '是否展示地址',
  `birthday` tinyint unsigned COMMENT '是否展示生日',
  `weibo` tinyint unsigned COMMENT '是否展示微博',
  `created_at` datetime(3) COMMENT '创建时间',
  `updated_at` datetime(3) COMMENT '更新时间',
  `deleted_at` datetime(3) COMMENT '删除时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uni_ext_corp_id` (`ext_corp_id`),
  KEY `idx_created_at` (`created_at`),
  KEY `idx_updated_at` (`updated_at`),
  KEY `idx_deleted_at` (`deleted_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='是否显示信息';

DROP TABLE IF EXISTS `customer_staff`;
CREATE TABLE `customer_staff` (
  `id` bigint NOT NULL COMMENT 'ID',
  `ext_corp_id` char(18) COMMENT '外部企业ID',
  `ext_creator_id` char(64) COMMENT '创建者外部员工ID',
  `ext_staff_id` char(64) COMMENT '员工ID',
  `ext_customer_id` char(64) COMMENT '客户ID',
  `remark` varchar(255) COMMENT '员工对客户的备注',
  `description` varchar(255) COMMENT '员工对此客户的描述',
  `createtime` datetime(3) COMMENT '员工添加客户的时间',
  `remark_corp_name` varchar(255) COMMENT '员工对客户备注的企业名称',
  `remark_mobiles` json COMMENT '对此客户备注的手机号码',
  `add_way` tinyint(8) COMMENT '添加此客户的来源,0-未知来源 1-扫描二维码 2-搜索手机号 3-名片分享 4-群聊 5-手机通讯录 6-微信联系人 7-来自微信的添加好友申请 8-安装第三方应用时自动添加的客服人员 9-搜索邮箱 201-内部成员共享 202-管理员/负责人分配',
  `oper_user_id` varchar(255) COMMENT '发起添加的userid',
  `state` varchar(255) COMMENT '区分客户具体是通过哪个「联系我」添加，由企业通过创建「联系我」方式指定',
  `is_notified` tinyint COMMENT '是否已发送通知 1-是 2-否',
  `internal_tag_ids` json,
  `signature` char(64),
  `created_at` datetime(3) COMMENT '创建时间',
  `updated_at` datetime(3) COMMENT '更新时间',
  `deleted_at` datetime(3) COMMENT '删除时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_ext_staff_id_ext_customer_id` (`ext_staff_id`, `ext_customer_id`),
  KEY `idx_ext_corp_id` (`ext_corp_id`),
  KEY `idx_ext_creator_id` (`ext_creator_id`),
  KEY `idx_ext_staff_id` (`ext_staff_id`),
  KEY `idx_ext_customer_id` (`ext_customer_id`),
  KEY `idx_signature` (`signature`),
  KEY `idx_created_at` (`created_at`),
  KEY `idx_updated_at` (`updated_at`),
  KEY `idx_deleted_at` (`deleted_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='客户-员工关系 员工客户关系的历史数据（流水记录）也在此表中。 员工删除客户/客户删除员工时 新增一条数据，写入 customer_delete_staff_at/staff_delete_customer_at, 同时软删除原有记录。';

DROP TABLE IF EXISTS `customer_staff_relation_history`;
CREATE TABLE `customer_staff_relation_history` (
  `id` bigint NOT NULL COMMENT 'ID',
  `ext_corp_id` char(18) COMMENT '外部企业ID',
  `ext_creator_id` char(64) COMMENT '创建者外部员工ID',
  `ext_staff_id` char(64) COMMENT '员工ID',
  `ext_customer_id` char(64) COMMENT '客户ID',
  `createtime` datetime(3) COMMENT '员工添加客户的时间',
  `customer_delete_staff_at` json COMMENT '客户删除员工的时间',
  `staff_delete_customer_at` json COMMENT '员工删除客户的时间',
  `created_at` datetime(3) COMMENT '创建时间',
  `updated_at` datetime(3) COMMENT '更新时间',
  `deleted_at` datetime(3) COMMENT '删除时间',
  PRIMARY KEY (`id`),
  KEY `idx_ext_corp_id` (`ext_corp_id`),
  KEY `idx_ext_creator_id` (`ext_creator_id`),
  KEY `idx_ext_staff_id` (`ext_staff_id`),
  KEY `idx_ext_customer_id` (`ext_customer_id`),
  KEY `idx_created_at` (`created_at`),
  KEY `idx_updated_at` (`updated_at`),
  KEY `idx_deleted_at` (`deleted_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='员工客户关系的历史数据（流水记录）。 员工删除客户/客户删除员工时 新增一条数据，写入 customer_delete_staff_at/staff_delete_customer_at, 同时软删除原有记录。';

DROP TABLE IF EXISTS `staff`;
CREATE TABLE `staff` (
  `id` bigint AUTO_INCREMENT NOT NULL COMMENT 'ID',
  `ext_corp_id` char(18) COMMENT '外部企业ID',
  `ext_id` varchar(64) COMMENT '外部员工ID',
  `role_id` bigint COMMENT '角色ID',
  `role_type` varchar(191) DEFAULT 'staff' COMMENT '角色类型',
  `name` varchar(255) COMMENT '员工名',
  `address` varchar(255) COMMENT '地址',
  `alias` varchar(255) COMMENT '别名',
  `avatar_url` varchar(128) COMMENT '头像地址',
  `email` varchar(128),
  `gender` tinyint COMMENT '0表示未定义，1表示男性，2表示女性',
  `status` tinyint COMMENT '激活状态: 1=已激活，2=已禁用，4=未激活，5=退出企业。已激活代表已激活企业微信或已关注微工作台（原企业号）。未激活代表既未激活企业微信又未关注微工作台（原企业号）。',
  `mobile` varchar(11) COMMENT '手机号',
  `qr_code_url` varchar(255) COMMENT '二维码',
  `telephone` char(11) COMMENT '电话',
  `enable` tinyint unsigned,
  `signature` char(128) COMMENT '微信返回的内容签名',
  `external_position` longtext,
  `external_profile` longtext,
  `extattr` longtext,
  `customer_count` bigint,
  `dept_ids` json,
  `welcome_msg_id` bigint,
  `is_authorized` tinyint unsigned,
  `enable_msg_arch` tinyint unsigned DEFAULT 2,
  `created_at` datetime(3) COMMENT '创建时间',
  `updated_at` datetime(3) COMMENT '更新时间',
  `deleted_at` datetime(3) COMMENT '删除时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_ext_corp_id_ext_staff_id` (`ext_corp_id`, `ext_id`),
  KEY `idx_ext_corp_id` (`ext_corp_id`),
  KEY `idx_role_type` (`role_type`),
  KEY `idx_mobile` (`mobile`),
  KEY `idx_welcome_msg_id` (`welcome_msg_id`),
  KEY `idx_created_at` (`created_at`),
  KEY `idx_updated_at` (`updated_at`),
  KEY `idx_deleted_at` (`deleted_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='员工';

DROP TABLE IF EXISTS `department`;
CREATE TABLE `department` (
  `id` bigint AUTO_INCREMENT NOT NULL COMMENT 'ID',
  `ext_corp_id` char(18) COMMENT '外部企业ID',
  `ext_id` int COMMENT '企微定义的部门ID',
  `name` varchar(255) COMMENT '部门名称',
  `ext_parent_id` int unsigned COMMENT '上级部门ID,根部门为1',
  `order` int unsigned COMMENT '在父部门中的次序值',
  `welcome_msg_id` bigint COMMENT '部门使用的欢迎语',
  `staff_num` int DEFAULT 0 COMMENT '成员数量',
  `created_at` datetime(3) COMMENT '创建时间',
  `updated_at` datetime(3) COMMENT '更新时间',
  `deleted_at` datetime(3) COMMENT '删除时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_ext_corp_id_ext_dept_id` (`ext_corp_id`, `ext_id`),
  KEY `idx_ext_corp_id` (`ext_corp_id`),
  KEY `idx_created_at` (`created_at`),
  KEY `idx_updated_at` (`updated_at`),
  KEY `idx_deleted_at` (`deleted_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='部门';

DROP TABLE IF EXISTS `material_lib_tag`;
CREATE TABLE `material_lib_tag` (
  `id` bigint NOT NULL COMMENT 'ID',
  `ext_corp_id` char(18) COMMENT '外部企业ID',
  `ext_creator_id` char(64) COMMENT '创建者外部员工ID',
  `name` longtext,
  `created_at` datetime(3) COMMENT '创建时间',
  `updated_at` datetime(3) COMMENT '更新时间',
  `deleted_at` datetime(3) COMMENT '删除时间',
  PRIMARY KEY (`id`),
  KEY `idx_ext_corp_id` (`ext_corp_id`),
  KEY `idx_ext_creator_id` (`ext_creator_id`),
  KEY `idx_created_at` (`created_at`),
  KEY `idx_updated_at` (`updated_at`),
  KEY `idx_deleted_at` (`deleted_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='素材库标签';

DROP TABLE IF EXISTS `staff_department`;
CREATE TABLE `staff_department` (
  `ext_corp_id` char(18),
  `ext_staff_id` varchar(64),
  `ext_department_id` int unsigned,
  `staff_id` bigint NOT NULL,
  `department_id` bigint NOT NULL,
  `is_leader` tinyint unsigned COMMENT '是否是所在部门的领导',
  `order` int unsigned COMMENT '所在部门的排序',
  PRIMARY KEY (`staff_id`, `department_id`),
  UNIQUE KEY `idx_ext_corp_id_ext_staff_id` (`ext_corp_id`, `ext_staff_id`, `ext_department_id`),
  KEY `idx_ext_corp_id` (`ext_corp_id`),
  KEY `idx_ext_staff_id` (`ext_staff_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='员工部门关系';

DROP TABLE IF EXISTS `customer_staff_tag`;
CREATE TABLE `customer_staff_tag` (
  `id` bigint NOT NULL COMMENT 'ID',
  `ext_corp_id` char(18) COMMENT '外部企业ID',
  `ext_creator_id` char(64) COMMENT '创建者外部员工ID',
  `customer_staff_id` bigint,
  `ext_tag_id` char(64),
  `group_name` longtext,
  `tag_name` longtext,
  `type` tinyint,
  `created_at` datetime(3) COMMENT '创建时间',
  `updated_at` datetime(3) COMMENT '更新时间',
  `deleted_at` datetime(3) COMMENT '删除时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `ext_tag_id_cs_id` (`customer_staff_id`, `ext_tag_id`),
  KEY `idx_ext_corp_id` (`ext_corp_id`),
  KEY `idx_ext_creator_id` (`ext_creator_id`),
  KEY `idx_created_at` (`created_at`),
  KEY `idx_updated_at` (`updated_at`),
  KEY `idx_deleted_at` (`deleted_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='客户员工标签关系';

DROP TABLE IF EXISTS `group_chat_tag_group`;
CREATE TABLE `group_chat_tag_group` (
  `id` bigint NOT NULL COMMENT 'ID',
  `ext_corp_id` char(18) COMMENT '外部企业ID',
  `ext_creator_id` char(64) COMMENT '创建者外部员工ID',
  `name` char(64),
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`),
  KEY `idx_ext_corp_id` (`ext_corp_id`),
  KEY `idx_ext_creator_id` (`ext_creator_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='客户群标签组';

DROP TABLE IF EXISTS `corp_setting`;
CREATE TABLE `corp_setting` (
  `id` bigint NOT NULL COMMENT 'ID',
  `ext_corp_id` char(18) COMMENT '外部企业ID',
  `ext_creator_id` char(64) COMMENT '创建者外部员工ID',
  `is_material_used` tinyint unsigned,
  `created_at` datetime(3) COMMENT '创建时间',
  `updated_at` datetime(3) COMMENT '更新时间',
  `deleted_at` datetime(3) COMMENT '删除时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `ext_corp_id` (`ext_corp_id`),
  KEY `idx_ext_creator_id` (`ext_creator_id`),
  KEY `idx_created_at` (`created_at`),
  KEY `idx_updated_at` (`updated_at`),
  KEY `idx_deleted_at` (`deleted_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='企业设置';

DROP TABLE IF EXISTS `group_chat_tag`;
CREATE TABLE `group_chat_tag` (
  `id` bigint NOT NULL COMMENT 'ID',
  `ext_corp_id` char(18) COMMENT '外部企业ID',
  `ext_creator_id` char(64) COMMENT '创建者外部员工ID',
  `group_chat_tag_group_id` varchar(191),
  `name` char(64),
  `created_at` datetime(3) COMMENT '创建时间',
  `updated_at` datetime(3) COMMENT '更新时间',
  `deleted_at` datetime(3) COMMENT '删除时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_group_id_name` (`group_chat_tag_group_id`, `name`),
  KEY `idx_ext_corp_id` (`ext_corp_id`),
  KEY `idx_ext_creator_id` (`ext_creator_id`),
  KEY `idx_created_at` (`created_at`),
  KEY `idx_updated_at` (`updated_at`),
  KEY `idx_deleted_at` (`deleted_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='客户群标签';

DROP TABLE IF EXISTS `tag_group`;
CREATE TABLE `tag_group` (
  `id` bigint NOT NULL COMMENT 'ID',
  `ext_corp_id` char(18) COMMENT '外部企业ID',
  `ext_creator_id` char(64) COMMENT '创建者外部员工ID',
  `ext_id` char(64) COMMENT '外部标签分组ID',
  `name` varchar(191) COMMENT '组名字',
  `create_time` int(16) COMMENT '',
  `order` int(32) COMMENT 'order值大的排序靠前',
  `department_list` json COMMENT '该标签组可用部门列表,默认0全部可用',
  `created_at` datetime(3) COMMENT '创建时间',
  `updated_at` datetime(3) COMMENT '更新时间',
  `deleted_at` datetime(3) COMMENT '删除时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uni_ext_id` (`ext_id`),
  KEY `idx_ext_corp_id` (`ext_corp_id`),
  KEY `idx_ext_creator_id` (`ext_creator_id`),
  KEY `idx_name` (`name`),
  KEY `idx_order` (`order`),
  KEY `idx_created_at` (`created_at`),
  KEY `idx_updated_at` (`updated_at`),
  KEY `idx_deleted_at` (`deleted_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='企微客户标签组';

DROP TABLE IF EXISTS `tag`;
CREATE TABLE `tag` (
  `id` bigint NOT NULL COMMENT 'ID',
  `ext_corp_id` char(18) COMMENT '外部企业ID',
  `ext_creator_id` char(64) COMMENT '创建者外部员工ID',
  `ext_id` char(64) COMMENT '外部标签ID',
  `ext_group_id` char(64) COMMENT '外部标签组ID',
  `name` char(255) COMMENT '标签名称',
  `group_name` char(255) COMMENT '标签组名称',
  `create_time` int COMMENT '创建时间',
  `order` int unsigned COMMENT '标签排序值，值大的在前',
  `type` tinyint COMMENT '所打标签类型, 1-企业设置, 2-用户自定义',
  `created_at` datetime(3) COMMENT '创建时间',
  `updated_at` datetime(3) COMMENT '更新时间',
  `deleted_at` datetime(3) COMMENT '删除时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uni_ext_id` (`ext_id`),
  UNIQUE KEY `idx_group_name_tag_name` (`name`, `group_name`),
  KEY `idx_ext_corp_id` (`ext_corp_id`),
  KEY `idx_ext_creator_id` (`ext_creator_id`),
  KEY `idx_ext_group_id` (`ext_group_id`),
  KEY `idx_order` (`order`),
  KEY `idx_created_at` (`created_at`),
  KEY `idx_updated_at` (`updated_at`),
  KEY `idx_deleted_at` (`deleted_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='企微客户标签';

DROP TABLE IF EXISTS `chat_msg`;
CREATE TABLE `chat_msg` (
  `id` bigint NOT NULL COMMENT 'ID',
  `ext_corp_id` char(18) COMMENT '外部企业ID',
  `ext_creator_id` char(64) COMMENT '创建者外部员工ID',
  `msg_id` char(128) COMMENT '外部消息ID',
  `action` char(8) COMMENT '消息动作，目前有send(发送消息)/recall(撤回消息)/switch(切换企业日志)三种类型',
  `from` char(64) COMMENT '消息发送方id。同一企业内容为userid，非相同企业为external_userid。消息如果是机器人发出，也为external_userid',
  `to_list` json COMMENT '消息接收方列表',
  `room_id` char(128) COMMENT '群聊消息的群id。如果是单聊则为空',
  `msg_time` bigint(64) COMMENT '消息发送时间戳，utc时间，ms单位。',
  `msg_type` varchar(64) COMMENT '文本消息为：text',
  `content_text` longtext COMMENT '聊天的文本内容',
  `seq` bigint COMMENT '消息的seq值，标识消息的序号。再次拉取需要带上上次回包中最大的seq。Uint64类型，范围0-pow(2,64)-1',
  `session_id` char(128) COMMENT '消息的会话ID,相同收发方的会话ID相同',
  `session_type` char(64) COMMENT '会话类型',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uni_msg_id` (`msg_id`),
  KEY `idx_ext_corp_id` (`ext_corp_id`),
  KEY `idx_ext_creator_id` (`ext_creator_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会话存档消息';

DROP TABLE IF EXISTS `chat_msg_content`;
CREATE TABLE `chat_msg_content` (
  `id` bigint NOT NULL COMMENT 'ID',
  `ext_corp_id` char(18) COMMENT '外部企业ID',
  `ext_creator_id` char(64) COMMENT '创建者外部员工ID',
  `chat_msg_id` longtext COMMENT '内部消息ID',
  `content_type` longtext COMMENT '消息类型',
  `content` longtext COMMENT '非文字类型的消息内容',
  `file_url` longtext COMMENT '文件下载地址',
  `file_name` longtext COMMENT '文件名',
  `created_at` datetime(3) COMMENT '创建时间',
  `updated_at` datetime(3) COMMENT '更新时间',
  `deleted_at` datetime(3) COMMENT '删除时间',
  PRIMARY KEY (`id`),
  KEY `idx_ext_corp_id` (`ext_corp_id`),
  KEY `idx_ext_creator_id` (`ext_creator_id`),
  KEY `idx_created_at` (`created_at`),
  KEY `idx_updated_at` (`updated_at`),
  KEY `idx_deleted_at` (`deleted_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会话存档消息内容';

DROP TABLE IF EXISTS `quick_reply_group`;
CREATE TABLE `quick_reply_group` (
  `id` bigint NOT NULL COMMENT 'ID',
  `ext_corp_id` char(18) COMMENT '外部企业ID',
  `ext_creator_id` char(64) COMMENT '创建者外部员工ID',
  `name` varchar(255) COMMENT '分组名称',
  `parent_id` bigint COMMENT '父级id',
  `departments` json COMMENT '可见部门ids',
  `is_top_group` tinyint COMMENT '是否是顶级分组',
  `order` int,
  `created_at` datetime(3) COMMENT '创建时间',
  `updated_at` datetime(3) COMMENT '更新时间',
  `deleted_at` datetime(3) COMMENT '删除时间',
  PRIMARY KEY (`id`),
  KEY `idx_ext_corp_id` (`ext_corp_id`),
  KEY `idx_ext_creator_id` (`ext_creator_id`),
  KEY `idx_created_at` (`created_at`),
  KEY `idx_updated_at` (`updated_at`),
  KEY `idx_deleted_at` (`deleted_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='话术库分组';

DROP TABLE IF EXISTS `quick_reply`;
CREATE TABLE `quick_reply` (
  `id` bigint NOT NULL COMMENT 'ID',
  `ext_corp_id` char(18) COMMENT '外部企业ID',
  `ext_creator_id` char(64) COMMENT '创建者外部员工ID',
  `corp_id` longtext COMMENT '内部企业id',
  `name` varchar(255) COMMENT '话术名',
  `quick_reply_type` tinyint,
  `searchable_text` json COMMENT '用于搜索的词语，多为标题',
  `send_count` int unsigned COMMENT '已发送次数',
  `ext_staff_id` char(64) COMMENT '创建人企微ID',
  `staff_name` varchar(128) COMMENT '创建人名字',
  `scope` varchar(64),
  `group_id` longtext,
  `created_at` datetime(3) COMMENT '创建时间',
  `updated_at` datetime(3) COMMENT '更新时间',
  `deleted_at` datetime(3) COMMENT '删除时间',
  PRIMARY KEY (`id`),
  KEY `idx_ext_corp_id` (`ext_corp_id`),
  KEY `idx_ext_creator_id` (`ext_creator_id`),
  KEY `idx_created_at` (`created_at`),
  KEY `idx_updated_at` (`updated_at`),
  KEY `idx_deleted_at` (`deleted_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='话术内容';

DROP TABLE IF EXISTS `quick_reply_detail`;
CREATE TABLE `quick_reply_detail` (
  `id` bigint NOT NULL COMMENT 'ID',
  `ext_corp_id` char(18) COMMENT '外部企业ID',
  `ext_creator_id` char(64) COMMENT '创建者外部员工ID',
  `quick_reply_id` longtext,
  `quick_reply_content` json,
  `scope` longtext,
  `content_type` tinyint unsigned COMMENT '单项类型',
  `send_count` bigint,
  `created_at` datetime(3) COMMENT '创建时间',
  `updated_at` datetime(3) COMMENT '更新时间',
  `deleted_at` datetime(3) COMMENT '删除时间',
  PRIMARY KEY (`id`),
  KEY `idx_ext_corp_id` (`ext_corp_id`),
  KEY `idx_ext_creator_id` (`ext_creator_id`),
  KEY `idx_created_at` (`created_at`),
  KEY `idx_updated_at` (`updated_at`),
  KEY `idx_deleted_at` (`deleted_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='话术库每条记录内容';

DROP TABLE IF EXISTS `customer_remark`;
CREATE TABLE `customer_remark` (
  `id` bigint NOT NULL COMMENT 'ID',
  `ext_corp_id` char(18) COMMENT '外部企业ID',
  `ext_creator_id` char(64) COMMENT '创建者外部员工ID',
  `name` char(64),
  `field_type` longtext,
  `has_staff_used` boolean,
  `rank_num` int unsigned,
  `created_at` datetime(3) COMMENT '创建时间',
  `updated_at` datetime(3) COMMENT '更新时间',
  `deleted_at` datetime(3) COMMENT '删除时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_corp_id_name` (`name`),
  KEY `idx_ext_corp_id` (`ext_corp_id`),
  KEY `idx_ext_creator_id` (`ext_creator_id`),
  KEY `idx_created_at` (`created_at`),
  KEY `idx_updated_at` (`updated_at`),
  KEY `idx_deleted_at` (`deleted_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='自定义信息';

DROP TABLE IF EXISTS `remark_option`;
CREATE TABLE `remark_option` (
  `id` bigint AUTO_INCREMENT NOT NULL COMMENT 'ID',
  `remark_id` bigint,
  `name` char(64),
  `created_at` datetime(3) COMMENT '创建时间',
  `updated_at` datetime(3) COMMENT '更新时间',
  `deleted_at` datetime(3) COMMENT '删除时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_remark_id_name` (`remark_id`, `name`),
  KEY `idx_created_at` (`created_at`),
  KEY `idx_updated_at` (`updated_at`),
  KEY `idx_deleted_at` (`deleted_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='对于多选类型信息的选项';

DROP TABLE IF EXISTS `customer_event`;
CREATE TABLE `customer_event` (
  `id` bigint NOT NULL COMMENT 'ID',
  `ext_corp_id` char(18) COMMENT '外部企业ID',
  `ext_creator_id` char(64) COMMENT '创建者外部员工ID',
  `content` text COMMENT '事件内容',
  `event_type` char(64) COMMENT '事件类型',
  `event_name` char(64) COMMENT '事件名称',
  `ext_customer_id` char(64) COMMENT '企微定义的客户ID',
  `ext_staff_id` varchar(64) COMMENT '微信定义的员工ID',
  `relate_staff_avatar` varchar(128) COMMENT '员工头像',
  `relate_staff_name` varchar(255) COMMENT '员工名字',
  `send_at` tinyint COMMENT '提醒类型事件的发送时间',
  `created_at` datetime(3) COMMENT '创建时间',
  `updated_at` datetime(3) COMMENT '更新时间',
  `deleted_at` datetime(3) COMMENT '删除时间',
  PRIMARY KEY (`id`),
  KEY `idx_ext_corp_id` (`ext_corp_id`),
  KEY `idx_ext_creator_id` (`ext_creator_id`),
  KEY `idx_event_type` (`event_type`),
  KEY `idx_ext_staff_id` (`ext_staff_id`),
  KEY `idx_created_at` (`created_at`),
  KEY `idx_updated_at` (`updated_at`),
  KEY `idx_deleted_at` (`deleted_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='事件类型';

DROP TABLE IF EXISTS `internal_tag`;
CREATE TABLE `internal_tag` (
  `id` bigint NOT NULL COMMENT 'ID',
  `ext_corp_id` char(18) COMMENT '外部企业ID',
  `ext_creator_id` char(64) COMMENT '创建者外部员工ID',
  `ext_staff_id` char(64),
  `name` char(64),
  `created_at` datetime(3) COMMENT '创建时间',
  `updated_at` datetime(3) COMMENT '更新时间',
  `deleted_at` datetime(3) COMMENT '删除时间',
  PRIMARY KEY (`id`),
  KEY `idx_ext_corp_id` (`ext_corp_id`),
  KEY `idx_ext_creator_id` (`ext_creator_id`),
  KEY `idx_ext_staff_id` (`ext_staff_id`),
  KEY `idx_created_at` (`created_at`),
  KEY `idx_updated_at` (`updated_at`),
  KEY `idx_deleted_at` (`deleted_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='内部客户标签';

DROP TABLE IF EXISTS `material`;
CREATE TABLE `material` (
  `id` bigint NOT NULL COMMENT 'ID',
  `ext_corp_id` char(18) COMMENT '外部企业ID',
  `ext_creator_id` char(64) COMMENT '创建者外部员工ID',
  `material_type` char(12),
  `title` char(64) COMMENT '素材标题',
  `file_size` bigint COMMENT '素材文件大小',
  `file_url` varchar(512) COMMENT '素材下载地址',
  `link` varchar(255),
  `digest` text COMMENT '链接类型素材的摘要',
  `material_tag_list` json,
  `created_at` datetime(3) COMMENT '创建时间',
  `updated_at` datetime(3) COMMENT '更新时间',
  `deleted_at` datetime(3) COMMENT '删除时间',
  PRIMARY KEY (`id`),
  KEY `idx_ext_corp_id` (`ext_corp_id`),
  KEY `idx_ext_creator_id` (`ext_creator_id`),
  KEY `idx_material_type` (`material_type`),
  KEY `idx_title` (`title`),
  KEY `idx_created_at` (`created_at`),
  KEY `idx_updated_at` (`updated_at`),
  KEY `idx_deleted_at` (`deleted_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='素材';

DROP TABLE IF EXISTS `remainder`;
CREATE TABLE `remainder` (
  `id` bigint NOT NULL COMMENT 'ID',
  `ext_corp_id` char(18) COMMENT '外部企业ID',
  `ext_creator_id` char(64) COMMENT '创建者外部员工ID',
  `content` longtext COMMENT '提醒内容',
  `send_at` datetime(3) COMMENT '提醒时间',
  `created_at` datetime(3) COMMENT '创建时间',
  `updated_at` datetime(3) COMMENT '更新时间',
  `deleted_at` datetime(3) COMMENT '删除时间',
  PRIMARY KEY (`id`),
  KEY `idx_ext_corp_id` (`ext_corp_id`),
  KEY `idx_ext_creator_id` (`ext_creator_id`),
  KEY `idx_created_at` (`created_at`),
  KEY `idx_updated_at` (`updated_at`),
  KEY `idx_deleted_at` (`deleted_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='客户提醒';

DROP TABLE IF EXISTS `mass_msg`;
CREATE TABLE `mass_msg` (
  `id` bigint NOT NULL COMMENT 'ID',
  `ext_corp_id` char(18) COMMENT '外部企业ID',
  `ext_creator_id` char(64) COMMENT '创建者外部员工ID',
  `send_type` tinyint unsigned COMMENT '1-立即发送,2-定时发送',
  `ext_staff_ids` JSON,
  `ext_department_ids` JSON,
  `msg` json COMMENT '消息内容',
  `ext_msg_id` varchar(33) COMMENT '微信消息ID',
  `mission_status` tinyint unsigned COMMENT '创建企业群发消息的状态,1-预约发送,2-发送中,3-发送成功,4-发送失败,5-已取消',
  `ext_customer_filter_enable` tinyint unsigned COMMENT '是否有筛选条件',
  `ext_customer_filter` json COMMENT '发送客户的筛选条件',
  `delivered_num` int COMMENT '已发送消息的员工数',
  `success_num` int COMMENT '成功送达消息的员工数',
  `un_delivered_num` int COMMENT '需要发送消息的员工总数',
  `failed_num` int COMMENT '未送达客户计数',
  `send_at` tinyint,
  `created_at` datetime(3) COMMENT '创建时间',
  `updated_at` datetime(3) COMMENT '更新时间',
  `deleted_at` datetime(3) COMMENT '删除时间',
  PRIMARY KEY (`id`),
  KEY `idx_ext_corp_id` (`ext_corp_id`),
  KEY `idx_ext_creator_id` (`ext_creator_id`),
  KEY `idx_ext_msg_id` (`ext_msg_id`),
  KEY `idx_created_at` (`created_at`),
  KEY `idx_updated_at` (`updated_at`),
  KEY `idx_deleted_at` (`deleted_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='企业群发消息内容 消息内容不可修改';

DROP TABLE IF EXISTS `mass_msg_staff`;
CREATE TABLE `mass_msg_staff` (
  `id` bigint NOT NULL COMMENT 'ID',
  `ext_corp_id` char(18) COMMENT '外部企业ID',
  `ext_creator_id` char(64) COMMENT '创建者外部员工ID',
  `mass_msg_id` bigint,
  `ext_staff_id` longtext,
  `ext_customer_id` longtext,
  `ext_chat_id` longtext,
  `is_sent` tinyint unsigned DEFAULT 2,
  `is_delivered` tinyint unsigned DEFAULT 2,
  `failed_reason` tinyint unsigned,
  `created_at` datetime(3) COMMENT '创建时间',
  `updated_at` datetime(3) COMMENT '更新时间',
  `deleted_at` datetime(3) COMMENT '删除时间',
  PRIMARY KEY (`id`),
  KEY `idx_ext_corp_id` (`ext_corp_id`),
  KEY `idx_ext_creator_id` (`ext_creator_id`),
  KEY `idx_mass_msg_id` (`mass_msg_id`),
  KEY `idx_created_at` (`created_at`),
  KEY `idx_updated_at` (`updated_at`),
  KEY `idx_deleted_at` (`deleted_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='客户群发员工执行记录';

DROP TABLE IF EXISTS `welcome_msg`;
CREATE TABLE `welcome_msg` (
  `id` bigint NOT NULL COMMENT 'ID',
  `ext_corp_id` char(18) COMMENT '外部企业ID',
  `ext_creator_id` char(64) COMMENT '创建者外部员工ID',
  `name` char(128) COMMENT '标题',
  `welcome_msg` json,
  `main_welcome_msg_id` bigint,
  `enable_time_period_msg` tinyint,
  `effective_at` json,
  `start_time` bigint COMMENT '分时段欢迎语-开始时间',
  `end_time` bigint COMMENT '分时段欢迎语-结束时间',
  `created_at` datetime(3) COMMENT '创建时间',
  `updated_at` datetime(3) COMMENT '更新时间',
  `deleted_at` datetime(3) COMMENT '删除时间',
  PRIMARY KEY (`id`),
  KEY `idx_ext_corp_id` (`ext_corp_id`),
  KEY `idx_ext_creator_id` (`ext_creator_id`),
  KEY `idx_created_at` (`created_at`),
  KEY `idx_updated_at` (`updated_at`),
  KEY `idx_deleted_at` (`deleted_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='主欢迎语-多个分时欢迎语 主欢迎语维护可用员工和部门id列表';

DROP TABLE IF EXISTS `event_notify`;
CREATE TABLE `event_notify` (
  `id` bigint NOT NULL COMMENT 'ID',
  `ext_corp_id` char(18) COMMENT '外部企业ID',
  `event_name` text COMMENT '时间名称',
  `is_notify_admins` tinyint COMMENT '1-打开 2-关闭',
  `is_notify_staff` tinyint COMMENT '1-打开 2-关闭',
  `notify_type` tinyint COMMENT '通知类型 1-实时 2-定时',
  `ext_staff_ids` json COMMENT '接收通知的管理员',
  `created_at` datetime(3) COMMENT '创建时间',
  `updated_at` datetime(3) COMMENT '更新时间',
  `deleted_at` datetime(3) COMMENT '删除时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uni_ext_corp_id` (`ext_corp_id`),
  KEY `idx_created_at` (`created_at`),
  KEY `idx_updated_at` (`updated_at`),
  KEY `idx_deleted_at` (`deleted_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='删人提醒事件通知设置';

DROP TABLE IF EXISTS `group_chat`;
CREATE TABLE `group_chat` (
  `id` bigint NOT NULL COMMENT 'ID',
  `ext_corp_id` char(18) COMMENT '外部企业ID',
  `ext_creator_id` char(64) COMMENT '创建者外部员工ID',
  `ext_chat_id` char(64) COMMENT '群聊id',
  `name` varchar(255) COMMENT '群名字',
  `owner` char(64) COMMENT '群主ExtID',
  `owner_name` char(64) COMMENT '群主名字',
  `create_time` datetime(3) COMMENT '创建时间',
  `notice` text COMMENT '群公告',
  `admin_list` json COMMENT '群管理员列表',
  `status` tinyint unsigned DEFAULT 2 COMMENT '群状态 1-解散 2-未解散',
  `total` int unsigned DEFAULT 0 COMMENT '群人数',
  `today_join_member_num` int unsigned DEFAULT 0 COMMENT '今日进群人数',
  `today_quit_member_num` int unsigned DEFAULT 0 COMMENT '今日退群人数',
  `owner_avatar_url` longtext,
  `owner_role_type` longtext,
  `created_at` datetime(3) COMMENT '创建时间',
  `updated_at` datetime(3) COMMENT '更新时间',
  `deleted_at` datetime(3) COMMENT '删除时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uni_ext_chat_id` (`ext_chat_id`),
  KEY `idx_ext_corp_id` (`ext_corp_id`),
  KEY `idx_ext_creator_id` (`ext_creator_id`),
  KEY `idx_name` (`name`),
  KEY `idx_owner` (`owner`),
  KEY `idx_created_at` (`created_at`),
  KEY `idx_updated_at` (`updated_at`),
  KEY `idx_deleted_at` (`deleted_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='客户群';

DROP TABLE IF EXISTS `group_chat_member`;
CREATE TABLE `group_chat_member` (
  `id` bigint NOT NULL COMMENT 'ID',
  `ext_corp_id` char(18) COMMENT '外部企业ID',
  `ext_creator_id` char(64) COMMENT '创建者外部员工ID',
  `ext_chat_id` char(64) COMMENT '群聊id',
  `userid` char(64) COMMENT '群成员id',
  `type` tinyint COMMENT '群成员类型',
  `join_time` bigint COMMENT '入群时间',
  `join_scene` tinyint COMMENT '入群方式',
  `invitor` char(64) COMMENT '邀请者。目前仅当是由本企业内部成员邀请入群时会返回该值',
  `unionid` char(64) COMMENT '外部联系人在微信开放平台的唯一身份标识（微信unionid）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_chat_id_user_id` (`ext_chat_id`, `userid`),
  KEY `idx_ext_corp_id` (`ext_corp_id`),
  KEY `idx_ext_creator_id` (`ext_creator_id`),
  KEY `idx_ext_chat_id` (`ext_chat_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='客户群成员';

DROP TABLE IF EXISTS `group_chat_group`;
CREATE TABLE `group_chat_group` (
  `id` bigint NOT NULL COMMENT 'ID',
  `ext_corp_id` char(18) COMMENT '外部企业ID',
  `ext_creator_id` char(64) COMMENT '创建者外部员工ID',
  `name` text,
  `is_default` tinyint DEFAULT 2 COMMENT '是否为默认分组，1：是；2：否',
  `created_at` datetime(3) COMMENT '创建时间',
  `updated_at` datetime(3) COMMENT '更新时间',
  `deleted_at` datetime(3) COMMENT '删除时间',
  PRIMARY KEY (`id`),
  KEY `idx_ext_corp_id` (`ext_corp_id`),
  KEY `idx_ext_creator_id` (`ext_creator_id`),
  KEY `idx_created_at` (`created_at`),
  KEY `idx_updated_at` (`updated_at`),
  KEY `idx_deleted_at` (`deleted_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='客户群分组';

DROP TABLE IF EXISTS `group_chat_auto_join_code`;
CREATE TABLE `group_chat_auto_join_code` (
  `id` bigint NOT NULL COMMENT 'ID',
  `ext_corp_id` char(18) COMMENT '外部企业ID',
  `ext_creator_id` char(64) COMMENT '创建者外部员工ID',
  `create_type` tinyint COMMENT '拉群方式，1-群二维码，2-企微活码',
  `group_id` bigint,
  `remark` varchar(128),
  `auto_reply` text,
  `day_add_user_limit_enable` tinyint unsigned,
  `backup_staff_ids` json,
  `config_id` varchar(191) COMMENT '自动拉群码配置ID',
  `qr_code` longtext COMMENT '联系二维码的URL',
  `skip_verify` tinyint unsigned DEFAULT 1 COMMENT '外部客户添加时是否无需验证，假布尔类型',
  `state` longtext COMMENT '企业自定义的state参数',
  `add_customer_count` bigint DEFAULT 0 COMMENT '扫码添加人次',
  `daily_add_customer_limit_enable` tinyint COMMENT '是否开启员工每日添加上限',
  `auto_tag_enable` tinyint COMMENT '是否自动打标签',
  `ext_tag_ids` json COMMENT '自动打标签绑定的标签ID数组',
  `ext_staff_ids` json COMMENT '关联的外部员工ID',
  `created_at` datetime(3) COMMENT '创建时间',
  `updated_at` datetime(3) COMMENT '更新时间',
  `deleted_at` datetime(3) COMMENT '删除时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_group_id_remark` (`group_id`, `remark`),
  KEY `idx_ext_corp_id` (`ext_corp_id`),
  KEY `idx_ext_creator_id` (`ext_creator_id`),
  KEY `idx_remark` (`remark`),
  KEY `idx_config_id` (`config_id`),
  KEY `idx_created_at` (`created_at`),
  KEY `idx_updated_at` (`updated_at`),
  KEY `idx_deleted_at` (`deleted_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='自动拉群码';

DROP TABLE IF EXISTS `group_chat_qrcode`;
CREATE TABLE `group_chat_qrcode` (
  `group_chat_auto_join_id` bigint COMMENT '自动拉群码id',
  `order` int unsigned,
  `qr_media_id` text COMMENT '群二维码pic media id',
  `qr_url` text COMMENT '群二维码的pic url',
  `user_limit` int unsigned COMMENT '群二维码添加好友数上限',
  `status` tinyint unsigned COMMENT '群二维码状态,1- 使用中 2-已停用'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='自动拉群码中的群二维码';

DROP TABLE IF EXISTS `group_chat_auto_join_code_staff`;
CREATE TABLE `group_chat_auto_join_code_staff` (
  `id` bigint NOT NULL COMMENT 'ID',
  `ext_corp_id` char(18) COMMENT '外部企业ID',
  `ext_creator_id` char(64) COMMENT '创建者外部员工ID',
  `group_chat_auto_join_code_id` bigint COMMENT '自动拉群码id',
  `daily_add_customer_count` bigint DEFAULT 0 COMMENT '员工每日添加客户计数',
  `add_customer_count` bigint DEFAULT 0 COMMENT '员工累计添加客户计数',
  `daily_add_customer_limit` bigint COMMENT '员工每日添加客户上限',
  `avatar` longtext COMMENT '员工头像',
  `staff_id` bigint COMMENT '员工ID',
  `ext_staff_id` varchar(191) COMMENT '外部员工ID',
  `name` longtext COMMENT '员工名称',
  `created_at` datetime(3) COMMENT '创建时间',
  `updated_at` datetime(3) COMMENT '更新时间',
  `deleted_at` datetime(3) COMMENT '删除时间',
  PRIMARY KEY (`id`),
  KEY `idx_ext_corp_id` (`ext_corp_id`),
  KEY `idx_ext_creator_id` (`ext_creator_id`),
  KEY `GroupChatAutoJoinCodeIndex` (`group_chat_auto_join_code_id`, `staff_id`),
  KEY `idx_ext_staff_id` (`ext_staff_id`),
  KEY `idx_created_at` (`created_at`),
  KEY `idx_updated_at` (`updated_at`),
  KEY `idx_deleted_at` (`deleted_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='自动拉群码绑定的员工';

DROP TABLE IF EXISTS `group_chat_welcome_msg`;
CREATE TABLE `group_chat_welcome_msg` (
  `id` bigint NOT NULL COMMENT 'ID',
  `ext_corp_id` char(18) COMMENT '外部企业ID',
  `ext_creator_id` char(64) COMMENT '创建者外部员工ID',
  `content` longtext,
  `attachment_type` longtext,
  `attachment` tinyint,
  `created_at` datetime(3) COMMENT '创建时间',
  `updated_at` datetime(3) COMMENT '更新时间',
  `deleted_at` datetime(3) COMMENT '删除时间',
  PRIMARY KEY (`id`),
  KEY `idx_ext_corp_id` (`ext_corp_id`),
  KEY `idx_ext_creator_id` (`ext_creator_id`),
  KEY `idx_created_at` (`created_at`),
  KEY `idx_updated_at` (`updated_at`),
  KEY `idx_deleted_at` (`deleted_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='入群欢迎语';

DROP TABLE IF EXISTS `group_chat_mass_msg`;
CREATE TABLE `group_chat_mass_msg` (
  `id` bigint NOT NULL COMMENT 'ID',
  `ext_corp_id` char(18) COMMENT '外部企业ID',
  `ext_creator_id` char(64) COMMENT '创建者外部员工ID',
  `send_type` tinyint unsigned COMMENT '1-立即发送,2-定时发送',
  `ext_staff_ids` JSON,
  `msg` json COMMENT '消息内容',
  `ext_msg_id` varchar(33) COMMENT '微信消息ID',
  `mission_status` tinyint unsigned COMMENT '创建企业群发消息的状态,1-预约发送,2-发送中,3-发送成功,4-发送失败,5-已取消',
  `delivered_num` int unsigned COMMENT '已发送群主计数',
  `success_num` int unsigned COMMENT '已送达群聊数',
  `un_delivered_num` int unsigned COMMENT '未发送群主计数',
  `failed_num` int COMMENT '未送达群聊计数',
  `send_at` tinyint,
  `created_at` datetime(3) COMMENT '创建时间',
  `updated_at` datetime(3) COMMENT '更新时间',
  `deleted_at` datetime(3) COMMENT '删除时间',
  PRIMARY KEY (`id`),
  KEY `idx_ext_corp_id` (`ext_corp_id`),
  KEY `idx_ext_creator_id` (`ext_creator_id`),
  KEY `idx_ext_msg_id` (`ext_msg_id`),
  KEY `idx_created_at` (`created_at`),
  KEY `idx_updated_at` (`updated_at`),
  KEY `idx_deleted_at` (`deleted_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='客户群群发消息内容 消息内容不可修改';

DROP TABLE IF EXISTS `customer_statistic`;
CREATE TABLE `customer_statistic` (
  `id` bigint NOT NULL COMMENT 'ID',
  `ext_corp_id` char(18) COMMENT '外部企业ID',
  `ext_creator_id` char(64) COMMENT '创建者外部员工ID',
  `ext_staff_id` char(48) COMMENT '外部员工ID',
  `total_customer_num` bigint unsigned COMMENT '客户总数',
  `increase_customer_num` bigint unsigned COMMENT '新增客户总数',
  `decrease_customer_num` bigint unsigned COMMENT '流失客户总数',
  `date` date COMMENT '日期',
  `created_at` datetime(3) COMMENT '创建时间',
  `updated_at` datetime(3) COMMENT '更新时间',
  `deleted_at` datetime(3) COMMENT '删除时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `ext_staff_id_date` (`ext_staff_id`, `date`),
  KEY `idx_ext_corp_id` (`ext_corp_id`),
  KEY `idx_ext_creator_id` (`ext_creator_id`),
  KEY `idx_created_at` (`created_at`),
  KEY `idx_updated_at` (`updated_at`),
  KEY `idx_deleted_at` (`deleted_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='按天统计客户数量 unique_index: ext_staff_id - date';

DROP TABLE IF EXISTS `data_export`;
CREATE TABLE `data_export` (
  `id` bigint NOT NULL COMMENT 'ID',
  `ext_corp_id` char(18) COMMENT '外部企业ID',
  `ext_creator_id` char(64) COMMENT '创建者外部员工ID',
  `export_time` longtext,
  `status` longtext,
  `url` longtext,
  `type` longtext,
  `created_at` datetime(3) COMMENT '创建时间',
  `updated_at` datetime(3) COMMENT '更新时间',
  `deleted_at` datetime(3) COMMENT '删除时间',
  PRIMARY KEY (`id`),
  KEY `idx_ext_corp_id` (`ext_corp_id`),
  KEY `idx_ext_creator_id` (`ext_creator_id`),
  KEY `idx_created_at` (`created_at`),
  KEY `idx_updated_at` (`updated_at`),
  KEY `idx_deleted_at` (`deleted_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='数据导出任务';

DROP TABLE IF EXISTS `contact_way_group`;
CREATE TABLE `contact_way_group` (
  `id` bigint NOT NULL COMMENT 'ID',
  `ext_corp_id` char(18) COMMENT '外部企业ID',
  `ext_creator_id` char(64) COMMENT '创建者外部员工ID',
  `name` varchar(191) COMMENT '分组名称',
  `sort_weight` bigint DEFAULT 1000 COMMENT '分组排序权重',
  `count` bigint COMMENT '该分组渠道码数量',
  `is_default` tinyint DEFAULT 2 COMMENT '是否为默认分组，1：是；2：否',
  `created_at` datetime(3) COMMENT '创建时间',
  `updated_at` datetime(3) COMMENT '更新时间',
  `deleted_at` datetime(3) COMMENT '删除时间',
  PRIMARY KEY (`id`),
  KEY `idx_ext_corp_id` (`ext_corp_id`),
  KEY `idx_ext_creator_id` (`ext_creator_id`),
  KEY `idx_name` (`name`),
  KEY `idx_sort_weight` (`sort_weight`),
  KEY `idx_created_at` (`created_at`),
  KEY `idx_updated_at` (`updated_at`),
  KEY `idx_deleted_at` (`deleted_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='渠道码分组';

DROP TABLE IF EXISTS `contact_way`;
CREATE TABLE `contact_way` (
  `id` bigint NOT NULL COMMENT 'ID',
  `ext_corp_id` char(18) COMMENT '外部企业ID',
  `ext_creator_id` char(64) COMMENT '创建者外部员工ID',
  `name` varchar(255) COMMENT '渠道码名称',
  `config_id` varchar(191) COMMENT '渠道码配置ID',
  `group_id` bigint COMMENT '活码分组ID',
  `qr_code` longtext COMMENT '联系二维码的URL',
  `remark` longtext COMMENT '渠道码的备注信息',
  `skip_verify` tinyint DEFAULT 1 COMMENT '外部客户添加时是否无需验证，假布尔类型',
  `state` longtext COMMENT '企业自定义的state参数',
  `add_customer_count` bigint DEFAULT 0 COMMENT '扫码添加人次',
  `auto_reply_type` tinyint DEFAULT 1 COMMENT '欢迎语类型：1，渠道欢迎语；2, 渠道默认欢迎语；3，不送欢迎语；',
  `auto_reply` json COMMENT '欢迎语策略',
  `customer_desc` longtext COMMENT '客户描述',
  `customer_desc_enable` tinyint COMMENT '是否开启客户描述',
  `customer_remark` longtext COMMENT '客户备注',
  `customer_remark_enable` tinyint COMMENT '是否开启客户备注',
  `daily_add_customer_limit_enable` tinyint COMMENT '是否开启员工每日添加上限',
  `daily_add_customer_limit` bigint COMMENT '员工每日添加上限',
  `schedule_enable` tinyint COMMENT '是否开启工作日调度',
  `staff_control_enable` tinyint COMMENT '是否开启员工自行上下线',
  `auto_tag_enable` tinyint COMMENT '是否自动打标签',
  `customer_tag_ext_ids` json COMMENT '自动打标签绑定的标签ExtID数组',
  `auto_skip_verify_enable` tinyint DEFAULT 1 COMMENT '是否开启自动通过好友时段控制',
  `skip_verify_start_time` bigint COMMENT '自动通过好友开启时刻',
  `skip_verify_end_time` bigint COMMENT '自动通过好友结束时刻',
  `ext_staff_ids` json COMMENT '实时关联的外部员工ID',
  `nickname_block_enable` tinyint COMMENT '是否开启客户昵称屏蔽欢迎语',
  `nickname_block_list` json COMMENT '客户昵称屏蔽欢迎语列表',
  `created_at` datetime(3) COMMENT '创建时间',
  `updated_at` datetime(3) COMMENT '更新时间',
  `deleted_at` datetime(3) COMMENT '删除时间',
  PRIMARY KEY (`id`),
  KEY `idx_ext_corp_id` (`ext_corp_id`),
  KEY `idx_ext_creator_id` (`ext_creator_id`),
  KEY `idx_name` (`name`),
  KEY `idx_config_id` (`config_id`),
  KEY `idx_group_id` (`group_id`),
  KEY `idx_add_customer_count` (`add_customer_count`),
  KEY `idx_auto_reply_type` (`auto_reply_type`),
  KEY `idx_created_at` (`created_at`),
  KEY `idx_updated_at` (`updated_at`),
  KEY `idx_deleted_at` (`deleted_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='渠道码';

DROP TABLE IF EXISTS `contact_way_schedule`;
CREATE TABLE `contact_way_schedule` (
  `id` bigint NOT NULL COMMENT 'ID',
  `ext_corp_id` char(18) COMMENT '外部企业ID',
  `ext_creator_id` char(64) COMMENT '创建者外部员工ID',
  `contact_way_id` bigint COMMENT '渠道码ID',
  `daily_add_customer_limit` bigint COMMENT '员工每日添加客户上限',
  `weekdays` json COMMENT '工作日',
  `start_time` bigint COMMENT '开始时间',
  `end_time` bigint COMMENT '结束时间',
  `created_at` datetime(3) COMMENT '创建时间',
  `updated_at` datetime(3) COMMENT '更新时间',
  `deleted_at` datetime(3) COMMENT '删除时间',
  PRIMARY KEY (`id`),
  KEY `idx_ext_corp_id` (`ext_corp_id`),
  KEY `idx_ext_creator_id` (`ext_creator_id`),
  KEY `idx_contact_way_id` (`contact_way_id`),
  KEY `idx_created_at` (`created_at`),
  KEY `idx_updated_at` (`updated_at`),
  KEY `idx_deleted_at` (`deleted_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='渠道码调度设置（根据时间自动上下线员工）';

DROP TABLE IF EXISTS `contact_way_schedule_staff`;
CREATE TABLE `contact_way_schedule_staff` (
  `id` bigint NOT NULL COMMENT 'ID',
  `ext_corp_id` char(18) COMMENT '外部企业ID',
  `ext_creator_id` char(64) COMMENT '创建者外部员工ID',
  `contact_way_id` bigint COMMENT '渠道码ID',
  `contact_way_schedule_id` bigint DEFAULT 0 COMMENT '调度设置ID',
  `add_customer_count` bigint DEFAULT 0 COMMENT '员工累计添加客户计数',
  `daily_add_customer_count` bigint DEFAULT 0 COMMENT '员工每日添加客户计数',
  `daily_add_customer_limit` bigint COMMENT '员工每日添加客户上限',
  `ext_staff_id` varchar(191) COMMENT '外部员工ID',
  `name` varchar(255) COMMENT '员工名',
  `avatar_url` varchar(128) COMMENT '头像地址',
  `online` tinyint DEFAULT 1 COMMENT '员工是否在线',
  `created_at` datetime(3) COMMENT '创建时间',
  `updated_at` datetime(3) COMMENT '更新时间',
  `deleted_at` datetime(3) COMMENT '删除时间',
  PRIMARY KEY (`id`),
  KEY `idx_ext_corp_id` (`ext_corp_id`),
  KEY `idx_ext_creator_id` (`ext_creator_id`),
  KEY `idx_contact_way_id` (`contact_way_id`),
  KEY `ContactWayScheduleIndex` (`contact_way_schedule_id`, `ext_staff_id`),
  KEY `idx_online` (`online`),
  KEY `idx_created_at` (`created_at`),
  KEY `idx_updated_at` (`updated_at`),
  KEY `idx_deleted_at` (`deleted_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='渠道码绑定的员工';

DROP TABLE IF EXISTS `contact_way_backup_staff`;
CREATE TABLE `contact_way_backup_staff` (
  `id` bigint NOT NULL COMMENT 'ID',
  `ext_corp_id` char(18) COMMENT '外部企业ID',
  `ext_creator_id` char(64) COMMENT '创建者外部员工ID',
  `contact_way_id` bigint COMMENT '渠道码ID',
  `add_customer_count` bigint DEFAULT 0 COMMENT '员工累计添加客户计数',
  `daily_add_customer_count` bigint DEFAULT 0 COMMENT '员工每日添加客户计数',
  `daily_add_customer_limit` bigint COMMENT '员工每日添加客户上限',
  `ext_staff_id` varchar(191) COMMENT '外部员工ID',
  `name` varchar(255) COMMENT '员工名',
  `avatar_url` varchar(128) COMMENT '头像地址',
  `online` tinyint DEFAULT 1 COMMENT '员工是否在线',
  `created_at` datetime(3) COMMENT '创建时间',
  `updated_at` datetime(3) COMMENT '更新时间',
  `deleted_at` datetime(3) COMMENT '删除时间',
  PRIMARY KEY (`id`),
  KEY `idx_ext_corp_id` (`ext_corp_id`),
  KEY `idx_ext_creator_id` (`ext_creator_id`),
  KEY `contactWayIndex` (`contact_way_id`, `ext_staff_id`),
  KEY `idx_online` (`online`),
  KEY `idx_created_at` (`created_at`),
  KEY `idx_updated_at` (`updated_at`),
  KEY `idx_deleted_at` (`deleted_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='渠道码绑定的备份员工';

DROP TABLE IF EXISTS `contact_way_staff`;
CREATE TABLE `contact_way_staff` (
  `id` bigint NOT NULL COMMENT 'ID',
  `ext_corp_id` char(18) COMMENT '外部企业ID',
  `ext_creator_id` char(64) COMMENT '创建者外部员工ID',
  `contact_way_id` bigint COMMENT '渠道码ID',
  `add_customer_count` bigint DEFAULT 0 COMMENT '员工累计添加客户计数',
  `daily_add_customer_count` bigint DEFAULT 0 COMMENT '员工每日添加客户计数',
  `daily_add_customer_limit` bigint COMMENT '员工每日添加客户上限',
  `ext_staff_id` varchar(191) COMMENT '外部员工ID',
  `name` varchar(255) COMMENT '员工名',
  `avatar_url` varchar(128) COMMENT '头像地址',
  `online` tinyint DEFAULT 1 COMMENT '员工是否在线',
  `created_at` datetime(3) COMMENT '创建时间',
  `updated_at` datetime(3) COMMENT '更新时间',
  `deleted_at` datetime(3) COMMENT '删除时间',
  PRIMARY KEY (`id`),
  KEY `idx_ext_corp_id` (`ext_corp_id`),
  KEY `idx_ext_creator_id` (`ext_creator_id`),
  KEY `contactWayIndex` (`contact_way_id`, `ext_staff_id`),
  KEY `idx_online` (`online`),
  KEY `idx_created_at` (`created_at`),
  KEY `idx_updated_at` (`updated_at`),
  KEY `idx_deleted_at` (`deleted_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='渠道码绑定的员工';

DROP TABLE IF EXISTS `permission`;
CREATE TABLE `permission` (
  `id` bigint AUTO_INCREMENT NOT NULL COMMENT 'ID',
  `name` varchar(191) COMMENT '权限名称',
  `description` longtext COMMENT '权限描述',
  `biz_name` longtext COMMENT '业务名称',
  `biz_identity` varchar(191) COMMENT '业务标识',
  `operation` longtext COMMENT '操作',
  `identity` varchar(191) COMMENT '权限标识',
  `sort_weight` bigint DEFAULT 1000 COMMENT '权限排序权重',
  `created_at` datetime(3) COMMENT '创建时间',
  `updated_at` datetime(3) COMMENT '更新时间',
  `deleted_at` datetime(3) COMMENT '删除时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uni_identity` (`identity`),
  KEY `idx_name` (`name`),
  KEY `idx_biz_identity` (`biz_identity`),
  KEY `idx_sort_weight` (`sort_weight`),
  KEY `idx_created_at` (`created_at`),
  KEY `idx_updated_at` (`updated_at`),
  KEY `idx_deleted_at` (`deleted_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='权限';

DROP TABLE IF EXISTS `role`;
CREATE TABLE `role` (
  `id` bigint NOT NULL COMMENT 'ID',
  `ext_corp_id` char(18) COMMENT '外部企业ID',
  `ext_creator_id` char(64) COMMENT '创建者外部员工ID',
  `name` varchar(191) COMMENT '角色名称',
  `description` longtext COMMENT '角色描述',
  `type` varchar(191) DEFAULT 'Staff' COMMENT '角色类型',
  `sort_weight` bigint DEFAULT 1000 COMMENT '角色排序权重',
  `is_default` tinyint DEFAULT 2 COMMENT '是否为默认角色，1：是；2：否',
  `permission_ids` json COMMENT '角色绑定的权限标识数组',
  `created_at` datetime(3) COMMENT '创建时间',
  `updated_at` datetime(3) COMMENT '更新时间',
  `deleted_at` datetime(3) COMMENT '删除时间',
  PRIMARY KEY (`id`),
  KEY `idx_ext_corp_id` (`ext_corp_id`),
  KEY `idx_ext_creator_id` (`ext_creator_id`),
  KEY `idx_name` (`name`),
  KEY `idx_type` (`type`),
  KEY `idx_sort_weight` (`sort_weight`),
  KEY `idx_created_at` (`created_at`),
  KEY `idx_updated_at` (`updated_at`),
  KEY `idx_deleted_at` (`deleted_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色';
