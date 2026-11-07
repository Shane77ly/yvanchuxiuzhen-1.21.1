---
topic: world
role: architecture
status: draft
updated: 2026-11-07
---

# 世界：架构调查与提案

来源事实与目标方案分别陈述。目标方案未批准；源码定位采用 [系统导航](../README.md)的 A/B 规则。

## 正式虚空维度（2026-11-07）

本节为 skill 当前依赖的直接来源迁移，不表示后文世界主体方案已实施。来源为 `A:world/dimension/XvkongDimension.java`、`A:world/biome/XvkongqvnxiBiome.java` 与 `data/yvanchuxiuzhen/{dimension,dimension_type}/xvkong.json`；编辑器两个元素与实际注册、技能裂隙调用已交叉阅读。归属 world，当前无可变状态或新 Java API，公开契约见 [README](README.md#公开操作)。

### 1.18.2 版本实际规则

- `minecraft:noise` 生成器，单项 multi_noise 群系 `xvkongqvnxi`；噪声范围 Y=0–127、水平格宽8、垂直格高4。默认方块、流体和地表均为空气；关闭原生生成生物、含水层和矿脉。群系没有地物、雕刻器或生物条目，原版结构的适用群系不包含它。
- 群系有降水，温度与降水量均0.5；雾、水、水雾、天空、草、叶颜色均为来源整数−16777216；没有环境音或背景音乐定义。
- 维度范围 Y=0–255，逻辑高度256，坐标倍率1；无天光、无顶、环境亮度0、非自然、非灼热、猪灵安全；床可用、重生锚不可用、无袭击、不固定时间。
- 类型 JSON 实际绑定 `minecraft:overworld`。Java 登记的同 Mod ID 无天空特效没有被该类型引用，本批保持实际绑定，不主动切换效果。
- `Fixers` 在公共初始化中向全局 `WorldCarver.CAVE`、`CANYON` 的可替换集合加入AIR；它不只作用于虚空。虚空群系本身没有雕刻器。

### 1.21.1 版本表达

`tools/porting/convert_world.py` 由 `convert_data.py` 调用，直接读取来源维度、类型和实际群系 Java 定义，四个目标文件均写入 `data_manifest.csv`，登记来源路径与SHA-1。正式虚空资源位于主源集；移除 smoke 同 ID 的平坦基岩替身。

精确目标依据为 NeoForge21.1.265 的 `NoiseBasedChunkGenerator`、`NoiseGeneratorSettings`、`DensityFunctions`、`NoiseRouterData`、`DimensionType`、群系相关Codec、`Carvers` 与 `WorldCarver`。1.18.2版本密度函数与滑移公式另从同版本Minecraft源码核对，Forge40.1.0源码包没有修改这些类的补丁。转换差异：

| 来源字段或机制 | 目标表达与影响 |
| --- | --- |
| `minecraft:slide` 与 noise 的上下滑移 | 按垂直格高4展开：顶部从Y56至312、由1至0，目标−23.4375；底部从Y4至32、由0至1，目标−0.234375；保留先顶部后底部，以及原插值、混合、压缩顺序 |
| `sampling`、`island_noise_override`、`terrain_shaper` 与旧开关 | 目标Codec已移除，保留来源路由引用 `minecraft:end/sloped_cheese` 与其他密度字段；旧sampling的水平2/格宽8、垂直1/格高4对应目标End混合噪声0.25/0.25、因子80/160、smear倍率4；地形输出仍为空气 |
| 生成器独立 `seed` | 目标生成器Codec已移除，改用目标原生世界种子；只有一个群系、方块与流体均为空气，因此不产生地形或群系分布差异 |
| 旧 `structures` 分布表 | 目标按结构集与结构适用群系决定；该空群系不加入任何原版结构群系标签，保留无自然结构 |
| 新必填 `spawn_target` | 空列表；不新增玩家出生点偏好 |
| 新必填怪物光照字段 | 方块光上限0、总亮度阈值0–7均匀取值，对应1.18.2版本 `Monster.isDarkEnoughToSpawn`；群系没有自然生物条目 |
| CAVE/CANYON 的全局AIR替换 | 给 `minecraft:overworld_carver_replaceables` 增补AIR；目标原版 cave、cave_extra_underground、canyon 共用此标签，nether_cave使用另一标签。其他Mod自定义雕刻器配置不属于本批对照范围 |

技能入虚空时按裂隙坐标各轴除50并四舍五入，返回乘50，编号保存在 xiulian 体质数据；本批不改变传送代码。其他六个自定义返回维度尚未接入，随 E 阶段各自实现。正式维度的数据加载与保存证据见下方记录；完整视觉、自然裂隙往返、多人、1.18.2版本实机和两版本对照尚未执行。

### 本批验证

`build`（含ArchUnit）通过；补入来源群系名称后，专用服务器仅启动一次至Done，正式维度与群系加载成功。原生命令强制加载虚空区块(0,0)，在(0,64,0)同时确认 `xvkongqvnxi` 与空气；随后移除强加载，原生stop保存四个世界并正常退出，日志ERROR0，导出群系1个。没有新增测试类、专项选择器或重跑旧裂隙检查。证据为 `tmp/world-void-build.log`、`tmp/world-void-content.log`、`tmp/world-void-server.log` 与 `build/registry-export/biome.txt`。

本次确认数据加载、一个实际生成区块及保存；不作为自然裂隙往返、客户端视觉、多人或两版本实机对照的证据。台账仅标“已注册”。

## 1.18.2 状态与生成

范围包含 8 维度、68 群系、220 个 Feature 注册、161 结构文件；编辑器有 66 structure 元素和 35 feature 元素，二者与注册/文件计数不是同一口径。自然投放、固定场景、指令放置和副本刷出分别记录。

`B:WorldbeginProcedure:29–54` 的 worlddata 初始化有开关，但后面的材料/药草/服饰/结构/悬赏字符串赋值在开关之外；每次入口执行都会重写这些定义。`WorldcloseProcedure:26` 只复位 worldchufa。不能把所有这些字符串误判为允许永久在线编辑的世界真值。

## 目标所有权与接口提案

定义：维度/群系/生成配置与结构。实例：维度状态与已生成标志。城镇归 [town](../town/architecture.md)，副本归 [dungeon](../dungeon/architecture.md)。MapVariables 的主世界范围和 WorldVariables 的维度范围分别保存。

world 提供投放、维度与结构放置、结构管理操作；creature 负责生成实体的玩法初始化。`Weidubianhuan` 中按 `gamemoshi` 恢复游戏模式的分支归 dungeon。

按来源实际触发方式迁移生成。固定坐标目录、管理工具、传送器和场景清理不能被自然生成配置替代。目标具体 Codec/放置 API 在目标构建源码就绪后锁定，不能凭其他版本签名编写。

## 验证与裁定

保存/加载、多人进入、死亡/离场、跨维度返回和场景卸载分别检查。主世界与维度数据不能用英文类名推断。是否承诺 1.18.2 世界存档转换仍待负责人决定；保留资源 ID 本身不提供完整存档升级能力。

## 实现前须定（B1 实测）

结构 `difujiegou` 展开后 304 MB、8,442,357 个方块，其中约 92% 是同一个状态（调色板索引 41，应为空气），来源在 `DimensiondilingDangWanJiaJinRuWeiDuShiProcedure` 里进入地府维度时 `getOrCreate` 加载并摆放。1.21.1 开发客户端默认堆下加载它在解析 NBT 时 `OutOfMemoryError`（2026-11-02 实测，15.9 秒后失败，与数据版本升级无关）。实现地府维度时从三个办法中选：压缩掉空气条目并在摆放前清空区域（改变模板内容，需对照来源摆放结果）、拆成多个较小模板分段摆放、把它保持原样并提高运行内存要求。裁定前文件原样放在 `data/yvanchuxiuzhen/structure/difujiegou.nbt`。
