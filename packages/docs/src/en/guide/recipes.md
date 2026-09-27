# Recipes

This chapter collects common patterns for real-world business scenarios; all can be used directly in your project. They combine unix-router features with uni-app x constraints to provide best practices.

> **See a complete runnable version first**: the repo's [`packages/playground`](https://github.com/MengXi-Studio/unix-router/tree/master/packages/playground) is a full uni-app x project integrating "home entry + query/params passing + login guard + useLink + self-check", you can verify each section of this page against it directly.

## Full Login Auth Flow

When an unauthenticated user accesses a protected page, redirect to the login page (a `NavigationRedirect` redirect + a `redirect` query recording the origin), then return to the original page after login.

### Routes and Guard

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
	return uni.getStorageSync('token') != null
}

router.beforeEach((to, from) => {
	// 1. Unauthenticated access to a protected page → login page (replace, avoid returning to the intermediate state); query records the return address
	if (to.meta.requireAuth === true && !isLoggedIn()) {
		return {
			location: { name: 'login', query: new Map<string, string>([['redirect', to.fullPath]]) },
			mode: 'replace'
		}
	}
	// 2. Authenticated user visiting the login page → home page
	if (to.name === 'login' && isLoggedIn()) {
		return { name: 'home' }
	}
	return true
})

export default router
```

### Return to the Origin Page After Login

```ts
async function onLoginSuccess(): Promise<void> {
	const redirect = route.query.get('redirect')
	if (redirect != null) {
		await router.replace(redirect) // back to the original page, leaving no trace of the login page on the stack
	} else {
		await router.relaunch({ name: 'home' })
	}
}
```

When landing directly on a protected page on a cold start, the guard has not executed — see [Cold Start Guard](#cold-start-guard) below.

## TabBar Apps

Pages with `meta.isTab: true` automatically switch to `uni.switchTab` on navigation (this applies to `push` / `replace` / `relaunch` alike). Note that **`uni.switchTab` discards the entire query string**, so neither `query` nor `params` (which travels via an internal query key) reaches the tabBar page — use global state or storage to pass data:

```ts
const routes: RouteConfig[] = [
	{ path: 'pages/index/index', name: 'home', meta: { isTab: true, title: 'Home' } },
	{ path: 'pages/mine/mine', name: 'mine', meta: { isTab: true, title: 'Mine' } }
]

await router.push({ name: 'home' }) // switchTab automatically
```

```ts
// Write before navigating
uni.setStorageSync('mine_entry', 'settings')
await router.push({ name: 'mine' })

// Read and clean up in the tabBar page's onShow
uni.removeStorageSync('mine_entry')
```

For complex structured data, keep it in a module-level reactive global state instead of storage.

## Detail Page Parameter Passing

For scenarios like a detail page that "needs an id the moment it opens", passing params with [ParamsPlugin](./params) is more direct than stitching a URL query (the value never appears in the URL):

```ts
import { createRouter, ParamsPlugin } from '@meng-xi/unix-router'

const router = createRouter({ routes, plugins: [new ParamsPlugin()] })

// List page
await router.push({
	name: 'detail',
	params: new Map<string, string>([['id', '1024']])
})
```

```vue
<!-- pages/detail/detail.uvue -->
<script setup lang="uts">
import { useRoute } from '@meng-xi/unix-router'

const route = useRoute()
const id = route.params.get('id') // '1024'
</script>

<template>
	<view class="page">
		<text>Detail ID: {{ id }}</text>
	</view>
</template>
```

::: warning
`params` values must be strings and JSON-serializable. Using `params` without registering `ParamsPlugin` throws `PLUGIN_REQUIRED`.
:::

## Page-to-Page Communication Callback

For "list page → edit page → save and return the result", use [EventsPlugin](./events): the opener registers an `events` listener map, and the opened page sends data **back** via `useOpenerEventChannel()`.

```ts
import { createRouter, EventsPlugin } from '@meng-xi/unix-router'

