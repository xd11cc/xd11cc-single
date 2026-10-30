package com.xd11cc.single.enums;

import com.xd11cc.single.common.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @Author: xd11cc
 * @Date: 2025/6/15 10:41
 * 一共 7 位，分成三段
 * 第一段，1 位，类型
 *      1 - 业务级别异常
 *      x - 预留
 * 第二段，3 位，模块
 *      001 - 认证安全
 *      003 - 字典      004 - 菜单
 *      005 - 文件      006 - 代码生成
 *      007 - 社交登录  008 - 租户
 *      009 - 部门      010 - 角色
 *      011 - 岗位      012 - 系统配置
 *      013 - 通知      014 - 分布式锁
 *      015 - 定时任务
 * 第三段，3 位，错误码
 *       不限制规则。
 *       一般建议，每个模块自增。
 **/
@AllArgsConstructor
@Getter
public enum SystemErrorEnum implements ErrorCode {

    // ==================== HTTP 标准错误 ====================
    BAD_REQUEST(400, "请求参数有误"),
    BAD_REQUEST1(400, "请求参数有误，{}"),
    UNAUTHORIZED(401, "未授权"),
    METHOD_NOT_ALLOWED(405, "请求方法不正确"),
    SYSTEM_ERROR(500, "系统异常，请联系管理员处理"),

    // ==================== 001 - 认证安全模块 ====================
    USER_NOT_FOUND(1001001, "用户不存在"),
    PASSWORD_ERROR(1001002, "密码错误"),
    ILLEGAL_VISIT(1001003, "非法访问"),
    USER_FORBIDDEN(1001004, "账户已禁用"),
    USER_LOCKED(1001005, "密码错误超过{}次，账户已锁定"),
    CAPTCHA_ERROR(1001006, "验证码错误"),
    CAPTCHA_EXPIRE(1001007, "验证码已过期"),
    USERNAME_EXISTS(1001008, "用户名已存在"),
    ADMIN_NOT_ALLOW_DELETE(1001009, "管理员账号不允许删除"),
    OLD_PASSWORD_ERROR(1001010, "旧密码错误"),
    NO_PERMISSION(1001011, "没有操作权限"),
    LOGIN_FAILED(1001012, "用户名或密码错误"),
    TOO_MANY_REQUESTS(1001013, "请求过于频繁，请稍后重试"),
    RATE_LIMIT_UNAVAILABLE(1001014, "限流服务暂不可用，请稍后重试"),

