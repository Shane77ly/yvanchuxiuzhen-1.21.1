---
topic: target-api-evidence
role: data
status: partial
updated: 2026-11-01
---

# 目标 API 核对

本轮先查阅本地 NeoForge 21.1.228 源码。源码根目录为邻近工程的 `reference/neoforge-21.1.228-sources`；其绝对位置由[旧工程入口](legacy/README.md)的来源工作区定位。

这些证据支撑架构边界，不能替代编译和游戏验证。新项目尚无 Java 实现。外部文档检索本轮未返回可用正文，因此以下结论均以明确的本地源码为依据。

| 机制 | 已检查源码（相对 NeoForge 源码根） | 可以确认的事实 | 本项目仍待验证 |
| --- | --- | --- | --- |
| 延迟注册/命名空间 | `net/neoforged/neoforge/registries/DeferredRegister.java` | create/createItems/createBlocks 的 namespace 参数用于构造注册 ID | 一个实际 Mod 中两个旧命名空间的资源、标签、结构、网络和打包完整运行 |
| 玩家数据与死亡 | `net/neoforged/neoforge/attachment/AttachmentType.java` | 附件类型需注册；可序列化实体附件默认不在死亡时复制，可通过 copyOnDeath 选择复制；方块/区块变更需相应标脏 | 每类档案的死亡/寿尽/轮回政策、事件顺序及持久化测试 |
| 实体复制事件 | `net/neoforged/neoforge/event/entity/player/PlayerEvent.java` 的 Clone | 提供原对象和是否死亡信息；复制不限于普通死亡 | 与附件自动复制协作时不会重复执行玩法损失/奖励 |
| 网络注册 | `net/neoforged/neoforge/network/event/RegisterPayloadHandlersEvent.java` | payload 注册发生于 Mod 总线；使用前需登记；单人内存连接也序列化 | 实际协议、长度约束、身份/菜单校验与拒绝路径 |
| 网络执行线程 | `net/neoforged/neoforge/network/registration/PayloadRegistrar.java` | 初始 handler 线程为 MAIN；支持 playToServer/playToClient 和显式线程选择 | 不因旧经验再套重复异步队列；所有玩法修改在服务端主线程 |
| 服务端重载 | `net/neoforged/neoforge/event/AddReloadListenerEvent.java` | 游戏事件总线提供重载监听器；此时内建和动态注册表已冻结；监听器得到 registry lookup | 跨系统定义快照一致性、失败恢复和已运行会话处理必须自己实现 |

本地 `reference/vanilla-1.21.1` 是数据参考，不能假定其中存在完整 Minecraft Java 源码。Data Component 的具体 Codec/StreamCodec、SavedData 工厂、菜单容量、实体生成和渲染注册等，应在实现时查阅目标开发环境解析出的对应源码；本轮只设计数据归属，不凭空指定未核实构造签名。

基础依赖候选采用本地可核实版本；不声称它们是最新版本。构建与第三方库的具体组合必须在[阶段 B](../development/roadmap.md)完成实际解析和启动验证。
