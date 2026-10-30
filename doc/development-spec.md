# 后端开发规范

适用范围：`xd11cc-single`（Spring Boot 2.7 + Java 8 + MyBatis-Plus）。
前端规范见前端仓库 `docs/development-spec.md`，本文件只管后端。

> **本文件是「怎么做」——步骤与代码示例。**
> **「不能做什么、为什么」见 [AGENTS.md](../AGENTS.md) 的「关键约束」。**
> 两边不重复：约束只写一份，这里只给操作路径。

---

## 1. 代码放哪

```
controller → service → mapper → entity
     ↓           ↓
   common / infrastructure / modules
```

| 要加的东西 | 放哪 |
|---|---|
| 新接口 | `controller/`，按资源命名 `XxxController` |
| 业务逻辑 | `service/` + `service/impl/` |
| 跨实体/带引擎的业务 | `modules/<模块名>/`（参考 `pay`、`workflow`） |
| 通用能力（不依赖业务） | `common/` |
| 中间件连接 | `infrastructure/` |
| 实体 / 入参出参 / 内部流转对象 | `entity/domain`、`entity/vo`、`entity/dto` |

拿不准时的判据：**它依赖业务语义吗？** 依赖就进 `modules`，不依赖才进 `common`。

---

## 2. 建实体与传输对象

| 类型 | 位置 | 命名 |
|---|---|---|
| DO | `entity/domain` | `XxxDO`，继承 `BaseDO` 或 `BaseTenantDO` |
| VO | `entity/vo` | `XxxAddVO` / `XxxUpdateVO` / `XxxQueryVO` / `XxxVO` |
| DTO | `entity/dto` | 内部流转、跨模块传递 |

```java
// 需要租户隔离的表
public class SystemNoticeDO extends BaseTenantDO { ... }

// 全局表（已进 ignoreTables 的）
public class SystemMenuDO extends BaseDO { ... }
```

`BaseDO` 已含 `createUserId / createTime / updateUserId / updateTime / delFlag`，不要重复声明。
字段注释写数据库含义，不要写「这是一个 xxx」。

---

## 3. 加一个字典驱动的状态

四步，顺序不能反：

```
1. DB 列：char(2) + 注释写明字典类型
2. 枚举：只放 code
3. 字典：system_dict_type + system_dict_data
4. 实体 / VO：String 类型
```

```sql
`status` char(2) NOT NULL DEFAULT '0' COMMENT '状态，字典类型mq_fail_status（0-待处理 1-已重放）'
```

```java
public enum MqFailStatus {
    PENDING("0"),
    REPLAYED("1"),
    ;
    private final String code;
}
```

字典数据要满足：`value` 填编码、`label` 填文案、`sort` 从 1 递增且互不相同、`list_class` 用 Element Plus 标签色
（`primary` / `success` / `info` / `warning` / `danger`）。

> 约束细节见 AGENTS.md「状态与枚举」「字典」两节。

改完记得清字典缓存：Redis 里 `dict_type:` 前缀的 key，或重启。直接改 SQL 不会刷新缓存。

---

## 4. 加一个错误码

1. 打开 `SystemErrorEnum`，找到目标模块的段
2. 追加到该段末尾，序号 +1
3. 需要变量时用 `{}` 占位

```java
// 002 - 工作流模块
PROCESS_CANNOT_CANCEL(1002009, "流程无法取消"),
EXPRESSION_PARAM_MISSING(1002017, "表达式参数缺失：{}"),
```

```java
throw new ServiceException(SystemErrorEnum.EXPRESSION_PARAM_MISSING, new Object[]{paramKey});
```

全局异常处理器会去掉 `code:` 前缀并格式化占位符，前端拿到的 `msg` 已经是实参填好的。

> 段位分配与「不许寄生」的规则见 AGENTS.md「错误码」。

---

## 5. 涉及多租户时

先确认租户，再碰数据库：

```java
// 正确：请求进来就有租户（TenantFilter 已设好），直接查
public List<SystemUserDO> getList(SystemUserQueryVO vo) {
    return baseMapper.selectList(wrapper);   // 拦截器自动加 WHERE tenant_id = ?
}
```

手动跨租户执行：

```java
TenantContextHolder.runAsTenant(tenantId, () -> {
    // 这段以指定租户身份执行
});
```

**手写 SQL 时**拦截器不生效，必须自己带条件：

```xml
<select id="countByType" resultType="int">
    SELECT COUNT(*) FROM system_notice
    WHERE tenant_id = #{tenantId} AND del_flag = 0
</select>
```

> 为什么「租户上下文早于 DB 访问」、两种豁免的区分，见 AGENTS.md「多租户」。

---

## 6. 改表结构 / 改种子数据

两个 SQL 文件手工维护，**改完必须同步**：

1. 改实体 / 改数据库
2. 同步 `doc/sql/xd11cc_single.sql` 的 `CREATE TABLE`
3. 有初始化数据的，同步 `doc/sql/xd11cc_single_data.sql`
4. 验证脚本与真实库一致

```bash
# 导入临时库后与真实库比对，期望无差异
mysql -uroot -p xd11cc_check < doc/sql/xd11cc_single.sql
mysql -uroot -p xd11cc_check < doc/sql/xd11cc_single_data.sql
```

