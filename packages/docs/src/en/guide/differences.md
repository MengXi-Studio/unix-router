# Differences from vue-router

unix-router aligns with vue-router 4 in API style, but because uni-app x uses a **static pages.json page model**, there are fundamental differences between the two. This page explains which capabilities are **implemented equivalently**, which are **not supported**, and which **behave differently**.

## Capability Comparison

| vue-router 4 | unix-router (uni-app x) |
| --- | --- |
| `createRouter({ history })` | `createRouter({ routes })` — history is handled by the native page stack (`getCurrentPages`); there is no HTML5 history |
| Path / named matching | ✅ Supported (no nested or dynamic segments, see below) |
| `route.query` | ✅ `Map<string, string>`, goes straight into the URL |
| `route.params` | ✅ `Map<string, string>`, requires ParamsPlugin (the `__params__` internal key channel); not path params |
| `route.meta` / `fullPath` | ✅ Supported |
| `route.hash` | Always `''` (there is no URL hash concept) |
| `currentRoute` reactive | ✅ Supported |
| push / replace / back | ✅ Supported (+ `relaunch`; `meta.isTab` automatically uses `switchTab`) |
| `go(n)` | ⚠️ Limited semantics — use `back(delta)` instead |
| Guard `next()` callback | ❌ Replaced by the **return-value style** — `null` / `true` passes, `false` aborts, `Error` cancels, a location object redirects |
| Duplicate navigation `resolve(false)` | ❌ Replaced by a **reject** with `NavigationFailure` (`DUPLICATED`, detected by push only) |
| Concurrent navigations (the latter cancels the former) | ❌ Replaced by **automatic queueing and serial execution** |
| beforeEach / beforeResolve / afterEach / beforeEnter | ✅ Supported |
| onBeforeRouteLeave / Update / Enter | ✅ Supported, but there is no keep-alive → Leave is the most reliable (see below) |
| `useRouter / useRoute / useLink` | ✅ Supported (+ `useOpenerEventChannel`) |
| `isReady / onError` | ✅ Supported (+ `onRouteChange`) |
| NavigationFailure error system | ✅ Supported (7 error codes) |
| **Nested routes** `children` | ❌ Not supported (flat page model) |
| **Dynamic routes** `addRoute / removeRoute` | ❌ Not supported (pages must be registered in pages.json at compile time) |
| **Dynamic route segments** `/user/:id` | ❌ Not supported (the path is the page path; pass data via query / params) |
| **Named views / RouterView** | ❌ Not supported (no in-page render outlet) |
| **scrollBehavior** | ❌ Not supported (scrolling is managed natively by uni-app) |
| Hash history mode | ❌ Not supported (`route.hash` is always `''`) |
| `app.use(router)` provide / mixin injection | ⚠️ Registered on **H5 only** (`$router` / `$route` provide + the onShow global mixin for auto sync); none of it is registered on native platforms, where `useRouter` falls back to the global active router |

## Key Differences Explained

### 1. Flat Page Model — No Nesting, No Dynamic Segments

Pages must be registered in `pages.json` first, and `path` is the real page path (e.g. `pages/detail/detail`). Therefore:

- No `children` nesting and no named views — pages are siblings in a flat stack, and the "nesting" semantics are naturally carried by the page stack.
- No `/user/:id` dynamic segments — pass data via query (goes into the URL) or params (the ParamsPlugin channel).
- No `addRoute / removeRoute` — paths added at runtime cannot be compiled into the bundle. Subpackages (`subPackages`) can serve as resource-level "lazy loading", but they are not vue-router's nested/dynamic semantics.

### 2. No URL / history

uni-app x has no browser URL; "history" is handled by the native page stack (`getCurrentPages`):

- There is no `createWebHistory / createWebHashHistory / createMemoryHistory`.
- `route.hash` is always `''`; `fullPath` is composed of path + query.
- There is no `scrollBehavior`; scrolling behavior is managed natively.
- Going back pops the stack — `back(delta)` is based on the page stack depth. A delta that is not a positive integer rejects with `ABORTED`, and insufficient stack depth rejects with `CANCELLED`.

### 3. query / params Are Both Map&lt;string, string&gt;

In vue-router, params come from dynamic path segments; in unix-router, query and params are unified as `Map<string, string>` (string values):

