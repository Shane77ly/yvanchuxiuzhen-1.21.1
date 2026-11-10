---
topic: cooking
role: entry
status: partial
updated: 2026-11-10
---

# 烹饪

标识 `cooking`，类别：系统。职责：食材、厨具、菜谱、食用效果（[系统设计](../../development/system-design.md)）。

当前阶段：D 部分实现。除 B1 内容身份与资源及辟谷效果 `effect_031` 外，已接回铁锅、砂锅的14槽库存/菜单、两锅自然合成配方、菜谱执行路径和墨料坯自然生产。2026-11-10 已接入木甑、八酒、陈酿与天然猴儿酒38条注册，蒸锅/笼屉8条注册，以及37种盘菜和52种直接食用物品的126条注册；三族完成来源与精确目标源码的独立静态复审。该三族批次资源转换13411项、既有missing19、dropped91，data转换1703项；完整构建及检查代码编译通过，内置 `runSmokeClient -PsmokeOnly=fulucooking` 124项INFO、0 FAIL、DONE，受控客户端场景四维度正常保存退出、ERROR/FATAL0。木甑、蒸锅和笼屉四张指定截图已查看，背景和可见中文正常；该截图与自动场景不代表完整视觉、重登或生存获取验收。当前追加的配方标签、餐食Tooltip与植物供给批次另见本页后续记录和月志。完整食材与成品获取链未闭合。

2026-11-09 内置客户端 `fulucooking` 已验收铁锅自然右键开窗、墨料坯开始/10刻交付/五次翻炒完成与保存读回，以及砂锅菜谱扣料、槽4覆盖、菜谱保留和完成经验；该轮与符箓共用场景共14项通过。2026-11-10 的124项报告覆盖当日三族，证据及受控计划刻/保存读回边界见[月志](logs/2026-11.md)。餐具、案板/菜刀与菜谱命令对应客户端142项自动检查通过；植物供给/Tooltip批次的160项最终结果及截图方式见本页后续月志记录。以上均为指定内置场景，不代表完整生存供给、自然生成、重登周期、多人、完整视觉、来源版本实机或两版本对照。观想自然入口和世界结构获取仍未闭合。

- [架构与调查正文](architecture.md)：来源入口、数据所有者、调用契约、生命周期和裁定事项。
- 候选归属与已读状态见[归属表](../../reference/legacy/inventory/ownership_assignment.csv)；这是检索范围，不是本系统已验证功能数。
- [内容台账](ledger/content.csv)：来源注册的目标 ID、归属状态与迁移状态（`tools/porting/check_ledger.py` 检查）。
- [源码证据](ledger/source-evidence.csv)：两锅配方、菜谱、墨料坯、木甑、酒、陈酿、蒸锅/笼屉及普通食物消费的来源对应；全局右键只登记蒸锅分支，不改 flow 所有者。未覆盖项目范围内全部烹饪内容。
- [记录索引](changes.md)：按月查看已实现范围与验证证据。
- [系统导航](../README.md)记录源码引用规则与各项证据范围。

## 公开操作

炼丹通过原生状态效果入口施加 `effect_031`；`baoshidu` 计时由 cooking 独占写入，不暴露直接写入口。已查 `typed_field_accesses.csv`，该实体持久键不在强类型字段表；`key_expressions.csv` 与来源代码确认读写仅在 `Effect031ZaiXiaoGuoChiXuShiMeiKeFaSheng`。厨具库存与生产状态由 cooking 独占写入；其他系统不得直接改写方块实体或菜谱状态。材料实例只通过 `Materials.instance` 查询，职业只通过 `Professions.level` 查询并以 `Professions.addExperience` 写入经验；覆盖 `typed_field_accesses.csv:1304–1309` 的外部读写点。

两锅批次实现中：`fooddaguo`、`fooddunguo` 的14槽库存、烹饪状态与坐标延迟由 cooking 独占写入；菜单按钮只提交按钮编号，服务端验证当前菜单绑定的方块实体。材料等级只读 `Materials.instance`，厨艺等级读取 `Professions.level`，完成经验通过 `Professions.addExperience` 写入并同步；覆盖 `typed_field_accesses.csv:1304–1309` 两锅对 `cooking_LEVEL` 和 `cooking_experience` 的全部外部读写。不存在 cooking 对职业附件或材料实例的直接改写。菜谱当前读取物品 `CUSTOM_DATA` 中已核来源字段；完整食材与成品获取链及菜谱自然获取尚未闭合。已读来源算法按 `forge:cailiao` 槽位平均材料实例等级；来源帮助文案称“同级”材料，与实际算法不完全一致，目标保留算法并记录该来源差异。墨料坯生产静态代码复核未发现公式、扣料和10刻交付时序偏差；铁锅墨料坯与砂锅菜谱路径已有内置客户端验收；本批专服加载保存退出已通过，专服自然玩家交互未验收。