    // ==================== 002 - 工作流模块 ====================
    HANDLER_NOT_FOUND(1002001, "未找到对应的工作流处理器"),
    PROCESS_DEFINITION_NOT_FOUND(1002002, "流程定义不存在"),
    PROCESS_INSTANCE_NOT_FOUND(1002003, "流程实例不存在"),
    TASK_NOT_FOUND(1002004, "任务不存在"),
    TASK_NOT_ASSIGNEE(1002005, "无权操作该任务"),
    PROCESS_ALREADY_COMPLETED(1002006, "流程已结束"),
    DEPLOY_FAILED(1002007, "流程部署失败"),
    START_FAILED(1002008, "流程发起失败"),
    PROCESS_CANNOT_CANCEL(1002009, "流程无法取消"),
    WORKFLOW_CATEGORY_CODE_EXISTS(1002010, "流程分类编码已存在"),
    EXPRESSION_CONFIG_KEY_EXISTS(1002012, "表达式配置键已存在"),
    EXPRESSION_CONFIG_KEY_INVALID(1002013, "配置键格式错误"),
    EXPRESSION_TEMPLATE_INVALID(1002014, "表达式模板必须是完整的${...}表达式"),
    EXPRESSION_PARAM_SCHEMA_INVALID(1002015, "参数定义必须是JSON数组"),
    EXPRESSION_CONFIG_NOT_FOUND(1002016, "表达式配置不存在或已停用"),
    EXPRESSION_PARAM_MISSING(1002017, "表达式参数缺失：{}"),
    EXPRESSION_PARAM_UNRESOLVED(1002018, "表达式存在未解析参数"),
    EXPRESSION_PARAM_FORMAT_ERROR(1002019, "表达式参数格式错误"),
    EXPRESSION_PARAM_NUMBER_ERROR(1002020, "数值参数格式错误"),
    EXPRESSION_PARAM_BOOLEAN_ERROR(1002021, "布尔参数格式错误"),
    EXPRESSION_PARAM_VARIABLE_ERROR(1002022, "流程变量名格式错误"),
    EXPRESSION_PARAM_KEY_DUPLICATE(1002023, "参数键为空或重复"),
    EXPRESSION_PLACEHOLDER_MISMATCH(1002024, "模板占位符与参数定义不一致"),
    EXPRESSION_RESOLVE_FAILED(1002025, "流程表达式解析失败：{}"),
    EXPRESSION_CONDITION_NODE_MISSING(1002026, "连线缺少条件表达式节点"),
    EXPRESSION_BEAN_NOT_FOUND(1002027, "表达式引用的Bean不存在：{}"),
    PROCESS_VARIABLE_DEFINITION_INVALID(1002028, "流程变量定义必须是数组"),
    PROCESS_VARIABLE_KEY_INVALID(1002029, "流程变量Key为空、格式错误或重复：{}"),
    MULTI_INSTANCE_NO_TAKE_LISTENER(1002030, "会签/或签节点入边未配置take表达式监听器：{}"),
    MULTI_INSTANCE_NO_INCOMING(1002031, "会签/或签节点不存在入边：{}"),
    EXPRESSION_TYPE_UNSUPPORTED(1002032, "不支持的表达式使用场景"),
    EXPRESSION_RETURN_TYPE_MISMATCH(1002033, "表达式使用场景与返回类型不匹配"),
    LISTENER_EXPRESSION_NO_PARAM(1002034, "监听器表达式不支持动态参数"),
    MULTIPLE_ACTIVE_DEFINITION(1002035, "存在多个已激活的流程版本，请挂起不需要的版本"),
    PROCESS_VARIABLE_REQUIRED_MISSING(1002036, "缺少必填流程变量：{}"),
    PROCESS_VARIABLE_VALIDATE_FAILED(1002037, "流程变量校验失败：{}"),
    PROCESS_VARIABLE_TYPE_NUMBER(1002038, "流程变量必须为数值：{}"),
    PROCESS_VARIABLE_TYPE_BOOLEAN(1002039, "流程变量必须为布尔值：{}"),
    PROCESS_VARIABLE_TYPE_COLLECTION(1002040, "流程变量必须为集合：{}"),
    PROCESS_RESOURCE_READ_FAILED(1002041, "读取流程定义资源失败"),
    DELEGATE_TARGET_USER_INVALID(1002042, "转办目标用户ID必须为数字：{}"),
    BPMN_PROCESS_COUNT_INVALID(1002043, "BPMN 必须且只能包含一个 process，当前数量：{}"),
    BPMN_PROCESS_ID_MISMATCH(1002044, "流程标识与 BPMN 中 processId 不一致：modelKey={}, bpmnProcessId={}"),

    // ==================== 003 - 字典模块 ====================
    DICT_TYPE_HAVE_DATA(1003001, "{}字典类型下存在字典数据"),
    DICT_TYPE_EXISTS(1003002, "字典类型已存在"),
    DICT_DATA_EXISTS(1003003, "字典数据已存在"),
    DICT_TYPE_NOT_EXISTS(1003004, "字典类型不存在"),

    // ==================== 004 - 菜单模块 ====================
    MENU_HAVE_CHILDREN(1004001, "当前目录或菜单下存在子集"),

    // ==================== 005 - 文件模块 ====================
    INIT_MINIO_ERROR(1005001, "初始化 MinIO 失败"),
    MINIO_UPLOAD_FILE(1005002, "文件上传失败"),
    MINIO_GET_FILE(1005003, "获取预览文件失败"),
    MINIO_DELETE_FILE(1005004, "删除文件失败"),
    MINIO_DOWNLOAD_FILE(1005005, "文件下载失败"),
    PDF_TO_WORD_ERROR(1005006, "PDF 转 Word 失败"),

    // ==================== 006 - 代码生成模块 ====================
    TEMPLATE_LOADING_ERROR(1006001, "模板加载失败"),
    GENERATE_CODE_ERROR(1006002, "代码生成失败"),

