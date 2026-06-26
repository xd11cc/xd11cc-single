# XD11CC Single - 多租户 SaaS 后端框架

**[English](README_EN.md) | 中文**

![img.png](doc/img/img.png)

一个基于 Spring Boot 2.7 构建的生产级多租户 SaaS 后端框架，采用 Java 8 开发，提供认证授权、实时通讯、任务调度等开箱即用的企业级解决方案。

## 技术栈

| 层级 | 技术 | 版本 | 说明 |
|------|------|------|------|
| **基础框架** | Spring Boot | 2.7.18 | 应用框架 |
| **安全认证** | Spring Security + JWT | 5.7.x + 0.11.5 | 无状态认证，Redis 会话管理 |
| **社交登录** | JustAuth | 1.16.7 | OAuth2 第三方登录（GitHub/Google/微信等） |
| **ORM 框架** | MyBatis-Plus | 3.5.3.1 | 增强 CRUD + 自动填充 + 多租户拦截 |
| **分页插件** | PageHelper | 1.4.6 | 基于 ThreadLocal 的分页拦截 |
| **数据库** | MySQL | 8.0.33 | utf8mb4 + utf8mb4_0900_ai_ci |
| **连接池** | Druid | 1.2.24 | SQL 监控 + 慢查询检测 + 动态多数据源 |
| **缓存** | Redis + Redisson | 6.0+ / 3.36.0 | 分布式锁、限流、会话存储 |
| **消息队列** | RabbitMQ | 2.7.x | 异步解耦、事件通知 |
| **实时通讯** | Netty WebSocket | 4.1.x | 独立端口、长连接推送 |
| **任务调度** | XXL-JOB + Quartz | 2.4.0 / 2.7.x | 分布式定时任务 + 本地调度 |
| **对象存储** | MinIO | 8.6.0 | 兼容 S3 协议的分布式存储 |
| **支付集成** | 支付宝 SDK + 微信支付 SDK | 4.40.831 / 4.8.0 | 多渠道支付客户端抽象 |
| **接口文档** | Swagger2 + Knife4j | 2.9.2 / 3.0.3 | 在线 API 文档 |
| **容器化** | Docker + Docker Compose | - | 一键部署 |
| **工具库** | Hutool / Fastjson2 / MapStruct / EasyExcel / TTL | 5.8.16 / 2.0.39 / 1.6.3 / 3.3.2 / 2.13.2 | 实用工具集 |

## 功能特性

### 🏗️ 核心架构

- **多租户隔离** — 共享数据库 + 行级隔离方案，通过 `TenantFilter` 解析域名/Header 获取租户 ID，`TransmittableThreadLocal` 自动传播租户上下文至异步线程，`MyBatis-Plus TenantLineInnerInterceptor` 自动追加 `WHERE tenant_id = ?` 条件
- **动态数据源** — 运行时通过 `@DataSource` 注解 + AOP 切面切换数据源，支持读写分离场景
- **RBAC 权限体系** — 完整的用户、角色、菜单、部门、岗位权限模型，支持细粒度权限控制
- **数据权限控制** — `@DataScope` 注解实现部门级/个人级数据权限隔离，基于 AOP 动态追加部门过滤条件
- **代码生成器** — 基于 Freemarker 模板引擎快速生成 CRUD 代码（Controller/Service/Mapper/Entity/VO/DTO）

### 🔐 安全模块

- **JWT 认证** — 无状态 Token 认证，JWT 作为 Redis Key 索引，服务端主动控制会话生命周期（踢人、刷新权限）
- **OAuth2 社交登录** — 集成 JustAuth，支持 GitHub、Google、微信等 20+ 社交平台登录
- **接口限流** — `@RateLimit` 注解驱动，基于 Redisson RRateLimiter 实现令牌桶算法，支持 IP/用户/全局三种限流维度
- **RSA 加密** — 密码传输端到端加密，防止中间人窃取
- **验证码** — 图形验证码 + Redis TTL 过期机制，防暴力破解
- **CORS 跨域** — 统一跨域配置，支持白名单域名控制

### 📡 中间件集成

