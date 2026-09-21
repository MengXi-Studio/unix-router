# uni API 拦截

业务代码里 `uni.navigateTo` 一写，路由守卫就形同虚设。`InterceptorPlugin` 通过 `uni.addInterceptor` 拦截原生导航 API 的直接调用，将其**转交路由器**执行完整守卫链，让守卫「下沉」到 uni API 层。

## 动机

unix-router 的守卫链只覆盖 `router.*` 发起的导航。若业务代码（三方组件、旧代码）绕过路由器直调 `uni.navigateTo` 等原生 API，鉴权、埋点等守卫逻辑会被静默绕过。启用拦截后，原生直调也会进入与 `router.push` 完全相同的流程。

## 启用拦截（双条件）

两个条件**缺一不可**：

```ts
import { createRouter, InterceptorPlugin } from '@meng-xi/unix-router'

const router = createRouter({
	routes,
	plugins: [new InterceptorPlugin()], // 条件一：注册插件（实例化）
	interceptUniApi: true               // 条件二：开启开关（默认 false）
})
```

- 只注册插件、不开 `interceptUniApi` → 插件安装时直接跳过，不安装拦截器；
- 只开 `interceptUniApi`、不注册插件 → 输出警告，选项被忽略。

生效后以下调用等价（守卫都生效）：

```ts
await router.push({ name: 'about' })            // 走路由器
uni.navigateTo({ url: '/pages/about/about' })  // 被拦截 → 转交路由器
```

## 拦截的 API

5 个原生导航 API 全部转交路由器，执行完整守卫链：

| 被拦截的 uni API | 转交为 |
| --- | --- |
| `uni.navigateTo` | `router.push` |
| `uni.redirectTo` | `router.replace` |
| `uni.reLaunch` | `router.relaunch` |
| `uni.switchTab` | `router.push`（路由器按 `meta.isTab` 自动分发 switchTab） |
| `uni.navigateBack` | `router.back` |

::: tip 不影响路由器自身调用
路由器内部发起的 uni 调用带内部标记，拦截器检测到即放行，不会二次拦截。
:::

## 示例：拦截后守卫统一登录鉴权

守卫只写一次，`router.*` 与原生直调全部生效：

```ts
// router/index.uts
import { createRouter, InterceptorPlugin } from '@meng-xi/unix-router'

export const router = createRouter({
	routes,
	plugins: [new InterceptorPlugin()],
	interceptUniApi: true
})

// 全局前置守卫：未登录一律中止（原生直调被拦截转交后同样经过这里）
router.beforeEach((to) => {
	const token = uni.getStorageSync('token') as string
	const hasToken: boolean = token.length > 0
	if (to.path !== '/pages/login/login' && !hasToken) {
		return false
	}
})
```

```ts
// 业务 / 三方代码：绕过路由器直调原生 API，守卫依然生效
uni.navigateTo({ url: '/pages/order/order' })
// 实际执行：拦截器阻止原始调用 → 转交 router.push → 守卫链 → 未登录被中止
```

## 平台版本门槛

拦截依赖 `uni.addInterceptor`，各端自以下 HBuilderX 版本起支持：

| 平台 | 最低 HBuilderX 版本 |
| --- | --- |
| Web | 4.0 |
| 微信小程序 | 4.41 |
| Android | 3.97 |
| iOS | 4.11 |
| HarmonyOS | 4.61 |

运行时能力缺失时**自动降级**：输出警告、拦截禁用，但**不阻塞导航**（原生调用按原逻辑执行）。

## 手动安装 / 卸载

除自动安装外，也可在 Router 实例化后做精细化控制（延迟安装、按条件启停）：

```ts
import { installInterceptors, removeInterceptors } from '@meng-xi/unix-router'

// 手动安装（内部调用 uni.addInterceptor 注册 5 个拦截器）
installInterceptors(router)

// 手动卸载（移除拦截器并释放路由器引用）
removeInterceptors()
```

::: warning 单实例限制
同一时刻仅支持一个路由器实例的拦截器；重复安装会先卸载旧拦截器并输出警告。
:::

### 示例：手动安装 / 卸载（延迟启停）

注册插件但不开 `interceptUniApi`（安装时跳过自动安装），运行期按需手动启停：

