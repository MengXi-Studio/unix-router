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
| 🟢 **入门** | [介绍](./introduction) → [安装](./installation) → [快速开始](./getting-started) | 从零跑通第一个跳转：安装依赖、实例化注册插件、创建路由器、发起导航 |
| 🟢 **核心功能** | [路由配置](./route-config) → [路由导航](./navigation) → [组合式 API](./composables) | 路由表与命名路由、四种导航与 TabBar 识别、useRouter / useRoute / useLink |
| 🟡 **进阶** | [路由元信息](./meta) → [路由守卫](./guards) → [错误处理](./error-handling) | meta 驱动页面行为、完整守卫链与登录鉴权、失败判定与全局捕获 |
| 🟠 **插件体系** | [插件系统（总览）](./plugins) → [参数传递](./params) → [页面间通信](./events) → [导航动画](./animation) → [uni API 拦截](./interceptor) | 4 个内置插件（Params / Events / Animation / Interceptor）逐一精讲 + PluginContext 8 个 hook，编写自定义插件 |
| 🔴 **精通** | [导航流程原理](./navigation-flow) → [完整实战](./recipes) → [平台兼容性](./compatibility) → [与 vue-router 的差异](./differences) → [常见问题](./faq) | 理解一次导航的内部机制、搭出登录+TabBar+详情完整应用、平台差异与高频坑排查 |

> **提示**：代码仓库内的 [`packages/playground`](https://github.com/MengXi-Studio/unix-router/tree/master/packages/playground) 是一个可直接运行的 uni-app x 工程，内含 `pages/test` 功能自检页（程序化输出 PASS/FAIL），可边看文档边对照验证。

## 核心能力一览

| 能力 | 说明 |
| --- | --- |
| 路由匹配 | path / name 双索引，字符串 / 对象 / 命名三种解析方式；`strict` 严格模式下未匹配命名路由抛 `ROUTE_NOT_FOUND` |
| 导航 | push / replace / relaunch / back，`meta.isTab` 自动 `switchTab`；并发导航自动排队串行执行，仅 push 检测重复导航（`DUPLICATED`） |
| 守卫 | beforeEach / beforeEnter（路由独享）/ beforeResolve / afterEach + 组件内 onBeforeRouteLeave / Update / Enter；返回值风格重定向，深度上限 10，`guardTimeout` 超时保护 |
| 冷启动守卫 | `guardRoute(location?, { onAbort? })` 对直达页补执行守卫链（不实际导航，redirect 默认 `relaunch` 执行真实跳转） |
| 参数与查询 | query 直接进 URL；params 经 ParamsPlugin（`__params__` 内部 key 通道，可 `paramsPersistent` 持久化）；`queryInt` / `queryNumber` / `queryBool` 类型化读取 |
| 组合式 API | useRouter / useRoute / useLink / useOpenerEventChannel，以及组件内守卫三件套（onBeforeRouteLeave / Update / Enter） |
| 页面事件通信 | EventsPlugin：push 携带 events 监听表创建通道，被打开页 `useOpenerEventChannel` 回传/接收，另导出全局 `eventBus` |
| 导航动画 | AnimationPlugin：App / 小程序透传原生 `animationType` / `animationDuration`，H5 用 Web Animations API 实现，back 自动映射退出型动画 |
| uni API 拦截 | InterceptorPlugin + `interceptUniApi`：外部直接调用 `uni.navigateTo` 等也走守卫链（基于 `uni.addInterceptor`，运行时缺失自动降级） |
| 状态同步 | `syncRoute()` 按页面栈同步 `currentRoute`；H5 端 `install` 自动注册 onShow 全局 mixin；`onRouteChange` 监听路由变化 |
| 错误体系 | RouterError / NavigationFailure，7 类错误码（ABORTED / CANCELLED / DUPLICATED / ROUTE_NOT_FOUND / NAVIGATION_API_ERROR / SETUP_ERROR / PLUGIN_REQUIRED），导航失败一律 reject；onError + isNavigationFailure |
| 插件体系 | RouterPlugin 抽象类 + PluginContext 8 个 hook；内置 4 个插件：ParamsPlugin / EventsPlugin / AnimationPlugin / InterceptorPlugin，实例化注册 |

> 安装：`npm install @meng-xi/unix-router`

## 下一步

- 想立即动手？从[安装](./installation)或[快速开始](./getting-started)开始。
- 想了解设计取舍？查看[与 vue-router 的差异](./differences)。
