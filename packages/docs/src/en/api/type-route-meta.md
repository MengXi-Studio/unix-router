# RouteMeta

Route metadata (the type of `RouteConfig.meta`). Common fields are built in, and custom meta can be extended via module augmentation.

```ts
interface RouteMeta {
	title?: string      // page title
	isTab?: boolean     // whether it is a tabBar page (decides whether to navigate with switchTab)
	requireAuth?: boolean // whether login is required (guards can intercept based on this)
}
```

## Extending Custom meta

```ts
// env.d.ts / router.d.ts
import '@meng-xi/unix-router'

declare module '@meng-xi/unix-router' {
	interface RouteMeta {
		roles?: string[]
		keepAlive?: boolean
	}
}
```

After extending, you can use it with type hints in both config and guards:

```ts
const routes: RouteConfig[] = [
	{ path: 'pages/admin/admin', name: 'admin', meta: { roles: ['admin'], title: 'Admin' } }
]

router.beforeEach((to, from) => {
	const roles = to.meta.roles
	if (roles && !hasRole(roles)) return { name: 'home' }
})
```

## Related APIs

- [RouteConfig](./type-route-config)
- [Route Metadata Guide](../guide/meta)