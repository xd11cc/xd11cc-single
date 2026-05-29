/*
 Navicat Premium Data Transfer

 Source Server         : xd11cc-docker-mysql
 Source Server Type    : MySQL
 Source Server Version : 80042 (8.0.42)
 Source Host           : 127.0.0.1:3306
 Source Schema         : xd11cc_single

 Target Server Type    : MySQL
 Target Server Version : 80042 (8.0.42)
 File Encoding         : 65001

 Date: 28/05/2026 14:24:58
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
  UNIQUE KEY `uk_source_tenant` (`source`,`tenant_id`,`del_flag`),
  KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='授权应用配置表';

-- ----------------------------
-- Records of auth_client_config
-- ----------------------------
BEGIN;
COMMIT;

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
  `token` varchar(255) NOT NULL COMMENT '社交token',
  `row_token_info` varchar(1024) NOT NULL COMMENT '社交token原始信息',
  `nickname` varchar(32) DEFAULT NULL COMMENT '社交昵称',
  `avatar` varchar(255) NOT NULL COMMENT '社交头像',
  `row_user_info` varchar(2048) NOT NULL COMMENT '社交用户原始信息',
  `code` varchar(32) NOT NULL COMMENT '最后一次认证code',
  `state` varchar(32) NOT NULL COMMENT '最后一次认证state',
  `bind_time` datetime NOT NULL COMMENT '绑定时间',
  `del_flag` tinyint DEFAULT '0' COMMENT '删除标识 0-未删除 null-已删除',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户id',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_uuid_source` (`uuid`,`source`,`del_flag`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='社交用户表';

-- ----------------------------
-- Records of auth_social_user
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for system_config
-- ----------------------------
DROP TABLE IF EXISTS `system_config`;
CREATE TABLE `system_config` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `config_key` varchar(100) NOT NULL COMMENT '配置键',
  `config_value` varchar(500) NOT NULL COMMENT '配置值',
  `config_name` varchar(32) NOT NULL COMMENT '配置名称',
  `create_user_id` bigint NOT NULL COMMENT '创建人id',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_user_id` bigint NOT NULL COMMENT '更新人id',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `del_flag` tinyint DEFAULT '0' COMMENT '删除标识 0-未删除 null-已删除',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户id',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_key_tenant` (`config_key`,`tenant_id`,`del_flag`) COMMENT '同一租户下 key 唯一'
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统配置';

-- ----------------------------
-- Records of system_config
-- ----------------------------
BEGIN;
INSERT INTO `system_config` (`id`, `config_key`, `config_value`, `config_name`, `create_user_id`, `create_time`, `update_user_id`, `update_time`, `del_flag`, `remark`, `tenant_id`) VALUES (1, 'minio-domain', 'https://xd11cc.xyz/minio-api', 'minio域名', 1, '2026-05-20 15:44:42', 1, '2026-05-20 15:44:47', 0, NULL, 1);
INSERT INTO `system_config` (`id`, `config_key`, `config_value`, `config_name`, `create_user_id`, `create_time`, `update_user_id`, `update_time`, `del_flag`, `remark`, `tenant_id`) VALUES (2, 'auth-redirect-successUrl', 'http://localhost:20001/xd11cc/dashboard?token=%s', '授权成功跳转地址', 1, '2026-05-21 11:14:10', 1, '2026-05-21 11:14:15', 0, NULL, 1);
INSERT INTO `system_config` (`id`, `config_key`, `config_value`, `config_name`, `create_user_id`, `create_time`, `update_user_id`, `update_time`, `del_flag`, `remark`, `tenant_id`) VALUES (3, 'auth-redirect-bindUserUrl', 'http://localhost:20001/xd11cc/login?source=%s&state=%s&need-bind', '授权绑定跳转地址', 1, '2026-05-21 11:15:38', 1, '2026-05-21 11:15:44', 0, NULL, 1);
COMMIT;

-- ----------------------------
-- Table structure for system_dept
-- ----------------------------
DROP TABLE IF EXISTS `system_dept`;
CREATE TABLE `system_dept` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `parent_id` bigint DEFAULT NULL COMMENT '父部门id',
  `dept_code` varchar(20) NOT NULL COMMENT '部门编码',
  `dept_name` varchar(20) NOT NULL COMMENT '部门名称',
  `leader_id` bigint NOT NULL COMMENT '部门负责人',
  `sort` int NOT NULL COMMENT '排序',
  `status` char(2) NOT NULL COMMENT '部门状态，字典类型system_status',
  `create_user_id` bigint NOT NULL COMMENT '创建人id',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_user_id` bigint NOT NULL COMMENT '更新人id',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `del_flag` tinyint DEFAULT '0' COMMENT '删除标识 0-未删除 null-已删除',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户id',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_dept_code_tenant` (`dept_code`,`tenant_id`,`del_flag`),
  KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='部门表';

-- ----------------------------
-- Records of system_dept
-- ----------------------------
BEGIN;
INSERT INTO `system_dept` (`id`, `parent_id`, `dept_code`, `dept_name`, `leader_id`, `sort`, `status`, `create_user_id`, `create_time`, `update_user_id`, `update_time`, `del_flag`, `remark`, `tenant_id`) VALUES (1, NULL, 'head_office', '总公司', 1, 0, '0', 1, '2026-05-27 11:00:08', 1, '2026-05-27 11:00:08', 0, NULL, 1);
INSERT INTO `system_dept` (`id`, `parent_id`, `dept_code`, `dept_name`, `leader_id`, `sort`, `status`, `create_user_id`, `create_time`, `update_user_id`, `update_time`, `del_flag`, `remark`, `tenant_id`) VALUES (2, 1, 'development', '研发部', 1, 0, '0', 1, '2026-05-27 11:03:23', 1, '2026-05-27 11:03:23', 0, NULL, 1);
COMMIT;

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
  KEY `idx_dept_id` (`dept_id`),
  KEY `idx_post_id` (`post_id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='部门岗位表';

-- ----------------------------
-- Records of system_dept_post
-- ----------------------------
BEGIN;
INSERT INTO `system_dept_post` (`id`, `dept_id`, `post_id`, `create_user_id`, `create_time`, `update_user_id`, `update_time`, `del_flag`, `remark`, `tenant_id`) VALUES (1, 1, 1, 1, '2026-05-27 11:05:40', 1, '2026-05-27 11:05:40', 0, NULL, 1);
INSERT INTO `system_dept_post` (`id`, `dept_id`, `post_id`, `create_user_id`, `create_time`, `update_user_id`, `update_time`, `del_flag`, `remark`, `tenant_id`) VALUES (2, 2, 1, 1, '2026-05-27 11:05:40', 1, '2026-05-27 11:05:40', 0, NULL, 1);
INSERT INTO `system_dept_post` (`id`, `dept_id`, `post_id`, `create_user_id`, `create_time`, `update_user_id`, `update_time`, `del_flag`, `remark`, `tenant_id`) VALUES (3, 1, 2, 1, '2026-05-27 11:06:17', 1, '2026-05-27 11:06:17', 0, NULL, 1);
INSERT INTO `system_dept_post` (`id`, `dept_id`, `post_id`, `create_user_id`, `create_time`, `update_user_id`, `update_time`, `del_flag`, `remark`, `tenant_id`) VALUES (4, 2, 2, 1, '2026-05-27 11:06:17', 1, '2026-05-27 11:06:17', 0, NULL, 1);
INSERT INTO `system_dept_post` (`id`, `dept_id`, `post_id`, `create_user_id`, `create_time`, `update_user_id`, `update_time`, `del_flag`, `remark`, `tenant_id`) VALUES (5, 1, 3, 1, '2026-05-27 11:06:54', 1, '2026-05-27 11:06:54', 0, NULL, 1);
INSERT INTO `system_dept_post` (`id`, `dept_id`, `post_id`, `create_user_id`, `create_time`, `update_user_id`, `update_time`, `del_flag`, `remark`, `tenant_id`) VALUES (6, 2, 3, 1, '2026-05-27 11:06:54', 1, '2026-05-27 11:06:54', 0, NULL, 1);
COMMIT;

-- ----------------------------
-- Table structure for system_dict_data
-- ----------------------------
DROP TABLE IF EXISTS `system_dict_data`;
CREATE TABLE `system_dict_data` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `dict_type` varchar(20) NOT NULL COMMENT '字典类型',
  `label` varchar(20) NOT NULL COMMENT '标签',
  `value` varchar(20) NOT NULL COMMENT '键值',
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
  UNIQUE KEY `uk_type_label_tenant` (`dict_type`,`label`,`tenant_id`,`del_flag`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='字典数据表';

-- ----------------------------
-- Records of system_dict_data
-- ----------------------------
BEGIN;
INSERT INTO `system_dict_data` (`id`, `dict_type`, `label`, `value`, `css_class`, `list_class`, `sort`, `status`, `create_user_id`, `create_time`, `update_user_id`, `update_time`, `del_flag`, `remark`, `tenant_id`) VALUES (1, 'system_status', '0', '开启', '', '', 0, '0', 1, '2026-01-30 17:41:51', 1, '2026-01-30 17:41:51', NULL, '', 1);
INSERT INTO `system_dict_data` (`id`, `dict_type`, `label`, `value`, `css_class`, `list_class`, `sort`, `status`, `create_user_id`, `create_time`, `update_user_id`, `update_time`, `del_flag`, `remark`, `tenant_id`) VALUES (2, 'system_status', '1', '停用', NULL, NULL, 1, NULL, 1, '2026-01-30 20:59:12', 1, '2026-01-30 20:59:12', NULL, NULL, 1);
INSERT INTO `system_dict_data` (`id`, `dict_type`, `label`, `value`, `css_class`, `list_class`, `sort`, `status`, `create_user_id`, `create_time`, `update_user_id`, `update_time`, `del_flag`, `remark`, `tenant_id`) VALUES (3, 'system_status', '0', '正常', NULL, NULL, 0, NULL, 1, '2026-01-30 21:04:27', 1, '2026-01-30 21:04:27', NULL, NULL, 1);
INSERT INTO `system_dict_data` (`id`, `dict_type`, `label`, `value`, `css_class`, `list_class`, `sort`, `status`, `create_user_id`, `create_time`, `update_user_id`, `update_time`, `del_flag`, `remark`, `tenant_id`) VALUES (4, 'system_status', '0', '正常', NULL, NULL, 0, NULL, 1, '2026-01-30 21:09:44', 1, '2026-01-30 21:21:20', NULL, NULL, 1);
INSERT INTO `system_dict_data` (`id`, `dict_type`, `label`, `value`, `css_class`, `list_class`, `sort`, `status`, `create_user_id`, `create_time`, `update_user_id`, `update_time`, `del_flag`, `remark`, `tenant_id`) VALUES (5, 'system_status', '0', '正常', NULL, 'success', 0, '0', 1, '2026-01-30 22:05:33', 1, '2026-02-10 15:54:58', 0, NULL, 1);
INSERT INTO `system_dict_data` (`id`, `dict_type`, `label`, `value`, `css_class`, `list_class`, `sort`, `status`, `create_user_id`, `create_time`, `update_user_id`, `update_time`, `del_flag`, `remark`, `tenant_id`) VALUES (6, 'system_status', '1', '停用', NULL, 'danger', 1, '0', 1, '2026-01-30 22:05:49', 1, '2026-02-02 17:32:16', 0, NULL, 1);
INSERT INTO `system_dict_data` (`id`, `dict_type`, `label`, `value`, `css_class`, `list_class`, `sort`, `status`, `create_user_id`, `create_time`, `update_user_id`, `update_time`, `del_flag`, `remark`, `tenant_id`) VALUES (7, 'system_menu_type', 'M', '目录', NULL, 'primary', 0, '0', 1, '2026-02-02 17:49:06', 1, '2026-02-02 17:49:58', 0, NULL, 1);
INSERT INTO `system_dict_data` (`id`, `dict_type`, `label`, `value`, `css_class`, `list_class`, `sort`, `status`, `create_user_id`, `create_time`, `update_user_id`, `update_time`, `del_flag`, `remark`, `tenant_id`) VALUES (8, 'system_menu_type', 'C', '菜单', NULL, 'primary', 1, '0', 1, '2026-02-02 17:50:24', 1, '2026-02-09 17:31:00', 0, NULL, 1);
INSERT INTO `system_dict_data` (`id`, `dict_type`, `label`, `value`, `css_class`, `list_class`, `sort`, `status`, `create_user_id`, `create_time`, `update_user_id`, `update_time`, `del_flag`, `remark`, `tenant_id`) VALUES (9, 'system_menu_type', 'B', '按钮', NULL, 'primary', 2, '0', 1, '2026-02-02 17:50:35', 1, '2026-02-09 17:31:04', 0, NULL, 1);
INSERT INTO `system_dict_data` (`id`, `dict_type`, `label`, `value`, `css_class`, `list_class`, `sort`, `status`, `create_user_id`, `create_time`, `update_user_id`, `update_time`, `del_flag`, `remark`, `tenant_id`) VALUES (11, 'system_user_sex', '0', '女', NULL, 'primary', 1, '0', 1, '2026-02-02 17:52:17', 1, '2026-02-09 17:38:47', 0, NULL, 1);
INSERT INTO `system_dict_data` (`id`, `dict_type`, `label`, `value`, `css_class`, `list_class`, `sort`, `status`, `create_user_id`, `create_time`, `update_user_id`, `update_time`, `del_flag`, `remark`, `tenant_id`) VALUES (12, 'system_user_sex', '1', '男', NULL, 'primary', 0, '0', 1, '2026-02-02 17:52:22', 1, '2026-02-02 17:52:22', 0, NULL, 1);
INSERT INTO `system_dict_data` (`id`, `dict_type`, `label`, `value`, `css_class`, `list_class`, `sort`, `status`, `create_user_id`, `create_time`, `update_user_id`, `update_time`, `del_flag`, `remark`, `tenant_id`) VALUES (13, 'system_visible', '0', '显示', NULL, 'primary', 0, '0', 1, '2026-02-10 15:59:00', 1, '2026-02-10 15:59:23', 0, NULL, 1);
INSERT INTO `system_dict_data` (`id`, `dict_type`, `label`, `value`, `css_class`, `list_class`, `sort`, `status`, `create_user_id`, `create_time`, `update_user_id`, `update_time`, `del_flag`, `remark`, `tenant_id`) VALUES (14, 'system_visible', '1', '隐藏', NULL, 'primary', 1, '0', 1, '2026-02-10 15:59:17', 1, '2026-02-10 15:59:30', 0, NULL, 1);
COMMIT;

-- ----------------------------
-- Table structure for system_dict_type
-- ----------------------------
DROP TABLE IF EXISTS `system_dict_type`;
CREATE TABLE `system_dict_type` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `dict_type` varchar(20) NOT NULL COMMENT '字典类型',
  `dict_name` varchar(20) NOT NULL COMMENT '字典名称',
  `create_user_id` bigint NOT NULL COMMENT '创建人id',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_user_id` bigint NOT NULL COMMENT '更新人id',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `del_flag` tinyint DEFAULT '0' COMMENT '删除标识 0-未删除 null-已删除',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `type` (`dict_type`,`del_flag`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='字典类型表';

-- ----------------------------
-- Records of system_dict_type
-- ----------------------------
BEGIN;
INSERT INTO `system_dict_type` (`id`, `dict_type`, `dict_name`, `create_user_id`, `create_time`, `update_user_id`, `update_time`, `del_flag`, `remark`) VALUES (1, 'system_status', '状态', 1, '2026-01-28 00:00:08', 1, '2026-01-28 11:17:50', 0, '系统通用状态');
INSERT INTO `system_dict_type` (`id`, `dict_type`, `dict_name`, `create_user_id`, `create_time`, `update_user_id`, `update_time`, `del_flag`, `remark`) VALUES (2, 'system_menu_type', '菜单类型', 1, '2026-01-28 09:49:18', 1, '2026-01-28 09:49:18', 0, NULL);
INSERT INTO `system_dict_type` (`id`, `dict_type`, `dict_name`, `create_user_id`, `create_time`, `update_user_id`, `update_time`, `del_flag`, `remark`) VALUES (3, 'system_user_sex', '性别', 1, '2026-01-28 00:00:28', 1, '2026-01-28 15:37:56', 0, NULL);
INSERT INTO `system_dict_type` (`id`, `dict_type`, `dict_name`, `create_user_id`, `create_time`, `update_user_id`, `update_time`, `del_flag`, `remark`) VALUES (4, 'system_visible', '显示状态', 1, '2026-02-10 15:58:37', 1, '2026-02-10 15:58:37', 0, '是否显示状态');
COMMIT;

-- ----------------------------
-- Table structure for system_job
-- ----------------------------
DROP TABLE IF EXISTS `system_job`;
CREATE TABLE `system_job` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `job_name` varchar(50) NOT NULL COMMENT '任务名称',
  `job_group` char(2) NOT NULL COMMENT '任务组名',
  `invoke_target` varchar(255) NOT NULL COMMENT '调度目标字符串',
  `cron_expression` varchar(50) NOT NULL COMMENT 'cron执行表达式',
  `execution_policy` char(2) NOT NULL COMMENT '执行策略',
  `concurrent` char(2) NOT NULL COMMENT '是否允许并发执行',
  `status` char(2) NOT NULL COMMENT '状态',
  `create_user_id` bigint NOT NULL COMMENT '创建人id',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_user_id` bigint NOT NULL COMMENT '更新人id',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `del_flag` tinyint DEFAULT '0' COMMENT '删除标识 0-未删除 null-已删除',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统任务表';

-- ----------------------------
-- Records of system_job
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for system_job_log
-- ----------------------------
DROP TABLE IF EXISTS `system_job_log`;
CREATE TABLE `system_job_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `job_id` bigint NOT NULL COMMENT '任务id',
  `job_name` varchar(50) NOT NULL COMMENT '任务名称',
  `job_group` char(2) NOT NULL COMMENT '任务组名',
  `invoke_target` varchar(255) NOT NULL COMMENT '调度目标字符串',
  `job_message` varchar(500) DEFAULT NULL COMMENT '日志信息',
  `status` char(2) DEFAULT NULL COMMENT '执行状态',
  `create_user_id` bigint NOT NULL COMMENT '创建人id',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_user_id` bigint NOT NULL COMMENT '更新人id',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `del_flag` tinyint DEFAULT '0' COMMENT '删除标识 0-未删除 null-已删除',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户id',
  PRIMARY KEY (`id`),
  KEY `idx_job_id` (`job_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统任务日志表';

-- ----------------------------
-- Records of system_job_log
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for system_menu
-- ----------------------------
DROP TABLE IF EXISTS `system_menu`;
CREATE TABLE `system_menu` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `parent_id` bigint DEFAULT NULL COMMENT '父菜单id',
  `menu_name` varchar(20) NOT NULL COMMENT '菜单名称',
  `sort` int NOT NULL COMMENT '显示排序',
  `path` varchar(255) DEFAULT NULL COMMENT '路由路径（如/user/list）',
  `component` varchar(255) DEFAULT NULL COMMENT '组件路径',
  `route_name` varchar(255) DEFAULT NULL COMMENT '路由名称',
  `query` varchar(255) DEFAULT NULL COMMENT '路由参数',
  `icon` varchar(255) DEFAULT NULL COMMENT '菜单图标',
  `menu_type` char(2) NOT NULL COMMENT '菜单类型，字典类型system_menu_type',
  `status` char(2) NOT NULL COMMENT '菜单状态，字典类型system_status',
  `permission` varchar(255) DEFAULT NULL COMMENT '权限字符',
  `visible` tinyint NOT NULL DEFAULT '0' COMMENT '是否隐藏，0-否 1-是',
  `create_user_id` bigint NOT NULL COMMENT '创建人id',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_user_id` bigint NOT NULL COMMENT '更新人id',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `del_flag` tinyint DEFAULT '0' COMMENT '删除标识 0-未删除 null-已删除',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_path` (`path`,`del_flag`) USING BTREE,
  KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB AUTO_INCREMENT=46 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='菜单表';

-- ----------------------------
-- Records of system_menu
-- ----------------------------
BEGIN;
INSERT INTO `system_menu` (`id`, `parent_id`, `menu_name`, `sort`, `path`, `component`, `route_name`, `query`, `icon`, `menu_type`, `status`, `permission`, `visible`, `create_user_id`, `create_time`, `update_user_id`, `update_time`, `del_flag`, `remark`) VALUES (1, NULL, '系统管理', 1, 'system', 'Layout', 'System', NULL, 'ep:setting', 'M', '0', NULL, 0, 1, '2026-01-20 18:03:15', 1, '2026-05-21 14:26:19', 0, NULL);
INSERT INTO `system_menu` (`id`, `parent_id`, `menu_name`, `sort`, `path`, `component`, `route_name`, `query`, `icon`, `menu_type`, `status`, `permission`, `visible`, `create_user_id`, `create_time`, `update_user_id`, `update_time`, `del_flag`, `remark`) VALUES (2, 1, '菜单管理', 5, 'menu', 'system/menu/index', 'Menu', NULL, 'ep:menu', 'C', '0', NULL, 0, 1, '2026-01-20 22:46:41', 1, '2026-05-27 15:06:24', 0, NULL);
INSERT INTO `system_menu` (`id`, `parent_id`, `menu_name`, `sort`, `path`, `component`, `route_name`, `query`, `icon`, `menu_type`, `status`, `permission`, `visible`, `create_user_id`, `create_time`, `update_user_id`, `update_time`, `del_flag`, `remark`) VALUES (3, 1, '字典管理', 6, 'dict', 'system/dict/index', 'Dict', NULL, 'ep:notebook', 'C', '0', NULL, 0, 1, '2026-01-22 10:28:08', 1, '2026-05-27 15:06:36', 0, NULL);
INSERT INTO `system_menu` (`id`, `parent_id`, `menu_name`, `sort`, `path`, `component`, `route_name`, `query`, `icon`, `menu_type`, `status`, `permission`, `visible`, `create_user_id`, `create_time`, `update_user_id`, `update_time`, `del_flag`, `remark`) VALUES (4, 1, '字典数据', 6, 'dict-data', 'system/dict/data', 'DictData', NULL, NULL, 'C', '0', NULL, 1, 1, '2026-01-28 17:26:27', 1, '2026-05-27 15:06:45', 0, NULL);
INSERT INTO `system_menu` (`id`, `parent_id`, `menu_name`, `sort`, `path`, `component`, `route_name`, `query`, `icon`, `menu_type`, `status`, `permission`, `visible`, `create_user_id`, `create_time`, `update_user_id`, `update_time`, `del_flag`, `remark`) VALUES (5, 3, '字典查询', 4, NULL, NULL, NULL, NULL, NULL, 'B', '0', 'system:dictType:list', 0, 1, '2026-02-11 11:07:38', 1, '2026-03-17 10:27:57', NULL, NULL);
INSERT INTO `system_menu` (`id`, `parent_id`, `menu_name`, `sort`, `path`, `component`, `route_name`, `query`, `icon`, `menu_type`, `status`, `permission`, `visible`, `create_user_id`, `create_time`, `update_user_id`, `update_time`, `del_flag`, `remark`) VALUES (6, NULL, '系统监控', 3, 'monitor', 'Layout', 'Monitor', NULL, 'ep:monitor', 'M', '0', NULL, 0, 1, '2026-02-12 14:31:43', 1, '2026-05-21 14:39:39', 0, NULL);
INSERT INTO `system_menu` (`id`, `parent_id`, `menu_name`, `sort`, `path`, `component`, `route_name`, `query`, `icon`, `menu_type`, `status`, `permission`, `visible`, `create_user_id`, `create_time`, `update_user_id`, `update_time`, `del_flag`, `remark`) VALUES (7, 3, '字典新增', 1, NULL, NULL, NULL, NULL, NULL, 'B', '0', 'system:dictType:add', 0, 1, '2026-02-12 14:45:05', 1, '2026-03-17 10:28:17', 0, NULL);
INSERT INTO `system_menu` (`id`, `parent_id`, `menu_name`, `sort`, `path`, `component`, `route_name`, `query`, `icon`, `menu_type`, `status`, `permission`, `visible`, `create_user_id`, `create_time`, `update_user_id`, `update_time`, `del_flag`, `remark`) VALUES (8, 3, '字典修改', 3, NULL, NULL, NULL, NULL, NULL, 'B', '0', 'systm:dictType:update', 0, 1, '2026-02-12 14:45:38', 1, '2026-03-17 10:28:05', 0, NULL);
INSERT INTO `system_menu` (`id`, `parent_id`, `menu_name`, `sort`, `path`, `component`, `route_name`, `query`, `icon`, `menu_type`, `status`, `permission`, `visible`, `create_user_id`, `create_time`, `update_user_id`, `update_time`, `del_flag`, `remark`) VALUES (9, 3, '字典删除', 2, NULL, NULL, NULL, NULL, NULL, 'B', '0', 'system:dictType:delete', 0, 1, '2026-02-12 14:46:05', 1, '2026-03-17 10:28:10', 0, NULL);
INSERT INTO `system_menu` (`id`, `parent_id`, `menu_name`, `sort`, `path`, `component`, `route_name`, `query`, `icon`, `menu_type`, `status`, `permission`, `visible`, `create_user_id`, `create_time`, `update_user_id`, `update_time`, `del_flag`, `remark`) VALUES (10, 6, '数据管理', 1, 'druid', 'monitor/druid/index', 'Druid', NULL, 'ep:data-line', 'C', '0', NULL, 0, 1, '2026-02-13 09:30:16', 1, '2026-05-21 14:39:48', 0, NULL);
INSERT INTO `system_menu` (`id`, `parent_id`, `menu_name`, `sort`, `path`, `component`, `route_name`, `query`, `icon`, `menu_type`, `status`, `permission`, `visible`, `create_user_id`, `create_time`, `update_user_id`, `update_time`, `del_flag`, `remark`) VALUES (11, 3, '字典导出', 5, NULL, NULL, NULL, NULL, NULL, 'B', '0', 'system:dictType:export', 0, 1, '2026-03-17 10:47:57', 1, '2026-03-17 10:47:57', 0, NULL);
INSERT INTO `system_menu` (`id`, `parent_id`, `menu_name`, `sort`, `path`, `component`, `route_name`, `query`, `icon`, `menu_type`, `status`, `permission`, `visible`, `create_user_id`, `create_time`, `update_user_id`, `update_time`, `del_flag`, `remark`) VALUES (12, NULL, '系统工具', 2, 'tool', 'Layout', 'Tool', NULL, 'ep:shopping-bag', 'M', '0', NULL, 0, 1, '2026-04-20 15:01:32', 1, '2026-05-21 14:39:23', 0, NULL);
INSERT INTO `system_menu` (`id`, `parent_id`, `menu_name`, `sort`, `path`, `component`, `route_name`, `query`, `icon`, `menu_type`, `status`, `permission`, `visible`, `create_user_id`, `create_time`, `update_user_id`, `update_time`, `del_flag`, `remark`) VALUES (13, 12, '代码生成', 1, 'generateCode', 'tool/generateCode/index', 'GenerateCode', NULL, 'ep:connection', 'C', '0', NULL, 0, 1, '2026-04-20 15:04:39', 1, '2026-05-21 14:39:32', 0, NULL);
INSERT INTO `system_menu` (`id`, `parent_id`, `menu_name`, `sort`, `path`, `component`, `route_name`, `query`, `icon`, `menu_type`, `status`, `permission`, `visible`, `create_user_id`, `create_time`, `update_user_id`, `update_time`, `del_flag`, `remark`) VALUES (14, 1, '系统配置', 4, 'config', 'system/config/index', 'Config', NULL, 'ep:operation', 'C', '0', NULL, 0, 1, '2026-05-21 15:09:44', 1, '2026-05-21 15:18:14', 0, NULL);
INSERT INTO `system_menu` (`id`, `parent_id`, `menu_name`, `sort`, `path`, `component`, `route_name`, `query`, `icon`, `menu_type`, `status`, `permission`, `visible`, `create_user_id`, `create_time`, `update_user_id`, `update_time`, `del_flag`, `remark`) VALUES (15, 1, '用户管理', 2, 'user', 'system/user/index', 'User', NULL, 'ep:user', 'C', '0', NULL, 0, 1, '2026-05-21 15:28:34', 1, '2026-05-27 15:05:57', 0, NULL);
INSERT INTO `system_menu` (`id`, `parent_id`, `menu_name`, `sort`, `path`, `component`, `route_name`, `query`, `icon`, `menu_type`, `status`, `permission`, `visible`, `create_user_id`, `create_time`, `update_user_id`, `update_time`, `del_flag`, `remark`) VALUES (16, 1, '部门管理', 1, 'dept', 'system/dept/index', 'Dept', NULL, 'lucide:network', 'C', '0', NULL, 0, 1, '2026-05-26 16:24:33', 1, '2026-05-27 15:06:16', 0, NULL);
INSERT INTO `system_menu` (`id`, `parent_id`, `menu_name`, `sort`, `path`, `component`, `route_name`, `query`, `icon`, `menu_type`, `status`, `permission`, `visible`, `create_user_id`, `create_time`, `update_user_id`, `update_time`, `del_flag`, `remark`) VALUES (17, 1, '岗位管理', 4, 'post', 'system/post/index', 'Post', NULL, 'lucide:badge-check', 'C', '0', NULL, 0, 1, '2026-05-26 16:31:34', 1, '2026-05-27 15:06:10', 0, NULL);
INSERT INTO `system_menu` (`id`, `parent_id`, `menu_name`, `sort`, `path`, `component`, `route_name`, `query`, `icon`, `menu_type`, `status`, `permission`, `visible`, `create_user_id`, `create_time`, `update_user_id`, `update_time`, `del_flag`, `remark`) VALUES (18, 1, '角色管理', 3, 'role', 'system/role/index', 'Role', NULL, 'lucide:shield-check', 'C', '0', NULL, 0, 1, '2026-05-26 16:36:15', 1, '2026-05-27 15:06:06', 0, NULL);
INSERT INTO `system_menu` (`id`, `parent_id`, `menu_name`, `sort`, `path`, `component`, `route_name`, `query`, `icon`, `menu_type`, `status`, `permission`, `visible`, `create_user_id`, `create_time`, `update_user_id`, `update_time`, `del_flag`, `remark`) VALUES (19, 1, '授权应用配置', 7, 'authClient', 'system/authClient/index', 'AuthClient', NULL, 'lucide:key-round', 'C', '0', NULL, 0, 1, '2026-05-27 14:55:23', 1, '2026-05-27 15:06:52', 0, NULL);
INSERT INTO `system_menu` (`id`, `parent_id`, `menu_name`, `sort`, `path`, `component`, `route_name`, `query`, `icon`, `menu_type`, `status`, `permission`, `visible`, `create_user_id`, `create_time`, `update_user_id`, `update_time`, `del_flag`, `remark`) VALUES (20, 16, '部门新增', 1, NULL, NULL, NULL, NULL, '', 'B', '0', 'system:dept:add', 0, 1, '2026-05-28 10:36:05', 1, '2026-05-28 10:36:05', 0, NULL);
INSERT INTO `system_menu` (`id`, `parent_id`, `menu_name`, `sort`, `path`, `component`, `route_name`, `query`, `icon`, `menu_type`, `status`, `permission`, `visible`, `create_user_id`, `create_time`, `update_user_id`, `update_time`, `del_flag`, `remark`) VALUES (21, 16, '删除部门', 2, NULL, NULL, NULL, NULL, '', 'B', '0', 'system:dept:delete', 0, 1, '2026-05-28 10:36:22', 1, '2026-05-28 10:36:22', 0, NULL);
INSERT INTO `system_menu` (`id`, `parent_id`, `menu_name`, `sort`, `path`, `component`, `route_name`, `query`, `icon`, `menu_type`, `status`, `permission`, `visible`, `create_user_id`, `create_time`, `update_user_id`, `update_time`, `del_flag`, `remark`) VALUES (22, 16, '修改部门', 3, NULL, NULL, NULL, NULL, '', 'B', '0', 'system:dept:update', 0, 1, '2026-05-28 10:36:54', 1, '2026-05-28 10:37:01', 0, NULL);
INSERT INTO `system_menu` (`id`, `parent_id`, `menu_name`, `sort`, `path`, `component`, `route_name`, `query`, `icon`, `menu_type`, `status`, `permission`, `visible`, `create_user_id`, `create_time`, `update_user_id`, `update_time`, `del_flag`, `remark`) VALUES (23, 15, '新增用户', 1, NULL, NULL, NULL, NULL, '', 'B', '0', 'system:user:add', 0, 1, '2026-05-28 10:51:54', 1, '2026-05-28 10:51:54', 0, NULL);
INSERT INTO `system_menu` (`id`, `parent_id`, `menu_name`, `sort`, `path`, `component`, `route_name`, `query`, `icon`, `menu_type`, `status`, `permission`, `visible`, `create_user_id`, `create_time`, `update_user_id`, `update_time`, `del_flag`, `remark`) VALUES (24, 15, '删除用户', 2, NULL, NULL, NULL, NULL, '', 'B', '0', 'system:user:delete', 0, 1, '2026-05-28 10:52:08', 1, '2026-05-28 10:52:08', 0, NULL);
INSERT INTO `system_menu` (`id`, `parent_id`, `menu_name`, `sort`, `path`, `component`, `route_name`, `query`, `icon`, `menu_type`, `status`, `permission`, `visible`, `create_user_id`, `create_time`, `update_user_id`, `update_time`, `del_flag`, `remark`) VALUES (25, 15, '修改用户', 3, NULL, NULL, NULL, NULL, '', 'B', '0', 'system:user:update', 0, 1, '2026-05-28 10:52:23', 1, '2026-05-28 10:52:23', 0, NULL);
INSERT INTO `system_menu` (`id`, `parent_id`, `menu_name`, `sort`, `path`, `component`, `route_name`, `query`, `icon`, `menu_type`, `status`, `permission`, `visible`, `create_user_id`, `create_time`, `update_user_id`, `update_time`, `del_flag`, `remark`) VALUES (26, 15, '重置密码', 4, NULL, NULL, NULL, NULL, '', 'B', '0', 'system:user:resetPassword', 0, 1, '2026-05-28 10:52:47', 1, '2026-05-28 10:52:47', 0, NULL);
INSERT INTO `system_menu` (`id`, `parent_id`, `menu_name`, `sort`, `path`, `component`, `route_name`, `query`, `icon`, `menu_type`, `status`, `permission`, `visible`, `create_user_id`, `create_time`, `update_user_id`, `update_time`, `del_flag`, `remark`) VALUES (27, 18, '新增角色', 1, NULL, NULL, NULL, NULL, '', 'B', '0', 'system:role:add', 0, 1, '2026-05-28 10:55:00', 1, '2026-05-28 10:55:00', 0, NULL);
INSERT INTO `system_menu` (`id`, `parent_id`, `menu_name`, `sort`, `path`, `component`, `route_name`, `query`, `icon`, `menu_type`, `status`, `permission`, `visible`, `create_user_id`, `create_time`, `update_user_id`, `update_time`, `del_flag`, `remark`) VALUES (28, 18, '删除角色', 2, NULL, NULL, NULL, NULL, '', 'B', '0', 'system:role:delete', 0, 1, '2026-05-28 10:55:26', 1, '2026-05-28 10:55:26', 0, NULL);
INSERT INTO `system_menu` (`id`, `parent_id`, `menu_name`, `sort`, `path`, `component`, `route_name`, `query`, `icon`, `menu_type`, `status`, `permission`, `visible`, `create_user_id`, `create_time`, `update_user_id`, `update_time`, `del_flag`, `remark`) VALUES (29, 18, '修改角色', 3, NULL, NULL, NULL, NULL, '', 'B', '0', 'system:role:update', 0, 1, '2026-05-28 10:57:48', 1, '2026-05-28 10:57:48', 0, NULL);
INSERT INTO `system_menu` (`id`, `parent_id`, `menu_name`, `sort`, `path`, `component`, `route_name`, `query`, `icon`, `menu_type`, `status`, `permission`, `visible`, `create_user_id`, `create_time`, `update_user_id`, `update_time`, `del_flag`, `remark`) VALUES (30, 14, '新增配置', 1, NULL, NULL, NULL, NULL, '', 'B', '0', 'system:config:add', 0, 1, '2026-05-28 10:58:22', 1, '2026-05-28 10:58:22', 0, NULL);
INSERT INTO `system_menu` (`id`, `parent_id`, `menu_name`, `sort`, `path`, `component`, `route_name`, `query`, `icon`, `menu_type`, `status`, `permission`, `visible`, `create_user_id`, `create_time`, `update_user_id`, `update_time`, `del_flag`, `remark`) VALUES (31, 14, '删除配置', 2, NULL, NULL, NULL, NULL, '', 'B', '0', 'system:config:delete', 0, 1, '2026-05-28 10:58:37', 1, '2026-05-28 10:58:37', 0, NULL);
INSERT INTO `system_menu` (`id`, `parent_id`, `menu_name`, `sort`, `path`, `component`, `route_name`, `query`, `icon`, `menu_type`, `status`, `permission`, `visible`, `create_user_id`, `create_time`, `update_user_id`, `update_time`, `del_flag`, `remark`) VALUES (32, 14, '更新配置', 3, NULL, NULL, NULL, NULL, '', 'B', '0', 'system:config:update', 0, 1, '2026-05-28 10:58:50', 1, '2026-05-28 10:58:50', 0, NULL);
INSERT INTO `system_menu` (`id`, `parent_id`, `menu_name`, `sort`, `path`, `component`, `route_name`, `query`, `icon`, `menu_type`, `status`, `permission`, `visible`, `create_user_id`, `create_time`, `update_user_id`, `update_time`, `del_flag`, `remark`) VALUES (33, 17, '新增岗位', 1, NULL, NULL, NULL, NULL, '', 'B', '0', 'system:post:add', 0, 1, '2026-05-28 10:59:10', 1, '2026-05-28 10:59:10', 0, NULL);
INSERT INTO `system_menu` (`id`, `parent_id`, `menu_name`, `sort`, `path`, `component`, `route_name`, `query`, `icon`, `menu_type`, `status`, `permission`, `visible`, `create_user_id`, `create_time`, `update_user_id`, `update_time`, `del_flag`, `remark`) VALUES (34, 17, '删除岗位', 2, NULL, NULL, NULL, NULL, '', 'B', '0', 'system:post:delete', 0, 1, '2026-05-28 10:59:22', 1, '2026-05-28 10:59:22', 0, NULL);
INSERT INTO `system_menu` (`id`, `parent_id`, `menu_name`, `sort`, `path`, `component`, `route_name`, `query`, `icon`, `menu_type`, `status`, `permission`, `visible`, `create_user_id`, `create_time`, `update_user_id`, `update_time`, `del_flag`, `remark`) VALUES (35, 17, '修改岗位', 3, NULL, NULL, NULL, NULL, '', 'B', '0', 'system:post:update', 0, 1, '2026-05-28 10:59:34', 1, '2026-05-28 10:59:34', 0, NULL);
INSERT INTO `system_menu` (`id`, `parent_id`, `menu_name`, `sort`, `path`, `component`, `route_name`, `query`, `icon`, `menu_type`, `status`, `permission`, `visible`, `create_user_id`, `create_time`, `update_user_id`, `update_time`, `del_flag`, `remark`) VALUES (36, 2, '新增菜单', 1, NULL, NULL, NULL, NULL, '', 'B', '0', 'system:menu:add', 0, 1, '2026-05-28 11:02:56', 1, '2026-05-28 11:02:56', 0, NULL);
INSERT INTO `system_menu` (`id`, `parent_id`, `menu_name`, `sort`, `path`, `component`, `route_name`, `query`, `icon`, `menu_type`, `status`, `permission`, `visible`, `create_user_id`, `create_time`, `update_user_id`, `update_time`, `del_flag`, `remark`) VALUES (37, 2, '删除菜单', 2, NULL, NULL, NULL, NULL, '', 'B', '0', 'system:menu:delete', 0, 1, '2026-05-28 11:03:11', 1, '2026-05-28 11:03:11', 0, NULL);
INSERT INTO `system_menu` (`id`, `parent_id`, `menu_name`, `sort`, `path`, `component`, `route_name`, `query`, `icon`, `menu_type`, `status`, `permission`, `visible`, `create_user_id`, `create_time`, `update_user_id`, `update_time`, `del_flag`, `remark`) VALUES (38, 2, '修改菜单', 3, NULL, NULL, NULL, NULL, '', 'B', '0', 'system:menu:update', 0, 1, '2026-05-28 11:03:25', 1, '2026-05-28 11:03:25', 0, NULL);
INSERT INTO `system_menu` (`id`, `parent_id`, `menu_name`, `sort`, `path`, `component`, `route_name`, `query`, `icon`, `menu_type`, `status`, `permission`, `visible`, `create_user_id`, `create_time`, `update_user_id`, `update_time`, `del_flag`, `remark`) VALUES (39, 4, '新增字典数据', 1, NULL, NULL, NULL, NULL, '', 'B', '0', 'system:dictData:add', 0, 1, '2026-05-28 11:04:14', 1, '2026-05-28 11:04:14', 0, NULL);
INSERT INTO `system_menu` (`id`, `parent_id`, `menu_name`, `sort`, `path`, `component`, `route_name`, `query`, `icon`, `menu_type`, `status`, `permission`, `visible`, `create_user_id`, `create_time`, `update_user_id`, `update_time`, `del_flag`, `remark`) VALUES (40, 4, '删除字典数据', 2, NULL, NULL, NULL, NULL, '', 'B', '0', 'system:dictData:delete', 0, 1, '2026-05-28 11:04:28', 1, '2026-05-28 11:04:28', 0, NULL);
INSERT INTO `system_menu` (`id`, `parent_id`, `menu_name`, `sort`, `path`, `component`, `route_name`, `query`, `icon`, `menu_type`, `status`, `permission`, `visible`, `create_user_id`, `create_time`, `update_user_id`, `update_time`, `del_flag`, `remark`) VALUES (41, 4, '修改字典数据', 3, NULL, NULL, NULL, NULL, '', 'B', '0', 'system:dictData:update', 0, 1, '2026-05-28 11:04:41', 1, '2026-05-28 11:04:41', 0, NULL);
INSERT INTO `system_menu` (`id`, `parent_id`, `menu_name`, `sort`, `path`, `component`, `route_name`, `query`, `icon`, `menu_type`, `status`, `permission`, `visible`, `create_user_id`, `create_time`, `update_user_id`, `update_time`, `del_flag`, `remark`) VALUES (42, 4, '导出字典数据', 4, NULL, NULL, NULL, NULL, '', 'B', '0', 'system:dictData:export', 0, 1, '2026-05-28 11:05:01', 1, '2026-05-28 11:05:01', 0, NULL);
INSERT INTO `system_menu` (`id`, `parent_id`, `menu_name`, `sort`, `path`, `component`, `route_name`, `query`, `icon`, `menu_type`, `status`, `permission`, `visible`, `create_user_id`, `create_time`, `update_user_id`, `update_time`, `del_flag`, `remark`) VALUES (43, 19, '新增授权配置', 1, NULL, NULL, NULL, NULL, '', 'B', '0', 'auth:clientConfig:add', 0, 1, '2026-05-28 11:05:23', 1, '2026-05-28 11:05:23', 0, NULL);
INSERT INTO `system_menu` (`id`, `parent_id`, `menu_name`, `sort`, `path`, `component`, `route_name`, `query`, `icon`, `menu_type`, `status`, `permission`, `visible`, `create_user_id`, `create_time`, `update_user_id`, `update_time`, `del_flag`, `remark`) VALUES (44, 19, '删除授权配置', 2, NULL, NULL, NULL, NULL, '', 'B', '0', 'auth:clientConfig:delete', 0, 1, '2026-05-28 11:05:47', 1, '2026-05-28 11:05:47', 0, NULL);
INSERT INTO `system_menu` (`id`, `parent_id`, `menu_name`, `sort`, `path`, `component`, `route_name`, `query`, `icon`, `menu_type`, `status`, `permission`, `visible`, `create_user_id`, `create_time`, `update_user_id`, `update_time`, `del_flag`, `remark`) VALUES (45, 19, '修改授权配置', 3, NULL, NULL, NULL, NULL, '', 'B', '0', 'auth:clientConfig:update', 0, 1, '2026-05-28 11:06:03', 1, '2026-05-28 11:06:03', 0, NULL);
COMMIT;

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
  UNIQUE KEY `uk_post_code_tenant` (`post_code`,`tenant_id`,`del_flag`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='岗位表';

-- ----------------------------
-- Records of system_post
-- ----------------------------
BEGIN;
INSERT INTO `system_post` (`id`, `post_code`, `post_name`, `status`, `create_user_id`, `create_time`, `update_user_id`, `update_time`, `del_flag`, `remark`, `tenant_id`) VALUES (1, 'development_manager', '研发经理', '0', 1, '2026-05-27 11:05:40', 1, '2026-05-27 11:05:40', 0, NULL, 1);
INSERT INTO `system_post` (`id`, `post_code`, `post_name`, `status`, `create_user_id`, `create_time`, `update_user_id`, `update_time`, `del_flag`, `remark`, `tenant_id`) VALUES (2, 'technical_leader', '技术组长', '0', 1, '2026-05-27 11:06:17', 1, '2026-05-27 11:06:17', 0, NULL, 1);
INSERT INTO `system_post` (`id`, `post_code`, `post_name`, `status`, `create_user_id`, `create_time`, `update_user_id`, `update_time`, `del_flag`, `remark`, `tenant_id`) VALUES (3, 'java_developemnt', 'Java开发工程师', '0', 1, '2026-05-27 11:06:54', 1, '2026-05-27 11:06:54', 0, NULL, 1);
COMMIT;

-- ----------------------------
-- Table structure for system_role
-- ----------------------------
DROP TABLE IF EXISTS `system_role`;
CREATE TABLE `system_role` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `role_code` varchar(20) NOT NULL COMMENT '角色编码',
  `role_name` varchar(20) NOT NULL COMMENT '角色名称',
  `data_scope` char(2) NOT NULL DEFAULT '1' COMMENT '数据范围 1-全部 2-本部门及下级 3-本部门 4-仅本人 5-自定义',
  `status` char(2) NOT NULL COMMENT '角色状态，字典类型system_status',
  `create_user_id` bigint NOT NULL COMMENT '创建人id',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_user_id` bigint NOT NULL COMMENT '更新人id',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `del_flag` tinyint DEFAULT '0' COMMENT '删除标识 0-未删除 null-已删除',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户id',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_code_tenant` (`role_code`,`tenant_id`,`del_flag`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色表';

-- ----------------------------
-- Records of system_role
-- ----------------------------
BEGIN;
INSERT INTO `system_role` (`id`, `role_code`, `role_name`, `data_scope`, `status`, `create_user_id`, `create_time`, `update_user_id`, `update_time`, `del_flag`, `remark`, `tenant_id`) VALUES (1, 'super_admin', '超级管理员', '1', '0', 1, '2026-05-27 10:59:35', 1, '2026-05-28 14:20:43', 0, NULL, 1);
COMMIT;

-- ----------------------------
-- Table structure for system_role_dept
-- ----------------------------
DROP TABLE IF EXISTS `system_role_dept`;
CREATE TABLE `system_role_dept` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `role_id` bigint NOT NULL COMMENT '角色id',
  `dept_id` bigint NOT NULL COMMENT '部门id',
  `create_user_id` bigint NOT NULL COMMENT '创建人id',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_user_id` bigint NOT NULL COMMENT '更新人id',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `del_flag` tinyint DEFAULT '0' COMMENT '删除标识 0-未删除 null-已删除',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户id',
  PRIMARY KEY (`id`),
  KEY `idx_role_id` (`role_id`)
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
  KEY `idx_role_id` (`role_id`),
  KEY `idx_menu_id` (`menu_id`)
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色菜单表';

-- ----------------------------
-- Records of system_role_menu
-- ----------------------------
BEGIN;
INSERT INTO `system_role_menu` (`id`, `role_id`, `menu_id`, `create_user_id`, `create_time`, `update_user_id`, `update_time`, `del_flag`, `remark`, `tenant_id`) VALUES (1, 1, 1, 1, '2026-05-28 14:20:43', 1, '2026-05-28 14:20:43', 0, NULL, 1);
INSERT INTO `system_role_menu` (`id`, `role_id`, `menu_id`, `create_user_id`, `create_time`, `update_user_id`, `update_time`, `del_flag`, `remark`, `tenant_id`) VALUES (2, 1, 16, 1, '2026-05-28 14:20:43', 1, '2026-05-28 14:20:43', 0, NULL, 1);
INSERT INTO `system_role_menu` (`id`, `role_id`, `menu_id`, `create_user_id`, `create_time`, `update_user_id`, `update_time`, `del_flag`, `remark`, `tenant_id`) VALUES (3, 1, 15, 1, '2026-05-28 14:20:43', 1, '2026-05-28 14:20:43', 0, NULL, 1);
INSERT INTO `system_role_menu` (`id`, `role_id`, `menu_id`, `create_user_id`, `create_time`, `update_user_id`, `update_time`, `del_flag`, `remark`, `tenant_id`) VALUES (4, 1, 18, 1, '2026-05-28 14:20:43', 1, '2026-05-28 14:20:43', 0, NULL, 1);
INSERT INTO `system_role_menu` (`id`, `role_id`, `menu_id`, `create_user_id`, `create_time`, `update_user_id`, `update_time`, `del_flag`, `remark`, `tenant_id`) VALUES (5, 1, 14, 1, '2026-05-28 14:20:43', 1, '2026-05-28 14:20:43', 0, NULL, 1);
INSERT INTO `system_role_menu` (`id`, `role_id`, `menu_id`, `create_user_id`, `create_time`, `update_user_id`, `update_time`, `del_flag`, `remark`, `tenant_id`) VALUES (6, 1, 17, 1, '2026-05-28 14:20:43', 1, '2026-05-28 14:20:43', 0, NULL, 1);
INSERT INTO `system_role_menu` (`id`, `role_id`, `menu_id`, `create_user_id`, `create_time`, `update_user_id`, `update_time`, `del_flag`, `remark`, `tenant_id`) VALUES (7, 1, 2, 1, '2026-05-28 14:20:43', 1, '2026-05-28 14:20:43', 0, NULL, 1);
INSERT INTO `system_role_menu` (`id`, `role_id`, `menu_id`, `create_user_id`, `create_time`, `update_user_id`, `update_time`, `del_flag`, `remark`, `tenant_id`) VALUES (8, 1, 3, 1, '2026-05-28 14:20:43', 1, '2026-05-28 14:20:43', 0, NULL, 1);
INSERT INTO `system_role_menu` (`id`, `role_id`, `menu_id`, `create_user_id`, `create_time`, `update_user_id`, `update_time`, `del_flag`, `remark`, `tenant_id`) VALUES (9, 1, 4, 1, '2026-05-28 14:20:43', 1, '2026-05-28 14:20:43', 0, NULL, 1);
INSERT INTO `system_role_menu` (`id`, `role_id`, `menu_id`, `create_user_id`, `create_time`, `update_user_id`, `update_time`, `del_flag`, `remark`, `tenant_id`) VALUES (10, 1, 19, 1, '2026-05-28 14:20:43', 1, '2026-05-28 14:20:43', 0, NULL, 1);
INSERT INTO `system_role_menu` (`id`, `role_id`, `menu_id`, `create_user_id`, `create_time`, `update_user_id`, `update_time`, `del_flag`, `remark`, `tenant_id`) VALUES (11, 1, 12, 1, '2026-05-28 14:20:43', 1, '2026-05-28 14:20:43', 0, NULL, 1);
INSERT INTO `system_role_menu` (`id`, `role_id`, `menu_id`, `create_user_id`, `create_time`, `update_user_id`, `update_time`, `del_flag`, `remark`, `tenant_id`) VALUES (12, 1, 13, 1, '2026-05-28 14:20:43', 1, '2026-05-28 14:20:43', 0, NULL, 1);
INSERT INTO `system_role_menu` (`id`, `role_id`, `menu_id`, `create_user_id`, `create_time`, `update_user_id`, `update_time`, `del_flag`, `remark`, `tenant_id`) VALUES (13, 1, 6, 1, '2026-05-28 14:20:43', 1, '2026-05-28 14:20:43', 0, NULL, 1);
INSERT INTO `system_role_menu` (`id`, `role_id`, `menu_id`, `create_user_id`, `create_time`, `update_user_id`, `update_time`, `del_flag`, `remark`, `tenant_id`) VALUES (14, 1, 10, 1, '2026-05-28 14:20:43', 1, '2026-05-28 14:20:43', 0, NULL, 1);
COMMIT;

-- ----------------------------
-- Table structure for system_user
-- ----------------------------
DROP TABLE IF EXISTS `system_user`;
CREATE TABLE `system_user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `username` varchar(20) NOT NULL COMMENT '用户名',
  `password` varchar(64) NOT NULL COMMENT '密码',
  `nickname` varchar(20) NOT NULL COMMENT '昵称',
  `id_card` varchar(18) DEFAULT NULL COMMENT '身份证号码',
  `sex` char(2) NOT NULL COMMENT '性别，字典类型system_user_sex',
  `phone` varchar(11) DEFAULT NULL COMMENT '手机号码',
  `email` varchar(20) DEFAULT NULL COMMENT '邮箱',
  `dept_id` bigint NOT NULL COMMENT '部门id',
  `dept_name` varchar(20) NOT NULL COMMENT '部门名称',
  `post_id` bigint NOT NULL COMMENT '岗位id',
  `post_name` varchar(20) NOT NULL COMMENT '岗位名称',
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
  UNIQUE KEY `uk_username_tenant` (`username`,`tenant_id`,`del_flag`),
  KEY `idx_dept_id` (`dept_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户表';

-- ----------------------------
-- Records of system_user
-- ----------------------------
BEGIN;
INSERT INTO `system_user` (`id`, `username`, `password`, `nickname`, `id_card`, `sex`, `phone`, `email`, `dept_id`, `dept_name`, `post_id`, `post_name`, `status`, `head_url`, `create_user_id`, `create_time`, `update_user_id`, `update_time`, `del_flag`, `remark`, `tenant_id`) VALUES (1, 'admin', '$2a$10$gHpwj0TMpbxdV5fOdTw03.xcfIxeXRjSo0xVa3J9q1vn27EyHpXkK', 'xd11cc', NULL, '1', '15396919088', '295900422@qq.com', 1, '总公司', 1, '研发经理', '0', NULL, 1, '2025-07-17 09:18:26', 1, '2026-05-28 10:24:09', 0, NULL, 1);
INSERT INTO `system_user` (`id`, `username`, `password`, `nickname`, `id_card`, `sex`, `phone`, `email`, `dept_id`, `dept_name`, `post_id`, `post_name`, `status`, `head_url`, `create_user_id`, `create_time`, `update_user_id`, `update_time`, `del_flag`, `remark`, `tenant_id`) VALUES (2, 'wangchen', '$2a$10$RDVljAeGhmzJ5yePhBbEmeBSXTKToRpbzyNVZd28A12W.lqpiC4UO', '老王', NULL, '1', NULL, NULL, 2, '研发部', 1, '研发经理', '0', NULL, 1, '2026-05-27 16:40:50', 1, '2026-05-27 16:40:50', 0, NULL, 1);
COMMIT;

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
  KEY `idx_user_id` (`user_id`),
  KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户角色表';

-- ----------------------------
-- Records of system_user_role
-- ----------------------------
BEGIN;
INSERT INTO `system_user_role` (`id`, `user_id`, `role_id`, `create_user_id`, `create_time`, `update_user_id`, `update_time`, `del_flag`, `remark`, `tenant_id`) VALUES (1, 2, 1, 1, '2026-05-27 16:40:50', 1, '2026-05-27 16:40:50', 0, NULL, 1);
INSERT INTO `system_user_role` (`id`, `user_id`, `role_id`, `create_user_id`, `create_time`, `update_user_id`, `update_time`, `del_flag`, `remark`, `tenant_id`) VALUES (2, 1, 1, 1, '2026-05-27 16:59:54', 1, '2026-05-27 16:59:54', NULL, NULL, 1);
INSERT INTO `system_user_role` (`id`, `user_id`, `role_id`, `create_user_id`, `create_time`, `update_user_id`, `update_time`, `del_flag`, `remark`, `tenant_id`) VALUES (3, 1, 1, 1, '2026-05-28 09:42:34', 1, '2026-05-28 09:42:34', NULL, NULL, 1);
INSERT INTO `system_user_role` (`id`, `user_id`, `role_id`, `create_user_id`, `create_time`, `update_user_id`, `update_time`, `del_flag`, `remark`, `tenant_id`) VALUES (4, 1, 1, 1, '2026-05-28 09:42:53', 1, '2026-05-28 09:42:53', NULL, NULL, 1);
INSERT INTO `system_user_role` (`id`, `user_id`, `role_id`, `create_user_id`, `create_time`, `update_user_id`, `update_time`, `del_flag`, `remark`, `tenant_id`) VALUES (5, 1, 1, 1, '2026-05-28 09:43:03', 1, '2026-05-28 09:43:03', NULL, NULL, 1);
INSERT INTO `system_user_role` (`id`, `user_id`, `role_id`, `create_user_id`, `create_time`, `update_user_id`, `update_time`, `del_flag`, `remark`, `tenant_id`) VALUES (6, 1, 1, 1, '2026-05-28 09:43:51', 1, '2026-05-28 09:43:51', NULL, NULL, 1);
INSERT INTO `system_user_role` (`id`, `user_id`, `role_id`, `create_user_id`, `create_time`, `update_user_id`, `update_time`, `del_flag`, `remark`, `tenant_id`) VALUES (7, 1, 1, 1, '2026-05-28 09:49:44', 1, '2026-05-28 09:49:44', NULL, NULL, 1);
INSERT INTO `system_user_role` (`id`, `user_id`, `role_id`, `create_user_id`, `create_time`, `update_user_id`, `update_time`, `del_flag`, `remark`, `tenant_id`) VALUES (8, 1, 1, 1, '2026-05-28 09:49:52', 1, '2026-05-28 09:49:52', NULL, NULL, 1);
INSERT INTO `system_user_role` (`id`, `user_id`, `role_id`, `create_user_id`, `create_time`, `update_user_id`, `update_time`, `del_flag`, `remark`, `tenant_id`) VALUES (9, 1, 1, 1, '2026-05-28 09:50:00', 1, '2026-05-28 09:50:00', NULL, NULL, 1);
INSERT INTO `system_user_role` (`id`, `user_id`, `role_id`, `create_user_id`, `create_time`, `update_user_id`, `update_time`, `del_flag`, `remark`, `tenant_id`) VALUES (10, 1, 1, 1, '2026-05-28 09:50:08', 1, '2026-05-28 09:50:08', NULL, NULL, 1);
INSERT INTO `system_user_role` (`id`, `user_id`, `role_id`, `create_user_id`, `create_time`, `update_user_id`, `update_time`, `del_flag`, `remark`, `tenant_id`) VALUES (11, 1, 1, 1, '2026-05-28 09:50:19', 1, '2026-05-28 09:50:19', NULL, NULL, 1);
INSERT INTO `system_user_role` (`id`, `user_id`, `role_id`, `create_user_id`, `create_time`, `update_user_id`, `update_time`, `del_flag`, `remark`, `tenant_id`) VALUES (12, 1, 1, 1, '2026-05-28 09:51:54', 1, '2026-05-28 09:51:54', NULL, NULL, 1);
INSERT INTO `system_user_role` (`id`, `user_id`, `role_id`, `create_user_id`, `create_time`, `update_user_id`, `update_time`, `del_flag`, `remark`, `tenant_id`) VALUES (13, 1, 1, 1, '2026-05-28 10:24:05', 1, '2026-05-28 10:24:05', NULL, NULL, 1);
INSERT INTO `system_user_role` (`id`, `user_id`, `role_id`, `create_user_id`, `create_time`, `update_user_id`, `update_time`, `del_flag`, `remark`, `tenant_id`) VALUES (14, 1, 1, 1, '2026-05-28 10:24:09', 1, '2026-05-28 10:24:09', 0, NULL, 1);
COMMIT;

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
  `request_param` text DEFAULT NULL COMMENT '请求参数',
  `response_result` text DEFAULT NULL COMMENT '响应结果',
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
  KEY `idx_module` (`module`),
  KEY `idx_operate_type` (`operate_type`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_create_user_id` (`create_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='操作日志表';

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
  KEY `idx_username` (`username`),
  KEY `idx_login_time` (`login_time`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='登录日志表';

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
  `account_count` int NOT NULL DEFAULT 0 COMMENT '账号额度（0=不限）',
  `status` char(2) NOT NULL DEFAULT '0' COMMENT '状态 0-正常 1-停用',
  `expire_time` datetime NOT NULL COMMENT '过期时间',
  `create_user_id` bigint NOT NULL COMMENT '创建人id',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_user_id` bigint NOT NULL COMMENT '更新人id',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除标识 0-未删除 null-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_domain` (`domain`, `del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='租户表';

-- ----------------------------
-- Table structure for system_notice
-- ----------------------------
DROP TABLE IF EXISTS `system_notice`;
CREATE TABLE `system_notice` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `title` varchar(100) NOT NULL COMMENT '标题',
  `content` text COMMENT '内容',
  `type` tinyint NOT NULL COMMENT '类型 1-通知 2-消息 3-待办',
  `scope` tinyint NOT NULL DEFAULT 1 COMMENT '范围 1-全部 2-指定部门 3-指定用户',
  `scope_dept_ids` varchar(1024) DEFAULT NULL COMMENT '范围部门id列表，逗号分隔',
  `scope_user_ids` varchar(1024) DEFAULT NULL COMMENT '范围用户id列表，逗号分隔',
  `sender_id` bigint NOT NULL COMMENT '发送人id',
  `sender_name` varchar(32) DEFAULT NULL COMMENT '发送人姓名',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态 0-草稿 1-已发布 2-已撤回',
  `publish_time` datetime DEFAULT NULL COMMENT '发布时间',
  `create_user_id` bigint DEFAULT NULL COMMENT '创建人id',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_user_id` bigint DEFAULT NULL COMMENT '更新人id',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除标识 0-未删除 null-已删除',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户id',
  PRIMARY KEY (`id`),
  KEY `idx_tenant_type_status` (`tenant_id`, `type`, `status`),
  KEY `idx_sender_id` (`sender_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统通知表';

-- ----------------------------
-- Table structure for system_notice_user
-- ----------------------------
DROP TABLE IF EXISTS `system_notice_user`;
CREATE TABLE `system_notice_user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `notice_id` bigint NOT NULL COMMENT '通知id',
  `user_id` bigint NOT NULL COMMENT '用户id',
  `read_status` tinyint NOT NULL DEFAULT 0 COMMENT '已读状态 0-未读 1-已读',
  `read_time` datetime DEFAULT NULL COMMENT '已读时间',
  `create_user_id` bigint DEFAULT NULL COMMENT '创建人id',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_user_id` bigint DEFAULT NULL COMMENT '更新人id',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除标识 0-未删除 null-已删除',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户id',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_notice_user` (`notice_id`, `user_id`, `del_flag`),
  KEY `idx_user_read` (`user_id`, `read_status`),
  KEY `idx_notice_id` (`notice_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统通知用户关联表';

SET FOREIGN_KEY_CHECKS = 1;
