---
topic: equipment
role: architecture
status: partial
updated: 2026-11-08
---

# 装备、法宝、本命与饰品：架构调查与提案

来源事实与目标方案分别陈述。前半的完整系统方案尚未整体采用；本页末尾单独记录已按批准系统设计实施的弓与拂尘、妖化、宝箱成品批次。源码定位采用 [系统导航](../README.md)的 A/B 规则。
## 1.18.2 实例与调用

装备包括基础 Item 类型、炼器生成的 `lianqizong`、原生附魔、耐久和实例词条，不能只迁移注册类。饰品有四个独立玩家槽；本命法宝保存到 `benmingfabao`，属性比较用的 `kuijia1…4/wuqilinshi` 是另一种职责。

`A:DantianbenmingfabaoProcedure:23–79` 读取境界、体质培养贡献和器灵附魔，向法宝自身写入等级/经验，阈值为 `2000 + benminglevel * 2000`。`BenmingfabaotishengProcedure:15–30` 交替增加法术/精神字段，同时修改 `lianqizong` 中相应来源值。交替标志现放在玩家 `tizhiitem`。

## 目标所有权与接口提案

equipment 拥有器物实例组件与专属槽位；lianqi 计算制作结果后调用器物构造入口。基础定义、实例覆盖、附魔和本命成长分开，避免炼器端和战斗端各保存一份器物属性。

公开操作：装备/取出、实例生成/升级、修理/耐久消耗、绑定本命、推进培养、查询属性贡献和可施放能力。法宝主动效果由 skill 执行，装备系统不拥有世界扫描和伤害事件总线。

载体随物品或玩家槽保存；界面编辑只在服务端提交。取出、死亡、物品移动和本命关系解绑需要明确物品归属；普通 Clone 与轮回规则分别对照。

## 规则裁定

交替培养标志是随玩家还是随某件法宝，现有载体明确为玩家；目标如果移入物品组件会改变换法宝行为，必须作为规则变更评审。不得凭“更合理”直接迁移该字段。服饰、动画护甲和渲染效果也在完整范围。

本命槽位（玩家 `benmingfabao`）归 xiulian 丹田，本命培养数据在物品上归本系统（[系统设计](../../development/system-design.md)“依赖规则”）。

## 来源登记位置

GeckoLib 盔甲（`Armor01Item`–`Armor13Item`、`Fuqi02Item`、`Clothes05Item`、`Clothes19Item`、`Yaohua0101Item`，共 65 件，`Armor10Item` 只有胸甲）不在 `ModItems`，由 `YvanchuxiuzhenModGeckoLibArmors` 用独立的注册器登记，渲染器与动画由 `ModGeckoLibArmors`、`ArmorAnimationFactory` 装配。2026-11-02 之前的登记索引漏扫了这个类，现已补入台账（装备 61 件、`festival` 4 件）；它们随 GeckoLib 的引入迁移。

## 向 attribute 提交属性贡献（2026-11-03 attribute 归属阅读）

attribute 的归属阅读把下列元素归入本系统，实现时按 [attribute 公开操作](../attribute/README.md)提交加成层贡献：

- `ShuxingjisuanZong`：来源每 tick 比较四件防具与主手和快照 `kuijia1`–`4`、`wuqilinshi`（`bubianzhi` 随机标记与物品种类），变化时重算。目标监听装备变化，按当前装备提交防具 `lianqizong.<属性>`、饰品物品 NBT `<属性>`、四件防具的附魔（`dawn` → 生命回复，`powerup` → 灵力回复，`attdef` → 物理防御，`manadef` → 法术防御）、`colddown` 附魔等级 × 0.01（四件防具与四个饰品槽）与手持武器（`forge:wuqi` 或 `forge:fabao`）`lianqizong.attack_num`、`mana_Att`、`soul_Att`；同种物品组件变化也要重新提交。登录与重生时按当前装备重新提交（贡献随附件跨越死亡保留）。
- `Weaponshuxing`：妖化防具（`forge:yaohuaarmor`，按部位带 `toukui`、`xiongjia`、`kuzi`、`zhanxve`）的物品修饰符。Q17 裁定后最大生命已含防具炼器数值，`lianqizong.health_Max` 的生命修饰符不再添加，否则重复计入；击退抗性、攻速、移速三项（按基础值乘算）保留。
- `Attributexianshi`（原初系统）：主手长枪 `weaponspear_01`–`09`（`forge:yispear`）的 `forge:attack_range` +2；1.21.1 中对应的原版属性实现时核对。

## 弓与拂尘（2026-11-07）

### 来源与状态

