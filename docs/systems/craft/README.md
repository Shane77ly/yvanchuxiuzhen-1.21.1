---
topic: craft
role: entry
status: partial
updated: 2026-11-10
---

# 工艺

标识 `craft`，类别：系统。职责：木匠台、陶轮、石磨、织布机与蚕丝链（[系统设计](../../development/system-design.md)）。

石磨与石碾批次已接回的三槽库存、自然交互、七种即时研磨、`/grind` 命令、原生菜单及方块与物品 Gecko 渲染。完整构建已通过；菜单中文键转换与登记修复后，最终既有 `fulucooking` 的53项全部 INFO、0 FAIL、`DONE`，其中本批新增六项全部通过。客户端退出码0、四维度正常保存、ERROR/FATAL 0，截图研磨中文正常；专服已启动至 Done 并原生 stop、四维度保存、构建成功退出且 ERROR/FATAL 0。专服自然玩家与自然获取链验证未执行；不能据此把工艺系统标记完成。B1 原有“已注册”内容只表示身份、资源与基础属性就位。

- [架构与调查正文](architecture.md)：来源入口、数据所有者、调用契约、生命周期和剩余范围。
- 归属范围 128 个元素；木匠台完整菜单与过程族、木匠台和缫丝轮自然配方接入后为 106 个已读、22 个候选。这是来源阅读覆盖，不是玩法验收数。见[归属表](../../reference/legacy/inventory/ownership_assignment.csv)。
- [内容台账](ledger/content.csv)：来源注册的目标 ID、归属状态与迁移状态（`tools/porting/check_ledger.py` 检查）。
- [来源证据](ledger/source-evidence.csv)：设备、菜单、渲染、研磨与织布链的具体源码及目标实现。
- [系统导航](../README.md)记录源码引用规则与证据等级。

当前织布机批次正在接回 `zhibuji` 方块、物品和1槽方块实体，覆盖八种基础布、双向金属布与云雷锦九种布的优先分支；完整构建与检查源集编译通过，既有 `fulucooking` 59项 INFO、0 FAIL、DONE，新增织布五项与容器上限回归通过；客户端正常保存四维度、退出码0、ERROR/FATAL0。专服Done后原生stop、四维度保存、exit0与ERROR/FATAL0；详见本批月志。材料自然供给与其他工艺设备继续保留未完成范围。

木匠台与缫丝轮代码、工厂、菜单和客户端资源已正式接入。木匠台完整37配方、43按钮、42槽共享库存及来源累计计数/结果覆盖规则已静态核对；缫丝轮三色蛹、邻容器与热水釜两路径、方向快照与20刻周期已实现。完整 `build compileSmokeJava` 通过；本轮既有 `fulucooking` 73项 INFO、0 FAIL、DONE，客户端正常保存四维度、exit0、ERROR/FATAL0；专服Done后原生stop、四维度保存、BUILD SUCCESSFUL/exit0、ERROR/FATAL0。木工菜单截图已查看，动态产物与材料为中文；陶轮与蚕盒仍未正式接入。

## 公开操作

石磨与石碾的库存、研磨执行和动画请求归 craft。自然右键与命令复用同一研磨入口；当前没有供其他系统调用的玩家档案查询或写操作。织布机即时加工归本系统，通过 `Materials.setInstance(ItemStack,int,int)` 写输出等级和品阶，不直接改 material 组件。

| 操作 | 调用与数据边界 |
| --- | --- |
| `Grinding.grind(ServerLevel, Vec3, String)` | 按坐标读取无方向物品能力，先掉出旧成品，再解析次数并研磨；不限于石磨方块。供本系统设备与命令复用，主辅料及结果只按注册 ID 引用 |
| `StoneMillRuntime.use(Level, BlockPos, Player)` | 本系统方块入口：潜行打开绑定设备库存的菜单，普通使用即时研磨四轮并请求动画1；玩法只在服务端执行 |
| `StoneMillBlockEntity.handler(Direction)` | 本系统能力装配：无方向提供库存写入，六方向按来源只插槽0/2、只抽槽1 |
| `LoomRuntime.use(Level, BlockPos, Player)` | 本系统织布机交互：按主手材料与玩家库存加工，统一请求动画/粒子；设备1槽库存不参与配方 |
| `CarpentryRuntime.use / refreshCandidates / select / craft / cycleCategory` | 本系统木匠台入口、候选刷新、选择快照、即时扣料制作与类别切换；42槽库存和全部快照归同一方块实体，多玩家共享 |
| `CarpentryBlockEntity.handler(Direction)` / `CarpentryMenu.press` | 无方向完整42槽包装，六方向只取41且不能插入；按钮以当前菜单和同一设备绑定后执行，不新增距离或旁观者门槛 |
| `SilkReelRuntime.use / tick` | 本系统缫丝启动及20刻周期推进；作业状态和9槽库存归设备，原料来自启动方向前方低一格的邻容器槽0或热水釜掉落实体，不写其他系统玩家档案 |

本批对照 `typed_field_accesses.csv`，按 `Stonemill`、`Stoneroller`、`Grind` 类名检出 0 行；完整方法阅读也未发现玩家能力字段读写。织布机 `Zhibuji` 与 `Cailiaopinjie`、木匠台 `Mujiang` / `Mugongtai` 以及缫丝 `Saosilun` 检索同样为0行，完整调用链没有职业或修炼玩家字段读写。这个结论覆盖已读四设备族，不覆盖尚未逐项阅读的其他工艺设备。七种产物及来源输出复制、主料数量缓存行为见[正文](architecture.md#石磨与-grind-来源核对2026-11-09)。

## 剩余范围与验证

陶轮与陶坯、蚕盒及蚕蛾链尚未正式接入；木匠台与缫丝轮本批构建、客户端73项与专服启动/保存退出通过。织布机真实右键棉花、带组件双金属、云雷选布优先与副手边界、受控单槽保存及六向能力已有运行证据；其余配方逐项实际点击、失败提示与动画、实际漏斗/拆除/重登及完整视觉仍待验收。棉麻作物、妖兽毛/蛛丝、三色蛹、金属锭和引雷草的自然生产者尚未全部移植；缫丝已运行验证金蛹无热源容器路径与红蛹热水釜路径；全部三色/朝向与只读容器边界仍待验收。木匠台产出的35家具放置行为归decor；`itemdanyaohulu_04` 小盒子、`itemdanyaohulu_05` 六边盒的后续使用及榉木自然供给不因制作入口接通而完成。石磨与石碾两份自然配方及挖掘标签已转换；首轮真实潜行开窗、辰砂四轮研磨、N²旧输出、辅料逐轮重读、受控保存读回与六向能力已有运行证据。菜单汉化修复后的最终复测已通过，自然合成操作、拆除掉落、退出重进与完整模型/动画视觉仍待验证；专服自然玩家、1.18.2 版本实机、1.21.1 版本多人和两版本对照未执行。

批次变化与验证边界记录在[月度日志](logs/2026-11.md)，[变更索引](changes.md)只保留导航。
