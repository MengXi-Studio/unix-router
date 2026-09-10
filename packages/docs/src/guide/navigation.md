# 路由导航

unix-router 提供 `push / replace / relaunch / back` 四种导航，底层映射到 uni 原生导航 API，并自动识别 tabBar 页面。

## 导航方式

| 方式 | 对应 uni API | 说明 |
| --- | --- | --- |
| `push` | `navigateTo` / `switchTab` | 入栈跳转，可返回 |
| `replace` | `redirectTo` / `switchTab` | 替换当前页，不新增栈 |
| `relaunch` | `reLaunch` / `switchTab` | 关闭所有页并打开目标 |
| `back` | `navigateBack` | 返回上一页/多级 |

`router.push` 等均返回 `Promise`，导航成功 resolve、失败 reject（可用 `isNavigationFailure` 判定）。

## 位置形式（RouteLocationRaw）

既支持字符串，也支持对象：

```ts
// 1. 字符串路径
await router.push('/pages/about/about')

// 2. 路径对象（path 优先于 name）
await router.push({ path: 'pages/about/about', query: new Map([['a', '1']]) })

// 3. 命名对象（推荐用 name，编译一致）
await router.push({ name: 'about', query: new Map([['a', '1']]) })
```

::: tip query 与 params 都是 Map
uni-app x 的 query/params 以 `Map<string, string>` 承载，读取用 `.get(key)` / `.has(key)`。
:::

## 传递查询参数 query

query 会出现在 URL 中，可刷新保留：

```ts
await router.push({
	name: 'detail',
	query: new Map([['id', '1024']])
})

// 目标页
const route = useRoute()
console.log(route.query.get('id')) // '1024'
```

## 传递对象参数 params（ParamsPlugin）

复杂对象不适合塞进 URL。注册 `ParamsPlugin` 后，`params` 通过 `__params__` 内存 key 通道传递：

```ts
import { createRouter, ParamsPlugin } from '@meng-xi/unix-router'

const router = createRouter({ routes, plugins: [ParamsPlugin] })

// 发起页
await router.push({
	name: 'detail',
	params: new Map<string, string>([['id', '1024'], ['name', '张伟']])
})

// 目标页读取
const route = useRoute()
console.log(route.params.get('id'))   // '1024'
console.log(route.params.get('name')) // '张伟'
```

> 需要跨刷新保留参数时可开启 `paramsPersistent: true`（存入 storage）。未注册 `ParamsPlugin` 却用 `params` 会抛 `PLUGIN_REQUIRED`。详见[插件系统](./plugins)。

## tabBar 页面

目标路由 `meta.isTab === true` 时，路由器自动改用 `uni.switchTab`：

```ts
const routes: RouteConfig[] = [
	{ path: 'pages/index/index', name: 'home', meta: { isTab: true } },
	{ path: 'pages/mine/mine', name: 'mine', meta: { isTab: true } }
]

await router.push({ name: 'mine' }) // 自动走 switchTab
```

> `switchTab` 不支持 query，请在 tabBar 页用 `onShow` + 全局状态携带参数（见[实战指南](./recipes#tabbar-页面数据传递)）。

## 返回

```ts
await router.back()   // 返回上一页
await router.back(2)  // 返回两级
```

- `delta` 需为正整数
- 页面栈不足时返回 `CANCELLED` 失败
- App/H5 的返回会经过守卫链；小程序的宿主返回需 `onRouteChange` 事后处理（见[平台兼容性](./compatibility)）

## 重复导航与并发

- **重复导航**：`push` 到与当前完全相同的位置会抛 `DUPLICATED`，可用 `replace` 规避或捕获忽略
- **并发排队**：上一导航未完成时，新导航会等待其完成再执行

```ts
try {
	await router.push({ name: 'about' })
} catch (err) {
	if (router.code === 16) return // RouterErrorCode.DUPLICATED，忽略
	// 其他导航失败：处理
}
```

## 程序化导航与声明式

除了调用 `router.push`，还可以：

```ts
// 程序化（任意上下文）
router.replace('/pages/login/login')

// 声明式（组件）
import { RouterLink } from '@meng-xi/unix-router'
// <RouterLink to="pages/about/about">关于</RouterLink>
```

## 冷启动直接进入

`uni-app x` 冷启动/直接 URL 进入页面**不经过守卫链**（页面由 pages.json 直接加载）。如需补跑守卫，用 `guardRoute`：

```ts
// App.vue onLaunch
router.isReady().then(() => {
	router.guardRoute(`/${options?.path ?? ''}`, {
		onAbort: (failure) => router.relaunch({ name: 'login' })
	})
})
```

## 下一步

- [路由守卫](./guards) — 导航过程中的权限控制
- [插件系统](./plugins) — ParamsPlugin / InterceptorPlugin
- [组合式 API](./composables) — useRoute / useLink