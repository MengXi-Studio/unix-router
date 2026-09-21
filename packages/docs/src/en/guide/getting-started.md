# Quick Start

Walk through the core closed loop of unix-router with a runnable demo — define routes → create the router (with plugins) → install the app → navigate + guards.

## Prerequisites

- A uni-app x project (HBuilderX 4.0+ can run web / Mini Program; the minimum HBuilderX versions for `addInterceptor` per platform are listed in [Platform Compatibility](./compatibility))
- The latest stable HBuilderX is recommended

## 1. Install

```bash
npm install @meng-xi/unix-router
# or
pnpm add @meng-xi/unix-router
```

> In uni-app x you can also bring in the UTS source via `uni_modules` (this library ships as `.uts` sources, compiled on the fly per platform). See [Installation](./installation).

## 2. Define the Route Config

Paths must match the registration in `pages.json` (without a leading `/`):

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

Plugins are subclasses of the `RouterPlugin` abstract class and **must be instantiated** when registered:

```ts
// router/index.ts
import { createRouter, ParamsPlugin, InterceptorPlugin } from '@meng-xi/unix-router'
import { routes } from './routes'

export const router = createRouter({
	routes,
	strict: true, // unmatched named routes throw ROUTE_NOT_FOUND
	plugins: [new ParamsPlugin(), new InterceptorPlugin()], // page params + uni navigation interception
	interceptUniApi: true // InterceptorPlugin switch — external uni.navigateTo calls also run guards
})
```

::: warning The old style is deprecated
`plugins: [ParamsPlugin]` (passing the class directly) is no longer supported — plugins are abstract classes, so you must register an instance created with `new`.
:::

## 4. Install into the Vue App

```ts
// main.ts
import { createSSRApp } from 'vue'
import App from './App.uvue'
import { router } from './router'

export function createApp() {
	const app = createSSRApp(App)
	app.use(router) // on H5 this registers the global mixin (onShow auto-syncs via syncRoute); on App / Mini Program, call router.syncRoute() yourself in each page's onShow
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
	router.push({ name: 'about', query: new Map<string, string>([['from', 'home']]) })
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
Read it reactively with `useRoute()`: `route.path` / `route.query` / `route.params` / `route.meta`.
:::

## 6. Guards (permissions)

Guards use the **return-value style**: return `true` (or `null`) to pass; return a location object to redirect, and `{ location, mode }` lets you specify the navigation mode used for the redirect:

```ts
// router/index.ts
router.beforeEach((to, from) => {
	// not logged in visiting a protected page → redirect to login and record the origin
	if (to.meta.requireAuth == true && !isLoggedIn()) {
		return {
			location: { name: 'login', query: new Map<string, string>([['redirect', to.fullPath]]) },
			mode: 'replace'
		}
	}
	// logged in visiting the login page → go home
	if (to.name == 'login' && isLoggedIn()) {
		return { name: 'home' }
	}
	return true
})
```

## 7. Passing Params (ParamsPlugin)

Params travel across pages through the `__params__` internal key channel (they do not appear in the user-visible URL query), with values of type `Map<string, string>`:

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

::: tip
`params` requires `ParamsPlugin` to be registered (done in step 3 above); using it without registration rejects with `PLUGIN_REQUIRED`. See [Plugin System](./plugins) and [Passing Params](./params).
:::

## Complete Runnable Template

`packages/playground` in the repository is a runnable uni-app x project:
- Covers navigation (push/replace/relaunch/back), guards, the detail page, params, RouterLink, tabBar, and 404
- `pages/test` is a self-check page (a full PASS run is used for regression)

### Trial Android App

Don't want to set up a project yourself? Install the trial Android App directly (built from the example project; includes all examples — tabBar, navigation guards, params passing, RouterLink, navigation animations, and more):

[⬇️ Download the trial Android App (.apk)](https://mp-b8b8347a-48e9-434d-8302-3e9d99c2cb01.cdn.bspapp.com/cloudstorage/app-build-pkg/1789882378601-__UNI__B6A50A8_1789882365595.apk)

> Note: the trial build is mainly for previewing features quickly. For production integration, follow the steps above to install and use the library in your own uni-app x project.

## Next Steps

- [Route Configuration](./route-config) — RouteConfig / RouteMeta / named routes
- [Navigation](./navigation) — the four navigation modes and passing params
- [Passing Params](./params) — cross-page params and query parsing utilities
- [Composables](./composables) — useRouter / useRoute / useLink
