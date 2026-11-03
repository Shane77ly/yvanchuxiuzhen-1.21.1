# 菜单、资源与表现对齐

已核对范围：菜单、资源和世界内容条目来自两个 MCreator 生成的 1.18.2 Forge Mod 的调查阶段；实体、粒子、声音和刷怪蛋条目来自一次 MCreator + GeckoLib 来源到 NeoForge 1.21.1 的实体移植实践。

## 菜单与界面

- 来源菜单按“入口、槽位、按钮、条件、输入输出、关闭行为”登记，不用屏幕数量代替功能数量。HUD、覆盖层、按键、Tooltip 和普通交互另列。
- `addSlot` 语句数不等于最终槽位数；循环添加的槽位逐个核算。
- 客户端 UI 不拥有玩法存档、公式或扣费逻辑；所有按钮接入相同的服务端入口。
- 来源写在客户端的条件（按钮可用性、本地判断等）在目标版本的服务端入口仍须存在。逐层对照时专门检查这些条件是否丢失。

## 资源

- 核对模型父链、纹理、blockstate 全状态、动画、音效、粒子、翻译与字体；持物、落地、背包、方块世界表现分别验收。
- 目标版本可能改变资源和数据包目录名。按目标版本原版数据的实际目录放置，并在项目内保持一致；不机械照搬来源路径。
- 实体类型清单区分本体、形态、弹体和纯视觉实体，不把实体类型总数当作生物种数。

## 粒子

- 完整链：粒子类型注册 + `particles/<名称>.json` + JSON 引用的全部 sprite PNG。缺少 PNG 时游戏中显示紫黑缺失纹理，编译不会发现。
- 客户端实现逐项对照：尺寸、寿命、帧步进、重力/阻尼/是否保留输入速度、渲染类型、光照。粒子 ID 与生成参数相同不足以证明一致；误用共享的 provider 会改变尺寸、密度、速度和寿命。
- 服务端触发核对类型、时机、数量、坐标偏移和速度。
- 不用外观相近的粒子替代来源粒子 ID。

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

声音事件名与文件名可以不同，按 `sounds.json` 指向的文件复制。合并多个来源时，声音事件按归属拆到各单位的内容表与 `sounds.json` 片段，字幕文本随事件进入同一单位的语言片段，构建时合并；转换工具遇到没有归属的事件报错。验证时核对导出的声音事件数、客户端每个事件都有定义且字幕已翻译；实际播放另行试听。

## 实体

- 实现前记录来源事实：注册 ID、显示名、尺寸、追踪范围与更新间隔、经验、默认等级/天赋、纹理变体、声音、掉落。
- 每个技能记录：候选条件与权重、动画、起手/命中/结束 tick、朝向、移动/寻路、命中中心与范围、伤害类型与倍率、击退、效果、粒子、声音。实体类之外的受伤前钩子、被动钩子同样纳入。
- 来源的等待队列迁为明确的 tick 门控，不压缩为单帧执行。
- 依赖视线向量的技能先在服务端设定朝向。
- 受击动画不打断进行中的技能动画；中断技能时同步清理动画状态。
- 刷怪蛋除物品注册外，还要核对物品模型、翻译和创造模式物品栏收录。

## 世界内容

- 核对全部维度、群系、结构 NBT、出生与自然生成、矿物/药草/材料供给、宝箱与商店、传送回程。
- 结构文件存在不证明有自然生成入口。
- 自然获取线路未接通时，不能因命令可以生成物品而标记玩法完成。

## 资源目录与模型引用迁移（MCreator 1.18.2 到 NeoForge 1.21.1，已用于全量方块与物品）

- 纹理目录 `blocks/`、`items/` 在 1.21.1 的图集配置中是 `block/`、`item/`，须改目录并同步改写模型 JSON 中 `textures` 与 `parent`、blockstate 中 `model` 的引用；没有命名空间的引用按 `minecraft` 解析。
- 来源 Blockbench 导出的模型常含无命名空间的 `parent`（如与文件同名的 `food002`），指向不存在的原版模型：旧版本回退为缺失模型的父级，不影响自身 elements；迁移时丢弃该 parent 可消除加载警告。判断是否存在，对照目标版本的原版资源包（`client-extra` 制品）而不是来源工程。
- 渲染层由模型 JSON 的 `render_type` 声明并沿 parent 链继承：为需要非实心渲染层的方块生成子模型（`parent` 指向原模型，加 `render_type`），blockstate 与方块物品模型改引用子模型，原模型可被多个方块共用。
- 模型 `textures` 中指向空路径的键（来源缺陷）在新旧版本都显示缺失纹理；保持来源并登记，不自行补。
