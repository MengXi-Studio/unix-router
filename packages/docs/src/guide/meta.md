# 路由元信息

`meta` 是挂在路由配置上的强类型元数据（UTS `type`），供守卫、页面、组件读取。内置三个常用字段，覆盖**页面标题**、**TabBar 判定**、**登录拦截**三类高频场景。

## 内置字段

```ts
type RouteMeta = {
	title?: string        // 页面标题（可用于导航栏标题）
	isTab?: boolean       // 是否为 tabBar 页面（决定使用 switchTab 导航）
	requireAuth?: boolean // 是否要求登录（常与守卫配合做登录拦截）
}
```

路由配置示例：

```ts
{ path: 'pages/index/index', name: 'home', meta: { title: '首页', isTab: true } }
```

## 读取 meta

页面 / 组合式 API 中通过 `useRoute()` 响应式读取：

```ts
import { useRoute } from '@meng-xi/unix-router'
const route = useRoute()

route.meta.title
route.meta.isTab
```

守卫中读取「目标页」的 meta 做导航决策：

```ts
router.beforeEach((to, from) => {
	if (to.meta.requireAuth === true) {
		// …
	}
	return true
})
```

## title：页面标题

### 在 afterEach 中统一设置（推荐）

一次注册，全局生效——每完成一次导航，就按目标页 `meta.title` 更新导航栏标题：

```ts
router.afterEach((to, from, failure) => {
	if (failure !== null) {
		return // 导航失败不更新标题
	}
	const title = to.meta.title ?? ''
	if (title.length > 0) {
		uni.setNavigationBarTitle({ title })
	}
})
```

### 在页面中按需使用

```ts
import { useRoute } from '@meng-xi/unix-router'
const route = useRoute()

onShow(() => {
	uni.setNavigationBarTitle({ title: route.meta.title ?? '' })
})
```

## isTab：TabBar 页判定

目标路由 `meta.isTab === true` 时，路由器自动改用 `uni.switchTab` 导航（`push` / `replace` / `relaunch` 均适用）。注意 `switchTab` **不携带 query**，Tab 页参数请走全局状态或[页面间通信](./events)。

::: warning 别忘了标记 TabBar 页
凡出现在 `pages.json` tabBar 列表中的页面，路由配置都要加 `isTab: true`，否则会被 `navigateTo` 打开而失败。
:::

## requireAuth：登录拦截

与全局守卫配合实现登录拦截（完整流程见[路由守卫](./guards)）：

```ts
router.beforeEach((to, from) => {
	if (to.meta.requireAuth === true && !isLoggedIn()) {
		return {
			location: { name: 'login', query: new Map<string, string>([['redirect', to.fullPath]]) },
			mode: 'replace'
		}
	}
	return true
})
```

## 自定义 meta 字段

**UTS 不支持接口声明合并**，vue-router 的模块增强写法（`declare module` + `interface RouteMeta`）在 uni-app x 下不可用。自定义字段必须**直接在 `RouteMeta` type 定义中追加字段**。

本库以 UTS 源码分发，直接编辑类型定义文件即可（npm 引入位于 `node_modules/@meng-xi/unix-router`，uni_modules 引入位于 `uni_modules/ux-router`）：

```ts
// RouteMeta 类型定义
export type RouteMeta = {
	title?: string
	isTab?: boolean
	requireAuth?: boolean
	// —— 以下为追加的自定义字段 ——
	icon?: string          // 页面图标
	requireAdmin?: boolean // 管理员页面
}
```

追加后即可在路由配置与守卫中直接使用（享受完整类型提示）：

```ts
{ path: 'pages/admin/admin', name: 'admin', meta: { title: '管理后台', requireAdmin: true } }
```

::: tip 与 vue-router 的关键差异
vue-router 通过 `declare module 'vue-router' { interface RouteMeta { … } }` 声明合并扩展 meta；UTS 采用名义类型系统、**没有声明合并**，只能在原类型上修改。
:::

## 常见坑

- **忘记设 `meta.isTab`**：TabBar 页被 `navigateTo` 打开会失败。给 TabBar 页统一加 `isTab: true`。
- **可选字段直接 truthy 判断**：UTS 强类型下请用 `=== true` / `!= null` 显式判断，例如 `if (to.meta.requireAuth === true)`。
- **在错误时机读 meta**：路由状态在页面 `onShow` 时由路由器同步（`app.use(router)` 已自动接管），请勿在更早的时机读取目标页 meta。

## 下一步

- [路由守卫](./guards) — `requireAuth` 登录拦截的完整流程
- [路由导航](./navigation) — `isTab` 如何自动切换 `switchTab`
- [RouteMeta 类型](../api/type-route-meta) — 类型定义参考
