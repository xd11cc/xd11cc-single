# XD11CC Single - Multi-Tenant SaaS Backend Framework

**English | [中文](README.md)**

![img.png](doc/img/img.png)

A production-ready, multi-tenant SaaS backend framework built with Spring Boot. Provides out-of-the-box solutions for authentication, authorization, real-time messaging, task scheduling, and more.

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Framework | Spring Boot 2.7 / Java 8 |
| Security | Spring Security + JWT + OAuth2 (JustAuth) |
| ORM | MyBatis-Plus + PageHelper |
| Database | MySQL 8.0 + Druid Connection Pool |
| Cache | Redis + Redisson (Distributed Lock & Rate Limiting) |
| Message Queue | RabbitMQ (Direct/Topic/Fanout) |
| Real-time | Netty WebSocket |
| Task Scheduling | XXL-Job + Quartz |
| File Storage | MinIO |
| API Docs | Swagger2 / Knife4j |
| Deployment | Docker + Docker Compose |
| Others | MapStruct, EasyExcel, Freemarker, Lombok |

## Features

### Core Architecture
- **Multi-Tenancy** — Tenant isolation via Filter + MyBatis interceptor, automatic tenant context propagation with `TransmittableThreadLocal`
- **Dynamic Datasource** — Runtime datasource switching with `@DataSource` annotation and AOP
- **RBAC Permission System** — Users, Roles, Menus, Departments, Posts with fine-grained access control
- **Code Generator** — Freemarker-based code generation for rapid CRUD development

### Security
- **JWT Authentication** — Stateless token-based auth with Redis session management
- **OAuth2 Social Login** — Integrated with JustAuth supporting GitHub, Google, WeChat, etc.
- **API Rate Limiting** — Annotation-driven (`@RateLimit`) rate limiting powered by Redisson
- **RSA Encryption** — Password transmission encryption

### Middleware Integration
- **WebSocket Push** — Netty-based WebSocket server for real-time notifications
- **Message Queue** — RabbitMQ with producer confirmation and manual consumer ACK
- **Distributed Scheduling** — XXL-Job integration for reliable task execution
- **Object Storage** — MinIO integration for file upload/download

### Infrastructure
- **Global Exception Handling** — Unified error response with custom error codes
- **Request Logging & Monitoring** — Druid SQL monitoring with slow query detection
- **Docker Ready** — One-command deployment with Docker Compose

## Project Structure

```
src/main/java/com/xd11cc/single/
├── config/                 # Configuration & Infrastructure
│   ├── annotation/         # Custom annotations (@DataSource, @RateLimit, @TenantIgnore)
│   ├── aspectj/            # AOP aspects for annotations
│   ├── auth/               # OAuth2 social login configuration
│   ├── context/            # Context holders (Tenant, DataSource, Permission)
│   ├── exception/          # Custom exceptions & error codes
│   ├── filter/             # JWT filter, Tenant filter
│   ├── handler/            # Security handlers, MyBatis field handler
│   ├── interceptor/        # Request interceptors, Tenant DB interceptor
│   ├── job/                # XXL-Job handlers
│   ├── mq/                 # RabbitMQ queue configuration
│   ├── netty/              # WebSocket server & handlers
│   └── properties/         # Configuration property classes
├── constants/              # Application constants
├── controller/             # REST API controllers
├── convert/                # MapStruct object converters
├── entity/
│   ├── base/               # Base entities (BaseDO, ResponseVO, PageVO)
│   ├── domain/             # Database entities
│   ├── dto/                # Data transfer objects
│   └── vo/                 # View objects (request/response)
├── enums/                  # Business enumerations
├── mapper/                 # MyBatis-Plus mappers
├── service/                # Business logic interfaces
│   └── impl/              # Service implementations
└── utils/                  # Utility classes (JWT, RSA, IP, Page, etc.)
```

## Quick Start

### Prerequisites

- JDK 8+
- MySQL 8.0+
- Redis 6.0+
- RabbitMQ 3.8+ (optional)
- MinIO (optional)

### Local Development

1. **Create database**

```sql
CREATE DATABASE xd11cc_single DEFAULT CHARACTER SET utf8mb4;
```

Import the schema from `doc/sql/xd11cc_single.sql`.

2. **Configure application**

Update `src/main/resources/application-dev.yml` with your local database, Redis, and other service credentials.

3. **Run**

```bash
mvn spring-boot:run
```

The application starts at `http://localhost:10001/xd11cc`.

API documentation is available at `http://localhost:10001/xd11cc/swagger-ui.html`.

### Docker Deployment

```bash
# Build
mvn clean package -DskipTests

# Build Docker image
docker build -t xd11cc-single .

# Run
docker-compose up -d
```

## API Endpoints

| Module | Endpoint | Description |
|--------|----------|-------------|
| Auth | `POST /login/loginByPassword` | Username/password login |
| Auth | `GET /login/getCaptcha` | Get CAPTCHA code |
| Auth | `GET /login/authorize/{source}` | OAuth2 social login |
| User | `GET /system/user/list` | User management |
| Menu | `GET /system/menu/list` | Menu & permission management |
| Dict | `GET /system/dict/type/list` | Dictionary management |
| Config | `GET /system/config/list` | System configuration |
| File | `POST /file/upload` | File upload (MinIO) |
| CodeGen | `POST /generate/code/preview` | Code generation preview |

## Architecture Highlights

### Multi-Tenant Data Isolation

```
Request → TenantFilter (extract tenant from domain/header)
        → TenantContextHolder (store in TransmittableThreadLocal)
        → TenantDatabaseInterceptor (auto-append WHERE tenant_id = ?)
        → Response
```

### Authentication Flow

```
Login Request → LoginService → Spring Security Authentication
             → JWT Token Generation → Redis Session Storage
             → Return Token to Client

Subsequent Requests → JwtAuthenticationTokenFilter
                    → Validate Token → Load User from Redis
                    → Set SecurityContext → Process Request
```

### Rate Limiting

```java
@RateLimit(time = 60, count = 10, type = RateLimitEnum.IP)
@GetMapping("/api/resource")
public ResponseVO getResource() { ... }
```

## Roadmap

- [x] Organization architecture (User, Role, Menu, Dept, Post)
- [x] Spring Security + JWT authentication
- [x] Swagger2 API documentation
- [x] Druid dynamic multi-datasource
- [x] Redis + Redisson caching & rate limiting
- [x] RabbitMQ message queue
- [x] XXL-Job task scheduling
- [x] Netty WebSocket real-time push
- [x] MinIO file storage
- [x] OAuth2 social login (JustAuth)
- [x] Docker containerization
- [ ] Freemarker code generator templates (in progress)
- [ ] Flowable workflow engine
- [ ] Alipay & WeChat Pay integration

## License

See [LICENSE](LICENSE) for details.
