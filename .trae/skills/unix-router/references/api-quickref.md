# unix-router API 速查表

> 供 AI 编码助手按需查阅。所有示例为 UTS / uni-app x 语法；import 路径按项目形态替换（npm：`@meng-xi/unix-router`；uni_modules：`@/uni_modules/ux-router/utssdk/index.uts`）。

## RouteConfig 与 RouteMeta

```ts
type RouteConfig = {
	path: string              // 必填，不带前导斜杠，与 pages.json 一致
	name?: string             // 命名路由
	meta?: RouteMeta
	redirect?: RouteLocationRaw // ⚠️ 0.7.0 起仅声明类型，导航流程暂未消费（预留字段），勿依赖
	beforeEnter?: NavigationGuard | NavigationGuard[] // 单路由独享前置守卫，先于全局 beforeResolve
}

type RouteMeta = {
	title?: string       // 页面标题
	isTab?: boolean      // 是否 tabBar 页（决定 switchTab 导航）
	requireAuth?: boolean // 是否要求登录（由守卫据此拦截，库本身不消费）
}
```

**自定义 meta 字段**：UTS 不支持接口声明合并，`declare module { interface RouteMeta }` 写法在原生端无效。自定义字段必须直接在 `RouteMeta` type 定义处追加（库源码或 fork / 复制的 `route.uts`），否则对象字面量赋值编译失败。

## createRouter 选项（RouterOptions）

| 选项 | 类型 | 默认值 | 说明 |
| --- | --- | --- | --- |
| `routes` | `RouteConfig[]` | 必填 | 路由配置，`path` 不带前导斜杠、与 `pages.json` 一致 |
| `strict` | `boolean` | `true` | 严格模式：未匹配命名路由抛 `RouterError ROUTE_NOT_FOUND`；`false` 仅警告并按 path 处理 |
| `guardTimeout` | `number` | `10000` | 守卫超时（ms），`0` 关闭；超时警告并中止导航（`CANCELLED`），异步守卫（返回 Promise）同样受约束 |
| `readyTimeout` | `number` | `0` | 就绪超时（ms），`0` 永不超时，防止 `await isReady()` 挂起 |
| `interceptUniApi` | `boolean` | `false` | 拦截直调的 `uni.*` 原生导航（需 `InterceptorPlugin`；版本门槛 Web 4.0 / 微信 4.41 / Android 3.97 / iOS 4.11 / Harmony 4.61，不满足自动降级） |
| `plugins` | `RouterPlugin[]` | - | 插件列表，**必须传实例**：`[new ParamsPlugin(), new InterceptorPlugin(), new AnimationPlugin(), new EventsPlugin()]` |
| `paramsPersistent` | `boolean` | `false` | 默认将 params 持久化到 storage（需 `ParamsPlugin`；写失败自动回退内存） |
| `animation` | `NavigationAnimation` | - | 全局默认动画 `{ type: AnimationType, duration?: number }`（`duration` 默认 300ms；需 `AnimationPlugin`） |

导出常量：`DEFAULT_GUARD_TIMEOUT = 10000`、`DEFAULT_READY_TIMEOUT = 0`、`MAX_REDIRECT_DEPTH = 10`（守卫重定向深度上限，超限 `CANCELLED`）、`ROUTER_SYMBOL = '__unix_router__'`。

## Router 实例方法

| 分类 | API | 返回 |
| --- | --- | --- |
| 导航 | `push(location)` / `replace(location)` / `relaunch(location)` / `back(delta?: number \| null)` | 均返回 `Promise<NavigationResult>`（= 目标 `RouteLocation`，成功 resolve、失败 reject `NavigationFailure`） |
| 路由表 | `getRoutes()` / `hasRoute(name)` / `resolve(to)` | `resolve` 解析非法位置（path 与 name 均未提供）抛 `ROUTE_NOT_FOUND` |
| 状态 | `currentRoute`（只读）/ `isReady()` / `onRouteChange(cb)` | `onRouteChange` 返回取消函数 |
| 守卫/错误 | `beforeEach(cb)` / `beforeResolve(cb)` / `afterEach(cb)` / `onError(cb)` | **均返回取消注册函数** `() => void`；`onError` 签名 `(error, to, from) => void` |
| 同步/扩展 | `syncRoute()` / `guardRoute(to?, options?)` | `guardRoute` 冷启动补执行守卫链（不实际导航、不跑 `beforeEnter` / `beforeResolve` / `afterEach`）；中止触发 `onAbort(failure)` 并 reject；守卫重定向时按重定向模式真实导航（缺省 `relaunch`） |
| 安装 | `install(app)`（经 `app.use(router)` 调用） | — |

