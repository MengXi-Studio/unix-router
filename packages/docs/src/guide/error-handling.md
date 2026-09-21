# 错误处理

unix-router 提供 vue-router 风格的完整错误体系：导航失败统一以 `Promise` reject 表达，通过错误码精准分类。

## 错误对象层级

```
Error
└── RouterError          // 路由器错误基类
    └── NavigationFailure // 导航失败（被中止 / 取消 / 重复等）
```

`RouterError` / `NavigationFailure` 除 `message` / `name` 外携带三个字段：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `code` | `RouterErrorCode` | 错误码，见下方错误码全表 |
| `to` | `RouteLocation` | 触发错误的目标路由 |
| `from` | `RouteLocation` | 触发错误的来源路由 |

另有 `UniNavigationApiError`（接口）：`uni.*` 原生导航 API `fail` 回调的错误负载（`errMsg` / `context`），用于原生失败原因定位。

## isNavigationFailure 精准判断

`isNavigationFailure(error, codes?)` 判断错误是否为（指定类型的）导航失败：

```ts
import { isNavigationFailure, RouterErrorCode } from '@meng-xi/unix-router'

// 传错误码：判断是否为该类型的导航失败
isNavigationFailure(err, RouterErrorCode.DUPLICATED) // boolean
// 不传错误码：仅判断是否为导航失败
isNavigationFailure(err)
```

## 错误码全表

| 错误码 | 数值 | 触发场景 |
| --- | --- | --- |
| `ABORTED` | `4` | 守卫返回 `false` 中止导航（含 `back()` 的 `delta` 非正整数） |
| `CANCELLED` | `8` | 守卫抛出 `Error`、守卫超时（默认 10s）、重定向超过深度上限（10）、`back()` 页面栈不足 |
| `DUPLICATED` | `16` | 重复 `push` 当前地址（`path`+`query`+`params`+`hash` 与当前完全一致；仅 `push` 检测） |
| `ROUTE_NOT_FOUND` | `32` | 未匹配到路由（严格模式下 `name` 未注册）或位置非法 |
| `NAVIGATION_API_ERROR` | `64` | `uni.*` 导航 API 调用失败，或导航完成后页面栈顶确认失败（500ms 轮询） |
| `SETUP_ERROR` | `128` | 路由安装环境错误 |
| `PLUGIN_REQUIRED` | `256` | 使用 `params` / `events` 等插件能力但未注册对应插件（`ParamsPlugin` / `EventsPlugin`） |

## Promise reject 处理模式

导航失败**不会同步 throw**，全部通过 `Promise` reject 传递（对齐 vue-router）。两种处理模式：

```ts
import { isNavigationFailure, RouterErrorCode, NavigationFailure } from '@meng-xi/unix-router'

// 模式一：try/catch + await
async function goDetail() {
	try {
		await router.push({ name: 'detail' })
	} catch (e) {
		const failure = e as NavigationFailure
		if (isNavigationFailure(failure, RouterErrorCode.DUPLICATED)) {
			return // 已在目标页，忽略
		}
		console.error('导航失败', (e as Error).message)
	}
}

// 模式二：.catch
router.push({ name: 'detail' }).catch((e: any) => {
	const failure = e as NavigationFailure
	console.warn('导航失败', failure.message)
})
```

::: tip 导航失败不 throw
即使守卫返回 `false`、原生 API 失败，`router.push(...)` 之后的同步代码仍会正常执行；需要感知结果时用 `await` / `.catch`。

```ts
router.push({ name: 'profile' })
console.log('导航已发起') // 立即执行，不会因守卫中止而跳过
```
:::

## 全局捕获 onError

`router.onError` 注册全局错误处理器，**返回取消函数**：

```ts
const offError = router.onError((error, to, from) => {
	// error: Error（导航失败时为 NavigationFailure，可 isNavigationFailure 收窄）
	console.warn(`导航失败 ${from.fullPath} -> ${to.fullPath}: ${error.message}`)
})

// 取消监听
offError()
```

触发时机小结：

- **守卫中止 / 取消**：`afterEach(to, from, failure)` 收到失败，各 `onError` 回调被调用；
- **原生 API 失败**：`currentRoute` 回退到来源路由，并触发错误处理；
- **重复导航**：仅 reject `DUPLICATED`，可按需忽略。

## 常见失败排查表

| 现象 | 错误码 | 可能原因 | 处理 |
| --- | --- | --- | --- |
| 导航被莫名拦截 | `ABORTED` (4) | 某个守卫返回了 `false` | 检查守卫分支是否符合预期 |
| 导航取消 | `CANCELLED` (8) | 守卫抛错 / 守卫超时（默认 10s）/ 重定向成环超 10 层 / `back` 栈不足 | 查看警告日志定位守卫；检查重定向条件与栈深度 |
| 连点按钮报错 | `DUPLICATED` (16) | 重复 `push` 当前地址 | 捕获忽略，或改用 `replace` |
| 收到 32 | `ROUTE_NOT_FOUND` | `name` 未在 `routes` 注册 / 位置非法（strict 模式） | 核对路由配置与 `pages.json` |
| 收到 64 | `NAVIGATION_API_ERROR` | 页面未在 `pages.json` 注册 / 原生 API `fail` / 栈顶确认失败 | 核对 `pages.json` 页面注册与路径一致性 |
| 收到 256 | `PLUGIN_REQUIRED` | 使用 `params` / `events` 但未注册对应插件 | 注册 `ParamsPlugin` / `EventsPlugin` |

> 提示：`catch (e)` 中的 `e` 在 UTS 中统一按 `(e as NavigationFailure)` 或 `(e as Error)` 收窄后读取字段。

## 下一步

- [插件系统](./plugins) — `PLUGIN_REQUIRED` 对应的插件注册方式
- [RouterErrorCode](../api/type-router-error-code) — 错误码枚举类型参考
