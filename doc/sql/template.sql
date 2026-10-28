-- ============================================================
-- 建表模板（与当前数据库完全对齐，2026-10-28 依据运行库逐表核对）
--
-- 使用方式：复制对应范式，改表名、业务字段、索引列名
--
-- 全库统一约定
--   引擎/字符集 : ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
--   审计四件套  : create_user_id / create_time / update_user_id / update_time
--                 全部 NOT NULL，位置固定在业务字段之后
--   逻辑删除    : del_flag tinyint DEFAULT '0'，0=未删除，NULL=已删除
--   备注        : remark varchar(255) DEFAULT NULL，位于 del_flag 之后
--   多租户      : tenant_id bigint DEFAULT NULL，位于最后
--   唯一索引    : 命名 uk_*，列顺序固定为 (tenant_id, 业务唯一键, del_flag)
--   普通索引    : 命名 idx_*，列顺序同样 tenant_id 在前
--   排序字段    : sort int NOT NULL DEFAULT '0'
--   状态字段    : status char(2) NOT NULL，取值配合字典表（13 个状态列中 10 个用此型）
--                 若状态是数字编码而非字典值，用 tinyint（参考 mq_fail_message）
--                 注意：workflow_category(char(1))、workflow_expression_config(varchar(1))
--                 两处偏离了此约定，新表请勿沿用
--
-- 为什么唯一索引必须含 del_flag：
--   逻辑删除后 del_flag 变为 NULL，而 NULL 不参与唯一性判重，
--   因此删除后可重新创建同名数据，不会撞唯一键。
-- ============================================================


-- ============================================================
-- 范式一：业务表（租户隔离 + 逻辑删除 + 审计）
-- 参考实现：system_post / workflow_category
-- ============================================================