导航行为要点：

- **并发导航自动排队串行执行**（vue-router 是后者取消前者，此处不是）
- **`DUPLICATED` 仅 `push` 检测**（path+query+params+hash 与当前完全一致）；`replace` / `relaunch` 不检测
- **`back(delta)` 语义**：默认 `1`（传 `null` 同按 `1`）；非正整数 reject `ABORTED`；页面栈不足 reject `CANCELLED`；返回前只执行 `beforeEach` + `beforeResolve`（不经过 `beforeEnter` 与插件 enrich / afterResolve）
- `currentRoute` 与 `useRoute()` **独立维护**：`back()` / `syncRoute()` 只更新 `currentRoute`，**不回写 `useRoute()`**——需要最新状态时在 `onShow` 里读 `router.currentRoute`

导航目标（location）形式：字符串 `'/pages/detail/detail'`；对象 `{ path? / name? / query? / params? / animationType? / animationDuration? / events? }`。`query` / `params` 均为 `Map<string, string>`。

底层 API 映射：`push`→`uni.navigateTo`、`replace`→`uni.redirectTo`、`relaunch`→`uni.reLaunch`、`back`→`uni.navigateBack`；目标 `meta.isTab === true` 时 `push` / `replace` / `relaunch` 自动改用 `uni.switchTab`（switchTab 不支持 query / params 传参）。

## 守卫全集

| 守卫 | 注册处 | 说明 |
| --- | --- | --- |
| `beforeEach` | `router.beforeEach` | 全局前置：鉴权 / 重定向 |
| `beforeResolve` | `router.beforeResolve` | 全局解析守卫（组件内守卫解析后） |
| `afterEach` | `router.afterEach` | 全局后置：埋点 / 标题（回调含 `failure` 参数） |
| `beforeEnter` | `RouteConfig` 内 | 单路由独享守卫 |
| `onBeforeRouteLeave` | 页面组件内 | 离开守卫（返回 false 阻止离开） |
| `onBeforeRouteEnter` | 页面组件内 | 进入守卫（返回 false 阻止进入） |
| `onBeforeRouteUpdate` | 页面组件内 | 复用组件参数变化守卫 |

守卫返回值（`NavigationGuardReturn`，签名放宽为 `any` 以兼容非蒸汽编译）：

| 返回 | 行为 |
| --- | --- |
| `null` / `true` | 放行 |
| `false` | 中止导航（`ABORTED`） |
| `Error` | 中止导航并作为失败原因（`CANCELLED`） |
| `string` | 重定向到该路径 |
| `RawLocationLike`（`{ path? / name? / query? / params? }`） | 重定向到该位置（沿用原导航模式） |
| `NavigationRedirect`（`{ location, mode?: 'push' \| 'replace' \| 'relaunch' }`） | 重定向到 `location` 并可指定导航方式 |

`afterEach` 回调签名 `(to, from, failure: Error | null)`，成功时 `failure` 为 `null`。重定向重新走完整守卫链，深度上限 10，超出按 `CANCELLED` 取消。

## 组合式 API

| API | 返回 | 说明 |
| --- | --- | --- |
| `useRouter()` | Router | 路由器实例 |
| `useRoute()` | RouteLocation（响应式） | `path` / `fullPath` / `name`（`string \| null`）/ `query` / `params`（均为 Map）/ `meta` / `hash`（恒 `''`）/ `matched`（`RouteConfig[]`）。**`onShow` 触发早于 `syncRoute()`，此时读到的是旧值**——读本页初始 query 用 `onLoad(options)` 直接取 uni 传入参数，不依赖 `useRoute()` |
| `useLink({ to, replace?, relaunch? })` | `{ route, isActive, isExactActive, href, navigate }` | 声明式导航状态与触发；各状态为 `ComputedRef`（**脚本中访问需 `.value`**，模板自动解包）；`isActive` 为路径前缀匹配；`navigate()` 返回 `Promise<RouteLocation \| null>`（push 返回目标位置，replace / relaunch 返回 `null`）；`replace` 与 `relaunch` 同 true 时 `relaunch` 优先 |
| `useOpenerEventChannel()` | `EventChannel \| null` | 打开方事件通道（需 `EventsPlugin`；返回可空，使用前判空；`onShow` 内即可用，不依赖路由同步时机） |

