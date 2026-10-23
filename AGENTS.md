# AGENTS.md - XD11CC Single 项目说明

## 项目概述

**xd11cc-single** 是一个基于 Spring Boot 2.7.x + Java 8 的多租户 SaaS 单体后端框架，定位是开箱即用的企业级后端解决方案。

## 架构要点

### 多租户隔离
- 共享数据库 + 行级隔离（`TenantFilter` 解析域名/Header → `TransmittableThreadLocal` 传播 → `MyBatis-Plus TenantLineInnerInterceptor` 追加 `WHERE tenant_id = ?`）
- `@TenantIgnore` 跳过租户过滤（如 system_menu、system_dict_type）

### 认证授权
- Spring Security + JWT（无状态），JWT 作为 Redis Key 索引，服务端主动控制会话生命周期
- OAuth2 社交登录（JustAuth，20+ 平台）
- 白名单免认证：security.permit-all-urls

### AOP 自定义注解（核心扩展点）
| 注解 | 切面 | 作用 |
|------|------|------|
| `@DataSource` | DataSourceAspect | 动态数据源切换 |
| `@RedisLock` (原 @RedisLock，改名为 RedisLock?) | RedisLockAspect | Redisson RLock，SpEL Key，ALL/KEY 模式 |
| `@RequestLimit` (改名为 @RequestLimit?) | RequestLimitAspect | Redisson RRateLimiter，令牌桶 |
| `@DataScope` | DataScopeAspect | 部门级/个人级数据权限过滤 |
| `@OperateLog` | OperateLogAspect | 异步记录操作日志 |

### 持久层
- MyBatis-Plus 3.5.3.1（自动填充 + 多租户拦截 + 逻辑删除 `del_flag`）
- PageHelper 1.4.6 分页
- MapStruct 1.6.3 对象转换（entity/convert/ 下的 Converter）

### 中间件
- Redis + Redisson（分布式锁/限流/会话）
- Druid（SQL 监控 + 慢查询告警 + 动态多数据源）
- RabbitMQ（异步解耦）
- Netty WebSocket（独立端口 12001）
- XXL-JOB（分布式定时）+ Quartz（本地调度）
- MinIO（对象存储）
- Spire.PDF（PDF 转 Word）

### 工具库
- Hutool / Fastjson2 / EasyExcel / TTL / jjwt / Knife4j + Swagger

## 包结构

```
com.xd11cc.single
├── common/            # 全项目共享的基础能力
│   ├── annotation/    # @RedisLock @RequestLimit @DataScope @OperateLog 等
│   ├── aspectj/       # 对应切面实现
│   ├── base/          # ArrayValuable 等通用基类/接口
│   ├── auth/          # OAuth2 授权（AuthRequestFactory 等）
│   ├── concurrency/   # 线程池配置
│   ├── context/       # TenantContextHolder / DynamicDataSourceContextHolder
│   ├── docs/          # Swagger/Knife4j
│   ├── event/         # Spring 事件（NoticeEvent 等）
│   ├── exception/     # ServiceException / ErrorCode
│   ├── initializer/   # 启动初始化
│   ├── mq/            # RabbitMQ 配置
│   ├── properties/    # @ConfigurationProperties
│   ├── security/      # Spring Security 配置
│   └── web/           # Filter / Interceptor / 全局异常处理 / CORS
├── infrastructure/    # 中间件与基础设施连接
│   ├── datasource/    # Druid + 动态数据源
│   ├── mybatis/       # MyBatis-Plus 配置
│   ├── netty/         # WebSocket 服务端
│   ├── redis/         # RedisCache / Redisson
│   ├── schedule/      # XXL-JOB
│   └── storage/       # MinIO
├── modules/           # 业务模块（垂切）
│   ├── pay/           # 支付（PayClient 接口 + 支付宝/微信实现）
│   └── workflow/      # 工作流引擎装配与业务扩展点
├── constants/         # CacheConstants/SecurityConstants
├── controller/        # REST 控制器
├── convert/           # MapStruct 对象转换器
├── entity/            # base/domain/dto/vo
├── enums/             # 业务枚举
├── mapper/            # MyBatis-Plus Mapper
├── service/           # 接口 + impl
└── utils/             # SecurityUtils/PageUtils/JsonUtils/DateUtils 等
```

**分层依赖约定**（重构后已验证）：

```
controller → service → mapper → entity
     ↓           ↓
   common / infrastructure / modules
```

- `entity`、`utils`、`enums` 只依赖 `common`（不得依赖 `infrastructure` 或 `modules`）
- `common` 与 `infrastructure` 之间保持单向，不得反向依赖 `service`/`controller`
- 业务模块放 `modules/<模块名>/`，不要塞进 `common` 或 `infrastructure`

## 编码约定

1. **Java 8 语法**，不要求启用 record/switch 表达式等 Java 17+ 特性
2. **包名**：`com.xd11cc.single`，模块子目录（config/controller/service/...）
3. **实体命名**：Domain 类结尾不加 `DO`（如 `SystemUser`），但 Base 层有 `BaseTenantDO` 通用父类
4. **Vo/Dto**：`vo` 放响应/请求，`dto` 放内部流转
5. **异常**：统一 `ServiceException` + `ErrorCode` 枚举（格式 `类型_模块_编号`）
6. **分页结果**：`PageResult<T>` 泛型封装，配合 `PageUtils.page()` 统一逻辑
7. **断言**：`AssertUtils` 校验失败抛异常
8. **脱敏**：`MaskUtils`（手机/身份证/邮箱）
9. **异步上下文传播**：关键线程池配合 `DelegatingSecurityContextExecutor` 传递安全上下文，Tenant 用 `TransmittableThreadLocal`
10. **Swagger**：接口地址前缀 `/xd11cc`，knif4j 访问 `/doc.html`

## 快捷命令

```bash
# 本地 dev 启动（需先创建 xd11cc_single 库并导入 SQL）
mvn spring-boot:run

# 跳过测试打包
mvn clean package -DskipTests
```

## 注意事项

- 主类是 `SingleController.java`（命名非 Application，不要误删）
- `DataSourceAutoConfiguration` 已排除（动态数据源）
- `spring.main.allow-circular-references=true` 已开启循环依赖支持
- URL 匹配策略强制 `ant_path_matcher`（与 Security 兼容）
- 开发中遇到的 CI/CD、部署、Git 操作，需用户明确指令后执行（严禁 push --force 等破坏性操作）
- 代码变更不要修改项目的公共文档路径约定