18 把弓为 `Weaponspeedbow01–09Item` 和 `Weaponlongbow01–09Item`，实际均继承 `SwordItem`，选中更新共用 `A:Weaponspeedbow01DangGongJuChuXianZaiShouZhongShiProcedure`。九把拂尘为 `Weaponhossu01–09Item`，共用选中与挥动过程 `Weaponhossu01DangGongJuChuXianZaiShouZhongShi`、`Weaponhossu01DangShiTiHuiDongWuPinShi`；第一把为剑，其余八把为多用途 `TieredItem`。隐藏物品、实体与飞行过程分别是 `FuchenItem`、`FuchenEntity`、`FuchenTouZhiWuFeiXingTick`。本批接回这些入口及其消费者，其他装备尚未实现。验证结果见本节末尾及项目月度记录。

Q57：弓读取实体 `synchronizedata.rightclicking`，绑定的右键过程为空。两个来源的 `src` 与 `elements` 全检只找到弓过程读取与 B 的 `Zxc` 游离实验块（另含 `rifhtclicking` 拼写），未找到有效写入。目标保留来源条件流程与剑类型，不新增鼠标同步；自然右键不能开始蓄力。该断链的修复取舍待负责人裁定，人工注入标记的检查只证明条件分支运行。

### 弓的条件流程

`Skills.arrow` 非空且满足资源条件才推进：灵气箭 `skill_arrow_02` 的灵力至少为层数 × 5，或者库存含所选箭；其他箭必须在库存中。创造与无限附魔均不绕过这一步。库存比较按完整物品实例，消耗则按物品类型移除一件，保留来源区别。

按住标记时每刻基础拉弓速度为 3，再加快速装填等级；层数至少 5、潜行且所选为第 01/02/08/09 箭、普通箭或光灵箭时再加 1。速弓每刻增加全量，长弓增加一半；不足 100 时允许本刻越过 100，下一刻发现至少 100 才写 `manxvli`，广播一枚 `xvliman` 并播放 `bowxvliman`。`xvlilizi` 仍用客户端本地 `addParticle`，不把来源服务端无效的调用改成广播。

松开且蓄力大于 0 时，把用于发射的蓄力截至 100，写 `arrowskill`，再让主手损耗一点耐久（创造同样消耗，破损后伤害值清零）。轻羽等级先于损耗读取；弓类别与无限附魔在损耗后读取，保留破损时的先后次序。发射位置为眼高减 0.1、无散布，速弓速度为 `3 × 蓄力 × 0.01 + 轻羽等级 / 2`，长弓的首项系数为 6。

| 所选箭 | 基础伤害 | 固定击退 | 发射声音与音量 |
| --- | --- | --- | --- |
| 第 01 箭 | 1 | 0 | 原版箭发射，1 |
| 普通箭 | 最终物攻 / 3；Alt 再乘 1.5 | 1；Ctrl 穿透 5 | 原版箭发射，1 |
| 第 02 箭 | 0.1 | 0 | `bowshoot3`，1 |
| 第 03 箭 | 0.1 | 1 | `bowshoot4`，1.2 |
| 第 04 箭 | 0.1 | 1 | `bowshoot5`，1；再播 `bowshoot1`，0.3 |
| 第 05 箭 | 0.1 | 0 | `bowshoot6`，1 |
| 第 06 箭 | 最终物攻 / 3 | 1 | `bowshoot1`，0.3 |
| 第 07/08/09 箭 | 0.1 | 0（来源 `(int)0.5`） | `bowshoot1`，0.3 |
| 原版光灵箭（使用第 10 箭实体） | 0.1 | 0（同上） | 原版箭发射，1 |

技能箭为静音弹体，拾取许可与来源一致，具体命中和物品掉落由 skill 消费。普通箭保持原版注册类型；21.1.265 的 `Arrow` 构造器从发射武器快照计算穿透，命中从该快照读取击退。因此只用原版弓的快照携带冲击 1 和按 Ctrl 决定的穿透 5，不把手持弓的其他附魔带入箭。两项随原生 `weapon/PierceLevel` 存档，避免临时匿名子类重读后丢失行为。

发射后清蓄力。第 02 箭扣层数 × 5 灵力，即使由库存分支放行也照扣，可能出现负值；扣后不足下次费用则只清箭物品。其他箭没有无限时移除一件，库存不再有箭时只清箭物品。轮盘序号不重置；普通箭不消费满蓄力标记，技能箭按其原有飞行过程消费。

独立算例：层数 5、潜行、快速装填 2 时，速弓每刻 +6、长弓 +3；物攻 9、Alt、长弓满蓄力且轻羽 2 时，普通箭基础伤害 4.5、速度 7。层数 5、灵力 20 但库存有灵气箭时，发射扣 25 后为 −5，箭物品选择清空而序号 3 保留。

### 拂尘与生命周期

挥动时 `fuchenshiyongtick < 1` 且灵力至少为层数 × 2 + 2，才生成速度 0.5、基础伤害 `(float)0.1`、击退 0、穿透 10 的静音 `projectile_fuchen`；扣灵力、设冷却 20、实际挥动物品损耗一点耐久。冷却仅在选中九把拂尘之一时每刻减一，切走暂停。目标 `yihossu` 对应的来源标签没有这九个成员，不用空标签替代实际入口绑定。

