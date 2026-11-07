# 从 GitHub 到图书管理系统：完整过程与知识点

> 时间：2026-11-07 晚上（约 2.5 小时）
> 结果：`https://github.com/Luochuanxiang/library-system`，一个能增删改查的 Java Swing + MySQL 桌面程序

---

## 一、时间线：我们到底做了哪些事

### 阶段 0：准备账号（从零开始）

| 做了什么 | 为什么 |
|---|---|
| 用 19862768798@163.com 注册 GitHub | 求职专用邮箱，手机号邮箱好记 |
| 用户名从 `luo.c.x` 改成 `luochuanxiang` | GitHub 用户名只能字母数字和单个连字符，不能有点 |
| 发现 GitHub 界面没有官方中文 | 社区在用 Weblate 慢慢翻译；翻译插件是机翻，会翻错（Feed→“饲料”） |

### 阶段 1：装 Git（版本控制工具）

| 做了什么 | 踩到的坑 |
|---|---|
| 用 npmmirror 镜像下载 `Git-2.56.0-64-bit.exe` | winget（Windows 包管理器）下载卡死 |
| 用 `Start-Process -Verb RunAs` 静默安装 | 直接执行报 `error 5 拒绝访问`（需要管理员） |
| 把 `C:\Program Files\Git\cmd` 加进用户 PATH | 安装时没勾“加到 PATH”，导致 `git` 命令找不到 |

### 阶段 2：配置 Git 并认识它

```bash
git config --global user.name  "罗传翔"
git config --global user.email "luochuanxiang@users.noreply.github.com"
git config --global init.defaultBranch main
git config --global core.quotepath false
git config --global credential.helper manager
```

| 知识点 | 说明 |
|---|---|
| Git 是什么 | 代码的“存档/快照”工具：每次 commit 存一张快照，能回退、能对比、能多人协作 |
| 工作目录 / 暂存区 / 本地仓库 / 远程仓库 | 改文件 → `git add`（放进暂存）→ `git commit`（拍快照）→ `git push`（上传） |
| 为什么用 `noreply` 邮箱 | GitHub 开了“邮箱隐私保护”，用真实邮箱 push 会报 `GH007: Your push would publish a private email address` |
| credential helper（凭据助手） | 帮你记住账号密码，push 时不用每次输 |

### 阶段 3：建第一个仓库

1. GitHub 网页：新建仓库 `library-system`（Public，不勾 README）
2. 本地：`git init` → 写 README.md → `git add` → `git commit`（`11a4a98`）→ `git push`
3. 遇到 GH007 → 改成 noreply 邮箱 → `git commit --amend --reset-author` → 推送成功

### 阶段 4：把数据从 C 盘搬到 D 盘

- C: 200GB 已用 162.6GB（剩 38GB），D: 723GB 剩 331GB
- 项目从 `C:\Users\asus\projects\library-system` 移到 `D:\projects\library-system`
- **关键**：`.git` 文件夹跟着走，remote 配置不变，迁移后照常 commit/push

### 阶段 5：MySQL 数据库（最曲折的一步）

| 做了什么 | 结果 |
|---|---|
| 发现已装 MySQL 8.4.9，但从未配置（无 my.ini、无数据目录） | 从零初始化 |
| `mysqld --initialize-insecure --datadir=D:/MySQLData` | 数据目录放 D 盘，省 C 盘空间 |
| 写 `D:\MySQLData\my.ini` 配置 basedir/datadir/端口/字符集 | 统一配置，方便管理和迁移 |
| 启动失败：报 `Too many arguments (first extra is 'Files/MySQL/MySQL')` | PowerShell 传参时把 `C:\Program Files\...` 的空格拆断了 |
| 改用 `--defaults-file` 单参数启动 | 解决 |
| 数据库进程莫名“自己死了” | 之前用 bat 里的 `start` 启动，进程挂在命令窗口下，窗口一关被带走；改成独立隐藏进程启动 |
| 设 root 密码 `root1234`，建库 `library_system`（utf8mb4） | 本地开发环境，密码简单好记 |

**为什么不能装成 Windows 服务？** 当前会话没有管理员权限（`net session` 报系统错误 5），所以只能手动启动（双击 `D:\projects\start-mysql.bat`），电脑重启后不会自动运行。

### 阶段 6：JDBC 驱动 + 建表 + 最小连接测试

