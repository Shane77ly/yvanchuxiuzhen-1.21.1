# 菜单、资源与表现对齐

已核对范围：菜单、资源和世界内容条目来自两个 MCreator 生成的 1.18.2 Forge Mod 的调查阶段；实体、粒子、声音和刷怪蛋条目来自一次 MCreator + GeckoLib 来源到 NeoForge 1.21.1 的实体移植实践。

## 菜单与界面

- 来源菜单按“入口、槽位、按钮、条件、输入输出、关闭行为”登记，不用屏幕数量代替功能数量。HUD、覆盖层、按键、Tooltip 和普通交互另列。
- `addSlot` 语句数不等于最终槽位数；循环添加的槽位逐个核算。
- 客户端 UI 不拥有玩法存档、公式或扣费逻辑；所有按钮接入相同的服务端入口。
- 来源写在客户端的条件（按钮可用性、本地判断等）在目标版本的服务端入口仍须存在。逐层对照时专门检查这些条件是否丢失。
- 菜单装入玩家保存的物品时，核对原对象引用、复制和数量修改的顺序。来源共用引用时，原位修改可能在关闭前就被其他读取者观察到，而替换槽对象要等关闭才回写；不能把两种路径统一成独立编辑缓存。可把共享引用限定在数据所有者内部，对外查询仍给副本。
- 核对 Shift 搬移向已有堆叠合并的过滤规则。来源覆写可能在合并时也检查 `mayPlace`，只检查空槽插入不能等价；重开装入与当前标签不匹配的物品时尤其如此。

## 资源

- 核对模型父链、纹理、blockstate 全状态、动画、音效、粒子、翻译与字体；持物、落地、背包、方块世界表现分别验收。
- 目标版本可能改变资源和数据包目录名。按目标版本原版数据的实际目录放置，并在项目内保持一致；不机械照搬来源路径。
- 状态效果图标不能只搬入编辑器指定的原图。已核对 NeoForge 21.1.265 的 `MobEffectTextureManager.get` 按效果注册键读取 sprite，原版 `atlases/mob_effects.json` 扫描 `textures/mob_effect`；应复制来源实际按注册 ID 命名的图标，目标 ID 改名时同步改文件名。
- 实体类型清单区分本体、形态、弹体和纯视觉实体，不把实体类型总数当作生物种数。
- MCreator 1.18.2 箭类弹体逐类核对实际注册的渲染器及 `ItemSupplier.getItem()`，不按弹体 ID 推导显示物品；来源可能返回空栈、其他材料物品或使用独立 Java 模型。模型的 `setupAnim` 只有在实际渲染链调用时才生效，不因方法存在而补入调用。已对照来源原生模型渲染器及 NeoForge 21.1.265 的 `EntityRenderer`、`ThrownItemRenderer`、`ModelPart`；几何、部件姿态、纹理坐标、旋转顺序、渲染层和颜色分别保持来源语义。

## 粒子

- 完整链：粒子类型注册 + `particles/<名称>.json` + JSON 引用的全部 sprite PNG。缺少 PNG 时游戏中显示紫黑缺失纹理，编译不会发现。
- 客户端实现逐项对照：尺寸、寿命、帧步进、重力/阻尼/是否保留输入速度、渲染类型、光照。粒子 ID 与生成参数相同不足以证明一致；误用共享的 provider 会改变尺寸、密度、速度和寿命。
- 服务端触发核对类型、时机、数量、坐标偏移和速度。
- 不用外观相近的粒子替代来源粒子 ID。

MCreator 1.18.2 贴图粒子模板的参数化迁移（已核对构造、提供器、tick 和光照方法，并静态对照 NeoForge 21.1.265 的 `Particle`、`TextureSheetParticle`、`ParticleEngine`）：

- `quadSize *= scale` 乘的是原生基类随机尺寸，不能改成固定尺寸。输入速度、碰撞宽高与重力分别保留；来源使用不带速度的基类构造器时，不改用会随机扰动速度的重载。
- 随机寿命 `max(1, L + nextInt(2J) - J)` 的整数范围是 −J 至 J−1；旋转更新保留父类 tick 之后的顺序和弧度单位。
- `setSpriteFromAge` 初始化与随后 `(age / frameTicks) % frames + 1`、分母 `frames` 的推进是两种取帧方式，不能统一改成按寿命线性播放。逐项核对随机初始 sprite、循环帧、渲染层和是否覆写全亮。
- 纯贴图粒子使用 `RegisterParticleProvidersEvent.registerSpriteSet` 接入原生引擎。引擎在资源重载时清空活动粒子并重新绑定 SpriteSet；没有独立顶点提交与活动索引时，继续使用该生命周期。
- 多来源同名粒子即使图片相同，提供器参数也可能不同。先比较实际类；同路径图片只在字节一致时复用，JSON 帧序和不同提供器分别保留。

