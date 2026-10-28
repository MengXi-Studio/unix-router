[中文](./README.md) | **English**

<div align="center">
  <a href="https://github.com/MengXi-Studio/unix-router">
    <img alt="MengXi Studio Logo" width="215" src="https://github.com/MengXi-Studio/unix-router/blob/master/packages/docs/src/public/logo.png">
  </a>
  <a href="https://github.com/MengXi-Studio/unix-router">
    <img alt="WeChat Official Account QR Code" width="215" src="https://github.com/MengXi-Studio/unix-router/blob/master/packages/docs/src/public/QR_code.jpg">
  </a>

  <h1>@meng-xi/unix-router</h1>
  <p>A vue-router-style routing system for uni-app x (written in UTS, dual-mode compatible)</p>

[![license](https://img.shields.io/github/license/MengXi-Studio/unix-router.svg)](./LICENSE) [![npm](https://img.shields.io/npm/v/@meng-xi/unix-router?color=blue)](https://www.npmjs.com/package/@meng-xi/unix-router)
![npm](https://img.shields.io/npm/dt/@meng-xi/unix-router?color=green)
</div>

## Why unix-router

Hand-rolling routing in uni-app x means dealing with all of this: direct `uni.navigateTo` calls that bypass your business rules, manually distinguishing tab pages from regular pages for every navigation API, complex params squeezed into URL strings, hand-built `uni.$on` channels for cross-page callbacks — plus edge cases like H5 refresh, the back button and deeplink cold starts. unix-router converges all of it into a router with a vue-router mindset:

- **Familiar with vue-router?** `createRouter` / `push` / `back`, global and in-component guards, and the `useRouter` / `useRoute` composables carry over directly — zero mental remapping, pick it up at a glance;
- **Multi-platform release?** One UTS codebase compiles to App (Android / iOS / HarmonyOS), Web and mini-programs; the page stack is the history, `meta.isTab` auto-switches to `switchTab`, and cross-platform quirks (null semantics, UTS strict typing) are handled deterministically inside the library;
- **Care about business rules?** The guard chain intercepts every navigation: `beforeEach` for auth redirects, and with `interceptUniApi` enabled even direct `uni.navigateTo` calls go through the guards — no bypass lane for your business rules.

The library depends only on the native `uni.*` navigation APIs and the `getCurrentPages()` page stack — no custom routing stack. UTS source is distributed directly (Web / mini-programs → JS, Android → Kotlin, iOS → Swift) and compiled on the fly by the uni-app x toolchain.

## Features

- **vue-router-style API** - `createRouter`, `push` / `replace` / `relaunch` / `back`, auto-switches to `switchTab` based on `meta.isTab`, duplicate navigation rejected (`DUPLICATED`), concurrent navigations auto-queued; back falls back to the entry page automatically when the page stack is insufficient (cold start / H5 refresh)
- **Complete guard system** - global `beforeEach` / `beforeResolve` / `afterEach`, route-exclusive `beforeEnter`, in-component `onBeforeRouteLeave` / `onBeforeRouteUpdate` / `onBeforeRouteEnter`; controllable redirects, guard timeout protection (`guardTimeout`), and `guardRoute()` to re-run guards on cold starts (H5 deep links / deeplinks)
- **File-based routing (build-time opt-in)** - import the vite plugin from `@meng-xi/unix-router/vite-plugin`, declare a `defineUniPage` macro (or a `<route-config>` block) next to your pages, and `pages.json` plus the route table (`routes.gen.uts`) are generated at build time — no more duplicated manual maintenance of path / title / isTab; the three independent plugins (`routeGen` / `pagesGen` / `routesGen`) can be registered alone or in combination
- **Cross-page data passing** - `params` (`Map<string,string>`) travel via **ParamsPlugin** (keyed store, plaintext keys never exposed) with optional persistence to storage; `events` via **EventsPlugin** mirroring the official event-channel semantics; `query` is a `Map` with `queryInt()` / `queryNumber()` / `queryBool()` convenience helpers
- **uni API interception (opt-in)** - with `interceptUniApi: true`, native navigations that bypass the router (direct `uni.navigateTo` / `switchTab` calls) are also intercepted and routed through the guard chain, sinking business rules down to the uni API layer
- **Navigation window animation (opt-in)** - **AnimationPlugin**: App passes native `animationType` / `animationDuration` through, H5 plays enter / exit animations with WAAPI; supports a global default (`animation` option) and per-navigation overrides
- **Ready-made companions** - `useLink()` and the **`RouterLink`** component, automatic state sync on `onShow` (reactive `currentRoute`, aligned with non-router navigation), the `RouterError` / `isNavigationFailure()` error system, named routes with strict mode

## Installation

```bash
pnpm add @meng-xi/unix-router
```

> Distributed as UTS source (Web / mini-programs → JS, Android → Kotlin, iOS → Swift), compiled on the fly by the uni-app x build chain. For App native, distributing via `uni_modules/<name>/utssdk` is recommended.

## Quick Start

### 1. Create and Mount the Router

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

### 2. Navigation, Guards & Reactive Route

```typescript
import { useRouter, useRoute } from '@meng-xi/unix-router'

const router = useRouter()

// Four navigation modes; each resolves to the target route location (NavigationResult = RouteLocation)
await router.push({ name: 'about', query: new Map([['a', '1']]) })
await router.replace({ path: '/pages/detail/detail', params: new Map([['from', 'Home']]) })
await router.relaunch('/pages/index/index')
await router.back(1)  // back one page; falls back to the first tabBar page when the stack is insufficient

// Global guard: unauthenticated requireAuth → redirect to login
router.beforeEach((to, from) => {
  if (to.meta.requireAuth && !isLoggedIn()) {
    return { name: 'login' } // redirect
  }
  return true // null / true means continue
})

// Reactive current route; fields accessible directly in script/template
const route = useRoute()
route.path            // /pages/about/about
route.query.get('id') // '1'
```

> **Note**: `back(delta)` requires an explicit argument (e.g. `back(1)`). UTS interface method signatures do not support default values — on App (Kotlin) builds, optional parameters compile into required ones, and omitting the argument causes a `No value passed for parameter 'delta'` compile error.

> If the target route is declared as a TabBar page via `meta.isTab`, `push` / `replace` / `relaunch` automatically use `uni.switchTab`.

## Router Options

Common options of `createRouter`:

| Option         | Type            | Default  | Description                                                    |
| -------------- | --------------- | -------- | -------------------------------------------------------------- |
| `routes`       | `RouteConfig[]` | -        | Route config; must match the `pages.json` declarations         |
| `strict`       | `boolean`       | `true`   | Strict mode; an unmatched named route throws `RouterError`     |
| `guardTimeout` | `number`        | `10000`  | Guard timeout (ms); warns and aborts navigation, `0` disables  |
| `readyTimeout` | `number`        | `0`      | Ready timeout (ms); prevents `await router.isReady()` hanging  |
| `plugins`      | `RouterPlugin[]`| -        | Optional plugins, **pass instances**, e.g. `[new ParamsPlugin()]` / `[new InterceptorPlugin()]` / `[new AnimationPlugin()]` / `[new EventsPlugin()]` |
| `paramsPersistent` | `boolean`  | `false`  | Whether to persist `params` to storage by default (requires `ParamsPlugin`) |
| `interceptUniApi` | `boolean`   | `false`  | Opt-in: intercept `uni.*` native navigation so direct calls also pass through the guard chain (runtime support: Web 4.0 / WeChat 4.41 / Android 3.97 / iOS 4.11 / Harmony 4.61) |
| `animation`    | `NavigationAnimation` | - | Global default navigation animation `{ type, duration }` (requires `AnimationPlugin` to take effect) |

```typescript
// With this enabled, business code that directly calls uni.navigateTo('/pages/xxx') also triggers the guard chain
const router = createRouter({ routes, interceptUniApi: true })

// Optional: register plugins + a global default animation
const router2 = createRouter({
  routes,
  plugins: [new ParamsPlugin(), new InterceptorPlugin(), new AnimationPlugin()],
  animation: { type: 'slide-in-right', duration: 300 }
})
```

## Try the Demo

Example project [packages/playground](https://github.com/MengXi-Studio/unix-router/tree/master/packages/playground) (covering navigation, guards, params passing, event communication, RouterLink, navigation animations, and more).

**On a real Android device**: [download the trial App (APK)](https://mp-b8b8347a-48e9-434d-8302-3e9d99c2cb01.cdn.bspapp.com/cloudstorage/app-build-pkg/1790413072682-__UNI__B6A50A8_1790413055611.apk) — a cloud-packaged build with real native rendering; the package is rebuilt with each release, and the link follows the latest release notes.

## Community & Support

Want to learn about the design ideas and implementation details (page-stack-as-history routing, building a router under UTS strict typing, file-based route generation, etc.)? The WeChat Official Account "**梦曦工作室** (MengXi Studio)" publishes a series of deep dives — scan to follow:

<p align="center">
  <img alt="WeChat Official Account QR Code" width="180" src="https://github.com/MengXi-Studio/unix-router/blob/master/packages/docs/src/public/QR_code.jpg">
</p>

- 🐛 [Issues](https://github.com/MengXi-Studio/unix-router/issues) / 💬 [Discussions](https://github.com/MengXi-Studio/unix-router/discussions)

## Documentation

📖 Complete docs from **beginner to master** (🟢 Beginner → 🟡 Intermediate → 🔴 Master, bilingual):

**[https://mengxi-studio.github.io/unix-router/](https://mengxi-studio.github.io/unix-router/)**

Reading guide (English docs): start with the [Introduction](https://mengxi-studio.github.io/unix-router/en/guide/introduction.html), then follow [Quick Start](https://mengxi-studio.github.io/unix-router/en/guide/getting-started.html) → [Route Configuration](https://mengxi-studio.github.io/unix-router/en/guide/route-config.html) → [Navigation](https://mengxi-studio.github.io/unix-router/en/guide/navigation.html); go deeper with [Guards](https://mengxi-studio.github.io/unix-router/en/guide/guards.html), and finish with [Recipes](https://mengxi-studio.github.io/unix-router/en/guide/recipes.html).

## Changelog

📝 **[https://github.com/MengXi-Studio/unix-router/blob/master/packages/docs/src/changelog.md](https://github.com/MengXi-Studio/unix-router/blob/master/packages/docs/src/changelog.md)**

## License

[MIT](./LICENSE)
