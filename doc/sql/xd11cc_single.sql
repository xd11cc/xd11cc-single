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

 Date: 28/04/2026 09:10:34
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for system_dept
-- ----------------------------
DROP TABLE IF EXISTS `system_dept`;
CREATE TABLE `system_dept` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `parent_id` bigint NOT NULL DEFAULT '0' COMMENT '父部门id',
  `dept_code` varchar(20) NOT NULL COMMENT '部门编码',
  `dept_name` varchar(20) NOT NULL COMMENT '部门名称',
  `leader_id` bigint NOT NULL COMMENT '部门负责人',
  `sort` int NOT NULL COMMENT '排序',
  `status` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '部门状态，字典类型system_status',
  `create_user_id` bigint NOT NULL COMMENT '创建人id',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_user_id` bigint NOT NULL COMMENT '更新人id',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标识 0-未删除 1-已删除',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '备注',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户id',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='部门表';

-- ----------------------------
-- Records of system_dept
-- ----------------------------
BEGIN;
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
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标识 0-未删除 1-已删除',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '备注',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='部门岗位表';

-- ----------------------------
-- Records of system_dept_post
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for system_dict_data
-- ----------------------------
DROP TABLE IF EXISTS `system_dict_data`;
CREATE TABLE `system_dict_data` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `dict_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '字典类型',
  `label` varchar(20) NOT NULL COMMENT '标签',
  `value` varchar(20) NOT NULL COMMENT '键值',
  `css_class` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '键值颜色',
  `list_class` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '背景颜色',
  `sort` int NOT NULL COMMENT '排序',
  `status` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '状态，字典system_status',
  `create_user_id` bigint NOT NULL COMMENT '创建人id',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_user_id` bigint NOT NULL COMMENT '更新人id',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `del_flag` tinyint DEFAULT '0' COMMENT '删除标识 0-未删除 1-已删除',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户id',
  PRIMARY KEY (`id`),
  UNIQUE KEY `label` (`dict_type`,`label`,`del_flag`) USING BTREE
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
  `dict_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '字典类型',
  `dict_name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '字典名称',
  `create_user_id` bigint NOT NULL COMMENT '创建人id',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_user_id` bigint NOT NULL COMMENT '更新人id',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `del_flag` tinyint DEFAULT '0' COMMENT '删除标识 0-未删除 null-已删除',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '备注',
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
  `job_name` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '任务名称',
  `job_group` char(2) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '任务组名',
  `invoke_target` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '调度目标字符串',
  `cron_expression` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'cron执行表达式',
  `execution_policy` char(2) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '执行策略',
  `CONCURRENT` char(2) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '是否允许并发执行',
  `STATUS` char(2) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '状态',
  `create_user_id` bigint NOT NULL COMMENT '创建人id',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_user_id` bigint NOT NULL COMMENT '更新人id',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标识 0-未删除 1-已删除',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '备注',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统任务表';

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
  `job_name` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '任务名称',
  `job_group` char(2) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '任务组名',
  `invoke_target` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '调度目标字符串',
  `job_message` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '日志信息',
  `STATUS` char(2) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '执行状态',
  `create_user_id` bigint NOT NULL COMMENT '创建人id',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_user_id` bigint NOT NULL COMMENT '更新人id',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标识 0-未删除 1-已删除',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '备注',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统任务日志表';

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
  `path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '路由路径（如/user/list）',
  `component` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '组件路径',
  `route_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '路由名称',
  `query` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '路由参数',
  `icon` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '菜单图标',
  `menu_type` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '菜单类型，字典类型system_menu_type',
  `status` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '菜单状态，字典类型system_status',
  `permission` varchar(255) DEFAULT NULL COMMENT '权限字符',
  `visible` tinyint NOT NULL DEFAULT '0' COMMENT '是否隐藏，0-否 1-是',
  `create_user_id` bigint NOT NULL COMMENT '创建人id',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_user_id` bigint NOT NULL COMMENT '更新人id',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `del_flag` tinyint DEFAULT '0' COMMENT '删除标识 0-未删除 1-已删除',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `path` (`path`,`del_flag`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='菜单表';

-- ----------------------------
-- Records of system_menu
-- ----------------------------
BEGIN;
INSERT INTO `system_menu` (`id`, `parent_id`, `menu_name`, `sort`, `path`, `component`, `route_name`, `query`, `icon`, `menu_type`, `status`, `permission`, `visible`, `create_user_id`, `create_time`, `update_user_id`, `update_time`, `del_flag`, `remark`) VALUES (1, NULL, '系统管理', 1, 'system', 'Layout', 'System', NULL, 'Setting', 'M', '0', NULL, 0, 1, '2026-01-20 18:03:15', 1, '2026-01-20 18:03:21', 0, NULL);
INSERT INTO `system_menu` (`id`, `parent_id`, `menu_name`, `sort`, `path`, `component`, `route_name`, `query`, `icon`, `menu_type`, `status`, `permission`, `visible`, `create_user_id`, `create_time`, `update_user_id`, `update_time`, `del_flag`, `remark`) VALUES (2, 1, '菜单管理', 1, 'menu', 'system/menu/index', 'Menu', NULL, 'Menu', 'C', '0', NULL, 0, 1, '2026-01-20 22:46:41', 1, '2026-01-20 22:46:46', 0, NULL);
INSERT INTO `system_menu` (`id`, `parent_id`, `menu_name`, `sort`, `path`, `component`, `route_name`, `query`, `icon`, `menu_type`, `status`, `permission`, `visible`, `create_user_id`, `create_time`, `update_user_id`, `update_time`, `del_flag`, `remark`) VALUES (3, 1, '字典管理', 2, 'dict', 'system/dict/index', 'Dict', NULL, 'Notebook', 'C', '0', NULL, 0, 1, '2026-01-22 10:28:08', 1, '2026-01-22 10:28:14', 0, NULL);
INSERT INTO `system_menu` (`id`, `parent_id`, `menu_name`, `sort`, `path`, `component`, `route_name`, `query`, `icon`, `menu_type`, `status`, `permission`, `visible`, `create_user_id`, `create_time`, `update_user_id`, `update_time`, `del_flag`, `remark`) VALUES (4, 1, '字典数据', 3, 'dict-data', 'system/dict/data', 'DictData', NULL, NULL, 'C', '0', NULL, 1, 1, '2026-01-28 17:26:27', 1, '2026-01-28 17:26:33', 0, NULL);
INSERT INTO `system_menu` (`id`, `parent_id`, `menu_name`, `sort`, `path`, `component`, `route_name`, `query`, `icon`, `menu_type`, `status`, `permission`, `visible`, `create_user_id`, `create_time`, `update_user_id`, `update_time`, `del_flag`, `remark`) VALUES (5, 3, '字典查询', 4, NULL, NULL, NULL, NULL, NULL, 'B', '0', 'system:dictType:list', 0, 1, '2026-02-11 11:07:38', 1, '2026-03-17 10:27:57', 0, NULL);
INSERT INTO `system_menu` (`id`, `parent_id`, `menu_name`, `sort`, `path`, `component`, `route_name`, `query`, `icon`, `menu_type`, `status`, `permission`, `visible`, `create_user_id`, `create_time`, `update_user_id`, `update_time`, `del_flag`, `remark`) VALUES (6, NULL, '系统监控', 3, 'monitor', 'Layout', 'Monitor', NULL, 'Monitor', 'M', '0', NULL, 0, 1, '2026-02-12 14:31:43', 1, '2026-04-20 15:02:06', 0, NULL);
INSERT INTO `system_menu` (`id`, `parent_id`, `menu_name`, `sort`, `path`, `component`, `route_name`, `query`, `icon`, `menu_type`, `status`, `permission`, `visible`, `create_user_id`, `create_time`, `update_user_id`, `update_time`, `del_flag`, `remark`) VALUES (7, 3, '字典新增', 1, NULL, NULL, NULL, NULL, NULL, 'B', '0', 'system:dictType:add', 0, 1, '2026-02-12 14:45:05', 1, '2026-03-17 10:28:17', 0, NULL);
INSERT INTO `system_menu` (`id`, `parent_id`, `menu_name`, `sort`, `path`, `component`, `route_name`, `query`, `icon`, `menu_type`, `status`, `permission`, `visible`, `create_user_id`, `create_time`, `update_user_id`, `update_time`, `del_flag`, `remark`) VALUES (8, 3, '字典修改', 3, NULL, NULL, NULL, NULL, NULL, 'B', '0', 'systm:dictType:update', 0, 1, '2026-02-12 14:45:38', 1, '2026-03-17 10:28:05', 0, NULL);
INSERT INTO `system_menu` (`id`, `parent_id`, `menu_name`, `sort`, `path`, `component`, `route_name`, `query`, `icon`, `menu_type`, `status`, `permission`, `visible`, `create_user_id`, `create_time`, `update_user_id`, `update_time`, `del_flag`, `remark`) VALUES (9, 3, '字典删除', 2, NULL, NULL, NULL, NULL, NULL, 'B', '0', 'system:dictType:delete', 0, 1, '2026-02-12 14:46:05', 1, '2026-03-17 10:28:10', 0, NULL);
INSERT INTO `system_menu` (`id`, `parent_id`, `menu_name`, `sort`, `path`, `component`, `route_name`, `query`, `icon`, `menu_type`, `status`, `permission`, `visible`, `create_user_id`, `create_time`, `update_user_id`, `update_time`, `del_flag`, `remark`) VALUES (10, 6, '数据管理', 1, 'druid', 'monitor/druid/index', 'Druid', NULL, 'DataLine', 'C', '0', NULL, 0, 1, '2026-02-13 09:30:16', 1, '2026-02-13 09:30:16', 0, NULL);
INSERT INTO `system_menu` (`id`, `parent_id`, `menu_name`, `sort`, `path`, `component`, `route_name`, `query`, `icon`, `menu_type`, `status`, `permission`, `visible`, `create_user_id`, `create_time`, `update_user_id`, `update_time`, `del_flag`, `remark`) VALUES (11, 3, '字典导出', 5, NULL, NULL, NULL, NULL, NULL, 'B', '0', 'system:dictType:export', 0, 1, '2026-03-17 10:47:57', 1, '2026-03-17 10:47:57', 0, NULL);
INSERT INTO `system_menu` (`id`, `parent_id`, `menu_name`, `sort`, `path`, `component`, `route_name`, `query`, `icon`, `menu_type`, `status`, `permission`, `visible`, `create_user_id`, `create_time`, `update_user_id`, `update_time`, `del_flag`, `remark`) VALUES (12, NULL, '系统工具', 2, 'tool', 'Layout', 'Tool', NULL, 'ShoppingBag', 'M', '0', NULL, 0, 1, '2026-04-20 15:01:32', 1, '2026-04-20 15:17:35', 0, NULL);
INSERT INTO `system_menu` (`id`, `parent_id`, `menu_name`, `sort`, `path`, `component`, `route_name`, `query`, `icon`, `menu_type`, `status`, `permission`, `visible`, `create_user_id`, `create_time`, `update_user_id`, `update_time`, `del_flag`, `remark`) VALUES (13, 12, '代码生成', 1, 'generateCode', 'tool/generateCode/index', 'GenerateCode', NULL, 'Connection', 'C', '0', NULL, 0, 1, '2026-04-20 15:04:39', 1, '2026-04-21 16:24:30', 0, NULL);
COMMIT;

-- ----------------------------
-- Table structure for system_post
-- ----------------------------
DROP TABLE IF EXISTS `system_post`;
CREATE TABLE `system_post` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `post_code` varchar(20) NOT NULL COMMENT '岗位编码',
  `post_name` varchar(20) NOT NULL COMMENT '岗位名称',
  `status` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '岗位状态，字典类型system_status',
  `create_user_id` bigint NOT NULL COMMENT '创建人id',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_user_id` bigint NOT NULL COMMENT '更新人id',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标识 0-未删除 1-已删除',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '备注',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='岗位表';

