# 错误处理

unix-router 提供 vue-router 风格的完整错误体系。

## 错误类型

| 类型 | 说明 |
| --- | --- |
| `RouterError` | 路由器错误基类（含 `code` / `to` / `from`） |
| `NavigationFailure` | 导航失败（继承 `RouterError`，被中止/取消/重复时抛出） |
| `RouterErrorCode` | 错误码枚举 |

## 错误码 RouterErrorCode

| 枚举 | 值 | 说明 |
| --- | --- | --- |
| `ABORTED` | 4 | 守卫返回 `false` 中止导航 |
| `CANCELLED` | 8 | 守卫抛错 / 重定向超深度上限 |
| `DUPLICATED` | 16 | 重复导航（`push` 到 `path+query+params+hash` 与当前完全一致） |
| `ROUTE_NOT_FOUND` | 32 | 严格模式下未匹配到命名路由 |
| `NAVIGATION_API_ERROR` | 64 | `uni.*` 原生导航 API 失败 |
| `SETUP_ERROR` | 128 | 路由安装环境错误 |

## 捕获导航失败

编程式导航可能 reject，用 `isNavigationFailure` 收窄判断：

```ts
import { isNavigationFailure, RouterErrorCode } from '@meng-xi/unix-router'

try {
	await router.push('/pages/index/index')
} catch (e) {
	// e 为 Error；isNavigationFailure 收窄为 NavigationFailure
	const failure = e as NavigationFailure
	if (isNavigationFailure(failure, RouterErrorCode.DUPLICATED)) {
		// 重复导航，可忽略
	} else if (isNavigationFailure(failure, RouterErrorCode.ABORTED)) {
		// 守卫中止
	}
}
```

## 全局错误处理 onError

导航抛出的错误会同步触发所有 `onError` 处理器（`afterEach` 也会收到失败）：

```ts
router.onError((error, to, from) => {
	console.warn(`导航失败(${to.fullPath}): ${error.message}`)
})
```

## 守卫内部抛错

守卫抛出 `Error` 会取消导航（`CANCELLED`），并触发 `onError` / `afterEach(failure)`。

## 触发时机小结

- 守卫 **中止/取消**：`afterEach(to, from, failure)` + 各 `onError` 回调被调用。
- 原生 API **调用失败**：`currentRoute` 回退到来源路由，并触发错误处理。
- **重复导航**：仅抛 `DUPLICATED`，不调用 `onError` 之外的多余逻辑（可按需忽略）。只有 `path+query+params+hash` 全部一致才触发。

## 实战处理策略

导航失败大体分三类，处理目标不同：

| 场景 | 错误码 | 处理方式 |
| --- | --- | --- |
| 重复导航 | `DUPLICATED` | 忽略（已在目标页） |
| 守卫中止 | `ABORTED` / `CANCELLED` | 静默或提示"操作被取消"；守卫已负责重定向，勿再导航 |
| 真错误 | `ROUTE_NOT_FOUND` / `NAVIGATION_API_ERROR` 等 | 上报 + 给用户提示 |

**推荐写法**：`push` 装进 try/catch，按错误码分流，其余 `throw` 交给 `onError`：

```ts
async function safePush(location: RouteLocationRaw) {
	try {
		await router.push(location)
		return true
	} catch (e) {
		const failure = e as NavigationFailure
		if (isNavigationFailure(failure, RouterErrorCode.DUPLICATED)) {
			return false          // 已在目标页，不算失败
		}
		if (isNavigationFailure(failure, RouterErrorCode.ABORTED)) {
			return false          // 守卫主动中止，属预期
		}
		console.error('导航失败', (e as Error).message)
		return false
	}
}
```

**通用兜底**：在 `onError` 统一登记所有非预期失败（埋点 / 日志），避免每个调用点重复写。

> 提示：`try/catch` 里 `catch (e)` 的 `e` 在 UTS 中是 `unknown`/`Error`，统一 `(e as Error).message` 或 `(e as NavigationFailure)` 收窄后读取。