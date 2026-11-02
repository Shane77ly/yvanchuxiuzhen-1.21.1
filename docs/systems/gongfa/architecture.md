---
topic: gongfa
role: architecture
status: draft
updated: 2026-11-01
---

# 功法、研习与学习小游戏：架构调查与提案

来源事实与目标方案分别陈述。目标方案未批准；源码定位采用 [系统导航](../README.md)的 A/B 规则。
## 1.18.2 数据与调用

`A:BookRightClickProcedure:135–260` 检查绑定、境界和灵根；`GongfashuxingjisuanProcedure:13–28` 将绑定者和初始等级写入手持书籍。`KaishigongfaProcedure:82–129` 在装备书上更新 `ExercisesExp/ExercisesLevel`，逐项增长属性后重算玩家属性。

`PlayerGUI04DangZheGeJieMianBeiGuanBiProcedure:19–199` 将菜单 0/1/2/7 槽写回功法/轻功/飞行/武器技，3–6 槽对应四页共 16 技能槽。打开过程延迟填槽；`PlayerTickProcedure:351–361` 用 `gongfalinshi` 做比较。该暂存不是第二本永久成长档案。

**同名字段已经分开核对**：B 的 `gongfa/gongfalinshi/xvexigongfa` 在显式类型访问索引中没有类外访问；A 的功法栏有真实入口。B 的功法相关内容还包含学习小游戏：`B:GongfaxvexiGuiDangZheGeJieMianDaKaiShiProcedure:28–145` 建立穴位/星宿目标，使用长度 280 的固定随机序列游标 `suijishu`，起点为 (8,8)，把 `gongfaxvexi` 同时写到装置和玩家持久 NBT。不能用两个字段同名推断存在两套实际功法装备栏。

## 目标所有权与接口提案

功法定义说明属性档位、需求和升级公式；书籍组件保存绑定、等级、经验与实例值。玩家功法槽只保存真实书籍，成长不会转为永久解锁列表。参悟台保存自身输入与加工状态。

学习小游戏单独保留 `LearningSession`：装置位置/维度、参与者、目标集、当前位置、剩余灵力和序列游标；其结果调用书籍学习入口。不要把 415 个显示过程机械变成 415 个服务，也不能删掉这部分交互。显示网格由会话状态计算。

接口分别为绑定书籍、装备/取出、推进研习、参悟、创建/操作学习会话、读取属性贡献。装备界面跨功法和技能的操作由具体协调入口完成；菜单关闭与取出必须以真实物品去向验证。

## 裁定与验证

`Gongfashuaxin/2` 未从当前结构入口图到达；不能仅因它们做增减属性就选作目标主链，当前有效链包括 PlayerTick 的 `Shuxingjisuan/2`。学习会话同时写玩家/装置后的多人覆盖行为需明确目标操作规则。数值、实例转移、重登、普通死亡、参悟和小游戏成功/失败分别对照。
