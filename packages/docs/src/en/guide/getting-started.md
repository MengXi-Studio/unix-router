# Getting Started

Get unix-router up and running in 5 steps.

## 1. Define the Route Config

Create a `router.config.ts` (the paths must match the registration in `pages.json`):

```ts
import type { RouteConfig } from '@meng-xi/unix-router'

export const routes: RouteConfig[] = [
	{ path: 'pages/index/index', name: 'home', meta: { title: 'Home', isTab: true } },
	{ path: 'pages/about/about', name: 'about', meta: { title: 'About' } },
	{ path: 'pages/guards/guards', name: 'guards', meta: { title: 'Guards', requireAuth: true } }
]
```

## 2. Create the Router

```ts
import { createRouter } from '@meng-xi/unix-router'
import { routes } from './router.config'

export const router = createRouter({ routes, strict: true })
```

## 3. Install into the Vue App

```ts
// main.ts
import { createSSRApp } from 'vue'
import App from './App.uvue'
import { router } from './router'

export function createApp() {
	const app = createSSRApp(App)
	app.use(router) // install: provide + $router/$route + global mixin
	return { app }
}
```

## 4. Use It in a Page

```vue
<script setup lang="uts">
import { useRouter, useRoute } from '@meng-xi/unix-router'

const router = useRouter()
const route = useRoute() // reactive current route

const go = () => {
	router.push({ name: 'about', query: new Map([['from', 'home']]) })
}
</script>
```

## 5. Add Guards (Optional)

```ts
router.beforeEach((to, from) => {
	if (to.meta.requireAuth && !isLoggedIn()) return { name: 'login' }
	return true
})
```

That's it. You can now call `router.push('/pages/about/about')` or use `<RouterLink to="...">` in any `.uvue` page.

## Complete Example

`packages/playground` in the repository is a runnable uni-app x test project covering navigation, guards, parameters, error handling, and the `pages/test` self-check page. You can use it as a reference template.