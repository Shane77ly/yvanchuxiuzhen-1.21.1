---
topic: plant
role: architecture
status: draft
updated: 2026-11-19
---

# 植物：架构调查与提案

来源事实与目标实现分别陈述。本批按已批准的系统职责接回来源行为，其余目标方案仍为草案；源码定位采用 [系统导航](../README.md)的 A/B 规则。

## 人间道桃叶生命周期（2026-11-19）

1.18.2 版本 `PeachleavesBlock` 直接继承原生 `LeavesBlock`，不重写距离或液体交互；来源树叶沿原木标签与叶片传播距离，不可含水。目标桃叶复用已有 `FruitTreeLeavesBlock`：按来源叶距计划刻更新、玩家放置时设持久，并禁用流体入口；父类保留的含水状态不参与流体交互。内容表及转换工厂覆盖同步，既有桃叶战利品表不变。

定向转换、生产与Smoke编译及受控玩家放置、叶距更新、直接液体交互检查通过；不证明自然桃树、落叶与拾取正常。自然采收、枯萎、通过水桶物品操作、真实保存重进、1.18.2 版本实机及两版本对照均未执行。

## 人间道虫蚀草采收

1.18.2 版本 `A:block/ChongshicaoBlock.java` 的 `getDrops` 保留非空原生战利品，仅在列表为空时返回自身一份。1.21.1 版本由虫蚀草专用方块消费内容表的回退数量，保留原花类存活、偏移和炖菜效果，不改其他野花。原生配置仍在人间道 `biomepeach_01`、`biomepeach_02` 投放；Forge 40.1.0 官方映射源码 `FeatureUtils.simplePatchPredicate` 将空底材列表映射到 `BlockPredicate.ONLY_IN_AIR_PREDICATE`，严格匹配 `Blocks.AIR`，等同目标 `matching_blocks`，不改生成规则。采收所得物品按既有药材定义由炼丹系统消费。[限定实机](logs/2026-11.md#2026-11-19-人间道自然虫蚀草采收与拾取)已验证一个自然样点的生存采收及原生拾取，实际制药和读盘见[炼丹记录](../liandan/logs/2026-11.md#2026-11-19-人间道虫蚀草炼丹与炉次重进)。

## 人间道桑树、桃树与成熟水稻自然供给（2026-11-18）

`mulberry_02`、`peach_01`、`shicai_001_plant_08` 已具备正式注册、配置、投放和来源群系挂载。桑树挂入 `biomewind_02`、`biomeautumn_01`、`biomefeng_02`；模板桃树挂入 `biomefeng_02`、`biomepeach_01`；成熟水稻挂入三个 `biomeautumn`。实现均保留人间/妖魂维度准入；现有这些来源群系的维度表命中在人间道，不以准入本身推定妖魂另有投放。桃林的原生树地物也可产生同一种桃叶，看到桃叶不能单独归因给模板 `peach_01`。

桑叶原生掉落桑葚、桑苗或桑叶；生存破坏另有独立生蚕判定。Craft 的蚕与蚕匣按桑叶身份消费，Cooking 保留桑葚。桃叶走来源概率掉落林桃，Material 食用产核是独立前置，Plant 只接产出的桃核。成熟水稻生成上下两格、下半格产水稻种子；Cooking 配方用四份种子合成一份大米。不补造新玩家文本或跨系统写入口。

`plant:peach_kernel` 只处理顶面点击，在被点方块上方写普通桃苗并扣一枚，不添加来源没有的目标空气或地面检查。`plantsupply` 基础模式定位未经玩家采收的自然方块、存活和水稻双格；林桃模式另核自然实得果子、食用产核、播种及受控成长的真实关档重开。结果仅限[本月实机路径](logs/2026-11.md#2026-11-18-人间道自然林桃至播种与重进)，不以 `give`、`place` 或概率未命中代替。

## 1.18.2 范围

来源包括农作物、树木与普通植物；当前归属阅读范围见本系统README与归属表。`Shicai*` 种子与作物的功能族横跨 plant 与 cooking 的候选归属，开工时按归属判定确认。

## 目标所有权与接口提案

作物、树木与野生植物的生长阶段、掉落与再生归本系统；自然投放由 world 的地物定义引用。

## 验收

植物要同时核对获得、阶段、掉落和再生。

## 水稻001与桑苗桑叶来源规则（2026-11-10）

本节记录按已批准系统归属正式接回的来源行为；不把其余架构提案标为已批准。首批21条行为注册（水稻17、桑苗桑叶4）及水稻地物1条与[内容台账](ledger/content.csv)对应。具体来源路径与SHA256分别在[来源证据](ledger/source-evidence.csv)和[来源指纹](ledger/source-fingerprints.csv)。静态来源阅读、目标精确API核对、正式装配与实际运行验收分别登记；首批验证与续批状态分别见月志。

### 水稻种植、生长与收获

`A:item/Shicai001seedItem`调用共享种植过程的001分支：只在原版FARMLAND、UP面且上方空位放阶段01、扣1；useOn始终SUCCESS，创造模式物品恢复由目标原生外层处理。Plant写入作物，食材身份保持Cooking；`A:Shicai001seedhecheng`和既有目标配方均为4颗种子合成1份`shicai_001`，不改方向或产量，配方按食材加工职责归Cooking，来源证据在Cooking台账。

`A:Shicai001Plant01GengXinYouXiKeProcedure`读世界当前half，upper跳过；下方名为moisture的整型属性缺失记-1，低于7先掉落再移除，达到7才按Math.random()<0.2推进。01–07同时保留随机刻与来源显式tick钩子，不主动添加计划刻。01–04继承FlowerBlock，迟缓100刻；05–08继承DoublePlantBlock，原生土壤和双格生命周期保留。01–07 XZ偏移，08不偏移，燃烧100/60。来源Forge40.1.0 canSustainPlant先允许BushBlock.mayPlaceOn，不能仅凭CROP名增设全部阶段只许耕地。

04→05先上方默认LOWER，再尝试改上方UPPER，最后当前格LOWER；05→06→07→08先当前AIR、上方AIR，再执行相同三次写入，flags3，无空间预检。骨粉目标01–07有效，Math.random()<0.5成功；成功时先当前格成熟并复制当时世界状态的同名兼容属性，再上方成熟并复制其当时状态，最后尝试把上方设UPPER。点击upper不归一下半格，保留来源可能形成异常布局的顺序；最终布局待实机，不预判修复。原生骨粉对有效服务端目标总扣1，包含概率失败。

01–07先保留非空原生loot，否则1颗种子；08为6颗，双格只有LOWER产出。成熟类没有随机生长与骨粉接口。破坏任一半格的邻居更新、创造破坏与重载需要分别运行核验，不因静态fallback代码就计完整收获已验收。

### 桑苗、生蚕与叶片生命周期

`A:MulberrysaplingDangGuFenChengGongShiYongProcedure`依次排除fabao_009weidu/xvkong、Math.random()<0.05、上方一格可见天空。随机刻、计划刻、骨粉三入口共用此过程。骨粉目标与成功条件恒true，原生有效目标服务端扣1；未长成、遮天或排除维度仍可耗骨粉，不额外补亮度、空间、土壤或邻树门槛。苗的原生loot非空优先，空时fallback自身1个。

模板始终mulberry_01；依次独立抽样25%、33.3%、50%、else，不替换成均匀随机四选一。锚点及旋转为(-3,+3,+3)/CCW90、(+3,+3,+3)/180、(+3,+3,-3)/CW90、(-3,+3,-3)/NONE；mirrorNONE、保留实体、flags3，不套用野外mulberry_02的AIR忽略设置。模板后依来源顺序补y+2环8叶、中心y+2/y+1木；根在首个服务端Post写木，不检查当前仍为苗，不改成gameTime+1。临时队列不持久化，停服清引用，保留来源可覆盖同刻新方块的顺序。

`A:MulberryleavesDangFangKuaiBeiWanJiaPoPiShiProcedure`在super.onDestroyedByPlayer后执行，即使super返回false也执行；不加工具、willHarvest或创造门槛。服务端依次独立5%普通蚕、0.5%金蚕，同次可双生；位置(x+.5,y,z+.5)、旋转0、MOB_SUMMONED、finalizeSpawn后addFreshEntity，实体仍由Craft拥有。客户端不执行无玩法结果的全局随机抽样。

普通叶片loot桑葚/苗/叶权重5/1/5，桑葚1–2、苗1、叶1，前两项Fortune ore_drops，各项爆炸衰减；不增加剪刀或丝触条件。两版本剪刀接口的非实体默认结果为空，原生剪刀交互路径面向LivingEntity，不能据Leaves实现剪刀接口虚构额外叶片收获。叶距仅允许logs与LeavesBlock传播，保留随机衰减、persistent和一刻更新；目标父类WATERLOGGED属性留默认false，但桶注取水、含水流体和含水更新关闭，以免新增来源没有的玩法。

### 自然投放与供给边界

水稻成熟地物类型、configured与placed已装配；`convert_data.py` 经 `convert_plant_worldgen.py` 从锁定来源生成两份JSON并登记同一来源SHA-1，不手写manifest。来源随机patch为tries1、xz7、y3，内部SimpleBlock/ONLY_IN_AIR；外层顺序Count1→Rarity32→InSquare→MOTION_BLOCKING→BiomeFilter。仅dimensionrenjian/dimensionyaohun与biomeautumn_01/02/03；World维度/群系未接回前不安装缺引用modifier，不扩到主世界。类型注册与/place仅能证明独立入口，不代替新生成区块的自然获取。

桑树野外mulberry_02仅允许dimensionrenjian/dimensionyaohun与biomewind_02、biomeautumn_01、biomefeng_02；每次机会随机整数1..1000000<=50000，nextInt(1)+1恒1次，x/z各nextInt(16)，WORLD_SURFACE_WG-1，允许下方grass_block/dirt_path/mycelium/dirt/coarse_dirt/podzol/rooted_dirt；模板mulberry_02锚点(-4,+1,-4)、NONE、AIR忽略、保留实体、flags2。当前接线与验收边界见[人间自然供给](#人间道桑树桃树与成熟水稻自然供给2026-11-18)。

### 验证边界

完整build及内置 `runSmokeClient -PsmokeOnly=fulucooking` 的160项自动检查已通过；其中受控覆盖与证据等级见[月志](logs/2026-11.md)。数据JSON在本次转换后重新生成且来源SHA-1进入manifest，静态Codec结构复核完成；专服启动/stop保存四维度通过；未执行自然新生成区块、完整区块重载、来源1.18.2版本实机、多人、完整视觉和两版本对照。概率按源码抽样顺序核对，不以偶然未触发写失败断言。

## 糯稻002与两编号谷物规则（2026-11-10）

将001与002的来源Java按编号归一后逐份对照：生长过程全文一致，阶段02–07类全文一致；01不同在001迟缓/默认PLAINS、002速度/CROP，08不同在001无randomTicks/PLAINS、002有randomTicks/CROP。目标每阶段内容表保留这些差异，不新增成熟推进回调。源码的CROP标记不将整个地面收窄成仅耕地：Forge40.1.0的canSustainPlant先认可BushBlock.mayPlaceOn的DIRT/原版耕地，再判断PlantType；目标21.1.265 Bush/DoublePlant原生地面与双格生命周期已逐段核对。

来源编号到枚举、阶段工厂与播种入口的完整对应，见[作物编号表](#28种作物供给整批2026-11-10)。

两编号都先排除UPPER；土壤moisture缺失为-1、低于7掉落后销毁，否则20%推进。04→05先上方默认LOWER、再按当时世界状态改UPPER、最后当前LOWER；05→08先依次清当前与上方，再同样三次写入，无空间预检。单格01–04和双格05–07保留计划刻与随机刻钩子。成熟LOWER空loot回落本编号6种子，UPPER无掉落；其余阶段fallback本编号1颗。种子只UP+原版耕地+上方空位，useOn总SUCCESS；创造消耗恢复由原版调用外层承担。

两编号01–07骨粉有效且50%成功，概率失败仍由原生扣1。001按当前世界状态复制兼容属性再写成熟；002只写默认成熟下格、默认上格、再尝试上格UPPER，不复制原属性。均不归一上半格，不补空间/土壤预检；其余编号在独立核对来源后复用相同机制，004/005/006差异见后文。

002成熟地物类型复用相同的两维度过滤：renjian/yaohun；configured/placed参数与001同为patch1/xz7/y3、Count1→Rarity32→InSquare→MOTION_BLOCKING→Biome。002只列autumn01/02，001另含autumn03。两个编号的JSON各从其Feature来源生成并记录各自SHA-1；World维度/群系未完成前都不安装modifier，自然新区域未验收。002四种子转1糯米的配方属于Cooking食材加工，不改变Plant种子所有权；配方、物品类及编辑器定义另录Cooking证据。

糯稻002目标运行证据：完整`build compileSmokeJava`通过（4m31s，含既有test/check与ArchUnit）；标准`convert_data.py`输出1707项，002两份configured/placed JSON各追踪锁定来源SHA-1。内置`runSmokeClient -PsmokeOnly=fulucooking`共166项INFO、0 FAIL、DONE，客户端正常保存四维度退出、ERROR/FATAL0；本批新增6项均通过。专服启动至Done后通过原生halt(false)正常保存四维度，Gradle BUILD SUCCESSFUL、游戏日志ERROR/FATAL0；控制台stdin已关闭，未收到stop命令；动态加载既有停服agent产生JDK警告，PowerShell包装返回1，Gradle原生返回0。证据为`tmp/shicai002-{build.log,smoke.log,report.txt,client.log,server.log}`。新增检查覆盖002原生生存播种扣种及泥土拒绝、受控原生randomTick干旱枯萎与对应种子掉落、原生骨粉有效目标扣一及本次结果、成熟上下半原生loot，以及真实加载RecipeManager的四种子配方匹配/assemble产一糯米；配方检查没有模拟玩家工作台取出。完整自然成长、上下半骨粉异常布局、自然新区域、完整保存重登、来源1.18.2版本实机、多人及两版本对照未执行。

## 28种作物供给整批（2026-11-10）

在001/002之后按来源调用链整合004–008、011–033共28种作物，覆盖129个阶段及23个Plant独立播种物。编号是注册身份的一部分，各族只共享已核相同机制；逐编号枚举、工厂和内容行保留差异，无需按编号复制整套类。以下工厂均可在PlantSetup逐行核对，`NNN`为表中三位编号，阶段范围含两端。表中食材名称沿最终中文映射；完整来源路径和规则分支见本系统[来源证据](ledger/source-evidence.csv)。

| 来源编号与食材 | 来源阶段ID | 实现身份与阶段工厂 | 播种物与入口 |
| --- | --- | --- | --- |
| 001 水稻 | `shicai_001_plant_01..08` | `CerealCrop.SHICAI_001`，`plant:shicai001_01..08` | `shicai_001seed`，`plant:shicai001_seed` |
| 002 糯稻 | `shicai_002_plant_01..08` | `CerealCrop.SHICAI_002`，`plant:shicai002_01..08` | `shicai_002seed`，`plant:shicai002_seed` |
| 004 黄米 | `shicai_004_plant_01..08` | `CerealCrop.SHICAI_004`，`plant:shicai004_01..08` | 本体`shicai_004`，`plant:shicai004_seed` |
| 005 高粱 | `shicai_005_plant_01..08` | `CerealCrop.SHICAI_005`，`plant:shicai005_01..08` | 本体`shicai_005`，`plant:shicai005_seed` |
| 006 玉米 | `shicai_006_plant_01..08` | `CerealCrop.SHICAI_006`，`plant:shicai006_01..08` | `shicai_006seed`，`plant:shicai006_seed` |
| 007 尖椒 | `shicai_007_plant_01..05` | `GrainCrop.SHICAI_007`，`plant:shicai007_01..05` | `shicai_00701`，`plant:shicai007_seed` |
| 008 莲藕 | `shicai_008plant_01..04`，plant前无下划线 | `LotusRootBlock`/`LotusPartBlock`，`plant:lotus_root/stem/pod/leaf` | Cooking莲子`shicai_00801`，`cooking:lotus_seed`→`Planting.lotus` |
| 011 芹菜 | `shicai_011_plant_01..04` | `VegetableCrop.SHICAI_011`，`plant:shicai011_01..04` | `shicai_01101`，`plant:shicai011_seed` |
| 012 小葱 | `shicai_012_plant_01..04` | `VegetableCrop.SHICAI_012`，`plant:shicai012_01..04` | `shicai_01201`，`plant:shicai012_seed` |
| 013 大葱 | `shicai_013_plant_01..04` | `VegetableCrop.SHICAI_013`，`plant:shicai013_01..04` | `shicai_01301`，`plant:shicai013_seed` |
| 014 菜心 | `shicai_014_plant_01..04` | `VegetableCrop.SHICAI_014`，`plant:shicai014_01..04` | `shicai_01401`，`plant:shicai014_seed` |
| 015 蒜苗/蒜苔/大蒜 | `shicai_015_plant_01..04` | `VegetableCrop.SHICAI_015`，`plant:shicai015_01..04` | Cooking大蒜`shicai_01502`，`cooking:plantable_garlic`→`Planting.garlic` |
| 016 韭菜/韭菜花 | `shicai_016_plant_01..04` | `VegetableCrop.SHICAI_016`，`plant:shicai016_01..04`；03/04为`VegetableHarvestBlock` | `shicai_01601`，`plant:shicai016_seed` |
| 017 茼蒿 | `shicai_017_plant_01..04` | `ProduceCrop.SHICAI_017`，`plant:produce017_01..04` | `shicai_01701`，`plant:produce017_seed` |
| 018 苋菜 | `shicai_018_plant_01..04` | `ProduceCrop.SHICAI_018`，`plant:produce018_01..04` | `shicai_01801`，`plant:produce018_seed` |
| 019 茴香 | `shicai_019_plant_01..04` | `ProduceCrop.SHICAI_019`，`plant:produce019_01..04` | `shicai_01901`，`plant:produce019_seed` |
| 020 花椰菜 | `shicai_020_plant_01..04` | `ProduceCrop.SHICAI_020`，`plant:produce020_01..04` | `shicai_02001`，`plant:produce020_seed` |
| 021 菠菜 | `shicai_021_plant_01..04` | `ProduceCrop.SHICAI_021`，`plant:produce021_01..04` | `shicai_02101`，`plant:produce021_seed` |
| 022 洋葱 | `shicai_022_plant_01..04` | `ProduceCrop.SHICAI_022`，`plant:produce022_01..04` | `shicai_02201`，`plant:produce022_seed` |
| 023 黄花菜 | `shicai_023_plant_01..04` | `ProduceCrop.SHICAI_023`，`plant:produce023_01..04` | `shicai_02301`，`plant:produce023_seed` |
| 024 大白菜 | `shicai_024_plant_01..04` | `ProduceCrop.SHICAI_024`，`plant:produce024_01..04` | `shicai_02401`，`plant:produce024_seed` |
| 025 辣椒 | `shicai_025_plant_01..06` | `GrainCrop.SHICAI_025`，`plant:shicai025_01..06` | `shicai_02501`，`plant:shicai025_seed` |
| 026 西红柿 | `shicai_026_plant_01..05` | `GrainCrop.SHICAI_026`，`plant:shicai026_01..05` | `shicai_02601`，`plant:shicai026_seed` |
| 027 茄子 | `shicai_027_plant_01..05` | `GrainCrop.SHICAI_027`，`plant:shicai027_01..05` | `shicai_02701`，`plant:shicai027_seed` |
| 028 姜 | `shicai_028_plant_01..04` | `ProduceCrop.SHICAI_028`，`plant:produce028_01..04` | Cooking本体，`cooking:produce028_food`→`Planting.shicai028` |
| 029 白萝卜 | `shicai_029_plant_01..04` | `ProduceCrop.SHICAI_029`，`plant:produce029_01..04` | Cooking本体→`Planting.shicai029`；`shicai_02901`→`plant:produce029_seed` |
| 030 水萝卜 | `shicai_030_plant_01..04` | `ProduceCrop.SHICAI_030`，`plant:produce030_01..04` | Cooking本体→`Planting.shicai030`；`shicai_03001`→`plant:produce030_seed` |
| 031 心里美 | `shicai_031_plant_01..04` | `ProduceCrop.SHICAI_031`，`plant:produce031_01..04` | Cooking本体→`Planting.shicai031`；`shicai_03101`→`plant:produce031_seed` |
| 032 红薯 | `shicai_032_plant_01..04` | `ProduceCrop.SHICAI_032`，`plant:produce032_01..04` | Cooking本体，`cooking:produce032_food`→`Planting.shicai032` |
| 033 芋头 | `shicai_033_plant_01..04` | `ProduceCrop.SHICAI_033`，`plant:produce033_01..04` | Cooking本体，`cooking:produce033_food`→`Planting.shicai033` |

### 004/005/006八阶段谷物

完整读取各阶段源码及其生长、骨粉、播种过程后，只有一致的阶段机制复用Cereal。004/005本体没有食物组件，主动useOn为播种，归Plant；食材加工消费者仍由Cooking拥有。`A:MuzengzhengliuProcedure:140–163`确实消费高粱005两份与小麦两份制作`jiuniang_003`，不能写成无酿酒消费者。006食材本体仍归Cooking，006seed与玉米1→种子8配方归Plant。

04之前单格、05–08双格，普通生长无新增光照条件，湿度/双格清空及三次写序沿前节Cereal规则。00401空loot异常回落糯稻002seed一颗；004/005其他未成熟阶段掉本体一份，006掉006seed一颗；双格上半空。成熟下半004/005各掉本体六份、006掉玉米两份，上半空。父类非空loot始终优先。004/005/006的01–07 randomTicks=true、08=false；炖菜效果01–04均迟缓五秒，各编号内容行独立保存。

004/005/006普通骨粉以50%成功写默认成熟态，不复制现有属性、不归一下半且无空间预检。00603例外调用普通生长：外层成功后仍先查湿度，满足后重新20%抽样，最多03→04；不能直接成熟。三个播种物均要求原版耕地、UP和上方空位，放01后扣一，失败也SUCCESS，创造数量恢复沿原生外层。

### 007/025/026/027可重复采摘族

`Grain`类名承载已核同型可重复采摘作物，实际身份始终为枚举编号和独立内容行。007/026/027阶段序列01→02→05→03→04，025为01→02→03→04；04采摘均先回05，025再经05→06→04。生长刻先查土壤名为moisture的整数属性，缺失为−1、低于7掉资源再destroy(false)；满湿度且亮度至少9时以20%推进。成熟04无生长刻，其余保留随机刻与计划刻。

骨粉有效目标恒真、50%成功，普通阶段成功直04；02505例外仍走普通生长，保留湿度、亮度与第二次20%抽样。采摘每轮重新抽1..3作为循环上界，各独立生成一件食材，拾取延迟10；一次总量抽样会改变概率，不能替代。由该循环得到一/二/三件概率分别1/3、4/9、2/9。04右键支持原生空手及持物入口并SUCCESS。破坏时007所有阶段fallback种子1；025/026/027成熟各食材2，其他种子1，非空loot优先。四种食材留Cooking，四种不可食种子与产种配方归Plant；产种量007为1，其余各4。炖菜效果007/025速度、026挖掘疲劳、027迟缓，均五秒；成熟025/026/027 OffsetType.NONE，其他沿独立内容行。

### 011–016四阶段蔬菜

01–03随机刻及计划刻使用满湿度、亮度至少9、20%推进；缺湿度按−1并枯萎。普通骨粉50%成功直04；01503蒜苔成功后调用普通生长，01604保留有效骨粉接口但成功无效果。普通持骨粉点01603/04先走收割，潜行可按原生规则绕方块交互。01603/04收割均回02、掉韭菜1，04额外韭菜花1，分别独立实体且延迟10。

01501/02/03/04空loot依次掉大蒜1、蒜苗2、蒜苔2、大蒜4；01104掉芹菜1。其他成熟012/013/014/016依赖实际资源loot，不能追加Java保底。012/013的两次loot各等权食材1或种子，种子Uniform min=3/max=1原样保留；目标精确UniformGenerator→Mth.nextInt在min>=max时返回3，来源实机未执行。014两次loot等权食材1或种子1..3。016一次loot等权种子1..3、韭菜花1、韭菜1..2；韭菜花entry无爆炸衰减和fortune，其余沿来源。011种子配方芹菜1→种子3、016韭菜花1→种子4归Plant。

五种不可食种子与所有阶段归Plant，大蒜01502可食且播种，经公开API委托，蒜苗/蒜苔与韭菜花保持各自食物身份。011炖菜急迫、其余速度，均五秒；成熟均NONE，01502/03及01603也NONE，其余沿内容表。不存在015成熟地物，不能补造。

### 017–024、028–033普通作物

01–03生长与骨粉机制已独立核对，使用Produce族：缺湿度/低于7先掉资源再destroy(false)，满湿度且亮度至少9时20%推进；50%骨粉成功直接04并复制当前世界兼容属性，不加湿度/亮度门槛。成熟04不生长、不接受骨粉、offset NONE。未成熟空loot各播种物1，02403改为种子3；成熟fallback逐编号如下，全部无来源block loot资源，Java fallback是主路径。

| 编号 | 成熟食材数量 | 炖菜效果（均五秒） |
| --- | --- | --- |
| 017 | 2 | 全阶段挖掘疲劳 |
| 018 | 4 | 全阶段急迫 |
| 019 | 4 | 全阶段迟缓 |
| 020 | 1 | 全阶段迟缓 |
| 021 | 4 | 全阶段力量 |
| 022 | 1 | 全阶段速度 |
| 023 | 1 | 全阶段迟缓 |
| 024 | 1 | 01/04跳跃提升，02/03速度 |
| 028 | 3 | 全阶段瞬间伤害 |
| 029 | 3 | 全阶段速度 |
| 030 | 6 | 全阶段速度 |
| 031 | 3 | 全阶段速度 |
| 032 | 4 | 01速度，02–04迟缓 |
| 033 | 3 | 01–03迟缓，04速度 |

028–033六种本体全部具备食物组件和播种入口，留Cooking；029/030/031还有独立种子入口，两者均接。028营养0、饱和0仍是食物且非alwaysEat，不删除其食物组件。其余十一种独立种子与产种配方归Plant，食材本体仍由Cooking按既有食物定义承载。

### 008莲藕与采藕自由NBT

实际阶段ID为`shicai_008plant_01..04`。根01的随机刻与计划刻先20%抽样，再扫描上方1..10格；02茎继续，首个非茎处若流体legacy state为WATER，先放02再设waterlogged。若空气，独立30%茎、未中再独立40%莲蓬、否则叶；叶方向再依次独立25%、33.3%、50%及剩余分支。没有骨粉入口。

02/03/04邻接更新检查下方`shicai_008plant`标签，失去支撑destroy(false)无掉落。根/茎/蓬/叶空loot分别泥土1、空栈、莲子4、睡莲1，原生非空loot优先；含水茎/叶沿目标原生SimpleWaterloggedBlock。

根开始挖掘时扫描上方1..10格，不要求茎连续，首个蓬/叶写玩家自由NBT `caiou=1+floor(height*0.5)`，没找到不清旧值。原生玩家破坏super之后，不以创造/willHarvest/父返回值为门槛，按(int)caiou生成独立莲藕各1、拾取延迟10，最后清零。Plant独占该键；服务端决定玩法真值。强类型typed_field_accesses未覆盖该自由NBT，已按实际开始挖掘与破坏调用链补核。

可食莲子00801由Cooking持有，经`Planting.lotus`：泥土上方精确水块即可，不检查点击面，泥土本格变根并扣1。共享过程还包含耕地UP+上空时只扣1、不放块的来源异常，保持该结果；创造恢复沿原生外层，不自行加消耗豁免。

### 成熟地物、转换与证据边界

28个已接成熟地物使用同一个`CropFeature`保留两维度限制；001/002既有CerealFeature及其他族的等价候选类合并，不复制完全相同实现。新增26种各有configured/placed两份数据，由`convert_plant_worldgen.py`经标准`convert_data.py`生成并追踪各自来源Java SHA-1。参数默认RandomPatch tries1/xz7/y3、simple_block成熟默认态；普通单格作物保留来源逐类谓词，001/002/004/005/006成熟双格谷物按来源`List.of()`使用空placement谓词列表，不额外要求空气落点。其余族继续按来源配置核对；双格成熟态含half=lower；外层Count1→Rarity32→InSquare→MOTION_BLOCKING→Biome。023为tries4/count1，032为tries2/count2。仅dimensionrenjian/dimensionyaohun；未装任何替代主世界或指向缺失群系的modifier。豆科成熟地物046/047/048/051/054/055/056与双株芝麻05701的来源配置同样使用空placement谓词列表；目标转换器按Feature类型输出空列表，不额外要求空气落点。其tries、成熟方块状态、外层Count/Rarity/InSquare/Heightmap/Biome顺序、群系引用与维度限制保持原规则。来源符号与未验证边界见[本批月志](logs/2026-11.md#2026-11-19-成熟豆科与芝麻地物谓词对齐)。

以下列出新增地物的来源群系（均省略`biome`前缀），供World后续装配；008/015没有本批成熟地物。

| 编号 | 来源群系 |
| --- | --- |
| 004/005/006 | autumn_01、autumn_02 |
| 007 | wind_02、autumn_03、wind_04 |
| 011/012/013 | autumn_01、autumn_02 |
| 014 | wind_03、wind_02 |
| 016 | autumn_03 |
| 017 | wind_03、wind_04 |
| 018 | autumn_02 |
| 019 | jade_03、jade_02、autumn_02 |
| 020 | jade_03、jade_02 |
| 021 | autumn_01、feng_02、autumn_02、feng_03、feng_01 |
| 022 | wind_03、autumn_01、autumn_02、wind_04 |
| 023 | autumn_03、feng_01 |
| 024 | wind_03、autumn_01、jade_01、autumn_02、wind_04 |
| 025 | autumn_01、autumn_02 |
| 026 | jade_03、autumn_02 |
| 027 | jade_01、jade_03、jade_02 |
| 028 | autumn_01、cloud_01、autumn_02 |
| 029 | jade_01、jade_03、jade_02、autumn_02 |
| 030 | jade_01、jade_03、jade_02、autumn_02 |
| 031 | autumn_01、jade_01、jade_03、jade_02、autumn_02 |
| 032 | wind_01、wind_03、wind_02、autumn_01、autumn_02 |
| 033 | jade_01、jade_03、jade_02、peach_02、peach_01 |

2026-11-18按当前活动集成复核另12个菌类/豆科/芝麻成熟地物的既有注册、两道维度门、configured/placed参数、来源manifest与27处VEGETAL_DECORATION挂载：042/045位于jade_03；043位于jade_03、turtle_02；044位于jade_03、wind_04；046/047/048/051/054/055/056位于autumn_01、autumn_02；057位于autumn_01–03及feng_01–04。12条Feature台账由待处理回填为已注册，未新增生产代码或资源；来源字节与SHA256复用锁定证据，补入主线尚未接收的12条来源证据与指纹。此为来源与当前目标的静态闭包，不代表自然新区块出现、成熟植株存活或玩家采收验收，也不包含候选四道中的模板地物。

本批完整build/compileSmokeJava已通过，data输出1759项。最终既有`runSmokeClient -PsmokeOnly=fulucooking`为238项INFO、0 FAIL、DONE（3m11s/退出0），客户端四维度正常保存退出且ERROR/FATAL0。专服在Done后通过持有stdin实际发送原生stop，正常保存四维度，BUILD SUCCESSFUL（29s）/退出0、ERROR/FATAL0。28种作物的自然投放、完整成长/采摘循环、完整保存重登、来源1.18.2版本实机、多人、完整视觉与两版本对照未执行。此前001/002运行结果不能代替新增28种作物验收。

## 009/010果树与034–039藤本（2026-11-10）

编号仍以来源注册ID为准；共享Java类只表达核实后相同的行为。树木与藤本的注册映射如下：

| 来源编号 | 中文与来源内容ID | 目标实现 |
| --- | --- | --- |
| 009 花椒树 | `shicai_00902`树干、`shicai_00903`树叶、`shicai_00904`树苗 | `FruitTree.SHICAI_009`、`fruit_tree_log/leaves`、`shicai009_sapling` |
| 010 八角树 | `shicai_01001`树干、`shicai_01002`树叶、`shicai_01003`树苗 | `FruitTree.SHICAI_010`、同一木/叶类及`shicai010_sapling` |
| 034 南瓜 | `shicai_034_plant_01..05` | `VineCrop.SHICAI_034`、`vine034_stage` |
| 035 黄瓜 | `shicai_035_plant_02..04`，来源没有01阶段 | `VineCrop.SHICAI_035`、`vine035_stage` |
| 036 冬瓜 | `shicai_036_plant_01..05` | `VineCrop.SHICAI_036`、`vine036_stage` |
| 037 苦瓜 | `shicai_037_plant_01..03` | `VineCrop.SHICAI_037`、`vine037_stage` |
| 038 丝瓜 | `shicai_038_plant_01..03` | `VineCrop.SHICAI_038`、`vine038_stage` |
| 039 葫芦 | `shicai_039_plant_01..04`；03904为葫芦库存方块 | `VineCrop.SHICAI_039`、`vine039_stage`、`GourdBlockEntity` |

六种独立种子分别是`shicai_03401`至`shicai_03901`，由编号各自的`VineSeedItem`入口承载。来源种子use先查UP面及上方空位：旧材质DIRT/GRASS分支放置首阶段并扣一；FARMLAND分支只扣一、不放置。035从02开始。保留来源CROP类型、首阶段花朵骨粉入口、每次50%生长成功率与各编号独立的方向抽样，不套用普通作物的统一湿度/亮度门槛。

009/010树苗的骨粉资格与成功查询均为真，但共享生长过程仍先抽5%，所以骨粉不保证成树。随机刻、计划刻与骨粉共用该门槛，之后按来源顺序独立抽样四种模板旋转。009生长模板为`shicai00908`，模板后立即补一格`shicai_00902`；010使用`shicai01005`并立即补三格`shicai_01001`。两者自然模板则为`shicai00907`和`shicai01004`，只允许人间维度、草方块基底及来源3%抽样，placed JSON不另加placement。World尚未提供`dimensionrenjian`和`biomeautumn_02/03`，因此目前只完成Feature、configured/placed注册，未安装群系modifier，也没有自然生成实机证据。`FruitTreeFeature`缓存模板对象的`/reload`行为尚无可用人间维度验证，保留来源方式并登记为待测风险。

树叶距离仅把`minecraft:logs`识别为木干，其余沿来源LeavesBlock距离规则；持久模板叶不会仅因距离远而枯萎。两种树叶均无右键采摘或剪刀自掉落扩展，空loot才按各自来源表处理；木干与树苗保留非空loot优先、空时自掉落一份。

藤本逐号保留来源异常：03504右键回到03并掉一份食材；03703虽有共享采摘分支但方块未绑定use；038的自然生长过程实际读写035阶段，因而038自然不推进；03803绑定的use过程不匹配成熟阶段，返回成功但无变化。03902/03由计划刻每40游戏刻重复调度，不使用随机刻；039成长分支保留来源西向上层分支误写EAST的结果。03904右键只在动画1/2之间切换；九槽方块实体保存物品并提供来源能力，菜单提供器仍返回独立27槽菜单，不把该菜单当作葫芦库存界面。1.18.2 版本 `YvanchuxiuzhenModItems.java` 中03904展示物品的属性为 `tab(null)`；1.21.1 版本内容行不设置创造栏，仍保留注册身份和正常方块物品入口。

009/010与034–039共65条Plant行为台账已迁移（果树12条、藤本阶段和物品46条、种子6条、葫芦方块实体1条）；两项果树Feature保持“已注册”，备注World群系未装配。资源审计核对29个blockstate、44个物品模型入口及179份引用闭包文件，未见模型/贴图缺失；154份真实来源文件SHA-1一致。最终`runSmokeClient -PsmokeOnly=fulucooking`报告301项INFO、0 FAIL、DONE，客户端四维度保存退出；专项专服启动至Done后收到原生stop并正常保存，ERROR/FATAL为0。客户端场景覆盖注册与若干方块路径，不覆盖完整果树生长、果树自然生成、`/reload`缓存、多玩家、完整葫芦动画、完整存档重登、来源1.18.2版本实机或两版本对照。

## 040–058 菌类、豆科与芝麻（2026-11-10）

本节以来源数字作为对账主键；英文枚举和共享行为类只表达共用实现职责。核对时依次查来源编号、来源注册ID/元素、目标`content/plant/blocks.json`工厂、枚举编号与阶段、`PlantFeatures`地物ID。每个来源编号都保留独立枚举值和内容工厂映射，不要求每个编号各建一套Java类。

| 来源编号 | 目标阶段注册ID | 编号/阶段数据 | 行为实现与播种入口 | 成熟地物ID |
| --- | --- | --- | --- | --- |
| 040 | `shicai_040_plant_01..04` | `FungusCrop.SHICAI_040` | `WoodFungusBlock`、`FungusGrowth`、`Planting.shicai040` | 无 |
| 041 | `shicai_041_plant_01..04` | `FungusCrop.SHICAI_041` | `WoodFungusBlock`、`FungusGrowth`、`Planting.shicai041` | 无 |
| 042 | `shicai_042_plant_01..05` | `FungusCrop.SHICAI_042` | `GroundFungusBlock`、`FungusGrowth`、`Planting.shicai042` | `shicai_042_plant_05` |
| 043 | `shicai_043_plant_01..04` | `FungusCrop.SHICAI_043` | `GroundFungusBlock`、`FungusGrowth`、`Planting.shicai043` | `shicai_043_plant_04` |
| 044 | `shicai_044_plant_01..04` | `FungusCrop.SHICAI_044` | `GroundFungusBlock`、`FungusGrowth`、`Planting.shicai044` | `shicai_044_plant_04` |
| 045 | `shicai_045_plant_01..04` | `FungusCrop.SHICAI_045` | `GroundFungusBlock`、`FungusGrowth`、`Planting.shicai045` | `shicai_045_plant_04` |
| 046 | `shicai_046_plant_01..05` | `LegumeCrop.SHICAI_046` | `LegumeFlowerBlock`、`LegumeGrowth`、`Planting.shicai046` | `shicai_046_plant_04` |
| 047 | `shicai_047_plant_01..05` | `LegumeCrop.SHICAI_047` | `LegumeFlowerBlock`、`LegumeGrowth`、`Planting.shicai047` | `shicai_047_plant_04` |
| 048 | `shicai_048_plant_01..05` | `LegumeCrop.SHICAI_048` | `LegumeFlowerBlock`、`LegumeGrowth`、`Planting.shicai048` | `shicai_048_plant_04` |
| 049 | `shicai_049_plant_01..05` | `LegumeCrop.SHICAI_049` | `LegumeFlowerBlock`、`LegumeGrowth`、`Planting.shicai049` | 来源无对应地物 |
| 050 | `shicai_050_plant_01..05` | `LegumeCrop.SHICAI_050` | `LegumeFlowerBlock`、`LegumeGrowth`、`Planting.shicai050` | 来源无对应地物 |
| 051 | `shicai_051_plant_01..05` | `LegumeCrop.SHICAI_051` | `LegumeFlowerBlock`、`LegumeGrowth`、`Planting.shicai051` | `shicai_051_plant_04` |
| 052 | `shicai_052_plant_01..05` | `LegumeCrop.SHICAI_052` | `LegumeSupportBlock`、`LegumeGrowth`、`Planting.shicai052` | 来源无对应地物 |
| 053 | `shicai_053_plant_01..05` | `LegumeCrop.SHICAI_053` | `LegumeSupportBlock`、`LegumeGrowth`、`Planting.shicai053` | 来源无对应地物 |
| 054 | `shicai_054_plant_01..05` | `LegumeCrop.SHICAI_054` | `LegumeFlowerBlock`、`LegumeGrowth`、`Planting.shicai054` | `shicai_054_plant_04` |
| 055 | `shicai_055_plant_01..04` | `LegumeCrop.SHICAI_055` | `LegumeFlowerBlock`、`LegumeGrowth`、`Planting.shicai055` | `shicai_055_plant_04` |
| 056 | `shicai_056_plant_01..05` | `LegumeCrop.SHICAI_056` | `LegumeFlowerBlock`、`LegumeGrowth`、`Planting.shicai056` | `shicai_056_plant_05` |
| 057 | `shicai_05701`双格植株 | 黑芝麻物品`shicai_057`归Cooking；方块物品沿用来源编号 | 通用`double_plant`；`PlantFeatures`中的`CropFeature` | `shicai_05701` |
| 058 | Plant无方块和阶段 | Cooking物品`shicai_058` | 无独立种植或生成行为 | 来源无对应地物 |

`PlantSetup`将菌类阶段ID映射到共用的木生/地生实现。豆科阶段ID使用`plant:legume_crop`工厂，并由`LegumeBlocks.create`按每行的编号与阶段解析为对应`LegumeCrop`及花型或支架型方块。公开的`Planting`方法是跨系统稳定入口，其枚举编号与来源物品注册ID保持对应。

### 040–045 菌类规则

040/041为可贴附木面的菌类，可附着于原木标签方块、菌丝或灰化土；042–045为菌丝或灰化土支撑的地生菌类。阶段与掉落由`FungusCrop`按编号参数化，不为每个编号重复创建Java类。来源各阶段掉落量为：040/041分别1、2、3、5；042为1、2、3、4、5；043–045为1、1、1、3。骨粉可使贴附型菌类直接成熟，042推进一阶段；来源成功率分支予以保留。目标客户端场景覆盖两种播种方式、部分骨粉路径及040–045成熟掉落。2026-11-17已精确核实Forge 40.1.0的`BlockBehaviour.randomTick`会调用`this.tick`，来源菌类继承链无中间覆写；目标菌类现有显式委派保留该行为，不需修改。该静态结论不扩大已有客户端覆盖，完整自然成长仍未验收。

### 046–056 豆科规则

共用生长过程保留各编号的阶段分支。普通作物046–051、054–056在耕地播种；支架作物052/053在泥土或草方块播种，生长时复制朝向和含水状态。来源共享物品过程在052/053的耕地分支会扣除一份食材但不放置作物，目标保留此结果。04804声明随机刻，但其来源randomTick分支为空操作：湿度达标时不推进，低湿度也不移除；来源独立的右键采摘分支仍保留。052/053采摘时生成三个独立物品实体并恢复到来源阶段；05304无普通掉落，05504未绑定右键采摘，且仅01–03阶段接受骨粉。这些差异逐项记录于`LegumeCrop`、`LegumeGrowth`和编号对照表，不从相邻编号推断。

来源的七个地物分别对应046/047/048/051/054/055阶段04与056阶段05；目标地物类型及configured/placed资源均已注册。049/050/052/053没有对应来源地物。World尚未接入所需维度与群系，故群系modifier及自然供给未闭合；地物注册或资源存在不等于已验证自然生成。

### 057/058 芝麻供给边界

05701为双格植株。来源战利品池独立抽取1–2次，每次以相同权重在黑芝麻`shicai_057`与白芝麻`shicai_058`间选择，数量1–6，并应用爆炸衰减与时运。来源放置地物位于`dimensionrenjian`与`dimensionyaohun`的七个群系；目标类型及configured/placed资源已注册，但World尚未安装群系modifier，也未验证自然供给。来源覆写`getPlantType`返回`PLAINS`；1.21.1目标使用的NeoForge 21.1.265源码中已无旧Forge `IPlantable`/`PlantType` API，因此不以未实现旧接口查询作为目标行为差异，需按目标双格方块实际土壤放置结果核对兼容行为。058没有独立作物或生成入口；四份白芝麻加工一份`tiaowei_08`的石磨消费归Craft。

### 本批验证边界

最终目标版本构建`build compileSmokeJava`通过；`runSmokeClient -PsmokeOnly=fulucooking`为343项INFO、0 FAIL、DONE，客户端四维度正常保存退出，ERROR/FATAL0。报告与日志见`build/smoke/report.txt`、`tmp/plant-supply-final-build.log`、`tmp/plant-supply-final-smoke.log`及[本月日志](logs/2026-11.md)。专服原生`stop`后四维度保存，退出码0、BUILD SUCCESSFUL、ERROR/FATAL0，证据`tmp/plant-supply-final-server.log`。客户端覆盖本批播种、采收、成熟麻破坏掉落、桃苗催化、双格芝麻与相关既有场景；并未覆盖完整树木长成、世界自然生成、新区域自然成长、完整存档重登、多人、完整视觉、来源1.18.2实机或两版本对照。Feature只完成目标注册与configured/placed资源，不等于群系挂载或自然生成。

## 非编号树苗、纤维作物、霓裳草与桃花（2026-11-10）

本批按来源注册ID核对，不把英文枚举名作为玩家核对主键；共享行为类与逐行工厂映射共同承载来源差异。17种树苗由`TreeSapling`逐项登记：`cassia_02`、`laurel_02`、`elm_02`、`privet_02`、`peachsapling`、`sylvestrissapling`、`chestnutsapling`、`beechsapling`、`cypresssapling`、`toothedsapling`、`camphorsapling`、`nanmusapling`、`ginkgosapling`、`maplesapling`、`poplarsapling`、`willowsapling`、`sandalwoodsapling`；每项记录模板、偏移、树干策略和模板分支。共享生长入口每次随机刻/骨粉尝试以5%进入模板放置，排除`fabao_009weidu`与`xvkong`；桃/杨有40%主模板与60%替代模板两条来源分支，月桂保留两个独立概率条件及其余分支。模板偏移、栗树四柱、楠木3×7×3树干和各自木材ID按枚举逐行映射，不合并成通用树冠或单一树干写法。桑苗“需可见天空”的额外来源规则独立于此组。

棉、麻由`FiberCrop`与`FiberGrowth`共用阶段生长。两者按来源阶段有9个成熟阶段块身份；棉05右键恢复04并固定掉棉花、独立30%掉种，麻04玩家破坏掉种并独立30%额外掉麻纤维。霓裳草保留01–06阶段增长率及07终态；来源06阶段没有randomTick/tick覆写，只有骨粉过程调用共享增长过程。目标06因此不接受自然随机刻推进；其骨粉转07与来源召引已按下文“荫翠符催熟与霓裳草开花”闭合，运行边界单独登记。

桃花`peachflower`来源只在下方为空时以20%概率播放粒子，不会把方块转换为桃叶。来源方块继承1.18.2版本原生树叶，放置后调度20刻；每次计划刻先运行父类叶距更新、再执行粒子过程并重新调度20刻，邻块变化亦可能安排叶距计划刻。目标`PeachFlowerBlock`复用`FruitTreeLeavesBlock`的来源叶距、放置持久与无含水交互，再执行自身粒子和重排；父类仍带目标版本含水状态但禁用流体入口。催化物将普通桃苗转为血桃苗的来源操作由树苗方块共用入口承载；不因模板生长代码共享而合并掉各注册ID的映射。

已注册的菌类/豆科成熟地物继续保持独立于自然供给验收的状态。所需群系仍缺`biomeautumn_01–03`、`biomecloud_01`、`biomefeng_01–04`、`biomejade_01–03`、`biomepeach_01–02`、`biometurtle_02`、`biomewind_01–04`，且`dimensionrenjian`、`dimensionyaohun`尚未接入目标World；当前不安装指向缺失Holder的modifier。静态来源、目标构建与冒烟检查已完成；自然生成仍待World依赖接回后验收。

## 野生植物与自然树木地物扩展（2026-11-11）

新增47个非编号普通野生植物RandomPatch Feature与20个模板树Feature。野生植物保留逐ID tries、count、rarity、heightmap、biome及dimension；共用实现只承载源码一致的RandomPatch与维度门控。棉麻和霓裳草的对应野外Feature也在此注册，但不因此补造生长或召兽行为。树木Feature保留逐项模板ID、维度、概率、heightmap、基底、模板锚点偏移；`privet_05`独有随机镜像和旋转，桑树与檀香树使用不同高度图和基底。`treejiegou_*`经模板调色板核对为树木模板地物，不按名字推断为通用结构。

`convert_wild_plant_worldgen.py`与`convert_tree_worldgen.py`由标准`convert_data.py`调用，每份configured/placed资源使用`converter.write`追踪来源Java SHA-1。67个Feature在台账记为“已注册”，表示目标类型与数据已接入，不表示已自然投放：来源群系与`dimensionrenjian`、`dimensionyaohun`等尚未由World提供，故没有添加错误维度或主世界替代modifier。霓裳草06召引已由同步开花事件接入Creature，见“荫翠符催熟与霓裳草开花”。

四种树叶钩子按来源每20刻安排更新；仅在下方为空时以20%概率发送一个粒子，中心坐标、散布和速度均逐项保留。`ginkgoleaves`用`ginkgoleaveslizi`，`mapleleaves`用`mapleleaveslizi`，两种白杨叶都用`poplarleaveslizi`。三种来源LeavesBlock保留父类距离更新，`mapleleaves`继续继承普通Block。

来源`GinkgoleavesBlock`、`PoplarleavesBlock`和`PoplarleavesyellowBlock`均继承1.18.2版本原生LeavesBlock，计划刻先执行父类叶距更新，再执行粒子过程并重排20刻。目标三种粒子叶复用`FruitTreeLeavesBlock`的来源叶距、放置持久和无含水交互，再执行各自粒子和重排；不改变粒子条件、掉落表、群系装配或枫叶。

`mapleresin`接入专用薄片方块：保留含水状态、水平朝向、垂直点击朝北、四向边缘形状、空视觉形状、天空光、旋转镜像及缺少loot时掉落一个`mapleresinitem`。来源与目标源码明细、逐ID参数及哈希见本批月志列出的实现报告和数据转换清单。构建及客户端冒烟通过；专服启动至Done且注册表导出包含枫脂方块和物品，但未取得原生stop/保存退出证据。自然生成、新区块分布、完整树苗成长、枫脂水桶交互与两版本对照尚未验。

## 普通植物地物与水草回调（2026-11-11）

本批16种普通配置型植物Feature与六种自然树木模板保留来源ID，登记“已注册”。生成实现由World的`NativeWorldFeatures`、`DimensionFeature`和`WorldTemplateFeature`按注册ID读取植物方块；具体生成规则只保留于[世界正文](../world/architecture.md#自然供给地物与随机结构2026-11-11)。非植物石团、石柱、斑块、浮岛、温泉等候选依据实际职责移至World，不能按来源目录继承Plant归属。

`RushesBlock`与`RicciafluitansBlock`共用`RushesFangZhiFangKuaiShiProcedure`。目标仅对`rushes`和`ricciafluitans`使用`plant:water_plant`工厂，由`WaterPlantBlock.onPlace`在原生回调后读取上方流体的`createLegacyBlock().getFluidState().isSource()`；成立时读取当前本体状态写`waterlogged=true`，标志3。默认状态仍为false，不把此回调推广到其他水生植物。没有玩家或世界持久字段，World只投放注册方块，Plant负责自身回调。

本批来源文件、SHA256和目标实现见本系统[来源证据](ledger/source-evidence.csv)。完整群系、维度及自然获取未闭合；本轮构建、专服与客户端的实际覆盖和未执行项见[世界月志](../world/logs/2026-11.md)，不以启动回归替代自然生长或两版本对照。

## 荫翠符催熟与霓裳草开花（2026-11-13）

`PlantRipening.ripen(ServerLevel, BlockPos)` 是fulu唯一逐格催熟入口，依来源顺序处理霓裳草、`yvanchuxiuzhen:yvanchuzuowu`、原版 `CropBlock`/`StemBlock`、`minecraft:saplings`。各分支的调度、骨粉次数和无额外音效规则由[符箓正文](../fulu/architecture.md#荫翠符30与植物催熟2026-11-13)统一记录；不让fulu直接修改植物内部阶段。

霓裳草01–05的原生增长率保持不变，06依然没有自然随机刻或计划刻推进。既有骨粉过程的六阶成功分支先发粒子、同步发 `NichangGrassBloomEvent`，再写07；荫翠符则先写07、同步发同一事件，再发粒子，分别保持两个来源过程的实际顺序。事件携带服务端维度与不可变方块坐标，Creature订阅并独占妖兽生成与已有妖兽召引；Plant不引用Creature实现。原06工厂的 `performBonemeal` 待迁移标记已随行为闭合从转换器及生成定义移除。

本次来源过程未出现强类型玩家档案外部访问，开花事件没有玩家附件写入。06方块台账改记“行为已迁移”；野外Feature、自然获取、全部植物生命周期的独立验收边界保持各自行。静态来源核对、必要编译与六项结构检查通过；荫翠符单项六阶段、范围、作物、调度及真实召引样本通过，骨粉开花实机仍未执行。结果统一见[符箓月志](../fulu/logs/2026-11.md#2026-11-13-荫翠符30与霓裳草开花链)。

## 高草碰撞与动画恢复（2026-11-16）

`biggrass`由`BigGrassBlock`保留来源DoublePlantBlock、无碰撞、无掉落、无偏移和燃烧100/蔓延60。`entityInside`调用父类后读取当前世界本格；实体宽或高至少1、未潜行且当前half为lower才先写`biggrassyincang`，再读取本格动画属性写1，均为flags3。保留两端调用入口，不新增服务端门禁；只接触上半株、潜行或宽高都小于1时不切换。普通方块与动画方块都保持“手持物不是自身方块物品时可替换”的来源判定。

`AnimatedBigGrassBlock`保留动画状态0..2与30刻计划更新。到刻依次清当前格、在上格放普通高草默认态、读取上格half并尝试改upper、最后在当前格放普通高草默认态，再为旧动画方块安排30刻；不预检空间或土壤、不调整写入顺序。后一次计划是否执行仍由原生方块身份检查负责。拾取动画块得到普通高草，空loot回退AIR；来源类没有库存破坏掉落覆写，因此不根据编辑器库存选项补造回收。

`AnimatedBigGrassBlockEntity`独占九槽库存、64堆叠与Items/LootTable原生保存。六面允许全部槽插取；无方向查询不暴露物品能力。提供者仍返回独立的三行箱子菜单，菜单容器没有连接九槽，方块也没有右键开菜单入口。30刻恢复会以普通高草替换临时实体；不能把它按持久家具的重登库存场景验收。来源过程没有强类型玩家字段访问，本族不新增跨系统持久数据写入。

方块实体和隐藏方块物品使用来源`big_grass_03`几何、数字动画与`big_grass03`贴图，材质为entityTranslucent。闲置动画0循环，非零动画按来源PLAY_ONCE与渲染端本地复位顺序执行；物品控制器不主动选择动画。来源动画资源只有0/1，保留状态范围2而不补动画。物品parent链为`item/biggrassyincang`→`displaysettings/big_grass_03.item`→`builtin/entity`，保留七种手持/展示变换；中文名为“高草”，隐藏物品没有创造栏。

自然入口复用已接`WildPlantFeatures`及主线`biometiger_02`群系的`yvanchuxiuzhen:biggrass`引用；来源只允许妖魂道，RandomPatch tries64、Count20、InSquare、`PlacementUtils.HEIGHTMAP_WORLD_SURFACE`、Biome，目标高度图为`WORLD_SURFACE_WG`。已有Feature源码证据直接复用，本批只补方块生命周期及转换输入，不重复创建World生成资源。静态接线不代表新区块自然获取通过。

来源路径与SHA256记在本系统来源证据及指纹表。本批静态核对覆盖内容唯一性、来源基础属性、资产引用闭包、汉化和自然入口；`BigGrassCheck`提供碰撞门槛、六面/无方向能力、九槽物品组件NBT读回、独立菜单、自然30服务端刻恢复和双格破坏必要场景，已接入共享biggrass选择器但未执行。标准转换隔离输出、集中编译和六架构检查通过；高草定向目标客户端、来源1.18.2版本实机、真实退出重进、多人及完整视觉/两版本对照均未执行；具体结果见[本月记录](logs/2026-11.md#2026-11-16-高草完整生命周期)。

## 反应野外方块（2026-11-16）

`ReactiveWildBlocks`按来源五种基类装配27个ID，内容表与`factory_overrides.csv`逐项对应；已接回的回调不继续保留pending_hooks。普通方块：huaguang、barrenstonesting、snowdrift、moeye；双格：iceboundrock、tailbladegrass；花型：opticalprism、cannibalmoss、cannibalmoss_02、bitegrass_01、bitegrass_02、thundergrass、thistlesandthorns、lurker、cloudgrass_01、cloudgrass_02、soulgrass、bladegrass、peachsaplingxve；树叶：sandalwoodleaves；含水型：ironrope及witheredbone_01..06。

华光把速度设为(0,0.2,0)；棱镜发送15粒子、声音后无掉落销毁；食人苔切02，02施加20刻20级缓慢；咬草01造成5点通用伤害并切02；荒石刺/荆棘伤害4；潜伏者伤害4并加1200刻1级缓慢；云草01发40云粒子切02；魂草发2魂粒子；刀草/尾刀草分别伤害8/24后销毁；六种枯骨播放骷髅声。02食人苔/咬草/云草在随机刻委派或计划刻恢复，雷草同一路径在水平±3格生成真实闪电。血桃苗同一路径排除画卷与虚空，以独立5%抽样先发粒子、请求桃木精、再置空气；没有新增计划刻排程。

雪堆空手和持物原生右键均生成一枚owner为玩家的雪球；普通瞄准垂直量0.1，潜行按视线，速度1/散布1。冰封岩玩家破坏先查主副手精准采集，任一非零不生成；否则各else-if独立抽样，依次0.142/0.166/0.2/0.25/0.333/0.5选择蝙蝠/蜜蜂/猫/鸡/狐狸/兔，仍可能无动物。铁索开始挖掘发火粒子，主手非正确工具时写玩家自由NBT`manaattackbegin=true`并造成5点火伤；此自由NBT不是强类型玩家档案，不添加其他系统字段写入。檀叶破坏按独立else-if抽样额外掉叶10%、苗10%、双苗5%，双苗保持两个独立物品实体。

魔眼玩家破坏同步发布`WildPlantCreatureEvent(DEMON_EYE,玩家)`；血桃苗发布`BLOOD_PEACH`。Plant只提供事实与坐标；Creature订阅后创建已注册moshou_003/taomujing，按来源位置、独立朝向及MOB_SUMMONED初始化加入世界。魔眼在初始化前设置玩家目标。此接口无持久玩家档案写入，不引入Plant对Creature实现的依赖。

### 人间道引雷草掉落（2026-11-19）

1.18.2版本`ThundergrassBlock.getDrops`先调用父类掉落；非空结果原样返回，空结果回退一株自身物品。1.21.1版本`ReactiveWildBlocks.ReactiveFlower`只对注册路径`thundergrass`保留同一分支，其他反应植物的掉落结果不受影响。实现使用`Block.getDrops`的NeoForge入口承接原生战利品上下文，不绕过非空loot。

静音客户端Smoke的`thundergrassdrops`单项已通过，直接调用方块掉落函数确认空loot得到一株引雷草；该结果不等价于真实生存采收、非空自定义loot优先、自然供给、织布机交互、保存重进、多人/专服、1.18.2版本实机或两版本对照验收。

### Q218 五种反应植物的随机刻委派差异

2026-11-17已核实来源机制，修复已实现，集中编译与目标受控必要单项通过；此前“没有scheduleTick因而自然入口未证实”的结论失效。锁定Forge 40.1.0映射源码包中，`BlockBehaviour.java:336–337`的`randomTick`直接调用`this.tick`；`Block`、`BushBlock`和`FlowerBlock`均无中间覆写。Cannibalmoss02Block:63、Bitegrass02Block:46、ThundergrassBlock:71、Cloudgrass02Block:64、PeachsaplingxveBlock:62覆写tick并声明randomTicks，因此均有来源随机刻入口，不依赖显式scheduleTick。精确NeoForge 21.1.265的`BlockBehaviour.java:384–385`已为空实现；目标`ContentFlower`也无委派，需要由`ReactiveWildBlocks.ReactiveFlower.randomTick`显式调用tick，恢复上述来源调用链。这是版本机制适配，不是新增玩法或待裁定调整。

受影响注册ID恰为`cannibalmoss_02`、`bitegrass_02`、`cloudgrass_02`、`thundergrass`和`peachsaplingxve`。三种02状态恢复、雷草真实闪电与血桃苗5%召引的原有执行顺序、概率和维度限制保持；没有添加计划刻排程，也没有调整随机刻频率。关键消费者来源为MoeyeDangFangKuaiBeiWanJiaPoPiShiProcedure:18、PeachsaplingxveGengXinYouXiKeProcedure:31与IronropeDangWanJiaKaiShiPoPiShiProcedure:19；冰封岩精准采集同时检查双手。

旧`reactiveplants`报告保留为历史：正式右键/破坏、碰触状态伤害、双手精准采集抑制和人工scheduleTick的有限结果仍有效，其中“randomTick不转调”的断言是失效规格，不再作为行为对齐证据。当前夹具已更正三种02的受控随机刻恢复和雷草闪电断言，新增血桃苗有限随机刻成功样本；计划刻检查暂时关闭自然随机刻后执行，结束恢复原规则值。夹具已通过集中编译与reactiveplants必要单项，直接调用randomTick不代表自然新区块等待、完整供给或概率分布验收；概率掉落/普通冰封岩随机分支、来源版本实机、真实保存重进、多人和完整视觉仍未验。实际结果与原件位置见[本月更正记录](logs/2026-11.md#2026-11-17-反应植物随机刻委派更正)。

## 植物物品扩展：堆肥与燃料（2026-11-18）

锁定1.18.2版本的94个`itemextension`元素与生成的`YvanchuxiuzhenModFuels`、`YvanchuxiuzhenModCompostableItems`总表逐项一致：52条燃料、43条堆肥，涉及93个实际物品。两类扩展同属Plant规则，物品身份分别归Plant、Cooking、Material和Decor；只按稳定物品ID声明原生消费数据，不调用其他系统内部实现或修改持久状态。`typed_field_accesses.csv`无本规则的外部强类型字段读写。扩展元素不另造注册身份，既有内容台账只补受影响物品的说明，状态不因一项扩展而整项升级。

精确NeoForge21.1.265的`ComposterBlock.getValue`只读取物品holder上的`NeoForgeDataMaps.COMPOSTABLES`，已不查询弃用的静态`COMPOSTABLES`表；`IItemExtension.getBurnTime`读取`FURNACE_FUELS`，原生熔炉通过`ItemStack.getBurnTime(recipeType)`消费。两份资源因此写入`data/neoforge/data_maps/item/compostables.json`与`furnace_fuels.json`；此处`neoforge`是加载器定义的数据映射ID，全部自定义物品键仍为`yvanchuxiuzhen`。`replace=false`保留其他映射项，来源燃料值没有配方类型、NBT或状态条件，逐物品原值转换。`convert_plant_item_extensions.py`独立解析两份总表和相应注册符号，拒绝未识别调用、重复项与未登记物品；标准数据转换的共享接线随精确补丁交主线。

燃料时长为40项1600刻、10项300刻，以及焦木`scorchedwooden`4800刻、远古木`ancienttimeswood`20000刻。300刻组为远古叶`ancienttimesleaves`、枫脂`mapleresinitem`和八种木板。堆肥概率为38项0.3、桂花`laurel_04`与桃花`peachflower`0.6、林桃`peachpeach`0.65、远古叶与银杏果`ginkgofruit`1.0。远古叶、枫脂各有两种扩展；原生首层对非零概率必定成功，后续仍按原生随机概率推进，不另写堆肥算法。

全部94项均未启用发射器行为。`Elm05ranliao`的燃料开关为false、堆肥概率为0，生成总表无对应项；保留该无动作定义，不据此强制其物品燃烧时间为0。`Cassia03ranliao`虽以ranliao命名，实际仅堆肥0.3，不按名称补燃料。来源`WorkAtComposter`只选择小麦种子和甜菜种子，因此本批映射明确`can_villager_compost=false`，不扩展村民自动堆肥材料。

本批对94份元素与四份来源Java保留SHA256/字节数，并核对精确1.18.2与1.21.1框架入口；标准转换生成的两份JSON与来源表、冻结数据逐项语义一致，来源manifest只追加本批6条记录。转换前保护已有生成物，首次因独立树缺精确目标源码Jar而中断；恢复部分输出并补齐前置后成功，其他系统数据和语言副作用均按原件恢复，详情见[本月记录](logs/2026-11.md#2026-11-18-植物物品扩展堆肥与燃料)。

`compileJava compileSmokeJava`及`PlantItemExtensionsCheck`必要客户端单项通过，报告30 INFO、0 FAIL、DONE，所有维度正常保存、Gradle退出0。受控样本覆盖四档燃料在熔炼与null配方参数下的读取、五种堆肥值、原生顶部输入/侧面拒绝、首层与七层、20刻成熟后底部取骨粉及归零，真实等待至少340服务端刻后核对300刻燃料只完成一份圆石熔炼。数据映射全量静态核对与有限机器运行分别登记，不据此提升整项物品状态。两份最终合并中文各93个名称与来源逐值一致；本批没有新增玩家界面或渲染，无截图验收结论。数据重载、正式玩家点击、真实退出重进、完整自然供给、多人、专服、来源1.18.2版本实机和两版本对照仍未执行。

## 野生植物土壤命名空间（2026-11-18）

八种来源方块的`mayPlaceOn`明确比较指定Mod土壤，`canSurvive`再调用该判定；高铁竹草的上半株单独要求下方为自身下半株。原版土壤仍按来源完整列表及原顺序保留，共51条土壤引用，其中九条为以下Mod引用：

| 方块 | 来源类及mayPlaceOn行 | 允许的Mod土壤 |
| --- | --- | --- |
| `scorchedgrass`烧焦的草 | `ScorchedgrassBlock:57` | `scorchedsoil` |
| `thundergrass`引雷草 | `ThundergrassBlock:58` | `scorchedsoil` |
| `thistlesandthorns`荆棘 | `ThistlesandthornsBlock:62` | `loess` |
| `ironbamboo`铁竹草 | `IronbambooBlock:59` | `loess` |
| `ironbambootail`高铁竹草 | `IronbambootailBlock:55` | `loess` |
| `tripterygiumwilfordii`雷公藤 | `TripterygiumwilfordiiBlock:58` | `loess` |
| `soulgrass`阴魂草 | `SoulgrassBlock:50` | `loess`、`youbosoil` |
| `corpsegrass`尸草 | `CorpsegrassBlock:38` | `rottingsoil` |

`Source.block_const`映射值为裸注册路径。生产转换器`legacy_blocks._parse_plant`此前直接将Mod引用写入ground，而平台`BlockTraits.mayPlaceOn`按完整资源位置读取。精确NeoForge21.1.265源码中，`ResourceLocation.parse`对裸路径采用`minecraft`命名空间；默认方块注册表对未注册键返回空气。因此原数据会拒绝实际Mod土壤，且把空气误列为可存活地面，影响物品放置、邻居存活与`SimpleBlockFeature`的前置存活判断。

本批在转换器复用`build_ledger.NAMESPACE`为Mod土壤输出完整ID，并同步八个内容行的九处引用。原版ID、来源顺序、工厂、属性、效果和掉落不变；平台仍按原有完整ID规则读取。四种土壤当前由Decor注册，本批不改变其数据或所有者，也不引入跨系统持久化写入。原生双格生命周期及既有反应回调继续由对应实现承担。

八种植物的SIMPLE_BLOCK内层、来源Feature指纹和现有configured/placed资源已静态复核。人间道的烧焦草/引雷草及妖魂道的四种植物已有当前群系消费者；阴魂草/尸草所需无归道与六个群系尚未接收，本批不据此提升自然供给结论。台账只追加八个方块与八个Feature的修复说明，保持原状态，不把局部数据更正等同于整项验收。

`PlantGroundCheck`受控覆盖来源Mod土壤存活、原版土壤样本、拒绝错误Mod土壤与空气、SIMPLE_BLOCK内层投放/悬空拒绝、高铁竹草双格存活、原生生存物品放置/扣一及失去土壤后的邻居更新。夹具保存并恢复主手、模式与三格方块/方块实体；不运行自然斑块或绕过维度门。18份来源、35份活动依赖、10份精确目标API与实际合并中文已核对，四份系统文档和相关证据同步。标准`convert_content.py --owner plant`成功生成八行51条地面引用，与冻结包及来源原顺序一致；其他物品/语言副作用已保护恢复。生产/Smoke编译与一次`plantground`必要客户端单项通过，79项功能断言及两项公共检查共81 INFO、0 FAIL、DONE，所有维度保存且Gradle退出0。两份最终合并中文各8键逐值一致；本批只改土壤引用，没有新增界面或渲染，不作截图验收结论。自然新区块、完整获取、真实保存重进、多人、专服、1.18.2版本实机和两版本对照均未执行，原件与日志分类见本月记录。

## 柳树垂枝原位替换（2026-11-18）

来源`A:block/WillowdroopingBlock.java:51–53`明确覆写`canBeReplaced`，只比较当前放置上下文手中物品是否为自身方块物品；`elements/Willowdrooping.mod.json`同时声明`isReplaceable=true`。因此手持其他物品及空手上下文允许替换，手持柳树垂枝自身物品时不替换当前格，原生放置流程转用点击面的相邻格。基础材质仍为LEAVES，不能把整类叶片标成可替换或仅靠材质默认值代替这个入口。

当前活动集成的通用`ContentBlock`未覆写此方法，LEAVES材料映射的replaceable为false；NeoForge21.1.265精确`BlockBehaviour.java:293–295`的默认判定先要求此标志，导致原位替换被拦截。目标新增`WillowDroopingBlock`及`plant:willow_drooping`工厂，覆写同一上下文物品身份判定；内容表其余字段与方块物品沿用来源身份，不新增持久字段、掉落或客户端文案。

已有供给消费者为`biomeautumn_03`中的`tree_biomeautumn_03`：两种树装饰器均生成`willowdrooping`。普通`willowsapling`的随机刻、计划刻和骨粉经`TreeSaplingGrowth`按原概率生成`tree_jiegou21`，模板含垂枝；两份柳树模板与来源解压后字节一致。柳叶原生战利品表仍能产出柳苗。这些消费者已静态核对，不据此登记自然新区域或成树验收。

受影响台账仅柳树垂枝方块一行，状态仍为“已注册”；物品扩展包中的30%堆肥独立接收，不作为替换路径依赖。原生受控夹具`WillowDroopingCheck`覆盖其他方块原位放置/扣一、自身物品相邻放置/扣一、副手上下文及空手判定。标准`convert_content.py --owner plant`转换、`compileJava compileSmokeJava`及一次`willowdrooping`客户端单项通过：八项功能断言均通过，报告11 INFO、0 FAIL、DONE，所有维度保存且Gradle退出0。实际合并中文“柳树垂枝”已核对；本批没有新增界面或渲染，未作截图验收。真实保存重进、自然新区块/成树获取、多人、专服、1.18.2版本实机和两版本对照未执行。

## 尸草原位替换（2026-11-18）

来源`A:block/CorpsegrassBlock.java:33–35`覆写`canBeReplaced`，比较当前放置上下文手中物品与自身物品；编辑器`Corpsegrass.mod.json`声明`isReplaceable=true`。手持其他物品或空手上下文允许原位替换，自身物品拒绝原位替换。目标通用ContentFlower继承精确NeoForge21.1.265的默认判定，而PLANT材质replaceable=false，会先阻断该入口。新增`CorpseGrassBlock`及`plant:corpse_grass`工厂，只补回此覆写；炖菜效果、腐土存活、无掉落、偏移和客户端资产沿既有数据。

同一已存在回调清单中的宫廷草`diplidisdiandra`也有同型覆写，但目标`REPLACEABLE_WATER_PLANT`材质replaceable=true，默认判定在现有物品身份下与来源等价，保留当前含水方块实现。柳树垂枝的同型缺陷已经独立修复，规则见[垂枝正文](#柳树垂枝原位替换2026-11-18)。

尸草土壤命名空间由[八种野生植物土壤](#野生植物土壤命名空间2026-11-18)独立修复；本包只改factory字段，接收时保留当前ground。现有尸草Feature/configured/placed已静态复核，无归道及其三个来源群系仍未接收，自然获取不因原位替换修复而闭合。一个方块台账仅追加说明，不提升整项状态；来源类/编辑器两条证据和指纹同步。

`CorpseGrassCheck`经原生服务端放置入口核对主手石头原格替换/扣一、副手上下文原格替换/只扣副手、手持尸草拒绝原位替换及空手判定，七项受控检查通过。夹具保存并恢复双手、模式、九格方块及方块实体，不制造自然来源或存活验收。四份来源、九份活动代码/资源、精确目标默认方法及实际合并中文“尸草”“宫廷草”已核对，未新增玩家文本。标准转换、生产/Smoke编译与本项受控客户端通过，详细结果见[本月记录](logs/2026-11.md#2026-11-18-尸草原位替换)；真实保存重进、自然新区块/获取、多人、专服、1.18.2版本实机及两版本对照未执行。本批无界面或渲染改动，未作截图验收。

## 无归两草生成消费者（2026-11-18）

World接回无归实际十群系后，阴魂草在三百川、尸草在尸绝01的正式群系引用进入功能树。两草均保持来源patch64、xz7/y3和AIR内层谓词，外层count分别8/16、InSquare、FULL_RANGE及BiomeFilter；阴魂草15地面白名单、尸草仅腐土及既有原位替换规则保持。weakness/nausea1200刻是Flower炖菜效果；阴魂草碰触仅服务端SOUL两粒、中心+.5、spread1/.5/1、speed0，没有碰触药水效果。来源没有非空loot，保持无拾取物。

一次限定`wugui,wuguiportal`受控客户端实际调用两草正式configured，在15×15幽薄土/腐土分区生成阴魂草7格、尸草12格，逐格确认存活和空掉落；两草及支撑土在真实退出重进后保持，随后恢复环境。两草和土壤截图已人工查看，仅代表受控样点；原点一块FULL自然样本两草计数不作为自然供给率证据。金色板块/魔眼结构两Feature保留Plant台账身份，规则由[World无归正文](../world/architecture.md#无归十群系与门户接收2026-11-18)唯一维护。金色板块只验实际类型和主世界拒绝，魔眼模板已验受控生成与保存；自然生成率及魔眼玩家破坏至Creature的完整路径未重放。

既有土壤ground、尸草替换和五土壤必要单项未受本次规则影响，复用原证据。本次不代表两草自然获取、全部视觉、完整生命周期、多人/专服或1.18.2版本实机/两版本对照通过；详细记录见[本批月志](logs/2026-11.md#2026-11-18-无归两草消费者与两地物接收)。

## 五种来源土壤支持与掉落回退（2026-11-18）

锁定1.18.2 版本的`ScorchedsoilBlock`、`LoessBlock`、`YoubosoilBlock`、`RottingsoilBlock`和`GrassblockBlock`均覆写`canSustainPlant`并返回true；前四类见32–34行，草块见40–42行。来源调用者仍有独立前置条件，不能把这个返回值直接扩为目标所有植物或所有存活条件。五个ID、方块物品、来源属性、资源和grassblock草色继续归Decor；其`decor:plant_soil`工厂委托`PlantSoilSupport.create`，Plant独占种植规则及专用方块实现，不移动注册归属。该入口没有玩家强类型持久字段读写，`typed_field_accesses.csv`无对应外部访问。

`SourcePlantSoilBlock`只对来源原版Bush族的53种实际身份（含仙人掌和甘蔗）以及本Mod的Bush族提供TriState.TRUE，其余身份留DEFAULT。目标新增的竹、可可、紫颂、垂滴叶等土壤查询不据此新增来源没有的支持，也不向通用dirt标签添加成员。对于已有显式ground的18种Mod植物，`ContentFlower`和`ContentDoublePlant`先执行来源白名单，五土壤交叉90对仅九对允许；四种双株的上半株仅承认自身下半株。无显式ground继续原生存活路径，已有反应花型与尸草继承该规则。

精确NeoForge 21.1.265将部分土壤查询提前返回，须定向保留来源调用者的其余条件：作物仍要求亮度至少8或可见天空；蘑菇仍要求专用生长基底标签或亮度小于13；仙人掌仍拒绝水平实体/熔岩并要求上方非液体，甘蔗保留来源五土壤支持下无邻水的路径。三个RETURN Mixin仅作用于这五种土壤及已支持植物。生长速度另以土壤支持计算，不能在土壤钩子硬编码光照；五土壤非肥沃，原生3×3贡献保持普通土壤的1.0。

成熟南瓜/西瓜藤的果实落点额外接受这五种土壤。`StemSoilFruitMixin`只桥接randomTick中唯一的is(TagKey)检查，原调用结果优先，来源支持作为补充分支；未增加通用标签成员。主接收已用精确目标制品javap确认调用描述符为`BlockState.is(TagKey):boolean`，运行应用及原生成熟结瓜由本批必要夹具验证，不降级为静默忽略。

五行已有`fallback_drop`数据此前没有Java消费者且缺少对应战利品表。新专用方块先返回非空原生战利品结果；为空时四种土壤各返自身一件，grassblock返minecraft:dirt一件，分别对应来源37–42行和45–50行。其他方块不继承本回退，也不以固定表覆盖来源的非空战利品优先规则。Decor草色注册及绑定保持原指纹，本批没有渲染修改。

标准`convert_content.py --owner decor`已接收五行factory及已处理pending_hooks，其他生成副作用按转换前字节恢复。生产/Smoke编译、六项架构检查与一次`PlantSoilsCheck`必要客户端通过；327项功能检查覆盖本批上述入口，10组南瓜/西瓜均在seed0的第六次原生randomTick结瓜并连接，石头256次对照未结瓜。完整结果、源码指纹及未验边界只在[本月记录](logs/2026-11.md#2026-11-18-五种来源土壤支持与掉落回退)登记。真实保存重进、自然生成/获取、非空自定义战利品注入、grassblock草色画面、多人/专服、1.18.2 版本实机和两版本对照均未执行。

## 成熟双格谷物地物谓词对齐（2026-11-19）

锁定1.18.2版本 `Shicai001Plant08Feature`、`Shicai002Plant08Feature`、`Shicai004Plant08Feature`、`Shicai005Plant08Feature`、`Shicai006Plant08Feature` 的 `simplePatchConfiguration` 使用空放置谓词列表。目标原五份configured feature多出 `matching_blocks: minecraft:air`，会排除来源允许的落点；现已移除，并在 `convert_plant_worldgen.py` 中对双格成熟作物生成空 `placement`，避免转换重跑回退。该修正仅覆盖五种成熟双格谷物，其他成熟Feature仍需逐族核对；未运行自然新区块、玩家采收、保存重进、专服或两版本对照。