第二至第九把的工具等级恒为 1、任意方块挖掘速度为 2，能采掘斧/锄/镐/锹标签中的方块但不能采掘要求铁或钻石等级者；保留五类默认工具能力。主手伤害修饰为 0、攻速修饰 −3.5；挖掘损耗 1，近战损耗 2（来源的耐久调用传入受击者）。耐久值从内容表取得，不另存第二份定义。

弹体在 `super.tick` 后推进：有发射者才关闭重力，每刻广播 `lingqi` 四枚、三轴散布各 1；在以自身为中心、半边长 2 的 AABB 中按距离排序，敌友由发射者判定。每个对象只在没有 `uid<UUID>` 标记时执行一次 `shanghai 无 <发射者UUID> false 2 1`，然后标记；无伤害能力的对象仍记标记。第十刻移除并清计数，插地也移除。命中生物后无条件减一插箭数，保留来源调用顺序。弹体自身不可见、没有实际可拾取物品；内部保留非空箭物品以满足目标存档格式。

隐藏 `fuchen` 物品右键开始使用、无动作、时长 72000 刻，松开立即发射速度 2、基础伤害 0.1、无穿透的同类弹体，禁止拾取；物品无耐久，不扣库存或灵力。来源发射辅助查询空声音 ID，没有可迁移的有效声音，不给它补造音效。

玩家持久字段沿用 xiulian 的逐键公开操作，箭选择归 skill；服务端持有玩法真值。选中推进在玩家 Post tick 处理，挥动复用 `ItemSwingEvent`，实体通过 equipment 内容表登记，渲染只在本系统客户端入口装配。弹体计数和逐对象命中标记随实体持久数据保存，穿透沿用原生存档键。

### 验证范围

静态来源与目标 21.1.265 源码已核对；第二至第九把拂尘归一化后，除了名称与耐久值外源码一致。`build compileSmokeJava`（含 ArchUnit）通过；仅一次 `runSmokeClient -PsmokeOnly=equipmentweapon`，五项全部符合并 `DONE`，客户端正常保存退出，ERROR 0。验证覆盖挥动与重复挥动、扣费耐久、切走暂停及选中恢复、隐藏物品松开、范围敌友与一次命中、十刻寿命、自然右键空过程，以及人工标记下的两种蓄力、普通箭发射/存档与灵气箭库存放行后扣到负值。

专用服务器一次启动到 Done，原生 `stop` 后所有维度正常保存退出，ERROR 0；实体导出 68 个，包含 `projectile_fuchen`。

证据：`tmp/equipment-server.log`、`tmp/equipment-build.log`、`tmp/equipment-smoke.log`、`tmp/equipment-report.txt`、`tmp/equipment-family-source.txt`。1.18.2 实机、两版本对照、多人、完整声音及视觉验收未执行；人工标记不代表来源自然右键入口可用。妖化见下节；品阶、饰品飞行减耗与其他装备功能留待后续批次。

## 覆火葫芦（2026-11-08）

### 来源行为与数据

来源入口为 `Fabao010DangYouJianDianJiKongQiShiShiTiDeWeiZhiProcedure` 的物品右键过程。普通右键按 `fire10num` 到 `fire1num` 逆序选择最高可用等级；十级至二级分别消耗 `256/128/64/32/16/8/4/2/1` 灵力，一级不消耗，高等级不足时回退低等级；没有火焰提示“空”，有火焰但全部不足提示“您的灵力不足！”。发射速度为 `0.6`、散布参数为 `10`、基础伤害为 `0.1`，二级至十级调用对应材料实体，一级调用普通火焰实体。

潜行右键以视线前方五格的方块为中心扫描 `-4` 至 `3` 的三个坐标偏移，共 `8×8×8` 个位置。普通火与灵魂火为一级；`cailiao_221`–`229`、`231`–`239` 对应二级至十级。法宝等级门槛为 `1/6/11/16/21/26/31/36/41`，吸收后将方块清为空气并增加对应火焰数量。目标用类型化 `FabaoFireData` 组件保存十个数量，服务端是唯一真值。

### 目标实现

`Fabao010Item` 接收普通与潜行右键，调用 yihuo 的 `absorbFire`、`fabaoAbsorptionGrade` 和 `shootMaterial` 公开接口；不直接修改异火持久对象。`Fabao010huoyan` 为 `AbstractArrow` 弹体，保持无重力、火焰粒子、十刻寿命、插地移除、命中方块上方放置普通火和来源命中后的 `ManaAttackDamage` 等字段写入。隐藏 `fabao_010huoyan` 物品松开发射同一弹体，速度 `2`、散布 `0`、禁止拾取并扣一点耐久。两个物品使用专用工厂，弹体登记为 `projectile_fabao_010huoyan`。

### 验证范围

