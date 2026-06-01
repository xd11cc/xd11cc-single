package com.xd11cc.single.enums;

import com.xd11cc.single.config.exception.ErrorCode;
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
 *      001 - 认证安全  002 - 工作流(WorkflowErrorEnum)
 *      003 - 字典      004 - 菜单
 *      005 - 文件      006 - 代码生成
 *      007 - 社交登录  008 - 租户
 *      009 - 部门      010 - 角色
 *      011 - 岗位      012 - 系统配置
 *      013 - 通知
 * 第三段，3 位，错误码
 *       不限制规则。
 *       一般建议，每个模块自增。
 **/
@NoArgsConstructor
@AllArgsConstructor
@Getter
public enum SystemErrorEnum implements ErrorCode {

    // ==================== HTTP 标准错误 ====================
    BAD_REQUEST(400, "请求参数有误"),
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

    // ==================== 003 - 字典模块 ====================
    DICT_TYPE_HAVE_DATA(1003001, "{}字典类型下存在字典数据"),
    DICT_TYPE_EXISTS(1003002, "字典类型已存在"),
    DICT_DATA_EXISTS(1003003, "字典数据已存在"),
    DICT_TYPE_NOT_EXISTS(1003004, "字典类型不存在"),

    // ==================== 004 - 菜单模块 ====================
    MENU_HAVE_CHILDREN(1004001, "当前目录或菜单下存在子集"),

    // ==================== 005 - 文件模块 ====================
    INIT_MINIO_ERROR(1005001, "初始化Minio失败"),
    MINIO_UPLOAD_FILE(1005002, "文件上传失败"),
    MINIO_GET_FILE_FILE(1005003, "获取预览文件失败"),
    MINIO_DELETE_FILE_FILE(1005004, "删除文件失败"),
    MINIO_DOWNLOAD_FILE_FILE(1005005, "文件下载失败"),
    PDF_TO_WORD_ERROR(1005006, "pdf转word失败"),

    // ==================== 006 - 代码生成模块 ====================
    TEMPLATE_LOADING_ERROR(1006001, "模版加载失败"),
    GENERATE_CODE_ERROR(1006002, "代码生成失败"),

    // ==================== 007 - 社交登录模块 ====================
    AUTH_SOURCE_FORBIDDEN(1007001, "{}授权方式以被禁用"),
    SOCIAL_USER_BINDEDE(1007002, "该社交账号已绑定其他用户"),
    SOCIAL_USER_NOT_FOUND(1007003, "社交用户信息不存在"),
    SOCIAL_AUTH_NOT_SUPPORT(1007004, "社交登录不支持"),
    AUTH_SOURCE_EXISTS(1007005, "该应用类型已存在"),

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
    ROLE_BINDEDE_USER(1010002, "角色已分配用户，不允许删除"),

    // ==================== 011 - 岗位模块 ====================
    POST_CODE_EXISTS(1011001, "岗位编码已存在"),

    // ==================== 012 - 系统配置模块 ====================
    CONFIG_KEY_EXISTS(1012001, "配置键已存在"),

    // ==================== 013 - 通知模块 ====================
    NOTICE_NOT_FOUND(1013001, "通知不存在"),
    NOTICE_ALREADY_PUBLISHED(1013002, "通知已发布，不允许修改"),
    NOTICE_NOT_PUBLISHED(1013003, "通知未发布，不允许撤回"),

    ;

    private int errorCode;

    private String errorMsg;
}
