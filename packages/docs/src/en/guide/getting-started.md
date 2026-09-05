# Quick Start

This page takes you from **creating a project to completing your first cross-page navigation**, with copy-paste code you can run and verify.

> Want to see it working without the setup? Use the [`packages/playground`](https://github.com/MengXi-Studio/unix-router/tree/master/packages/playground) — a pre-configured uni-app x project whose home page is the router entry and whose `pages/test` is the self-check page.

## 0. Prerequisites

- [HBuilderX](https://www.dcloud.io/hbuilderx.html) (uni-app x edition)
- A uni-app x project (pick the **uni-app x** template when creating it)
- `@meng-xi/unix-router` installed ([installation](./installation))

## 1. Write the Route Config

Create `router.config.ts`. **`path` must exactly match the page paths registered in `pages.json`** (in this project, `pages/index/index`, etc.):

```ts
import type { RouteConfig } from '@meng-xi/unix-router'

export const routes: RouteConfig[] = [
	{ path: 'pages/index/index', name: 'home', meta: { title: 'Home', isTab: true } },
	{ path: 'pages/about/about', name: 'about', meta: { title: 'About' } }
]
```

- `path`: the page path (no leading `/` needed), corresponding to `pages.json`
- `name`: named route, navigable by name (recommended; decouples callers from the path)
- `meta`: arbitrary metadata (e.g. `title`, `isTab`) read by guards / pages

## 2. Create the Router

Create `router.ts`, plus a simple guard for demonstration:

```ts
import { createRouter } from '@meng-xi/unix-router'
import { routes } from './router.config'

export const router = createRouter({ routes, strict: true })

// Optional: log every navigation
router.afterEach((to, from) => {
	console.log(`[unix-router] ${from.fullPath} -> ${to.fullPath}`)
})
```

`strict: true` means navigating with an unregistered `name` **throws immediately** (handy for catching typos early). See [Route Configuration](./route-config).

## 3. Install into the Vue App

In `main.ts`, `app.use(router)`. It provides the router, mounts `$router/$route`, and injects a global mixin — every page `onShow` calls `syncRoute()` so `currentRoute` always reflects the real page:

```ts
import { createSSRApp } from 'vue'
import App from './App.uvue'
import { router } from './router'

export function createApp() {
	const app = createSSRApp(App)
	app.use(router)
	return { app }
}
```

## 4. Put a Navigation Button on the Home Page

In `pages/index/index.uvue`, navigate by `name`:

```vue
<template>
	<view class="page">
		<text class="title">Home</text>
		<button @tap="goAbout">Go to About</button>
	</view>
</template>

<script setup lang="uts">
import { useRouter } from '@meng-xi/unix-router'

const router = useRouter()

function goAbout(): void {
	// navigate by name and carry query (Map form)
	router.push({ name: 'about', query: new Map([['from', 'home']]) })
}
</script>
```

## 5. Read the Route on the About Page

In `pages/about/about.uvue`, use the reactive `currentRoute`:

```vue
<template>
	<view class="page">
		<text class="title">About</text>
		<text>From: {{ from }}</text>
		<text>Current path: {{ currentPath }}</text>
	</view>
</template>

<script setup lang="uts">
import { useRoute } from '@meng-xi/unix-router'

const route = useRoute()
// fields are accessible directly in script (route is a reactive object)
const currentPath = ref(route.path)
const from = ref(route.query.get('from') ?? '')
</script>
```

> Remember to register `pages/about/about` in `pages.json`, otherwise the target page will not be compiled into the bundle.

## 6. Run and Verify

1. Tap the home button → you should navigate to the About page, with "From: home" shown.
2. The console shows `[unix-router] /pages/index/index -> /pages/about/about` (your `afterEach` log).
3. If nothing happens / white screen, check: does the path exactly match `pages.json`? Is the page registered? Is there an error in `onLoad/setup`? (See [FAQ](./faq).)

## Done ✅

You've completed the first flow: **route table → create router → install → navigate by name → read the reactive route**.

## What's Next

- The four navigation modes (`push / replace / relaunch / back`) and [parameter passing](./navigation)
- [Composables](./composables): `useRouter` / `useRoute` / `useLink`
- [Route Guards](./guards): login auth, `beforeEnter`, cold-start re-run
- Want a full project? Jump to [Recipes](./recipes)

> Note: `pages/about/about` is illustrative; adjust it to your actual pages in `pages.json`.