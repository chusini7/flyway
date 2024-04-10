## 数据库结构同步工具

### 功能简介

> 支持MySQL数据库表、视图、函数、存储过程、事件的模板化导出，数据源、导出目录、模板均可自定义配置

### 环境说明
> 目前支持windows X64以及linux X64系统使用

### 使用

全局参数：

> `--file` 配置文件，默认`./config.yml`，定义数据源、导出配置、模板
>
> `--source` 指定数据源，默认`dev`

以上--file可使用短参数-f，--source短参数-s

#### 支持导出的类型

> table 表
>
> view 视图
>
> function 函数
>
> procedure 存储过程
>
> event 事件/触发器

#### 按类型导出

```bash
$ ./migration [type] [options]
```

> type 对应导出类型，options为导出参数

#### 示例

##### 导出表Create语句示例：

```bash
$ ./migration table [TABLES] -f ./config.yml -s test
```

例如要导出release环境 a b c三张表，可执行下列命令

```bash
$ ./migration table a b c -f ./config.yml -s release
```

#### 导出所有

```bash
$ ./migration all [type] [options]
```

> 以上type与导出类型对应，无需指定具体表、存储过程等名称

### 配置

#### 数据源配置

> 支持多个数据源，在导出时加参数`--source=xxx`或`-s xxx`即可使用对应数据源

```yaml
datasource:
  mysql:
    dev:
      user: root
      password: eFreight.cn
      net: tcp
      addr: 172.16.10.119
      port: 3307
      schema: ef_cloud
      charset: utf8
      collation: ""
    test:
      user: root
      password: eFreight.cn
      net: tcp
      addr: 172.16.10.119
      port: 3306
      schema: ef_cloud
      charset: utf8
      collation: ""
    release:
      user: root
      password: eFreight.cn
      net: tcp
      addr: 172.16.10.119
      port: 3307
      schema: ef_cloud
      charset: utf8
      collation: ""
```

#### 模板配置

> 如未配置模板，默认加载`./templates/default.tmpl`文件

```yaml
template:
  - ./templates/default.tmpl
```

#### 导出配置

> 不同脚本可各自配置目录、模板、命名规则

```yaml
save:
  view:
    path: ./views-migration
    tmpl: '{{ template "universal_migration_template" . }}'
    prefix: R__
    suffix: .sql
  table:
    path: ./tables-migration
    tmpl: '{{ template "table_migration_template" . }}'
    prefix: R__
    suffix: .sql
  function:
    path: ./functions-migration
    tmpl: '{{ template "universal_migration_template" . }}'
    prefix: R__
    suffix: .sql
  procedure:
    path: ./procedures-migration
    tmpl: '{{ template "universal_migration_template" . }}'
    prefix: R__
    suffix: .sql
  event:
    path: ./events-migration
    tmpl: '{{ template "universal_migration_template" . }}'
    prefix: R__
    suffix: .sql
  ddl:
    path: ./ddl-migration
    tmpl: '{{ template "universal_migration_template" . }}'
    prefix: V__
    suffix: .sql
  dml:
    path: ./dml-migration
    tmpl: '{{ template "universal_migration_template" . }}'
    prefix: V__
    suffix: .sql
```



### 注意事项

**未使用连接池，使用all命令时可能会导致连接数过多错误，如出现，请执行以下命令到目标数据库**

```mysql
SET GLOBAL max_connections = 500;
```