-- ----------------------------
-- Records of system_post
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for system_role
-- ----------------------------
DROP TABLE IF EXISTS `system_role`;
CREATE TABLE `system_role` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `role_code` varchar(20) NOT NULL COMMENT '角色编码',
  `role_name` varchar(20) NOT NULL COMMENT '角色名称',
  `status` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '角色状态，字典类型system_status',
  `create_user_id` bigint NOT NULL COMMENT '创建人id',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_user_id` bigint NOT NULL COMMENT '更新人id',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标识 0-未删除 1-已删除',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '备注',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色表';

-- ----------------------------
-- Records of system_role
-- ----------------------------
BEGIN;
COMMIT;

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
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标识 0-未删除 1-已删除',
  `remark` varchar(255) NOT NULL COMMENT '备注',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色菜单表';

-- ----------------------------
-- Records of system_role_menu
-- ----------------------------
BEGIN;
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
  `sex` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '性别，字典类型system_logic_status',
  `phone` varchar(11) DEFAULT NULL COMMENT '手机号码',
  `email` varchar(20) DEFAULT NULL COMMENT '邮箱',
  `dept_id` bigint NOT NULL COMMENT '部门id',
  `dept_name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '部门名称',
  `post_id` bigint NOT NULL COMMENT '岗位id',
  `post_name` varchar(20) NOT NULL COMMENT '岗位名称',
  `status` char(2) NOT NULL COMMENT '账号状态，字典类型system_enable_status',
  `head_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '头像路径',
  `create_user_id` bigint NOT NULL COMMENT '创建人id',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_user_id` bigint NOT NULL COMMENT '更新人id',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标识 0-未删除 1-已删除',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '备注',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户表';

-- ----------------------------
-- Records of system_user
-- ----------------------------
BEGIN;
INSERT INTO `system_user` (`id`, `username`, `password`, `nickname`, `id_card`, `sex`, `phone`, `email`, `dept_id`, `dept_name`, `post_id`, `post_name`, `status`, `head_url`, `create_user_id`, `create_time`, `update_user_id`, `update_time`, `del_flag`, `remark`, `tenant_id`) VALUES (1, 'admin', '$2a$10$x34vGZit4tN/ftY6UpswsuQhfND.F7.fU291viOXwRZe8RuhdiThO', 'xd11cc', NULL, '0', NULL, NULL, 1, '测试', 1, '测试', '0', NULL, 1, '2025-07-17 09:18:26', 1, '2025-07-17 09:18:31', 0, NULL, 1);
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
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标识 0-未删除 1-已删除',
  `remark` varchar(255) NOT NULL COMMENT '备注',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户角色表';

-- ----------------------------
-- Records of system_user_role
-- ----------------------------
BEGIN;
COMMIT;

SET FOREIGN_KEY_CHECKS = 1;
