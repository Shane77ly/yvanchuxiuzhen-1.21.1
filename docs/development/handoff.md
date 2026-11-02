---
topic: handoff
role: guide
status: current
updated: 2026-11-02
---

# 交接说明

本页写给接手移植的新会话与负责人。它只给出当前状态、阅读顺序、环境事实和下一步；规则正文在各自文档中，这里只链接。

## 当前状态（2026-11-02）

| 项 | 状态 |
| --- | --- |
| 分支与提交 | 只有 `main`（2026-11-02 由 `codex/architecture` 改名，前缀来自最初创建仓库的工具，不具技术含义）；最新提交以 `git log` 为准 |
| 已完成 | 两个 1.18.2 工程的全量静态调查与索引；19 个系统调查正文；通用移植技能；开发环境（构建、开发服务端与客户端加载已实测） |
| 代码 | 只有加载入口 `net.yvanchuxiuzhen.YvanchuxiuzhenMod`，不注册任何内容 |
| 已批准 | 2026-11-02 负责人批准整体架构、[工程结构与长期维护](engineering.md)、[实施路线](roadmap.md)（`3e0585c` 版本），记录见[项目状态](../systems/project/README.md)；不含存档导入、界面复刻程度、license、Q01–Q14 |
| 未执行 | 玩法实现、内容注册、资源迁移、1.18.2 实机对照、存档与视觉验收；goal 未授权 |

## 进行中

B0 架构验证切片：分支 `port/b0-slice`（由批准记录提交后的 `main` 建立），从下方“下一步”步骤 1 开始。会话中断时在此写明最后提交与下一步具体操作；B0 合入 `main` 后删除该条。

## 先读顺序

1. 仓库根 `AGENTS.md`：项目档案、当前关口、工程与文档规则。
2. 本页。
3. `.claude/skills/minecraft-mod-porting/SKILL.md`：通用移植规则与系统工作单（规则正文）。
4. [工程结构与长期维护](engineering.md)：代码目录、边界检查、内容表、资源转换、台账、依赖治理、并行协作。
5. [整体架构](architecture.md)与[数据与运行时](data-and-runtime.md)：系统所有权、调用方向、状态归属。
6. [实施路线](roadmap.md)：批次、出口和系统完成条件。
7. 所接手系统的 `docs/systems/<系统>/README.md` 与 `architecture.md`。

## 开工前必须确认

AGENTS.md 的“当前关口”写明架构未批准时，只能做调查、文档与工具工作，不写玩法代码。2026-11-02 负责人已批准架构、工程结构与路线并记入 AGENTS.md 当前关口与[项目状态](../systems/project/README.md)，B0 已开始。批准只覆盖这三份文档；下列事项仍待负责人决定，遇到时在所属系统实现前提请裁定：

| 事项 | 当前方案 | 位置 |
| --- | --- | --- |
| 1.18.2 世界与玩家存档是否导入 | 未承诺 | [范围](../design/scope.md) |
| 界面复刻程度 | 保留全部操作与反馈，布局另评审 | [范围](../design/scope.md) |
| 发布 license | 暂填 All Rights Reserved | [架构](architecture.md) |
| 来源矛盾 Q01–Q14 | 逐项裁定，实现所属系统前解决 | [调查问题表](../reference/legacy/source-survey.md) |

## 本机环境事实

| 项 | 值 |
| --- | --- |
| JDK | Temurin 21.0.9（`C:\Program Files\Eclipse Adoptium\jdk-21.0.9.10-hotspot`）；另有 JDK 17 可用于运行 1.18.2 工程（未验证） |
| Gradle | wrapper 8.8；GRADLE_USER_HOME 为 `D:\Gradle` |
| 来源工作区 | `D:/yuanchuxiuzhen-1.21.1-port/yuanchuxiuzhen-1.21.1`，两个 1.18.2 工程位置见[来源说明](../reference/legacy/README.md) |
| 目标源码 | 构建后的 `build/moddev/artifacts/neoforge-<版本>-sources.jar`，含 Minecraft 与 NeoForge 源码；FML 在 Gradle 缓存 `net.neoforged.fancymodloader` |
| 构建 | `./gradlew build` |
| 运行 | `./gradlew runServer`、`./gradlew runClient`；运行目录 `run/`（已忽略） |
| Git | `origin` = https://github.com/Shane77ly/yvanchuxiuzhen-1.21.1 （公开）；凭据由 Git Credential Manager 保存的 `Shane77ly` 账户提供，走 HTTPS（本机未配置 GitHub SSH）；提交作者 `Lix <207489130@qq.com>`；`.gitattributes` 规定换行 |
| 文档与来源检查 | `python -B -X utf8 tools/docs/check_docs.py`；`python -B -X utf8 tools/porting/inventory_legacy.py --check` |

