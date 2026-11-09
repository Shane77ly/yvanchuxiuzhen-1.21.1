---
topic: lianqi
role: entry
status: partial
updated: 2026-11-09
---

# 炼器

标识 `lianqi`，类别：系统。职责：鼎炉、图纸、熔炼、精炼、修理（[系统设计](../../development/system-design.md)）。

当前阶段：D阶段八鼎自然炉次、图纸、装料与领取已接回；本批接回修理台、针线笸箩、天道图纸台、毒药炼锅与六材料副手淬毒，覆盖新增15条、累计42条注册的行为实现。归属147项全部已读，来源证据170条。四装置第二轮既有内置`equipmentproduct`客户端47项INFO、0FAIL、DONE，正常保存四维度退出、ERROR/FATAL为0；专服四种方块实体受控样本已完成真实保存、退出、重启读回。持久化文本Component汉化补充后，最终`build compileSmokeJava`与第三轮客户端48项INFO、0FAIL、DONE通过，四维度正常保存退出，ERROR/FATAL0。最终复审补齐模板自定义名去方括号规则，第四轮构建与48项检查再次通过；首轮失败、实际覆盖、截图裁切及剩余验收见[月志](logs/2026-11.md)。完整生命周期、多人、视觉与两版本对照未完成，系统保持partial。

- [架构与调查正文](architecture.md)：来源入口、数据所有者、调用契约、生命周期和裁定事项。
- 候选归属147个元素，147个已读；这是来源职责阅读数，不是该单位已验证功能数。29项收口包含已有资源、12个未发现自然绑定的过程和2个有消费者的辅助过程，见[正文](architecture.md#剩余候选来源审读2026-11-09)与[归属表](../../reference/legacy/inventory/ownership_assignment.csv)。
- [内容台账](ledger/content.csv)：来源注册的目标 ID、归属状态与迁移状态（`tools/porting/check_ledger.py` 检查）。
- [来源证据](ledger/source-evidence.csv)：逐条登记本批静态读取、规则与目标操作。
- [系统导航](../README.md)记录源码引用规则与各项证据范围。

千材录两个属性/等级说明过程已随实际菜单移归[material](../material/README.md)。

## 公开操作

- `LianqiRules.WEAPON_LEVEL_UP`：查询炼器鼎是否允许升级武器、法宝和护甲；规则真值由世界游戏规则保存，炼器系统是唯一消费者。
- `TripodRuntime.initialize/use/tick/button`：初始化、自然菜单、计划刻及经当前菜单校验的按钮操作。
- `TripodRuntime.heat/exhausted`：接异火供热/耗尽事件，仅修改自身鼎状态。
- `RepairTableRuntime.rightClick/opened/refresh/button`：自然开窗、需求缓存刷新与即时扣料修复，40刻后刷新提示；飞行器实例经equipment公开操作写入。
- `SewingBasket.opened/select/page/craft/closed`：共享八槽预览、延后选页、按Item检查材料、复制结果槽交付与关闭清空。
- `BlueprintTableRuntime.populate/refresh/save`：每次开窗目录、九材料预览及在槽9载体保存图纸；名称输入仍保留来源整合端与专服边界。
- `PoisonCrafting.rightClickBlock/rightClickItem/useCauldron`：水锅转毒锅、六材料副手淬毒与毒锅使用；装备累计记录经`Equipment.recordPoisonEnchantment`写入。

材料、材料仓、职业与装备跨系统操作及唯一写入者见[正文](architecture.md#数据所有者与公开操作)。本批未新增Test或检查选择器，未使用Computer Use；实际构建和项目内置客户端结果分别写入月志。

本系统变化记录在[月度日志](logs/2026-11.md)。

本批汉化与行为验证完成，下一步按路线推进D后续制符系统，并接续炼器未验范围。七种宝箱材料品质消费者随treasure接续；`Armor`标签既有替代差异及12个孤立过程去向边界见正文，不把归属已读计为这些项目已验收。完整移植goal已恢复active；skill遗留未闭合，当前不合main或push。
