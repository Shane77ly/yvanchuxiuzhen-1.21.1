---
topic: project
role: entry
status: current
updated: 2026-11-02
---

# 项目状态与决定

## 已确认要求

| 日期 | 负责人要求 | 当前状态 |
| --- | --- | --- |
| 2026-11-01 | 新建独立项目，完整移植两个 1.18.2 Mod，先调查架构 | 已有独立调查目录；未实现游戏功能 |
| 2026-11-01 | Minecraft 1.21.1 + NeoForge + Java 21，合为一个 Mod | 已建立 Gradle 开发环境并通过构建与加载检查；未注册内容 |
| 2026-11-01 | 编号/变量 Excel、全量范围、标准开发文档 | 工作簿与文档已建立，逐条语义及目标映射待评审 |
| 2026-11-01 | 不做无需求的防御封装，不写无意义断言测试 | 已写入开发规则；本轮无游戏测试代码 |
| 2026-11-01 | 当前不要开 goal，方案尚未通过 | 本轮未创建或恢复 goal；2026-11-02 架构批准不含启动 goal，仍等待明确启动指令 |
| 2026-11-01 | 使用“1.18.2 版本”的明确表述 | 本项目文档及工作簿统一；来源路径/符号照实记录 |
| 2026-11-02 | 自建可迭代的通用移植技能，按提案整理技能结构并修改 AGENTS.md；暂不公开 | 已建立 [移植技能](../../../.claude/skills/minecraft-mod-porting/SKILL.md)，AGENTS.md 改为项目档案 + 技能引用；`docs` 中的同类方法正文已迁出，只保留本项目适用与决定；技能未在实施中使用 |
| 2026-11-02 | 按大型 Mod 长期维护与依赖环境重新设计工程结构与移植流程，并编写交接文档 | 已提交方案：[工程结构](../../development/engineering.md)、[路线](../../development/roadmap.md)、[交接说明](../../development/handoff.md)；工程结构与路线已于 2026-11-02 批准（见下一行） |
| 2026-11-02 | 先整理 Git，并明确进度记录与交接更新规则 | 分支改名为 `main`，加入 `.gitattributes`；规则写入 AGENTS.md“进度记录与交接”与[工程结构](../../development/engineering.md)“Git 与会话协作”；作者邮箱改为负责人提供的地址，远端为公开 GitHub 仓库 |
| 2026-11-02 | Mod ID `yvanchuxiuzhen`、Java 基础包 `net.yvanchuxiuzhen`、license 暂填 All Rights Reserved；先建立开发环境 | 已建立构建与加载入口；license 待逐资产核对后再定 |
| 2026-11-02 | 批准 [整体架构](../../development/architecture.md)、[工程结构与长期维护](../../development/engineering.md)、[实施路线](../../development/roadmap.md)（main 提交 `3e0585c` 版本）；未决事项（存档导入、界面复刻程度、license、Q01–Q14）不在批准范围；从 `main` 建立 `port/b0-slice`，自 [交接说明](../../development/handoff.md) B0 步骤 1 开始 | 三份文档状态改为 `current`；关口由架构设计阶段进入 B0；goal 仍未授权；B0 进展见[月度记录](logs/2026-11.md)与交接说明“进行中” |
| 2026-11-02 | NeoForge 版本不必追最新，下限定为 21.1.228 | `neoforge.mods.toml` 范围 `[21.1.228,21.2)`；B0 步骤 1 已将构建升至 21.1.265、ModDevGradle 2.0.148，构建与两端启动检查通过（见[月度记录](logs/2026-11.md)）；[工程结构](../../development/engineering.md)“依赖治理”同步 |
| 2026-11-02 | 采用[系统设计](../../development/system-design.md)第二版的命名规则；采用收紧后的系统标准与清单，`yihuo` 暂定独立 | 已记入该文“负责人决定”；依赖次序、公开操作清单、归属判定待定，B0 步骤 2 起继续暂停 |
| 2026-11-02 | 依赖规则：系统之间不得相互依赖（构建检查），不设编号次序或依赖表；`combat` 只读 `attribute` 最终值；本命槽位归 `xiulian`，本命培养数据归 `equipment` | 已写入[系统设计](../../development/system-design.md)“依赖规则”；公开操作清单、归属判定待定 |
| 2026-11-02 | 来源矛盾 Q15：光灵箭 `skill_arrow_10` 的堆叠上限保持 1（物品自身属性），配方 `skillarrow_10hecheng` 的产出由 5 改为 1 | 已记入[技能](../skill/architecture.md)“裁定（Q15）”；转换工具对产物数量超过物品堆叠上限的配方统一降到上限并记录 |
| 2026-11-02 | 采用公开操作清单作为每个系统的开工条件；采用“规则决定归属、脚本只给候选”，台账增加归属状态列 | 系统设计全部批准，AGENTS.md“工程边界”写入三条约束；该文“批准后的迁移”已完成（见[月度记录](logs/2026-11.md)），B0 从步骤 2 继续 |
| 2026-11-02 | 内容族的 Java 包名由 `family` 改为 `collection`；“内容族”这一中文名称不变 | 已改包 `net.yvanchuxiuzhen.collection`、ArchUnit 规则 1、4、6 与[工程结构](../../development/engineering.md)目录及规则 |
| 2026-11-02 | 全部注册与资源统一使用命名空间 `yvanchuxiuzhen`，原初系统内容不再保留 `yvanchuxitong`（取代已批准架构中的“保留两个命名空间”） | 常量改为 `platform/ModId`；片段路径去掉命名空间一级；台账 477 行 `target_id` 改为统一命名空间，2 处同名冲突暂定改名；[架构](../../development/architecture.md)、[工程结构](../../development/engineering.md)、[范围](../../design/scope.md)等同步（见[月度记录](logs/2026-11.md)） |
| 2026-11-02 | 不再做小规模切片：B0 在框架完成后结束，进入下一阶段并建立新分支 | B0 以步骤 1–5 结束，关口改为 B1，工作分支 `port/b1-content`；B0 步骤 6 以全量方式并入 B1，步骤 7 的运行机制在首个需要它的系统批次实现并实测（去向见[交接说明](../../development/handoff.md)）；AGENTS.md 当前关口、[路线](../../development/roadmap.md)、[工程结构](../../development/engineering.md)同步 |

