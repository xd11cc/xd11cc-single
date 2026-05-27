-- ============================================================
-- 通用建表模板（多租户 + 逻辑删除 + 审计字段）
-- 使用方式：复制后修改表名、业务字段、索引部分
-- ============================================================

DROP TABLE IF EXISTS `模块_业务表名`;
CREATE TABLE `模块_业务表名` (
  -- ==================== 主键 ====================
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',

  -- ==================== 业务字段（按需修改） ====================
  `name` varchar(50) NOT NULL COMMENT '名称',
  `code` varchar(50) NOT NULL COMMENT '编码',
  `status` char(2) NOT NULL DEFAULT '0' COMMENT '状态，字典类型system_status',
  `sort` int DEFAULT '0' COMMENT '排序',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',

  -- ==================== 审计字段（固定） ====================
  `create_user_id` bigint NOT NULL COMMENT '创建人id',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_user_id` bigint NOT NULL COMMENT '更新人id',
  `update_time` datetime NOT NULL COMMENT '更新时间',

  -- ==================== 逻辑删除（固定） ====================
  -- 正常=0，删除=NULL（NULL不参与唯一索引判重，允许删除后重建同名数据）
  `del_flag` tinyint DEFAULT '0' COMMENT '删除标识 0-未删除 NULL-已删除',

  -- ==================== 多租户（固定） ====================
  `tenant_id` bigint DEFAULT NULL COMMENT '租户id',

  -- ==================== 索引 ====================
  PRIMARY KEY (`id`),
  -- 唯一索引必须包含 del_flag（逻辑删除后允许重建）
  -- 多租户表必须包含 tenant_id（租户隔离）
  UNIQUE KEY `uk_code_tenant` (`code`, `tenant_id`, `del_flag`)
  -- 普通索引按查询需求添加
  -- KEY `idx_xxx` (`xxx`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='业务表';


-- ============================================================
-- 树形结构表模板（部门/菜单等有父子关系的表）
-- ============================================================

DROP TABLE IF EXISTS `模块_树形表名`;
CREATE TABLE `模块_树形表名` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',

  -- ==================== 树形字段（固定） ====================
  `parent_id` bigint DEFAULT NULL COMMENT '父级id，顶级为NULL',

  -- ==================== 业务字段 ====================
  `name` varchar(50) NOT NULL COMMENT '名称',
  `code` varchar(50) NOT NULL COMMENT '编码',
  `sort` int NOT NULL DEFAULT '0' COMMENT '排序',
  `status` char(2) NOT NULL DEFAULT '0' COMMENT '状态，字典类型system_status',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',

  -- ==================== 审计字段 ====================
  `create_user_id` bigint NOT NULL COMMENT '创建人id',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_user_id` bigint NOT NULL COMMENT '更新人id',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `del_flag` tinyint DEFAULT '0' COMMENT '删除标识 0-未删除 NULL-已删除',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户id',

  -- ==================== 索引 ====================
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code_tenant` (`code`, `tenant_id`, `del_flag`),
  KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='树形业务表';


-- ============================================================
-- 关联表模板（用户角色、角色菜单、部门岗位等中间表）
-- ============================================================

DROP TABLE IF EXISTS `模块_关联表名`;
CREATE TABLE `模块_关联表名` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',

  -- ==================== 关联字段（按需修改） ====================
  `主表_id` bigint NOT NULL COMMENT '主表id',
  `从表_id` bigint NOT NULL COMMENT '从表id',

  -- ==================== 审计字段 ====================
  `create_user_id` bigint NOT NULL COMMENT '创建人id',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_user_id` bigint NOT NULL COMMENT '更新人id',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `del_flag` tinyint DEFAULT '0' COMMENT '删除标识 0-未删除 NULL-已删除',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户id',

  -- ==================== 索引 ====================
  PRIMARY KEY (`id`),
  KEY `idx_主表_id` (`主表_id`),
  KEY `idx_从表_id` (`从表_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='关联表';