- **WebSocket 推送** — 基于 Netty 独立端口（12001）提供 WebSocket 服务，支持 Token 认证、心跳检测（30s）、空闲连接清理
- **消息队列** — RabbitMQ 集成，生产者 Confirm + Return 回调保证消息不丢，消费者手动 ACK 确保可靠消费
- **分布式调度** — XXL-JOB 集成，支持分片广播、故障转移、失败重试等企业级调度能力
- **本地调度** — Quartz 集成，支持内存模式 + JDBC 持久化双模式，`AbstractQuartzJob` 抽象基类封装执行上下文（租户 ID、执行参数），支持 `@DisallowConcurrentExecution` 防并发控制
- **分布式锁** — `@Lock` 注解驱动，基于 Redisson RLock 实现，支持 SpEL Key 动态锁粒度（ALL/KEY 两种模式），可配置等待超时、重试次数、自动释放时间
- **支付集成** — 统一 `PayClient` 接口抽象多渠道支付（支付宝 PC/WAP/扫码/APP/条码 + 微信 JSAPI/Native/WAP/App/条码），`@PayClientCode` 注解 + 工厂模式自动注册，支持统一下单、退款、回调解析
- **对象存储** — MinIO 文件上传/下载，预签名 URL 直传减轻后端带宽压力
- **PDF 转换** — Spire.PDF 集成，支持 PDF 转 Word 等文档格式转换

### 🔧 基础设施

- **全局异常处理** — 统一错误响应格式，模块化错误码设计（类型_模块_编号）
- **操作日志** — `@OperateLog` 注解自动记录操作日志，异步写入不影响主流程
- **登录日志** — 记录登录 IP、设备、时间、状态，支持审计追溯
- **请求监控** — Druid SQL 监控面板，慢查询自动检测告警
- **参数校验** — JSR-303 注解校验（`@Valid` + `@NotBlank`/`@NotNull` 等）
- **类型安全分页** — `PageResult<T>` 泛型封装分页结果，配合 `PageUtils.page()` 工具方法统一分页逻辑
- **线程池管理** — 专用线程池隔离（Netty 业务池、操作日志池、通知推送池），配合 `DelegatingSecurityContextExecutor` 传递安全上下文
- **Docker 部署** — Docker Compose 一键部署应用 + 中间件

### 📊 业务模块

- **系统管理** — 用户、角色、菜单、部门、岗位、字典、配置、日志
- **租户管理** — 租户 CRUD、域名绑定、租户隔离配置
- **通知公告** — 系统公告发布、撤回、已读状态管理
- **文件管理** — 文件上传/下载/预览（MinIO）
- **社交登录配置** — 第三方登录应用配置管理

## 项目结构

```
src/main/java/com/xd11cc/single/
├── config/                       # 配置 & 基础设施
│   ├── annotation/               # 自定义注解
│   │   ├── DataScope.java        #   数据权限控制
│   │   ├── DataSource.java       #   动态数据源切换
│   │   ├── Lock.java             #   分布式锁
│   │   ├── OperateLog.java       #   操作日志记录
│   │   ├── PayClientCode.java    #   支付渠道标记
│   │   ├── PayClientScan.java    #   支付客户端扫描注册
│   │   ├── RateLimit.java        #   接口限流
│   │   └── TenantIgnore.java     #   跳过租户过滤
│   ├── aspectj/                  # AOP 切面实现
│   ├── auth/                     # OAuth2 社交登录配置 (AuthRequestFactory)
│   ├── context/                  # 上下文持有者 (Tenant/DataSource/Permission)
│   ├── event/                    # Spring 事件机制 (NoticeEvent/NoticeEventListener)
│   ├── exception/                # 自定义异常 & ErrorCode 接口
│   ├── filter/                   # Servlet 过滤器 (JWT认证/租户识别)
│   ├── handler/                  # 处理器 (Security认证成功失败/MyBatis自动填充/全局异常)
│   ├── initializer/              # 启动初始化器 (租户缓存预热)
│   ├── interceptor/              # Spring MVC 拦截器 (租户数据库拦截)
│   ├── mq/                       # RabbitMQ 队列配置
│   ├── netty/                    # Netty WebSocket 服务端 (Server/Channel/Handler)
│   ├── pay/                      # 支付客户端抽象层 (工厂/支付宝/微信)
│   ├── properties/               # @ConfigurationProperties 配置属性类
│   ├── schedule/                 # 定时任务 (quartz/xxl)
│   ├── MybatisPlusConfig.java    # MyBatis-Plus 配置 (拦截器/分页)
│   ├── SecurityConfig.java       # Spring Security 配置
│   ├── RedisConfig.java          # Redis 序列化配置
│   ├── DruidConfig.java          # Druid 连接池 + 监控配置
│   ├── ThreadPoolConfig.java     # 线程池配置 (netty/log/notice)
│   ├── NettyServer.java          # Netty WebSocket 启动类
│   └── QuartzConfig.java         # Quartz 调度器配置
├── constants/                    # 应用常量 (CacheConstants/SecurityConstants)
├── controller/                   # REST API 控制器
├── convert/                      # MapStruct 对象转换器
├── entity/
│   ├── base/                     # 基础实体 (BaseDO/BaseTenantDO/ResponseVO/PageVO/PageResult)
│   ├── domain/                   # 数据库实体 DO
│   ├── dto/                      # 数据传输对象 (内部流转)
│   └── vo/                       # 视图对象 (请求/响应)
├── enums/                        # 业务枚举 (SystemErrorEnum 等)
├── mapper/                       # MyBatis-Plus Mapper
├── service/                      # 业务逻辑接口
│   └── impl/                     # Service 实现
└── utils/                        # 工具类
    ├── SecurityUtils.java        #   安全工具 (获取当前用户/加密)
    ├── PageUtils.java            #   分页工具 (统一分页逻辑)
    ├── TreeUtils.java            #   树形结构工具 (O(n) HashMap实现)
    ├── ExcelUtils.java           #   Excel导入导出 (EasyExcel)
    ├── AssertUtils.java          #   断言工具 (校验失败抛ServiceException)
    ├── JsonUtils.java            #   JSON序列化 (Fastjson2)
    ├── DateUtils.java            #   日期工具
    ├── MaskUtils.java            #   数据脱敏 (手机/身份证/邮箱)
    └── ...                       #   其他工具类
```