静态核对依据为上述 1.18.2 版本过程；目标 API 按 NeoForge 21.1.265 源码改写。`gradlew build`、`check_docs.py`、`check_ledger.py` 与 `git diff --check` 通过。沿用现有 `runSmokeClient -PsmokeOnly=equipmentweapon`，五项既有弓与拂尘检查全部 `DONE`，客户端正常保存退出，`run/logs/latest.log` 未见 `ERROR/FATAL`。该 smoke 只证明新增物品、实体注册和客户端装配未阻塞启动，不替代覆火葫芦吸收、发射、命中、组件存档和视觉的专项验收。未执行 1.18.2 实机、两版本对照、多人及完整视觉验收。

## 妖化（2026-11-07）

### 来源与入口

已静态核对 `A:Jinengyaohua`、`Yaohua`、`Yaohuabuwei0/1/2`、`Yaohuabuwe3`、`Yaohua0101TouKuiShiJianMeiYouXiKe`、`Yaohuajieshu`、`Weaponshuxing` 以及 `Yaohua0101Item/Model/ArmorRenderer`。技能菜单第19号按钮经 `DemonTransformationEvent` 请求本系统，equipment 依赖 skill 的公开事件；防具、物品实例与变身生命周期归 equipment。`PlayerTick` 的妖化冷却已由 xiulian 每秒递减，本批不新增第二个计时器。

五个体质标记 `tizhiopen_203/106/107/108/109` 有任一个即可通过第一条件；虽然提示写金丹期以上，源码没有额外境界门槛。头盔不在 `yaohuaarmor` 且冷却不大于0时，先播放妖化声音再检查空位，否则显示冷却并关闭菜单。体质不符只提示，菜单保留。空位不足同样关闭菜单，但不写冷却。

Q58：来源用完整玩家物品能力计数，包含主背包、护甲及副手，共41格，不能把提示的“四个背包空位”改成只查主背包。成功时先按靴子至头盔的顺序复制原甲并按 `giveItemToPlayer` 放入背包，再清空原部位，最后按头盔至靴子穿上新甲。背包塞不下时沿原生发物品逻辑掉落；结束不自动穿回。结束回调也不检查槽内是否仍为原件，会清空当时槽内物品。影响限于来源变身路径，评估轻微，保留实际行为，不补库存规则或装备恢复。

### 数值、保存与消费

`EquipmentStats` 组件保存六项取整数值和三项倍率，来源 `fabaotype` 由实际护甲部位表示。修正值按109、108、107、106顺序分别取0.5、0.2、0、−0.5，仅203时取0；使用实际 `LEVELnumber`，不加境界修正。

| 项 | 四部位共用公式，L为层数、j为修正 |
| --- | --- |
| 生命 | `round(L × 0.7 × (0.95 + j))` |
| 灵力 | `round((L × 0.55 + 0.4) × (1.05 + j))` |
| 神识 | `round((L × 0.55 + 0.4) × (1.15 + j))` |
| 物理、法术防御 | `round((L × 0.42 + 1.2) × (0.95 + j))` |
| 神识防御 | `round((L × 0.42 + 1.2) × (1 + j))` |
| 击退抗性、攻速 | `(L × 0.1 + 0.4) × 系数 / 100`；胸甲系数均为j，其余为0.85、0.7 |
| 移速 | `L × 1.25 × 系数 / 100`；胸甲系数为j，其余0.7 |

独立算例：L=21、109开启时，每件为生命21、灵力19、神识20、三种防御15；四件生命加成84。头盔抗性0.02125、攻速0.0175、移速0.18375，胸甲对应0.0125、0.0125、0.13125。舍入先调用 `Math.round`，再按来源转int；不改变负值的舍入。

六项属性提交 attribute 加成层；每刻比较实际贡献，变化时才提交，覆盖同种物品组件变化及重生后的重新提交。combat 经 `ArmorStatEvent` 查询来源所需防具数值；来源法术护甲项实际读 `mana_Att`，妖化未写入该项，保持0。Q17 已裁定生命进入实际属性，只保留击退抗性、攻速和移速的乘基础值原版修饰符，不再加另一份最大生命。其他装备的完整数值结构及生产者仍待后续迁移。

四件防具附绑定与消失诅咒，各部位原生防御为头2、胸3、腿2、靴1，耐久为275、400、375、325，附魔能力0、无修复材料和有效装备声音，隐藏于创造栏。内容与语言由来源转换，来源几何、动画和纹理经资源清单迁移；客户端使用 GeckoLib 4.9.3 的原生护甲渲染，默认八骨骼绑定与半透明渲染，循环 `animation.yaohua_0101.idle`。仅客户端装配渲染提供器。

