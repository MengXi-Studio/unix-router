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

## 相关 API

- [createRouter()](./create-router)
- [RouteConfig](./type-route-config)