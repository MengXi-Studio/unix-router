# Recipes

This chapter collects common solutions for real business scenarios, all of which can be used directly in your projects. It combines unix-router's features with uni-app x's limitations to provide best practices.

## Login Authentication

Redirect unauthenticated users to the login page when they visit a protected page, and return to the original page after login.

### Routes and Guards

```ts
// router/routes.ts
import type { RouteConfig } from '@meng-xi/unix-router'

export const routes: RouteConfig[] = [
	{ path: 'pages/index/index', name: 'home', meta: { isTab: true, title: 'Home' } },
	{ path: 'pages/login/login', name: 'login', meta: { title: 'Login' } },
	{ path: 'pages/profile/profile', name: 'profile', meta: { requireAuth: true, title: 'Profile' } }
]
```

```ts
// router/index.ts
import { createRouter } from '@meng-xi/unix-router'
import { routes } from './routes'

const router = createRouter({ routes, strict: true })

function isLoggedIn(): boolean {
	return !!uni.getStorageSync('token')
}

router.beforeEach((to, from) => {
	// 1. Unauthenticated user visiting a protected page → login page (replace, to avoid returning to an intermediate state)
	if (to.meta.requireAuth && !isLoggedIn()) {
		return {
			location: { name: 'login', query: new Map([['redirect', to.fullPath]]) },
			mode: 'replace'
		}
	}
	// 2. Logged-in user visiting the login page → home page
	if (to.name === 'login' && isLoggedIn()) {
		return { name: 'home' }
	}
})

export default router
```

### Returning to the Original Page from the Login Page

```ts
onLoginSuccess(async () => {
	const redirect = route.query.get('redirect')
	if (redirect) {
		await router.replace(redirect) // back to the original page; the login page leaves no stack entry
	} else {
		await router.relaunch({ name: 'home' })
	}
})
```

## Role-Based Access Control

Implement role-based access control by extending `RouteMeta` and guards.

```ts
// types/router.d.ts
import '@meng-xi/unix-router'

declare module '@meng-xi/unix-router' {
	interface RouteMeta {
		roles?: string[]
	}
}
```

```ts
const routes: RouteConfig[] = [
	{ path: 'pages/admin/admin', name: 'admin', meta: { roles: ['admin'], title: 'Admin' } }
]

router.beforeEach((to, from) => {
	const roles = to.meta.roles
	if (roles && !hasRole(roles)) {
		uni.showToast({ title: 'No access', icon: 'none' })
		return { name: 'home' }
	}
})
```

## Leaving a Form Confirmation

Prevent users from accidentally leaving an unsaved form, implemented with the in-component leave guard `onBeforeRouteLeave`.

```vue
<script setup lang="uts">
import { ref } from 'vue'
import { onBeforeRouteLeave } from '@meng-xi/unix-router'

const dirty = ref(false)

onBeforeRouteLeave((to, from) => {
	if (dirty.value) {
		// return a Promise; resolve(true) allows / resolve(false) blocks
		return new Promise<boolean>((resolve) => {
			uni.showModal({
				title: 'Notice',
				content: 'There are unsaved changes. Leave anyway?',
				success: (res) => resolve(res.confirm)
			})
		})
	}
})
</script>
```

::: tip Platform Limitations
The in-component leave guard works under controlled navigations (`router.back` / `push`, etc.). Native Mini Program back (top arrow / swipe) is controlled by the host and cannot be intercepted synchronously; use `onRouteChange` + `syncRoute` to handle it afterward. See [Platform Compatibility](./compatibility).
:::

## Data Prefetching

Prefetch data before navigation using `router.beforeResolve`.

```ts
const preloaders: Record<string, (to: RouteLocation) => Promise<void>> = {
	detail: async (to) => {
		const store = useDetailStore()
		await store.fetchDetail(to.query.get('id'))
	}
}

router.beforeResolve(async (to, from) => {
	const loader = preloaders[to.name as string]
	if (loader) {
		uni.showLoading({ title: 'Loading...' })
		try {
			await loader(to)
		} catch (err) {
			uni.showToast({ title: 'Failed to load', icon: 'none' })
			return false // data loading failed, abort the navigation
		} finally {
			uni.hideLoading()
		}
	}
})
```

## Automatically Setting the Page Title

Use `afterEach` to set the navigation bar title consistently.

```ts
router.afterEach((to) => {
	const title = to.meta.title as string | undefined
	uni.setNavigationBarTitle({ title: title || 'Default Title' })
})
```

## Page Stack Depth Management

Prevent Mini Program page stack overflow (limit is about 10 levels) by wrapping a safe navigation helper.

```ts
const STACK_WARNING_THRESHOLD = 8

async function safePush(location: RouteLocationRaw) {
	const pages = getCurrentPages()
	if (pages.length >= STACK_WARNING_THRESHOLD) {
		console.warn('[unix-router] page stack near limit, use relaunch instead')
		await router.relaunch(location)
	} else {
		await router.push(location)
	}
}
```

## Analytics Tracking

Build page tracking using `afterEach` for complete navigations plus `onRouteChange` for state sync.

```ts
router.afterEach((to, from) => {
	analytics.report('page_view', to.path, from.path, false)
})

router.onRouteChange((to, from) => {
	// state sync (physical back, etc.) can be distinguished here
	if (to._synced) {
		analytics.report('page_view', to.path, from.path, true)
	}
})
```

## Guard Composition Order

When composing multiple guards, register them in the following order:

1. Maintenance / global interception (first)
2. Login authentication
3. Access control
4. Data prefetching (`beforeResolve`)
5. Post-processing (`afterEach`)

```ts
setupMaintenanceGuard(router)
setupAuthGuard(router)
setupPermissionGuard(router)
setupPreloadGuard(router)
setupTitleGuard(router)
setupAnalyticsGuard(router)
```

## Route Modularization

Split routes by module in large projects and then merge them.

```ts
// modules/user/routes.ts
export const userRoutes: RouteConfig[] = [
	{ path: 'pages/profile/profile', name: 'profile', meta: { requireAuth: true } }
]

// router/routes.ts
import { userRoutes } from '@/modules/user/routes'
import { orderRoutes } from '@/modules/order/routes'

export const routes = [
	{ path: 'pages/index/index', name: 'home', meta: { isTab: true } },
	...userRoutes,
	...orderRoutes
]
```

## Next Steps

- [FAQ](./faq) — frequently asked questions and troubleshooting
- [Platform Compatibility](./compatibility) — platform limitations
- [API Reference](../api/create-router) — full API documentation