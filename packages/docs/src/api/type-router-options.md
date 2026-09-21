# RouterOptions

`createRouter()` 的初始化选项。

```ts
import type { RouterOptions } from '@meng-xi/unix-router'

const options: RouterOptions = {
	routes,
	strict: true,
	plugins: [new ParamsPlugin()],
	guardTimeout: 10000,
	readyTimeout: 0
}
```

## 字段

| 字段 | 类型 | 默认值 | 说明 |
| --- | --- | --- | --- |
| `routes` | `RouteConfig[]` | — | **必需**。路由配置列表，路径须与 `pages.json` 注册一致 |
| `strict` | `boolean` | `true` | 严格模式。`true` 时未匹配的命名路由抛 `RouterError ROUTE_NOT_FOUND`；`false` 时仅警告并按路径处理 |
| `guardTimeout` | `number` | `10000` | 守卫超时（毫秒），`0` 禁用。超时输出警告并中止导航 |
| `readyTimeout` | `number` | `0` | 就绪超时（毫秒），`0` 永不超时 |
| `interceptUniApi` | `boolean` | `false` | **opt-in**。启用后拦截 `uni.*` 原生导航 API（`navigateTo` / `redirectTo` / `switchTab` / `reLaunch` / `navigateBack`），外部直接调用也转交 `router.*` 走完整守卫链。**须配合 `InterceptorPlugin`**。受运行时版本支持：Web 4.0 / 微信 4.41 / Android 3.97 / iOS 4.11 / HarmonyOS 4.61，缺失时自动降级并警告 |
| `plugins` | `RouterPlugin[]` | — | 插件列表，按需注册扩展能力。**传入实例**：`[new ParamsPlugin(), new InterceptorPlugin(), new EventsPlugin(), new AnimationPlugin()]`，见[插件系统](../guide/plugins) |
| `paramsPersistent` | `boolean` | `false` | 是否默认将 params 持久化到 storage（写入失败自动回退内存）。**须配合 `ParamsPlugin`** |
| `animation` | `NavigationAnimation` | — | 全局默认导航动画 `{ type: AnimationType, duration?: number }`（`duration` 默认 300ms）。App / 小程序透传原生 `animationType`，H5 端由插件以 Web Animations API 实现。**须配合 `AnimationPlugin`** |

::: warning 插件相关选项须配合对应插件
`interceptUniApi` / `paramsPersistent` / `animation` 分别依赖 `InterceptorPlugin` / `ParamsPlugin` / `AnimationPlugin`，注册了选项但未注册对应插件时选项被忽略并输出警告。
:::

> **注意**：微信小程序端 `<navigator>` 组件跳转与点击 tabBar（底层不触发 `uni.switchTab`）无法被拦截，此场景需在页面 `onShow` 兜底守卫。

## 相关 API

- [createRouter()](./create-router)
- [RouteConfig](./type-route-config)
