# RouterPlugin / PluginContext

插件体系的类型参考：自定义插件继承 `RouterPlugin`，通过 `PluginContext` 注册 hook。用法与完整示例见[插件系统](../guide/plugins)。

```ts
import { RouterPlugin } from '@meng-xi/unix-router'
import type {
	PluginContext,
	PluginData,
	PluginNavigationMode,
	NavigationPrepareContext,
	NavigationCompleteContext
} from '@meng-xi/unix-router'
```

## RouterPlugin

插件抽象基类。**必须以 class 继承实现并传入实例**（Kotlin / Swift 端对象字面量含方法会被推断为 `UTSJSONObject`，无法作为插件工作）：

```ts
abstract class RouterPlugin {
	/** 插件名称（hasPlugin(name) 与 PLUGIN_REQUIRED 预检的匹配依据） */
	name: string
	/** 安装插件：通过 context 注册 hook，从 options 读取插件选项；createRouter 内部调用一次 */
	abstract install(context: PluginContext, options: RouterOptions): void
}
```

## PluginContext

路由器暴露给插件的 hook 注册接口（class 承载）：

**hook 注册（8 个）**：

| 成员 | 签名 | 触发时机 |
| --- | --- | --- |
| `onEnrichLocation` | `(hook: (location: RouteLocationRaw) => RouteLocationRaw) => void` | `matcher.resolve()` 之前增强原始位置，链式执行 |
| `onAfterResolve` | `(hook: (enrichedLocation: RouteLocationRaw, pluginData: PluginData) => void) => void` | resolve 之后、守卫链之前，提取插件数据 |
| `onPrepareNavigation` | `(hook: (ctx: NavigationPrepareContext) => void) => void` | uni API 调用前，可修改 `ctx.query` / `ctx.options` |
| `onBeforeNavigation` | `(hook: (ctx: NavigationPrepareContext) => Promise<void> \| void) => void` | 真正调用 uni API 之前，可异步，多个 hook 串行 |
| `onCompleteNavigation` | `(hook: (ctx: NavigationCompleteContext) => void) => void` | uni API 成功且页面栈确认后，可扩展 `ctx.result` |
| `onNavigationAbort` | `(hook: (pluginData: PluginData) => void) => void` | 导航中止 / 失败时清理（异常被吞掉） |
| `onRouteSync` | `(hook: (query: Map<string, string>, params: Map<string, string>) => void) => void` | 路由状态同步期间，从 URL query 提取插件数据 |
| `onAppInstall` | `(hook: (app: any) => void) => void` | `app.use(router)` 时触发 |

**上下文成员**：

| 成员 | 类型 | 说明 |
| --- | --- | --- |
| `currentRoute` | `RouteLocation` | 当前路由位置（只读 getter，实时取值） |
| `resolve` | `(location: RouteLocationRaw) => RouteLocation` | 解析路由位置，与 `router.resolve` 等价 |
| `router` | `any` | 路由器实例引用（规避原生端 interface 跨文件退化，插件内按需 `as Router`） |
| `paramsManager` | `any` | 核心共享的 `ParamsManager` 实例（按需 `as ParamsManager`） |
| `hasPlugin` | `(name: string) => boolean` | 检查指定插件是否已注册 |

## PluginData

```ts
type PluginData = Map<string, any>
```

一次导航内各插件阶段共享的数据容器：`onAfterResolve` 写入 → `onPrepareNavigation` / `onBeforeNavigation` / `onCompleteNavigation` 读取 → `onNavigationAbort` 清理。重定向会复用同一个 `pluginData`。

## NavigationPrepareContext

`onPrepareNavigation` / `onBeforeNavigation` 的上下文：

| 成员 | 类型 | 说明 |
| --- | --- | --- |
| `to` / `from` | `RouteLocation` | 目标 / 来源路由 |
| `mode` | `PluginNavigationMode` | `'push' \| 'replace' \| 'relaunch' \| 'back'` |
| `pluginData` | `PluginData` | 阶段共享数据 |
| `query` | `Map<string, string>` | 实际导航 URL 的 query（**可变**：可添加内部 key） |
| `options` | `UniNavigationOptions` | uni 导航选项（**可变**：可修改动画参数等） |

## NavigationCompleteContext

`onCompleteNavigation` 的上下文：

| 成员 | 类型 | 说明 |
| --- | --- | --- |
| `to` | `RouteLocation` | 目标路由 |
| `mode` | `PluginNavigationMode` | 导航模式 |
| `pluginData` | `PluginData` | 阶段共享数据 |
| `result` | `RouteLocation` | 导航结果（**可变**：可扩展） |

## 内置插件与辅助导出

| 导出 | 说明 | 指南 |
| --- | --- | --- |
| `ParamsPlugin` / `createParamsManager` | 页面参数传递（内存 / 持久化） | [参数传递](../guide/params) |
| `EventsPlugin` / `eventBus` | 页面间通信（events 注册表 + EventChannel） | [页面间通信](../guide/events) |
| `AnimationPlugin` | 导航窗口动画（App 原生透传 / H5 WAAPI） | [导航动画](../guide/animation) |
| `InterceptorPlugin` / `installInterceptors` / `removeInterceptors` | 拦截 uni 原生导航 API | [uni API 拦截](../guide/interceptor) |

## 相关 API

- [插件系统](../guide/plugins) — 架构说明、注册方式与完整自定义插件示例
- [RouterOptions](./type-router-options) — `plugins` 与各插件配套选项
- [导航流程原理](../guide/navigation-flow) — 各 hook 在导航时序中的精确位置
