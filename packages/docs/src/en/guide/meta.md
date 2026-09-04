# Route Metadata

Each route can carry `meta` metadata, accessible in guards and pages. Common fields are built in and type hints are provided.

## Built-in Fields

```ts
interface RouteMeta {
	title?: string // page title
	isTab?: boolean // whether it is a tabBar page (decides switchTab)
	requireAuth?: boolean // whether login is required (often used with guards)
}
```

```ts
{ path: 'pages/index/index', name: 'home', meta: { title: 'Home', isTab: true } }
```

## Reading and Writing meta

```ts
const route = useRoute()
console.log(route.meta.title) // current page title
```

Read the target page's meta inside a guard to decide behavior:

```ts
router.beforeEach((to, from) => {
	if (to.meta.isTab) {
		// tab pages use switchTab
	}
})
```

## Custom meta (Module Augmentation)

Augment the module's `RouteMeta` to add custom fields with type hints:

```ts
declare module '@meng-xi/unix-router' {
	interface RouteMeta {
		animation?: string // custom animation name
		keepAlive?: boolean
	}
}
```

> In uni-app x, `meta` is carried as a structured interface (UTS strongly typed), and the fields are optional properties.