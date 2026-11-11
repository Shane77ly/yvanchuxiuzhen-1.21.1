---
topic: plant
role: architecture
status: draft
updated: 2026-11-10
---

# 植物：架构调查与提案

来源事实与目标实现分别陈述。本批按已批准的系统职责接回来源行为，其余目标方案仍为草案；源码定位采用 [系统导航](../README.md)的 A/B 规则。

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

桑树野外mulberry_02的来源规则已核但本批不实现Feature：仅dimensionrenjian/dimensionyaohun与biomewind_02、biomeautumn_01、biomefeng_02；每次机会随机整数1..1000000<=50000，nextInt(1)+1恒1次，x/z各nextInt(16)，WORLD_SURFACE_WG-1，允许下方grass_block/dirt_path/mycelium/dirt/coarse_dirt/podzol/rooted_dirt；模板mulberry_02锚点(-4,+1,-4)、NONE、AIR忽略、保留实体、flags2。两结构已有转换身份，不代表自然链闭合；Mulberry02元素仍保留原归属状态，源码分支证据与实现状态分开。

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

28个已接成熟地物使用同一个`CropFeature`保留两维度限制；001/002既有CerealFeature及其他族的等价候选类合并，不复制完全相同实现。新增26种各有configured/placed两份数据，由`convert_plant_worldgen.py`经标准`convert_data.py`生成并追踪各自来源Java SHA-1。参数默认RandomPatch tries1/xz7/y3、simple_block成熟默认态与空气过滤；双格成熟含half=lower；外层Count1→Rarity32→InSquare→MOTION_BLOCKING→Biome。023为tries4/count1，032为tries2/count2。仅dimensionrenjian/dimensionyaohun；未装任何替代主世界或指向缺失群系的modifier。以下列出新增地物的来源群系（均省略`biome`前缀），供World后续装配；008/015没有本批成熟地物。

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

藤本逐号保留来源异常：03504右键回到03并掉一份食材；03703虽有共享采摘分支但方块未绑定use；038的自然生长过程实际读写035阶段，因而038自然不推进；03803绑定的use过程不匹配成熟阶段，返回成功但无变化。03902/03由计划刻每40游戏刻重复调度，不使用随机刻；039成长分支保留来源西向上层分支误写EAST的结果。03904右键只在动画1/2之间切换；九槽方块实体保存物品并提供来源能力，菜单提供器仍返回独立27槽菜单，不把该菜单当作葫芦库存界面。

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

040/041为可贴附木面的菌类，可附着于原木标签方块、菌丝或灰化土；042–045为菌丝或灰化土支撑的地生菌类。阶段与掉落由`FungusCrop`按编号参数化，不为每个编号重复创建Java类。来源各阶段掉落量为：040/041分别1、2、3、5；042为1、2、3、4、5；043–045为1、1、1、3。骨粉可使贴附型菌类直接成熟，042推进一阶段；来源成功率分支予以保留。目标客户端场景覆盖两种播种方式、部分骨粉路径及040–045成熟掉落。Forge 40.1.0随机刻基类委派尚未精确核实，因此不能据此认定所有刻更新路径均已验收。

### 046–056 豆科规则

共用生长过程保留各编号的阶段分支。普通作物046–051、054–056在耕地播种；支架作物052/053在泥土或草方块播种，生长时复制朝向和含水状态。来源共享物品过程在052/053的耕地分支会扣除一份食材但不放置作物，目标保留此结果。04804声明随机刻，但其来源randomTick分支为空操作：湿度达标时不推进，低湿度也不移除；来源独立的右键采摘分支仍保留。052/053采摘时生成三个独立物品实体并恢复到来源阶段；05304无普通掉落，05504未绑定右键采摘，且仅01–03阶段接受骨粉。这些差异逐项记录于`LegumeCrop`、`LegumeGrowth`和编号对照表，不从相邻编号推断。

来源的七个地物分别对应046/047/048/051/054/055阶段04与056阶段05；目标地物类型及configured/placed资源均已注册。049/050/052/053没有对应来源地物。World尚未接入所需维度与群系，故群系modifier及自然供给未闭合；地物注册或资源存在不等于已验证自然生成。

### 057/058 芝麻供给边界

05701为双格植株。来源战利品池独立抽取1–2次，每次以相同权重在黑芝麻`shicai_057`与白芝麻`shicai_058`间选择，数量1–6，并应用爆炸衰减与时运。来源放置地物位于`dimensionrenjian`与`dimensionyaohun`的七个群系；目标类型及configured/placed资源已注册，但World尚未安装群系modifier，也未验证自然供给。来源覆写`getPlantType`返回`PLAINS`；1.21.1目标使用的NeoForge 21.1.265源码中已无旧Forge `IPlantable`/`PlantType` API，因此不以未实现旧接口查询作为目标行为差异，需按目标双格方块实际土壤放置结果核对兼容行为。058没有独立作物或生成入口；四份白芝麻加工一份`tiaowei_08`的石磨消费归Craft。

### 本批验证边界

