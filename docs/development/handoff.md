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
| 分支与提交 | `main` 与工作分支 `port/b1-content`；最新提交以 `git log` 为准 |
| 已完成 | 两个 1.18.2 工程的全量静态调查与索引；系统调查正文（2026-11-02 按[系统设计](system-design.md)重组为 28 个系统、3 个内容族与玩家面板）；通用移植技能；开发环境（构建、开发服务端与客户端加载已实测） |
| 代码 | 公共入口与客户端入口；`material`、`skill`、`craft`、`decor` 的 Setup 只登记空注册器，尚未注册任何内容；ArchUnit、台账工具与片段合并已接入 |
| 批次进度 | B0 完成步骤 1–5 后按负责人指示结束（2026-11-02），当前执行 B1；[系统设计](system-design.md) 2026-11-02 全部批准并完成迁移；命名空间统一为 `yvanchuxiuzhen`。逐步进度见[月度记录](../systems/project/logs/2026-11.md) |
| 已批准 | 2026-11-02 负责人批准整体架构、[工程结构与长期维护](engineering.md)、[实施路线](roadmap.md)（`3e0585c` 版本），记录见[项目状态](../systems/project/README.md)；不含存档导入、界面复刻程度、license、Q01–Q14 |
| 未执行 | 玩法实现、内容注册、资源迁移、1.18.2 实机对照、存档与视觉验收；goal 未授权 |

## 进行中

B1 步骤 1（来源格式盘点与内容表格式定稿）待开始：分支 `port/b1-content`，从 `main` 建立。下一步按下文 B1 步骤 1 读 MCreator 元素定义，列出元素类型、字段与工厂类型清单，写入[工程结构](engineering.md)“注册与内容表”。

## 先读顺序

1. 仓库根 `AGENTS.md`：项目档案、当前关口、工程与文档规则。
2. 本页。
3. `.claude/skills/minecraft-mod-porting/SKILL.md`：通用移植规则与系统工作单（规则正文）。
4. [工程结构与长期维护](engineering.md)：代码目录、边界检查、内容表、资源转换、台账、依赖治理、并行协作。
5. [系统设计](system-design.md)：系统划分、归属判定、依赖规则与公开操作。
6. [整体架构](architecture.md)与[数据与运行时](data-and-runtime.md)：源码边界、调用组织、状态归属。
7. [实施路线](roadmap.md)：批次、出口和系统完成条件。
8. 所接手系统的 `docs/systems/<系统>/README.md` 与 `architecture.md`。

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

## 下一步：B1 全量内容身份与资源

范围与出口见[路线](roadmap.md) B1 行：全部物品、方块、方块物品、声音与创造栏按内容表登记，全部不依赖新代码类型的资源完成转换，结构 NBT 升级方式定案。按顺序执行，每步在开发服务端与客户端实测，结论写回[工程结构](engineering.md)：

1. 来源格式盘点与内容表格式定稿：以 MCreator 元素定义（`elements/*.mod.json`）为来源，列出 B1 范围的元素类型与字段（编辑器元素：方块 834、植物 295、动画方块 40、流体 5、物品 698、工具 103、远程物品 85、盔甲 18、动画盔甲 17、动画物品 4；方块模板 `blockBase` 为空 663、台阶与楼梯各 30、树叶 27、门与玻璃板各 16、压力板 15、栅栏、栅栏门、活板门、按钮各 8、墙 5；带过程的方块 321、带物品栏的 115）；确定工厂类型清单与内容表字段；划清“不依赖新代码类型”与“随所属系统迁移”（方块实体、菜单、GeckoLib 渲染、实体、粒子提供器、状态效果、附魔、群系、地物）的界线。
2. 内容表加载器与工厂（`platform`）：Codec 解码，未知工厂类型报出文件与条目，按单位登记到各自的 `DeferredRegister`；创造栏。
3. 注册表导出与台账比对：开发环境导出注册表，`check_ledger.py` 比对状态为“已注册”及以上的目标 ID。
4. 资源转换工具：从元素定义与来源资源生成内容表、模型、方块状态、纹理、语言片段、声音片段，以及配方、掉落表、标签、进度的 1.21.1 格式；命名空间改写与同名资源处理；输出转换清单，`check_ledger.py` 检查清单覆盖。
5. 结构 NBT 升级方式实测定案。
6. 按单位推进：按[系统设计](system-design.md)“归属判定”读候选归属并改为“已读”，运行转换并登记，开发服务端与客户端核对缺失模型、纹理与翻译，台账改为“已注册”。
7. 出口：路线 B1 行“本批完成依据”；可复用的工程做法按技能维护规则写入移植技能 references。

### B0 结束时的状态与未完成项去向

B0 完成步骤 1–5 后由负责人 2026-11-02 指示结束（逐步记录见[月度记录](../systems/project/logs/2026-11.md)）：

- 依赖：NeoForge 21.1.265、ModDevGradle 2.0.148，`neoforge.mods.toml` 下限为负责人决定的 21.1.228；此后版本不必追最新，只需不低于 21.1.228。
- 骨架：公共入口调用各单位的 `<名称>Setup.register`，客户端入口 `@Mod(value = ModId.VALUE, dist = Dist.CLIENT)`；Mod ID 常量 `platform/ModId` 同时是唯一命名空间。实测专用服务器只构造公共入口，客户端先公共后客户端。
- ArchUnit：`ArchitectureTest` 检查[工程结构](engineering.md)六条规则，随 `gradlew check` 运行。
- 台账：`build_ledger.py` 同步 4,867 条来源注册到 31 个单位与 `project` 台账；`check_ledger.py` 检查列、覆盖、目录与归属、目标 ID 命名空间与唯一性、状态值、非“待处理”须“已读”。
- 片段合并：`mergeAssetFragments` 把 `src/main/lang/<语言>/<单位>.json` 与 `src/main/sounds/<单位>.json` 合并为生成资源。

步骤 6 的内容表、资源转换与注册表导出并入 B1 上列步骤 2–4。步骤 7 的运行机制在首个需要它的系统批次按正式结构实现，在开发服务端与客户端实测后写回[工程结构](engineering.md)。B0 曾选定的来源实物与读过的归属仍可作为首个实例：

| 机制 | 曾选实物 | 归属 | 预计批次 |
| --- | --- | --- | --- |
| 玩家附件（Codec、按[复制策略表](../reference/legacy/inventory/clone_policy.csv)设置死亡复制、同步给本人） | PlayerVariables 字段 `colddownGold`（double，初值 0，`non_death_only`） | skill | C |
| payload | `AnjianWMessage`（按键 W，处理过程 `ShenfaWProcedure`） | skill | C |
| 代码注册转数据定义 | 原初系统唯一的附魔 `yvanchuxitong:chain`（目标 `yvanchuxiuzhen:chain`） | combat | C |
| GameTest 独立源集（确认不进入发布 JAR）、JUnit 规则测试 | 按首个系统风险最高的流程选取 | — | C |
| 方块实体与菜单 | 家具 `Beechfurniture14`：`RandomizableContainerBlockEntity`，27 格，自有菜单 `Beechfurniture14GuiMenu`；归属已读（27 格无槽位过滤，不属于 `storage`） | decor | D |
| GeckoLib 实体（选定 GeckoLib 版本） | `Silkwormmoth`：`PathfinderMob`，实现 `IAnimatable`；归属已读（只由蚕盒生成） | craft | D |

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
