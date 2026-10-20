---
name: unix-router
description: 在 uni-app x 项目中使用 @meng-xi/unix-router（npm）或 ux-router（uni_modules）实现路由导航、守卫、参数传递、导航动画、页面事件通信、uni API 拦截或文件路由生成时使用。涵盖两种引入形态判定与 UTS 强类型约束下的正确写法。
---

# unix-router 使用技能

本库是 uni-app x 的 vue-router 风格路由库，UTS 源码分发，编译期转 JS / Kotlin / Swift / ArkTS。按以下流程操作；需要完整 API 速查表时读 `references/api-quickref.md`。

## 1. 判定引入形态

先检查项目形态，决定 import 路径（同一项目禁止混用）：

- 项目存在 `uni_modules/ux-router/` → uni_modules 形态，统一用 `@/uni_modules/ux-router/utssdk/index.uts`
- `package.json` 依赖含 `@meng-xi/unix-router` → npm 形态，统一用 `@meng-xi/unix-router`

以下示例以 npm 路径书写；uni_modules 项目把 import 来源整体替换即可，其余代码不变。

## 2. 标准初始化（新项目搭建）

1. 定义路由表：`RouteConfig[]`，`path` 与 `pages.json` 注册一致（**不带前导斜杠**），可加 `name` 与 `meta`
2. `createRouter({ routes })` 创建实例
3. `src/main.uts` 的 `createApp()` 中 `app.use(router)` —— 不装实例则 `useRouter()` 拿不到路由器
4. App / 小程序端在每个页面 `onShow` 调 `router.syncRoute()`；H5 端自动同步无需手调

## 3. 任务配方

### 页面跳转

```ts
const router = useRouter()
await router.push({ name: 'detail', query: new Map<string, string>([['id', '1']]) })
await router.push('/pages/detail/detail') // 字符串目标，前导斜杠可省
await router.replace({ path: '/pages/detail/detail', params: new Map<string, string>([['from', 'home']]) }) // params 需 ParamsPlugin
await router.relaunch('/pages/index/index')
await router.back()     // 返回一页
await router.back(2)    // 返回多级
```

目标 `meta.isTab === true` 自动走 `uni.switchTab`。**TabBar 页不能用 query / params 传参**（原生限制，events 通道同样经 query 桥接不可达），改用全局状态、`uni.setStorageSync` 或 `eventBus` 广播（不经导航 URL，tab 页可收到）。

导航失败不 throw，统一 Promise reject。需要感知结果时：

```ts
import { isNavigationFailure, RouterErrorCode, NavigationFailure } from '@meng-xi/unix-router'
try {
	await router.push({ name: 'detail' })
} catch (e) {
	const failure = e as NavigationFailure // UTS 中 e 需 as 收窄后才能读字段
	if (isNavigationFailure(failure, RouterErrorCode.DUPLICATED)) {
		return // 重复 push（仅 push 检测），按需忽略或改 replace
	}
}
```

无 `go(n)` 方法——向后返回一律用 `back(delta)`。

### 登录鉴权守卫

```ts
router.beforeEach((to, from) => {
	if (to.meta.requireAuth === true && !isLoggedIn()) {
		return { name: 'login', query: new Map<string, string>([['redirect', to.fullPath]]) }
	}
	return true
})
```

返回 `true` / `null` 放行，`false` / `Error` 中止，`string`（路径）或位置对象 `{ path / name / query / params }` 即重定向，`{ location, mode: 'push' | 'replace' | 'relaunch' }` 可指定重定向导航方式。组件内用 `onBeforeRouteLeave` / `onBeforeRouteEnter` / `onBeforeRouteUpdate`。两条守卫纪律：**守卫内禁止调 `router.push`**（会发起新导航排队，重定向一律 `return 位置`）；**所有分支必须显式 `return`**（漏 return 返回 undefined，行为不可靠）。

### 读取路由状态

```ts
const route = useRoute()
route.path            // /pages/detail/detail
route.params.get('id') // Map 读取，返回 string | null
route.query.get('tab') ?? ''

import { queryInt, queryBool } from '@meng-xi/unix-router'
const page = queryInt(route.query, 'page', 1) // 便捷解析，默认值兜底
```

### 导航动画

```ts
import { AnimationPlugin } from '@meng-xi/unix-router'

const router = createRouter({
	routes,
	plugins: [new AnimationPlugin()],
	animation: { type: 'slide-in-right', duration: 300 } // 全局默认
})
// 单次覆盖（仅 push / replace / relaunch；back 用全局默认并自动映射退出型）
await router.push({ path: '/pages/detail/detail', animationType: 'fade-in', animationDuration: 500 })
```

### 页面事件通信（打开页 ↔ 被打开页）

```ts
// 打开方：注册 EventsPlugin 后携带 events 监听（类型是 Map<string, (data: any) => any>，不是对象字面量）
import { EventsPlugin } from '@meng-xi/unix-router'
await router.push({
	path: '/pages/detail/detail',
	events: new Map<string, (data: any) => any>([
		['selected', (data: any) => { /* 接收被打开页回传 */ }]
	])
})
// 被打开页：onShow 内即可使用；useOpenerEventChannel() 返回可空值，使用前判空
import { useOpenerEventChannel } from '@meng-xi/unix-router'
const channel = useOpenerEventChannel()
if (channel !== null) {
	channel.emit('selected', { id: '42' }) // 回传给打开方 events 表同名监听
	const id = channel.on('preview', (data: any) => { /* 监听打开方推送 */ })
	// channel.off(id) 精确移除监听
}
```

### 拦截直调的原生导航

需要业务代码里的 `uni.navigateTo` 直调也走守卫时：`createRouter({ routes, plugins: [new InterceptorPlugin()], interceptUniApi: true })`。

