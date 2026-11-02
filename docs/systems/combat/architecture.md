---
topic: combat
role: architecture
status: draft
updated: 2026-11-01
---

# 战斗、状态、附魔与关系：架构调查与提案

来源事实与目标方案分别陈述。目标方案未批准；源码定位采用 [系统导航](../README.md)的 A/B 规则。
## 1.18.2 数据与入口

`A:EntityHurtProcedure:49–100` 监听 `LivingAttackEvent`，处理状态、战斗记分板及后续附魔入口；`EntityBeforeHurtProcedure:45–170` 监听 `LivingHurtEvent`，判断 NPC 免伤、玩家/生物队伍、闪避和物理结算。两者是不同阶段。`EntityBeforeHurt2Procedure` 只有局部声明，不承担有效伤害计算。

关系并不只有队伍字符串：玩家 `friendlist` 物品的嵌套 CompoundTag、实体 `friendlist`、`team` 以及主人 UUID 都参与判定。还存在记分板 `zdztbegin/zdzttick`（`EntityHurtProcedure:79–98`）。这些状态不能因未列入 Variables 而丢失。

77 个状态效果、85 个附魔注册及伤害命令均在范围。402 处命令执行调用另有索引，包含效果、粒子及自定义命令路径，不能只沿 `.execute` 静态调用追伤害。

## 实际命令链与未闭合的字段链

`A:command/ShanghaiCommand.java:24–62` 注册 `/shanghai` 并调用 `ShanghaiGoProcedure`。后者 68–90 行读取类别、攻击者 UUID、固定伤害标记、基础量、倍率；111–150 行处理关系，154–182 行处理闪避，183–214 行调用 `Zhilingdamage/Zhilingdefence` 并结算：音波/灵魂为攻击量减防御，其余为 `攻击量² / (攻击量 + 防御)`。这只是该命令路径的阶段公式，不代表全部属性贡献公式。

216–257 行处理保命状态和 `itemfulu_16` 的受伤替代分支；实际 `hurt` 在 257、321 行。超过当前生命的分支之后还更新体质击杀计数（258–317 行），不等于已确认 `hurt` 后目标确实死亡。目标击杀结果应如何定义需裁定，不能默默搬到死亡事件后宣称等价。

| 字段读取位置 | 读取的对象 | 当前证据 |
| --- | --- | --- |
| DamageIntegration:26、ManaDamage:50/57/61 | 受击实体的 ManaAttackDamage | 两类计算过程未从结构入口图到达 |
| SoulAttjisuan:50/57/61 | 受击实体的 SoulAttackDamage | 计算过程未从结构入口图到达 |
| B Zhenyang01DangTouZhiWuJiZhongShiTiShi:22 | 弹体的 ManaAttackDamage | 复制到受击实体；该读取本身不调用伤害结算 |
| A SkillTree01TouZhiWuFeiXingTick:28 | 玩家所选技能物品的同名键 | 不属于阵法写入的实体载体，不能因同名认作消费者 |

以上来自全量键接收者索引和实际源码复核，仍不排除运行时脚本的动态键操作。`/shanghai` 直接以命令参数输入，不能自动视为所有实体伤害键的统一消费者。保真验收需要分别覆盖命令路径、原生攻击和这些未闭合路径。

## 目标所有权与接口提案

关系服务负责队伍/好友/主人归属查询；伤害入口明确攻击者、受击者、来源、类别、基础量及来源效果。把免伤/目标过滤、伤害变换、实际承伤后的效果分到目标事件对应阶段；禁止同时在入口和事件中重复计算同一份伤害。

保留原生 `hurt`、直接 `setHealth`、效果施加等不同路径的证据，逐项决定目标方式。武器/法宝/技能/符箓/阵法/生物提交命中，不直接写别的系统存档。附魔数据定义与执行规则分离；特例可以有专用方法。

## 待裁定与验收

`EntityBeforeHurtProcedure:102–110` 与 `ShanghaiGoProcedure:132–134` 的非玩家攻击玩家分支混用了双方数据来源，需独立裁定。前者、命令路径、阵法字段链不能强行合成一套推测公式。伤害阶段映射见公共目标 API 证据；目标版本运行时还要验证护甲、吸收、无敌帧、友军和击杀归属。