| 做了什么 | 说明 |
|---|---|
| 下载 `mysql-connector-j-8.4.0.jar`（2.5MB） | Maven 坐标是 `com.mysql:mysql-connector-j`，用阿里云镜像下载最快 |
| 建 `books` 表 | id / title / author / price / stock / created_at |
| 写 `DbTest.java` 验证连接 | 打印“连接成功！MySQL 版本：8.4.9” |

**链路**：Java 程序 → JDBC 驱动（翻译官）→ MySQL 服务器 → `D:\MySQLData` 数据文件

### 阶段 7：DAO 层（数据访问层）

| 文件 | 作用 |
|---|---|
| `Book.java` | 实体类，一个对象 = 表里一行 |
| `DbUtil.java` | 统一提供数据库连接（地址/账号/密码只写一次） |
| `BookDao.java` | 增 `add`、删 `delete`、改 `update`、查 `listAll` / `findById` |
| `DaoTest.java` | 控制台测试：加 3 本 → 查 → 改 → 删 → 删不存在的 |

**测试全通过**（这是“地基通了”的标志）。

### 阶段 8：界面 5a——只读表格窗口

| 文件 | 作用 |
|---|---|
| `BookTableModel.java` | 表格模型：告诉 JTable 有几行几列、每格显示什么 |
| `MainFrame.java` | 主窗口：JFrame + JTable + JScrollPane |

### 阶段 9：界面 5b——完整增删改查

| 文件/功能 | 作用 |
|---|---|
| `BookDialog.java` | 添加/修改的模态对话框，含输入校验 |
| 工具栏 4 个按钮 | 刷新 / 添加图书 / 修改选中 / 删除选中 |
| 双击行 | 直接打开修改 |
| 删除二次确认 | 防止误删 |

### 阶段 10：收尾

- 重写 README（功能、截图、技术栈、结构、运行方法、后续计划）
- 加 `sql/init.sql`（建库建表 + 示例数据，别人能一键初始化）
- 界面截图存 `docs/screenshot.png`
- `.gitignore`：忽略 `out/`、`*.class`、`.idea/`、`*.iml`

**最终 6 次提交**：

```
4815c92 第 5 步完成：增删改查界面（按钮+对话框）、README、建库脚本、界面截图
3caa744 第 5 步 5a：表格窗口显示图书列表
16feaf9 补充 .gitignore：忽略 IDEA 的 .idea 和 *.iml
492660a 完成 DAO 层：Book 实体、BookDao 增删改查、连接测试通过
7abe20c 搭建数据库环境：加入 JDBC 驱动、建 books 表、验证 Java 连接 MySQL
ecc4a4f 初始化项目：添加 README
```

---

## 二、知识点清单（按重要程度）

### 1. Git / GitHub（求职必备）

- **仓库（repository）**：一个项目一个仓库，有本地和远程两份
- **commit（提交）**：一次快照，带一句说明；历史就是一条提交链
- **push（推送）/ pull（拉取）**：本地和远程同步
- **.gitignore**：告诉 Git 哪些文件不要跟踪（编译产物、IDE 配置）
- **README.md**：项目的门面，别人（和面试官）第一眼看的就是它
- **凭据管理**：credential manager 记住密码；GitHub 支持 HTTPS 克隆
- **小步提交**：每完成一个功能就提交一次，而不是攒到最后

### 2. MySQL / SQL

- **数据库 → 表 → 行/列**：库像 Excel 文件，表像 sheet
- **主键（PRIMARY KEY）+ 自增（AUTO_INCREMENT）**：每行唯一编号
- **数据类型**：`VARCHAR` 文本、`DECIMAL(10,2)` 金额（别用 float 存钱）、`INT` 整数、`TIMESTAMP` 时间
- **`utf8mb4`**：支持中文和 emoji 的字符集，建库建表时指定
- **常用语句**：`CREATE TABLE`、`INSERT`、`SELECT ... WHERE`、`UPDATE ... WHERE`、`DELETE ... WHERE`、`TRUNCATE`（清空）
- **`WHERE` 不能漏**：`DELETE FROM books` 会删光整张表

### 3. JDBC（Java 连数据库）