最终目标版本构建`build compileSmokeJava`通过；`runSmokeClient -PsmokeOnly=fulucooking`为343项INFO、0 FAIL、DONE，客户端四维度正常保存退出，ERROR/FATAL0。报告与日志见`build/smoke/report.txt`、`tmp/plant-supply-final-build.log`、`tmp/plant-supply-final-smoke.log`及[本月日志](logs/2026-11.md)。专服原生`stop`后四维度保存，退出码0、BUILD SUCCESSFUL、ERROR/FATAL0，证据`tmp/plant-supply-final-server.log`。客户端覆盖本批播种、采收、成熟麻破坏掉落、桃苗催化、双格芝麻与相关既有场景；并未覆盖完整树木长成、世界自然生成、新区域自然成长、完整存档重登、多人、完整视觉、来源1.18.2实机或两版本对照。Feature只完成目标注册与configured/placed资源，不等于群系挂载或自然生成。

## 非编号树苗、纤维作物、霓裳草与桃花（2026-11-10）

本批按来源注册ID核对，不把英文枚举名作为玩家核对主键；共享行为类与逐行工厂映射共同承载来源差异。17种树苗由`TreeSapling`逐项登记：`cassia_02`、`laurel_02`、`elm_02`、`privet_02`、`peachsapling`、`sylvestrissapling`、`chestnutsapling`、`beechsapling`、`cypresssapling`、`toothedsapling`、`camphorsapling`、`nanmusapling`、`ginkgosapling`、`maplesapling`、`poplarsapling`、`willowsapling`、`sandalwoodsapling`；每项记录模板、偏移、树干策略和模板分支。共享生长入口每次随机刻/骨粉尝试以5%进入模板放置，排除`fabao_009weidu`与`xvkong`；桃/杨有40%主模板与60%替代模板两条来源分支，月桂保留两个独立概率条件及其余分支。模板偏移、栗树四柱、楠木3×7×3树干和各自木材ID按枚举逐行映射，不合并成通用树冠或单一树干写法。桑苗“需可见天空”的额外来源规则独立于此组。

棉、麻由`FiberCrop`与`FiberGrowth`共用阶段生长。两者按来源阶段有9个成熟阶段块身份；棉05右键恢复04并固定掉棉花、独立30%掉种，麻04玩家破坏掉种并独立30%额外掉麻纤维。霓裳草保留01–06阶段增长率及07终态；来源06阶段没有randomTick/tick覆写，只有骨粉过程调用共享增长过程。目标06因此不接受自然随机刻推进；其骨粉转07分支和粒子已接入，但来源`NichanggrassyaoshouProcedure`触发的妖兽召引尚待Creature系统闭合，不能将06登记为完整行为迁移。

桃花`peachflower`来源只在下方为空时以20%概率播放粒子，不会把方块转换为桃叶。目标`PeachFlowerBlock`按20刻调度叶距检查并执行同一条件粒子行为。催化物将普通桃苗转为血桃苗的来源操作由树苗方块共用入口承载；不因模板生长代码共享而合并掉各注册ID的映射。

已注册的菌类/豆科成熟地物继续保持独立于自然供给验收的状态。所需群系仍缺`biomeautumn_01–03`、`biomecloud_01`、`biomefeng_01–04`、`biomejade_01–03`、`biomepeach_01–02`、`biometurtle_02`、`biomewind_01–04`，且`dimensionrenjian`、`dimensionyaohun`尚未接入目标World；当前不安装指向缺失Holder的modifier。静态来源、目标构建与冒烟检查已完成；自然生成仍待World依赖接回后验收。

## 野生植物与自然树木地物扩展（2026-11-11）

新增47个非编号普通野生植物RandomPatch Feature与20个模板树Feature。野生植物保留逐ID tries、count、rarity、heightmap、biome及dimension；共用实现只承载源码一致的RandomPatch与维度门控。棉麻和霓裳草的对应野外Feature也在此注册，但不因此补造生长或召兽行为。树木Feature保留逐项模板ID、维度、概率、heightmap、基底、模板锚点偏移；`privet_05`独有随机镜像和旋转，桑树与檀香树使用不同高度图和基底。`treejiegou_*`经模板调色板核对为树木模板地物，不按名字推断为通用结构。

`convert_wild_plant_worldgen.py`与`convert_tree_worldgen.py`由标准`convert_data.py`调用，每份configured/placed资源使用`converter.write`追踪来源Java SHA-1。67个Feature在台账记为“已注册”，表示目标类型与数据已接入，不表示已自然投放：来源群系与`dimensionrenjian`、`dimensionyaohun`等尚未由World提供，故没有添加错误维度或主世界替代modifier。霓裳草06召兽仍依赖Creature公开召唤入口。

四种树叶钩子按来源每20刻安排更新；仅在下方为空时以20%概率发送一个粒子，中心坐标、散布和速度均逐项保留。`ginkgoleaves`用`ginkgoleaveslizi`，`mapleleaves`用`mapleleaveslizi`，两种白杨叶都用`poplarleaveslizi`。三种来源LeavesBlock保留父类距离更新，`mapleleaves`继续继承普通Block。

`mapleresin`接入专用薄片方块：保留含水状态、水平朝向、垂直点击朝北、四向边缘形状、空视觉形状、天空光、旋转镜像及缺少loot时掉落一个`mapleresinitem`。来源与目标源码明细、逐ID参数及哈希见本批月志列出的实现报告和数据转换清单。构建及客户端冒烟通过；专服启动至Done且注册表导出包含枫脂方块和物品，但未取得原生stop/保存退出证据。自然生成、新区块分布、完整树苗成长、枫脂水桶交互与两版本对照尚未验。