const router = createRouter({ routes, plugins: [new EventsPlugin()] })

// Opener (list page): register the listener map
await router.push({
	path: 'pages/edit/edit',
	events: new Map<string, (data: any) => any>([
		['saved', (data: any) => {
			console.log('Edit page returned:', data)
			refreshList()
		}]
	])
})
```

```vue
<!-- pages/edit/edit.uvue -->
<script setup lang="uts">
import { onUnload } from '@dcloudio/uni-app'
import { useOpenerEventChannel } from '@meng-xi/unix-router'

const channel = useOpenerEventChannel() // EventChannel | null

function save(): void {
	if (channel != null) {
		channel.emit('saved', { title: 'New title' }) // send back to the opener
	}
}

onUnload(() => {
	if (channel != null) {
		channel.off('saved') // optional: remove the listener by id
	}
})
</script>
```

`useOpenerEventChannel()` can be called and emit data right inside a page's `onShow`, with no dependency on route-state-sync timing.

## Navigation Animation

Use [AnimationPlugin](./animation) to configure a global default animation; a single navigation can override it:

```ts
import { createRouter, AnimationPlugin } from '@meng-xi/unix-router'

const router = createRouter({
	routes,
	plugins: [new AnimationPlugin()],
	animation: { type: 'slide-in-right', duration: 300 } // global default
})

// Per-navigation override: this navigation uses fade-in
await router.push({
	path: 'pages/detail/detail',
	animationType: 'fade-in',
	animationDuration: 500
})
```

- On App / Mini Program, `animationType` is passed through natively; on H5, the plugin implements it with the Web Animations API (a back first plays the exit animation, then the real `navigateBack`).
- `back()` uses the global default animation as the exit animation (back has no location to carry; per-navigation override only applies to forward navigations).
- `switchTab` has no animation.

## 404 Fallback

Under `strict: true` (the default), an unmatched named route throws `RouterError ROUTE_NOT_FOUND` immediately. Combine `onError` with `guardRoute`'s `onAbort` to fall back to the home page:

```ts
import { isNavigationFailure, RouterErrorCode } from '@meng-xi/unix-router'

const router = createRouter({ routes, strict: true })

// Controlled navigation: named route missing / resolve failed
router.onError((error, to, from) => {
	if (isNavigationFailure(error, RouterErrorCode.ROUTE_NOT_FOUND)) {
		router.relaunch({ name: 'home' })
	}
})

// Cold start: page has loaded but the guard rules it unreachable (e.g. requireAuth without login, invalid target)
router.isReady().then(() => {
	router.guardRoute(undefined, {
		onAbort: (failure) => {
			router.relaunch({ name: 'home' })
		}
	}).catch(() => {})
})
```

## Cold Start Guard

When a user lands directly via an H5 direct URL or an App deeplink / scheme, the page has loaded but **the guard chain never executed**. `guardRoute()` only re-runs the global `beforeEach` (it does not run `beforeEnter` / `beforeResolve` / `afterEach` again) without performing actual navigation:

```ts
router.isReady().then(() => {
	router.guardRoute(undefined, {
		onAbort: (failure) => {
			// the page has already rendered and cannot truly be blocked; jump to the login page or a safe page
			router.relaunch({ name: 'login' })
		}
	}).catch(() => {})
})
```

- Guard **allows**: returns the target location; no handling needed.
- Guard **aborts**: fires `onAbort` (and rejects); the page has loaded and cannot be blocked — redirect to a safe page here.
- Guard **redirects**: performs a real navigation in the redirect mode, defaulting to `relaunch`.

## Role-Based Access Control

Implement role-based access control by extending `RouteMeta` and a guard.

```ts
// types/router.d.ts (only affects TS / editor)
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
	if (roles != null && !hasRole(roles)) {
		uni.showToast({ title: 'No permission', icon: 'none' })
		return { name: 'home' }
	}
	return true
})
```

> ⚠️ **UTS limitation**: the `declare module` augmentation above only works for TS/editor autocomplete; uni-app x native platforms do not support interface declaration merging. To make a field available at the App native compile time, extend it directly at the type declaration — see [Route Meta](./meta#custom-meta-fields).

## Form Leave Confirmation

Prevent a user from accidentally leaving an unsaved form via the in-component leave guard `onBeforeRouteLeave`.

```vue
<script setup lang="uts">
import { ref } from 'vue'
import { onBeforeRouteLeave } from '@meng-xi/unix-router'