- **驱动（driver）**：一个 jar，让 Java 能跟 MySQL 通信
- **`Class.forName("com.mysql.cj.jdbc.Driver")`**：加载驱动
- **连接 URL**：`jdbc:mysql://主机:端口/库名?参数`（字符集、时区、SSL 等参数）
- **`PreparedStatement`**：用 `?` 占位符传参，**防 SQL 注入**、自动转义、还能复用
- **`try-with-resources`**：`try (Connection c = ...)` 自动关闭，防止连接泄漏
- **`ResultSet`**：查询结果集，用 `rs.next()` 一行行走

### 4. Java 基础（这次用到的）

- **类与对象**：`Book` 类是模板，`new Book(...)` 是具体对象
- **封装**：private 字段 + getter/setter
- **`BigDecimal`**：金额计算用它，避免 double 精度误差
- **`List<T>` / `ArrayList`**：集合，装一堆对象
- **异常处理**：`try-catch`、`throws`、`SQLException`
- **lambda 表达式**：`button.addActionListener(e -> onOk())`，是匿名内部类的简写（Java 8）

### 5. Swing 界面

- **`JFrame`**：窗口；**`JTable`**：表格（自己不存数据）；**`JScrollPane`**：滚动容器
- **`AbstractTableModel`**：表格的数据来源，必须实现 `getRowCount` / `getColumnCount` / `getValueAt`
- **`JDialog`**：对话框；第三个参数 `true` = 模态（不关它，主窗口点不动），`setVisible(true)` 会阻塞直到关闭
- **`JOptionPane`**：弹提示 / 确认框（删除二次确认用 `showConfirmDialog`）
- **事件监听**：`ActionListener` 响应按钮点击
- **事件分发线程（EDT）**：Swing 规定界面必须在 `SwingUtilities.invokeLater` 里创建
- **排序后的行号陷阱**：开了 `setAutoCreateRowSorter(true)`，选中的视图行号要用 `convertRowIndexToModel` 转成真实数据行号

### 6. 分层架构（写项目的思路）

```
界面层 MainFrame / BookDialog / BookTableModel
     ↓ 只调用方法，不写 SQL
数据访问层 BookDao
     ↓ 统一拿连接
工具层 DbUtil
     ↓
JDBC 驱动 → MySQL → 数据文件
          ↓
   实体层 Book（贯穿各层的数据载体）
```

**好处**：改界面不影响数据库代码；换数据库只改 DAO 和工具类。

### 7. 工程习惯（面试加分）

- `.gitignore` 排除编译产物和 IDE 配置
- `sql/init.sql` 让别人能一键还原数据库
- README 写清楚：是什么、怎么跑、用什么技术、长什么样（截图）
- 启动脚本（`start-mysql.bat` / `stop-mysql.bat`）降低使用门槛
- 每完成一步就提交一次，commit 信息写清做了什么

---

## 三、踩过的坑（连同解决办法）

| 坑 | 解决办法 |
|---|---|
| GitHub 用户名填 `luo.c.x` 不合法 | 只能字母数字和单个连字符 |
| GitHub 无官方中文，插件机翻乱翻 | 认英文 + 记常用词，关键页面看原文 |
| winget 下载 Git 卡死 | 用 npmmirror 镜像直接下 exe |
| Git 静默安装报 `error 5` | 用管理员权限 `Start-Process -Verb RunAs` |
| `git` 命令找不到 | 把 `C:\Program Files\Git\cmd` 加进 PATH |
| push 报 `GH007`（邮箱隐私） | 用 `xxx@users.noreply.github.com` 匿名邮箱 |
| MySQL 启动报 `Too many arguments` | 用 `--defaults-file` 单参数，避免路径空格被拆 |
| MySQL 进程被“带走” | 别用继承窗口的 `start`，改独立隐藏进程启动 |
| Maven 下载驱动 404 | 坐标是 `com.mysql:mysql-connector-j`，不是 `mysql:mysql-connector-j` |
| 控制台中文乱码 | 客户端字符集（`--default-character-set=utf8mb4`）和 `-Dfile.encoding=UTF-8` |

---

## 四、下一步可以做什么

- [ ] 搜索框（按书名/作者模糊查询，SQL 用 `LIKE`）
- [ ] 分页（`LIMIT` / `OFFSET`）
- [ ] 借阅记录表（读者、借出、归还时间）+ 库存联动
- [ ] 导出 Excel（POI 库）
- [ ] 把界面做得更好看（表格条纹、右键菜单）

---

*本文档由 2026-11-07 的开发过程整理。项目：https://github.com/Luochuanxiang/library-system*
