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

`A:item/Shicai001seedItem`调用共享种植过程的001分支：只在原版FARMLAND、UP面且上方空位放阶段01、扣1；useOn始终SUCCESS，创造模式物品恢复由目标原生外层处理。Plant写入作物，食材身份保持Cooking；`A:Shicai001seedhecheng`和既有目标配方均为4颗种子合成1份`shicai_001`，不改方向或产量，配方归属候选本批未裁定。

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

| 来源编号 | 身份绑定 | 工厂与实现 |
| --- | --- | --- |
| 001 水稻 | `CerealCrop.SHICAI_001` | `plant:shicai001_01..08` / `plant:shicai001_seed`，阶段类及CerealGrowth |
| 002 糯稻 | `CerealCrop.SHICAI_002` | `plant:shicai002_01..08` / `plant:shicai002_seed`，阶段类及CerealGrowth |

两编号都先排除UPPER；土壤moisture缺失为-1、低于7掉落后销毁，否则20%推进。04→05先上方默认LOWER、再按当时世界状态改UPPER、最后当前LOWER；05→08先依次清当前与上方，再同样三次写入，无空间预检。单格01–04和双格05–07保留计划刻与随机刻钩子。成熟LOWER空loot回落本编号6种子，UPPER无掉落；其余阶段fallback本编号1颗。种子只UP+原版耕地+上方空位，useOn总SUCCESS；创造消耗恢复由原版调用外层承担。

两编号01–07骨粉有效且50%成功，概率失败仍由原生扣1。001按当前世界状态复制兼容属性再写成熟；002只写默认成熟下格、默认上格、再尝试上格UPPER，不复制原属性。均不归一上半格，不补空间/土壤预检；共享类只用于这两条已核规则，不将其套到其余作物。

002成熟地物类型复用相同的两维度过滤：renjian/yaohun；configured/placed参数与001同为patch1/xz7/y3、Count1→Rarity32→InSquare→MOTION_BLOCKING→Biome。002只列autumn01/02，001另含autumn03。两个编号的JSON各从其Feature来源生成并记录各自SHA-1；World维度/群系未完成前都不安装modifier，自然新区域未验收。002四种子转1糯米的配方属于Cooking食材加工，不改变Plant种子所有权；配方、物品类及编辑器定义另录Cooking证据。

糯稻002目标运行证据：完整`build compileSmokeJava`通过（4m31s，含既有test/check与ArchUnit）；标准`convert_data.py`输出1707项，002两份configured/placed JSON各追踪锁定来源SHA-1。内置`runSmokeClient -PsmokeOnly=fulucooking`共166项INFO、0 FAIL、DONE，客户端正常保存四维度退出、ERROR/FATAL0；本批新增6项均通过。专服启动至Done后通过原生halt(false)正常保存四维度，Gradle BUILD SUCCESSFUL、游戏日志ERROR/FATAL0；控制台stdin已关闭，未收到stop命令；动态加载既有停服agent产生JDK警告，PowerShell包装返回1，Gradle原生返回0。证据为`tmp/shicai002-{build.log,smoke.log,report.txt,client.log,server.log}`。新增检查覆盖002原生生存播种扣种及泥土拒绝、受控原生randomTick干旱枯萎与对应种子掉落、原生骨粉有效目标扣一及本次结果、成熟上下半原生loot，以及真实加载RecipeManager的四种子配方匹配/assemble产一糯米；配方检查没有模拟玩家工作台取出。完整自然成长、上下半骨粉异常布局、自然新区域、完整保存重登、来源1.18.2版本实机、多人及两版本对照未执行。
