---
topic: relation
role: architecture
status: partial
updated: 2026-11-03
---

# 关系：来源事实与实现

来源事实与目标实现分别陈述；源码定位采用 [系统导航](../README.md)的 A/B 规则。静态核对原初修真的生成 Java，未运行 1.18.2 版本。

## 1.18.2 数据

| 载体 | 内容 |
| --- | --- |
| 玩家 `team`（String，初值空） | 团队名；`PlayerJoinGame` 在为空时生成 8 个随机小写字母，并把它复制到实体 NBT `team` |
| 玩家 `friendlist`（ItemStack，载体物品 `cailiao_181`）的标签 `friendlist` | 以团队名为键的布尔值（友方集合）；好友格 `friendname<n>`、`friendteamname<n>`、`friendstar<n>`、`friendblack<n>`，好友数 `friendnumber`；登录时把本人团队设为 true |
| `friendlist` 物品标签的其他键 | 待处理申请 `zuduiname`、`zuduiteamname`；界面状态 `displaypagenumber`、`displayxvanzenumber` 与每行显示缓存 `displayfriend*<n>` |
| 其他实体的 NBT | `team` 与 `friendlist`（同样以团队名为键的布尔值）；技能、召唤、妖兽、符箓等约 120 处在生成实体时复制施放者的这两项，`EntityTeamShezhi` 把实体类型 ID 设为团队并加入友方集合 |
| 方块实体数据 `friendlist` | 陶罐、丹火、旗母、副本方块等判定接近者是否友方 |

复制策略：`team`、`friendlist` 为 all_clone_events（[复制策略表](../../reference/legacy/inventory/clone_policy.csv)）。

## 1.18.2 行为

- 敌友判定（`FriendpandingStoE`、`EtoE`、`StoEe`、`EtoS`、`BtoE`、`BtoEe`，六个过程只是参数顺序与来源不同）：判定方的 `friendlist` 中以对方 `team` 为键的值为 true 即友方；玩家读 PlayerVariables，其他实体读 NBT，方块读方块实体数据。
- 团队招募令 `ling_15`：潜行使用时刷新并打开好友界面 `Friendgui`；主手持有右键玩家（`RightClickEntity` 的玩家分支，`getHand() == getUsedItemHand()`）时向对方发好友申请。
- 申请（`Ling15RightClickEntity`）：先向申请者打印“循环：好友数”、逐格“名字：…”，对方已在列表且未删除时打印“不可以”与“…已成为您的好友，无需重复申请。”；否则提示“已向…发送好友申请。”，把申请者名字与团队写入对方的待处理申请并打开申请界面 `ZuduiteamGui`。
- 申请界面：文字“是否接受来自…的好友申请？”，同意 `ZuduiteamYes` 与拒绝 `ZuduiteamNo`。同意时申请者须在本人所在维度在线：申请者一侧在列表末尾加本人并把本人团队设为友方，本人一侧同样加申请者并清除申请；对方不在线时提示“§4对方已下线或不在此位面，无法添加好友！”。关闭界面时 `CloseteamGui` 写两个没有读取者的 NBT 键，无效果。
- 好友界面（`Friendshuaxin` 刷新）：每页 10 格；先列本页有星标的格，再列本页没有星标且未删除的格（包括好友数之外的空格）；在同一维度按显示名找到的玩家为在线，显示“(境界)名字”与六行属性（最大生命、灵力、精力、物理、法术、精神的上限或攻击及回复或防御，取 `_display`），否则“(未上线)名字”。刷新后选中行按名字保留。
- 好友界面按钮：选中 1–10 行；上一页（第 0 页时跳到最后一页，没有好友时为 -1）、下一页（最后一页时回到 0）；星标切换；上移、下移（与相邻格交换全部内容）；删除（标为删除、去掉星标、把该格团队移出友方集合，好友数不减少）。界面上的名字输入框没有对应按钮。

## 归属阅读（2026-11-03）

候选 84 个元素已读：86 个归 relation（含原候选 panel 的 `Gui0301`、`Gui0301anniu`、`Gui03anniu`，改团队名的界面），`FindTheEnemy` 归 combat（生物锁定目标时在攻击方头顶发感叹号粒子，与敌友数据无关）。依据写入[归属表](../../reference/legacy/inventory/ownership_assignment.csv)。

