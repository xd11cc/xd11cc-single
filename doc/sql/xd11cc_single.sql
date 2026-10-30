/*
 xd11cc-single 数据库结构脚本
 ------------------------------------------------------------
 库名   : xd11cc_single
 内容   : 仅表结构（DROP + CREATE + 索引 + 注释）
 数据   : 见同目录 xd11cc_single_data.sql（种子/基础数据）
 适用   : MySQL 8.0
 生成   : doc/sql/sync.sh —— 请勿手工编辑，改库后重跑脚本

 说明   :
   1. 本文件不含任何 INSERT，可安全用于重建空库
   2. 建表模板见同目录 template.sql
   3. 已剔除 AUTO_INCREMENT 计数值，避免数据变动导致结构文件漂移
*/
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for auth_client_config
-- ----------------------------
DROP TABLE IF EXISTS `auth_client_config`;
CREATE TABLE `auth_client_config` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `source` varchar(32) NOT NULL COMMENT '应用类型',
  `client_id` varchar(255) NOT NULL COMMENT '应用id',
  `client_secret` varchar(255) NOT NULL COMMENT '应用密钥',
  `redirect_uri` varchar(255) DEFAULT NULL COMMENT '重定向地址',
  `name` varchar(255) DEFAULT NULL COMMENT '应用名称',
  `icon` varchar(255) NOT NULL COMMENT '图标',
  `status` char(2) NOT NULL DEFAULT '0' COMMENT '状态，字典类型system_status',
  `sort` int NOT NULL COMMENT '排序',
  `create_user_id` bigint NOT NULL COMMENT '创建人id',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_user_id` bigint NOT NULL COMMENT '更新人id',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `del_flag` tinyint DEFAULT '0' COMMENT '删除标识 0-未删除 null-已删除',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户id',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_source` (`tenant_id`,`source`,`del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='授权应用配置表';

-- ----------------------------
-- Table structure for auth_social_user
-- ----------------------------
DROP TABLE IF EXISTS `auth_social_user`;
CREATE TABLE `auth_social_user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `uuid` varchar(100) NOT NULL COMMENT '第三方系统唯一id',
  `user_id` bigint DEFAULT NULL COMMENT '用户id',
  `source` varchar(32) NOT NULL COMMENT '应用类型',
  `open_id` varchar(64) DEFAULT NULL COMMENT '社交openId',
  `token` varchar(1000) NOT NULL COMMENT '社交token（生产环境建议加密存储）',
  `row_token_info` text NOT NULL COMMENT '社交token原始信息',
  `nickname` varchar(32) DEFAULT NULL COMMENT '社交昵称',
  `avatar` varchar(255) NOT NULL COMMENT '社交头像',
  `row_user_info` text NOT NULL COMMENT '社交用户原始信息',
  `code` varchar(32) NOT NULL COMMENT '最后一次认证code',
  `state` varchar(32) NOT NULL COMMENT '最后一次认证state',
  `bind_time` datetime NOT NULL COMMENT '绑定时间',
  `del_flag` tinyint DEFAULT '0' COMMENT '删除标识 0-未删除 null-已删除',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户id',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_source_uuid` (`tenant_id`,`source`,`uuid`,`del_flag`),
  KEY `idx_tenant_user` (`tenant_id`,`user_id`,`del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='社交用户表';

-- ----------------------------
-- Table structure for system_config
-- ----------------------------
DROP TABLE IF EXISTS `system_config`;
CREATE TABLE `system_config` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `config_key` varchar(100) NOT NULL COMMENT '配置键',
  `config_value` text NOT NULL COMMENT '配置值',
  `config_name` varchar(32) NOT NULL COMMENT '配置名称',
  `create_user_id` bigint NOT NULL COMMENT '创建人id',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_user_id` bigint NOT NULL COMMENT '更新人id',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `del_flag` tinyint DEFAULT '0' COMMENT '删除标识 0-未删除 null-已删除',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户id',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_config_key` (`tenant_id`,`config_key`,`del_flag`) COMMENT '同一租户下 key 唯一'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统配置';

-- ----------------------------
-- Table structure for system_dept
-- ----------------------------
DROP TABLE IF EXISTS `system_dept`;
CREATE TABLE `system_dept` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `parent_id` bigint DEFAULT NULL COMMENT '父部门id',
  `dept_code` varchar(50) NOT NULL COMMENT '部门编码',
  `dept_name` varchar(50) NOT NULL COMMENT '部门名称',
  `leader_id` bigint NOT NULL COMMENT '部门负责人',
  `sort` int NOT NULL DEFAULT '0' COMMENT '排序',
  `status` char(2) NOT NULL COMMENT '部门状态，字典类型system_status',
  `create_user_id` bigint NOT NULL COMMENT '创建人id',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_user_id` bigint NOT NULL COMMENT '更新人id',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `del_flag` tinyint DEFAULT '0' COMMENT '删除标识 0-未删除 null-已删除',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户id',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_tenant_dept_code` (`tenant_id`,`dept_code`,`del_flag`),
  KEY `idx_tenant_parent` (`tenant_id`,`parent_id`,`del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='部门表';

-- ----------------------------
-- Table structure for system_dept_post
-- ----------------------------
DROP TABLE IF EXISTS `system_dept_post`;
CREATE TABLE `system_dept_post` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `dept_id` bigint NOT NULL COMMENT '部门id',
  `post_id` bigint NOT NULL COMMENT '岗位id',
  `create_user_id` bigint NOT NULL COMMENT '创建人id',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_user_id` bigint NOT NULL COMMENT '更新人id',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `del_flag` tinyint DEFAULT '0' COMMENT '删除标识 0-未删除 null-已删除',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户id',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_dept_post` (`tenant_id`,`dept_id`,`post_id`,`del_flag`),
  KEY `idx_tenant_post` (`tenant_id`,`post_id`,`del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='部门岗位表';

-- ----------------------------
-- Table structure for system_dict_data
-- ----------------------------
DROP TABLE IF EXISTS `system_dict_data`;
CREATE TABLE `system_dict_data` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `dict_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '字典类型',
  `label` varchar(100) NOT NULL COMMENT '展示文案',
  `value` varchar(100) NOT NULL COMMENT '编码，与业务列取值对应',
  `css_class` varchar(100) DEFAULT NULL COMMENT '键值颜色',
  `list_class` varchar(100) DEFAULT NULL COMMENT '背景颜色',
  `sort` int NOT NULL COMMENT '排序',
  `status` char(2) DEFAULT NULL COMMENT '状态，字典类型system_status',
  `create_user_id` bigint NOT NULL COMMENT '创建人id',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_user_id` bigint NOT NULL COMMENT '更新人id',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `del_flag` tinyint DEFAULT '0' COMMENT '删除标识 0-未删除 null-已删除',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户id',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_type_label` (`tenant_id`,`dict_type`,`label`,`del_flag`) USING BTREE,
  KEY `idx_tenant_type_status_sort` (`tenant_id`,`dict_type`,`status`,`sort`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='字典数据表';

-- ----------------------------
-- Table structure for system_dict_type
-- ----------------------------
DROP TABLE IF EXISTS `system_dict_type`;
CREATE TABLE `system_dict_type` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `dict_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '字典类型',
  `dict_name` varchar(20) NOT NULL COMMENT '字典名称',
  `create_user_id` bigint NOT NULL COMMENT '创建人id',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_user_id` bigint NOT NULL COMMENT '更新人id',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `del_flag` tinyint DEFAULT '0' COMMENT '删除标识 0-未删除 null-已删除',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_dict_type` (`dict_type`,`del_flag`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='字典类型表';

-- ----------------------------
-- Table structure for system_login_log
-- ----------------------------
DROP TABLE IF EXISTS `system_login_log`;
CREATE TABLE `system_login_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `username` varchar(50) NOT NULL COMMENT '登录账号',
  `login_type` char(2) NOT NULL COMMENT '登录类型 1-密码登录 2-社交登录 3-退出登录',
  `status` char(2) NOT NULL DEFAULT '0' COMMENT '登录状态 0-成功 1-失败',
  `login_ip` varchar(50) DEFAULT NULL COMMENT '登录IP',
  `browser` varchar(50) DEFAULT NULL COMMENT '浏览器',
  `os` varchar(50) DEFAULT NULL COMMENT '操作系统',
  `msg` varchar(500) DEFAULT NULL COMMENT '提示消息',
  `login_time` datetime NOT NULL COMMENT '登录时间',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户id',
  PRIMARY KEY (`id`),
  KEY `idx_login_time` (`login_time`,`id`),
  KEY `idx_login_type_status_time` (`login_type`,`status`,`login_time`,`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='登录日志表';

-- ----------------------------
-- Table structure for system_menu
-- ----------------------------
DROP TABLE IF EXISTS `system_menu`;
CREATE TABLE `system_menu` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `parent_id` bigint DEFAULT NULL COMMENT '父菜单id',
  `menu_name` varchar(50) NOT NULL COMMENT '菜单名称',
  `sort` int NOT NULL DEFAULT '0' COMMENT '显示排序',
  `path` varchar(255) DEFAULT NULL COMMENT '路由路径（如/user/list）',
  `component` varchar(255) DEFAULT NULL COMMENT '组件路径',
  `route_name` varchar(255) DEFAULT NULL COMMENT '路由名称',
  `query` varchar(255) DEFAULT NULL COMMENT '路由参数',
  `icon` varchar(255) DEFAULT NULL COMMENT '菜单图标',
  `menu_type` char(2) NOT NULL COMMENT '菜单类型，字典类型system_menu_type',
  `status` char(2) NOT NULL COMMENT '菜单状态，字典类型system_status',
  `permission` varchar(255) DEFAULT NULL COMMENT '权限字符',
  `visible` char(2) NOT NULL DEFAULT '0' COMMENT '是否隐藏，字典类型system_visible（0-显示 1-隐藏）',
  `create_user_id` bigint NOT NULL COMMENT '创建人id',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_user_id` bigint NOT NULL COMMENT '更新人id',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `del_flag` tinyint DEFAULT '0' COMMENT '删除标识 0-未删除 null-已删除',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_path` (`path`,`del_flag`) USING BTREE,
  KEY `idx_menu_type_status_sort` (`menu_type`,`status`,`del_flag`,`sort`),
  KEY `idx_parent_del_sort` (`parent_id`,`del_flag`,`sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='菜单表';

-- ----------------------------
-- Table structure for system_notice
-- ----------------------------
DROP TABLE IF EXISTS `system_notice`;
CREATE TABLE `system_notice` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `title` varchar(100) NOT NULL COMMENT '标题',
  `content` text COMMENT '内容',
  `type` char(2) NOT NULL COMMENT '类型，字典类型system_notice_type（1-通知 2-消息 3-待办）',
  `scope` char(2) NOT NULL DEFAULT '1' COMMENT '范围，字典类型system_notice_scope（1-全部 2-指定部门 3-指定用户）',
  `scope_dept_ids` varchar(1024) DEFAULT NULL COMMENT '范围部门id列表，逗号分隔',
  `scope_user_ids` varchar(1024) DEFAULT NULL COMMENT '范围用户id列表，逗号分隔',
  `sender_id` bigint NOT NULL COMMENT '发送人id',
  `sender_name` varchar(32) DEFAULT NULL COMMENT '发送人姓名',
  `status` char(2) NOT NULL DEFAULT '0' COMMENT '状态，字典类型system_notice_status（0-草稿 1-已发布 2-已撤回）',
  `publish_time` datetime DEFAULT NULL COMMENT '发布时间',
  `create_user_id` bigint NOT NULL COMMENT '创建人id',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_user_id` bigint NOT NULL COMMENT '更新人id',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `del_flag` tinyint DEFAULT '0' COMMENT '删除标识 0-未删除 null-已删除',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户id',
  PRIMARY KEY (`id`),
  KEY `idx_tenant_type_status_id` (`tenant_id`,`type`,`status`,`id`),
  KEY `idx_tenant_sender` (`tenant_id`,`sender_id`,`del_flag`),
  KEY `idx_tenant_create_time` (`tenant_id`,`create_time`,`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统通知表';

-- ----------------------------
-- Table structure for system_notice_user
-- ----------------------------
DROP TABLE IF EXISTS `system_notice_user`;
CREATE TABLE `system_notice_user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `notice_id` bigint NOT NULL COMMENT '通知id',
  `user_id` bigint NOT NULL COMMENT '用户id',
  `read_status` char(2) NOT NULL DEFAULT '0' COMMENT '已读状态，字典类型system_read_status（0-未读 1-已读）',
  `read_time` datetime DEFAULT NULL COMMENT '已读时间',
  `create_user_id` bigint NOT NULL COMMENT '创建人id',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_user_id` bigint NOT NULL COMMENT '更新人id',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `del_flag` tinyint DEFAULT '0' COMMENT '删除标识 0-未删除 null-已删除',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户id',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_notice_user` (`tenant_id`,`notice_id`,`user_id`,`del_flag`),
  KEY `idx_tenant_user_read` (`tenant_id`,`user_id`,`del_flag`,`read_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统通知用户关联表';

-- ----------------------------
-- Table structure for system_operate_log
-- ----------------------------
DROP TABLE IF EXISTS `system_operate_log`;
CREATE TABLE `system_operate_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `module` varchar(50) NOT NULL COMMENT '操作模块',
  `operate_type` char(2) NOT NULL COMMENT '操作类型 1-新增 2-修改 3-删除 4-导出 5-其他',
  `operate_desc` varchar(100) DEFAULT NULL COMMENT '操作描述',
  `method` varchar(200) NOT NULL COMMENT '请求方法（类名.方法名）',
  `request_method` varchar(10) NOT NULL COMMENT '请求方式（GET/POST）',
  `request_url` varchar(255) NOT NULL COMMENT '请求URL',
  `request_param` text COMMENT '请求参数',
  `response_result` text COMMENT '响应结果',
  `status` char(2) NOT NULL DEFAULT '0' COMMENT '操作状态 0-成功 1-失败',
  `error_msg` varchar(2000) DEFAULT NULL COMMENT '错误信息',
  `operate_ip` varchar(50) DEFAULT NULL COMMENT '操作IP',
  `cost_time` bigint DEFAULT NULL COMMENT '耗时（毫秒）',
  `create_user_id` bigint NOT NULL COMMENT '操作人id',
  `create_time` datetime NOT NULL COMMENT '操作时间',
  `update_user_id` bigint NOT NULL COMMENT '更新人id',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `del_flag` tinyint DEFAULT '0' COMMENT '删除标识 0-未删除 null-已删除',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户id',
  PRIMARY KEY (`id`),
  KEY `idx_tenant_create_time` (`tenant_id`,`create_time`,`id`),
  KEY `idx_tenant_module_type_status` (`tenant_id`,`module`,`operate_type`,`status`),
  KEY `idx_tenant_operator` (`tenant_id`,`create_user_id`,`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='操作日志表';

-- ----------------------------
-- Table structure for system_post
-- ----------------------------
DROP TABLE IF EXISTS `system_post`;
CREATE TABLE `system_post` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `post_code` varchar(20) NOT NULL COMMENT '岗位编码',
  `post_name` varchar(20) NOT NULL COMMENT '岗位名称',
  `status` char(2) NOT NULL COMMENT '岗位状态，字典类型system_status',
  `create_user_id` bigint NOT NULL COMMENT '创建人id',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_user_id` bigint NOT NULL COMMENT '更新人id',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `del_flag` tinyint DEFAULT '0' COMMENT '删除标识 0-未删除 null-已删除',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户id',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_post_code` (`tenant_id`,`post_code`,`del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='岗位表';

-- ----------------------------
-- Table structure for system_role
-- ----------------------------
DROP TABLE IF EXISTS `system_role`;
CREATE TABLE `system_role` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `role_code` varchar(20) NOT NULL COMMENT '角色编码',
  `role_name` varchar(20) NOT NULL COMMENT '角色名称',
  `data_scope` char(2) NOT NULL DEFAULT '1' COMMENT '数据权限',
  `status` char(2) NOT NULL COMMENT '角色状态，字典类型system_status',
  `create_user_id` bigint NOT NULL COMMENT '创建人id',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_user_id` bigint NOT NULL COMMENT '更新人id',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `del_flag` tinyint DEFAULT '0' COMMENT '删除标识 0-未删除 null-已删除',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户id',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_role_code` (`tenant_id`,`role_code`,`del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色表';

-- ----------------------------
-- Table structure for system_role_dept
-- ----------------------------
DROP TABLE IF EXISTS `system_role_dept`;
CREATE TABLE `system_role_dept` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `role_id` bigint NOT NULL COMMENT '角色id',
  `dept_id` bigint NOT NULL COMMENT '部门id',
  `create_user_id` bigint NOT NULL COMMENT '创建人id',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_user_id` bigint NOT NULL COMMENT '更新人id',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `del_flag` tinyint DEFAULT '0' COMMENT '删除标识 0-未删除 null-已删除',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户id',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_role_dept` (`tenant_id`,`role_id`,`dept_id`,`del_flag`),
  KEY `idx_tenant_dept` (`tenant_id`,`dept_id`,`del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色-部门关联表（自定义数据权限）';

-- ----------------------------
-- Table structure for system_role_menu
-- ----------------------------
DROP TABLE IF EXISTS `system_role_menu`;
CREATE TABLE `system_role_menu` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `role_id` bigint NOT NULL COMMENT '角色id',
  `menu_id` bigint NOT NULL COMMENT '菜单id',
  `create_user_id` bigint NOT NULL COMMENT '创建人id',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_user_id` bigint NOT NULL COMMENT '更新人id',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `del_flag` tinyint DEFAULT '0' COMMENT '删除标识 0-未删除 null-已删除',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户id',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_role_menu` (`tenant_id`,`role_id`,`menu_id`,`del_flag`),
  KEY `idx_tenant_menu` (`tenant_id`,`menu_id`,`del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色菜单表';

-- ----------------------------
-- Table structure for system_tenant
-- ----------------------------
DROP TABLE IF EXISTS `system_tenant`;
CREATE TABLE `system_tenant` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '租户id',
  `name` varchar(50) NOT NULL COMMENT '租户名称',
  `domain` varchar(100) NOT NULL COMMENT '绑定域名',
  `contact_name` varchar(50) DEFAULT NULL COMMENT '联系人',
  `contact_phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
  `account_count` int NOT NULL DEFAULT '0' COMMENT '账号额度（0=不限）',
  `status` char(2) NOT NULL DEFAULT '0' COMMENT '状态 0-正常 1-停用',
  `expire_time` datetime NOT NULL COMMENT '过期时间',
  `create_user_id` bigint NOT NULL COMMENT '创建人id',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_user_id` bigint NOT NULL COMMENT '更新人id',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `del_flag` tinyint DEFAULT '0' COMMENT '删除标识 0-未删除 null-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_domain` (`domain`,`del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='租户表';

-- ----------------------------
-- Table structure for system_user
-- ----------------------------
DROP TABLE IF EXISTS `system_user`;
CREATE TABLE `system_user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `username` varchar(30) NOT NULL COMMENT '用户名',
  `password` varchar(64) NOT NULL COMMENT '密码',
  `nickname` varchar(30) NOT NULL COMMENT '昵称',
  `id_card` varchar(18) DEFAULT NULL COMMENT '身份证号码',
  `sex` char(2) NOT NULL COMMENT '性别，字典类型system_user_sex',
  `phone` varchar(11) DEFAULT NULL COMMENT '手机号码',
  `email` varchar(50) DEFAULT NULL COMMENT '邮箱',
  `dept_id` bigint NOT NULL COMMENT '部门id',
  `dept_name` varchar(50) NOT NULL COMMENT '部门名称',
  `post_id` bigint NOT NULL COMMENT '岗位id',
  `post_name` varchar(50) NOT NULL COMMENT '岗位名称',
  `status` char(2) NOT NULL COMMENT '账号状态，字典类型system_status',
  `head_url` varchar(255) DEFAULT NULL COMMENT '头像路径',
  `create_user_id` bigint NOT NULL COMMENT '创建人id',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_user_id` bigint NOT NULL COMMENT '更新人id',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `del_flag` tinyint DEFAULT '0' COMMENT '删除标识 0-未删除 null-已删除',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户id',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_username` (`tenant_id`,`username`,`del_flag`),
  KEY `idx_tenant_email` (`tenant_id`,`email`,`del_flag`),
  KEY `idx_tenant_dept_status` (`tenant_id`,`dept_id`,`status`,`del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户表';

-- ----------------------------
-- Table structure for system_user_role
-- ----------------------------
DROP TABLE IF EXISTS `system_user_role`;
CREATE TABLE `system_user_role` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `user_id` bigint NOT NULL COMMENT '用户id',
  `role_id` bigint NOT NULL COMMENT '角色id',
  `create_user_id` bigint NOT NULL COMMENT '创建人id',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_user_id` bigint NOT NULL COMMENT '更新人id',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `del_flag` tinyint DEFAULT '0' COMMENT '删除标识 0-未删除 null-已删除',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户id',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_user_role` (`tenant_id`,`user_id`,`role_id`,`del_flag`),
  KEY `idx_tenant_role` (`tenant_id`,`role_id`,`del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户角色表';

-- ----------------------------
-- Table structure for mq_fail_message
-- ----------------------------
DROP TABLE IF EXISTS `mq_fail_message`;
CREATE TABLE `mq_fail_message` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `message_id` varchar(64) NOT NULL COMMENT '消息唯一标识（发送端生成）',
  `queue_name` varchar(128) NOT NULL COMMENT '来源队列名',
  `data` text COMMENT '原始业务消息JSON',
  `message_type` varchar(255) DEFAULT NULL COMMENT '消息体全限定类名（Jackson __TypeId__），重放时用于还原类型',
  `error_msg` varchar(2000) DEFAULT NULL COMMENT '最后一次异常摘要',
  `retry_count` int NOT NULL DEFAULT '0' COMMENT '首次失败时的实际重试次数',
  `status` char(2) NOT NULL DEFAULT '0' COMMENT '状态，字典类型mq_fail_status（0-待处理 1-已重放 2-已忽略）',
  `create_user_id` bigint NOT NULL COMMENT '创建人id',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_user_id` bigint NOT NULL COMMENT '更新人id',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `del_flag` tinyint DEFAULT '0' COMMENT '删除标识：0-未删除，NULL-已删除',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户id',
  PRIMARY KEY (`id`),
  KEY `idx_tenant_message` (`tenant_id`,`message_id`,`del_flag`),
  KEY `idx_tenant_status_time` (`tenant_id`,`status`,`create_time`,`id`,`del_flag`),
  KEY `idx_queue_time` (`queue_name`,`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='MQ消费失败消息表';

-- ----------------------------
-- Table structure for workflow_category
-- ----------------------------
DROP TABLE IF EXISTS `workflow_category`;
CREATE TABLE `workflow_category` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `category_code` varchar(64) NOT NULL COMMENT '分类编码，租户内唯一，供流程模型引用',
  `category_name` varchar(100) NOT NULL COMMENT '分类名称',
  `sort` int NOT NULL DEFAULT '0' COMMENT '显示排序，值越小越靠前',
  `status` char(2) NOT NULL DEFAULT '0' COMMENT '状态，字典类型system_status（0-正常 1-停用）',
  `create_user_id` bigint NOT NULL COMMENT '创建人id',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_user_id` bigint NOT NULL COMMENT '更新人id',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `del_flag` tinyint DEFAULT '0' COMMENT '删除标识：0未删除，NULL已删除',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户id',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_category_code` (`tenant_id`,`category_code`,`del_flag`),
  KEY `idx_tenant_category_list` (`tenant_id`,`status`,`sort`,`id`,`del_flag`),
  KEY `idx_tenant_category_name` (`tenant_id`,`category_name`,`del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Flowable流程分类';

-- ----------------------------
-- Table structure for workflow_expression_config
-- ----------------------------
DROP TABLE IF EXISTS `workflow_expression_config`;
CREATE TABLE `workflow_expression_config` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `config_key` varchar(128) NOT NULL COMMENT '配置键',
  `config_name` varchar(100) NOT NULL COMMENT '配置名称',
  `expression_type` varchar(64) NOT NULL COMMENT '表达式使用场景',
  `expression_template` text NOT NULL COMMENT '完整Flowable表达式模板',
  `parameter_schema` text NOT NULL COMMENT '参数定义JSON数组',
  `return_type` varchar(32) NOT NULL COMMENT '返回类型',
  `status` char(2) NOT NULL DEFAULT '0' COMMENT '状态，字典类型system_status（0-正常 1-停用）',
  `create_user_id` bigint NOT NULL,
  `create_time` datetime NOT NULL,
  `update_user_id` bigint NOT NULL,
  `update_time` datetime NOT NULL,
  `del_flag` tinyint DEFAULT '0' COMMENT '删除标识 0-未删除 null-已删除',
  `remark` varchar(255) DEFAULT NULL,
  `tenant_id` bigint DEFAULT NULL COMMENT '租户id',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_config_key` (`tenant_id`,`config_key`,`del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Flowable表达式配置';

-- ----------------------------
-- Table structure for workflow_instance
-- ----------------------------
DROP TABLE IF EXISTS `workflow_instance`;
CREATE TABLE `workflow_instance` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `process_definition_key` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '流程定义Key',
  `process_definition_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'Flowable流程定义ID',
  `process_instance_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'Flowable流程实例ID',
  `business_key` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '业务标识，格式建议为业务类型:业务ID',
  `title` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '流程标题',
  `status` char(2) NOT NULL DEFAULT '0' COMMENT '状态，字典类型workflow_status（0-进行中 1-已完成 2-已取消 3-已驳回）',
  `starter_user_id` bigint NOT NULL COMMENT '发起人ID',
  `starter_username` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '发起人用户名',
  `variables` json DEFAULT NULL COMMENT '业务流程变量JSON快照',
  `end_time` datetime DEFAULT NULL COMMENT '流程结束时间',
  `create_user_id` bigint DEFAULT NULL COMMENT '创建人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_user_id` bigint DEFAULT NULL COMMENT '更新人ID',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` tinyint DEFAULT '0' COMMENT '删除标识：0-未删除，NULL-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_process_instance` (`process_instance_id`,`del_flag`) USING BTREE,
  KEY `idx_tenant_business` (`tenant_id`,`business_key`,`del_flag`) USING BTREE,
  KEY `idx_tenant_status_time` (`tenant_id`,`status`,`del_flag`,`create_time`,`id`) USING BTREE,
  KEY `idx_tenant_starter_status` (`tenant_id`,`starter_user_id`,`status`,`del_flag`) USING BTREE,
  KEY `idx_process_definition` (`tenant_id`,`process_definition_key`,`del_flag`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='工作流实例业务关联表';

-- ----------------------------
-- Table structure for workflow_task_record
-- ----------------------------
DROP TABLE IF EXISTS `workflow_task_record`;
CREATE TABLE `workflow_task_record` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `process_instance_id` varchar(64) NOT NULL COMMENT 'Flowable流程实例ID',
  `process_definition_key` varchar(128) NOT NULL COMMENT '流程定义Key',
  `process_definition_name` varchar(128) DEFAULT NULL COMMENT '流程定义名称',
  `business_key` varchar(255) DEFAULT NULL COMMENT '业务标识',
  `title` varchar(256) DEFAULT NULL COMMENT '流程标题',
  `task_id` varchar(64) NOT NULL COMMENT 'Flowable任务ID',
  `task_definition_key` varchar(128) DEFAULT NULL COMMENT '任务节点Key',
  `task_name` varchar(128) DEFAULT NULL COMMENT '任务节点名称',
  `assignee_user_id` bigint DEFAULT NULL COMMENT '处理人ID',
  `assignee_username` varchar(64) DEFAULT NULL COMMENT '处理人用户名',
  `assignee_dept_id` bigint DEFAULT NULL COMMENT '处理人部门ID（冗余，便于统计免 JOIN）',
  `action` varchar(32) NOT NULL COMMENT '动作：START/APPROVE/REJECT/DELEGATE/CANCEL/COMPLETE',
  `comment` varchar(1000) DEFAULT NULL COMMENT '审批意见',
  `start_time` datetime NOT NULL COMMENT '任务开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '任务结束时间',
  `duration_ms` bigint DEFAULT NULL COMMENT '处理耗时(毫秒)，预计算',
  `result` varchar(16) DEFAULT NULL COMMENT '结果：APPROVED/REJECTED/DELEGATED',
  `create_user_id` bigint NOT NULL,
  `create_time` datetime NOT NULL,
  `update_user_id` bigint NOT NULL,
  `update_time` datetime NOT NULL,
  `del_flag` tinyint DEFAULT '0' COMMENT '删除标识：0未删除，NULL已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_task_id_action` (`task_id`,`action`,`del_flag`),
  KEY `idx_tenant_assignee_time` (`tenant_id`,`assignee_user_id`,`end_time`),
  KEY `idx_tenant_dept_time` (`tenant_id`,`assignee_dept_id`,`end_time`),
  KEY `idx_tenant_proc_time` (`tenant_id`,`process_definition_key`,`end_time`),
  KEY `idx_tenant_time` (`tenant_id`,`end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Flowable任务记录表（审批流水宽表）';

SET FOREIGN_KEY_CHECKS = 1;
