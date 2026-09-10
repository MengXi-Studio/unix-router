# 介绍

`@meng-xi/unix-router` 是一个为 **uni-app x** 提供的路由管理库，API 风格对标 **vue-router 4**，但基于 uni-app x 的**静态页面模型**（`.uvue` 页面 + `pages.json` 注册 + `uni.*` 原生导航）编写，核心语言为 **UTS**（`.uts`）。

## 为什么需要它

uni-app x 原生提供了 `uni.navigateTo / redirectTo / reLaunch / navigateBack / switchTab`，当页面增多后直接使用这些 API 会有明显痛点：

- **无路由表**：页面路径散落各处，无集中管理，缺少命名路由与类型提示。
- **无守卫**：无法在跳转前统一做登录校验、埋点等逻辑。
- **无路由状态**：没有统一的 `currentRoute` 响应式对象，难以在组合式 API 中追踪当前页。
- **参数传递繁琐**：对象类型参数难以跨页传递，查询解析需要手写。

unix-router 用一套与 vue-router 一致的 API 抹平这些差异。

## 适合谁

- **刚从 uni-app（Vue3）迁到 uni-app x**：用熟悉的路由写法快速过渡。
- **页面已具规模、需要集中管理导航与鉴权**：路由表 + 守卫让跳转链路清晰可控。
- **希望跨 web / 小程序 / App 保持路由行为一致**：一套 API 覆盖多端。

## 本指南如何带你「从入门到精通」

文档按递进式结构组织，建议按顺序阅读：

| 阶段 | 主题 | 你将掌握 |
| --- | --- | --- |
| 🟢 **入门** | [快速开始](./getting-started) → [路由配置](./route-config) → [路由导航](./navigation) | 从零跑通第一个跳转，理解路由表、四种导航与参数传递 |
| 🟢 **入门** | [读取路由](../api/use-route) / [组合式 API](./composables) | `useRouter` / `useRoute` / `useLink` 与生命周期联动 |
| 🟡 **进阶** | [路由守卫](./guards) → [路由元信息](./meta) | 登录鉴权、`beforeEnter`、冷启动守卫、动态标题 |
| 🟡 **进阶** | [错误处理](./error-handling) → [导航流程](./navigation-flow) | 失败判定、全局错误捕获、理解一次导航的内部机制 |
| 🔴 **精通** | [完整实战](./recipes) → [平台兼容性](./compatibility) | 用一套模式搭出登录+TabBar+详情完整应用，掌握平台差异与常见坑 |

> **提示**：代码仓库内的 [`packages/playground`](https://github.com/MengXi-Studio/unix-router/tree/master/packages/playground) 是一个可直接运行的 uni-app x 工程，内含 `pages/test` 功能自检页（程序化输出 PASS/FAIL），可边看文档边对照验证。

## 核心能力一览

| 能力 | 说明 |
| --- | --- |
| 路由匹配 | path / name 双索引，字符串 / 对象 / 命名三种解析方式，严格模式 |
| 导航 | push / replace / relaunch / back，自动识别 TabBar |
| 守卫 | beforeEach / beforeResolve / afterEach / beforeEnter / onBeforeRouteLeave 等 |
| 组合式 API | useRouter / useRoute / useLink |
| 错误体系 | RouterError / NavigationFailure / isNavigationFailure |
| 扩展 | guardRoute 冷启动守卫、重复导航拦截、守卫重定向 + 深度上限 |
| 插件体系 | RouterPlugin / PluginContext（8 个 hook）/ ParamsPlugin / InterceptorPlugin（opt-in） |

> 安装：`npm install @meng-xi/unix-router`

## 下一步

- 想立即动手？从[安装](./installation)或[快速开始](./getting-started)开始。
- 想了解设计取舍？查看[与 vue-router 的差异](./differences)。