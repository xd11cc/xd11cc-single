# XD11CC Single - 多租户 SaaS 后端框架

**[English](README_EN.md) | 中文**

![img.png](doc/img/img.png)

一个基于 Spring Boot 构建的生产级多租户 SaaS 后端框架，提供认证授权、实时通讯、任务调度等开箱即用的解决方案。

## 技术栈

| 层级 | 技术 |
|------|------|
| 框架 | Spring Boot 2.7 / Java 8 |
| 安全 | Spring Security + JWT + OAuth2 (JustAuth) |
| ORM | MyBatis-Plus + PageHelper |
| 数据库 | MySQL 8.0 + Druid 连接池 |
| 缓存 | Redis + Redisson（分布式锁 & 限流） |
| 消息队列 | RabbitMQ（Direct/Topic/Fanout） |
| 实时通讯 | Netty WebSocket |
| 任务调度 | XXL-Job + Quartz |
| 文件存储 | MinIO |
| 接口文档 | Swagger2 / Knife4j |
| 部署 | Docker + Docker Compose |
| 其他 | MapStruct, EasyExcel, Freemarker, Lombok |

## 功能特性

### 核心架构
- **多租户隔离** — 通过 Filter + MyBatis 拦截器实现租户隔离，`TransmittableThreadLocal` 自动传播租户上下文
- **动态数据源** — 运行时通过 `@DataSource` 注解 + AOP 切换数据源
- **RBAC 权限体系** — 用户、角色、菜单、部门、岗位，支持细粒度权限控制
- **代码生成器** — 基于 Freemarker 模板引擎快速生成 CRUD 代码

### 安全模块
- **JWT 认证** — 无状态 Token 认证，Redis 管理会话
- **OAuth2 社交登录** — 集成 JustAuth，支持 GitHub、Google、微信等
- **接口限流** — 注解驱动 (`@RateLimit`)，基于 Redisson 实现
- **RSA 加密** — 密码传输加密

### 中间件集成
- **WebSocket 推送** — 基于 Netty 的 WebSocket 服务，实时消息通知
- **消息队列** — RabbitMQ，生产者确认 + 消费者手动 ACK
- **分布式调度** — XXL-Job 集成，可靠的定时任务执行
- **对象存储** — MinIO 文件上传/下载

### 基础设施
- **全局异常处理** — 统一错误响应，自定义错误码
- **请求监控** — Druid SQL 监控，慢查询检测
- **Docker 部署** — Docker Compose 一键部署

## 项目结构

```
src/main/java/com/xd11cc/single/
├── config/                 # 配置 & 基础设施
│   ├── annotation/         # 自定义注解 (@DataSource, @RateLimit, @TenantIgnore)
│   ├── aspectj/            # AOP 切面
│   ├── auth/               # OAuth2 社交登录配置
│   ├── context/            # 上下文持有者 (Tenant, DataSource, Permission)
│   ├── exception/          # 自定义异常 & 错误码
│   ├── filter/             # JWT 过滤器, 租户过滤器
│   ├── handler/            # Security 处理器, MyBatis 字段填充
│   ├── interceptor/        # 请求拦截器, 租户数据库拦截器
│   ├── job/                # XXL-Job 任务处理器
│   ├── mq/                 # RabbitMQ 队列配置
│   ├── netty/              # WebSocket 服务 & 处理器
│   └── properties/         # 配置属性类
├── constants/              # 应用常量
├── controller/             # REST API 控制器
├── convert/                # MapStruct 对象转换器
├── entity/
│   ├── base/               # 基础实体 (BaseDO, ResponseVO, PageVO)
│   ├── domain/             # 数据库实体
│   ├── dto/                # 数据传输对象
│   └── vo/                 # 视图对象 (请求/响应)
├── enums/                  # 业务枚举
├── mapper/                 # MyBatis-Plus Mapper
├── service/                # 业务逻辑接口
│   └── impl/              # Service 实现
└── utils/                  # 工具类 (JWT, RSA, IP, Page 等)
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
| 认证 | `POST /login/loginByPassword` | 账号密码登录 |
| 认证 | `GET /login/getCaptcha` | 获取验证码 |
| 认证 | `GET /login/authorize/{source}` | OAuth2 社交登录 |
| 用户 | `GET /system/user/list` | 用户管理 |
| 菜单 | `GET /system/menu/list` | 菜单权限管理 |
| 字典 | `GET /system/dict/type/list` | 字典管理 |
| 配置 | `GET /system/config/list` | 系统配置 |
| 文件 | `POST /file/upload` | 文件上传 (MinIO) |
| 代码生成 | `POST /generate/code/preview` | 代码生成预览 |

## 架构亮点

### 多租户数据隔离

```
请求 → TenantFilter (从域名/Header提取租户)
     → TenantContextHolder (存入 TransmittableThreadLocal)
     → TenantDatabaseInterceptor (自动追加 WHERE tenant_id = ?)
     → 响应
```

### 认证流程

```
登录请求 → LoginService → Spring Security 认证
        → JWT Token 生成 → Redis 会话存储
        → 返回 Token

后续请求 → JwtAuthenticationTokenFilter
        → 验证 Token → 从 Redis 加载用户
        → 设置 SecurityContext → 处理请求
```

### 接口限流

```java
@RateLimit(time = 60, count = 10, type = RateLimitEnum.IP)
@GetMapping("/api/resource")
public ResponseVO getResource() { ... }
```

## 开发计划

- [x] 组织架构（用户、角色、菜单、部门、岗位）
- [x] Spring Security + JWT 认证
- [x] Swagger2 接口文档
- [x] Druid 动态多数据源
- [x] Redis + Redisson 缓存 & 限流
- [x] RabbitMQ 消息队列
- [x] XXL-Job 任务调度
- [x] Netty WebSocket 实时推送
- [x] MinIO 文件存储
- [x] OAuth2 社交登录 (JustAuth)
- [x] Docker 容器化部署
- [ ] Freemarker 代码生成器模板（进行中）
- [ ] Flowable 工作流引擎
- [ ] 支付宝 & 微信支付集成

## 许可证

详见 [LICENSE](LICENSE)。