- `query` is encoded directly into the URL.
- `params` travel across pages via ParamsPlugin through the internal `__params__` key channel and **do not appear in the user-visible query**; the target page retrieves and rebuilds `route.params` during state sync.
- params require **ParamsPlugin** to be registered (`plugins: [new ParamsPlugin()]`); using them without registration rejects with `PLUGIN_REQUIRED`.
- To keep params across refreshes, enable `paramsPersistent` (persisted to uni storage, falling back to memory on failure).

### 4. Guards — Return-Value Style, No next()

vue-router guards decide the outcome through the `next()` callback; unix-router guards **return the result directly**:

- `null` / `true` — pass; `false` — abort (ABORTED); `Error` — cancel (CANCELLED).
- Return a `string` / location object to redirect; return `{ location, mode? }` to specify `push` / `replace` / `relaunch` (by default the original navigation mode is reused).
- Execution chain — beforeEach → beforeEnter (route-local) → beforeResolve → navigation → afterEach(to, from, failure | null).
- Async guards are protected by `guardTimeout` (default 10000ms, 0 disables it); on timeout the router warns and aborts the navigation.

### 5. Duplicate Navigations Reject; Concurrent Navigations Queue

- **Duplicate navigation**: vue-router resolves duplicates with `resolve(false)`; unix-router detects duplicates on **push only** and **rejects** with `NavigationFailure` (`DUPLICATED`). Catch it with `isNavigationFailure(err, RouterErrorCode.DUPLICATED)` — do not rely on a "silent success".
- **Concurrent navigations**: in vue-router, a newer navigation cancels the previous one; unix-router **queues concurrent navigations automatically and executes them serially** — none cancels another.
- **Redirect loop protection**: guard redirects have a depth limit of 10 (`MAX_REDIRECT_DEPTH`); exceeding it returns `CANCELLED`.
- **Result confirmation**: after a navigation is initiated, the router polls the page stack top to confirm success (within 500ms); on failure it rejects with `NAVIGATION_API_ERROR`.

### 6. Limits of In-Component Guards

uni-app x creates a new page instance for every navigation and has **no keep-alive**:

- `onBeforeRouteLeave` — the most reliable one; fires before the page leaves (including back).
- `onBeforeRouteUpdate` — the same component is almost never reused, so this rarely fires.
- `onBeforeRouteEnter` — the component instance does not exist yet, so you cannot access component state; its effect is limited.
- All three are implemented as filters over beforeResolve; the registration functions return **nothing** and the guards **cannot be unregistered** (unlike vue-router, where the registration function returns a cancel function).

### 7. Named Route Type Hints Differ by Platform

- **Web**: `RouteName` is `keyof RouteNameMap & string`, and you can get literal hints through module augmentation:

```ts
// Only affects TS / editor hints
declare module '@meng-xi/unix-router' {
	interface RouteNameMap {
		home: 'home'
		about: 'about'
	}
}
```

- **App / Mini Program**: UTS does not support `keyof` composite types, so `RouteName` degrades to `string`. With `strict` enabled, unregistered named routes throw `ROUTE_NOT_FOUND` at runtime as a safety net (`strict: false` warns and treats the name as a path).

### 8. Platform Differences of app.use(router)

- **H5**: `install` registers the `$router` / `$route` provide and global properties, and registers a global mixin that automatically calls `syncRoute()` in `onShow`.
- **Native platforms**: none of the above is registered; the router is only set as the global active router (used by the `useRouter()` fallback). It is recommended to call `router.syncRoute()` yourself in each page's `onShow`.

## Migration Mindset

If you are coming from vue-router, most of your code migrates directly — `createRouter`, guards (converted to the return-value style), `useRouter/useRoute/useLink`, and `push/replace/back` are used the same way. You only need to move the `components` config to `pages.json` registration, drop dynamic routes / nesting / named views / scrollBehavior, pass params via ParamsPlugin instead, catch duplicate navigations via reject, and expect `CANCELLED` when the stack is too shallow to go back.

## Next Steps

- [Route Configuration](./route-config) — unix-router's route table and named routes
- [FAQ](./faq) — troubleshooting common pitfalls
- [Platform Compatibility](./compatibility) — capabilities and version requirements per platform
