# Quick Start

Walk through the core closed loop of unix-router with a runnable demo: define routes → create the router (with plugins) → install the app → navigate + guards.

## Prerequisites

- A uni-app x project (HBuilderX 4.0+ can run web / Mini Program; the minimum HBuilderX versions for addInterceptor per platform are in [Platform Compatibility](./compatibility))
- HBuilderX latest stable version is recommended

## 1. Install

```bash
npm install @meng-xi/unix-router
# or
pnpm add @meng-xi/unix-router
```

> In uni-app x you can also bring in the UTS source via `uni_modules` (this library ships as `.uts` sources, compiled on-site per platform).

## 2. Define the Route Config

Paths must match the registration in `pages.json`:

```ts
// router/routes.ts
import type { RouteConfig } from '@meng-xi/unix-router'

export const routes: RouteConfig[] = [
	{ path: 'pages/index/index', name: 'home', meta: { title: 'Home', isTab: true } },
	{ path: 'pages/about/about', name: 'about', meta: { title: 'About' } },
	{ path: 'pages/login/login', name: 'login', meta: { title: 'Login' } },
	{ path: 'pages/profile/profile', name: 'profile', meta: { title: 'Profile', requireAuth: true } }
]
```

## 3. Create the Router (with plugins)

```ts
// router/index.ts
import { createRouter, ParamsPlugin, InterceptorPlugin } from '@meng-xi/unix-router'
import { routes } from './routes'

export const router = createRouter({
	routes,
	strict: true, // unmatched named routes throw ROUTE_NOT_FOUND
	plugins: [ParamsPlugin, InterceptorPlugin], // page params + uni navigation interception
	interceptUniApi: true // InterceptorPlugin switch: external uni.navigateTo also runs guards
})
```

## 4. Install into the Vue App

```ts
// main.ts
import { createSSRApp } from 'vue'
import App from './App.uvue'
import { router } from './router'

export function createApp() {
	const app = createSSRApp(App)
	app.use(router) // provide($router/$route) + global onShow auto syncRoute
	return { app }
}
```

## 5. Use It in a Page

```vue
<!-- pages/index/index.uvue -->
<script setup lang="uts">
import { useRouter } from '@meng-xi/unix-router'

const router = useRouter()

const goAbout = () => {
	router.push({ name: 'about', query: new Map([['from', 'home']]) })
}
</script>

<template>
	<view class="page">
		<text class="title">unix-router demo</text>
		<button class="btn" @click="goAbout">push to the About page</button>
		<RouterLink to="pages/about/about">RouterLink navigation</RouterLink>
	</view>
</template>
```

::: tip Inspect the current route
Read reactively with `useRoute()`: `route.path` / `route.query` / `route.params` / `route.meta`.
:::

## 6. Guards (permissions)

```ts
// router/index.ts
router.beforeEach((to, from) => {
	// not logged in visiting a protected page → redirect to login and record the source
	if (to.meta.requireAuth && !isLoggedIn()) {
		return {
			location: { name: 'login', query: new Map([['redirect', to.fullPath]]) },
			mode: 'replace'
		}
	}
	// logged in visiting the login page → go home
	if (to.name === 'login' && isLoggedIn()) {
		return { name: 'home' }
	}
	return true
})
```

## 7. Passing Params (ParamsPlugin)

```ts
// source page
await router.push({
	name: 'detail',
	params: new Map<string, string>([['id', '1024']])
})

// target page
const route = useRoute()
console.log(route.params.get('id')) // '1024'
```

## Complete Runnable Template

The `packages/playground` in the repo is a runnable uni-app x project:
- Covers navigation (push/replace/relaunch/back), guards, detail page, params, RouterLink, tabBar, 404
- `pages/test` is a self-check page (full PASS for regression)

## Next Steps

- [Navigation](./navigation) — the four navigation modes and passing params
- [Route Guards](./guards) — the guard system in detail
- [Plugin System](./plugins) — ParamsPlugin / InterceptorPlugin / custom plugins
- [Composables](./composables) — useRouter / useRoute / useLink