## 系统设计重审

2026-11-02 负责人指出系统命名与划分不成熟，指示先定系统设计，B0 步骤 2 起暂停。负责人审阅第一版后指出：B0 会话反问归属说明设计不起作用，归属不能靠脚本判定；陶罐、`festival`、`decor`、`treasure`/`town`/`dungeon` 与 `Silk*` 需按具体规则确认。[系统设计](../../development/system-design.md)第二版于 2026-11-02 全部批准，[架构](../../development/architecture.md)“系统所有权”与其他文档按该文“批准后的迁移”同步。

## 已批准与待裁定

已批准（2026-11-02）：一个 Gradle 工程、一个发布 JAR、按系统组织；单一命名空间 `yvanchuxiuzhen`（2026-11-02 由“保留两个来源命名空间”改定）；功法成长随物品、银行与材料仓归玩家、现场拍卖归方块、学习小游戏独立会话；代码目录、ArchUnit 边界、内容表、资源转换、台账 CSV、依赖治理与分支协作；A～I 全量批次与系统完成条件。正文为 [架构](../../development/architecture.md)、[工程结构](../../development/engineering.md)、[路线](../../development/roadmap.md)。三份文档中标注“待验证”的做法由 B0 实测后写回。

仍待负责人裁定：1.18.2 世界与玩家存档是否导入、界面复刻程度、发布 license，以及 [调查问题表](../../reference/legacy/source-survey.md)中的 Q01–Q14（实现所属系统前裁定）。批准范围之外的文档（各系统 `architecture.md`、[数据与运行时](../../development/data-and-runtime.md)等）仍是草案，不能从“已经写入文件”推定已批准；明确采用前不改变对应规则。

## 已完成与剩余

- 自动：7,874 Java、7,465 编辑器元素、4,938 注册、211 状态、183 复制策略、630 动态表达式分组；3,428 个过程的连接调用集合全部一致，192 个另有游离调用；事件/消息/命令/菜单/实体/依赖均有索引，11 页审阅工作簿。
- 人工：元数据、功法物品实例、闭关分流、炼器跨 Mod 职业读取、炼丹入口、符箓绘制、阵法触发、妖兽动画/生命周期、银行/商店/拍卖、任务动作、容器绑定等关键架构位置；具体范围见调查正文。
- 提案：[系统调查正文](../README.md)（2026-11-02 按系统设计重组）、[公共入口与依赖](../../development/entry-and-dependency.md)、数据生命周期、编号规则、内容迁移、实施顺序、开发及文档规范。
- 剩余调查：3,428 个过程的每条分支、动态键全部展开、所有数值/获取/消费者的闭环、两个版本实机对照；不是已完成的行为验收。
- 开发环境：`gradlew build` 成功；开发专用服务器启动至 Done、开发客户端加载至声音引擎启动，均列出本 Mod 且无 ERROR。只证明构建与加载入口，不涉及玩法。
- 未执行：玩法 Java 实现、内容注册、1.18.2 存档导入与视觉验收。

本轮检查结果见 [月度记录](logs/2026-11.md)，导航见 [文档入口](../../README.md)和 [变更索引](changes.md)。
