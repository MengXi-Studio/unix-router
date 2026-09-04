# 路由元信息

每个路由可携带 `meta` 元信息，用于在守卫、页面中访问。内置常用字段并通过类型提供提示。

## 内置字段

```ts
interface RouteMeta {
	title?: string // 页面标题
	isTab?: boolean // 是否为 tabBar 页面（决定 switchTab）
	requireAuth?: boolean // 是否要求登录（常与守卫配合）
}
```

```ts
{ path: 'pages/index/index', name: 'home', meta: { title: '首页', isTab: true } }
```

## 读写 meta

```ts
const route = useRoute()
console.log(route.meta.title) // 当前页标题
```

在守卫中读取目标页 meta 以决定行为：

```ts
router.beforeEach((to, from) => {
	if (to.meta.isTab) {
		// tab 页使用 switchTab
	}
})
```

## 自定义 meta（模块增强）

通过模块增强 `RouteMeta` 可加入自定义字段并获得类型提示：

```ts
declare module '@meng-xi/unix-router' {
	interface RouteMeta {
		animation?: string // 自定义动画名
		keepAlive?: boolean
	}
}
```

> 在 uni-app x 中，`meta` 以结构化接口承载（UTS 强类型），字段为可选属性。