运行检查注意：

- `runServer` 启动后等待控制台输入，不会自行退出。结束 Gradle 任务后按命令行查找残留进程并结束：

  ```powershell
  Get-CimInstance Win32_Process -Filter "Name='java.exe'" |
    Where-Object { $_.CommandLine -match 'yuanchuxiuzhen-rebuild' } |
    ForEach-Object { Stop-Process -Id $_.ProcessId -Force }
  ```

- 开发运行的专用服务器不要求 eula.txt；不要替负责人写入 EULA 同意。
- Windows 控制台以系统代码页显示中文 Mod 名时乱码；以 `run/logs/latest.log`（UTF-8）为准。
- 运行日志、临时脚本放 `tmp/`。

## 下一步：B0 架构验证切片

按顺序执行；每项都要在开发服务端与客户端实际运行，结果写回[工程结构](engineering.md)并去掉对应“待验证”。

1. 依赖：NeoForge 升到当时最新 21.1.x、ModDevGradle 升到最新 2.0.x（2026-11-02 分别为 21.1.265、2.0.148）；构建与启动检查；更新架构“技术依赖”与 AGENTS.md 目标源码版本。
2. 骨架：公共入口依次调用各系统的 `<名称>Setup.register`；客户端入口 `@Mod(value = MOD_ID, dist = Dist.CLIENT)` 调用 `<名称>ClientSetup`（含义见[工程结构](engineering.md)“工程形态”）；只建立切片实际用到的包。
3. ArchUnit：加入测试依赖与[工程结构](engineering.md)的四条规则；临时制造一次违规确认构建失败（不提交该违规）。
4. 台账：由 `registrations.csv` 与候选归属生成各系统 `ledger/content.csv`；编写 `tools/porting/check_ledger.py`。
5. 语言与声音片段合并任务，重复键使构建失败。
6. 内容表加载：一个系统的内容表，至少包含两个命名空间各一条真实来源物品；资源由转换工具迁入。
7. 运行机制切片，均使用真实来源内容：一个玩家附件（Codec、按[复制策略表](../reference/legacy/inventory/clone_policy.csv)设置死亡复制、同步给本人）；一个 payload；一个方块实体与菜单；一个 GeckoLib 实体（选定 GeckoLib 版本）；一个代码注册转数据定义（附魔或群系）；一个 GameTest（独立源集，确认不进入发布 JAR）；一个 JUnit 规则测试。
8. 出口：两个命名空间的注册、资源和翻译在客户端可见；开发服务端导出注册表与台账一致；检查全部通过；台账如实记录切片内容状态；验证通过、可用于其他移植项目的工程做法按技能维护规则写入移植技能 references；月度日志登记。

B0 之后按[路线](roadmap.md)进入 B1，再按系统推进。

## 每次会话的流程

按 AGENTS.md 的“进度记录与交接”执行；分支、合并、worktree 与提交格式见[工程结构](engineering.md)的“Git 与会话协作”。工作中按移植技能的系统工作单推进，遇到来源矛盾先记录并提请裁定。

## 已知陷阱

- 邻近工程 `yuanchuxiuzhen-1.21.1` 的玩法、数值、删减和完成状态不是依据；它的 Parchment 配置（1.21.11）也不适用于本项目。
- 来源中按 Java 注册的附魔、群系、地物在 1.21.1 必须改为数据定义；燃料与堆肥改为 NeoForge 数据映射。
- 来源语言文件中混有机器配置（如商品列表），迁移时先拆出，不能当作普通翻译。
- 不要用“注册成功”“构建通过”“文件存在”标记玩法完成；状态按台账约定填写。

## 文档地图

| 内容 | 位置 |
| --- | --- |
| 总入口 | [docs/README.md](../README.md) |
| 项目状态与决定 | [项目状态](../systems/project/README.md)，日志 `docs/systems/project/logs/` |
| 来源证据与索引 | [来源说明](../reference/legacy/README.md)、[索引](../reference/legacy/inventory/README.md) |
| 目标 API 待验证范围 | [目标 API](../reference/target-api.md) |
| 编码与测试规则 | [开发规则](coding-policy.md) |
| 文档维护 | [文档维护](documentation.md) |
