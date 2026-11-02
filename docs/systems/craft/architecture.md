---
topic: craft
role: architecture
status: draft
updated: 2026-11-02
---

# 工艺：架构调查与提案

来源事实与目标方案分别陈述。目标方案未批准；源码定位采用 [系统导航](../README.md)的 A/B 规则。

## 1.18.2 范围

砧板/石磨、木匠台、纺织/缝纫、陶轮/陶坯与蚕丝链。陶轮界面的 `TaoguanGuipageup`、`TaoguanGuipagedown` 由 `TaolunGuiButtonMessage` 触发，翻的是陶轮配方页。蚕蛾只由 `A:CanheGengXinYouXiKeProcedure:594` 生成，实体类没有专属过程；结茧、羽化由蚕盒驱动。

## 目标所有权与接口提案

工作台与蚕盒的进度状态归本系统；职业进度归 profession。生产入口按具体工艺实现预览、开始、推进和完成。

## 验收

各设备槽数、消耗与产物、拆除与卸载恢复分别核对。
