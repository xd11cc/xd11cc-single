# XD11CC Single - Multi-Tenant SaaS Backend Framework

**English | [中文](README.md)**

![img.png](doc/img/img.png)

A production-grade multi-tenant SaaS backend framework built on Spring Boot 2.7 and Java 8, providing
authentication and authorization, real-time messaging, task scheduling, and a workflow engine out of the box.

## 🌐 Live Demo

**Demo URL:** https://xd11ccc.cloud/xd11cc/login

> Demo account: `TBD` / `TBD`

## Tech Stack

| Layer | Technology | Version | Notes |
|------|------|------|------|
| **Core framework** | Spring Boot | 2.7.18 | Application framework |
| **Security** | Spring Security + JWT | 5.7.x + 0.11.5 | Stateless auth, Redis-backed sessions |
| **Social login** | JustAuth | 1.16.7 | OAuth2 login (GitHub / Gitee wired up) |
| **ORM** | MyBatis-Plus | 3.5.3.1 | Enhanced CRUD + auto-fill + tenant interceptor |
| **Pagination** | PageHelper | 1.4.6 | ThreadLocal-based pagination interceptor |
| **Database** | MySQL | 8.0.33 | utf8mb4 + utf8mb4_0900_ai_ci |
| **Connection pool** | Druid | 1.2.24 | SQL monitoring + slow query detection + dynamic data sources |
| **Cache** | Redis + Redisson | 6.0+ / 3.36.0 | Distributed locks, rate limiting, session storage |
| **Message queue** | RabbitMQ | 2.7.x | Async decoupling, event notification |
| **Real-time** | Netty WebSocket | 4.1.x | Dedicated port, long-lived connections |
| **Scheduling** | XXL-JOB | 2.4.0 | Distributed jobs, failover, retry |
| **Object storage** | MinIO | 8.6.0 | S3-compatible distributed storage |
| **Payments** | Alipay SDK + WeChat Pay SDK | 4.40.831 / 4.8.0 | Multi-channel payment client abstraction |
| **Workflow** | Flowable | 6.7.2 | BPMN 2.0 engine on a separate datasource |
| **API docs** | Springfox Swagger2 | 2.9.2 | Online API documentation (knife4j is not included) |
| **Containerization** | Docker + Docker Compose | - | One-command deployment |
| **Utilities** | Hutool / Fastjson2 / MapStruct / EasyExcel / TTL | 5.8.16 / 2.0.39 / 1.6.3 / 3.3.2 / 2.13.2 | Utility libraries |

## Features

### 🏗️ Core Architecture

- **Multi-tenant isolation** — Shared database with row-level isolation. `TenantFilter` resolves the tenant
  from the domain or header, `TransmittableThreadLocal` propagates the tenant context to async threads, and
  `MyBatis-Plus TenantLineInnerInterceptor` appends `WHERE tenant_id = ?` automatically.
- **Dynamic data sources** — Switch datasources at runtime with the `@DataSource` annotation + AOP, supporting
  read/write splitting.
- **RBAC** — Full user, role, menu, department, and post permission model with fine-grained control.
- **Data permissions** — `@DataScope` isolates data by department or by individual, appending filter
  conditions dynamically via AOP.
- **Code generator** — Generates CRUD code (Controller/Service/Mapper/Entity/VO/DTO) from Freemarker templates.

### 🔐 Security Module

- **JWT authentication** — Stateless token auth. The JWT is used as a Redis key index so the server keeps
  control of the session lifecycle (kick users, refresh permissions).
- **OAuth2 social login** — Integrated with JustAuth (40+ platforms supported upstream). Currently wired to
  **GitHub / Gitee**; adding a platform takes one enum constant plus one config row.
- **API rate limiting** — `@RateLimit` annotation-driven, based on the Redisson `RRateLimiter` token bucket.
  Supports IP / user / endpoint / expression dimensions, and they can be combined.
- **Password security** — BCrypt with salt; accounts lock for 30 minutes after 5 consecutive failures;
  CAPTCHA and login rate limiting together defend against brute force.
- **CAPTCHA** — Image CAPTCHA backed by Redis TTL expiration.
- **CORS** — Unified cross-origin configuration with a whitelist of allowed origins.

