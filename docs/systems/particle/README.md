---
topic: particle
role: entry
status: partial
updated: 2026-11-07
---

# 粒子库

标识 `particle`，类别：基座。职责：全部粒子类型与客户端实现，供各系统按 ID 触发（[系统设计](../../development/system-design.md)）。没有玩法规则，不设调查正文。

当前阶段：315 个来源粒子的注册、提供器、参数表、粒子 JSON 与全部 sprite 已迁移（2026-11-07，随 `port/c-skill` 补齐前置）。315 个归属元素全部已读；原候选归 creature 的三个、fulu 的四个粒子只处理自身表现和物理，已按系统设计归入粒子库，对应系统保留发出时机。台账暂为“已注册”，各系统发出处与全量视觉对照尚未完成。

- [内容台账](ledger/content.csv)：粒子类型注册的目标 ID 与迁移状态。
- [系统导航](../README.md)记录源码引用规则与各项证据范围。

## 公开操作

`system/particle/api/Particles.type(目标路径)` 返回 `SimpleParticleType`。服务端是否广播、发给谁、数量、位置和速度由调用者按来源通过原生入口决定；客户端本地生成使用 `ClientLevel.addParticle`。粒子库不回读玩法系统、玩家附件或技能书。

两个来源的 `lingqi` 不能合并：原初修真保留 `lingqi`（重力0、每刻旋转1弧度），原初系统使用已有台账映射 `lingqi_xitong`（重力−0.05、不旋转），均为30刻、八帧、每四刻推进。两者共用相同图案，提供器和参数仍独立。

## 来源模板与目标实现

来源为两个 Mod 的 `ModParticleTypes`、`ModParticles` 与 `client/particle/<元素>Particle`，315项均是 `TextureSheetParticle`。按移植技能读取同模板与参数差异后，`extract_particles.py` 逐项核对构造、刻过程、光照和提供器注册对应；未知行为直接报出来源。定义写入 `content/particle/particles.json`，由 `ParticleRow.CODEC` 读取，`ParticleSetup` 注册类型，客户端 `ParticleClientSetup` 在精确目标21.1.265的 `RegisterParticleProvidersEvent` 中注册提供器。

`LibraryParticle` 复用原生引擎和三个来源批次：lit 238项、translucent 57项、opaque 20项；279项按帧推进，其余随机取初始sprite。尺寸保留碰撞宽高，`scale` 乘原生基类的随机四边形尺寸；初速度逐轴乘来源系数，保留重力、碰撞、原生阻尼与光照。只有来源覆写光照的项返回15728880，不把所有粒子强行设为全亮。`guchong_05lizi` 是唯一 `SimpleParticleType(true)`，保留此标记。

57项带寿命抖动：`max(1, L + nextInt(2J) − J)`，随机整数为−J至J−1；其余使用固定寿命。旋转按来源在父类推进之后更新 `oRoll`、角度和角速度，单位是弧度，不重新解释为角度。动画构造时按age取sprite，每刻再以 `(age / frameTicks) % frames + 1` 与分母frames取帧，保留原式的偏移与循环，不改为按总寿命线性分摊。

资源转换按来源粒子JSON的实际引用保留帧序，统一命名空间；JSON的sprite路径不含 `particle/`，PNG放在 `textures/particle/`。来源315份JSON引用的3633个独立来源sprite文件均存在；合并命名空间时复用8个字节一致的同路径文件，目标共3625张独立贴图，全部进入转换manifest。目标 `ParticleEngine` 在资源重载时清空原生活动粒子并重绑定SpriteSet；本实现没有独立实例索引或自定义渲染状态。

本批检查入口为 `-PsmokeOnly=particle`，只验证两个同名粒子的重力差异、动画粒子的原生到期，并留存三种批次的画面；不重复旧神通。2026-11-07 仅运行一次，两项全部通过并 DONE；截图已查看，样本可见且未见缺失纹理。build（含 ArchUnit）、检查源集编译、专用服务端启动和正常保存停服通过；导出315个粒子类型，与4938行台账核对无问题。skill的元素弹体、灵纹弹和技能箭随后已接回来源粒子；其余完整发出处、315种视觉认可、多人和1.18.2版本实机对照未执行，台账保持“已注册”。详细证据登记在项目月度日志。

本轮变化统一记录在 [项目月度日志](../project/logs/2026-11.md)。