组件：`<RouterLink to="pages/detail/detail">`（props 仅 `to` / `replace` / `relaunch` 三个，同 true 时 `relaunch` 优先），自入口导出、需显式 import 使用（无 easycom 自动注册）。**没有 vue-router 的 `active-class` / `custom` / `exact-active-class` 等扩展 props**——激活态需求基于 `useLink()` 的 `isActive` / `isExactActive` 自行实现。

## 内置插件（注册必须实例化）

| 插件 | 能力 | 未注册时 |
| --- | --- | --- |
| `ParamsPlugin` | `params` 跨页传递（`__params__` 关联存储，键名不暴露明文） | 使用 params 抛 `PLUGIN_REQUIRED` |
| `InterceptorPlugin` | 拦截直调 `uni.*` 导航转守卫链（配合 `interceptUniApi: true`）；仅拦外部直调，路由器自身 `router.*` 发起的 uni 调用不二次拦截 | 选项忽略并警告 |
| `AnimationPlugin` | 导航窗口动画（App 原生透传 / H5 WAAPI） | `animation` 选项静默忽略 |
| `EventsPlugin` | 页面事件通信（对齐官方 `navigateTo` 的 `events` 语义；通道键经内部 `__evt__` query 桥接并对用户屏蔽） | 携带 `events` 导航抛 `PLUGIN_REQUIRED` |

EventsPlugin 行为细节：`channel.emit(eventName, data)` **只透传第一个附加参数**（底层回调单参签名），多字段合并为一个对象；通道生命周期——导航中止 / 失败自动清理不泄漏，页面 back 后打开方与被打开页**仍可继续通信**，每次导航通道相互隔离不串扰。

配套导出：`eventBus`（`$on` / `$once` / `$off` / `$emit`）；`installInterceptors` / `removeInterceptors`（手动装卸 uni API 拦截，InterceptorPlugin 内部使用）；`createParamsManager`（创建独立 ParamsManager，低频）。

## AnimationType 枚举值（对齐 uni-app x animationType）

`'auto'` | `'none'` | `'slide-in-right'` | `'slide-in-left'` | `'slide-in-top'` | `'slide-in-bottom'` | `'slide-out-right'` | `'slide-out-left'` | `'slide-out-top'` | `'slide-out-bottom'` | `'fade-in'` | `'fade-out'` | `'zoom-in'` | `'zoom-out'` | `'zoom-fade-in'` | `'zoom-fade-out'` | `'pop-in'` | `'pop-out'`

`back()` 自动将进入型映射为退出型（如 `slide-in-right` → `slide-out-right`）。小程序端官方不支持动画字段，仅 App（Android 4.18+ / iOS 4.25+ / HarmonyOS 4.61+）原生透传、H5 由 WAAPI 模拟。

## 错误体系（RouterErrorCode，数值对齐 vue-router 4）

| 错误码 | 值 | 含义 |
| --- | --- | --- |
| `ABORTED` | 4 | 导航被守卫中止（返回 false 或重定向异常） |
| `CANCELLED` | 8 | 守卫抛出异常或重定向超过最大深度 |
| `DUPLICATED` | 16 | 重复导航到当前路由（自动拒绝） |
| `ROUTE_NOT_FOUND` | 32 | 未匹配到页面路由 |
| `NAVIGATION_API_ERROR` | 64 | `uni.*` 原生导航 API 调用失败（`UniNavigationApiError`） |
| `SETUP_ERROR` | 128 | 路由安装环境错误 |
| `PLUGIN_REQUIRED` | 256 | 使用了插件功能但未注册对应插件 |

判断工具：`isNavigationFailure(error, code?)`（0.8.0+ 首参 `any | null`，内部 `instanceof` 收窄）；全局捕获 `router.onError(cb)`。

错误对象层级与字段：`Error` → `RouterError`（基类）→ `NavigationFailure`；除 `message` / `name` 外携带 `code`（`RouterErrorCode`）/ `to` / `from`（触发错误的 RouteLocation）。另有 `UniNavigationApiError`（接口，`uni.*` fail 回调负载 `errMsg` / `context`）。

**catch 中的收窄写法**：`catch (e)` 的 `e` 不能直接访问字段，统一 `as` 收窄：

```ts
router.push({ name: 'detail' }).catch((e: any | null) => {
	const failure = e as NavigationFailure
	if (isNavigationFailure(failure, RouterErrorCode.DUPLICATED)) {
		return // 重复 push，按需忽略
	}
	console.error('导航失败', failure.message)
})
```