### 📡 Middleware Integration

- **WebSocket push** — Netty-based server on a dedicated port (12001) with pre-handshake token
  authentication, heartbeat detection (3 consecutive 60s read-idle events trigger a disconnect), and idle
  connection cleanup.
- **Message queue** — RabbitMQ with publisher confirms and returns enabled
  (`publisher-confirm-type: correlated` / `publisher-returns`); manual consumer ACK is managed by
  `MqConsumeTemplate`.
- **Distributed scheduling** — XXL-JOB with shard broadcasting, failover, and retry.
- **Distributed locks** — `@RedisLock` annotation-driven, based on Redisson `RLock`, with SpEL-driven lock
  granularity (`ALL` / `KEY` modes) and configurable wait time, retry count, and lease time.
- **Payments** — A unified `PayClient` interface abstracts multiple channels (Alipay PC/WAP/QR/APP/barcode and
  WeChat JSAPI/Native/WAP/App/barcode). `@PayClientCode` + a factory register clients automatically, covering
  unified order, refund, and callback parsing.
- **Object storage** — MinIO upload/download with presigned URLs so clients upload directly, reducing backend
  bandwidth.
- **PDF conversion** — Spire.PDF integration (PDF to Word and other formats).

### 🔧 Infrastructure

- **Global exception handling** — Unified error responses with segmented error codes
  (`10` + two-digit module + three-digit sequence).
- **Operation logs** — `@OperateLog` records operations asynchronously without blocking the main flow.
- **Login logs** — Login IP, device, time, and status for auditing.
- **Request monitoring** — Druid SQL monitoring with automatic slow-query detection.
- **Validation** — JSR-303 annotations (`@Valid` + `@NotBlank` / `@NotNull`, etc.).
- **Type-safe pagination** — `PageResult<T>` plus `PageUtils.page()` for consistent pagination.
- **Thread pools** — Dedicated pools (operation-log pool and notification pool). The operation-log pool uses
  `DelegatingSecurityContextExecutor` to propagate the security context; the notification pool uses TTL to
  propagate the tenant context.
- **Docker deployment** — Docker Compose brings up the application and middleware in one command.

### 🔄 Workflow Engine (Flowable)

- **BPMN 2.0 designer** — Visual process design with bpmn.js, supporting user tasks, gateways, and
  multi-instance (parallel/sequential) approval.
- **Isolated engine datasource** — Flowable runs in its own schema (`xd11cc_flowable`), keeping `ACT_*` tables
  out of the business database.
- **Expression config center** — Approvers, candidates, and conditions are declared through
  `expressionConfigKey`, decoupling BPMN from business staff.
- **Process definition management** — Draft save, deploy, versioning, suspend/activate, and copy.
- **Approval flow** — Todo/done, approve, reject (including jumping back to any historical node), delegate,
  and withdraw.
- **Candidate tasks** — `candidateUsers` / `candidateGroups`, with candidate groups mapped to system roles.
- **Real-time notification** — Approval actions are pushed over WebSocket (`WORKFLOW_APPROVED` /
  `WORKFLOW_REJECTED`, etc.).
- **Multi-tenant isolation** — `tenantId` is passed explicitly at deploy time, so `ACT_RE_*` / `ACT_RU_*` rows
  carry the tenant identifier.

### 📊 Business Modules

- **System management** — Users, roles, menus, departments, posts, dictionaries, config, logs.
- **Tenant management** — Tenant CRUD, domain binding, tenant isolation config.
- **Notices** — Publish, revoke, and track read status, with WebSocket push and unread counts.
- **Workflow** — Categories, definitions, design, expression config, todo/done approvals.
- **File management** — Upload, download, and preview via MinIO.
- **Social login config** — Manage third-party login application configs.
- **Online users** — Inspect active sessions and force logout.

## Project Structure

