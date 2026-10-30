# AGENTS.md - XD11CC Single 项目说明

> 本文件是**约束与决策**：不能做什么、为什么、以及已经定过的事不要再翻。
> 「怎么做」的写法示例见 `doc/development-spec.md`，两边不重复。

## 项目概述

**xd11cc-single** 是一个基于 Spring Boot 2.7.x + Java 8 的多租户 SaaS 单体后端框架，定位是开箱即用的企业级后端解决方案。

## 架构要点

### 多租户隔离
- 共享数据库 + 行级隔离（`TenantFilter` 解析域名 → `TransmittableThreadLocal` 传播 → `MyBatis-Plus TenantLineInnerInterceptor` 追加 `WHERE tenant_id = ?`）
- 表级豁免走 `tenant.ignoreTables` 配置；方法级豁免用 `@TenantIgnore`

### 认证授权
- Spring Security + JWT（无状态），JWT 只承载会话 uuid，用户数据在 Redis，服务端可主动使会话失效
- 密码校验与错误次数锁定在 `PasswordAuthenticationProvider`（`UserDetailsService` 只负责加载用户）
- 第三方登录：JustAuth 1.16.7 底层支持 40+ 平台，**当前已接入 GitHub / Gitee**；新增平台 = `AuthExtendSource` 加一个常量 + `auth_client_config` 补一条数据
- 白名单免认证：`security.permit-all-urls`

### AOP 自定义注解（核心扩展点）
| 注解 | 切面 | 作用 |
|---|---|---|
| `@DataSource` | DataSourceAspect | 动态数据源切换 |
| `@RedisLock` | RedisLockAspect | Redisson RLock，SpEL Key |
| `@RateLimit` | RateLimitAspect | 接口限流，见「关键约束」 |
| `@DataScope` | DataScopeAspect | 部门级/个人级数据权限过滤 |
| `@OperateLog` | OperateLogAspect | 异步记录操作日志 |

### 持久层
- MyBatis-Plus 3.5.3.1（自动填充 + 多租户拦截 + 逻辑删除 `del_flag`）
- PageHelper 1.4.6 分页；MapStruct 1.6.3 对象转换

### 中间件
- Redis + Redisson（分布式锁/限流/会话）；Druid（监控 + 动态多数据源）
- RabbitMQ（异步解耦）；Netty WebSocket（独立端口 12001）
- XXL-JOB（分布式定时）+ Quartz（本地调度）；MinIO（对象存储）；Spire.PDF

### 工具库
Hutool / Fastjson2 / EasyExcel / TTL / jjwt / Swagger（springfox）

## 包结构

```
com.xd11cc.single
├── common/            # 全项目共享的基础能力
│   ├── annotation/    # @RedisLock @RateLimit @DataScope @OperateLog 等
│   ├── aspectj/       # 对应切面实现
│   ├── auth/          # OAuth2 授权（AuthRequestFactory 等）
│   ├── context/       # TenantContextHolder / PermissionContextHolder
│   ├── event/         # Spring 事件（NoticeEvent 等）
│   ├── exception/     # ServiceException / ErrorCode
│   ├── mq/            # RabbitMQ 收发与消费模板
│   ├── properties/    # @ConfigurationProperties
│   ├── ratelimit/     # 限流机制层（RateLimiter / KeyResolver / Spec）
│   ├── security/      # Spring Security 配置与认证提供者
│   └── web/           # Filter / Interceptor / 全局异常处理 / CORS
├── infrastructure/    # 中间件连接
│   ├── datasource/  mybatis/  netty/  redis/  schedule/  storage/
├── modules/           # 业务模块（垂切）
│   ├── pay/           # 支付（PayClient + 各渠道 adapter）
│   └── workflow/      # 工作流引擎装配与业务扩展点
├── constants/  controller/  convert/  entity/  enums/  mapper/  service/  utils/
```

**分层依赖**：

```
controller → service → mapper → entity
     ↓           ↓
   common / infrastructure / modules
```

## 关键约束

以下每一条都对应过一次真实事故或权衡，**新增代码不得违反**。理由写在这里，写法见 spec。

### 状态与枚举

1. **字典驱动的状态列一律 `char(2)` + Java `String`**，不用 `tinyint`、不用 `int`。
2. **`del_flag` 是唯一的 `tinyint`**（取值 `0` / `NULL`）。原因：`NULL` 不参与唯一索引判重，这是「删除后可重建同名数据」的实现基础，换类型会破坏该机制。
3. **`String` 状态比较禁用 `==` / `!=`**。它会退化为引用比较，条件恒真且编译器不报错。用 `Objects.equals` 或常量左置。
4. **枚举只放 code，不放中文文案**。文案归字典；两处各存一份必然分叉。

### 字典

5. **`system_dict_data` 的 `label` 是编码、`value` 是展示文案**（与字段名的直觉相反）。
   写反不会报错，只会得到「中文 → 编码」的反向表，表现为下拉框显示编码。
6. **新增状态列必须同时补字典**，否则前端只能硬编码文案。
7. **`sort` 每项必须不同且从 1 递增**。全为 0 时下拉顺序取决于主键，是偶然而非设计。

### 错误码

8. **格式 `10` + 模块两位 + 序号三位**（如 `1002009` = 工作流第 9 个）。
9. **新模块必须占用新的空段**，禁止寄生在已有段尾部——段内序号增长后必然撞号。
10. **段内序号追加到末尾，不复用已删除的编号。** 当前 16 段已分配：