木甑、酒与酒精的公开边界如下；具体公式与生命周期见[架构正文](architecture.md#木甑酒与陈酿2026-11-10-静态接入)。

| 操作 | 调用与数据边界 |
| --- | --- |
| `WoodenStillRuntime.button / tick` | 八配方开始、帮助和20刻生产周期；五槽库存及生产字段由 cooking 独占写入 |
| `WineRuntime.place / age / useJar` | 酒物品放坛、1200刻年份递增、展示和收取；年份真值为槽0物品的 `agenum` |
| `WineRuntime.snapshot / drink` | 开始用酒写共享体质快照，完成按该快照结算；通过 `Cultivation` 公开体质操作读写 |
| `Alcohol.amount / add` | 本系统酒精 Double 附件查询和增减；保存、死亡与非死亡克隆均保留，只同步本人；外部不得直接改附件 |
| `Alcohol.metabolize` | 订阅修炼公开突破后每秒事件，保持吸收/突破之后、冷却之前的来源顺序 |
| `Alcohol.refine` | 酒精至少1时先提示，再清零并移除反胃；观想自然调用尚未接通 |
| `Cultivation.add(EXPERIENCE_STAND)` / `Techniques.main` | 分别写待吸收修为、读主修功法定义名；数据归 xiulian 与 gongfa |
| `WineVesselBlockEntity.handler` / `WoodenStillMenu.press` | 木甑六面插0–3取4；酒坛六面禁插取，无方向提取/强制赋槽仍有效；按钮绑定当前菜单与同一设备 |
| `WineRuntime.ignite` | 酒坛爆炸及燃烧弹体命中入口；保留来源粒子、半径4正常掉落爆炸和343格直接火域 |

酒族覆盖 `typed_field_accesses.csv` 物理行2571–2609的全部39个唯一外部访问：体质读2/写1、酒精读9/写7、待吸收修为读8/写8、主修功法读4。逐行类名、Java行号、字段与访问方向已独立静态复审；酒精改由本系统拥有，其余经公开操作，不直接修改外部附件。开始快照与酒精属于不同所有者，饮用完成不清共享快照。天然猴儿酒块已有明确结构模板及解锁命令来源，尚待 world 批次接通实际获取，不能登记为无来源或获取已闭合。

蒸锅/笼屉与普通食物的入口和数据边界如下，完整顺序与来源异常见[架构正文](architecture.md)。

| 操作 | 调用与数据边界 |
| --- | --- |
| `SteamerRuntime.rightClick / used / button` | 蒸锅全局事件开窗、两设备自然右键、菜谱开始及五次完成；库存、标记、经验暂存与动画由 cooking 独占写入 |
| `SteamerBlockEntity.handler` / `SteamerMenu.press` | 蒸锅14槽64、笼屉9槽1；六面及无方向能力保留来源；按钮绑定当前菜单和同一设备 |
| `Professions.level / addExperience` | 蒸锅读厨艺等级，最后一次完成点击给经验；对应typed表物理行1310–1312的3项访问，源Java24读等级、100读经验、109写经验 |
| `MealEffects.afterEating / usePlate` | 原生食用后标签效果、盘菜饱食及容器替换；仅写原生食物与状态效果，无外部玩家附件字段访问 |
| `MealFoodItem / PoisonousFoodItem / RemainderFoodItem` | 46餐食、059毒材及5返物食品的原生完成入口；食用缩栈由原版处理，返物通过玩家原生背包/掉落交付 |

蒸锅族仅核对全局 `PlayerRightClickBlock` 的蒸锅分支，该全局过程仍归 flow，其他分支不据此标记已读或完成。蒸锅族source-evidence新增29项，其中28项归cooking、1项保留flow所有者。普通食物覆盖本族全部52个直接食用回调及37个盘菜回调，source-evidence新增93项；`typed_field_accesses.csv`外部读写为0；effect_028/029/030由 cooking 原生状态效果注册承载，其他系统通过原生效果API查询，不新增附件写入口。设备、菜谱、食用消费的静态接通不代表菜谱与食材自然获取已经闭合。

本轮变化记录在[系统月度日志](logs/2026-11.md)，项目进度见[项目月度日志](../project/logs/2026-11.md)。

本批在 `port/d-cooking` 接入三餐具、案板/菜刀17条注册及 `/shipu` 管理命令。三餐具单槽与设备状态由本系统独占，未新增外部持久字段访问；入口、来源异常及验证边界见[架构正文](architecture.md#堆叠餐具案板与菜刀2026-11-10)。本批完整构建、既有检查代码编译通过；客户端142项INFO、0 FAIL、DONE，正常保存四维度退出。餐具层/菜刀自然动画及碗和案板中文旁观菜单四张截图已查看。自然食材供给与菜谱获取仍待闭合，当前不登记系统完成。

铁盘独立来源核对发现空战利品表时应掉落自身一件，已由专用方块实现承载；专项场景验证与记录见[架构正文](architecture.md#铁盘掉落2026-11-10)及[月志](logs/2026-11.md)。自然食材供给与菜谱获取仍待闭合。

13条普通获取配方和8个有效分类标签已逐项来源/目标JSON及manifest核对；其中12条材料/食物加工归Cooking，001seed转大米归Cooking，039转种子归Plant，详见对应来源证据。Canbeground保持候选（仅游离编辑器积木引用，生成Java无实际消费者）。餐食Tooltip经既有客户端自动检查通过并查看指定坐标渲染截图；本批目标端场景160项INFO、0 FAIL、DONE。自然配方运行与完整食材供给仍待验收，规则见[架构正文](architecture.md#普通食物获取配方与分类标签2026-11-10)和[月志](logs/2026-11.md)。

糯稻002续批补读四种子转一份糯米的普通无序配方、定义和糯米物品类，按食材加工职责归Cooking；Plant只提供种子。目标既有配方无改动；原生RecipeManager匹配及assemble已在最新166项INFO、0 FAIL、DONE的内置场景通过，完整构建与专服加载保存退出通过。该检查未模拟玩家工作台取出，完整自然供给仍待闭合，见本批月志。