```
src/main/java/com/xd11cc/single/
├── common/                            # Shared capabilities
│   ├── annotation/                    #   Custom annotations
│   │   ├── DataScope.java             #     Data permissions
│   │   ├── DataSource.java            #     Dynamic datasource switching
│   │   ├── RedisLock.java             #     Distributed lock
│   │   ├── RateLimit.java             #     API rate limiting
│   │   ├── OperateLog.java            #     Operation logging
│   │   ├── TenantIgnore.java          #     Skip tenant filtering
│   │   ├── PayClientCode.java         #     Payment channel marker
│   │   └── PayClientScan.java         #     Payment client scanning
│   ├── aspectj/                       #   AOP aspects
│   ├── base/                          #   Base classes/interfaces (ArrayValuable)
│   ├── auth/                          #   OAuth2 authorization (AuthRequestFactory)
│   ├── concurrency/                   #   Thread pool configuration
│   ├── context/                       #   Context holders (Tenant/DataSource/Permission)
│   ├── docs/                          #   Swagger configuration
│   ├── event/                         #   Spring events (NoticeEvent, etc.)
│   ├── exception/                     #   ServiceException / ErrorCode
│   ├── initializer/                   #   Startup initialization (tenant cache warm-up)
│   ├── mq/                            #   RabbitMQ queue configuration
│   ├── properties/                    #   @ConfigurationProperties classes
│   ├── ratelimit/                     #   Rate limiting core (RateLimiter / KeyResolver / Spec)
│   ├── security/                      #   Spring Security configuration
│   └── web/                           #   Filters / interceptors / global exception / CORS
├── infrastructure/                    # Middleware and infrastructure
│   ├── datasource/                    #   Druid pool + dynamic datasource
│   ├── mybatis/                       #   MyBatis-Plus interceptor configuration
│   ├── netty/                         #   Netty WebSocket server
│   ├── redis/                         #   Redis cache + serialization
│   ├── schedule/                      #   XXL-JOB scheduling
│   └── storage/                       #   MinIO object storage
├── modules/                           # Business modules (vertical slices)
│   ├── pay/                           #   Payments (PayClient + Alipay/WeChat implementations)
│   └── workflow/                      #   Workflow engine wiring and business extension points
│       ├── FlowableConfig.java        #     Engine construction (separate datasource)
│       ├── FlowableWebMvcConfig.java  #     Interceptor registration
│       ├── FlowableSecurityInterceptor.java  # Flowable identity context
│       ├── AbstractWorkflowHandler.java      # Business extension point
│       ├── WorkflowHandlerFactory.java       # Handler strategy factory
│       ├── WorkflowEvent.java                # Domain event
│       └── WorkflowEventListener.java        # Event → WebSocket push
├── constants/                         # Application constants (CacheConstants/SecurityConstants)
├── controller/                        # REST controllers
├── convert/                           # MapStruct converters
├── entity/
│   ├── base/                          #   Base entities (BaseDO/BaseTenantDO/ResponseVO/PageResult)
│   ├── domain/                        #   Database entities (DO)
│   ├── dto/                           #   Internal transfer objects
│   └── vo/                            #   View objects (request/response)
├── enums/                             # Business enums (SystemErrorEnum, etc.)
├── mapper/                            # MyBatis-Plus mappers
├── service/                           # Service interfaces
│   └── impl/                          #   Service implementations
└── utils/                             # Utilities
    ├── SecurityUtils.java             #   Security helpers (current user/password)
    ├── PageUtils.java                 #   Pagination helper (PageHelper wrapper)
    ├── TreeUtils.java                 #   Tree building (O(n))
    ├── ExcelUtils.java                #   Excel import/export (EasyExcel)
    ├── JwtUtils.java                  #   JWT signing and parsing
    ├── IpUtils.java                   #   IP resolution
    ├── RSAUtils.java                  #   RSA encryption/decryption
    ├── TenantUtils.java               #   Tenant context helpers
    └── ...                            #   Other utilities
```

### Layered Dependency Rules

```
controller → service → mapper → entity
     ↓           ↓
   common / infrastructure / modules
```

- `entity`, `utils`, and `enums` depend only on `common` — never on `infrastructure` or `modules`
- `common` and `infrastructure` have a one-way relationship; they must not depend on `service` / `controller`
- New business modules go under `modules/<module>/`, never inside `common` or `infrastructure`

## Quick Start

### Prerequisites

