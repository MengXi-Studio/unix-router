# unix-router 使用规则

> 本文件面向 AI 编码助手。当项目引入了 `@meng-xi/unix-router`（npm 包）或 `ux-router`（uni_modules 分发，导入路径 `@/uni_modules/ux-router/utssdk/index.uts`）时，必须遵循以下约定编写代码。所有代码示例均为 UTS / uni-app x（uvue）语法。

## 1. 引入形态判定

两种形态 API 完全一致，仅 import 路径不同，同一项目禁止混用：

- npm 形态：`import { createRouter } from '@meng-xi/unix-router'`
- uni_modules 形态：`import { createRouter } from '@/uni_modules/ux-router/utssdk/index.uts'`

判定方法：项目存在 `uni_modules/ux-router/` → 用 uni_modules 路径；`package.json` 依赖含 `@meng-xi/unix-router` → 用 npm 路径。

## 2. 标准初始化

```ts
// src/router/index.uts
import { createRouter } from '@meng-xi/unix-router'
import type { RouteConfig } from '@meng-xi/unix-router'

export const routes: RouteConfig[] = [
	{ path: 'pages/index/index', name: 'home', meta: { title: '首页', isTab: true } },
	{ path: 'pages/detail/detail', name: 'detail', meta: { title: '详情' } }
]

export const router = createRouter({ routes, strict: true })
```

```ts
// src/main.uts —— 必须在 createApp 中 app.use(router)，否则 useRouter() 拿不到实例
import { createSSRApp } from 'vue'
import App from './App.uvue'
import { router } from './router'

export function createApp() {
	const app = createSSRApp(App)
	app.use(router)
	return { app }
}
```

## 3. 强制约定（红线）

1. **插件注册必须实例化**（0.7.0 起破坏性变更）：`plugins: [new ParamsPlugin()]`。写 `plugins: [ParamsPlugin]` 是错误。
2. **query / params 是 `Map<string, string>`**，不是普通对象。构造：`new Map<string, string>([['id', '1']])`；读取：`.get(key)` / `.has(key)`；禁止用点号或下标访问。值只能是字符串，复杂对象先 `JSON.stringify` 序列化，读取时自行解析。
3. **可空值用 `null`，禁止 `undefined`**。判断存在性用 `!= null` / `== null`；条件语句必须是布尔表达式（`if (redirect != null)`，禁止 truthy 写法 `if (redirect)`）。
4. **`RouteConfig.path` 不带前导斜杠**（`pages/index/index`，与 `pages.json` 一致）；导航输入（`push` 的字符串或对象 path）带不带前导斜杠均可。
5. **TabBar 页（`meta.isTab: true`）自动走 `uni.switchTab`，无法用 query / params 传参**（原生限制，params 与 events 的内部通道同样经 query 桥接，switchTab 下均不可达）。跨 TabBar 页传数据改用全局状态、`uni.setStorageSync` 或 `eventBus` 广播（不经导航 URL，tab 页可收到）。
6. **业务代码直调 `uni.navigateTo` / `uni.switchTab` 等不经过路由器与守卫**。需要拦截时初始化 `interceptUniApi: true` 并注册 `new InterceptorPlugin()`；拦截有运行时版本门槛（Web 4.0 / 微信 4.41 / Android 3.97 / iOS 4.11 / Harmony 4.61），不满足自动降级。拦截只覆盖 `uni.*` API 直调：微信小程序 `<navigator>` 组件跳转与 tabBar 点击不经过 `uni.switchTab`，无法拦截，需在页面 `onShow` 用守卫兜底。
7. **App / 小程序端在每个页面 `onShow` 手动调用 `router.syncRoute()`**（物理返回键、TabBar 切换不经过路由器，需对齐状态）。H5 端 `app.use(router)` 已注入全局 mixin 自动同步，无需手动调。注意 `onShow` 触发早于 `syncRoute()` 完成，此时 `useRoute()` 读到的是旧值——读本页初始 query 应在 `onLoad(options)` 直接取 uni 传入参数。
8. **功能依赖插件，未注册即失效**：
   - `params` 传参 → 必须注册 `new ParamsPlugin()`，否则抛 `PLUGIN_REQUIRED`
   - 页面事件通信（`events` / `useOpenerEventChannel`）→ 必须注册 `new EventsPlugin()`
   - 导航动画（`animation` 选项 / `animationType` 单次覆盖）→ 必须注册 `new AnimationPlugin()`，未注册时该选项被静默忽略
   - `paramsPersistent` / `interceptUniApi` → 分别依赖 ParamsPlugin / InterceptorPlugin，未注册时忽略并警告
9. **导航动画字段官方仅 App 支持**：
   - App（Android 4.18+ / iOS 4.25+ / HarmonyOS 4.61+）：原生窗口动画透传
   - H5：AnimationPlugin 用 Web Animations API 模拟
   - 小程序：官方不支持动画字段，无动画
   - 单次覆盖 `animationType` / `animationDuration` 仅对 `push` / `replace` / `relaunch` 有效；`back()` 无 location 参数，使用全局默认动画（自动映射为退出型）
