---
topic: liandan
role: entry
status: partial
updated: 2026-11-09
---

# 炼丹

标识 `liandan`，类别：系统。职责：丹炉、丹方、炸炉、分合丹、药理、毒药（[系统设计](../../development/system-design.md)）。

当前阶段：D 部分实现。八档丹炉、自然炼制、药性目录、丹方装料/取丹经验、八种药瓶消费/放置链、分丹台分合丹及药理台已接回；本批接回百草鉴自然开窗、逐项辨识、玩家附件、药草提示与全解锁入口，共 68 条注册标为“行为已迁移”。分丹台批次构建、内置客户端25项及专服加载保存退出通过；药理台来源静态核对、构建、内置客户端28项和专服加载保存退出通过。百草鉴来源静态核对、构建、内置客户端54条INFO/0FAIL/DONE及专服加载保存退出通过；毒药结束回调等仍待迁移。各批验证见[月度记录](logs/2026-11.md)，不代表全系统验收。

- [架构与调查正文](architecture.md)：来源入口、数据所有者、调用契约、生命周期和裁定事项。
- 候选归属 133 个元素，其中 121 个已读；这是检索范围，不是该单位已验证功能数。见[归属表](../../reference/legacy/inventory/ownership_assignment.csv)。
- [内容台账](ledger/content.csv)：来源注册的目标 ID、归属状态与迁移状态（`tools/porting/check_ledger.py` 检查）。
- [系统导航](../README.md)记录源码引用规则与各项证据范围。

千材录物品、辨识菜单、两份合成与相关过程已按材料辨识职责移归[material](../material/README.md)；百草鉴药材辨识仍属炼丹。

## 公开操作

本批按 `typed_field_accesses.csv` 核对全部 25 处访问：`playercaoyaojiexi` 20 处、`caoyaolist` 5 处，逐项去向见[字段访问覆盖](architecture.md#药草字段访问覆盖)。药草目录由 `Worldbegin` 写入、炼丹与百草鉴读取；玩家药草辨识由百草鉴与 `Jiesuocaoyao` 写入，跨系统读取只有 `Lore` 的提示分支。材料辨识与此无关。

| 操作 | 所有者与接入方式 | 本批状态 |
| --- | --- | --- |
| 药草药性目录、装置药性快照 | liandan 读取来源 Worldbegin 的完整有序目录，实体放置丹炉或药理台时写入自身 zong；药理台仅据此预览和写丹方，不编辑药性表 | 已实现；药理台内置客户端样本通过 |
| 丹火热量与耗尽查询 | 订阅 yihuo 的 FireHeatEvent、FireExhaustedEvent，由 liandan 唯一写丹炉 lianpower/gengxin | 已实现；验证见本月记录 |
| 火力符增益 | `Liandan.boostHeat` 写自身 firepower；来源 `Itemfulu20DangTouSheWuSheZhongCiFangKuai` 的消费者随 fulu 接入 | 已实现；验证见本月记录 |
| 炼丹职业快照及经验领取 | 通过 profession 的查询/加经验操作读取 Alchemy_LEVEL、在取丹时领取装置经验 | 已实现；验证见本月记录 |
| 玩家药草辨识、解锁与提示查询 | liandan 唯一写入 `HerbAnalysis` 玩家附件并只同步本人；界面与 Lore 药草提示读取只读快照，`LiandanHerbCommand` 承接 `unlock yaocai`；没有来源自定义重置入口 | 已实现并静态核对；本批构建、内置客户端54条INFO/0FAIL/DONE及专服加载保存退出通过 |
| 丹药服用及即时/持续药效 | liandan 解释丹药实例，通过原版 effect 命令保留参数验证与效果生命周期；既有效果经各数据所有者的公开操作施效 | 自服、堆叠合并、空容器返回及实体喂药已实现并静态核对；本批内置客户端样本通过，完整分支与两版本待验收 |
| 分丹台装填、分丹与合丹 | liandan 菜单绑定自身方块实体，由服务端按钮操作三槽库存；不提供其他系统直接写库存的入口 | 已实现并静态核对；构建、内置客户端25项及专服加载保存退出通过 |
| 药理台丹方预览与媒介写入 | liandan 按装置药性快照顺序聚合八槽材料；客户端算显示缓存，服务端写媒介实例；不消费材料或直接产丹 | 已实现并静态核对；构建、内置客户端28项及专服加载保存退出通过 |

本轮变化见[系统月度日志](logs/2026-11.md)，项目进度见[项目月度日志](../project/logs/2026-11.md)。