头盔 `DemonTimer` 组件保存刻数、秒数与结束标记：佩戴时每刻加1，20刻增加1秒、清刻数，并在不超过30秒时显示剩余秒数；不足30秒时施加2刻无可见粒子的隐身。卸下暂停，物品存盘重读保留计时。到30秒只启动一次消散：立即清靴子，5/10/15刻清护腿、胸甲和头盔，分别在玩家当前Y+4/8/12/16广播 `brokencrimson`。回调捕获原世界与玩家，延迟不写存盘，停服丢弃，保持来源。

验证：首次客户端在内容表严格校验处拒绝未声明的 `animated` 字段，未进入玩法；已补齐 `ArmorMaterialRow` 的字段契约，并让动画材质不建立原版两层纹理。修正后 `build compileSmokeJava`（含 ArchUnit）通过，仅一次进入世界的 `runSmokeClient -PsmokeOnly=demon`，四项主路径全部符合并 `DONE`：体质及满库存拒绝、真实按钮网络路径/旧甲搬移/独立算例/生命不重复/移速生效、物品保存重读/卸下暂停后恢复/双诅咒，以及到期清槽/旧甲仍在背包/贡献撤除。客户端 ERROR 0，所有维度正常保存退出；未重跑旧神通。检查截图文件名补上扩展名后只重新编译，未重跑。

专用服务器一次启动到 Done，原生 `stop` 正常保存全部世界并退出，ERROR 0；物品导出2068个，含四件妖化护甲。截图已查看，护甲架上的妖化模型可见，没有缺失纹理；不代表完整动画、玩家隐身层、声音或视觉认可。1.18.2版本实机、两版本对照、多人和死亡/重登场景未执行。

证据：`tmp/demon-build.log`、`tmp/demon-smoke-startup-failure.log`、`tmp/demon-smoke.log`、`tmp/demon-report.txt`、`tmp/demon-check-compile.log`、`tmp/demon-server.log` 与 `build/smoke/screenshots/equipment-demon-armor.png`。四件护甲标为“行为已迁移”，整个 equipment 与 skill 仍未完成。

## 宝箱成品与品阶（2026-11-07）

已静态读取 `B:LianqiCommand`、`BaoxiangChengpin`、`Lore` 的装备分支，及 `A:Shuxingjisuan/2/Zong`、`Weaponshuxing`、`PlayerRightClick`、`DamageIntegration`、`CanjianAnXiaAnJianShi`。入口协调归 treasure，实例与属性归 equipment，不能因 `/lianqi` 的名字把奖励生成并入正式炼器炉次。目标依据为 NeoForge 21.1.265 制品中的 `ItemCommands`、`ItemStack`、`ItemAttributeModifiers`、`ItemEnchantments`、`AttributeInstance`、物品能力与属性/提示事件源码。

### 参数与实例

公共六项为物品 ID、名字、说明、类型、品阶文字、品阶数值。品阶整数直接截断，经验在数值乘100后截断；例如4.6在Java double运算后经验为459，不能先取整或四舍五入。数值块对缺失或非数值输入取0；名字始终写自定义名称。

| 类型 | 后续参数 | 实例与原料 |
| --- | --- | --- |
| 武器、法宝 | 6–8物攻/法攻/神识攻击，9攻速、10移速、11抗性，12重量，13附魔数，14–22三组附魔 | 三攻击实例转int，原料各乘100后转int；三倍率保持double，原料乘2000；重量转int |
| 其他 | 6生命、7灵力、8神识、9护甲，10移速、11抗性，12–14三防，15重量，16附魔数，17–25三组附魔 | 生命/灵力/神识/护甲实例转int，原料分别乘20/5/5/5后转int；三防原料乘100后转int；两倍率原料为double乘2000 |

附魔数恰等于3才处理第三组，至少2处理第二组，至少1处理第一组，执行顺序2→1→0；大于3只处理后两组，不能改成夹到3。每组为命名空间、路径、等级；保留声明数量、实际处理的组号和原始注册名，目标附魔仍经真实注册表与原生附魔组件应用。

`EquipmentStats` 保存整数属性与倍率，`EquipmentProduct` 保存身份、品阶、说明、重量、原料数值和附魔记录，均使用类型化物品组件存盘并同步，不引入不定形物品NBT。没有本命培养或饰品数据时不补造对应字段。

### 原版属性与消费者

成品显式覆盖物品默认修饰符：武器固定攻速−2.6，攻速/移速非0时乘基础值，抗性大于0时乘基础值；防具只在护甲、移速、抗性大于0时添加对应修饰。头盔、胸甲、裤子、战靴分别作用于四槽；未知类型仅在原生修饰需要槽名时使来源临时物品命令无效，目标同样不生成成品。防具没有攻速原料。

Q17延续已批准处理：生命与物攻进入统一最终值，不再添加成品的生命/物攻原生修饰符；普通未加工工具的默认修饰不受影响。主手带 `wuqi` 或 `fabao` 标签时提交三攻击，四护甲提交原有六项；只在对应贡献变化时写回，切换、组件变化、登录或重生的新玩家对象会重算。来源法术护甲项实际读 `mana_Att`，保持该读取，不改读法防。

