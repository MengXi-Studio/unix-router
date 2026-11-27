# 实施记录索引

按时间倒序追加，每行：日期 · 任务文档相对链接 · 一句话结果。

- 2026-11-26 · [route-gen-页面style字段](./新增/2026-11-26/route-gen-页面style字段.md) · 实现 issue #10：`defineUniPage` / `<route-config>` 支持页面级 `style` 原样合并进 `pages.json`（style 优先于 title 生成的 `navigationBarTitleText`，raw 表达式告警忽略）；临时 fixture 运行时验证 7 项断言全过，lint / typecheck / sync:check / build:docs 全绿
- 2026-11-10 · [小程序原生返回-路由状态陈旧](./修复/2026-11-10/小程序原生返回-路由状态陈旧.md) · 修复 issue #7：`onShow` mixin 扩至小程序端 + 导航前页面栈自愈，消除原生返回后的 `DUPLICATED` 误判；类型检查通过，MP 运行时待微信开发者工具复测