| Component | Version | Required |
|------|------|----------|
| JDK | 8+ | ✅ Yes |
| MySQL | 8.0+ | ✅ Yes |
| Redis | 6.0+ | ✅ Yes |
| RabbitMQ | 3.8+ | Optional |
| MinIO | - | Optional (file features) |

> Flowable uses a **separate database** `xd11cc_flowable`. On first startup it is created automatically
> through `createDatabaseIfNotExist=true` on the connection string, so no manual setup is needed; the `ACT_*`
> tables are initialized by the engine.

### Local Development

**1. Create the business database and import schema + data**

```sql
CREATE DATABASE xd11cc_single DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
```

```bash
# Schema first, then seed data — both are required and must run in order
mysql -uroot -p xd11cc_single < doc/sql/xd11cc_single.sql
mysql -uroot -p xd11cc_single < doc/sql/xd11cc_single_data.sql
```

**2. Configure the application**

Update the following in `src/main/resources/application-dev.yml`:

- `spring.datasource.druid.master` — business database
- `spring.datasource.druid.flowable` — workflow engine database
- `spring.redis` — Redis connection

**3. Set the JWT secret (required)**

For security reasons the JWT signing key **must not be hard-coded**. Inject it via an environment variable or
startup argument, with a length of **at least 32 bytes**:

```bash
export SECURITY_TOKEN_SECRET="your-strong-secret-key-at-least-32-bytes"
```

The application fails fast with a clear message if the secret is missing or too short.

**4. Start**

```bash
mvn spring-boot:run
```

| URL | Description |
|------|------|
| `http://localhost:10001/xd11cc` | Application root |
| `http://localhost:10001/xd11cc/swagger-ui.html` | Swagger UI (API docs) |
| `ws://localhost:12001/ws?token=xxx` | WebSocket endpoint |

> **Note**: If you previously compiled with the old package layout, run `mvn clean` first.
> Stale classes under `target/classes` from the old package paths can cause bean conflicts.

**5. Log in**

The seed data ships with an administrator account — use it to log in (change the password immediately
afterwards):

| Username | Password | Notes |
|------|------|------|
| `admin` | `xd11cc@1216` | Administrator of tenant 1, holds every menu permission |

> Call `/login/getCaptcha` first to obtain a CAPTCHA, then POST `username` / `password` / `captchaId` /
> `captcha` to `/login/loginByPassword`. Requests must carry `tenant-id: 1` (or resolve the tenant through
> the `tenant_domain` mapping).

### Database Scripts

| Script | Purpose |
|------|------|
| `doc/sql/xd11cc_single.sql` | **Schema only** (DROP + CREATE + indexes for 24 tables); no INSERT statements |
| `doc/sql/xd11cc_single_data.sql` | **Seed data** (tenants, users, roles, menus, dictionaries, config, workflow categories — 15 tables) |
| `doc/sql/template.sql` | Table templates (business / tree / association / global / log — five patterns) |

> The schema script has been verified column-by-column and index-by-index against a running database;
> running both scripts reproduces it exactly. The schema script intentionally contains no INSERTs so it can
> be replayed against an empty or test database.

### Upgrading Flowable on an Existing Environment

Flowable's `ACT_*` tables are created automatically the first time it connects to `xd11cc_flowable`. To
control when that happens, set `flowable.database-schema-update` to `true` in `application.yml`, start the
application once, then set it back to `false`.

### Docker Deployment

```bash
# Package
mvn clean package -DskipTests

# Build the image
docker build -t xd11cc-single .

# Start
docker-compose up -d
```

## API Overview

> For the full API surface, see Swagger UI at `/xd11cc/swagger-ui.html`. The main entry points per module
> are listed below.
> All endpoints are prefixed with `/xd11cc` (for example `/xd11cc/system/user/page`).

### Auth `/login`

| Method | Path | Description |
|------|------|------|
| GET | `/login/getCaptcha` | Get an image CAPTCHA |
| POST | `/login/loginByPassword` | Username/password login |
| GET | `/login/getUserInfo` | Current user info |
| GET | `/login/getRoutes` | Current user's dynamic route menus |
| GET | `/login/authorize/{source}` | OAuth2 authorization redirect |
| GET | `/login/callback/{source}` | OAuth2 callback |