Q59：`BaoxiangChengpin` 写 `lianqizong.mana_Att`，而 `PlayerRightClick` 的施法耗费和 `DamageIntegration` 读取根 `mana_Att`，两者不能合并。根值的生产者主要是尚待迁移的灵兽袋、蛊罐与部分法宝实例。本批只监听 `WeaponStatEvent` 的 `lianqizong.item_level_num`，不为根法攻加恒零监听或用炼器法攻顶替；范围轻微、保持来源路径。格挡真实成品品阶8产生24刻初始计时。

提示接回来源的品阶、类型、三攻击或防具数值、重量及每24个UTF-16字符分行的说明；Q17移除原生同项修饰后，生命与物攻在该提示显示。成品的无主提示保留，本命认主与培养链仍待迁移。

### 验证范围

`build compileSmokeJava`（含ArchUnit）通过，`runSmokeClient -PsmokeOnly=equipmentproduct` 首次六项全部符合并DONE；复核补齐独立护甲整数后，再次构建及同一成品专项六项全部符合并DONE。覆盖无容器/占槽/权限拒绝、武器和附魔分支、保存重读与原料取整、真实属性不重复及倍率、品阶格挡/根法攻区分/客户端组件与提示、撤除贡献。两次客户端均ERROR0、正常保存全部世界退出，未重跑旧神通。专用服务器一次到Done、原生stop正常保存退出，ERROR0；该服务器检查在补齐护甲整数前运行，未重复启动。

证据：`tmp/equipment-product-{build,smoke,server}.log`、`tmp/equipment-product-final-{build,smoke}.log`、`tmp/equipment-product-{initial-report,report}.txt`。本批未增加注册物品，更新的是两个奖励命令/过程的归属证据；原内容台账状态不因此升级。未执行1.18.2实机、两版本对照、多人、玩家退出重进和完整视觉；宝箱奖励总表、炼器炉次、根法攻生产者与饰品自然入口仍待迁移。

## 四饰品槽与飞行减耗（2026-11-07）

### 来源、导航与槽位生命周期

已静态读取 `A:PlayerGUI03` 的菜单、界面与按钮消息、开启/关闭过程、`Shipintick`，以及 `B:ShipinCommand/ShipinGo`；同时核对 `PlayerGUI01`、K 键入口、`OpenGUI01/03`、显示过程、`Playerhuifu` 与两份玩家能力的复制规则。目标核对 NeoForge 21.1.265 的 `IMenuTypeExtension`、`IPlayerExtension.openMenu`、`ServerPlayer.doCloseContainer`、原生按钮消息、`Slot/SlotItemHandler` 及 `AbstractContainerMenu.moveItemStackTo` 源码。

K 键与记住分页归 panel，四槽菜单 `player_gui_03`、物品与关闭保存归 equipment。菜单在玩家 `(x,y+1,z)` 打开并传入实际方块坐标；有方块物品能力时绑定该能力，否则使用四槽临时容器。菜单共有四个饰品槽和36个背包槽。四类共36件来源饰品的物品构造均为 `stacksTo(1)`，目标内容表均为 `stack:1`，正常取得和穿戴每件只能一件；容器槽容量不代表物品可堆叠数量。原生菜单负责物品同步与按钮身份验证。

服务端第一次 Post tick 才初始化：依保存字段顺序取原物品对象，直接把数量设为1，再交给菜单。这里保留来源的共享引用：原位数量修改可被保存字段读到；换成另一对象时，直到关闭菜单才把四槽新引用写回。公开查询一律给副本，其他系统不能绕过装备所有者修改真值。四槽附件使用 Codec 随玩家存盘、全部克隆事件复制并只同步本人；菜单槽位单独走原生同步。关闭不返还或掉落四槽物品。绑定方块容器时，首次初始化会覆盖其前四槽，此后共享同一能力，按来源保留。

Q60：槽0/1/2/3分别要求 `headornaments/necklaceornaments/handornaments/beltornaments`，但保存字段顺序为头饰、项坠、腰佩、手饰；来源说明却称第三格为腰佩。保留标签、显示文字和保存字段三者的实际关系，重开不调换物品。来源首刻的数量归1也保留。影响限于饰品页，评估轻微，已登记调查表；没有自行交换槽位或补物品标签。Shift 搬移保留来源的标签检查，包括向已有堆叠合并时的 `mayPlace`。

### 根字段生产与属性消费

`/shipin` 无权限门槛，按空白拆参，返回0；物品 ID 转小写并统一原初系统命名空间。前六项为物品、名字、说明、类型、品阶文字、品阶整数，名字/说明为“无”时不写对应值。之后最多三组“键、类型、值”，前一组键为空时停止；`str/char` 写字符串，`int` 截断，`double` 保留小数，其他组解释为附魔的命名空间、路径与等级。先写固定字段，再顺序写动态键，所以动态键能覆盖固定字段；`EquipmentProperties` 保留未知键、三种类型与覆盖结果。数值转换失败取0是来源输入规则。物品在命令坐标加 `(0.5,0.8,0.5)` 掉落，拾取延迟10刻。

