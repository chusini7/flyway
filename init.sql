create table environment_config
(
    id          int auto_increment
        primary key,
    env         varchar(32)                        not null,
    url         varchar(256)                       not null,
    user        varchar(32)                        not null,
    password    varchar(64)                        not null,
    create_time datetime default CURRENT_TIMESTAMP not null
);