检查 JSON 引用的 sprite 是否齐全（PowerShell，替换命名空间和路径；无输出表示齐全）：

```powershell
$ns = '<命名空间>'
$particleDir = 'src\main\resources\assets\<命名空间>\particles'
$textureDir = 'src\main\resources\assets\<命名空间>\textures\particle'
Get-ChildItem -LiteralPath $particleDir -Filter '*.json' | ForEach-Object {
  $json = Get-Content -LiteralPath $_.FullName -Encoding UTF8 | ConvertFrom-Json
  foreach ($tex in $json.textures) {
    $name = $tex -replace "^${ns}:", ''
    if (-not (Test-Path -LiteralPath (Join-Path $textureDir ($name + '.png')))) {
      [PSCustomObject]@{ Particle = $_.Name; Missing = $name + '.png' }
    }
  }
}
```

PowerShell 复制带通配符的资源时使用 `Copy-Item -Path`；`-LiteralPath` 不展开通配符。

## 声音

来源可能跨编号复用声音（例如一个实体使用另一编号实体的脚步声）。核对声音注册、`sounds.json`、`.ogg` 文件和实体中的实际调用，不因编号不同而遗漏。

空声音ID也要核对注册表语义。已静态核对Forge40.1.0的`GameData`与`ForgeRegistry`：声音注册表没有默认项，未注册的空路径返回null；NeoForge21.1.265的`LivingEntity`默认受伤/死亡声音却是通用声音，省略来源的空声音覆写会增加声音，应显式返回null。

声音事件名与文件名可以不同，按 `sounds.json` 指向的文件复制。合并多个来源时，声音事件按归属拆到各单位的内容表与 `sounds.json` 片段，字幕文本随事件进入同一单位的语言片段，构建时合并；转换工具遇到没有归属的事件报错。验证时核对导出的声音事件数、客户端每个事件都有定义且字幕已翻译；实际播放另行试听。

## 实体

