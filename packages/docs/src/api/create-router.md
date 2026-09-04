# createRouter()

`createRouter()` 创建并返回一个 [Router 实例](./router-instance)（适配 uni-app x）。

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
| `strict` | `boolean` | `true` | 严格模式，启用后未匹配的命名路由将抛出 `ROUTE_NOT_FOUND` 异常 |
| `guardTimeout` | `number` | `10000` | 守卫超时（毫秒），设为 `0` 禁用 |
| `readyTimeout` | `number` | `0` | 路由器就绪超时（毫秒），`0` 表示永不超时 |

## 返回值

返回 [Router 实例](./router-instance)。不立即执行导航，需通过 `app.use(router)` 安装到 Vue 应用后才提供服务。

## 示例

```ts
// router/index.ts
import { createRouter } from '@meng-xi/unix-router'
import { routes } from './routes'

export const router = createRouter({
	routes,
	strict: true,
	guardTimeout: 15000 // 守卫中有网络请求时调大超时
})
```

## 相关 API

- [Router 实例](./router-instance)
- [RouterOptions](./type-router-options)
- [RouteConfig](./type-route-config)