## 快速开始

### 环境要求

- JDK 8+
- MySQL 8.0+
- Redis 6.0+
- RabbitMQ 3.8+（可选）
- MinIO（可选）

### 本地开发

1. **创建数据库**

```sql
CREATE DATABASE xd11cc_single DEFAULT CHARACTER SET utf8mb4;
```

导入 `doc/sql/xd11cc_single.sql` 中的表结构。

2. **修改配置**

更新 `src/main/resources/application-dev.yml` 中的数据库、Redis 等服务连接信息。

3. **启动**

```bash
mvn spring-boot:run
```

应用启动地址：`http://localhost:10001/xd11cc`

接口文档地址：`http://localhost:10001/xd11cc/swagger-ui.html`

### Docker 部署

```bash
# 打包
mvn clean package -DskipTests

# 构建镜像
docker build -t xd11cc-single .

# 启动
docker-compose up -d
```

## 接口概览

| 模块 | 接口 | 描述 |
|------|------|------|
| **认证** | `POST /login/loginByPassword` | 账号密码登录 |
| 认证 | `GET /login/getCaptcha` | 获取验证码 |
| 认证 | `GET /login/getUserInfo` | 获取当前登录用户信息 |
| 认证 | `GET /login/getRoutes` | 获取当前用户路由菜单 |
| 认证 | `GET /login/authorize/{source}` | OAuth2 社交登录认证 |
| 认证 | `GET /login/callback/{source}` | OAuth2 认证回调 |
| **用户** | `POST /system/user/page` | 用户分页列表 |
| 用户 | `POST /system/user/add` | 新增用户 |
| 用户 | `PUT /system/user/edit` | 编辑用户 |
| 用户 | `DELETE /system/user/remove/{id}` | 删除用户 |
| 用户 | `PUT /system/user/resetPassword` | 重置密码 |
| **角色** | `POST /system/role/page` | 角色分页列表 |
| 角色 | `POST /system/role/assign` | 分配角色权限 |
| **菜单** | `GET /system/menu/list` | 菜单列表（树形） |
| **部门** | `GET /system/dept/list` | 部门列表（树形） |
| 部门 | `POST /system/dept/add` | 新增部门 |
| **岗位** | `POST /system/post/page` | 岗位分页列表 |
| **字典** | `POST /system/dict/type/page` | 字典类型分页 |
| 字典 | `POST /system/dict/data/page` | 字典数据分页 |
| **配置** | `POST /system/config/page` | 系统配置分页 |
| **租户** | `POST /system/tenant/page` | 租户分页列表 |
| 租户 | `POST /system/tenant/add` | 新增租户 |
| 租户 | `PUT /system/tenant/edit` | 编辑租户 |
| **通知** | `POST /system/notice/page` | 通知公告分页 |
| 通知 | `POST /system/notice/publish/{id}` | 发布通知 |
| 通知 | `PUT /system/notice/revoke/{id}` | 撤回通知 |
| **日志** | `POST /system/operateLog/page` | 操作日志分页 |
| 日志 | `POST /system/loginLog/page` | 登录日志分页 |
| **文件** | `POST /file/upload` | 文件上传 (MinIO) |
| 文件 | `GET /file/download/{id}` | 文件下载 |
| **代码生成** | `POST /generate/code/preview` | 代码生成预览 |
| 代码生成 | `POST /generate/code/download` | 代码生成下载 |
| **社交登录** | `POST /auth/client/config/list` | 社交登录配置列表 |