脚本与真实库不一致时，**以真实库为准修脚本**，不要反过来。

表名用 `模块_实体`（`system_dict_data`、`workflow_task_record`），所有列必须有 `COMMENT`。

> 三份脚本的职责边界见 AGENTS.md「SQL 脚本」。

---

## 7. 写一个 MQ 生产者 / 消费者

### 生产

```java
mqMessageSender.send(MqQueueEnum.WORKFLOW_START, message);
mqMessageSender.sendDelay(MqQueueEnum.DEMO_DELAY, message, 30, TimeUnit.SECONDS);
```

事务里调用会自动挂到 `afterCommit`，事务回滚则不发送——不需要自己写补偿。

### 消费

```java
@Component
@RabbitListener(queues = "mq_demo_queue")
public class DemoConsumer {

    @Autowired
    private MqConsumeTemplate mqConsumeTemplate;

    @RabbitHandler
    public void onMessage(DemoMessage message) {
        mqConsumeTemplate.consume(() -> {
            // 业务逻辑，不要 try-catch
        });
    }
}
```

`Message` / `Channel` / `deliveryTag` 不进业务方法；租户上下文由 `MqTenantAspect` 设好；
`consume()` 负责「租户校验 → 延迟队列重试 → 失败落表 → 进死信」。

### 新队列

在 `MqQueueEnum` 加一项：

```java
DEMO("mq_demo_queue", "mq_demo_exchange", "mq.demo", false),
//                                                      ↑ delayed
```

> 为什么不能直接碰 `RabbitTemplate`、为什么业务里不许 catch，见 AGENTS.md「消息队列」。

---

## 8. 写一个接口

### 路径与返回

```java
@RestController
@RequestMapping("/dict/type")
public class SystemDictTypeController {

    @PostMapping("/add")
    public ResponseVO<Integer> add(@Valid @RequestBody SystemDictTypeAddVO vo) {
        int row = service.add(vo);
        return row > 0 ? ResponseVO.success(row, "新增成功") : ResponseVO.fail("新增失败");
    }
}
```

分页统一：

```java
return PageUtils.page(queryVO, () -> service.getList(queryVO));
```

列表接口入参继承 `BasePageVO`。不用 `ResponseEntity` 手工包装。

### 注解三件套

```java
@ApiOperation("新增字典类型")
@PostMapping("/add")
@PreAuthorize("@ss.hasPermission('system:dictType:add')")
@OperateLog(module = "字典类型", operateType = OperateTypeEnum.ADD)
```

权限编码格式 `模块:资源:动作`。

### 限流

```java
@RateLimit(permits = 5, windowSeconds = 60,
        subject = {Subject.IP, Subject.EXPRESSION}, expression = "#p0.username")
public ResponseVO<String> loginByPassword(@RequestBody LoginPasswordVO vo) { ... }
```

- `subject` 可多选，任一超限即拒绝，按声明顺序短路
- `expression` 用参数索引（`#p0`）而不是参数名，避免依赖 `-parameters` 编译选项
- 超限返回 HTTP 200 + 业务码，前端按 code 判断
- 内部路径需要限流时直接注入 `RateLimiter`，注解在自调用时不生效

> 两条策略（HTTP fail-closed / WS fail-open）与规格指纹机制见 AGENTS.md「限流」。

---

## 9. 写测试

当前 800+ 用例。约定：

- 继承 `BaseUnitTest`（已注入 `MockitoExtension`）
- JUnit 5 + AssertJ + Mockito BDD（`given` / `then`）
- 方法名用中文描述行为：`cancelProcess_completedStatus`
- 用 `@DisplayName` 写业务意图
- 严格桩报 `UnnecessaryStubbingException` 时，用 `@MockitoSettings(strictness = Strictness.LENIENT)`

**改行为前先补特征测试**，把现有行为钉住再动手。反面案例：`cancelProcess` 的状态校验分支长期无覆盖，
把 `!=` 改成 `equals` 时测试完全发现不了。

**不要假装的测试**：

- 只 `verify(mock)` 被调用、不断言结果的
- 只测 getter/setter 的

---

## 10. 提交前检查

**通用**

- [ ] `mvn -o test` 全绿
- [ ] 改过表结构 → 结构脚本已同步
- [ ] 加了错误码 → 落在正确的段，序号未复用
- [ ] 加了状态列 → 已补字典，`value`=编码 / `label`=文案

**改过字段类型**（七项逐一确认，清单见 AGENTS.md 第 23 条）

- [ ] 没有残留 `==` / `!=` 比较
- [ ] Mapper XML 里的硬编码值已同步
- [ ] 调用方传入的字面量已同步（前端下拉值、表单默认值）

**涉及 MQ**

- [ ] 只用了 `MqMessageSender`
- [ ] 消费方法内没有 `catch`
- [ ] 新队列进了 `MqQueueEnum`

**涉及多租户**

- [ ] 租户上下文早于数据库访问
- [ ] 原生 SQL 自带租户条件，或该表已进 `ignoreTables`

**涉及限流**

- [ ] 注解在能被外部调用的 public 方法上（不是自调用路径）
- [ ] 阈值变更后确认新桶生效（key 里的规格指纹变了）
