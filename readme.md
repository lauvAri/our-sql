# our-sql

![oursql](assets/oursql.png)

这是一个综合操作系统、编译原理和数据库系统的项目

## 项目结构

```text
our-sql
|--html # 前端网页
|--script # 自动化脚本（用于测试和启动项目）
|--src
    |--main
        |--java
            |--cli # 命令行接口，项目入口
            |--common # 公有方法和配置
            |--executor # SQL执行器
            |--parser # SQL解析
            |--store # 文件存储
```
## 构建与运行

开发环境：Java 21 + Maven 3.9.9

### 构建CLI
```bash
mvn clean package # 构建项目
java -jar ./target/our-sql-1.0-SNAPSHOT.jar # 运行项目
```

接下来你会进入一个以"oursql>"开头的交互式命令行，你可以在里面测试<a href="#sql">sql语句</a>

如果你使用Linux发行版，你可以进入`scripts/`查看启动脚本

### 构建前端工程

```bash
cd html
npm i # 如果你的网络条件不够好，你可以先通过 npm install -g nrm ，再通过 nrm 管理镜像，推荐使用tencent或者taobao镜像
npm run start
```

访问 http://localhost:9000 查看网页, 接下来，可以使用[ttyd](https://github.com/tsl0922/ttyd)构建服务端，你可以在参考[`scripts/run.sh`](https://github.com/lauvAri/our-sql/blob/master/scripts/run.sh)的启动参数，现在网页端就可以通过websocket与服务端进行通信了

## 分支协作

采用功能特性feature命名法，代码合并通过github的workflow中的pull request实现，保证代码质量审查。如果有合并测试分支，采用test-前缀命名法

- master # 最终代码
- feat-parse # SQL解析分支 maintained by [张辰铭](https://github.com/zhangchenming4017)
- feat-executor # SQL执行器分支 maintained by [刘喜贺](https://github.com/ONeofSu)
- feat-storage # 文件存储分支 maintained by [周攀豪](https://github.com/lauvAri)

## 项目介绍

该项目由三个大模块组成，包括SQL编译器、SQL执行器以及SQL存储器，涉及的专业知识包括：编译原理、数据库系统概念、操作系统原理。

支持的基础SQL语句包括： <code>create table</code>, <code>insert into</code>, <code>select from</code>, <code>delete from</code>；高级SQL语句包括：<code>show tables</code>, <code>update</code>, <code>limit</code>, <code>order by</code>