## 架构亮点

### 多租户数据隔离

```
请求 → TenantFilter (从域名/Header提取租户ID)
     → TenantContextHolder (存入 TransmittableThreadLocal，自动传播至异步线程)
     → JwtAuthenticationTokenFilter (JWT认证)
     → TenantDatabaseInterceptor (MyBatis-Plus TenantLineInnerInterceptor 自动追加 WHERE tenant_id = ?)
     → 业务逻辑执行
     → TenantContextHolder.clear() (finally 块清理，防止线程池复用污染)
```

**关键设计**：
- `TransmittableThreadLocal` 解决线程池复用时的租户上下文丢失问题
- `@TenantIgnore` 注解 + AOP 支持特定方法跳过租户过滤（如超管跨租户查询）
- `TenantUtils.executeIgnore()` 编程式忽略
- 逻辑删除字段 `del_flag = NULL` 配合唯一索引 `(xxx, tenant_id, del_flag)`，NULL 不参与判重，允许删除后重建同名记录

### 认证流程

```
登录请求 → LoginService.loginByPassword()
        → UserDetailServiceImpl.loadUserByUsername() (Spring Security 认证)
        → BCryptPasswordEncoder 密码校验
        → JWT Token 生成 (UUID 作为 Redis Key)
        → Redis 存储 LoginUserDTO (TTL 过期自动失效)
        → 返回 Token

后续请求 → JwtAuthenticationTokenFilter
        → 从 Header 提取 Token
        → Redis 查询 LoginUserDTO
        → 验证 Token 有效期 (剩余时间 < 阈值时自动续期)
        → 构建 UsernamePasswordAuthenticationToken
        → 设置 SecurityContextHolder
        → 继续 FilterChain
        → finally: TenantContextHolder.clear()
```

**关键设计**：
- JWT 作为 Redis Key 索引，服务端可主动踢人/刷新权限
- Token 续期在 Filter 层完成，客户端无感知
- 完全无状态（`SessionCreationPolicy.STATELESS`）

### 接口限流

```java
@RateLimit(key = "login:", time = 60, count = 10, type = RateLimitEnum.IP)
@PostMapping("/login/loginByPassword")
public ResponseVO<String> loginByPassword(...) { ... }
```

**实现原理**：
- `@RateLimit` 注解 + `RateLimitAspect` AOP 切面
- Redisson `RRateLimiter` 令牌桶算法（分布式场景多实例共享配额）
- 支持 `IP`/`USER`/`DEFAULT` 三种限流维度
- Key 格式：`rate_limit:{key}:{type}:{identifier}`

### 分布式锁

```java
@Lock(prefix = "order:pay", key = "#orderId", waitTime = 3, leaseTime = 30)
public void processPayment(String orderId) { ... }
```

**实现原理**：
- `@Lock` 注解 + `LockAspect` AOP 切面
- Redisson `RLock` 可重入锁实现
- 支持 `ALL`（全局互斥）/ `KEY`（按 SpEL 表达式分锁）两种粒度
- 可配置参数：`waitTime`（获取等待超时）、`leaseTime`（自动释放时间）、`retryTimes`（重试次数）
- Lock Key 格式：`lock:{prefix}:{lockMode}:{resolvedKey}`

### 支付客户端架构

```
支付请求 → PayClientFactory.getPayClient(channel)
        → PayClient.unifiedOrder(reqDTO)      // 统一下单
        → PayClient.parseOrderNotify(...)     // 渠道回调解析
        → PayClient.unifiedRefund(reqDTO)     // 统一退款
        → PayClient.parseRefundNotify(...)    // 退款回调解析
```

**关键设计**：
- `PayClient<Config>` 泛型接口统一支付宝/微信等渠道差异
- `@PayClientCode(PayChannelEnum.ALIPAY_WAP)` 注解标记渠道实现类
- `PayClientScannerRegistrar` 启动时扫描并注册到 `PayClientFactory`
- `AbstractAlipayPayClient` / `AbstractWxPayClient` 封装 SDK 公共逻辑
- 已接入渠道：支付宝（PC/WAP/扫码/APP/条码）、微信（JSAPI/Native/WAP/App/条码）