const dirty = ref(false)

onBeforeRouteLeave((to, from) => {
	if (dirty.value) {
		// return a Promise: resolve(true) allows / resolve(false) blocks
		return new Promise<boolean>((resolve) => {
			uni.showModal({
				title: 'Notice',
				content: 'You have unsaved changes. Leave anyway?',
				success: (res) => resolve(res.confirm)
			})
		})
	}
	return true
})
</script>
```

::: tip Platform limitation
The in-component leave guard takes effect under controlled navigation (`router.back` / `push`, etc.). A Mini Program's native back (top arrow / swipe) is controlled by the host and cannot be intercepted synchronously; use `onRouteChange` + `syncRoute` to handle it after the fact. See [Platform Compatibility](./compatibility).
:::

## Data Pre-fetching

Pre-fetch data before navigation using `router.beforeResolve`.

```ts
const preloaders = new Map<string, (to: RouteLocation) => Promise<void>>()
preloaders.set('detail', async (to) => {
	await fetchDetail(to.query.get('id'))
})

router.beforeResolve(async (to, from) => {
	if (to.name == null) {
		return true
	}
	const loader = preloaders.get(to.name)
	if (loader != null) {
		uni.showLoading({ title: 'Loading...' })
		try {
			await loader(to)
		} catch (err) {
			uni.showToast({ title: 'Load failed', icon: 'none' })
			return false // data load failed, abort the navigation
		} finally {
			uni.hideLoading()
		}
	}
	return true
})
```

## Auto Page Title

Set the navigation bar title uniformly with `afterEach`.

```ts
router.afterEach((to, from, failure) => {
	if (failure != null) {
		return
	}
	const title = to.meta.title
	uni.setNavigationBarTitle({ title: title != null ? title : 'Default title' })
})
```

## Page Stack Depth Management

Guard against Mini Program page stack overflow (cap around 10 levels) by wrapping a safe navigation.

```ts
const STACK_WARNING_THRESHOLD = 8

async function safePush(location: RouteLocationRaw): Promise<void> {
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

`afterEach` fires only after a controlled navigation completes; `onRouteChange` covers all route changes (navigation completion + state sync, e.g. physical back, tab switch). Combining the two gives you complete analytics.

```ts
// Controlled navigation analytics
router.afterEach((to, from) => {
	analytics.report('page_view', to.path, from.path)
})

// All route changes (including physical back, tab switch, and other state syncs)
router.onRouteChange((to, from) => {
	analytics.report('route_change', to.path, from.path)
})
```

## Guard Composition Order

When composing multiple guards, register them in this order:

1. Maintenance / global interception (first)
2. Login authentication
3. Permission control
4. Data pre-fetching (`beforeResolve`)
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

Large projects split routes by module, then merge them.

```ts
// modules/user/routes.ts
export const userRoutes: RouteConfig[] = [
	{ path: 'pages/profile/profile', name: 'profile', meta: { requireAuth: true } }
]

// router/routes.ts
import { userRoutes } from '@/modules/user/routes'
import { orderRoutes } from '@/modules/order/routes'

export const routes: RouteConfig[] = [
	{ path: 'pages/index/index', name: 'home', meta: { isTab: true } },
	...userRoutes,
	...orderRoutes
]
```

## Next Steps

- [Navigation Flow](./navigation-flow) — understand where guards and plugins sit in the timeline
- [Plugin System](./plugins) — an overview of the four built-in plugins
- [FAQ](./faq) — frequent questions and troubleshooting
- [API Reference](../api/create-router) — the full API documentation
