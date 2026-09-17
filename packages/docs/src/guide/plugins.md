# 插件系统

unix-router 采用**核心 + 插件**架构（对齐 uni-router / Swiper.js 风格）：

- **核心**只提供路由匹配、导航执行、守卫链、状态同步等 uni-app x 原生能力；
- **扩展能力**（页面参数、uni 原生导航拦截等）通过插件提供，按需注册。

## 设计原则

1. **核心精简**：核心不含业务扩展，保持稳定与轻量。
2. **插件扩展**：非核心能力均以插件形式在 `createRouter({ plugins: [...] })` 注册。
3. **零侵入**：不注册插件时该功能不可用；未注册却使用会抛 `PLUGIN_REQUIRED` 错误，明确引导。
4. **可组合**：插件按数组顺序安装，通过 `PluginContext` 注册 hook，注入到导航流程的各个阶段。

## 快速上手

```ts
import { createRouter, ParamsPlugin, InterceptorPlugin } from '@meng-xi/unix-router'

const router = createRouter({
	routes,
	strict: true,
	plugins: [ParamsPlugin, InterceptorPlugin], // 启用：页面参数 + uni 导航拦截
	interceptUniApi: true // InterceptorPlugin 的开关，默认 false
})
```

> 只需注册你需要的插件。未注册插件却使用对应功能将抛出 `PLUGIN_REQUIRED`。

## 内置插件

| 插件 | name | 能力 | 配套选项 |
| --- | --- | --- | --- |
| `ParamsPlugin` | `params` | 页面参数传递（`params`，内存/持久化） | `paramsPersistent` |
| `InterceptorPlugin` | `interceptor` | 拦截 uni 原生导航 API，守卫下沉到 uni API 层 | `interceptUniApi` |

### ParamsPlugin：页面参数

uni-app x 是静态页面模型，URL 不适合携带复杂对象。ParamsPlugin 采用"**内存存储 + `__params__` 内部 key**"方案：发起页把 `params` 存入管理器并生成 key，目标页通过 key 取回。

```ts
import { createRouter, ParamsPlugin } from '@meng-xi/unix-router'

const router = createRouter({ routes, plugins: [ParamsPlugin] })

// 传递字符串参数（经内部 key 通道）
await router.push({
	name: 'detail',
	params: new Map<string, string>([['id', '1024']])
})

// 目标页读取（route.params 由 ParamsPlugin 在状态同步时重建）
const route = useRoute()
console.log(route.params.get('id')) // '1024'
```

**持久化到 storage**：默认 params 存内存；需要跨刷新/重进保留时开启持久化：

```ts
const router = createRouter({
	routes,
	plugins: [ParamsPlugin],
	paramsPersistent: true // 默认将所有 params 持久化到 uni storage
})
```

::: warning 需要注册 ParamsPlugin
未注册 `ParamsPlugin` 却调用带 `params` 的导航，会抛 `PLUGIN_REQUIRED`：

```
使用 params 需注册 ParamsPlugin：createRouter({ plugins: [ParamsPlugin] })
```
:::

**导出**：`ParamsPlugin`（插件本体）+ `createParamsManager(options)`（底层参数管理器，供自定义插件复用）。

### InterceptorPlugin：uni 导航拦截

直接调用 `uni.navigateTo` 会**绕过路由守卫**。启用拦截后，外部直接调用也会被转交 `router.*` 走完整守卫链，守卫"下沉"到 uni API 层。

```ts
import { createRouter, InterceptorPlugin } from '@meng-xi/unix-router'

const router = createRouter({
	routes,
	plugins: [InterceptorPlugin],
	interceptUniApi: true
})

// 现在两者等价，守卫都生效：
await router.push({ name: 'about' })        // 走 router，守卫生效
uni.navigateTo({ url: '/pages/about/about' }) // 被拦截 → 转交 router，守卫生效
```

**各端所需最低 HBuilderX 版本**（官方 addInterceptor 兼容表）：

