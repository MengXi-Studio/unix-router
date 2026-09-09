[中文](./README.md) | **English**

<div align="center">
  <a href="https://github.com/MengXi-Studio/unix-router">
    <img alt="MengXi Studio Logo" width="215" src="https://github.com/MengXi-Studio/unix-router/blob/master/packages/docs/src/public/logo.png">
  </a>
  <a href="https://github.com/MengXi-Studio/unix-router">
    <img alt="WeChat Official Account QR Code" width="215" src="https://github.com/MengXi-Studio/unix-router/blob/master/packages/docs/src/public/QR_code.jpg">
  </a>
  <br>
  <h1>@meng-xi/unix-router</h1>
  <p>A vue-router-style routing system for uni-app x (written in UTS, dual-mode compatible)</p>

[![license](https://img.shields.io/github/license/MengXi-Studio/unix-router.svg)](LICENSE) [![npm](https://img.shields.io/npm/v/@meng-xi/unix-router?color=blue)](https://www.npmjs.com/package/@meng-xi/unix-router)
![npm](https://img.shields.io/npm/dt/@meng-xi/unix-router?color=green)

</div>

## Features

- **vue-router-style API** - `createRouter`, `push` / `replace` / `relaunch` / `back`, auto-switches to `switchTab` based on `meta.isTab`, duplicate navigation rejected (`DUPLICATED`), concurrent navigations auto-queued
- **Route guards** - `beforeEach` / `beforeResolve` / `afterEach` / `beforeEnter` / `onBeforeRouteLeave` / `onBeforeRouteEnter` / `onBeforeRouteUpdate`, controllable redirects, guard timeout protection (`guardTimeout`), and redirect depth limit
- **Cold-start guard** - `guardRoute()` re-runs the guard chain for H5 deep links / scene values / deeplink pages, with redirect and abort callbacks (`onAbort`)
- **Named routes & route meta** - navigate by `name`, carry custom data in `meta` (including `isTab`); under strict mode (`strict`) an unmatched named route throws `RouterError`
- **Page parameter passing** - `params` (`Map<string,string>`) passed across pages via query encoding (reserved `__unixr_p_` prefix); readable from `route.params` on the target page without exposing plaintext keys
- **Enhanced query parsing** - `route.query` is a `Map`, plus `queryInt()` / `queryNumber()` / `queryBool()` convenience helpers (provided by the library's built-in utilities)
- **Declarative navigation** - `useLink()` composable returning a reactive target route, active state (`isActive` / `isExactActive`), and a navigate function, for building custom link / menu components
- **Automatic route state sync** - `app.use(router)` injects a global mixin that calls `syncRoute()` on page `onShow`, keeping the reactive `currentRoute` aligned with non-router navigation (back button / TabBar switches)
- **Error handling** - `RouterError` / `NavigationFailure` / `UniNavigationApiError`, `RouterErrorCode` codes, `isNavigationFailure()` precise checks, `onError` global capture
- **Composition API** - `useRouter()` / `useRoute()` / `useLink()` / `onBeforeRouteLeave()`, reactive `currentRoute`, `isReady` / `onRouteChange` subscriptions
- **uni API interception (opt-in)** - with `interceptUniApi: true`, native navigations that bypass the router (direct `uni.navigateTo` / `switchTab` calls) are also intercepted and routed through the guard chain, sinking guards down to the uni API layer

## Installation

```bash
pnpm add @meng-xi/unix-router
```

> `packages/core` is distributed as **UTS source**. Web / Mini Program → JS, Android → Kotlin, iOS → Swift, compiled on the fly by the uni-app x build chain. For App native, distributing via `uni_modules/<name>/utssdk` is recommended.

## Quick Start

### 1. Create a Router

```typescript
// src/router.ts
import { createRouter } from '@meng-xi/unix-router'
import type { RouteConfig } from '@meng-xi/unix-router'

export const routes: RouteConfig[] = [
  { path: 'pages/index/index', name: 'home', meta: { title: 'Home', isTab: true } },
  { path: 'pages/about/about', name: 'about', meta: { title: 'About', requireAuth: true } }
]

export const router = createRouter({ routes, strict: true })

// main.ts
import { createSSRApp } from 'vue'
import App from './App.uvue'
import { router } from './router'

export function createApp() {
  const app = createSSRApp(App)
  app.use(router) // injects global mixin; calls syncRoute() on onShow
  return { app }
}
```

### 2. Route Navigation

```typescript
const router = useRouter()

// Resolves to the target route location (NavigationResult = RouteLocation)
await router.push({ name: 'about', query: new Map([['a', '1']]) })
await router.push('/pages/detail/detail')
await router.replace({ path: '/pages/detail/detail', params: new Map([['from', 'Home']]) })
await router.relaunch('/pages/index/index')
await router.back()      // back one step
await router.back(2)     // back two steps
```

> If the target route is declared as a TabBar page via `meta.isTab`, `push` / `replace` / `relaunch` automatically use `uni.switchTab`.

### 3. Route Guards

```typescript
router.beforeEach((to, from) => {
  if (to.meta.requireAuth && !isLoggedIn()) {
    return { name: 'login' } // redirect
  }
  return true // null / true means continue
})

// In-component leave guard
import { onBeforeRouteLeave } from '@meng-xi/unix-router'

onBeforeRouteLeave((to, from) => {
  if (hasUnsavedChanges) {
    return false // abort navigation
  }
})

// Cold-start guard (guardRoute): re-run the guard chain for H5 deep links / scene values / deeplink pages
router.isReady().then(() => {
  router.guardRoute(undefined, {
    onAbort: () => router.relaunch('/pages/index/index')
  })
})
```

### 4. Declarative Navigation & Reactive Route

```typescript
import { useLink, useRoute } from '@meng-xi/unix-router'

// useLink: reactive state and trigger for declarative navigation
const link = useLink({ to: '/pages/about/about' })
link.isActive.value   // whether the link is currently active
await link.navigate() // trigger navigation

// useRoute: reactive current route; fields accessible directly in script/template
const route = useRoute()
route.path          // /pages/about/about
route.query.get('id') // '1'
```

## Router Options

Common options of `createRouter`:

| Option         | Type            | Default  | Description                                                    |
| -------------- | --------------- | -------- | -------------------------------------------------------------- |
| `routes`       | `RouteConfig[]` | -        | Route config; must match the `pages.json` declarations         |
| `strict`       | `boolean`       | `true`   | Strict mode; an unmatched named route throws `RouterError`     |
| `guardTimeout` | `number`        | `10000`  | Guard timeout (ms); warns and aborts navigation, `0` disables  |
| `readyTimeout` | `number`        | `0`      | Ready timeout (ms); prevents `await router.isReady()` hanging  |
| `interceptUniApi` | `boolean`   | `false`  | Opt-in: intercept `uni.*` native navigation so direct calls also pass through the guard chain (runtime support: Web 4.0 / WeChat 4.41 / Android 3.97 / iOS 4.11 / Harmony 4.61) |

```typescript
// With this enabled, business code that directly calls uni.navigateTo('/pages/xxx') also triggers the guard chain
const router = createRouter({ routes, interceptUniApi: true })
```

## Documentation

📖 Complete docs from **beginner to master** (🟢 Beginner → 🟡 Intermediate → 🔴 Master):

**[https://github.com/MengXi-Studio/unix-router/tree/master/packages/docs](https://github.com/MengXi-Studio/unix-router/tree/master/packages/docs)**

Reading guide: start with the [Introduction & learning path](https://github.com/MengXi-Studio/unix-router/blob/master/packages/docs/src/en/guide/introduction.md), then follow [Quick Start](https://github.com/MengXi-Studio/unix-router/blob/master/packages/docs/src/en/guide/getting-started.md) → [Route Configuration](https://github.com/MengXi-Studio/unix-router/blob/master/packages/docs/src/en/guide/route-config.md) → [Navigation](https://github.com/MengXi-Studio/unix-router/blob/master/packages/docs/src/en/guide/navigation.md); go deeper with [Guards](https://github.com/MengXi-Studio/unix-router/blob/master/packages/docs/src/en/guide/guards.md), and finish with [Recipes](https://github.com/MengXi-Studio/unix-router/blob/master/packages/docs/src/en/guide/recipes.md).

## Changelog

📝 **[https://github.com/MengXi-Studio/unix-router/blob/master/packages/docs/src/changelog.md](https://github.com/MengXi-Studio/unix-router/blob/master/packages/docs/src/changelog.md)**

## License

[MIT](LICENSE)