### 线程池设计

```java
// Netty 业务线程池 (IO密集型)
@Bean("nettyTaskExecutor")
corePoolSize = CPU_CORES, maxPoolSize = CPU_CORES * 2
拒绝策略 = DiscardPolicy (WebSocket 消息可容忍丢失)
包装 = TtlExecutors.getTtlExecutor() (传递 ThreadLocal)

// 操作日志线程池 (数据不可丢)
@Bean("operateLogExecutor")
corePoolSize = CPU_CORES / 2, maxPoolSize = CPU_CORES
拒绝策略 = CallerRunsPolicy (背压到调用线程，确保日志不丢)
包装 = DelegatingSecurityContextExecutor + TTL (传递 SecurityContext + ThreadLocal)

// 通知推送线程池
@Bean("noticeTaskExecutor")
corePoolSize = CPU_CORES / 2, maxPoolSize = CPU_CORES
拒绝策略 = CallerRunsPolicy
包装 = TtlExecutors.getTtlExecutor()
```

**关键设计**：
- 专用线程池隔离，避免不同业务互相影响
- `DelegatingSecurityContextExecutor` 传递 SecurityContext 至异步线程
- `TtlExecutors.getTtlExecutor()` 传递 TransmittableThreadLocal
- 拒绝策略选择原则：数据可丢 → Discard；数据不可丢 → CallerRuns

### Netty WebSocket 架构

```
客户端连接 → BossGroup(1线程) 接受连接
          → WorkerGroup(N线程) 处理读写
          → Pipeline 处理链:
              1. HttpServerCodec (HTTP编解码)
              2. ChunkedWriteHandler (大数据分块)
              3. HttpObjectAggregator (HTTP聚合)
              4. IdleStateHandler (30s读空闲/60s写空闲检测)
              5. WebSocketServerProtocolHandler (WebSocket握手)
              6. WebSocketAuthHandler (自定义Token认证)
              7. WebSocketServerHandler (业务消息处理)
          → ChannelManager 管理用户连接映射 (userId ↔ Channel)
```

**关键设计**：
- 独立端口（12001），不与 HTTP 请求互相影响
- Reactor 多线程模型，单机可支撑万级连接
- TCP 参数优化：`TCP_NODELAY=true` 禁用 Nagle 算法降低延迟
- 心跳机制：30s 未收到客户端消息 → 主动断开死连接

### 数据权限控制

```java
@DataScope(deptAlias = "d", userAlias = "u")
@PostMapping("/system/user/page")
public ResponseVO<PageResult<SystemUserVO>> page(...) { ... }
```

**实现原理**：
- `@DataScope` 注解 + `DataScopeAspect` AOP 切面
- 根据当前用户的数据权限级别动态追加 SQL 条件：
  - 全部数据权限：不追加
  - 自定义数据权限：`WHERE d.id IN (1,2,3)`
  - 本部门数据权限：`WHERE d.id = #{deptId}`
  - 本部门及以下：`WHERE d.id IN (递归查询子部门)`
  - 仅本人数据：`WHERE u.id = #{userId}`

## 开发计划

- [x] 组织架构（用户、角色、菜单、部门、岗位）
- [x] Spring Security + JWT 认证
- [x] RBAC 权限体系 + 数据权限控制
- [x] Swagger2 接口文档
- [x] Druid 动态多数据源
- [x] Redis + Redisson 缓存 & 限流
- [x] RabbitMQ 消息队列
- [x] XXL-JOB 任务调度
- [x] Netty WebSocket 实时推送
- [x] MinIO 文件存储
- [x] OAuth2 社交登录 (JustAuth)
- [x] Docker 容器化部署
- [x] Freemarker 代码生成器模板
- [x] 多租户数据隔离
- [x] 通知公告模块
- [x] Quartz 定时任务（内存 + JDBC 持久化）
- [x] 分布式锁（@Lock 注解）
- [x] 支付基础设施（多渠道客户端抽象层，业务接口持续完善中）
- [ ] 支付业务接口（订单创建/回调处理/退款流程）
- [ ] 增强审计日志（字段级变更追踪）
- [ ] 国际化（i18n）

## 许可证

详见 [LICENSE](LICENSE)。