    // ==================== 007 - 社交登录模块 ====================
    AUTH_SOURCE_FORBIDDEN(1007001, "{}授权方式已被禁用"),
    SOCIAL_USER_ALREADY_BOUND(1007002, "该社交账号已绑定其他用户"),
    SOCIAL_USER_NOT_FOUND(1007003, "社交用户信息不存在"),
    SOCIAL_AUTH_NOT_SUPPORT(1007004, "社交登录不支持"),
    AUTH_SOURCE_EXISTS(1007005, "该应用类型已存在"),
    SOCIAL_AUTH_FAILED(1007006, "第三方授权登录失败：{}"),
    SOCIAL_AUTH_CONFIG_MISSING(1007007, "社交登录跳转配置缺失：{}"),
    SOCIAL_AUTH_REDIRECT_FAILED(1007008, "社交登录页面跳转失败"),

    // ==================== 008 - 租户模块 ====================
    CHOOSE_RIGHT_DOMAIN(1008001, "请使用正确的域名访问"),
    NOT_FOUND_TENANT(1008002, "不存在租户编号"),
    TENANT_DOMAIN_EXISTS(1008003, "租户域名已存在"),

    // ==================== 009 - 部门模块 ====================
    DEPT_CODE_EXISTS(1009001, "部门编码已存在"),
    DEPT_HAVE_CHILDREN(1009002, "当前部门下存在子部门"),
    DEPT_PARENT_CANNOT_SELF(1009003, "父部门不能为自身"),

    // ==================== 010 - 角色模块 ====================
    ROLE_CODE_EXISTS(1010001, "角色编码已存在"),
    ROLE_BOUND_USER(1010002, "角色已分配用户，不允许删除"),

    // ==================== 011 - 岗位模块 ====================
    POST_CODE_EXISTS(1011001, "岗位编码已存在"),

    // ==================== 012 - 系统配置模块 ====================
    CONFIG_KEY_EXISTS(1012001, "配置键已存在"),

    // ==================== 013 - 通知模块 ====================
    NOTICE_NOT_FOUND(1013001, "通知不存在"),
    NOTICE_ALREADY_PUBLISHED(1013002, "通知已发布，不允许修改"),
    NOTICE_NOT_PUBLISHED(1013003, "通知未发布，不允许撤回"),

    // ==================== 014 - 分布式锁 ====================
    LOCK_ACQUIRE_FAILED(1014001, "系统繁忙，请稍后重试"),

    // ==================== 015 - 定时任务 ====================
    JOB_NOT_FOUND(1015001, "定时任务不存在"),
    JOB_EXISTS(1015002, "定时任务已存在"),
    CRON_EXPRESSION_ERROR(1015003, "Cron 表达式格式错误"),
    JOB_RUN_FAILURE(1015004, "任务执行异常：{}"),

    // ==================== 016 - 消息队列模块 ====================
    MQ_TENANT_CONTEXT_MISSING(1016001, "发送MQ消息失败：当前上下文无租户信息"),
    MQ_MESSAGE_EMPTY(1016002, "发送MQ消息失败：消息体为空"),
    MQ_TENANT_ID_REQUIRED(1016003, "发送MQ消息失败：tenantId 不能为空"),
    MQ_HEADER_TENANT_MISSING(1016004, "MQ消费失败：消息头缺少 tenantId"),
    MQ_TENANT_ID_EMPTY(1016005, "MQ消费失败：租户ID为空"),
    MQ_TENANT_NOT_EXISTS(1016006, "MQ消费失败：租户不存在或已停用"),
    MQ_FAIL_RECORD_NOT_FOUND(1016007, "MQ失败消息不存在"),
    MQ_FAIL_REPLAY_NOT_ALLOWED(1016008, "仅待处理状态的消息允许重放"),
    MQ_QUEUE_UNDEFINED(1016009, "队列未定义，无法重放"),
    MQ_DELAY_QUEUE_REQUIRED(1016010, "发送延迟消息失败：目标队列未声明为延迟队列"),
    MQ_DELAY_VALUE_INVALID(1016011, "发送延迟消息失败：延迟时长必须大于 0"),
    MQ_DELAY_UNIT_REQUIRED(1016012, "发送延迟消息失败：时间单位不能为空"),
    MQ_FAIL_DATA_EMPTY(1016013, "消息内容不能为空"),
    MQ_FAIL_DATA_INVALID(1016014, "消息内容不是合法的JSON"),

    ;

    private final int errorCode;

    private final String errorMsg;
}