10. **守卫返回值约定**（`NavigationGuardReturn`）：`true` 或 `null` 放行；`false` 中止（`ABORTED`）；返回 `Error` 中止并作为失败原因（`CANCELLED`）；返回 `string`（路径）或位置对象 `{ path / name / query / params }` 即重定向（沿用原导航方式）；返回 `{ location, mode: 'push' | 'replace' | 'relaunch' }` 重定向并指定导航方式。重定向深度上限 `MAX_REDIRECT_DEPTH = 10`，超限按 `CANCELLED` 处理。**守卫内禁止调 `router.push`**（会发起新导航被排队，产生不可预期的导航序列，重定向一律 `return` 位置）；所有分支必须显式 `return`，漏 return 返回 undefined 行为不可靠。
11. **页面内守卫与组合式 API 只能在页面组件中使用**：`onBeforeRouteLeave` / `onBeforeRouteEnter` / `onBeforeRouteUpdate`、`useRouter()` / `useRoute()` / `useLink()` / `useOpenerEventChannel()`（返回可空值，使用前判空）。
12. **冷启动守卫**：H5 直达 / deeplink / 场景值进入时页面已加载但守卫未执行，需在 `router.isReady()` 后调用 `router.guardRoute()` 补跑守卫链，`onAbort` 回调中处理中止（如 `relaunch` 到安全页）。
13. **自定义 meta 字段必须修改 `RouteMeta` type 定义**：内置字段仅 `title` / `isTab` / `requireAuth` 三个；UTS 不支持接口声明合并，`declare module` 合并写法在原生端无效，自定义字段（如 `icon` / `tag`）需直接在 `RouteMeta` 类型定义处追加（fork / 复制的 `route.uts`）。随意写未声明字段会编译失败。
14. **vue-router 习惯写法禁用**（扁平页面模型，均不支持）：嵌套路由 `children`、动态路由 `addRoute` / `removeRoute`、动态段 `/user/:id`、`<router-view>` / 命名视图、`scrollBehavior`、hash 路由、`go(n)`（用 `back(delta)`）、守卫 `next()` 回调（改为直接返回结果）、`history` 选项、`active-class` 等 RouterLink 扩展 props（激活态基于 `useLink()` 的 `isActive` / `isExactActive` 自实现）。动态段传参一律改用 query / params。

## 4. 平台差异速查

| 差异点 | H5 | App（Android / iOS / HarmonyOS） | 小程序 |
| --- | --- | --- | --- |
| 编译产物 | JS | Kotlin / Swift / ArkTS | JS |
| 导航动画 | WAAPI 模拟 | 原生 `animationType` / `animationDuration` 透传 | 官方不支持，无动画 |
| `onShow` 路由同步 | 全局 mixin 自动 | 手动 `router.syncRoute()` | 手动 `router.syncRoute()` |
| `RouteName` 字面量类型 | `RouteNameMap` 模块增强（字面量提示） | 退化为 `string`（UTS 不支持 keyof 组合类型） | 退化为 `string` |
| uni API 拦截版本门槛 | 4.0 | 3.97 / 4.11 / 4.61 | 4.41（微信） |

## 5. 错误码速查（`RouterErrorCode`，数值对齐 vue-router 4）

| 错误码 | 值 | 含义 |
| --- | --- | --- |
| `ABORTED` | 4 | 导航被守卫中止（返回 false 或重定向异常）；含 `back()` 的 delta 非正整数 |
| `CANCELLED` | 8 | 守卫抛出异常或重定向超过最大深度；含 `back()` 页面栈不足 |
| `DUPLICATED` | 16 | 重复 push 当前地址（path+query+params+hash 完全一致；**仅 push 检测**） |
| `ROUTE_NOT_FOUND` | 32 | 未匹配到路由（strict 模式下未匹配命名路由抛出） |
| `NAVIGATION_API_ERROR` | 64 | `uni.*` 原生导航 API 调用失败 |
| `SETUP_ERROR` | 128 | 路由安装环境错误 |
| `PLUGIN_REQUIRED` | 256 | 使用了插件功能但未注册对应插件 |

错误判断：`isNavigationFailure(error)`（0.8.0+ 首参签名 `any | null`）；全局捕获 `router.onError`。`beforeEach` / `beforeResolve` / `afterEach` / `onError` / `onRouteChange` 注册均返回取消注册函数；catch 中的错误需 `as NavigationFailure` 收窄后才能读取 `code` / `to` / `from` 字段。并发导航自动排队串行执行；`back()` / `syncRoute()` 只更新 `router.currentRoute`，不回写 `useRoute()`。

## 6. 常见编译 / 运行错误排查

| 现象 | 原因与修复 |
| --- | --- |
| `.catch((e) => ...)` Android 编译失败 | UTS 的 `any` 编译为非空 Kotlin `Any`，回调参数写 `any \| null`；0.8.0+ 源码已内置 |
| `isNavigationFailure(e)` 编译失败 | 0.7.x 签名为 `Error \| null`，升级到 0.8.0+（已放宽为 `any \| null`） |
| H5 端 `router.push` 抛 TypeError | 0.7.x 缺陷（字段缺省为 `undefined`），升级到 0.8.0+ |
| App 端无导航动画 | 确认注册 `AnimationPlugin`；确认动画字段在 App 平台（4.18+ / 4.25+ / 4.61+）；小程序端官方无动画 |
| `useRouter()` 返回空 / 守卫不执行 | 确认 `app.use(router)` 已调用；确认路由守卫链未被拦截降级 |
| 未匹配命名路由抛错 | strict 模式（默认）行为；核对 `RouteConfig` 注册的 `name`，或改用 path 导航 |
| 使用 `params` / `events` 抛 `PLUGIN_REQUIRED` | 注册对应插件后再用 |

## 7. 版本要求

- 建议 0.8.0+：含文件路由生成插件（`@meng-xi/unix-router/vite-plugin`）、Android 云打包编译修复、App 端动画透传补全、H5 修复
- 0.7.0+：插件必须实例化注册
- 完整文档：仓库 `packages/docs`（更新日志 `packages/docs/src/changelog.md`）