## 目标实现

代码在 `system/relation`：

- `data`：玩家附件 `yvanchuxiuzhen:relation`（`PlayerRelation`：阵营、好友格、待处理申请、页码、选中行；界面行只同步不存档，死亡复制，只同步本人）；非玩家实体附件 `yvanchuxiuzhen:allegiance`（`Allegiance`：团队与友方集合，存档不同步）。载体物品与界面显示缓存不再使用。
- `api/Relations`：[公开操作](README.md)。
- `FriendList`、`TeamRequest`：好友界面与申请的服务端操作，按来源逐步迁移，包括下文 Q19 的来源缺陷。
- `content`：`RecruitmentItem`（`ling_15` 的物品类型，内容表工厂 `relation:ling_15`，登记在 `docs/reference/conversion/factory_overrides.csv`），菜单 `friendgui`、`zuduiteam_gui`（无槽位）。
- `network/RelationButtonPayload`：两个界面的按钮（客户端 → 服务端），按发送者当前菜单与按钮范围分派。
- `client`：`FriendScreen`、`TeamRequestScreen`，坐标、贴图与文字沿用来源；贴图按 `screen_textures.csv` 迁移，界面语言键由转换工具从来源写入语言片段。按钮贴图用 `platform/client/TextureButton`（1.21.1 的 `ImageButton` 改用精灵图集）。
- 登录：团队为空时生成 8 个随机小写字母，并把本人团队加入友方集合。

与来源的差异：玩家团队不再复制到实体 NBT（判定直接读附件）；`PlayerJoinGame` 另把团队名写进隐藏载体 `tizhiitem` 的布尔键，没有读取者，不迁移。好友行的境界名在 xiulian 提供查询前取所有玩家的初始境界“凡人”（来源 `LEVELname` 初值）。

## 来源问题

- Q19（来源缺陷，影响限于好友功能；按负责人 2026-11-03 指示评估为轻微，保持来源行为并记录，之后再议）：
  1. `Ling15RightClickEntity` 向申请者打印调试文字“循环：N”“名字：…”“不可以”。
  2. `ZuduiteamYes` 的重复判定：申请者一侧比较的是申请者自己的名字，本人一侧按申请者的好友数遍历本人列表，因此会重复添加同一好友。
  3. `Playeruidel` 删除在线好友时读错对象：在本人列表里找本人的名字（通常没有）；找到时把本人整份列表写给对方。通常结果是只删除本人一侧，对方仍保留好友。
  4. 删除时把该格团队移出友方集合；同一团队有多名好友或该好友与本人同团队时，其他人也随之变为非友方。
- Q20（入口断链）：改团队名的界面 `Gui0301` 只能由 `Gui03anniu` 打开，而后者在生成代码中没有调用者；`ling_15` 的提示文本“为此物重新命名后，手持此物潜行状态右键即可为团队重新命名”描述的功能在代码中不存在（潜行使用打开的是好友界面）；`Playeraddplayer`（按输入框名字找玩家）也没有调用者。按“没有调用者不删除”保留在台账，`gui_0301` 菜单行为“待处理”，不实现。

## 验证

- 开发客户端 `runSmokeClient` 的关系检查（`-PsmokeOnly=relation` 可单独运行）：登录团队名与自身友方；非玩家实体继承阵营、设置团队、加入友方集合；11 格好友的界面行、经网络按钮的选中、星标、上移（选中跟随名字）、删除（好友数不变、团队移出友方集合）、翻页与回到首页；向已在列表中的本人申请时不发申请；申请界面的同意（申请者不在线时不添加、保留申请）与拒绝（清除申请）；死亡重生与退出重进后团队与好友格保持。2026-11-03 实测 43 项全部符合，截图 `build/smoke/screenshots/relation_friends.png`、`relation_request.png`。
- 开发服务端启动无 ERROR。
- 未执行：两名玩家之间的申请、同意与删除（单人客户端只有一名玩家）；1.18.2 版本实机对照；方块实体的敌友判定（随首个方块实体调用者实现）。
