-- 创建库
create database if not exists ai_code_helper;

-- 切换库
use ai_code_helper;

-- 用户表
create table if not exists `user`
(
    id           bigint auto_increment primary key,
    user_account  varchar(256)                           not null comment '账号',
    user_password varchar(512)                           not null comment '密码',
    create_time   datetime     default CURRENT_TIMESTAMP not null comment '创建时间'
) comment '用户' collate = utf8mb4_unicode_ci;

-- 聊天记录表
create table if not exists `chat_message`
(
    id             bigint auto_increment primary key,
    user_id         bigint                                 not null comment '用户id',
    memory_id       int                                    not null comment '记忆/会话id',
    assistant_type varchar(64)                            not null comment '助手类型(code/travel/essay/material/medical/college)',
    role           varchar(32)                            not null comment '角色(user/ai)',
    content        text                                   not null comment '内容',
    create_time     datetime     default CURRENT_TIMESTAMP not null comment '创建时间'
) comment '聊天记录' collate = utf8mb4_unicode_ci;

-- 会话表
create table if not exists `chat_session`
(
    id             bigint auto_increment primary key,
    user_id         bigint                                 not null comment '用户id',
    assistant_type varchar(64)                            not null comment '助手类型',
    name           varchar(128)                           not null comment '会话名称',
    create_time     datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time     datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间'
) comment '聊天会话' collate = utf8mb4_unicode_ci;

-- Agent消息表
create table if not exists `agent_message`
(
    id             bigint auto_increment primary key,
    user_id         bigint                                 not null comment '用户id',
    session_id      bigint                                 not null comment '会话id',
    role           varchar(32)                            not null comment '角色(user/agent)',
    content        text                                   comment '内容(用户输入或Agent最终回答)',
    thought        text                                   comment '思考过程',
    action         varchar(128)                           comment '工具名称',
    action_input    text                                   comment '工具参数',
    observation    text                                   comment '观察结果',
    create_time     datetime     default CURRENT_TIMESTAMP not null comment '创建时间'
) comment 'Agent消息记录' collate = utf8mb4_unicode_ci;
