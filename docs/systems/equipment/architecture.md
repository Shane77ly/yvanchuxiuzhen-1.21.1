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

GeckoLib 盔甲（`Armor01Item`–`Armor13Item`、`Fuqi02Item`、`Clothes05Item`、`Clothes19Item`、`Yaohua0101Item`，共 65 件，`Armor10Item` 只有胸甲）不在 `ModItems`，由 `YvanchuxiuzhenModGeckoLibArmors` 用独立的注册器登记，渲染器与动画由 `ModGeckoLibArmors`、`ArmorAnimationFactory` 装配。2026-11-02 之前的登记索引漏扫了这个类，现已补入台账（装备 61 件、`festival` 4 件）。四件妖化护甲见[妖化](#妖化2026-11-07)，其余装备 57 件见[动画防具](#动画防具2026-11-08)；`Fuqi02` 四件属于 festival，不计入本批。

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

## 覆情法宝（`fabao_012`，2026-11-08）

来源入口是 `Fabao012DangYouJianDianJiKongQiShiShiTiDeWeiZhiProcedure`。普通与潜行均以视线四格的 `ClipContext.OUTLINE/NONE` 方块坐标为中心、使用 `AABB.inflate(4)` 查找实体。普通分支广播爱心和 `fenyan` 粒子，对所有范围内 `Animal` 调用 `setInLoveTime(200)`；潜行分支广播 `fenyan` 粒子，只对 `Relations.isFriendly` 为假的 `LivingEntity` 添加 `effect_032` 1200刻、等级0。入口的 `effect_002` 沉默限制和普通/潜行200/600刻冷却均保持来源。

目标使用 `Fabao012Item` 专用 `ContentSword` 工厂；状态效果与粒子分别调用 combat、particle 的公开接口，不直接写其他系统数据。

验证：本批未新增 Test 类、冒烟选择器或 Computer Use。`gradlew build`、`runSmokeClient -PsmokeOnly=equipmentweapon`、专用服务端启动至 `Done` 后原生 `stop`、`check_docs.py`、`check_ledger.py` 与 `git diff --check` 均通过；客户端与服务端日志未见 `ERROR/FATAL`。smoke 只作注册、资源加载和公共客户端启动回归，不替代普通发情、潜行幻术、冷却和沉默分支的完整自然操作验收。

## 明光镜法宝（2026-11-08）

### 来源行为与目标实现

来源入口由 `Fabao014Item` 的 `use`、`releaseUsing` 与四个绑定过程组成。右键只开始使用；松开时先检查 `effect_002`，再比较玩家当前 `power` 与 `mana_Att_display`，满足时扣除后者、消耗一点耐久并生成 `Fabao014Entity`。发射参数为视线方向、速度 `2`、散布 `0`，弹体禁止拾取；普通右键冷却 `80` 刻，潜行右键冷却 `300` 刻。

目标 `Fabao014` 使用 `AbstractArrow` 保留来源的无拾取弹体结构和空音效结果。首个更新刻读取主人主手物品 `sneak` 标签并写入弹体持久数据，随后关闭重力；每刻在弹体位置上方 `0.3` 广播两枚 `cailiao_229particle`，计数达到 `20` 时服务端移除，插入方块也移除。命中后先按 relation 判定敌对；潜行弹体对生物广播一枚 `fulutexiao_33` 并添加原版缓慢效果 `100` 刻、等级 `20`，随后对生物提交 combat 的光属性法术伤害 `10 × 1.1`。`mana_Att_display` 在目标由 attribute 的 `PlayerAttributes.value(player, Stat.MANA_ATT)` 提供，当前灵力由 xiulian 的 `Cultivation` 提供。

### 验证范围

静态依据为 1.18.2 版本五个 `Fabao014*` Java 文件；目标 API 按 NeoForge 21.1.265 源码改写。未新增 Test 类、冒烟选择器、断言或 Computer Use。`gradlew build`、`check_docs.py`、`check_ledger.py` 与 `git diff --check` 通过；沿用 `runSmokeClient -PsmokeOnly=equipmentweapon` 报告 `DONE`，客户端和专用服务端均正常保存退出，日志无 `ERROR/FATAL`。这些检查仅作注册、资源加载和启动回归；自然发射、命中、灵力扣除、冷却、视觉、1.18.2 实机、两版本对照与多人验收未执行。

## 明心镜法宝（2026-11-08）

### 来源行为与目标实现

`Fabao015Item` 只有 `SwordItem` 构造器，没有自定义使用、命中或实体过程。目标 `items.json` 的通用 `tool` 工厂通过 `ContentSword` 生成同一类剑，参数与来源逐项一致：耐久 `200`、速度 `4`、攻击加成 `-1`、等级 `1`、附魔值 `2`、攻击力 `3`、攻击速度 `-3`，修理材料为空。

### 验证范围

静态依据为 1.18.2 版本 `Fabao015Item.java`；目标依据为 `ContentTables.tool`、`ContentSword` 与 equipment 内容表。未新增代码、Test 类、冒烟选择器或 Computer Use；本批只做静态对照，未运行客户端，物品显示、1.18.2 实机和两版本对照未执行。

## 血刀法宝（2026-11-08）

### 来源行为与目标实现

来源入口由 `Fabao016Item`、`Fabao016DangShiTiBeiGongJuJiZhongShiProcedure` 和 `Fabao016DangYouJianDianJiKongQiShiShiTiDeWeiZhiProcedure` 组成。命中目标时，创造模式直接施加或叠加 `effect_004`；非创造模式跳过玩家，只对 `LEVELnumber <= 1` 的目标施加 600 刻流血。来源过程从一个未赋值的临时 `ItemStack.EMPTY` 读取 `lianqizong.item_level`，因此该值实际为默认 `0`；目标实现按来源结果保留，不改读炼器成品等级。

潜行右键按 `10 + LEVELnumber × 3` 扣除玩家生命。血量高于消耗时生成 `jingxve`，写入 `belongToMasterUUID`、`belongToName`、`playerlevelnum` 和 `playerlevel`，发送 15 个 `xvedi` 粒子，并通过 equipment 的物品发放入口交给玩家；血量不足时保留来源两条提示分支。`fabao_016` 的 80 耐久、剑属性和 `cailiao_201` 修理材料由专用物品工厂提供。

### 验证范围

静态依据为 1.18.2 版本三个 `Fabao016*` Java 文件；目标 API 按 NeoForge 21.1.265 源码改写。未新增 Test 类、冒烟选择器、断言或 Computer Use。`gradlew build`、`check_docs.py`、`check_ledger.py` 与 `git diff --check` 通过；沿用 `runSmokeClient -PsmokeOnly=equipmentweapon` 报告 `DONE`，客户端正常进入世界并保存退出。该检查仅作注册、资源加载和启动回归；血刀命中、流血、潜行采血、物品 NBT、视觉、1.18.2 实机、两版本对照与多人验收未执行。

## 赶仙鞭法宝（2026-11-08）

### 来源行为与目标实现

`Fabao022Item` 没有自定义使用或实体过程，但覆写主手属性：`ATTACK_DAMAGE` 加法修正为 `-2`，`ATTACK_SPEED` 加法修正为 `-2.4`；物品耐久为 `80`。目标 `Fabao022Item` 通过 `ItemAttributeModifiers` 与 equipment 专用工厂写入同样的主手属性，并接入共用 `AnimatedFabaoItem` 与 `AnimatedFabaoRenderer`；来源几何、动画、纹理及实际可达渲染路径见[动画资源](#文王鼓与赶仙鞭动画资源2026-11-08)。

### 验证范围

静态依据为 1.18.2 版本 `Fabao022Item.java` 及模型/渲染类；目标依据为 NeoForge 21.1.265 的属性组件 API 与 GeckoLib 4.9.3 的物品渲染 API。此前物品属性批次的 build 与既有 `equipmentweapon` smoke 已通过；本次动画资源批次的构建和运行结果另在上述动画资源节登记，不沿用此前结果宣称新资源验证通过。

## 文王鼓法宝（2026-11-08）

### 来源行为与目标实现

`Fabao021Item` 只有客户端 GeckoLib 物品模型、动画控制器和渲染钩子，没有自定义右键、命中、属性、耐久或实体过程。目标 `Fabao021Item` 通过共用 `AnimatedFabaoItem` 与 `AnimatedFabaoRenderer` 接入来源模型和 idle 动画；仍保留单件堆叠、创造栏归属和服务端无额外行为，渲染取舍与资源路径见[动画资源](#文王鼓与赶仙鞭动画资源2026-11-08)。

### 验证范围

静态依据为 1.18.2 版本 `Fabao021Item.java` 及其模型/渲染类；目标依据为 equipment 内容表与 GeckoLib 4.9.3 的物品渲染 API。本次动画资源批次的构建、运行结果与未验收范围统一登记在上述动画资源节。

## 飞行法器（2026-11-08）

### 来源行为与目标实现

按 1.18.2 版本 `FlyingitemnbtshezhiProcedure`、`Flyinginstrument0101DangYouJianDianJiKongQiShiShiTiDeWeiZhiProcedure`、`Flyinginstrument01DangYouJianDianJiShiTiProcedure`、`Flyinginstrument01DangShiTiGengXinKeShiProcedure` 与 `Flyinginstrument01DangShiTiBeiShaSiShiProcedure` 逐行接回 8 个飞行法器。物品首次右键写入 `flyitemID`、`flyname`、`flyentityID`、`health_Max`、`health`、`power_Max`、`power`、`speed_num`，召唤对应实体、复制玩家关系、设置生命并清空手持槽；已有字段的物品保留当前生命和灵力。

目标使用 `FlyingInstrumentItem` 专用工厂和 `FlyingInstrumentEntity` 共享实体，字段保存在物品 `CUSTOM_DATA` 与实体持久数据中。实体潜行右键按来源保存当前字段并取回物品；普通右键按 `lingshi` 标签和 `cailiao_191`–`199` 的 `1/4/16/64/256/1024/4096/16384/65536` 灵力值充能，手持 `hammer` 恢复 1 点生命，否则玩家骑乘。实体刻更新保留水中上浮、空气下落、骑乘能量门槛、`flying` 按视线移动、法器灵力优先扣除及 `jinkong` 复位；来源实体的药水、区域效果云、摔落、仙人掌和溺水免疫、20刻死亡移除、乘客高度偏移均已接回。

8 个实体的 GeckoLib 几何、动画与实体纹理已从 1.18.2 版本资源接入 `assets/yvanchuxiuzhen/geo`、`animations` 与 `textures/entities`，并通过装备实体模型表与现有 `AnimatedEntityRenderer` 注册。01/02/03 的乘客偏移为 `-0.2`，05 为 `-0.7`，06 为 `0.1`，07 为 `-0.3`，04/08 沿用原生偏移。

### 验证范围

静态依据为上述 1.18.2 版本过程与 8 个实体类；目标 API 按 NeoForge 21.1.265 源码改写。未新增 Test 类、冒烟选择器、断言或 Computer Use。首次专用服务端启动发现 8 个实体已写入模型表但遗漏 `entities.json`，导致属性事件取不到注册句柄；补齐原来源尺寸与 `MONSTER` 类别后，`gradlew build`、专用服务端启动至 `Done`、原生 `stop`、`check_docs.py`、`check_ledger.py` 与 `git diff --check` 均通过。服务端导出 101 个实体，含 8 个飞行法器；内置 `runSmokeClient -PsmokeOnly=equipmentweapon` 的五项既有弓与拂尘检查全部 `DONE`，客户端正常进入世界并保存退出，客户端与服务端日志未见 `ERROR/FATAL`。这些检查只用于注册、资源加载和公共启动回归，不替代飞行法器召唤、骑乘、充能、取回、死亡掉落、视觉、1.18.2 实机、两版本对照和多人专项验收。

## 本命培养（2026-11-08）

### 来源行为与目标实现

来源入口为 `DantianbenmingfabaoProcedure`，由修炼每秒推进调用；只有境界达到结丹期（`LEVELnumber >= 21`）且本命槽有物品时推进。经验增量为器灵附魔等级加体质中的通用、短兵器、长兵器、远程、道家、佛家和儒家培养速度。经验达到当前上限时只提升一级，上限重算为 `2000 + 等级 × 2000`；每次培养消耗一点耐久。升级按玩家体质标志交替增加本命器物的法术攻击和精神攻击，并同步回本命槽与丹田界面进度。

目标用 `BenmingData` 物品组件保存等级、经验和经验上限，用 `EquipmentStats` 的法术攻击/精神攻击字段承载升级结果。`BenmingCultivation` 监听已有 `CultivationSecondEvent`，通过 `Cultivation` 公开操作读取境界与体质键、提交本命物品和 `BenmingProgress`；装备属性层按主手物品消费两项攻击值。已有组件但经验上限不大于 0 时先按来源公式初始化，再计算本次经验。

本命槽的物品接受条件、打开/关闭和显示标志仍由 xiulian 的 `DantianActions` 持有；equipment 不直接修改修炼附件内部对象。

### 验证范围

静态依据为 1.18.2 版本 `DantianbenmingfabaoProcedure`、`BenmingfabaotishengProcedure`、`DantianbenmingexpProcedure` 与 `DantianbenmingxianshiProcedure`；目标 API 按 NeoForge 21.1.265 源码核对。未新增 Test 类、冒烟选择器、断言或 Computer Use。使用项目自带 `runSmokeClient -PsmokeOnly=equipmentweapon` 做注册、资源加载和公共客户端启动回归，既有弓与拂尘五项报告 `DONE`，客户端正常保存退出且日志无 `ERROR/FATAL`；该 smoke 不替代本命培养的境界门槛、经验升级、耐久消耗、属性交替、存档重载和完整界面专项验收。完整行为专项、1.18.2 实机、两版本对照、多人与完整视觉仍未执行。

## 投掷法宝（`fabao_001`–`008`，2026-11-08）

### 来源入口与逐类规则

来源为 `A:Fabao001Item`–`Fabao008Item`、对应实体类，以及 `Fabao00*KeYiShiYongYuanChengWuPinDeTiaoJian`、`TouZhiWuFeiXingTick`、`DangTouZhiWuJiZhongShiTiShi` 和 `DangTouSheWuSheZhongCiFangKuai` 过程。八种物品均可堆叠64件，右键开始使用且动画为 `NONE`。`001`–`003` 在 `releaseUsing` 发射；`004`–`008` 在 `onUsingTick` 发射后停止使用。弹药先查持有物，再查背包同类物品；非创造模式成功发射后消耗一件。`001/002` 主手攻击力加3、攻击速度减2.4，`008` 主手攻击力加2、攻击速度减2.4。

| ID | 冷却（刻） | 速度 / 基础箭伤害 / 击退 | 额外来源行为 |
| --- | ---: | --- | --- |
| `fabao_001` | 40 | `2 / 0.1 / 1` | 炼器物攻；各10%概率添加100刻 `effect_001`、`effect_002`；实体或方块命中返回物品 |
| `fabao_002` | 40 | `2 / 0.1 / 1` | 各20%概率添加100刻 `effect_001`、`effect_002`，10%概率物攻乘1.5；实体或方块命中返回物品 |
| `fabao_003` | 40 | `2 / 0.1 / 0` | 命中建立半径5的毒域；按首个匹配毒药施加1200刻效果；不返回物品 |
| `fabao_004` | 无 | `3 / 4 / 1` | 无使用条件过程；敌对生物命中5%概率施加1200刻 `effect_007`；不返回物品 |
| `fabao_005` | 10 | `4 / 0.1 / 0` | 炼器物攻、10%概率1200刻 `effect_004`、九种毒药；实体或方块命中返回物品 |
| `fabao_006` | 20 | `4 / 0.1 / 0` | 已初始化分支为10%概率物攻乘1.5，否则1倍；未初始化分支先以 `falling_block` 伤害源提交基础物攻，再以 `generic` 提交10%概率1.5倍、否则1倍；九种毒药；实体或方块命中返回物品 |
| `fabao_007` | 30 | `4 / 0.1 / 0` | 炼器物攻与九种毒药；实体或方块命中返回物品 |
| `fabao_008` | 40 | `4 / 0.1 / 0` | 炼器物攻、10%概率1200刻 `effect_003`、九种毒药；实体或方块命中返回物品 |

使用条件过程把本次手持栈原引用存入玩家临时 `feibiaolinshi`；弹体第一个有效主人刻才冻结 `lianqizong`、名称与毒药。`001/002/003` 读取同一临时栈；`003` 依荧光、蜂毒、火毒、赤炼毒、孢子、青藤毒、寒毒、烙毒、蛇纹毒的 `else-if` 顺序只保存首种，`005`–`008` 独立保存九种。九毒依次对应发光、`duyao_10/16/13/17/11/12/14/15`，持续1200刻，效果等级为附魔等级减一。

返回物品必须按实体命中、方块命中及初始化状态分别处理。`001/002/005`–`008` 已初始化的实体命中只重建炼器数据与名称；`005`–`008` 的该分支丢失所有附魔，来源并未把冻结的九毒写回。方块命中才重新添加九毒，并在炼器数据写 `tebiefabao=true`。未初始化的实体命中从被击目标的 `feibiaolinshi` 复制 NBT，普通 Mob 的能力默认栈为空；方块命中则从主人临时栈复制。目标保持这些实际分支，不把被击目标改读主人，也不为实体命中补回附魔。`006` 未初始化分支两次 `hurt` 使用不同伤害源，不能合并成一个总量。

`003` 的实体与方块命中各注册十次服务端 END tick 延迟，等待0、10至90刻；第一个脉冲在注册后的首次 END tick 推进。实体命中传入弹体坐标，方块命中传入方块坐标；两个过程内部都以 `x+0.5/y/z+0.5` 为中心，每次在中心上方一格广播逆向传送门与落蜜粒子，并对敌对生物施加单种毒药。来源实体在 `super.tick` 与飞行过程之后入地移除；延迟过程独立于弹体后续存活。

### 目标实现与静态复核

目标 `FabaoProjectileItem` 提供八种专用物品工厂，`FabaoProjectile` 提供八个 `EntityType` 的共用实现与逐类分派；注册表、物品模型与客户端渲染装配由 equipment 持有。`EquipmentData.FABAO_TEMP` 用 `FabaoTempData` 承载来源 `feibiaolinshi` 的物品引用与耗尽标记，保存、本人同步和死亡复制也由本系统持有。敌友查询调用 `Relations.isFriendly`，附魔和状态效果调用 combat 公开入口；没有直接修改其他系统玩家持久数据。`003` 的十次毒域脉冲用独立服务端 Post 延迟调度保留来源时序，不依赖箭实体的入地存活，也不建立通用延迟框架。

最后一件弹药按两个版本的空栈读取边界处理：1.18.2 版本数量归零后标签仍可读，附魔查询则返回0；没有自定义名时通过空栈物品读取 AIR 名称。1.21.1 版本的组件查询会隐藏零数量栈，因此仅当本次消耗使用栈最后一件时保存单件组件并置 `consumed=true`，首刻仍保留炼器数据和自定义名称、默认名取 AIR、九毒取0；未初始化回收仍按来源复制全部物品组件，可以恢复其中附魔。该桥接保留消耗前的原引用语义，不提前冻结弹体数据。

`001`–`004` 使用来源的物品弹体渲染；`005`–`008` 由 `FabaoProjectileModels` 的四个原生 `LayerDefinition` 与 `FabaoProjectileRenderer` 承载来源专用模型。四个几何层经静态归一比较与来源一致，方块数分别为3、12、5、8；四张 `textures/entities/fabao00501.png`–`fabao00801.png` 原字节复制且 SHA-256 一致，已登记 `system_assets.csv` 与 `manifest.csv`。渲染保留先偏航后俯仰、`entityCutout` 和来源透明度0.0625，对应目标 ARGB `0x0FFFFFFF`；这些是静态资源与 API 核对证据，完整视觉验收未执行。

本批按上列来源事实接回完整炼器数据、九毒及单毒顺序、各类命中和回收分支、`003` 毒域时序与中心、`006` 两次伤害、`008` 冷却和 `005`–`008` 专用模型。八行物品和八行弹体登记“行为已迁移”；逐类自然行为、存档与视觉验收仍保留在未执行范围。

### 验证范围

静态依据为上述八个物品类、八个实体类和完整绑定过程；已核对使用入口、发射参数、临时字段、毒药顺序、命中、返回物品和毒域延迟。未新增或修改 Test 类、冒烟选择器、检查断言，也未使用 Computer Use。最终 `gradlew build` 通过，包含既有 test/check 与 ArchUnit；证据为 `tmp/fabao-final-build.log`。文档检查88篇、台账4939行和差异空白检查均0项问题。

最终运行内置 `runSmokeClient -PsmokeOnly=equipmentweapon`，`EquipmentWeaponCheck` 五项既有弓与拂尘检查全为 `INFO`、`0 FAIL`、`DONE`；客户端正常保存 overworld、nether、end、xvkong 四维度后退出。专用服务器一次启动至 `Done`，导出109个实体，包含本批八种法宝弹体；Gradle 未转发标准输入，停服由现有工具调用原生 `MinecraftServer.halt(false)`，四维度正常保存并退出，未把键入但未转发的 `stop` 算作生效命令。客户端与专服日志均为 `ERROR/FATAL=0`，证据为 `tmp/fabao-smoke.log`、`tmp/fabao-report.txt`、`tmp/fabao-client-latest.log`、`tmp/fabao-server-latest.log` 与 `build/registry-export/entity_type.txt`。

上述运行结果只作注册、资源加载和公共启动回归；本批逐类自然发射、实体/方块命中、冷却、回收字段、临时附件与弹体存档重载/死亡复制、完整视觉、1.18.2 实机、两版本对照与多人验收未执行。

## 动画防具（2026-11-08）

### 来源范围与材料

本批来源为 `A:YvanchuxiuzhenModGeckoLibArmors` 登记的 `armor_01`–`armor_09`、`armor_11`–`armor_13` 各四件，`armor_10_chestplate` 一件，以及 `clothes_05`、`clothes_19` 各四件，共57件；范围逐行对照本系统台账与 `ownership_assignment.csv`，不按来源类索引推导归属。这15族的 `onArmorTick` 均为空。`Businessman11` 出生穿戴四件 `clothes_19`、`Guancai01` 给生成生物穿戴四件 `clothes_05` 等外部入口按各自所属系统接续，不能用本批物品注册代替获取和穿戴入口验收。`Fuqi02` 四件及其胸甲计时、粒子与发奖归festival，留待该内容族实施。

以下防御数组与耐久基数均按靴子、护腿、胸甲、头盔顺序；每槽耐久为 `13/15/16/11 × 耐久倍率`。所有族的击退抗性为0、来源装备声音为空注册查询；空修理材料以“无”表示。`armor_10` 只登记胸甲，其他三个数组值仅是来源材料定义，不新增对应物品。

| 族 | 耐久倍率 | 防御 | 附魔值 | 韧性 | 修理材料 | 循环动画 |
| --- | ---: | --- | ---: | ---: | --- | --- |
| `armor_01` | 15 | `2/4/5/2` | 9 | 0 | 无 | `animation.armor01.idle` |
| `armor_02` | 20 | `2/3/5/2` | 9 | 0 | 无 | `animation.armor02.idle` |
| `armor_03` | 25 | `2/3/5/2` | 9 | 0 | 无 | `animation.armor03.idle` |
| `armor_04` | 30 | `2/3/5/2` | 9 | 0 | 无 | `animation.armor04.idle` |
| `armor_05` | 35 | `2/3/5/2` | 9 | 0 | 无 | `animation.armor05.idle` |
| `armor_06` | 40 | `2/3/5/2` | 9 | 0 | 无 | `animation.armor06.idle` |
| `armor_07` | 45 | `2/3/5/2` | 9 | 0 | 无 | `animation.armor07.idle` |
| `armor_08` | 50 | `2/3/5/2` | 9 | 0 | 无 | `animation.armor08.idle` |
| `armor_09` | 55 | `2/3/5/2` | 9 | 0 | 无 | `animation.armor09.idle` |
| `armor_10` | 20 | `2/5/8/2` | 25 | 0.2 | `cailiao_171` | `animation.armor10.idle` |
| `armor_11` | 25 | `4/6/8/4` | 9 | 0 | 无 | `animation.Armor11.idle` |
| `armor_12` | 25 | `1/2/3/1` | 30 | 2 | 无 | `animation.Armor12.idle` |
| `armor_13` | 25 | `1/4/5/1` | 30 | 2 | 无 | `animation.Armor13.idle` |
| `clothes_05` | 3 | `1/2/2/1` | 10 | 0 | `cailiao_171` | `animation.clothes05.idle` |
| `clothes_19` | 5 | `1/3/4/1` | 15 | 0 | 无 | `animation.clothes17.idle` |

### 动画、资源与实际触发

来源各族包含过渡5刻的 idle 和 procedure 控制器；idle 在过程名为 `empty` 时循环。procedure 只在过程名非 `empty` 且控制器为 `STOPPED` 时设置显式 `PLAY_ONCE`，设置后仍为 `STOPPED` 才清空过程名并复位重载，按来源顺序保留。15份动画文件均只定义表中一个 idle 动画。`ArmorAnimationFactory` 是真实的玩家 END tick 事件消费者：读取护甲物品非空 `geckoAnim` 字符串、双端清空并仅客户端写入过程名。两个1.18.2工程源码中未找到非空写入者，这只限制内部触发路径的结论，已有非空物品实例输入与过程控制器仍须保留，不据此删除事件入口。`Armor11`–`13` 的动画名保留大小写。

来源渲染器均使用 `entityTranslucent`。`armor_12`、`armor_13`、`clothes_19` 绑定 `armorHead/armorBody/armorRightArm/armorLeftArm/armorRightLeg/armorLeftLeg/armorRightBoot/armorLeftBoot`；其他族绑定 `head/body/rightarm/leftarm/rightleg/leftleg/rightboot/leftboot`。`clothes_05` 使用 `clothes05` 的几何、动画和纹理；`clothes_19` 的几何和动画指向 `clothes17`，纹理独立指向 `clothes19.png`。资源按模型类实际路径接入，不按物品编号推导。

### 目标实现

目标以 `EquipmentGeoArmorItem` 承载57件物品，由 `EquipmentArmorVisual` 读取 `content/equipment/armor_models.json` 的15组表现定义，`EquipmentGeoArmorRenderer` 按各组八个骨骼名装配原生人形姿态。`EquipmentSetup` 从内容表保留部位、材料与耐久倍率，客户端装配注入 `GeoRenderProvider`。`extract_equipment_armors.py` 已输出57件物品、15族材料与表现、名称和159项资源，并登记资源清单、工厂覆写及移除这57条旧跳过记录。

重新运行数据转换后，恢复来源 `armor/toukui/xiongjia/kuzi/zhanxve` 五个总类和槽位标签的现有成员；护甲总标签本批新增45件，包括 `armor_01`–`armor_11` 的41件与 `clothes_05` 四件，各部位进入对应槽位标签。来源未包含 `armor_12`、`armor_13` 和 `clothes_19`，不自行补入。本批没有生成新的防具配方。

本批15族没有额外穿戴玩法过程。`EquipmentAnimations` 在 `PlayerTickEvent.Post` 按头、胸、腿、脚顺序消费动画护甲 `CUSTOM_DATA.geckoAnim`，双侧清空非空请求，仅客户端把名字提交给对应物品单例的过程槽；已有四件妖化护甲也接入同一入口与过渡5刻的两控制器。目标使用显式 `Animation.LoopType.PLAY_ONCE`，避免 `thenPlay` 沿动画JSON的默认循环模式。动画请求只读写本系统物品数据，没有跨系统玩家字段写入。

NeoForge21.1.265的 `ArmorItem` 保留四槽耐久基数。GeckoLib4.9.3的 `GeoArmorRenderer` 提供八个部位getter、按槽可见性和原生姿态变换；逐项对照3.0.57的 `fitToBiped`，对应部位偏移一致，几何不重写坐标。这些是精确版本静态API核对。

57件物品台账登记“行为已迁移”，真实 `geckoAnim` 事件消费和过程控制器已按来源补回。构建、资源转换、公共客户端与专用服务端结果及未验收范围统一见[本批验证](#验证边界)；这些结果不代表57件新防具的自然穿戴或完整视觉验收。

## 文王鼓与赶仙鞭动画资源（2026-11-08）

### 来源有效入口与目标实现

来源两个物品的 idle 控制器过渡为0，在曾获得显示上下文且 `animationprocedure` 为 `empty` 时循环。目标由 `AnimatedFabaoItem` 提供 `GeoItem` 缓存和 `idleController`，由 `AnimatedFabaoRenderer.renderByItem` 在完成首次渲染后开启 idle；`GeoRenderProvider` 仅由客户端装配。渲染器保留来源 `entityTranslucent`，持物和 GUI 变换沿用来源 `displaysettings/fabao021`、`displaysettings/fabao022` 父模型的 `display` 参数。

| 物品 | 几何 | 动画文件 / 循环 | 纹理 |
| --- | --- | --- | --- |
| `fabao_021` | `geo/fabao021.geo.json` | `animations/fabao021.animation.json` / `animation.fabao021.idle` | `textures/item/fabao021.png` |
| `fabao_022` | `geo/fabao022.geo.json` | `animations/fabao022.animation.json` / `animation.fabao022.idle` | `textures/item/fabao022.png` |

`fabao021` 的0.2秒 idle 没有骨骼轨道；`fabao022` 的1.2秒 idle 旋转 `sheng1`–`sheng9`。来源 `ItemAnimationFactory` 是真实的玩家 START tick 事件消费者：任一手为动画物品时依次处理主副手，非护甲的非空 `geckoAnim` 均清空；客户端分派到 `Ling14item`、`Fabao018`、`Fabao021`、`Fabao022` 四种物品，写入过程名并将主副手当前帧和前一帧共四个持物高度字段均设为1。两个1.18.2工程未找到非空写入者，已有非空物品实例输入入口仍保留。

目标 `EquipmentAnimations.tickItems` 在 `PlayerTickEvent.Pre` 保留该主副手顺序和双侧标签清空；本批只实现021/022两件 `AnimatedFabaoItem` 的过程分派，018和 `Ling14item` 留待所属批次。两件法宝各自的请求通过客户端提供者调用 `EquipmentItemAnimationClient`，把NeoForge21.1.265主副手当前帧和前一帧共四个持物高度字段设为1。两件法宝的 `procedureController` 过渡为0，非空过程名使idle停止；按来源 `STOPPED` 条件设置显式 `PLAY_ONCE`，设置后仍为 `STOPPED` 才清空并 `forceAnimationReset`。该过程名属于物品单例，不能改成每栈独立过程槽。

来源骨骼自定义指令队列没有注册消费，动画JSON也没有指令；模板的 `renderArms/shouldAllowHandRender` 无实际调用，`getUniqueID` 也不是 GeckoLib 3.0.57 使用的 `getInstanceId` 入口。这些模板不用于补造新的骨骼指令消费者、手部屏蔽或额外视角变换，和上段已经挂载的玩家刻事件分别处理。

来源物品模型父链中的 `builtin/entity` 必须保留。NeoForge 21.1.265 的 `ModelBakery.loadBlockModel` 直接提供该内建模型，`BlockModel.bake` 据此创建 `BuiltInModel` 并进入自定义物品渲染；它没有对应包内JSON。`convert_assets.py` 原先按JSON是否存在删除父级，本批修正为保留 `builtin/generated`、`builtin/entity`、`builtin/missing`。资源提取器 `extract_animated_fabao.py` 从两个模型类登记六条来源引用，再由通用转换器原样复制几何、动画与纹理。

### 验证边界

静态核对已覆盖1.18.2版本两个物品、模型、渲染器、动画JSON和 `ItemAnimationFactory`，以及GeckoLib4.9.3的 `GeoItem`、`GeoRenderProvider`、`GeoItemRenderer`、`GeoModel` 和NeoForge21.1.265模型烘焙入口。提取器已登记六项资源，通用转换输出12929个文件、91个丢弃父级。日志5条来源引用缺失中，3条是既有空 `blocks/`，另2条是两个 `displaysettings` 的particle纹理在1.18.2资源里写为 `item/fabao021`、`item/fabao022`，实际文件位于 `items/`；目标转换已生成两张 `textures/item` PNG，模型与渲染器最终引用均有实际资源，不能把该来源路径诊断写成目标缺图。证据为 `tmp/animated-equipment-assets.log`。

完整 `gradlew build` 通过，包含既有test/check与ArchUnit，证据为 `tmp/animated-equipment-build.log`。本批未新增或修改Test类、冒烟检查断言和选择器，也未使用Computer Use。内置 `runSmokeClient -PsmokeOnly=equipmentweapon,demon` 的五项既有弓/拂尘检查和四项既有妖化检查共九项均为 `INFO`、`0 FAIL`、`DONE`；客户端保存overworld、nether、end、xvkong四维度后正常退出，`ERROR/FATAL=0`。证据为 `tmp/animated-equipment-smoke.log`、`tmp/animated-equipment-report.txt` 和 `tmp/animated-equipment-client-latest.log`。

已查看 `build/smoke/screenshots/equipment-demon-armor.png`，仅确认护甲架上的既有妖化模型可见；它没有展示57件新防具或两件法宝。客户端的 `armor10` 来源贴图为56×35像素，加载时将mipmap限制为0的WARN保留登记，未修改来源尺寸，也不是ERROR。

专用服务器一次启动至 `Done`，本次TTY与init脚本成功转发标准输入，执行原生 `stop` 后正常保存四维度并退出，`ERROR/FATAL=0`；证据为 `tmp/animated-equipment-server-latest.log`。注册导出物品2125个、实体109个，来源指纹检查退出码0，证据为 `tmp/equipment-animated-inventory.log`。本批文档、台账和差异检查由主会话在四文档收口后统一执行。

上述运行只作公共注册、资源加载和既有场景回归。57件新防具的逐件自然穿戴、两件法宝自然持物、非空 `geckoAnim` 请求与单次动画恢复、存档重载、第一人称/第三人称/GUI完整视觉、1.18.2实机、两版本对照和多人验收未执行；不能以既有九项检查或妖化截图替代这些专项。
