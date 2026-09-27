# createRouter()

`createRouter(options: RouterOptions): Router` 创建并返回一个 [Router 实例](./router-instance)（适配 uni-app x）。

```ts
import { createRouter } from '@meng-xi/unix-router'

const router = createRouter({
	routes, // 必需
	strict: true
})
```

## 参数

`createRouter(options: RouterOptions)`

| 选项 | 类型 | 默认值 | 说明 |
| --- | --- | --- | --- |
| `routes` | `RouteConfig[]` | — | **必需**。路由配置列表，路径须与 `pages.json` 注册一致 |
| `strict` | `boolean` | `true` | 严格模式。`true` 时未匹配的命名路由抛 `RouterError ROUTE_NOT_FOUND`；`false` 时仅警告并按路径处理 |
| `guardTimeout` | `number` | `10000` | 守卫超时（毫秒），设为 `0` 禁用。超时输出警告并中止导航 |
| `readyTimeout` | `number` | `0` | 路由器就绪超时（毫秒），`0` 表示永不超时 |
| `interceptUniApi` | `boolean` | `false` | **opt-in**。启用后拦截 `uni.*` 原生导航 API（`navigateTo` / `redirectTo` / `switchTab` / `reLaunch` / `navigateBack`），外部直接调用也会转交路由器走完整守卫链。**须配合 `InterceptorPlugin`**，见 [uni API 拦截](../guide/interceptor) |
| `plugins` | `RouterPlugin[]` | — | 插件列表，按需注册扩展能力。**传入实例**：`[new ParamsPlugin(), new InterceptorPlugin()]`，见[插件系统](../guide/plugins) |
| `paramsPersistent` | `boolean` | `false` | 是否默认将 params 持久化到 storage（写入失败自动回退内存）。**须配合 `ParamsPlugin`** |
| `animation` | `NavigationAnimation` | — | 全局默认导航动画 `{ type: AnimationType, duration?: number }`（`duration` 默认 300ms）。App 端透传原生 `animationType`（官方仅 App 支持），H5 由插件以 Web Animations API 实现。**须配合 `AnimationPlugin`**，见[导航动画](../guide/animation) |

::: warning 插件相关选项须配合对应插件
`interceptUniApi` / `paramsPersistent` 分别依赖 `InterceptorPlugin` / `ParamsPlugin`，注册了选项但未注册对应插件时，选项被忽略并输出警告；`animation` 未注册 `AnimationPlugin` 时被静默忽略（无警告）。
:::

## 返回值

返回 [Router 实例](./router-instance)。不立即执行导航，需通过 `app.use(router)` 安装到 Vue 应用后才提供服务。

## 错误

- `strict: true`（默认）时，解析未匹配的**命名路由**会抛 `RouterError`（`ROUTE_NOT_FOUND`）；受控导航中该错误经 Promise reject + `router.onError` 递出。
- `strict: false` 时降级为警告，按路径处理。

## 示例

```ts
// router/index.ts
import { createRouter, ParamsPlugin, InterceptorPlugin } from '@meng-xi/unix-router'
import { routes } from './routes'

export const router = createRouter({
	routes,
	strict: true,
	plugins: [new ParamsPlugin(), new InterceptorPlugin()], // 页面参数 + uni 导航拦截
	interceptUniApi: true, // 外部 uni.navigateTo 也走守卫
	paramsPersistent: true, // params 持久化到 storage
	guardTimeout: 15000 // 守卫中有网络请求时调大超时
})
```

## 导出常量

| 常量 | 类型 | 值 | 说明 |
| --- | --- | --- | --- |
| `DEFAULT_GUARD_TIMEOUT` | `number` | `10000` | 守卫默认超时（ms），对应 `guardTimeout` 默认值 |
| `DEFAULT_READY_TIMEOUT` | `number` | `0` | 路由器就绪默认超时，对应 `readyTimeout` 默认值 |
| `MAX_REDIRECT_DEPTH` | `number` | `10` | 守卫重定向的最大深度，超过则导航取消（`CANCELLED`），防死循环 |
| `ROUTER_SYMBOL` | `string` | `'__unix_router__'` | `provide` / `inject` 配对标识（字符串 key，跨平台更稳） |

## 相关 API

- [Router 实例](./router-instance)
- [RouterOptions](./type-router-options)
- [RouteConfig](./type-route-config)