```ts
import { installInterceptors, removeInterceptors, useRouter } from '@meng-xi/unix-router'

const router = useRouter()

// 任意时机手动安装（内部逐个调用 uni.addInterceptor 注册 5 个拦截器）
installInterceptors(router)

// 需要临时关闭（调试、放开导航限制）时卸载并释放路由器引用
removeInterceptors()
```

对应路由器配置（条件一满足、条件二留给运行期决定）：

```ts
const router = createRouter({
	routes,
	plugins: [new InterceptorPlugin()]
	// 不设 interceptUniApi，插件安装时不会自动安装拦截器
})
```

## API 参考

### InterceptorPlugin

`RouterPlugin` 子类，uni 原生导航 API 拦截插件。

| 成员 | 签名 | 说明 |
| --- | --- | --- |
| `name` | 常量 `'interceptor'` | 插件名 |
| `constructor()` | 无参数 | — |
| `install` | `(context: PluginContext, options: RouterOptions): void` | `options.interceptUniApi === true` 时调用 `installInterceptors(context.router)`；Web 端在 app 卸载时注册 `removeInterceptors()` 清理（HMR 场景） |

注册写法（必须实例化；非蒸汽端插件必须 class 实现，对象字面量含方法会被推断为 UTSJSONObject）：

```ts
plugins: [new InterceptorPlugin()] // 通常配合 interceptUniApi: true
```

### 导出函数

| 函数 | 签名 | 说明 |
| --- | --- | --- |
| `installInterceptors` | `(router: Router): void` | 手动安装：对 5 个导航 API 各注册一个 `uni.addInterceptor`；`uni.addInterceptor` 不可用时输出警告并禁用；已有实例注册时先卸载并警告（同一时刻仅支持一个） |
| `removeInterceptors` | `(): void` | 逐个 `uni.removeInterceptor` 移除，重置并释放路由器引用 |
| `markRouterCall` | `(): void` | 标记下一次 uni API 调用由路由器内部发起（导航模块内部使用，拦截器检测到标记即放行；外部一般无需调用） |

被拦截的 API 列表（内部常量 `INTERCEPTED_APIS`）：`navigateTo`、`redirectTo`、`switchTab`、`reLaunch`、`navigateBack`。

### RouterOptions.interceptUniApi

```ts
interceptUniApi?: boolean // 默认 false
```

启用拦截的开关；`true` 但未注册 InterceptorPlugin 时安装阶段输出警告，选项被忽略。

### 错误行为

本插件不产生导航失败错误码（无 `PLUGIN_REQUIRED` 等）：

| 条件 | 行为 |
| --- | --- |
| 只注册插件、未开 `interceptUniApi` | 插件安装时直接跳过，不安装拦截器 |
| 只开 `interceptUniApi`、未注册插件 | 安装时输出警告，选项被忽略 |
| 运行时无 `uni.addInterceptor` | 输出警告，拦截禁用；原生调用按原逻辑执行，不阻塞导航 |
| 重复安装 | 先卸载旧拦截器并输出警告（同一时刻仅一个路由器实例生效） |

### 平台注意

- 拦截依赖 `uni.addInterceptor`，各端最低 HBuilderX 版本见上文「平台版本门槛」表（Web 4.0 / 微信小程序 4.41 / Android 3.97 / iOS 4.11 / HarmonyOS 4.61）；
- 拦截器以计数器区分「路由器发起」与「外部直调」：路由器调用前 `markRouterCall()` 标记，拦截器消费一次标记后放行，避免二次拦截；
- 外部直调被转交后，原始调用被阻止（`invoke` 返回 `false`，同时清空 `url` 作为双保险）；
- Web 端 `switchTab` 特殊处理：放行原始调用，仅在其 `success` 回调中 `syncRoute()` 同步状态（阻止会导致 TabBar 组件状态卡死）；
- 转交映射：`navigateTo → push`、`redirectTo → replace`、`reLaunch → relaunch`、`switchTab → push`（路由器按 `meta.isTab` 自动分发）、`navigateBack → back`（`delta > 0` 时透传返回层级）；query 会随 `parseUrl` 解析后透传给路由器。

## 下一步

- [错误处理](./error-handling) — 拦截转交后的导航失败如何捕获
- [插件系统](./plugins) — InterceptorPlugin 在插件体系中的位置
- [平台兼容性](./compatibility) — 各端 addInterceptor 支持详情
