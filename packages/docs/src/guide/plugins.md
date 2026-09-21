# 插件系统

unix-router 采用**核心 + 插件**架构（对齐 uni-router / Swiper.js 风格）：

- **核心**只做四件事：路由匹配、导航执行、守卫链、状态同步；
- **其余扩展能力**（页面参数、页面间通信、导航动画、uni API 拦截等）全部插件化，**opt-in** 按需注册。

不注册插件时，对应能力完全不存在：核心包保持精简稳定；未注册却使用会直接抛 `PLUGIN_REQUIRED` 错误，明确引导而非静默失败。

## 内置插件速览

| 插件 | name | 能力 | 配套选项 | 详情 |
| --- | --- | --- | --- | --- |
| `ParamsPlugin` | `params` | 页面参数传递（内存 / 持久化） | `paramsPersistent` | [参数传递](./params) |
| `EventsPlugin` | `events` | 页面间通信（`events` 监听表 + EventChannel 回传） | — | [页面间通信](./events) |
| `AnimationPlugin` | `animation` | 导航窗口动画（原生透传 / H5 WAAPI） | `animation` | [导航动画](./animation) |
| `InterceptorPlugin` | `interceptor` | 拦截 uni 原生导航 API，守卫下沉到 uni API 层 | `interceptUniApi` | [uni API 拦截](./interceptor) |

## 注册插件

插件是 `RouterPlugin` 抽象类的实例，注册时**必须实例化**：

```ts
import { createRouter, ParamsPlugin, EventsPlugin, AnimationPlugin, InterceptorPlugin } from '@meng-xi/unix-router'

const router = createRouter({
	routes,
	plugins: [new ParamsPlugin(), new EventsPlugin(), new AnimationPlugin(), new InterceptorPlugin()],
	interceptUniApi: true // 须配合 InterceptorPlugin，默认 false
})
```

::: warning 旧写法已废弃
`plugins: [ParamsPlugin]`（直接传类）为旧版写法，已废弃。插件以 abstract class 实现，必须传入实例 `new XxxPlugin()`。
:::

插件按数组顺序安装，安装时通过 `PluginContext` 注册 hook，注入到导航流程的各个阶段。设置了插件配套选项但未注册对应插件时（如 `interceptUniApi: true` 却没有 `InterceptorPlugin`），该选项被忽略并输出警告。

## RouterPlugin 抽象类

自定义插件继承 `RouterPlugin`，实现 `name` 字段与 `install(context, options)` 抽象方法：

```ts
import { RouterPlugin } from '@meng-xi/unix-router'
import type { PluginContext, RouterOptions } from '@meng-xi/unix-router'

class MyPlugin extends RouterPlugin {
	name = 'my-plugin'

	install(context: PluginContext, options: RouterOptions): void {
		// 通过 context 注册 hook，从 options 读取插件选项
	}
}
```

- `name: string`：插件唯一标识，`hasPlugin(name)` 依据它判断注册状态（`params` / `events` 的 `PLUGIN_REQUIRED` 预检也按 name 匹配）。
- `install(context, options)`：安装时调用一次，在 `createRouter` 内部、任何导航发生之前执行。

## PluginContext：8 个 hook

每个 hook 在导航流程的固定时机被调用。逐一举例：

| hook | 签名 | 触发时机 |
| --- | --- | --- |
| `onEnrichLocation` | `(location: RouteLocationRaw) => RouteLocationRaw` | `matcher.resolve()` **之前**，增强原始路由位置（如注入内部 key）。链式执行，前一个的返回值是下一个的输入 |
| `onAfterResolve` | `(enrichedLocation: RouteLocationRaw, pluginData: PluginData) => void` | resolve 之后、守卫链之前，从增强后的位置中提取插件数据写入 `pluginData` |
| `onPrepareNavigation` | `(ctx: NavigationPrepareContext) => void` | uni API 调用**之前**，可修改导航 URL 的 `ctx.query` 与 `ctx.options`（如追加内部 key、改写动画参数） |
| `onBeforeNavigation` | `(ctx: NavigationPrepareContext) => Promise<void> \| void` | 真正调用 uni API 之前，**可异步**（如 H5 返回需先播完退出动画），多个 hook **串行**执行 |
| `onCompleteNavigation` | `(ctx: NavigationCompleteContext) => void` | uni API 调用**成功**且页面栈确认后，可扩展导航结果 `ctx.result` |
| `onNavigationAbort` | `(pluginData: PluginData) => void` | 导航中止或失败时执行清理（异常被吞掉，不会中断失败流程） |
| `onRouteSync` | `(query: Map<string, string>, params: Map<string, string>) => void` | 路由状态同步期间，从 URL query 提取插件数据（如按 `__params__` key 重建 params），内部 key 应从此处移除 |
| `onAppInstall` | `(app: any) => void` | `router.install()` 被调用（`app.use(router)`）时触发，可注册 app 级清理逻辑 |

