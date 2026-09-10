# RouterErrorCode

路由错误码枚举。数值对齐 vue-router 4 的 `NavigationFailureType`，并补充 uni-app x 平台相关错误码。

```ts
import { RouterErrorCode } from '@meng-xi/unix-router'
```

| 枚举值 | 数值 | 说明 |
| --- | --- | --- |
| `ABORTED` | `4` | 导航被守卫中止（守卫返回 `false`） |
| `CANCELLED` | `8` | 守卫抛错或重定向超过最大深度 |
| `DUPLICATED` | `16` | 重复导航（`push` 到 path+query+params+hash 与当前完全一致） |
| `ROUTE_NOT_FOUND` | `32` | 未匹配到页面路由（uni-app x 扩展） |
| `NAVIGATION_API_ERROR` | `64` | `uni.*` 原生导航 API 调用失败（uni-app x 扩展） |
| `SETUP_ERROR` | `128` | 路由安装环境错误（uni-app x 扩展） |
| `PLUGIN_REQUIRED` | `256` | 使用了未注册插件的能力（如未注册 `ParamsPlugin` 却使用 `params`），须先注册对应插件（uni-app x 扩展） |

::: tip 位运算与枚举
建议配合位运算（如 `failure.code & RouterErrorCode.ROUTE_NOT_FOUND`）或直接比较（`failure.code === RouterErrorCode.ROUTE_NOT_FOUND`）使用。
:::

## 相关 API

- [isNavigationFailure](./type-navigation-guard)
- [错误处理指南](../guide/error-handling)