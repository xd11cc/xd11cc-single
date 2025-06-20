create table system_user(
    id bigint not null primary key auto_increment comment '主键id',
    username varchar(20) not null comment '用户名',
    password varchar(64) not null comment '密码',
    nickname varchar(20) not null comment '昵称',
    id_card varchar(18) comment '身份证号码',
    sex char(2) not null comment '性别，字典类型system_logic_status',
    phone varchar(11) comment '手机号码',
    email varchar(20) comment '邮箱',
    dept_id bigint not null comment '部门id',
    dept_name varchar(20) not null comment '部门名称',
    post_id bigint not null comment '岗位id',
    post_name varchar(20) not null comment '岗位名称',
    status char(2) not null comment '账号状态，字典类型system_enable_status',
    head_url varchar(255) not null comment '头像路径',
    create_user_id bigint not null comment '创建人id',
    create_time datetime not null comment '创建时间',
    update_user_id bigint not null comment '更新人id',
    update_time datetime not null comment '更新时间',
    del_flag tinyint not null default 0 comment '删除标识 0-未删除 1-已删除',
    remark varchar(255) not null comment '备注'
)comment '用户表';

create table system_user_role(
    id bigint not null primary key auto_increment comment '主键id',
    user_id bigint not null comment '用户id',
    role_id bigint not null comment '角色id',
    create_user_id bigint not null comment '创建人id',
    create_time datetime not null comment '创建时间',
    update_user_id bigint not null comment '更新人id',
    update_time datetime not null comment '更新时间',
    del_flag tinyint not null default 0 comment '删除标识 0-未删除 1-已删除',
    remark varchar(255) not null comment '备注'
)comment '用户角色表';

create table system_role(
    id bigint not null primary key auto_increment comment '主键id',
    role_code varchar(20) not null comment '角色编码',
    role_name varchar(20) not null comment '角色名称',
    status char(2) not null comment '角色状态，字典类型system_enable_status',
    create_user_id bigint not null comment '创建人id',
    create_time datetime not null comment '创建时间',
    update_user_id bigint not null comment '更新人id',
    update_time datetime not null comment '更新时间',
    del_flag tinyint not null default 0 comment '删除标识 0-未删除 1-已删除',
    remark varchar(255) not null comment '备注'
)comment '角色表';

create table system_role_menu(
    id bigint not null primary key auto_increment comment '主键id',
    role_id bigint not null comment '角色id',
    menu_id bigint not null comment '菜单id',
    create_user_id bigint not null comment '创建人id',
    create_time datetime not null comment '创建时间',
    update_user_id bigint not null comment '更新人id',
    update_time datetime not null comment '更新时间',
    del_flag tinyint not null default 0 comment '删除标识 0-未删除 1-已删除',
    remark varchar(255) not null comment '备注'
)comment '角色菜单表';

create table system_menu(
    id bigint not null primary key auto_increment comment '主键id',
    parent_id bigint not null default 0 comment '父菜单id',
    menu_name varchar(20) not null comment '菜单名称',
    sort int not null comment '显示排序',
    path varchar(255) not null comment '路由地址',
    if_frame char(2) not null comment '是否为外链，字典类型system_logic_status',
    menu_type char(2) not null comment '菜单类型，字典类型system_menu_type',
    visible char(2) not null comment '是否隐藏，字典类型system_logic_status',
    status char(2) not null comment '菜单状态，字典类型system_enable_status',
    permission varchar(255) comment '权限字符',
    icon varchar(255) comment '菜单图标',
    menu_use_type char(2) not null comment '菜单适用类型，字典类型menu_use_type',
    create_user_id bigint not null comment '创建人id',
    create_time datetime not null comment '创建时间',
    update_user_id bigint not null comment '更新人id',
    update_time datetime not null comment '更新时间',
    del_flag tinyint not null default 0 comment '删除标识 0-未删除 1-已删除',
    remark varchar(255) not null comment '备注'
)comment '菜单表';

create table system_dept(
    id bigint not null primary key auto_increment comment '主键id',
    parent_id bigint not null default 0 comment '父部门id',
    dept_code varchar(20) not null comment '部门编码',
    dept_name varchar(20) not null comment '部门名称',
    leader_id bigint not null comment '部门负责人',
    sort int not null comment '排序',
    status char(2) not null comment '部门状态，字典类型system_enable_status',
    create_user_id bigint not null comment '创建人id',
    create_time datetime not null comment '创建时间',
    update_user_id bigint not null comment '更新人id',
    update_time datetime not null comment '更新时间',
    del_flag tinyint not null default 0 comment '删除标识 0-未删除 1-已删除',
    remark varchar(255) not null comment '备注'
)comment '部门表';

create table system_dept_post(
    id bigint not null primary key auto_increment comment '主键id',
    dept_id bigint not null comment '部门id',
    post_id bigint not null comment '岗位id',
    create_user_id bigint not null comment '创建人id',
    create_time datetime not null comment '创建时间',
    update_user_id bigint not null comment '更新人id',
    update_time datetime not null comment '更新时间',
    del_flag tinyint not null default 0 comment '删除标识 0-未删除 1-已删除',
    remark varchar(255) not null comment '备注'
)comment '部门岗位表';

create table system_post(
    id bigint not null primary key auto_increment comment '主键id',
    post_code varchar(20) not null comment '岗位编码',
    post_name varchar(20) not null comment '岗位名称',
    status char(2) not null comment '岗位状态，字典类型system_enable_status',
    create_user_id bigint not null comment '创建人id',
    create_time datetime not null comment '创建时间',
    update_user_id bigint not null comment '更新人id',
    update_time datetime not null comment '更新时间',
    del_flag tinyint not null default 0 comment '删除标识 0-未删除 1-已删除',
    remark varchar(255) not null comment '备注'
)comment '岗位表';

create table system_dict_type(
    id bigint not null primary key auto_increment comment '主键id',
    type varchar(20) not null comment '字典类型',
    name varchar(20) not null comment '字典名称',
    create_user_id bigint not null comment '创建人id',
    create_time datetime not null comment '创建时间',
    update_user_id bigint not null comment '更新人id',
    update_time datetime not null comment '更新时间',
    del_flag tinyint not null default 0 comment '删除标识 0-未删除 1-已删除',
    remark varchar(255) not null comment '备注'
)comment '字典类型表';

create table system_dict_data(
    id bigint not null primary key auto_increment comment '主键id',
    type varchar(20) not null comment '字典数据类型',
    label varchar(20) not null comment '标签',
    value varchar(20) not null comment '键值',
    value_color varchar(20) comment '键值颜色',
    background_color varchar(20) comment '背景颜色',
    sort int not null comment '排序',
    create_user_id bigint not null comment '创建人id',
    create_time datetime not null comment '创建时间',
    update_user_id bigint not null comment '更新人id',
    update_time datetime not null comment '更新时间',
    del_flag tinyint not null default 0 comment '删除标识 0-未删除 1-已删除',
    remark varchar(255) comment '备注'
)comment '字典数据表';