### System Management

| Module | Main endpoints |
|------|----------|
| User | `POST /system/user/page` · `/add` · `/modifyById` · `/list` · `GET /getById/{id}` · `/removeByIds/{ids}` · `POST /resetPassword/{id}` |
| Role | `POST /system/role/page` · `/add` · `/modifyById` · `GET /menuIds/{roleId}` · `/deptIds/{roleId}` |
| Menu | `POST /menu/treeList` (tree) |
| Department | `POST /system/dept/treeList` · `/add` · `/modifyById` |
| Post | `POST /system/post/page` |
| Dictionary | `POST /dict/type/page` · `POST /dict/data/page` |
| Config | `POST /system/config/page` |
| Tenant | `POST /system/tenant/page` · `/add` · `/modifyById` |
| Online users | `GET /system/onlineUser/list` · force logout |

### Notices

| Method | Path | Description |
|------|------|------|
| POST | `/system/notice/page` | Paged notices |
| POST | `/system/notice/publish/{id}` | Publish a notice (triggers a WebSocket push) |
| POST | `/system/notice/revoke/{id}` | Revoke a notice |
| POST | `/system/notice-user/myPage` | My notices |
| GET | `/system/notice-user/unreadCount` | Unread count |
| POST | `/system/notice-user/markAsRead/{noticeId}` | Mark as read |

### Workflow `/workflow`

| Module | Main endpoints |
|------|----------|
| Category | `POST /workflow/category/page` · `/add` · `/modifyById` · `GET /list` |
| Definition | `POST /workflow/draft/save` · `/draft/deploy` · `GET /draft/{modelId}` · `POST /definition/page` · `/definition/suspend` · `/definition/activate` |
| Expression config | `POST /workflow/expression-config/page` · `/add` · `GET /options` |
| Process instance | `POST /workflow/process/mine` · `/cancel` · `GET /process/{processInstanceId}` (`process/start` is not exposed yet — it must be bound to a business document) |
| Approval tasks | `POST /workflow/task/todo` · `/done` · `/task/approve` · `/task/reject` · `/task/delegate` · `GET /task/rejectable-nodes` |

### Other

| Module | Main endpoints |
|------|----------|
| File | `POST /file/upload` · `/upload/batch` · `/download` |
| Code generator | `POST /generate/code/page` · `GET /generate/code?tableName=xxx` |
| Social login config | `POST /auth/client/config/page` |

## Architecture Highlights

### Multi-Tenant Data Isolation

```
Request → TenantFilter (resolve tenant ID from domain/header)
        → TenantContextHolder (store in TransmittableThreadLocal, propagates to async threads)
        → JwtAuthenticationTokenFilter (JWT authentication)
        → TenantDatabaseInterceptor (MyBatis-Plus TenantLineInnerInterceptor appends WHERE tenant_id = ?)
        → Business logic
        → TenantContextHolder.clear() (in a finally block, to avoid thread-pool reuse pollution)
```

**Key design points**:
- `TransmittableThreadLocal` prevents tenant context loss when threads are pooled and reused
- The `@TenantIgnore` annotation + AOP skips tenant filtering for specific methods (for example,
  super-admin cross-tenant queries)
- `TenantUtils.executeIgnore()` provides a programmatic equivalent
- The logical-delete column `del_flag = NULL` combined with the unique index
  `(tenant_id, business key, del_flag)` works because `NULL` is not compared during uniqueness checks,
  allowing a record to be recreated after deletion

### Authentication Flow

```
Login request → LoginService.loginByPassword()
             → UserDetailServiceImpl.loadUserByUsername() (load user + status check)
             → PasswordAuthenticationProvider (BCrypt check + failure-count lockout)
             → JWT token generation (UUID as the Redis key)
             → Redis stores LoginUserDTO (expires via TTL)
             → Return token

Subsequent requests → JwtAuthenticationTokenFilter
                   → Extract token from header
                   → Query LoginUserDTO from Redis
                   → Validate token expiry (auto-renew when remaining time < threshold)
                   → Build UsernamePasswordAuthenticationToken
                   → Set SecurityContextHolder
                   → Continue the filter chain
                   → finally: TenantContextHolder.clear()
```