错误触发要点：`DUPLICATED` 仅 `push` 检测（连点可捕获忽略或改 `replace`）；`back()` 栈不足 / delta 非正整数分别产生 `CANCELLED` / `ABORTED`；原生 API 失败后 `currentRoute` 回退来源路由；导航失败**不同步 throw**，`push(...)` 之后的同步代码照常执行，需 `await` / `.catch` 感知结果。

## vue-router 迁移差异（习惯写法禁用清单）

来自 vue-router 的代码需注意以下差异，避免照搬：

| vue-router 习惯 | unix-router 现实 | 替代做法 |
| --- | --- | --- |
| 嵌套路由 `children` | ❌ 不支持（扁平页面模型） | 页面平级注册，由页面栈承担层级 |
| 动态路由 `addRoute` / `removeRoute` | ❌ 不支持（页面须编译期注册进 pages.json） | 全部写在 `routes` 配置；分包见文件路由 `subPackages` |
| 动态段 `/user/:id` | ❌ 不支持 | query（进 URL）或 params（ParamsPlugin）传参 |
| `go(n)` | ❌ 无此方法 | `back(delta)`（仅向后）；`delta` 非正整数 `ABORTED`、栈不足 `CANCELLED` |
| 守卫 `next()` 回调 | ❌ 无 `next` 参数 | 守卫直接返回结果（返回值语义见上文守卫全集） |
| `<router-view>` / 命名视图 | ❌ 不支持 | 静态页面模型，每页一个独立 uvue 文件 |
| `scrollBehavior` / hash 路由 | ❌ 不支持（`route.hash` 恒为 `''`） | 滚动由 uni-app 原生管理 |
| 重复导航 `resolve(false)` | 改为 **reject** `NavigationFailure`（`DUPLICATED`，仅 push） | `isNavigationFailure(err, RouterErrorCode.DUPLICATED)` 捕获 |
| 并发导航后者取消前者 | 改为自动排队串行 | 无需处理 |
| `history` 选项 | ❌ 无（历史由原生页面栈承担） | `createRouter({ routes, ... })` |
| keep-alive | ❌ 无（每次导航新实例） | 组件内守卫中 `onBeforeRouteLeave` 最可靠；`onBeforeRouteUpdate` 几乎不触发；`onBeforeRouteEnter` 无法访问组件状态 |

## 自定义插件开发契约

需扩展库能力时继承 `RouterPlugin`（**必须 class 实现**——非蒸汽端含方法的对象字面量被推断为 `UTSJSONObject` 无法工作；`PluginContext` 同为 class）：

```ts
import { RouterPlugin } from '@meng-xi/unix-router'
import type { PluginContext, RouterOptions } from '@meng-xi/unix-router'

class MyPlugin extends RouterPlugin {
	name = 'my-plugin' // 唯一标识：hasPlugin(name) 与 PLUGIN_REQUIRED 预检按它匹配

	override install(context: PluginContext, options: RouterOptions): void {
		// 在 createRouter 内部、任何导航之前调用一次
		context.onAfterResolve((location, pluginData) => {
			pluginData.set('startedAt', Date.now()) // PluginData 即 Map<string, any>，单次导航各阶段共享
		})
	}
}
// 注册：plugins: [new MyPlugin()]
```

`PluginContext` 的 8 个 hook（按导航时序）：

| hook | 触发时机 |
| --- | --- |
| `onEnrichLocation` | resolve 之前增强原始位置（链式，返回值为下一环输入） |
| `onAfterResolve` | resolve 后、守卫链前，向 `pluginData` 写入插件数据 |
| `onPrepareNavigation` | uni API 调用前，可改写 `ctx.query` 与 `ctx.options` |
| `onBeforeNavigation` | 真正调用 uni API 前（可异步，多 hook 串行） |
| `onCompleteNavigation` | uni API 成功且页面栈确认后，可扩展 `ctx.result` |
| `onNavigationAbort` | 中止 / 失败时清理（异常被吞掉） |
| `onRouteSync` | `syncRoute()` 期间从 URL query 提取 / 移除插件内部 key |
| `onAppInstall` | `app.use(router)` 时触发 |

`PluginContext` 另有成员：`currentRoute`（只读）、`resolve(location)`、`router`（`any`，按需 `as Router`）、`paramsManager`（`any`，按需 `as ParamsManager`）、`hasPlugin(name)`。

## 平台差异矩阵

