drop table if exists aaa_test;
create table aaa_test
(
    id int auto_increment,
    name varchar(32) null,
    age int null,
    constraint aaa_test_pk
        primary key (id)
);