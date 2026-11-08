---
topic: material
role: architecture
status: partial
updated: 2026-11-08
---

# 材料、采集与辨识：架构调查与提案

来源事实、已实现入口及待接入范围分别陈述；下文实现状态不表示其余提案已经批准。源码定位采用 [系统导航](../README.md)的 A/B 规则。
## 1.18.2 定义与状态

材料共有身份来自注册 ID；等级/品阶在实例 `cailiaolevel/cailiaopinjie`，不是不同材料 ID。B 的 `WorldbeginProcedure:43–46` 在世界加载时写 `lianqicailiaolist`：每行包含材料 ID、重量及后续三元组，既有属性贡献、附魔，也有特殊标记。

`B:QiancailujiandingProcedure:49–149` 读取玩家 `playercailiaojiexi`，可从普通库存与玩家材料仓扣除辨识材料。其遍历等级为 1–40、品阶为 0–4；这是该消费者的范围，不推断所有材料永远只能有这些值。`YihuoshujvfuyvProcedure` 单独初始化异火辨识。

## 目标所有权与接口提案

材料定义保存编号/ID、重量、类别和各生产用途描述；材料实例保存等级、品阶及真实实例覆盖。辨识记录以材料 ID 为键保存进度、已揭示条目；`txtitemid/txtname` 等当前显示缓存不混入永久进度。

采集公开产物与职业进度操作；lianqi/liandan/zhenfa 各解释本系统用途，不让材料服务执行所有生产规则。storage 依赖材料实例与已辨识效果查询；材料辨识通过材料定义的 `MaterialInventoryRequestEvent` 请求 storage 消费材料，不能直接修改 `renwuitem`，也不反向依赖 storage。

动态键 `{materialId}jindu/jindumax/yaoxiao{index}` 变为 ID 索引的辨识记录。顺序、重复材料条目和同一材料多种用途必须保留；不能按 ID 简单覆盖后丢失第二组效果。

## 验证与范围

来源注册与目录都要覆盖，包含矿物、妖兽材料、灵木、宝石、异火等。采集掉落、材料生成等级与品阶、辨识扣料和生产消费者必须形成完整路径。当前没有批准重新计算材料权重或贡献预算。

## 2026-11-08 基础目录与实例载体

从锁定的 1.18.2 材料目录提取 225 条 `cailiao_*` 定义，写入 `content/material/definitions.json`。目标目录保留材料 ID、固定重量、经验、属性条目、特殊效果条目和特殊标记；资源由 `ContentFiles` 在 Mod 启动时用 Codec 解码，重复 ID 或未知字段直接中止启动。材料物品的 `cailiaolevel/cailiaopinjie` 改为 `material_instance` 组件，玩家 `playercailiaojiexi` 改为 `material_analysis` 附件，附件按来源玩家变量在死亡时复制并只同步本人。

`YihuoshujvfuyvProcedure:22–58` 的登录入口由 yihuo 通过 `Materials.initializeYihuo` 调用，覆盖 `cailiao_221`–`cailiao_239` 的 19 个目录 ID，并按来源固定写入 `jindumax = 6`，使用一次性标记避免重复初始化。最初的基础载体只保存进度，完整有序揭示状态在下述千材录批次补齐；此前的构建与客户端启动证据不能替代辨识验收。

验证证据：`gradlew build` 通过，包含 ArchUnit；沿用现有 `gradlew runSmokeClient -PsmokeOnly=skillparticle` 进入真实 1.21.1 客户端世界，资源加载、登录、保存退出完成，报告含 `DONE`，客户端日志 `ERROR=0`、`FATAL=0`。该已有选择器只证明公共资源和登录生命周期没有启动回归，不替代材料界面、扣料或两版本对照；本批没有新增 Test 类或冒烟选择器。

## 千材录与材料仓查询（2026-11-08）

### 来源顺序与持久化

重新按 `B:WorldbeginProcedure:43–46` 两段字符串拼接后的原始顺序核对全部225行，合计1451个完整三元组，目录中没有重复材料ID。`MaterialDefinition.entries()` 直接保存来源有序三元组；`entryCount()` 同时计入 `attribute/expnum`、普通属性、附魔与 `teshu`，不能把结构化属性和特殊效果重新拼接作为辨识次序。独立重量列不计入辨识。目录提取已纳入既有 `tools/porting/convert_content.py` 的 `convert_material_definitions`，同一次转换同时输出原结构化字段和有序 `entries`。主会话独立转换核对225/1451，发现旧提取的195号 `soulnum` 含Java字符串拼接残留，已按来源数值1修正；生成函数也保留未注册220/230号的空目录行，不为其补造物品注册或效果。

玩家附件保存每个 ID 的进度、当前上限和按1起始顺序对应的已揭示 `namespace/name/open`，特殊标记另保存为玩家全局标记；死亡复制与本人同步沿用既有附件机制。属性只写名称，附魔同时写命名空间和 `open=true`，`teshu` 写命名空间、名称与全局标记，不额外设置 `open`。材料仓筛选仅查看已揭示且 `open=false` 的名称；详情通过公开只读条目保留自己的来源显示判定。

异火初始化按目录中 `cailiao_221` 所在位置顺次处理19行，统一写上限6，并从已有进度后逐条追加来源效果、每条令进度加1。`cailiao_230` 是空定义，实际不追加效果，进度保持原值；不能把19个ID一律直接写成进度6。一次性标记已存在的开发存档不重放初始化；本批没有新增兼容转换。