| 平台 | 最低 HBuilderX 版本 |
| --- | --- |
| Web | 4.0 |
| 微信小程序 | 4.41 |
| Android | 3.97 |
| iOS | 4.11 |
| HarmonyOS | 4.61 |

::: warning 不影响路由器自身调用
拦截器仅针对**外部直接调用**生效；`router.push/replace/relaunch/back` 内部发起的 uni 调用不会二次拦截（通过内部标记区分）。
:::

拦截的 API：`navigateTo / redirectTo / switchTab / reLaunch / navigateBack`。同时会给出 `installInterceptors` / `removeInterceptors` 供底层使用。

**导出**：`InterceptorPlugin`（插件本体，注册 `plugins: [InterceptorPlugin]` 且 `interceptUniApi: true` 时自动安装）+ `installInterceptors(router)` / `removeInterceptors()`（手动安装/卸载拦截器，用于 Router 实例化后的精细化控制）。

### AnimationPlugin：导航窗口动画

为导航注入窗口过渡动画（对齐 uni-app x 原生 `animationType`）：
- **App / 小程序**：透传 `animationType` / `animationDuration` 给 `uni.*` 原生导航 API（原生窗口动画）；
- **H5**：通过 Web Animations API（`element.animate`）对页面容器播放进入 / 退出动画（无需 CSS `@keyframes`）。

```ts
import { createRouter, AnimationPlugin } from '@meng-xi/unix-router'

const router = createRouter({
	routes,
	plugins: [AnimationPlugin],
	animation: { type: 'slide-in-right', duration: 300 } // 全局默认动画（可选）
})

// 单次覆盖：本次导航使用 fade-in
router.push({ path: 'pages/detail/detail', animationType: 'fade-in', animationDuration: 500 })
```

**动画类型**：`slide-in-right` / `slide-in-left` / `slide-in-top` / `slide-in-bottom` / `fade-in` / `zoom-in` / `zoom-fade-in` / `pop-in` / `auto` / `none`。

- `back()` 使用全局默认动画作为**关闭动画**（back 无 location 可传，单次覆盖仅对前向导航有效）。
- 未注册插件时携带 `animationType` 的导航仍正常执行（动画被忽略）。
- H5 端依赖 `onBeforeNavigation` 异步钩子：返回时会先播完退出动画再真正 `navigateBack`。

**H5 动画时序**（防止首次进入卡顿）：`onCompleteNavigation` 时 uni-app x H5 已把新页内容替换进 `uni-page`（`data-page` 已切换），此时**同步应用动画起点样式**（如 `translateX(100%)` + 强制 reflow），让新页渲染首帧即位于屏幕外，再于下一帧播放滑入动画——避免"内容原位闪现后再跳到屏幕外滑入"的割裂感；动画结束后清理内联起点样式，避免残留影响后续 back 的退出动画。

**导出**：`AnimationPlugin`（插件本体）+ `DEFAULT_ANIMATION_DURATION`（默认动画时长常量，300ms）。

### EventsPlugin：页面间事件通信

补齐 uni-app x 的 `events`（页面间通信）能力：打开方 `push` 携带 `events` 监听表，被打开页通过通道**回传数据**给打开方（对齐 uni-app 官方 `navigateTo` events 语义，但不受官方 `uni.$on` 版本门槛限制）。

```ts
import { createRouter, EventsPlugin, useOpenerEventChannel } from '@meng-xi/unix-router'

const router = createRouter({ routes, plugins: [EventsPlugin] })

// 打开方：注册监听表，监听被打开页回传的数据
await router.push({
	path: 'pages/detail/detail',
	events: new Map([
		['acceptDataFromOpenedPage', (data: any) => console.log('收到回传', data)]
	])
})

// 被打开页：emit 回传数据 / on 接收打开方推送
const channel = useOpenerEventChannel() // EventChannel | null
if (channel !== null) {
	channel.emit('acceptDataFromOpenedPage', { result: 'ok' })
	channel.on('someEvent', (data: any) => {})
}
```

**EventChannel API**：`on` / `once` / `off`（移除监听器，传 `$on`/`$once` 返回的 id）/ `emit`。通道 key 经 URL 查询串 `__evt__` 跨页桥接，状态同步时剔除（不暴露给用户）。