每个已保存饰品槽计一次根字段贡献，不乘堆叠数量：生命/灵力/神识上限与恢复、三防共九项；四槽 `colddown` 每级提供0.01冷却缩减。四件护甲的 `dawn/powerup/attdef/manadef` 分别增加生命恢复、灵力恢复、物防、法防，`colddown` 同样每级0.01。与既有炼器贡献一起提交 attribute 的加成层，实际贡献变化时才写回。根 `mana_Att` 的真实 `/shipin` 生产者已接入 `WeaponStatEvent`，延续 Q59 与嵌套 `lianqizong.mana_Att` 分离；其他根值生产者仍随所属系统迁移。未为根字段新增来源没有的装备 Tooltip。

`FlightEnchantmentEvent` 汇总已保存四槽的 `flying` 等级，复用 skill 的原消耗公式；槽内替换尚未关闭时不提前把新物品计入。腰佩保存字段为 `fuqi_01` 时，按世界日时间每10刻在玩家Y+1广播2个 `fuqi_0101` 粒子，时间倒退重置计时，`shipintick` 仍由 xiulian 保管。该物品不在腰佩标签中，未补入标签。

### 验证范围

`build compileSmokeJava`（含 ArchUnit）通过；仅一次 `runSmokeClient -PsmokeOnly=accessories`。原报告记录12项符合，随后“重新打开存档超时”并以 `DONE` 结束，重登后两项未执行。负责人指出截图堆叠问题后复核发现：检查代码曾手动 `setCount(3)` 伪造超过饰品上限的堆叠，并把它用于搬移与保存断言；这些结果不能证明合法饰品的自然穿戴路径，相关通过结论撤回。36件饰品的来源构造与目标内容表已逐件静态核对，均为上限1，检查改为合法单件并核对实际物品上限；没有修改正确的物品堆叠配置。目标源码还显示重开含实验性设置的存档会进入 `BackupConfirmScreen`，已补该检查处理。两项检查代码修正后只编译，未重跑；本批合法单件的穿戴/保存实机验收仍未完成。原运行退出前全部世界正常保存，客户端 ERROR 0。

专用服务器一次到 Done，原生 `stop` 正常保存全部世界并退出，ERROR 0；菜单导出10个，包含 `player_gui_01/03`，物品数保持2068。两张旧截图只用于布局和贴图可见性，图中的数量3来自上述非法检查数据，不能代表正常饰品玩法；零层检查玩家显示未翻译的 `levelname00`，没有擅自夹到其他境界。完整视觉未认可。

证据：`tmp/accessory-{build,smoke,server}.log`、`tmp/accessory-report.txt`、`tmp/accessory-check-compile.log`、`tmp/accessory-stacking-audit.log` 与两张旧截图。GUI 当前仅部分实现：属性页和饰品页已接主路径，但两页的其他分页按钮、饰品临时队伍显示/退出未接；`GUIshipintai` 制作台亦未实现。其余页面随所属系统逐项闭合，不能留到最后统一补或宣称当前 GUI 已验收。合法单件穿戴、重登后实际操作、多人、完整视觉、1.18.2版本实机及两版本对照未完成。菜单台账仅标“已注册”并注明部分行为，不升级整套饰品物品的迁移状态。

后续合法单件验收（2026-11-07）：沿用现有 `runSmokeClient -PsmokeOnly=accessories`，未修改检查代码，14项全部通过并 `DONE`。真实 `/shipin` 生成上限一的单件，K键打开属性/饰品页，实际Shift搬移、关闭保存及本人同步、根法攻和属性提交、七级飞行减耗、头顶容器绑定、丹田往返、死亡复制、退出重登和重登后再打开均通过。两次退出均保存全部世界，客户端ERROR0，证据为 `tmp/accessory-native-{client.log,report.txt}`。本次补齐合法数据下的上述路径证据；其他分页、临时队伍、制作台、多人和两版本对照仍未完成，菜单及整套饰品物品状态不升级为完整验收。

## 开灵工具机与弹体（2026-11-08）

### 来源调用链与冻结字段

来源入口为 `PlayerRightClickProcedure.onRightClickItem` 的 `RightClickItem` 事件。开灵等级非零且主手为 `AxeItem`、`PickaxeItem`、`HoeItem` 或 `ShovelItem` 时，冒险模式被排除；服务端生成 `GongjvshechuEntity`，伤害为 `0.1`、速度为 `1`，位置为 `getEyeY() - 0.1`，并写入物品100刻冷却。`GongjvshechuTouZhiWuFeiXingTickProcedure` 在首个有效主人 tick 写入 `zhurenuuid`、`gongjvleixing`、`fumodengji` 并关闭重力，随后计数到16刻调用 `GongjvshechuDangTouSheWuSheZhongCiFangKuaiProcedure`；命中方块路径同样生成工具机并播放闪光。