### 自然菜单与辨识扣料

千材录物品 `qiancailubook` 自然右键打开 `MaterialAnalysisMenu`，保留 `qiancailu_gui` 菜单身份、176×166背景、上方202×120纸张、输入槽 `(30,-13)`、四个来源按钮和十行效果坐标。按钮0刷新，按钮1辨识，按钮2/3按来源顺序发送属性换算、等级品阶说明。菜单选中缓存与永久辨识附件分开；按钮消息绑定发送者当前菜单和容器ID，并检查来源开窗位置所在区块已加载，保留来源旁观者按钮入口。

辨识必须先在槽0放入 `cailiao` 材料。缓存为空或缓存ID与输入不同，本次按钮只刷新；否则成本为当前进度×2，进度0首次免费但仍要求槽0材料。可用总数为完整玩家物品能力中的同ID数量、槽0数量及允许消费时的仓ID原始总数，不按等级或品阶区别统计。确认总量足够后，依次请求仓按等级1–40、品阶0–4消费，再使用普通库存原生清除操作，仍不足的部分从槽0扣除；创造模式同样扣料。成功后进度加1、按原始序号揭示并刷新。消耗失败保留来源红色提示，已完全辨识显示来源完成提示。

来源自然开窗只写玩家坐标，不写物品手或实体标识；菜单生成模板因此会实际尝试绑定脚下方块的无方向物品能力。存在能力时输入属于该方块且关闭不返还；没有能力时使用临时输入，关闭按玩家存活和连接情况返还或掉落。目标按同一路径适配；自然右键和无方块绑定的辨识路径已有下述客户端证据，特殊方块绑定尚未专项实机。

### 保持的来源异常与未验收项

- `cailiao_211`–`219` 的原始文本在 `manadefnum/1attribute/knockbacknum` 处缺分隔符。辨识仍按完整三元组数向下取整，得到6条，保留 `1attribute` 值与错位的 `knockbacknum/1/attribute` 条目；不补造末尾未构成完整三元组的条目。
- 来源允许材料仓存入41级，辨识仓消费只扫1–40级；总量预检仍使用仓原始ID总数。仓等级/组清理与计数异常由 storage 保留，材料不重新计算仓数量。
- 来源刷新遇到首个同ID目录行即停止，辨识过程则遍历全部匹配行；当前锁定目录没有重复ID，因此本批不产生额外覆盖路径。
- `B:JiesuocailiaoProcedure:37–41` 会写 `yaoxiao{n}txt`，由 `YvanchuGoProcedure:24–27` 的 `unlock cailiao` 管理命令调用；命令名实际为 `yvanchuxitong`，权限1。该路径把 `teshu` 也当作附魔分支并写 `open=true`，不设置异火初始化使用的全局特殊标记，后续管理命令批次须保持其区别。`QiancailushuaxinProcedure:91` 虽复制介绍到显示缓存，但 `Qiancailutxtxiaoguo1`–`10:15` 只返回青色效果名称；千材录不显示介绍。本批补齐名称青色，管理解锁入口和介绍存储仍待管理命令所有者接回。

### 本批运行证据与边界

静态源码与目录核对完成；1.21.1版本实机自动化沿用既有 `runSmokeClient -PsmokeOnly=storage`，全场景45项玩法检查PASS、0FAIL、DONE，另2条INFO为过程说明。材料部分通过新玩家异火221号六条揭示与初始化标志、千材录真实右键、首次辨识只刷新、进度0免费揭示来源首条 `expnum`、成本2优先扣仓1级零品、成本4跨等级消费并保留来源组等级计数异常，以及已揭示属性供仓AND筛选。死亡后核对服务端辨识条目数保持；重登后核对双端辨识条目数一致。仓完整快照和采集等级在两阶段均双端一致；未逐字段检查全部揭示记录或其他九职业。

证据 `tmp/material-storage-client.log`、`tmp/material-storage-final-report.txt`、`tmp/material-storage-final-latest.log`；最终客户端WARN37（含已有厨具缺失纹理）、ERROR/FATAL0。首次场景因袋06工厂漏接失败，修复后重跑通过，`tmp/material-storage-first-*` 保留。最终 `build compileSmokeJava`（含既有test/check/ArchUnit）及专服启动至Done后原生stop均通过，四维度正常保存退出、ERROR/FATAL0、菜单18个；证据 `tmp/material-storage-build-final.log`、`tmp/material-storage-server-latest.log`。没有新增Test类、选择器或独立验证框架。

千材录截图 `build/smoke/screenshots/storage-material-analysis` 已保存且无扩展名，不能据截图标记完整视觉认可。普通库存与输入槽各自扣料、41级仓预检/实际扣料差异、方块能力绑定、全部显示状态、全缩放、多人、真实1.18.2版本实机和两版本对照未执行。管理解锁、采集材料生成与生产消费者仍由所属批次接回。

语言迁移按两份锁定来源补齐34个实际材料属性/特殊效果键（16个属性、18个teshu），中文和英文原文均保留，英文空值不自行翻译。来源A另有实体皮肤/潜力/身高三个属性键，未混入材料语言片段，随其实际所有者迁移。
