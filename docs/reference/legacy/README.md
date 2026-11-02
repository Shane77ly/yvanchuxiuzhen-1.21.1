---
topic: legacy-source
role: entry
status: partial
updated: 2026-11-01
---

# 1.18.2 工程来源

当前来源工作区：`D:/yuanchuxiuzhen-1.21.1-port/yuanchuxiuzhen-1.21.1`。提取器默认从新项目的同级目录 `yuanchuxiuzhen-1.21.1` 定位，支持用 `--source-workspace` 改为其他位置。1.18.2 来源目录只读，未复制其现有移植实现进新项目。

| 来源 | 相对来源工作区路径 | 已核实元数据 |
| --- | --- | --- |
| 原初修真 | `reference/old-1.18.2-mcreator/1.18.2yvanchuxiuzhen` | modid `yvanchuxiuzhen`，0.5.2，1.18.2 / Forge 40.1.0 / Java 17，必需原初系统 |
| 原初系统 | `reference/1.18.2yvanchuxitong` | modid `yvanchuxitong`，0.5.2，1.18.2 / Forge 40.1.0 / Java 17，必需原初修真 |

两工程 `build.gradle` 使用 ForgeGradle 5.1.+、official 1.18.2 mappings。修真工程 `mcreator.gradle` 另声明 `software.bernie.geckolib:geckolib-forge-1.18:3.0.57`。元数据声明 Academic Free License v3.0；最终资产许可应逐来源核对。

## 人工核实的边界证据

下列路径相对各 1.18.2 工程；可结合清单中的文件哈希识别本次版本。

| 证据 | 实际读取位置 | 本轮结论 |
| --- | --- | --- |
| 版本、加载器、Java | 两侧 `build.gradle` 和 `src/main/resources/META-INF/mods.toml` | 两者是同版本配套工程，互相强制依赖 |
| 重复功法存储 | 两侧 `src/main/java/net/mcreator/<modid>/network/<ModName>Variables.java` 的 PlayerVariables | 两边都有 gongfa/gongfalinshi；不能将其直接作为两个新功法真值 |
| 职业跨 Mod 调用 | 系统侧 `src/main/java/net/mcreator/yvanchuxitong/procedures/Tripod01ZaiFangKuaiShangYouJianProcedure.java:43` | 从修真玩家能力读取 Refiner_LEVEL，再写入鼎炉 RefinerLEVEL |
| 方块物品注册 | 两侧 `init/*ModItems.java` 的 block 辅助函数 | 物品清单需要解析方块引用，不能只匹配字面量 register |
| 声音注册 | 修真侧 `init/YvanchuxiuzhenModSounds.java` | 使用 Map 与 ResourceLocation 注册，不能只读取 RegistryObject 字段 |

## 静态基线与限制

见 [架构调查](source-survey.md) 和 [生成清单](inventory/README.md)。清单的源文件范围为两个工程 `src`、`elements` 及存在的 `build.gradle/gradle.properties`；不包含整个 Gradle 缓存、运行存档、工程外散落资源或构建输出。`mcreator.gradle` 的依赖声明已人工读取，但未纳入当前来源指纹范围。

静态注册数量不是完整功能数；玩家字段同名只说明需要核对归属，不证明含义完全相同。来源文档中已有的 67 本功法、符箓组合、阵法位置等统计，可辅助分批盘点，但本轮未据此宣称已独立逐条验证。

邻近工程的 `docs/reference/legacy/gameplay-map`、`docs/systems/project/legacy_parity.md` 与 `legacy_content_audit.md` 本轮仅作为检索入口。它们混有不同核对日期、工作树和后续采用决定，不直接作为本项目当前状态。

复现命令与覆盖边界见[清单说明](inventory/README.md)，迁移判定见[内容与对齐](../../development/content-and-parity.md)。
