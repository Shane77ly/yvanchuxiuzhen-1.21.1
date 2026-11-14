---
topic: quest
role: entry
status: partial
updated: 2026-11-14
---

# 任务

标识 `quest`，类别：系统。职责：对话动作、任务、悬赏、成就（[系统设计](../../development/system-design.md)）。

B1 已登记本单位不依赖新代码类型的内容身份与资源（方块8、物品8），只表示身份、资源与基础属性就位。

2026-11-14已实现本包必要的高级通宝玉单步银行对话：接收经济系统事件、保存会话、服务端END刻先清会话再打开银行登录页，死亡复制和登录清空按来源处理。此入口与银行账户菜单共同通过127条客户端检查，覆盖死亡与真实退出重进；构建、架构、文档和台账检查通过，结果见[任务月志](logs/2026-11.md)与[经济月志](../economy/logs/2026-11.md#2026-11-14-银行账户与高级通宝玉登录)。银行方块四选项、通用对话分支、任务存储、悬赏、奖励和成就仍未实现；本次不提升整个任务系统完成状态。专服及两版本实机未执行。

- [架构与调查正文](architecture.md)：来源入口、数据所有者、调用契约、生命周期和裁定事项。
- 候选归属 124 个元素，其中 16 个已读；本轮另核读`PlayerTickxitong`与`Duihuashijian`的银行路径分支，保留整元素“候选”，不计入完整已读数量。这是检索范围，不是该单位已验证功能数。见[归属表](../../reference/legacy/inventory/ownership_assignment.csv)。
- [内容台账](ledger/content.csv)：来源注册的目标 ID、归属状态与迁移状态（`tools/porting/check_ledger.py` 检查）。
- [系统导航](../README.md)记录源码引用规则与各项证据范围。

## 公开操作

| 公开操作 | 来源字段与当前边界 |
| --- | --- |
| `BankDialogueActions.request(ServerPlayer)` | 固定的`bankbankbank`单步“打开界面/银行”请求；写本任务会话的脚本、游标0和启动标记 |
| `BankDialogueActions.snapshot(Player)` | 只读该银行会话的脚本、游标和启动标记，不查询或改写其他任务档案 |
| `BankDialogueRequestEvent`订阅 | 高级通宝玉入口由经济系统发事件；任务单向调用经济的`BankMenus.openLogin`，不让经济直接写任务附件 |

已对照`typed_field_accesses.csv`中本链`Playerrightclickwithitem`、`PlayerTickxitong`、`Duihuashijian`、`PlayerJoinGame`的`renwuitem`读写及`renwuliebiao`读取范围：本银行脚本没有“任务储存”步骤，也未发现本链的持久任务记录生产者，不另造任务jindu。其他脚本、选项、交易和任务记录仍待所属完整工作包接入，不能把本链接口表当作全部任务字段已覆盖。详见[银行单步正文](architecture.md#高级通宝玉银行单步对话2026-11-14)。

本轮变化见[任务月志](logs/2026-11.md)，共同银行运行结果由[经济月志](../economy/logs/2026-11.md)维护。