**Key design points**:
- The JWT acts as a Redis key index, so the server can kick users or refresh permissions at will
- Token renewal happens in the filter layer — clients never notice it
- Fully stateless (`SessionCreationPolicy.STATELESS`)

### Rate Limiting

```java
@RateLimit(permits = 5, windowSeconds = 60,
        subject = {Subject.IP, Subject.EXPRESSION}, expression = "#p0.username")
@PostMapping("/login/loginByPassword")
public ResponseVO<String> loginByPassword(...) { ... }
```

**How it works**:
- `@RateLimit` annotation + `RateLimitAspect`; bucket creation and accounting live in
  `common/ratelimit/RateLimiter`
- Redisson `RRateLimiter` token bucket (quotas are shared across instances in a distributed deployment)
- Supports `IP` / `USER` / `METHOD` / `EXPRESSION` dimensions; they can be combined and the request is
  rejected if any dimension is exceeded
- The key carries a spec fingerprint: `rate_limit:{identity}:{permits}/{window}s`, so changing the limit
  switches to a fresh bucket and takes effect immediately
- Two deliberately different strategies: the HTTP entry point is fail-closed (reject) when the limiter is
  unavailable, while the WebSocket entry point is fail-open (allow)

### Distributed Lock

```java
@RedisLock(prefix = "order:pay", key = "#orderId", waitTime = 3, leaseTime = 30)
public void processPayment(String orderId) { ... }
```

**How it works**:
- `@RedisLock` annotation + `RedisLockAspect`
- Backed by Redisson's reentrant `RLock`
- Two granularities: `ALL` (all arguments joined into the lock key) and `KEY` (the lock key is the SpEL result)
- Configurable: `waitTime` (acquisition timeout), `leaseTime` (auto-release), `retryTimes`
- Lock key format: `redlock:{prefix}:{declaringClass}.{methodName}:{resolvedValue}`
  - `KEY` mode (non-empty key): `resolvedValue` is the SpEL result, e.g.
    `redlock:tenant:add:SystemTenantServiceImpl.add:example.com`
  - `ALL` mode (empty key): `resolvedValue` joins all arguments with `|`

### Payment Client Architecture

```
Payment request → PayClientFactory.getPayClient(channel)
                → PayClient.unifiedOrder(reqDTO)      // unified order
                → PayClient.parseOrderNotify(...)     // channel callback parsing
                → PayClient.unifiedRefund(reqDTO)     // unified refund
                → PayClient.parseRefundNotify(...)    // refund callback parsing
```

**Key design points**:
- The generic `PayClient<Config>` interface hides channel differences between Alipay and WeChat
- `@PayClientCode(PayChannelEnum.ALIPAY_WAP)` marks channel implementations
- `PayClientScannerRegistrar` scans and registers them into `PayClientFactory` at startup
- `AbstractAlipayPayClient` / `AbstractWxPayClient` hold shared SDK logic
- Wired channels: Alipay (PC/WAP/QR/APP/barcode) and WeChat (JSAPI/Native/WAP/App/barcode)

### Thread Pool Design

```java
// Operation-log pool (data must not be lost)
@Bean("operateLogExecutor")
corePoolSize = CPU_CORES / 2, maxPoolSize = CPU_CORES
RejectionPolicy = CallerRunsPolicy (back-pressure to the calling thread, so no log is dropped)
Wrapper = DelegatingSecurityContextExecutor + TTL (propagate SecurityContext + ThreadLocal)

// Notification push pool
@Bean("noticeTaskExecutor")
corePoolSize = CPU_CORES / 2, maxPoolSize = CPU_CORES
RejectionPolicy = CallerRunsPolicy
Wrapper = TtlExecutors.getTtlExecutor()
```

**Key design points**:
- Dedicated pools keep unrelated workloads from interfering with each other
- `DelegatingSecurityContextExecutor` propagates the SecurityContext to async threads
- `TtlExecutors.getTtlExecutor()` propagates `TransmittableThreadLocal`
- Rejection policy rule of thumb: lossy data → `Discard`; must-not-lose data → `CallerRuns`