- 实现前记录来源事实：注册 ID、显示名、尺寸、追踪范围与更新间隔、经验、默认等级/天赋、纹理变体、声音、掉落。
- 每个技能记录：候选条件与权重、动画、起手/命中/结束 tick、朝向、移动/寻路、命中中心与范围、伤害类型与倍率、击退、效果、粒子、声音。实体类之外的受伤前钩子、被动钩子同样纳入。
- 来源的等待队列迁为明确的 tick 门控，不压缩为单帧执行。
- 依赖视线向量的技能先在服务端设定朝向。
- 共用实体基类前，逐类核对导航、移动控制、重力、飞行/游泳属性、水下呼吸、水流推动与死亡移除时序。已核对MCreator 1.18.2动画实体同时存在地面、飞行和水中导航模板；水中导航还带匿名移动控制，不能只替换导航对象。同模板在替换类名后比较完整方法，再复用。
- 无重力的设置位置按构造器与`aiStep`实际顺序保留；首次`aiStep`后关闭重力不等于首次必然下落。已核对1.18.2原生`Mob`的NoAI/骑乘控制门槛及Forge40.1.0补丁、NeoForge21.1.265的`Entity.isControlledByLocalInstance`与`LivingEntity.travel`：两版本的无AI、无控制者生物都跳过原生travel，不能仅凭重力标志推导位移。
- `tickDeath`按实际递增与比较顺序移植；来源若先递增再比较零，不能按“死亡零刻”解释成立即移除。技能过程主动移除与原生死亡阶段分别登记。
- 伤害免疫逐类读取完整`hurt`，区分`getDirectEntity()`、间接攻击者、类型/标签和`getMsgId()`条件；没有覆写就保留继承行为。`causeFallDamage(false)`与空`checkFallDamage`也要单独迁移，不能用hurt中的FALL拒绝替代。已静态核对NeoForge21.1.265的`LivingEntity`：原生摔落流程会先处理事件、声音及落地逻辑，再调用hurt；旧`DamageSource.ANVIL`对应`DamageTypes.FALLING_ANVIL`，爆炸条件使用`DamageTypeTags.IS_EXPLOSION`。
- 受击、默认与技能动画的打断、共存和恢复条件按来源谓词处理，不自行统一。已核对MCreator 1.18.2与GeckoLib3.0.57的动画工厂会逐刻消费同步名字、复位undefined并写入本地过程名；仅看setAnimation不能判断同名请求是否会再播。迁移到GeckoLib4.9.3时，`RawAnimation.thenPlay`采用JSON默认循环，来源显式PLAY_ONCE应使用`then(名字, Animation.LoopType.PLAY_ONCE)`；完成后的重载对应`AnimationController.forceAnimationReset`。逐类读取控制器过渡刻数和默认动画条件，不从同族实体推断。
- 渲染器逐类核对缩放、阴影、死亡旋转覆写和发光层；渲染缩放与碰撞箱缩放分别记录。已核对GeckoLib3.0.57与4.9.3的死亡旋转默认均为90度，来源只有部分实体覆写为0，不能统一替换。4.9.3的`withScale`在主渲染前缩放，发光层`reRender`沿用外层变换而不重复缩放。头部骨骼的暂停条件也按来源保留；4.9.3的`GeoModel.handleAnimations`冻结时间后仍可能调用`setCustomAnimations`，不能依赖动画时钟代替自定义骨骼的暂停条件。
- 刷怪蛋除物品注册外，还要核对物品模型、翻译和创造模式物品栏收录。
- 施放生成、刷怪蛋与后代生成分别核对 `finalizeSpawn` 的参数和位置；不能由 `isFood` 恒为假推断 `getBreedOffspring` 没有运行入口。已读取 NeoForge21.1.265 的 `SpawnEggItem.spawnOffspringFromSpawnEgg`：对 `AgeableMob` 调用后代方法，随后设幼年、放置并加入世界；来源后代方法中的初始化仍须保留。
- 简化没有食物的驯服实体交互时，仍逐端核对主人、非主人、未驯服、刷怪蛋与持久化分支。技能候选按完整条件嵌套迁移，`isPassenger` 表示实体正在骑乘其他实体，不与实体承载乘客混用；独立结束过程与通用结束过程分别核对实际清除的键。

## 世界内容

- 核对全部维度、群系、结构 NBT、出生与自然生成、矿物/药草/材料供给、宝箱与商店、传送回程。
- 维度表现以 `dimension_type` 实际绑定的 `effects` 为准；来源客户端登记同名特效不代表被使用。正式维度接入时移除验证源集中同ID的替身，避免运行仍被测试数据覆盖。
- MCreator 1.18.2 的维度 Fixers 可能修改全局 CAVE/CANYON 可替换集合。迁移前查清作用范围；已核对 NeoForge21.1.265 的 `Carvers` 与 `WorldCarver.canReplaceBlock`：原版 cave、cave_extra_underground、canyon 共用 `minecraft:overworld_carver_replaceables` 标签，nether_cave使用另一标签，不能误把修改限制为某个维度，也不能扩到下界雕刻器。
- 结构文件存在不证明有自然生成入口。
- 自然获取线路未接通时，不能因命令可以生成物品而标记玩法完成。

## 资源目录与模型引用迁移（MCreator 1.18.2 到 NeoForge 1.21.1，已用于全量方块与物品）

- 纹理目录 `blocks/`、`items/` 在 1.21.1 的图集配置中是 `block/`、`item/`，须改目录并同步改写模型 JSON 中 `textures` 与 `parent`、blockstate 中 `model` 的引用；没有命名空间的引用按 `minecraft` 解析。
- 来源 Blockbench 导出的模型常含无命名空间的 `parent`（如与文件同名的 `food002`），指向不存在的原版模型：旧版本回退为缺失模型的父级，不影响自身 elements；迁移时丢弃该 parent 可消除加载警告。判断是否存在，对照目标版本的原版资源包（`client-extra` 制品）而不是来源工程。
- 渲染层由模型 JSON 的 `render_type` 声明并沿 parent 链继承：为需要非实心渲染层的方块生成子模型（`parent` 指向原模型，加 `render_type`），blockstate 与方块物品模型改引用子模型，原模型可被多个方块共用。
- 模型 `textures` 中指向空路径的键（来源缺陷）在新旧版本都显示缺失纹理；保持来源并登记，不自行补。
