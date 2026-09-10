# Differences from vue-router

unix-router aligns with vue-router 4 in API style, but because uni-app x uses a **static pages.json page model**, there are fundamental differences between the two. This page explains which capabilities are **implemented equivalently** and which are **not supported**.

## Capability Comparison

| vue-router 4 | unix-router (uni-app x) |
| --- | --- |
| `createRouter({ history })` | `createRouter({ routes })`, no `history` needed (the native page stack handles it) |
| path / named / params / query matching | ✅ Supported |
| `route.params / query / meta / fullPath` | ✅ Supported (params are passed across pages by ParamsPlugin via the `__params__` in-memory channel) |
| `currentRoute` reactive | ✅ Supported |
| push / replace / back | ✅ Supported (+ `relaunch`) |
| `router.go(n)` | ⚠️ Limited semantics: degraded to `back(delta)` |
| beforeEach / beforeResolve / afterEach | ✅ Supported |
| beforeEnter / onBeforeRouteLeave | ✅ Supported |
| `useRouter / useRoute / useLink` | ✅ Supported |
| Error system NavigationFailure | ✅ Supported |
| `isReady / onError` | ✅ Supported (+ `onRouteChange`) |
| **Dynamic routes** `addRoute / removeRoute` | ❌ Not supported (unregistered pages are ignored at compile time) |
| **Nested routes** `children` | ❌ Not supported (flat page model) |
| **Named views / RouterView** | ❌ Not supported (no in-page rendering placeholder) |
| **scrollBehavior** | ❌ Not supported (scrolling is managed natively by uni-app) |
| hash route mode | ❌ Not supported (no URL concept) |

## Key Differences Explained

### 1. No URL / history

uni-app x has no browser URL; "history" is handled by the native page stack (`getCurrentPages`). Therefore:
- There is no `createWebHistory` / `createWebHashHistory` / `createMemoryHistory`.
- There are no hash params; scroll behavior is managed natively.

### 2. Static Route Table

Pages must first be registered in `pages.json`; at runtime, `addRoute` cannot compile a page into the bundle → dynamic routes are not supported. Subpackages (`subPackages`) can act as a resource-level "lazy load", but they are not vue-router's nested/dynamic semantics.

### 3. How params Are Implemented

The native platform doesn't support path parameters (the path is the page path). unix-router uses **ParamsPlugin** to pass `params` across pages: the source page stores them into a manager and generates an internal key, carried out through the in-memory `__params__` channel in the URL; the target page retrieves and rebuilds `route.params` during state sync. Therefore:
- params are shaped like `Map<string,string>` (string values).
- params are not directly exposed in the URL; to keep them across refresh, enable `paramsPersistent` (stored to storage).
- Using `params` without registering `ParamsPlugin` throws `PLUGIN_REQUIRED`. See [Plugin System](./plugins).

### 4. `go(n)`

vue-router's `go` relies on history-based relative navigation, but uni-app x has no such stack API → `back(delta)` is used instead.

### 5. In-Component Guards

uni-app x creates a new page instance for every navigation (no keep-alive), so `onBeforeRouteUpdate` rarely triggers, `onBeforeRouteEnter` has limited effect, and `onBeforeRouteLeave` is the most commonly used.

## Migration Mindset

If you're coming from vue-router, most of your code migrates directly: `createRouter`, guards, `useRouter/useRoute/useLink`, and `push/replace/back` are all used the same way; you only need to move the `components` config to `pages.json` registration and drop dynamic routes / nesting / named views.