::: tip PluginData
`PluginData` 即 `Map<string, any>`，一次导航内各阶段共享：`onAfterResolve` 写入 → `onPrepareNavigation` / `onBeforeNavigation` / `onCompleteNavigation` 读取 → `onNavigationAbort` 清理。重定向会复用同一个 `pluginData`。
:::

## 上下文成员

`PluginContext` 除 hook 注册方法外，还暴露：

| 成员 | 类型 | 说明 |
| --- | --- | --- |
| `currentRoute` | `RouteLocation` | 当前路由位置（只读 getter，经注入的读取器实时取值） |
| `resolve` | `(location: RouteLocationRaw) => RouteLocation` | 解析路由位置为完整 `RouteLocation`（与 `router.resolve` 等价） |
| `router` | `any` | 路由器实例引用。声明为 `any` 以规避原生端 interface 跨文件退化，插件内按需 `as Router` |
| `paramsManager` | `any` | 核心共享的 `ParamsManager` 实例（供 `ParamsPlugin` 使用；按需 `as ParamsManager`） |
| `hasPlugin` | `(name: string) => boolean` | 检查指定插件是否已注册 |

## 完整自定义插件示例

以「导航埋点」插件为例，演示 class 继承、多 hook 协作与 `pluginData` 跨阶段传值：

```ts
import { RouterPlugin } from '@meng-xi/unix-router'
import type {
	PluginContext,
	PluginData,
	RouterOptions,
	NavigationPrepareContext,
	NavigationCompleteContext
} from '@meng-xi/unix-router'

/** 埋点插件：为每次导航打上开始时间戳，成功后上报 */
class AnalyticsPlugin extends RouterPlugin {
	name = 'analytics'

	override install(context: PluginContext, options: RouterOptions): void {
		// 1. resolve 前记录开始时间（写入 pluginData 的挂载点在 afterResolve 更稳妥）
		context.onEnrichLocation((location) => {
			// 如需给导航 URL 追加内部 key，在这里返回增强后的位置
			return location
		})

		// 2. resolve 后：提取数据，记录导航开始时间
		context.onAfterResolve((enrichedLocation, pluginData) => {
			pluginData.set('startedAt', Date.now())
		})

		// 3. uni API 调用前：改写导航 query（可选，示例追加时间戳标记）
		context.onPrepareNavigation((ctx: NavigationPrepareContext) => {
			ctx.query.set('_t', String(Date.now()))
		})

		// 4. 导航成功后上报
		context.onCompleteNavigation((ctx: NavigationCompleteContext) => {
			const startedAt = ctx.pluginData.get('startedAt')
			reportAnalytics(ctx.to.path, startedAt as number)
		})

		// 5. 中止 / 失败时清理
		context.onNavigationAbort((pluginData: PluginData) => {
			pluginData.clear()
		})
	}
}

// 注册：传入实例
const router = createRouter({ routes, plugins: [new AnalyticsPlugin()] })
```

## 执行顺序

`createRouter` 创建时按 `plugins` 数组顺序安装；一次前向导航中 hook 按阶段执行：

```
push / replace / relaunch
  → onEnrichLocation（增强原始位置）
  → matcher.resolve（解析目标）
  → onAfterResolve（提取插件数据到 pluginData）
  → beforeEach → beforeEnter → beforeResolve（守卫链）
  → onPrepareNavigation（改 query / 选项）
  → onBeforeNavigation（可异步，串行）
  → uni API（navigateTo / redirectTo / reLaunch / switchTab）
  → 页面栈顶确认 → onCompleteNavigation → afterEach
  →（任一环节失败）onNavigationAbort → afterEach(failure) → onError
```

## 平台注意（UTS 强类型）

::: warning 非蒸汽（Kotlin / Swift）端必须用 class 实现插件
在编译到 Kotlin / Swift 的平台上，**含方法的对象字面量会被推断为 `UTSJSONObject`**，无法作为插件工作。因此：

- 插件必须以 **class 继承 `RouterPlugin`** 实现，hook 注册均为 class 方法；
- 注册时传入**实例** `plugins: [new MyPlugin()]`，不能传类或对象字面量；
- 同理 `PluginContext` 也是 class 而非 type。
:::

## 下一步

- [导航流程原理](./navigation-flow) — 各 hook 在完整导航时序中的精确位置
- [RouterOptions](../api/type-router-options) — `plugins` 与各插件配套选项
- [参数传递](./params) / [页面间通信](./events) / [导航动画](./animation) / [uni API 拦截](./interceptor) — 四个内置插件的完整用法