### 文件路由生成（构建期，CLI 项目）

从 `@meng-xi/unix-router/vite-plugin` 引入；页面旁声明 `defineUniPage({ title, name, isTab })` 宏，构建期生成 `pages.json` 与 `routes.gen.uts`。三插件选型：`routeGen`（全量，默认推荐）/ `pagesGen`（仅 pages.json）/ `routesGen`（仅路由表）。硬约束：宏每页至多一次；宏内 `beforeEnter` / meta 表达式**必须自包含**（原样注入生成文件，引用页面内变量编译失败，改用 storage / 全局状态取值）；手工改生成文件里推导字段会被重生成刷新（要改声明源头）；**生成文件必须一并提交 git**（HBuilderX CLI 编译链不保证插件先行执行）。**HBuilderX 项目**根目录存在 `vite.config.ts` 会整体替换内置配置，必须自行引入 `@dcloudio/vite-plugin-uni`，否则 dev server 页面空白。

### 自定义插件（扩展库能力）

**必须 class 继承**（非蒸汽端含方法的对象字面量被推断为 `UTSJSONObject`，无法工作）：

```ts
import { RouterPlugin } from '@meng-xi/unix-router'
import type { PluginContext, RouterOptions } from '@meng-xi/unix-router'

class AnalyticsPlugin extends RouterPlugin {
	name = 'analytics' // 唯一标识，hasPlugin / PLUGIN_REQUIRED 预检按它匹配

	override install(context: PluginContext, options: RouterOptions): void {
		context.onAfterResolve((location, pluginData) => {
			pluginData.set('startedAt', Date.now()) // PluginData 即 Map<string, any>，单次导航各阶段共享
		})
		context.onCompleteNavigation((ctx) => { /* 导航成功后执行 */ })
	}
}
// 注册：plugins: [new AnalyticsPlugin()]
```

hook 全集（按时序）：`onEnrichLocation` / `onAfterResolve` / `onPrepareNavigation` / `onBeforeNavigation` / `onCompleteNavigation` / `onNavigationAbort` / `onRouteSync` / `onAppInstall`，详细签名见 `references/api-quickref.md` 的「自定义插件开发契约」节。

## 4. UTS 强类型红线（写码时逐条自检）

1. 插件注册写 `[new ParamsPlugin()]`，禁止类引用 `[ParamsPlugin]`
2. query / params 是 `Map<string, string>`：`.get()` / `.has()` / `.set()`，禁止对象式访问
3. 可空值用 `null`，判断写 `!= null`；条件必须是布尔表达式，禁止 truthy / falsy 隐式判断
4. `.catch` 回调参数类型写 `any | null`（UTS 的 `any` 编译为非空 Kotlin `Any`）
5. `isNavigationFailure(e)` 直接可用（0.8.0+ 签名 `any | null`，内部 instanceof 收窄）
6. 功能依赖插件：params / events / 动画 / 拦截分别需要 ParamsPlugin / EventsPlugin / AnimationPlugin / InterceptorPlugin，未注册不生效（params / events 抛 `PLUGIN_REQUIRED`，动画静默忽略）
7. uni-app x 页面文件后缀 `.uvue`；页面必须真实存在于 `pages.json`
8. **vue-router 习惯写法禁用**（扁平页面模型，均不支持）：嵌套路由 `children`、动态路由 `addRoute` / `removeRoute`、动态段 `/user/:id`、`<router-view>` / 命名视图、`scrollBehavior`、hash 路由、`go(n)`（用 `back(delta)`）、守卫 `next()` 回调（直接返回结果）、`history` 选项、`active-class` 等 RouterLink 扩展 props（激活态用 `useLink()` 自实现）

## 5. 错误排查

| 信号 | 处置 |
| --- | --- |
| `PLUGIN_REQUIRED` 抛错 | 按错误提示注册对应插件（实例化） |
| `ROUTE_NOT_FOUND` | 核对 `name` 拼写与 `RouteConfig` 注册；或改 path 导航 |
| `DUPLICATED` | 仅 `push` 检测重复导航（path+query+params+hash 完全一致），属预期行为：捕获忽略或改 `replace` |
| `ABORTED` / `CANCELLED` | 守卫返回 false / 守卫抛错 / 重定向超 10 层；`back()` 的 delta 非正整数（`ABORTED`）或页面栈不足（`CANCELLED`）；检查守卫逻辑与栈深度 |
| Android 编译失败含 `UTSPromise` / `catch` | 升级库到 0.8.0+（源码已修复） |
| H5 `router.push` 崩溃 TypeError | 升级库到 0.8.0+ |
| App 端无动画 | 动画字段官方仅 App 支持（Android 4.18+ / iOS 4.25+ / HarmonyOS 4.61+）；确认注册 AnimationPlugin；小程序端无动画属正常 |
| 守卫不执行 | 直调 `uni.*` 不走路由器，需 `interceptUniApi: true` + `new InterceptorPlugin()`；注意拦截版本门槛，不满足会自动降级 |

## 6. 深入资料

- 完整 API 速查（选项表 / 守卫清单 / AnimationType 枚举 / 错误对象字段 / vue-router 迁移差异 / 自定义插件契约 / 平台差异矩阵）：本技能目录 `references/api-quickref.md`
- 官方文档站与更新日志：仓库 `packages/docs`（`src/changelog.md`）
- 文件路由详细约定：`packages/docs/src/guide/file-based-routing.md`
- 实战场景配方（分包、鉴权组合等）：`packages/docs/src/guide/recipes.md`
- 与 vue-router 的完整差异说明：`packages/docs/src/guide/differences.md`