| 段 | 模块 | 段 | 模块 | 段 | 模块 | 段 | 模块 |
|---|---|---|---|---|---|---|---|
| 01 | 认证安全 | 05 | 文件 | 09 | 部门 | 13 | 通知 |
| 02 | 工作流 | 06 | 代码生成 | 10 | 角色 | 14 | 分布式锁 |
| 03 | 字典 | 07 | 社交登录 | 11 | 岗位 | 15 | 定时任务 |
| 04 | 菜单 | 08 | 租户 | 12 | 系统配置 | 16 | 消息队列 |

### 多租户

11. **租户上下文必须早于任何数据库访问**。`getRequiredTenantId()` 在租户为空时直接抛异常，先查库后设租户会失败。
12. **表级豁免走 `ignoreTables` 配置，方法级豁免用 `@TenantIgnore`**，两者不要混用。当前忽略表：`system_menu`、`system_dict_type`、`system_login_log`。
13. **手写 SQL（XML 里的原生语句）不受租户拦截器保护**，必须自带租户条件，否则跨租户泄露。

### 逻辑删除

14. **删除是 `UPDATE ... SET del_flag = NULL`，不是物理删除。** MyBatis-Plus 查询会自动加 `del_flag = 0`，手写 SQL 和原生 `count(*)` 不会。

### SQL 脚本

15. **`doc/sql/` 三份文件职责不重叠**：`xd11cc_single.sql` 只有结构（禁止 INSERT）、`xd11cc_single_data.sql` 只有数据、`template.sql` 是建表模板。
16. **改表结构必须同步结构脚本**，以真实库为准修脚本，不要反过来。

### 消息队列

17. **发送只走 `MqMessageSender`**（内置信封字段、租户、事务提交后发送）。业务侧不应直接接触 `RabbitTemplate`。
18. **消费的业务方法内不要 `catch` 异常**，抛给模板统一处理重试 → 落表 → 死信。自己 catch 会绕过整套机制。
19. **队列与交换机只用 `Declarables` 声明**。`RabbitAdmin` 只收集 `Declarables`，单个 Bean 会被静默忽略。

### 限流

20. **两条策略刻意不同，不要统一**：

| 入口 | 超限 | 限流组件故障 | 理由 |
|---|---|---|---|
| HTTP（`@RateLimit`） | 抛 `TOO_MANY_REQUESTS` | **拒绝**（fail-closed） | 限流是安全边界，失去保护不该放行 |
| WebSocket（`WsRateLimiter`） | 静默丢弃 + 限频日志 | **放行**（fail-open） | 断连代价远大于丢一条消息 |

21. **限流桶的 key 必须带规格指纹**（`rate_limit:{身份}:{permits}/{window}s`）。否则改阈值后 Redisson 的 `trySetRate` 返回 `false`，新配额不生效且无提示。
22. **AOP 注解在同类自调用时不生效**（`@RateLimit`、`@RedisLock` 等）。内部路径需要限流/加锁请直接注入对应模块。

### 变更纪律

23. **改字段类型必须端到端核对**：DB 列类型 → 实体/VO/DTO → 枚举 code → 所有比较点 → MyBatis `eq`/`set` 传参 → Mapper XML 硬编码值 → **调用方传入的字面量（前端下拉值、表单默认值、`Number(...)` 转换）**。最后一项编译期和类型检查都发现不了。
24. **不为了统一风格批量改写存量代码**。批量重命名/删注释会淹没真正的功能改动；统一应在写新代码时执行，存量代码只在因功能被改动时顺带调整。
25. **出参走 `ResponseVO`，错误抛 `ServiceException`**。不自己拼响应体、不返回裸 Map。

## 编码约定

1. **Java 8 语法**，不要求 record/switch 表达式等 Java 17+ 特性
2. **包名** `com.xd11cc.single`，模块见上方包结构
3. **实体命名**：Domain 类结尾不加 `DO`（如 `SystemUser`），Base 层有 `BaseTenantDO` 通用父类
4. **Vo/Dto**：`vo` 放请求/响应，`dto` 放内部流转
5. **分页结果**：`PageResult<T>` + `PageUtils.page()`
6. **断言**：`AssertUtils` 校验失败抛异常
7. **脱敏**：`MaskUtils`（手机/身份证/邮箱）
8. **异步上下文传播**：关键线程池配合 `DelegatingSecurityContextExecutor`，Tenant 用 `TransmittableThreadLocal`
9. **Swagger**：接口前缀 `/xd11cc`，文档入口 `/swagger-ui.html`（springfox，未引入 knife4j）

## 常用命令

```bash
mvn -o test                      # 全量测试（当前 800+ 用例）
mvn -o spring-boot:run           # 本地 dev 启动（需先建库并导入 SQL）
mvn clean package -DskipTests    # 跳过测试打包
```

## 注意事项

- 主类是 `SingleController.java`（命名非 Application，不要误删）
- `DataSourceAutoConfiguration` 已排除（动态数据源）
- `spring.main.allow-circular-references=true` 已开启
- URL 匹配策略强制 `ant_path_matcher`（与 Security 兼容）
- 测试服务用完要关（10001 / 12001 端口），Docker 基础设施容器保持运行
- CI/CD、部署、Git 操作需用户明确指令后执行（严禁 `push --force` 等破坏性操作）
- 代码变更不要修改项目的公共文档路径约定
