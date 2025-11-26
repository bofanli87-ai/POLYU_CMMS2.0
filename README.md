# 校园维护与管理系统 (CMMS)

## 项目简介

校园维护与管理系统是一个基于Java开发的桌面应用程序，用于管理校园内的各类维护活动、人员安排和数据报表生成。系统提供了活动管理、人员管理、数据查询和报表生成等核心功能，旨在提高校园维护工作的效率和管理水平。

## 项目结构

项目采用经典的MVC（Model-View-Controller）架构设计，代码组织清晰：
TiDB/
├── src/main/java/com/polyu/cmms/   # 主源码目录
│   ├── Main.java                    # 程序入口
│   ├── controller/                  # 控制器层
│   ├── model/                       # 数据模型层
│   │   ├── Activity.java            # 活动模型
│   │   ├── Staff.java               # 员工模型
│   │   └── WorksFor.java            # 员工-活动关联模型
│   ├── service/                     # 业务逻辑层
│   │   ├── ActivityService.java     # 活动管理服务
│   │   ├── StaffService.java        # 员工管理服务
│   │   ├── AuthService.java         # 权限认证服务
│   │   └── ...                      # 其他服务类
│   ├── util/                        # 工具类
│   │   ├── DatabaseUtil.java        # 数据库工具
│   │   ├── HtmlLogger.java          # 日志工具
│   │   └── createdata/              # 数据初始化工具
│   └── view/                        # 视图层
│       ├── MainFrame.java           # 主窗口
│       ├── ActivityManagementPanel.java # 活动管理面板
│       ├── StaffManagementPanel.java   # 人员管理面板
│       └── ...                      # 其他界面面板
├── pom.xml                          # Maven配置文件
└── README.md                        # 项目说明文档


## 技术栈

- **开发语言**: Java 24
- **GUI框架**: Java Swing
- **数据库**: MySQL/TiDB
- **JDBC驱动**: mysql-connector-j 8.4.0
- **构建工具**: Maven
- **版本控制**: Git

## 核心功能模块

### 1. 活动管理

- 活动创建、编辑、删除
- 活动状态更新
- 活动详情查看
- 员工分配到活动
- 活动查询和筛选

### 2. 人员管理

- 员工信息管理
- 员工角色和权限管理
- 员工状态管理
- 员工数据查询和报表

### 3. 数据管理

- 校园设施数据管理（建筑物、房间等）
- 数据导入导出
- 数据维护和更新

### 4. 报表与查询

- 活动报表生成
- 人员工作报表
- 数据统计分析
- 自定义查询功能

## 权限管理

系统实现了基于角色的访问控制（RBAC），主要权限包括：

- `MANAGE_STAFF`: 管理员工信息权限
- `VIEW_STAFF`: 查看员工信息权限
- `MANAGE_ACTIVITY`: 管理活动权限
- `VIEW_ACTIVITY`: 查看活动权限
- `VIEW_REPORT`: 查看报表权限
- `GENERATE_REPORT`: 生成报表权限

根据用户权限，系统会动态显示不同的功能面板。

## 数据模型

### 核心实体

1. **Staff（员工）**
   - 基本信息：ID、姓名、编号、年龄、性别等
   - 工作信息：角色、入职日期、联系方式等
   - 状态信息：是否在职、职责描述等

2. **Activity（活动）**
   - 基本信息：ID、类型、标题、描述等
   - 时间信息：计划日期、预计耗时、实际完成时间等
   - 位置信息：建筑物、区域、房间等
   - 状态信息：状态、优先级、危险级别等

3. **WorksFor（员工-活动关联）**
   - 记录员工与活动的分配关系
   - 包含活动职责描述等信息

## 日志系统

系统使用HtmlLogger记录用户操作日志，包括：
- 用户登录/登出
- 数据增删改查操作
- 错误和异常信息

日志以HTML格式保存，方便查看和分析。

## 数据库连接

系统使用DatabaseUtil工具类管理数据库连接，支持MySQL和TiDB数据库。数据库连接配置可在util包中修改。

## 安装与运行

### 前置条件

- JDK 24或更高版本
- Maven 3.6+（用于构建项目）
- MySQL或TiDB数据库

### 构建步骤

1. 克隆项目到本地
2. 配置数据库连接（修改DatabaseUtil类）
3. 初始化数据库表（运行util/createdata目录下的脚本）
4. 编译项目：`mvn clean compile`
5. 打包项目：`mvn package`
6. 运行程序：`java -jar target/TiDB-1.0-SNAPSHOT.jar`

### 开发环境配置

推荐使用IntelliJ IDEA或Eclipse作为开发环境，导入Maven项目即可开始开发。

## 注意事项

1. 首次运行需要初始化数据库表结构
2. 确保正确配置数据库连接信息
3. 系统使用TiDB兼容的SQL语法，可在MySQL或TiDB环境中运行
4. 日志文件默认保存在项目根目录下的logs文件夹
