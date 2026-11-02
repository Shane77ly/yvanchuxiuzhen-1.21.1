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
| 分支与提交 | `main` 与工作分支 `port/b0-slice`；最新提交以 `git log` 为准 |
| 已完成 | 两个 1.18.2 工程的全量静态调查与索引；19 个系统调查正文；通用移植技能；开发环境（构建、开发服务端与客户端加载已实测） |
| 代码 | 只有加载入口 `net.yvanchuxiuzhen.YvanchuxiuzhenMod`，不注册任何内容 |
| B0 进度 | 步骤 1（依赖升级）已完成；步骤 2 起暂停，等待[系统设计](system-design.md)批准。逐步进度见[月度记录](../systems/project/logs/2026-11.md) |
| 已批准 | 2026-11-02 负责人批准整体架构、[工程结构与长期维护](engineering.md)、[实施路线](roadmap.md)（`3e0585c` 版本），记录见[项目状态](../systems/project/README.md)；不含存档导入、界面复刻程度、license、Q01–Q14 |
| 未执行 | 玩法实现、内容注册、资源迁移、1.18.2 实机对照、存档与视觉验收；goal 未授权 |

## 进行中

系统设计重审：分支 `port/b0-slice`。负责人 2026-11-02 指示先定系统设计，并在审阅第一版后指出归属不能靠脚本判定；[系统设计](system-design.md)已改为第二版。2026-11-02 负责人决定命名规则与系统清单（`yihuo` 暂定独立）；该文“负责人决定”一节的依赖次序、公开操作清单、归属判定待定。全部决定后按该文“批准后的迁移”执行并合入 `main`，再从 B0 步骤 2（骨架）继续；下文 B0 切片表的所属系统按新清单重新标注。批准前不写玩法代码。

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
| Gradle | wrapper 8.8；GRADLE_USER_HOME 为 `D:\Gradle`。本机网络约 25–40 KB/s，依赖变化后的首次构建可能需二十分钟以上（2026-11-02 升级到 NeoForge 21.1.265 用时 24 分钟），日志在 `--console=plain` 下不显示下载进度，可看 `D:\Gradle\caches\modules-2\files-2.1` 与 `D:\Gradle\.tmp` 的文件变化判断是否仍在下载 |
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

1. 依赖（已完成 2026-11-02）：NeoForge 21.1.265、ModDevGradle 2.0.148，`neoforge.mods.toml` 下限为负责人决定的 21.1.228；构建与两端启动检查通过；架构“技术依赖”与 AGENTS.md 目标源码版本已更新。此后版本不必追最新，只需不低于 21.1.228。
2. 骨架：公共入口依次调用各系统的 `<名称>Setup.register`；客户端入口 `@Mod(value = MOD_ID, dist = Dist.CLIENT)` 调用 `<名称>ClientSetup`（含义见[工程结构](engineering.md)“工程形态”）；只建立切片实际用到的包。
3. ArchUnit：加入测试依赖与[工程结构](engineering.md)的四条规则；临时制造一次违规确认构建失败（不提交该违规）。
4. 台账：由 `registrations.csv` 与候选归属生成各系统 `ledger/content.csv`；编写 `tools/porting/check_ledger.py`。
5. 语言与声音片段合并任务，重复键使构建失败。
6. 内容表加载：一个系统的内容表，至少包含两个命名空间各一条真实来源物品；资源由转换工具迁入。
7. 运行机制切片，均使用真实来源内容：一个玩家附件（Codec、按[复制策略表](../reference/legacy/inventory/clone_policy.csv)设置死亡复制、同步给本人）；一个 payload；一个方块实体与菜单；一个 GeckoLib 实体（选定 GeckoLib 版本）；一个代码注册转数据定义（附魔或群系）；一个 GameTest（独立源集，确认不进入发布 JAR）；一个 JUnit 规则测试。
8. 出口：两个命名空间的注册、资源和翻译在客户端可见；开发服务端导出注册表与台账一致；检查全部通过；台账如实记录切片内容状态；验证通过、可用于其他移植项目的工程做法按技能维护规则写入移植技能 references；月度日志登记。

### B0 切片选定（初选，2026-11-02）

枠由路线 B0 行与上列步骤 6、7 规定；下表只是为每个枠选取的来源实物。依据是 `docs/reference/legacy/inventory` 的台账（静态）；所属系统以[整体架构](architecture.md)“系统所有权”为准，台账 `ownership_proposal.csv` 的归属只是候选分组（例如陶罐方块被分到 `creatures`），不作依据。来源行为在各步骤动工前读来源 Java 核实，核实后可调整并改写本表。

| 枠 | 来源实物 | 所属系统 | 台账依据 | 动工前待核 |
| --- | --- | --- | --- | --- |
| 内容表·`yvanchuxiuzhen` | 物品 `cailiao_001` | materials | `registrations.csv` | 资源文件与显示名 |
| 内容表·`yvanchuxitong` | 步骤 6 从该命名空间 188 个物品中选一个，优先与其他枠同系统者 | 待定 | `registrations.csv` | 同上 |
| 玩家附件 | PlayerVariables 字段 `colddownGold`：double，初值 0，复制策略 `non_death_only` | abilities | `clone_policy.csv`、`state_lifecycle.csv`（读 11、写 4 处） | 同步方式；写入与读取者的实际含义 |
| payload | `AnjianWMessage`（按键 W，处理过程 `ShenfaWProcedure`） | abilities | `network_surfaces.csv` | 处理过程是否读写 `colddownGold`；服务端校验 |
| 方块实体与菜单 | `yvanchuxitong` 的家具 `Beechfurniture14`：`RandomizableContainerBlockEntity`，27 格，`createMenu` 返回自有菜单 `Beechfurniture14GuiMenu` | life | 来源 `block/entity/Beechfurniture14BlockEntity.java`；全部 115 个方块实体中 35 个返回自有菜单，其余为原版 `ChestMenu` 占位 | 方块、菜单槽位与屏幕资源；`life` 与 `storage` 的归属（家具箱是否属于特殊容器）待依据[架构](architecture.md)“系统所有权”复核。已排除 `Taoguan01`：其方块实体的菜单是原版占位，真实菜单 `Taoguan01GUI` 属另一 Mod 的物品，且方块逻辑是蛊虫认主 |
| GeckoLib 实体 | `Silkwormmoth`：`PathfinderMob`，实现 `IAnimatable`，约 7 KB | creatures | `entity_surfaces.csv` | 属性、目标、掉落、几何/动画文件 |
| 代码注册转数据定义 | 附魔 `yvanchuxitong:chain`（该命名空间唯一的附魔） | combat | `registrations.csv` | 实际效果与消费者；若无法用数据驱动附魔表达，改选 `yvanchuxiuzhen` 的群系 |

GameTest 与 JUnit 针对上述内容中风险最高的两处（`colddownGold` 的死亡/非死亡复制与同步、`Chain` 的效果）选取，待步骤 7 核实后定。

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