`useOpenerEventChannel()` 不依赖路由状态同步时机：页面 `onShow` 执行早于 `onRouteSync`，内存 key 缺失时会按当前页面 URL 查询串（`__evt__`）兜底读取，因此 **onShow 内即可直接回传数据**。

::: warning 需要注册 EventsPlugin
未注册 `EventsPlugin` 却调用带 `events` 的导航，会抛 `PLUGIN_REQUIRED`：

```
使用 events 需注册 EventsPlugin：createRouter({ plugins: [EventsPlugin] })
```
:::

**导出**：`EventsPlugin`（插件本体）+ `eventBus`（自研全局事件总线实例 `$on` / `$off` / `$once` / `$emit`，按 id 移除监听器）。

## 插件上下文（PluginContext）

每个插件在 `install(context, options)` 里通过 `context` 注册 hook，路由器在导航流程各阶段调用：

| hook | 时机 | 用途 |
| --- | --- | --- |
| `onEnrichLocation` | `resolve` 前 | 增强原始路由位置（如注入内部 key） |
| `onAfterResolve` | resolve 后、守卫前 | 从增强位置提取插件数据 |
| `onPrepareNavigation` | uni API 调用前 | 修改导航 URL query 与选项 |
| `onCompleteNavigation` | uni API 调用成功后 | 扩展导航结果 |
| `onNavigationAbort` | 导航中止/失败时 | 清理插件资源 |
| `onRouteSync` | 状态同步期间 | 从 URL query 重建插件数据 |
| `onAppInstall` | `router.install()` 时 | 注册 app 级清理逻辑 |

`context` 还暴露只读成员：`currentRoute`、`resolve()`、`router`、`paramsManager`、`hasPlugin()`。

## 自定义插件

实现 `RouterPlugin` 接口（`name` + `install`）：

```ts
import type { RouterPlugin, PluginContext, RouterOptions } from '@meng-xi/unix-router'

/** 埋点插件：在每次导航完成时上报路由 */
const AnalyticsPlugin: RouterPlugin = {
	name: 'analytics',

	install(context: PluginContext, options: RouterOptions) {
		// 1. 导航开始前：给目标注入一个内部 key（可选）
		context.onEnrichLocation(location => location)

		// 2. resolve 后：记录目标
		context.onAfterResolve((enrichedLocation, pluginData) => {
			pluginData.set('timestamp', Date.now())
		})

		// 3. uni API 调用前：改写 query（可选）
		context.onPrepareNavigation(ctx => {
			// ctx.query.set('_t', String(Date.now()))
		})

		// 4. 导航成功后上报
		context.onCompleteNavigation(ctx => {
			const ts = ctx.pluginData.get('timestamp')
			reportAnalytics(ctx.to.path, ts as number)
		})

		// 5. 导航中止时清理
		context.onNavigationAbort(pluginData => {
			pluginData.clear()
		})
	}
}

// 注册
const router = createRouter({ routes, plugins: [AnalyticsPlugin] })
```

::: tip pluginData
`pluginData` 是 `Map`，在 `onAfterResolve` 阶段写入、`onPrepareNavigation/onCompleteNavigation/onNavigationAbort` 阶段读取，实现插件跨阶段传递数据。
:::

## 执行顺序

`createRouter` 创建时按 `plugins` 数组顺序安装；导航时 hook 按阶段执行：

```
push → enrichLocation → matcher.resolve → afterResolve
     → beforeEach → beforeEnter → beforeResolve
     → prepareNavigation → uni API → (成功) setRoute + completeNavigation + afterEach
     → (失败/中止) navigationAbort
```

## 下一步

- [导航守卫](./guards) — 守卫在插件流程中的位置
- [平台兼容性](./compatibility) — 各端 addInterceptor 版本要求
- [实战指南](./recipes) — 登录认证、权限、埋点等完整方案
- [API 参考](../api/create-router) — `plugins` / `interceptUniApi` / `paramsPersistent` 选项