| 差异点 | H5 | App（Android / iOS / HarmonyOS） | 小程序 |
| --- | --- | --- | --- |
| 编译产物 | JS | Kotlin / Swift / ArkTS（VDOM）或 JS（蒸汽模式） | JS |
| 导航动画 | WAAPI（`element.animate`） | 原生 `animationType` / `animationDuration` | 官方不支持，无动画 |
| `onShow` 路由同步 | `app.use(router)` 注入 mixin 自动 | 手动 `router.syncRoute()` | 手动 `router.syncRoute()` |
| `install()` 附加行为 | provide + 全局属性 + onShow mixin | 仅注册全局活动路由 | 仅注册全局活动路由 |
| `RouteName` 字面量类型 | `RouteNameMap` 模块增强 | 退化为 `string`（strict 校验兜底） | 退化为 `string` |
| `route.hash` | 恒为 `''`（uni-app x 不支持 hash 路由） | 恒为 `''` | 恒为 `''` |
| uni API 拦截版本门槛 | 4.0 | Android 3.97 / iOS 4.11 / Harmony 4.61 | 微信 4.41 |
| 物理返回 / 边缘滑动 | 不经过路由器 | 不经过路由器（`onShow` 由 `syncRoute()` 对齐） | 不经过路由器 |

## 查询工具函数

| 函数 | 签名 | 说明 |
| --- | --- | --- |
| `queryInt` | `(query: Map<string, string>, key: string, defaultValue: number): number` | 解析整型 query |
| `queryNumber` | `(query: Map<string, string>, key: string, defaultValue: number): number` | 解析数值 query |
| `queryBool` | `(query: Map<string, string>, key: string, defaultValue: boolean): boolean` | 解析布尔 query：值为 `'0'` / `'false'` 返回 false，其余非空值返回 true，空值 / 缺省返回默认值 |

## 文件路由生成（构建期 dev 工具，`@meng-xi/unix-router/vite-plugin`）

| 插件 | 职责 | 适用 |
| --- | --- | --- |
| `routeGen` | 页面文件 → `pages.json` + 路由表 `routes.gen.uts` 全量流水线 | 默认推荐 |
| `pagesGen` | 页面文件 → 仅 `pages.json`（含宏 dts） | 路由表手写 |
| `routesGen` | `pages.json` → 仅路由表（支持 `routes.ext.uts` 扩展声明注入 name / meta / beforeEnter） | pages.json 手写或由其他工具维护 |

公共选项：`pagesJsonPath` / `watch`（默认开，200ms 去抖串行两阶段）/ `verbose` / `errorStrategy`（`strict` 抛错 / `warn` 告警跳过）；`pages` 段（`pagesDir` / `subPackages` / `entryPage` / `tabBar` / `titleFallback`）；`router` 段（`dts` / `preserveRouteChanges` 默认开 / `extensions`）。

页面声明优先级：`defineUniPage` 宏 > `<route-config>` 自定义块 > 插件推导（字段级合并）；name 冲突回退全路径 camelCase，终极冲突按 `errorStrategy`。产物：`routes.gen.uts` / `route-name.gen.d.ts`（WEB 字面量类型）/ `define-uni-page.d.ts`（均可关）。

宏与生成文件的硬约束：

- **宏每页至多声明一次**（写两次在 `strict` 下直接终止构建）
- **宏 / 块内的 `beforeEnter` 与 meta 表达式必须自包含**——原样注入 `routes.gen.uts`，与页面文件是两个作用域，引用页面内变量编译失败；守卫逻辑改用 storage / 全局状态取值
- **手工修改生成文件里「由页面声明推导」的字段无意义**（重生成被刷新），要改声明源头；自定义路由走 `routes.ext.uts` 或 `router.extensions`
- **生成文件必须一并提交 git**——HBuilderX CLI 编译链不保证插件先行执行，watch/HMR 只是开发期辅助

HBuilderX 项目：根目录存在 `vite.config.ts` 会整体替换内置配置，必须自行引入 `@dcloudio/vite-plugin-uni`（内置 `uni()` 不再生效），否则 dev server 页面空白 / `/main` 404。

## 版本要点

- 0.8.0：文件路由生成插件、`vite-plugin` 子路径导出、三插件拆分；修复 Android 云打包编译（`.catch` 回调 `any | null`、`isNavigationFailure` 放宽）、H5 `push` 崩溃、H5 `uni.*` 编译警告；补全 App 端动画透传
- 0.7.0：插件必须实例化注册（破坏性）；非蒸汽（Kotlin/Swift/ArkTS 全链路）编译兼容
- 0.6.0：`EventsPlugin` / `eventBus` / `useOpenerEventChannel` / `events` 字段
- 0.5.0：`AnimationPlugin` / `RouterOptions.animation` / `queryInt` / `queryNumber` / `queryBool` 入口导出
- 0.4.0：`RouterLink` 组件 / 插件契约
