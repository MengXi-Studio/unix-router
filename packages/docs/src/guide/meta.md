# 路由元信息

每个路由可通过 `meta` 携带任意自定义数据，供守卫、页面、组件读取。内置常用字段，且是强类型（UTS `type`）。

## 内置字段

```ts
type RouteMeta = {
	title?: string        // 页面标题（可用于导航栏标题）
	isTab?: boolean       // 是否为 tabBar 页面（决定使用 switchTab）
	requireAuth?: boolean // 是否要求登录（常与守卫配合）
}
```

```ts
{ path: 'pages/index/index', name: 'home', meta: { title: '首页', isTab: true } }
```

## 读取 meta

在页面 / 组合式 API 中：

```ts
import { useRoute } from '@meng-xi/unix-router'
const route = useRoute()

route.meta.title        // 当前页标题
route.meta.isTab
```

在守卫中读取"目标页"的 meta 来决策：

```ts
router.beforeEach((to, from) => {
	// 按 meta 决定导航行为
	if (to.meta.isTab === true) {
		// 走 switchTab
	}
})
```

## meta 的三种典型用途

### 1. 登录鉴权开关 `requireAuth`

```ts
router.beforeEach((to, from) => {
	if (to.meta.requireAuth === true && !isLoggedIn()) {
		return { name: 'login', query: new Map([['redirect', to.fullPath]]) }
	}
	return true
})
```

### 2. 页面标题 `title`

在页面 `onShow` 设置导航栏标题：

```ts
import { useRoute } from '@meng-xi/unix-router'
const route = useRoute()

onShow(() => {
	uni.setNavigationBarTitle({ title: route.meta.title ?? '' })
})
```

### 3. TabBar 判定 `isTab`

导航器用它决定 `switchTab` 还是 `navigateTo`。**记得在路由配置里把 TabBar 页的 `meta.isTab` 设为 `true`**。

## 自定义 meta 字段

在 UTS 中 `RouteMeta` 是 `type`，自定义字段直接在类型声明处追加（推荐在路由配置附近建一个类型文件并 re-export）：

```ts
// 在你的类型文件里扩展后再使用
export type AppRouteMeta = {
	// 继承内置字段会在引用处合并；这里按需自定义你自己的元信息
	icon?: string
	tag?: 'new' | 'hot'
}
```

> ⚠️ 与 vue-router 不同：TS 的 `declare module { interface RouteMeta {...} }` 声明合并依赖接口继承，且 **UTS 不支持接口声明合并**，因此自定义 meta 请直接扩展类型定义，而非靠模块增强。

## 常见坑

- **忘记设 `meta.isTab`**：TabBar 页被 `navigateTo` 打开会失败/异常。给 TabBar 页 `isTab: true`。
- **读取未定义字段**：可选字段读取可能为 `undefined`/`null`，判断用 `=== true` / `=== undefined`，避免直接 truthy（UTS 强类型）。
- **在错误时机读 meta**：页面首帧前读到的可能是初始路由的 meta，需 `syncRoute()` 后再读（见[组合式 API](./composables#与页面生命周期的配合)）。