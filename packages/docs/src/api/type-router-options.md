# RouterOptions

`createRouter()` 的初始化选项。

```ts
import type { RouterOptions } from '@meng-xi/unix-router'

const options: RouterOptions = {
	routes,
	strict: true,
	guardTimeout: 10000,
	readyTimeout: 0
}
```

## 字段

| 字段 | 类型 | 默认值 | 说明 |
| --- | --- | --- | --- |
| `routes` | `RouteConfig[]` | — | **必需**。路由配置列表，路径须与 `pages.json` 注册一致 |
| `strict` | `boolean` | `true` | 严格模式，启用后未匹配的命名路由抛出 `ROUTE_NOT_FOUND` |
| `guardTimeout` | `number` | `10000` | 守卫超时（毫秒），`0` 禁用 |
| `readyTimeout` | `number` | `0` | 就绪超时（毫秒），`0` 永不超时 |
| `interceptUniApi` | `boolean` | `false` | **opt-in**。启用后拦截 `uni.*` 原生导航 API（`navigateTo` / `redirectTo` / `switchTab` / `reLaunch` / `navigateBack`），绕过路由器直接调用这些 API 的跳转也会转由 `router.*` 走完整守卫链，守卫下沉到 uni API 层。受运行时版本支持：Web 4.0 / 微信 4.41 / Android 3.97 / iOS 4.11 / HarmonyOS 4.61 |

> **注意**：微信小程序端 `<navigator>` 组件跳转与点击 tabBar（底层不触发 `uni.switchTab`）无法被拦截，此场景需在页面 `onShow` 兜底守卫。

## 相关 API

- [createRouter()](./create-router)
- [RouteConfig](./type-route-config)