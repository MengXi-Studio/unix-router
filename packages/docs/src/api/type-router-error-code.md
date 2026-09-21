# RouterErrorCode

路由错误码枚举。数值对齐 vue-router 4 的 `NavigationFailureType`，并补充 uni-app x 平台相关错误码。

```ts
import { RouterErrorCode } from '@meng-xi/unix-router'
```

| 错误码 | 数值 | 触发场景 |
| --- | --- | --- |
| `ABORTED` | `4` | 守卫返回 `false` 中止导航（含 `back()` 的 `delta` 非正整数） |
| `CANCELLED` | `8` | 守卫抛出 `Error`、守卫超时（`guardTimeout` 默认 10000ms）、重定向超过最大深度（10）、`back()` 页面栈不足 |
| `DUPLICATED` | `16` | 重复 `push` 当前地址（`path`+`query`+`params`+`hash` 与当前完全一致；仅 `push` 检测） |
| `ROUTE_NOT_FOUND` | `32` | 未匹配到路由（严格模式下 `name` 未注册）或位置非法（uni-app x 扩展） |
| `NAVIGATION_API_ERROR` | `64` | `uni.*` 原生导航 API 调用失败，或导航完成后页面栈顶确认失败（uni-app x 扩展） |
| `SETUP_ERROR` | `128` | 路由安装环境错误（uni-app x 扩展） |
| `PLUGIN_REQUIRED` | `256` | 使用插件能力但未注册对应插件（如未注册 `ParamsPlugin` 却使用 `params`、未注册 `EventsPlugin` 却使用 `events`）（uni-app x 扩展） |

::: tip 判断方式
配合 `isNavigationFailure(error, codes?)` 收窄判断，或直接比较（`failure.code === RouterErrorCode.ROUTE_NOT_FOUND`）。`NavigationFailure` 的 `to` / `from` 字段可进一步定位失败上下文。
:::

## 相关 API

- [isNavigationFailure](./type-navigation-guard)
- [错误处理指南](../guide/error-handling)