### Netty WebSocket Architecture

```
Client connect → BossGroup (1 thread) accepts connections
              → WorkerGroup (N threads) handles reads/writes
              → Pipeline:
                 1. IdleStateHandler (60s read idle / 25s write idle)
                 2. HttpServerCodec (HTTP codec)
                 3. HttpObjectAggregator (HTTP aggregation)
                 4. WebSocketAuthHandler (token authentication before the handshake)
                 5. WebSocketServerProtocolHandler (WebSocket handshake)
                 6. WebSocketServerHandler (business messages)
              → ChannelManager maps users to channels (userId ↔ Channel)
```

**Key design points**:
- Dedicated port (12001), isolated from HTTP traffic
- Reactor threading model, capable of tens of thousands of connections per node
- TCP tuning: `TCP_NODELAY=true` disables Nagle's algorithm to cut latency
- Dead-connection detection: only after 3 consecutive read-idle events (60s each), to avoid killing
  connections on flaky networks
- Two layers of rate limiting: a Redis token bucket on the sending side (shared across instances) plus a
  per-connection frame guard (in-memory counter that disconnects the client when exceeded)

### Data Permission Control

```java
@DataScope(deptAlias = "d", userAlias = "u")
@PostMapping("/system/user/page")
public ResponseVO<PageResult<SystemUserVO>> page(...) { ... }
```

**How it works**:
- `@DataScope` annotation + `DataScopeAspect`
- SQL conditions are appended dynamically based on the current user's data scope:
  - All data: nothing appended
  - Custom scope: `WHERE d.id IN (1,2,3)`
  - Own department: `WHERE d.id = #{deptId}`
  - Own department and below: `WHERE d.id IN (recursively resolved child departments)`
  - Self only: `WHERE u.id = #{userId}`

### Workflow Integration Architecture

```
                     ┌─────────────────────────────┐
   Designer (bpmn.js) ─→│  /workflow/draft/save       │  Draft stored in ACT_RE_MODEL
                     │  expressionConfigKey        │  Approver config
                     └──────────────┬──────────────┘
                                    ↓ deploy
                     ┌─────────────────────────────┐
                     │  ACT_RE_PROCDEF (tenantId)  │  Engine DB: xd11cc_flowable
                     └──────────────┬──────────────┘
                                    ↓ start
   Requester ───────→│  workflow_instance (biz DB) │  Business link + state machine
                                    ↓
   Approver ────────→│  Todo query (assignee/candidates)  │
                     │  Approve / Reject / Delegate       │
                     └──────────────┬──────────────┘
                                    ↓ publish domain event
                     WorkflowEvent → WorkflowEventListener
                                    ↓ @Async + AFTER_COMMIT
                           WebSocket approval notification
```

**Key design points**:

- **Dual datasources** — Engine tables (`ACT_*`) live in `xd11cc_flowable`, business tables in
  `xd11cc_single`, so engine upgrades cannot damage business data; the two are linked by
  `process_instance_id`.
- **Explicit tenant passing** — Flowable 6.7.2 has no ThreadLocal-based tenant injection, so the business
  layer passes `tenantId` explicitly on deploy and query, which keeps engine data tenant-isolated by
  construction.
- **Expression config center** — BPMN does not hard-code approvers; it declares an `expressionConfigKey`
  which `WorkflowExpressionResolver` resolves at deploy time, letting business staff maintain approval rules
  in the UI.
- **Business extension points** — `AbstractWorkflowHandler` exposes `onProcessStart` / `onTaskApprove` /
  `onProcessComplete` callbacks; business code registers by `processDefinitionKey`. When nothing is
  registered, a no-op implementation is used so the process is never blocked.
- **Explicit transaction boundary** — Approval events use `@TransactionalEventListener(AFTER_COMMIT)`, so a
  notification is only pushed after the database commit succeeds — no more "pushed but rolled back".
- **Candidate support** — Todo queries cover `assignee`, `candidateUsers`, and `candidateGroups`; candidate
  groups map to system role IDs so candidate tasks are never invisible.

## License

See [LICENSE](LICENSE).
