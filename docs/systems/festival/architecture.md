---
topic: festival
role: architecture
status: draft
updated: 2026-11-14
---

# 节日：架构调查与提案

来源事实与目标方案分别陈述。目标方案未批准；源码定位采用 [系统导航](../README.md)的 A/B 规则。

## 1.18.2 范围

内容族：鞭炮花炮、祈愿灯、财神服、大红包，单品自带抛射、粒子与随机奖励。只有 `Pao10`–`Pao12` 的引信计时（方块实体 `begin`、`tick`，库存容量 0）；不写玩家字段，被其他代码引用 2 处。

## 目标所有权与接口提案

内容表声明物品与实体，元素自身行为随内容迁移。没有 `api`。

## 财神（2026-11-11）

本节为已按来源编写、待主会话接线验证的实现记录，不改变其他节日内容的调查或批准状态。来源A `entity/CaishenyeEntity.java`、`procedures/CaishenyeDangShiTiBeiShaSiShiProcedure.java`、`CaishenyeDangYouJianDianJiShiTiProcedure.java`、`CaishenyeZiRanShiTiShengChengTiaoJianProcedure.java`的证据与指纹见本族`ledger/source-evidence.csv`与`ledger/source-fingerprints.csv`。

| 路径 | 保留行为与目标实现 |
| --- | --- |
| 实体注册 | `caishenye`为CREATURE，碰撞箱0.6×1.8，跟踪范围64，更新间隔3；内容表由正式转换器从来源注册提取 |
| 基础与AI | 生命100、移速0.3、护甲0、攻击3、跟随16、经验0；优先级1随机看向、2浮水、3速度1闲逛、4距离6看玩家 |
| 自然出生 | ON_GROUND与MOTION_BLOCKING_NO_LEAVES；仅检查服务器本机`Calendar.getInstance().get(Calendar.MONTH) == 2`，对应公历三月。来源没有在这个谓词中另加光照、难度或地面材质条件 |
| 群系条目 | 来源实体静态集合声明41个群系、CREATURE权重3、1–1只；当前41群系计划中实际有35个财神引用。World按来源事件追加顺序装配，Festival不重复安装独立modifier |
| 玩家右键 | 先调用原生交互，再把本实体yaw设为玩家yaw+180、pitch设0，并同步当前/前一帧body/head/yaw/pitch；返回双端成功，不扣物品、不产生奖励 |
| 死亡 | 原生die后仅服务端广播`flyskill_04`20个，位置Y+1，偏移0.5/1/0.5、速度0.1；死亡第1刻移除。1.21.1原生死亡路径已负责经验处理，来源经验为0 |
| 表现 | `caishen`几何、动画和纹理；阴影0.5、缩放1、死亡旋转0、头部骨骼`head`随视线；移动`animation.caishen.walk`、其余`animation.caishen.idle`，过渡4刻，过程动画保持来源一次播放机制 |
| 刷怪蛋 | `caishenye_spawn_egg`，底色-3407872、斑点-13261、来源MISC创造栏映射沿用共享转换器 |

实现位于`collection/festival/Caishenye`及`client/FestivalClientSetup`；`convert_caishenye.py`从锁定来源提取模型行、复制清单、注册/刷怪蛋/语言接线计划，并严格核对日历规则及刷怪蛋颜色。正式转换、构建、目标游戏加载、自然公历三月出生、完整保存重载、1.18.2版本实机与两版本对照均未执行；不登记行为验收或完整自然供给闭合。

## 手持烟花（2026-11-14）

`pao_02`电光花、`pao_03`呲花和`pao_07`喷花在`collection/festival/HandheldFireworkItem`按物品实例保存点燃状态与已燃刻数。右键将一件物品分成点燃栈和未点燃余量；切出当前选中手持槽时计时暂停，重复点燃返回来源的`PASS`行为。电光花持续600刻并在视线命中位置发射材料粒子；呲花持续600刻，每刻发射五个基础粒子并按来源逐分支独立抽样颜色；喷花持续1200刻并按每200刻阶段切换颜色。到期仅消费点燃栈的一件。创造模式保留来源整栈点燃分支，并把剩余未点燃数量分出。

来源过程的静态对照与冻结包复核已完成，包SHA256为`CBAD581193B0C80594CA37186151AF07BF10CC0D18EC48A8B81EF7E9AED6FCE1`。主线接入`HandheldFireworkCheck`，覆盖自然右键分栈、余量组件、选中槽计时、物品序列化、燃尽和创造模式分支；定向内容转换、生产/Smoke编译及必要客户端单项通过；截图可见中文物品名和三类粒子。燃放存档只验物品序列化，真实重登、全部颜色时序/完整视觉、自然获取、来源实机、多人及两版本对照未执行。结果见[本月日志](logs/2026-11.md#2026-11-14-手持烟花)。当前未发现需要新增裁定的来源差异；新疑点统一进入[来源问题索引](../../reference/legacy/source-survey.md#具体问题与裁定位置)。

## 祈愿灯（2026-11-14）

`fuqi_03`仅在右键顶面且正上方为空时消耗一件并生成`fuqi_0301`；条件不满足仍返回来源`SUCCESS`。实体按来源MONSTER注册，使用飞行导航但无AI、无重力，每刻朝向增加一度并以视线水平速度0.1、垂直速度0.1上升；免疫摔落和仙人掌伤害，死亡第二刻移除，没有额外寿命。保留来源模型、动画、普通贴图与发光层贴图；不把Fuqi06随机获取链并入本包。

交付包SHA256为`1CE0B6B0FBED44D4A64AD7D9C744FFA1E0540628C032862EC5F21187D21443C9`，16个源码/资源文件指纹见`ledger/source-evidence.csv`。`convert_content.py --owner festival`与`convert_assets.py --owner festival`已通过，资源转换67项、缺失0；生产/Smoke编译、必要客户端单项及六项架构检查待执行。实际获取仍依赖未迁移Fuqi06奖励，真实重登、来源实机、多人、专服及两版本对照未执行。交付夹具覆盖拒放/扣料、飞行与实体序列化；运行报告和截图确认前不登记行为验收。
