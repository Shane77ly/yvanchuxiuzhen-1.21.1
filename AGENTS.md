# 协作规则

## 项目档案

- 来源：原初修真 `yvanchuxiuzhen`、原初系统 `yvanchuxitong`，均为 MCreator 生成的 1.18.2 / Forge 40.1.0 / Java 17 工程，互为必需依赖。位置、锁定标识与静态证据见 `docs/reference/legacy/README.md`。
- 目标：Minecraft 1.21.1、NeoForge、Java 21，合并为一个可安装 Mod，内部按系统组织。
- 范围：全量移植两个 Mod 的全部系统。
- 目标源码：构建后的 `build/moddev/artifacts/neoforge-<版本>-sources.jar`（含 Minecraft 与 NeoForge 源码，随依赖版本生成；当前构建为 NeoForge 21.1.265，最低支持 21.1.228）；FML 等库在 Gradle 缓存。邻近工程的 `reference/neoforge-21.1.228-sources` 只在版本相同时作对照。
- 非依据：邻近 1.21.1 工程 `yuanchuxiuzhen-1.21.1` 只提供工程参考。
- 当前关口：B0 架构验证切片（`docs/development/roadmap.md`）。2026-11-02 负责人批准 `docs/development/architecture.md`、`engineering.md`、`roadmap.md`（main 提交 `3e0585c` 版本）并指示自 B0 步骤 1 开始，工作分支 `port/b0-slice`。批准范围仅限这三份文档；存档导入、界面复刻程度、license、来源矛盾 Q01–Q14 以及未列入的文档（含各系统 `architecture.md`、`data-and-runtime.md`）仍为待裁定或草案，对应内容实现前需另行裁定。goal 仍未授权，未经明确批准不创建 goal。2026-11-02 负责人全部批准 `docs/development/system-design.md`（系统划分、归属判定、依赖规则、公开操作清单）；该文“批准后的迁移”已完成，B0 从步骤 2 继续。
- 开发环境：Mod ID `yvanchuxiuzhen`，也是全部注册与资源的唯一命名空间（原初系统内容同样使用）；Java 基础包 `net.yvanchuxiuzhen`；构建组合与验证见 `docs/development/architecture.md` 的“技术依赖”。
- 交接：新会话先读 `docs/development/handoff.md`。
- 记录位置：内容映射与行为对齐见 `docs/development/content-and-parity.md`，台账 CSV 在 `docs/systems/<系统>/ledger/`（B0 建立），系统规则正文在 `docs/systems/<系统>`，证据与日志按下方“文档”登记。

## 移植方法

- 来源核对、架构、实现、对齐与验收作业，先读并遵守 `.claude/skills/minecraft-mod-porting/SKILL.md`。其核心规则对本项目全部移植作业有效。
- 文档统一写“1.18.2 版本”和“1.21.1 版本”，来源路径及代码符号照实保留。

## 进度记录与交接

- 会话开始：`git status` 确认工作树干净且位于对应工作分支；读本文件、`docs/development/handoff.md`、所属系统 README 与台账。
- 每个提交：同一提交内更新受影响的台账行与系统 README 状态，不留待事后补记。
- 会话结束或中断：所属系统（或项目）月度日志写明本次变化、验证结果与未完成项；`handoff.md`“进行中”写明分支、最后提交与下一步具体操作；运行检查后提交，工作树干净。中断时在工作分支提交 `wip: <摘要>`。
- 里程碑（B0 每一步、系统完成、批次出口、依赖更新、负责人决定）：更新 `handoff.md`“当前状态”“下一步”、项目状态页与路线状态，然后合入 `main`。
- 负责人决定在同一会话写入项目状态页；涉及关口时同步本文件“当前关口”。
- 分支、合并、worktree 与提交格式见 `docs/development/engineering.md`“Git 与会话协作”。

## 工程边界

- 按系统组织实现；数据所有者是唯一写入者。跨系统调用使用公开接口，禁止直接修改其他系统的持久化对象。
- 系统划分、归属与依赖以 `docs/development/system-design.md` 为准：系统之间不得相互依赖（ArchUnit 循环检查）；依赖由职责与数据归属决定，不按来源访问量推导。
- 内容归属按该文“归属判定”读来源规则确定；目录、名称、元素形态与脚本结果只是候选，候选归属不作为实现依据。
- 实现一个系统前，在其 README“公开操作”一节列出供其他系统调用的查询与写操作，并对照 `typed_field_accesses.csv` 覆盖全部外部读写点。
- 公共核心只容纳已出现共享需求的类型和语义，不堆积全部职业、物品或技能逻辑。
- 服务端保存玩法真值；客户端快照和显示不得反写整份玩家档案。
- 正式注释使用专业、客观的中文，说明当前职责、数据流、公式、生命周期和边界，不写试错过程及未来维护设想。
- Java 使用显式 import 与简类名，禁止通配符和无同名冲突的全限定类型直写。修改后检查无用导入、重复逻辑、复制残留和注释真实性。
- 文本一律按 UTF-8 读写。只修改本任务相关内容。

## 测试与文案

- 非必要不新增测试。对易回归公式、跨端同步、存档、库存、共享入口和已复现 Bug 选择针对性验证；低风险资源和文本调整只做适当构建或游戏检查。
- 不写无意义断言、常量非空测试或复制实现的测试。直接实现已确认规则，不添加无需求的判空、吞异常、默认值兜底、兼容分支、重试与通用框架；具体规则见 `docs/development/coding-policy.md`。
- Tooltip 只表达背景、操作、效果和必要数值。实现约束、自检结果、排除式“不会发生什么”留在开发文档。
- 玩家界面任务按用户默认 shadcn 技能的适用规范执行；Minecraft Java 客户端使用游戏原生技术或选定的客户端适配层，不引入 React 运行时。

## 文档

- 总入口 `docs/README.md`；公共架构在 `docs/development`，项目范围在 `docs/design`；系统规则正文在 `docs/systems/<系统>`。
- 每项规则只保留一个正文：通用移植方法以移植技能为正文，项目文档链接引用、不复制；系统实现状态、采用但未实现的规则、待核范围分别标明。
- 1.18.2 源码数据、许可证、结构导出、可复用实验保留其用途，不收容废弃设计。月度日志放所属系统 `logs/YYYY-MM.md`，`changes.md` 只作索引。
- 修改同步更新正文、状态与月度记录，并执行 `python -B -X utf8 tools/docs/check_docs.py`。修改移植技能同样登记在项目月度记录。
- 静态源码核对、自动化验证、真实1.18.2 / 1.21.1游戏验收分别登记；未执行项明确写未执行。
- 临时产物放 `tmp` 或 `build`。