DROP TABLE IF EXISTS `模块_业务表名`;
CREATE TABLE `模块_业务表名` (
  -- 主键
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',

  -- 业务字段（按需修改）
  `xxx_code` varchar(64) NOT NULL COMMENT '业务编码，租户内唯一',
  `xxx_name` varchar(100) NOT NULL COMMENT '业务名称',
  `sort` int NOT NULL DEFAULT '0' COMMENT '显示排序，值越小越靠前',
  `status` char(2) NOT NULL DEFAULT '0' COMMENT '状态，字典类型system_status',

  -- 审计字段（固定，NOT NULL）
  `create_user_id` bigint NOT NULL COMMENT '创建人id',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_user_id` bigint NOT NULL COMMENT '更新人id',
  `update_time` datetime NOT NULL COMMENT '更新时间',

  -- 逻辑删除（固定）
  `del_flag` tinyint DEFAULT '0' COMMENT '删除标识 0-未删除 null-已删除',

  -- 备注
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',

  -- 多租户（固定，放最后）
  `tenant_id` bigint DEFAULT NULL COMMENT '租户id',

  PRIMARY KEY (`id`),
  -- 唯一键：tenant_id 在前，del_flag 在最后
  UNIQUE KEY `uk_tenant_xxx_code` (`tenant_id`,`xxx_code`,`del_flag`),
  -- 列表查询索引：覆盖「租户 + 状态 + 排序 + 主键 + 逻辑删除」
  KEY `idx_tenant_xxx_list` (`tenant_id`,`status`,`sort`,`id`,`del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='业务表';


-- ============================================================
-- 范式二：树形表（父子结构，租户隔离）
-- 参考实现：system_dept
-- ============================================================

DROP TABLE IF EXISTS `模块_树形表名`;
CREATE TABLE `模块_树形表名` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',

  -- 树形字段（固定，顶级为 NULL）
  `parent_id` bigint DEFAULT NULL COMMENT '父级id',

  -- 业务字段
  `xxx_code` varchar(50) NOT NULL COMMENT '业务编码',
  `xxx_name` varchar(50) NOT NULL COMMENT '业务名称',
  `sort` int NOT NULL DEFAULT '0' COMMENT '排序',
  `status` char(2) NOT NULL COMMENT '状态，字典类型system_status',

  -- 审计字段
  `create_user_id` bigint NOT NULL COMMENT '创建人id',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_user_id` bigint NOT NULL COMMENT '更新人id',
  `update_time` datetime NOT NULL COMMENT '更新时间',

  `del_flag` tinyint DEFAULT '0' COMMENT '删除标识 0-未删除 null-已删除',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户id',

  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_xxx_code` (`tenant_id`,`xxx_code`,`del_flag`),
  -- 树形展开索引：租户 + 父级
  KEY `idx_tenant_parent` (`tenant_id`,`parent_id`,`del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='树形业务表';


-- ============================================================
-- 范式三：关联表（多对多中间表，租户隔离）
-- 参考实现：system_user_role / system_role_menu / system_role_dept / system_dept_post
--
-- 注意：唯一约束是必须的，否则同一对关系可被重复插入。
--       现有四个关联表全部采用 (tenant_id, 主表_id, 从表_id, del_flag) 四列唯一键。
-- ============================================================

DROP TABLE IF EXISTS `模块_关联表名`;
CREATE TABLE `模块_关联表名` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',

  -- 关联字段
  `主表_id` bigint NOT NULL COMMENT '主表id',
  `从表_id` bigint NOT NULL COMMENT '从表id',

  -- 审计字段
  `create_user_id` bigint NOT NULL COMMENT '创建人id',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_user_id` bigint NOT NULL COMMENT '更新人id',
  `update_time` datetime NOT NULL COMMENT '更新时间',

  `del_flag` tinyint DEFAULT '0' COMMENT '删除标识 0-未删除 null-已删除',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户id',

  PRIMARY KEY (`id`),
  -- 唯一约束：防止重复关联（缺了这条会允许同一对关系重复插入）
  UNIQUE KEY `uk_tenant_主表_从表` (`tenant_id`,`主表_id`,`从表_id`,`del_flag`),
  -- 反向查询索引：按从表查主表时使用
  KEY `idx_tenant_从表` (`tenant_id`,`从表_id`,`del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='关联表';


-- ============================================================
-- 范式四：全局表（无租户隔离，所有租户共享同一份数据）
-- 参考实现：system_menu / system_dict_type / system_tenant
--
-- 判断依据：数据是否需要按租户区分。菜单、字典类型属于平台级定义，
--          所有租户共用，因此不加 tenant_id。
-- 注意：此类表的唯一索引不含 tenant_id。
-- ============================================================

DROP TABLE IF EXISTS `模块_全局表名`;
CREATE TABLE `模块_全局表名` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',

  -- 业务字段
  `xxx_code` varchar(64) NOT NULL COMMENT '业务编码，全局唯一',
  `xxx_name` varchar(100) NOT NULL COMMENT '业务名称',
  `sort` int NOT NULL DEFAULT '0' COMMENT '显示排序',
  `status` char(2) NOT NULL DEFAULT '0' COMMENT '状态，字典类型system_status',

  -- 审计字段
  `create_user_id` bigint NOT NULL COMMENT '创建人id',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_user_id` bigint NOT NULL COMMENT '更新人id',
  `update_time` datetime NOT NULL COMMENT '更新时间',

  `del_flag` tinyint DEFAULT '0' COMMENT '删除标识 0-未删除 null-已删除',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',

  PRIMARY KEY (`id`),
  -- 全局唯一键：无 tenant_id
  UNIQUE KEY `uk_xxx_code` (`xxx_code`,`del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='全局业务表';


-- ============================================================
-- 范式五：日志表（高频写入，查询以时间为主）
-- 参考实现：system_login_log / system_operate_log
--
-- 与业务表的差异：
--   1. 不设 remark（日志不写备注）
--   2. 若日志只追加不更新，可省略 update_user_id / update_time（参考 system_login_log）
--   3. 索引按「租户 + 时间 + 主键」组织，贴合分页查询
-- ============================================================

DROP TABLE IF EXISTS `模块_日志表名`;
CREATE TABLE `模块_日志表名` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',

  -- 业务字段
  `biz_type` char(2) NOT NULL COMMENT '业务类型',
  `status` char(2) NOT NULL DEFAULT '0' COMMENT '状态 0-成功 1-失败',
  `msg` varchar(500) DEFAULT NULL COMMENT '提示消息',

  -- 审计字段
  `create_user_id` bigint NOT NULL COMMENT '操作人id',
  `create_time` datetime NOT NULL COMMENT '操作时间',
  `update_user_id` bigint NOT NULL COMMENT '更新人id',
  `update_time` datetime NOT NULL COMMENT '更新时间',

  `del_flag` tinyint DEFAULT '0' COMMENT '删除标识 0-未删除 null-已删除',

  `tenant_id` bigint DEFAULT NULL COMMENT '租户id',

  PRIMARY KEY (`id`),
  -- 分页查询：租户 + 时间 + 主键（保证翻页稳定）
  KEY `idx_tenant_create_time` (`tenant_id`,`create_time`,`id`),
  KEY `idx_tenant_biz_type_status` (`tenant_id`,`biz_type`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='日志表';
