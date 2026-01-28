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

 Date: 27/01/2026 17:21:36
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
  `status` char(2) NOT NULL COMMENT '部门状态，字典类型system_status',
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
-- Table structure for system_dict_data
-- ----------------------------
DROP TABLE IF EXISTS `system_dict_data`;
CREATE TABLE `system_dict_data` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `type` varchar(20) NOT NULL COMMENT '字典数据类型',
  `label` varchar(20) NOT NULL COMMENT '标签',
  `value` varchar(20) NOT NULL COMMENT '键值',
  `value_color` varchar(20) DEFAULT NULL COMMENT '键值颜色',
  `background_color` varchar(20) DEFAULT NULL COMMENT '背景颜色',
  `sort` int NOT NULL COMMENT '排序',
  `create_user_id` bigint NOT NULL COMMENT '创建人id',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_user_id` bigint NOT NULL COMMENT '更新人id',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标识 0-未删除 1-已删除',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='字典数据表';

-- ----------------------------
-- Table structure for system_dict_type
-- ----------------------------
DROP TABLE IF EXISTS `system_dict_type`;
CREATE TABLE `system_dict_type` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `type` varchar(20) NOT NULL COMMENT '字典类型',
  `name` varchar(20) NOT NULL COMMENT '字典名称',
  `create_user_id` bigint NOT NULL COMMENT '创建人id',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_user_id` bigint NOT NULL COMMENT '更新人id',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标识 0-未删除 1-已删除',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='字典类型表';

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
  `status` char(2) NOT NULL COMMENT '菜单状态，字典类型system_status',
  `permission` varchar(255) DEFAULT NULL COMMENT '权限字符',
  `visible` tinyint NOT NULL DEFAULT '0' COMMENT '是否隐藏，0-否 1-是',
  `create_user_id` bigint NOT NULL COMMENT '创建人id',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_user_id` bigint NOT NULL COMMENT '更新人id',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标识 0-未删除 1-已删除',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `path` (`path`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='菜单表';

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
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标识 0-未删除 1-已删除',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '备注',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='岗位表';

-- ----------------------------
-- Table structure for system_role
-- ----------------------------
DROP TABLE IF EXISTS `system_role`;
CREATE TABLE `system_role` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `role_code` varchar(20) NOT NULL COMMENT '角色编码',
  `role_name` varchar(20) NOT NULL COMMENT '角色名称',
  `status` char(2) NOT NULL COMMENT '角色状态，字典类型system_status',
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
  `status` char(2) NOT NULL COMMENT '账号状态，字典类型system_status',
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

SET FOREIGN_KEY_CHECKS = 1;
