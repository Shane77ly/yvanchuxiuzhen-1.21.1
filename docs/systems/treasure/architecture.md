---
topic: treasure
role: architecture
status: partial
updated: 2026-11-07
---

# 宝箱：架构调查与提案

来源事实与目标方案分别陈述。完整系统提案尚未整体采用；末节单独记录已按批准系统设计实施的成品入口。源码定位采用 [系统导航](../README.md)的 A/B 规则。

## 1.18.2 数据与入口（已读范围）

`Baoxiangzong` 由 `/baoxiangopen` 命令进入，按容器类型（如书架、药架）与类别（书籍堆、符箓堆、丹方、功法书、技能书等）分派到 `Baoxiang0101`、`BaoxiangFulu`、`BaoxiangLiandantuzhi`、`BaoxiangGongfa`、`BaoxiangJineng` 等过程。`TreasureChest11ZaiFangKuaiShangYouJian` 右键时调用 `TreasureChest11FangZhiFangKuaiShi`（按 1–100 随机值分 8 级）并写方块实体 `dakai`。`~/tag/baoxiang` 有 26 个标签。

## 目标所有权与接口提案

宝箱定义（容器类型、类别、等级与奖励表）归本系统；奖励物品由所属系统的生成入口创建，本系统组合结果。

## 待调查

各类别奖励过程、`Tiandaobaoxiang` 与 `/baoxiangset` 尚未逐条读取。

## 成品命令（2026-11-07）

已读 `B:LianqiCommand` 与 `BaoxiangChengpin`，归属确认为奖励生成而非正式炼器；未写玩家能力，`typed_field_accesses.csv`没有这两个入口的玩家字段访问。`/lianqi`要求权限1，参数按空白拆分，返回0；来源奖励字符串（如`BaoxiangFabao`）会调用该命令，完整奖励总表仍待迁移。

命令位置向下取整为方块坐标，先读物品能力槽0，非空时整段不执行；原生临时物品命令只支持`Container`，没有可用容器时不改成随地给予。目标经equipment的公开成品构造生成物品，经容器槽0读回副本，在命令原坐标掉落、拾取延迟10刻；物品能力可修改时清槽0。保留来源通过容器中转的限制与顺序。

品阶、参数、数值与Q59的唯一正文在[装备](../equipment/architecture.md#宝箱成品与品阶2026-11-07)。构建/ArchUnit及末次六项成品专项通过，专用服务器一次启动和原生stop保存退出通过，ERROR0；证据及运行次序同装备正文。本命、开箱分流、奖励概率与正式炼器均不能据此标记完成。
