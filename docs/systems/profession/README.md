---
topic: profession
role: entry
status: partial
updated: 2026-11-08
---

# 副职业

标识 `profession`，类别：系统。职责：10 种副职业的等级、经验与升级（[系统设计](../../development/system-design.md)）。

当前阶段：十职业进度核心与矿石采集入口已接入主Mod。现有storage客户端场景共45项玩法检查通过，其中职业覆盖真实矿石破坏加2经验、自然每刻升级扣旧上限和本人同步、实际死亡复制及重登持久化。职业页面、管理命令、浮生丹与生产装置消费者仍待所属批次接入，双手条件、低概率额外掉落等仍待专项验证，未完成系统验收。

- [架构与调查正文](architecture.md)：来源入口、数据所有者、调用契约、生命周期和裁定事项。
- 来源范围为 `FuzhiyeLevelUp` 与 `ZhiyeTXT01`–`20` 共21个元素，本批已逐个核对升级与文本行为；已同步共享归属表为21项已读。这是来源元素数，不是已验证功能数。见[归属表](../../reference/legacy/inventory/ownership_assignment.csv)。
- [进度字段台账](ledger/progress.csv)：十职业的30个来源数值、默认值、复制证据与目标状态。
- [系统导航](../README.md)记录源码引用规则与各项证据范围。

## 公开操作

公开枚举 `api.Profession` 固定十职业；`api.Professions` 提供下列操作。写操作只接收 `ServerPlayer`，查询接收 `Player` 供服务端与本人的客户端快照使用，不公开附件或整份档案写回。

| 操作 | 用途与来源 |
| --- | --- |
| `level`、`experience`、`experienceMax` | 查询独立等级、经验、上限；装置开窗/开炉快照、掉落公式与职业页文本使用 |
| `addExperience` | 生产者增加经验，不截断、不立即升级；来源真实生产入口与经验命令的加减分支 |
| `setExperience` | 管理命令经验赋值，不联动等级或上限 |
| `setLevel` | 管理命令等级赋值，不联动经验或上限；采集等级同时维护旧持久键镜像 |
| `reset` | 浮生丹调用，十职业恢复等级0、经验0、上限100；血脉等其他状态仍由其所有者处理 |
| `levelText`、`experienceText` | 对应 `ZhiyeTXT01`–`20`；等级用 `DecimalFormat("0")`，经验沿用 double 字符串 |

已对照 `typed_field_accesses.csv` 把全部外部访问按管理命令、重置、炼丹/炼器/符箓/厨具读取、矿石采集、蛊虫升级与页面显示分类；逐项接入状态见[正文](architecture.md)。`GatherExperienceEvent` 对应的来源命令没有注册入口，当前不消费该事件补造经验。

本轮变化与验证边界见[本月记录](logs/2026-11.md)。
