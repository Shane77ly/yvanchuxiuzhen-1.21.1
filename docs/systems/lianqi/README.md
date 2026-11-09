---
topic: lianqi
role: entry
status: partial
updated: 2026-11-09
---

# 炼器

标识 `lianqi`，类别：系统。职责：鼎炉、图纸、熔炼、精炼、修理（[系统设计](../../development/system-design.md)）。

当前阶段：D阶段八鼎自然开窗、十二槽保存、每二十刻供热、制作/升级/熔矿/精炼、图纸保存/装料/领取、原生界面与材料渲染已接回，`weaponlevelup`升级消费者已接回。静态来源核对与`build compileSmokeJava`通过；既有内置`equipmentproduct`客户端最终36项全INFO、0FAIL、DONE，其中本批8项通过。客户端停止阶段记录一条Netty连接关闭ERROR，四维度保存及退出完成；专服02鼎受控起炉后的真实计划刻熔矿/精炼样本通过，正常保存退出且ERROR/FATAL为0。本批27条注册记为行为已迁移，前两轮失败、实际覆盖与未验收边界见[月志](logs/2026-11.md)。天道图纸台、修理台、针线笸箩、毒药炼锅及其余候选仍待实现。

- [架构与调查正文](architecture.md)：来源入口、数据所有者、调用契约、生命周期和裁定事项。
- 候选归属146个元素，其中82个已读；这是检索范围，不是该单位已验证功能数。见[归属表](../../reference/legacy/inventory/ownership_assignment.csv)。
- [内容台账](ledger/content.csv)：来源注册的目标 ID、归属状态与迁移状态（`tools/porting/check_ledger.py` 检查）。
- [来源证据](ledger/source-evidence.csv)：逐条登记本批静态读取、规则与目标操作。
- [系统导航](../README.md)记录源码引用规则与各项证据范围。

千材录两个属性/等级说明过程已随实际菜单移归[material](../material/README.md)。

## 公开操作

- `LianqiRules.WEAPON_LEVEL_UP`：查询炼器鼎是否允许升级武器、法宝和护甲；规则真值由世界游戏规则保存，炼器系统是唯一消费者。
- `TripodRuntime.initialize/use/tick/button`：初始化、自然菜单、计划刻及经当前菜单校验的按钮操作。
- `TripodRuntime.heat/exhausted`：接异火供热/耗尽事件，仅修改自身鼎状态。

材料、材料仓、职业与装备跨系统操作及唯一写入者见[正文](architecture.md#数据所有者与公开操作)。本批未新增Test或检查选择器，未使用Computer Use；实际构建和项目内置客户端结果分别写入月志。

本系统变化记录在[月度日志](logs/2026-11.md)。