目标 `KailingProjectile` 在首个服务端 tick 冻结主人主手的工具类别和开灵等级，持久化 `ticks`、`enchantLevel`、类别、初始化和生成标记。命中与16刻到期共用一次生成保护，工具机创建时写入主人 UUID、附着方向、等级、寿命上限和中心坐标，随后以 `MOB_SUMMONED` 完成 Mob 初始化。

### 四种工具机状态机

`GongjvfuDangShiTiGengXinKeShiProcedure` 的斧过程保留 `寻找 → 寻路 → 采集 → 触点`，扫描范围为 `8 × fumodengji`，命中等待11刻；目标不可达时按来源每10刻调整垂直移动，邻近叶片时进入3×3×3清理并等待20刻，无目标20刻后结束。寿命达到 `fumodengji × 2400` 时使用来源的材料粒子和火焰，斧的来源缺省寿命初始化 `fumodengji × 400` 也保留。

`GongjvgaoDangShiTiGengXinKeShiProcedure` 的镐过程保留按附着方向的首目标、四向螺旋搜索、移动、准备、挖掘和结束；每轮达到25个目标后从中心重新开始。来源以 `STONE`、`ICE`、`CLAY` 材料判定，目标用镐可采掘标签、冰标签和黏土块表达；挖掘等待5刻。锄和铲过程共用来源的土地扫描与移动时序，移动阶段始终位于目标方块上方；锄按 `TOP_SNOW`、`DIRT`、`GRASS`、`SAND` 材料集合，铲按 `DIRT`、`GRASS` 集合，完成后清除植物并把目标设为湿润度7的耕地。来源过程中斧与镐在寿命上限为0时写入 `fumodengji × 400`，锄和铲不补写该缺省值。

主人 UUID 非空时，镐、锄、铲只在主人在线时执行来源的掉落与破坏；主人为空时按来源在目标位置掉落。来源1.18.2的 `PLANT`、`REPLACEABLE_PLANT` 材质在目标用可替换方块标签与 `BushBlock` 表达，未扩展为额外植物规则。

### 验证范围

静态核对依据为 `PlayerRightClickProcedure`、`GongjvshechuEntity`、`GongjvshechuTouZhiWuFeiXingTickProcedure`、`GongjvshechuDangTouSheWuSheZhongCiFangKuaiProcedure`、四个工具机 tick 过程和 `JianceshuyeProcedure`；目标 API 按 NeoForge 21.1.265 源码改写。`gradlew build`、`check_docs.py` 与 `check_ledger.py` 已通过。沿用现有 `runSmokeClient -PsmokeOnly=equipmentweapon`，五项既有弓与拂尘检查全部符合并以 `DONE` 结束，客户端正常保存退出且未见 `ERROR/FATAL`；该检查只证明新增实体注册、客户端装配和公共启动未阻塞，不替代工具机四类自然操作的专项验收。专用服务器启动至 `Done`，原生 `stop` 后全部维度正常保存退出，ERROR 0，实体导出 91 个。未执行1.18.2实机、两版本对照、多人及完整视觉验收。

## 覆风法宝（`fabao_011`，2026-11-08）

来源入口是 `Fabao011DangYouJianDianJiKongQiShiShiTiDeWeiZhiProcedure`。普通右键先沿玩家视线五格取 `ClipContext.OUTLINE/NONE` 的方块坐标，再以该坐标为中心 `AABB.inflate(4)` 查找实体；潜行右键直接以玩家坐标为中心 `AABB.inflate(5)` 查找。两条分支都按来源顺序处理敌对实体，调用 `SpellDamage.deal` 造成 `DamageCategory.WIND`、固定基础量5、倍率1.2的法术伤害，并把水平位移设为目标与玩家坐标差的0.5倍、垂直位移设为0.5。`skilleffect_08` 只跳过击退，`effect_002` 在入口处阻止整次施放。冷却使用实际物品栈，普通100刻、潜行200刻。

目标使用 `Fabao011Item` 专用 `ContentSword` 工厂保留原有剑属性，敌友判定调用 `Relations.isFriendly`，伤害调用 combat 公开接口；没有直接读写其他系统持久化字段。

验证范围：本批未新增 Test 类、冒烟选择器或 Computer Use。`gradlew build`、`runSmokeClient -PsmokeOnly=equipmentweapon`、专用服务端启动至 `Done` 后原生 `stop`、`check_docs.py`、`check_ledger.py` 与 `git diff --check` 均通过；客户端与服务端日志未见 `ERROR/FATAL`。该 smoke 只作注册、资源加载和公共客户端启动回归，不替代风场自然操作与两版本对照验收。
