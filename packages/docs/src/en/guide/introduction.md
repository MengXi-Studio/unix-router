# Introduction

`@meng-xi/unix-router` is a routing management library for **uni-app x**. Its API style mirrors **vue-router 4**, but it is built on uni-app x's **static page model** (`.uvue` pages + `pages.json` registration + `uni.*` native navigation) and written in **UTS** (`.uts`).

## Why You Need It

uni-app x natively provides `uni.navigateTo / redirectTo / reLaunch / navigateBack / switchTab`. As pages grow, using these APIs directly has obvious pain points:

- **No route table**: page paths are scattered everywhere with no centralized management, and there are no named routes or type hints.
- **No guards**: you cannot uniformly run login checks, analytics, and other logic before a navigation.
- **No route state**: there is no unified reactive `currentRoute`, making it hard to track the current page in the Composition API.
- **Cumbersome parameter passing**: object-shaped parameters are hard to pass across pages, and query parsing has to be handwritten.

unix-router smooths over these differences with an API set consistent with vue-router.

## Who Is It For

- **Migrating from uni-app (Vue 3) to uni-app x**: transition quickly with the routing style you already know.
- **Projects with many pages that need centralized navigation and auth**: a route table + guards make every navigation clear and controllable.
- **Teams that want consistent routing behavior across web / Mini Program / App**: one API set covers all platforms.

## How This Guide Takes You from Beginner to Mastery

The docs are organized progressively. We recommend reading them in order:

| Stage | Topics | What you'll master |
| --- | --- | --- |
| 🟢 **Getting Started** | [Introduction](./introduction) → [Installation](./installation) → [Getting Started](./getting-started) | Run your first navigation from scratch — install the package, instantiate and register plugins, create the router, navigate |
| 🟢 **Core Features** | [Route Configuration](./route-config) → [File-Based Routing](./file-based-routing) → [Navigation](./navigation) → [Composables](./composables) | The route table and named routes, declaring pages near the code with both configs generated automatically, the four navigation modes and TabBar detection, useRouter / useRoute / useLink |
| 🟡 **Advanced** | [Route Meta](./meta) → [Route Guards](./guards) → [Error Handling](./error-handling) | meta-driven page behavior, the full guard chain with login auth, failure detection and global capture |
| 🟠 **Plugin System** | [Plugins (Overview)](./plugins) → [Passing Params](./params) → [Page Communication](./events) → [Navigation Animations](./animation) → [uni API Interception](./interceptor) | Deep dives into the 4 built-in plugins (Params / Events / Animation / Interceptor) + the 8 PluginContext hooks, and how to write your own plugin |
| 🔴 **Mastery** | [Navigation Flow](./navigation-flow) → [Recipes](./recipes) → [Platform Compatibility](./compatibility) → [Differences from vue-router](./differences) → [FAQ](./faq) | Understand the internals of a navigation, build a complete login + TabBar + detail app, resolve platform differences and common pitfalls |

> **Tip**: [`packages/playground`](https://github.com/MengXi-Studio/unix-router/tree/master/packages/playground) in the repository is a runnable uni-app x project that includes a `pages/test` self-check page (programmatic PASS/FAIL output). Use it to verify what you read alongside the docs.

## Capabilities at a Glance

| Capability | Description |
| --- | --- |
| Route matching | `path` / `name` dual index; string / object / named resolution; with `strict` mode on, unmatched named routes throw `ROUTE_NOT_FOUND` |
| Navigation | push / replace / relaunch / back; `meta.isTab` automatically uses `switchTab`; concurrent navigations are queued and executed serially; only push detects duplicates (`DUPLICATED`) |
| Guards | beforeEach / beforeEnter (route-local) / beforeResolve / afterEach + in-component onBeforeRouteLeave / Update / Enter; return-value-style redirects with a depth limit of 10; `guardTimeout` timeout protection |
| Cold-start guards | `guardRoute(location?, { onAbort? })` re-runs the global `beforeEach` only for deep-linked entry pages (no actual navigation; a redirect defaults to `relaunch` to perform the real jump) |
| Params & query | query goes straight into the URL; params travel via ParamsPlugin (the `__params__` internal key channel, with optional `paramsPersistent` persistence); `queryInt` / `queryNumber` / `queryBool` for typed reads |
| Composition API | useRouter / useRoute / useLink / useOpenerEventChannel, plus the three in-component guards (onBeforeRouteLeave / Update / Enter) |
| Page event communication | EventsPlugin — pass an events listener map in push to create the channel; the opened page uses `useOpenerEventChannel` to emit back / receive; also exports a global `eventBus` |
| Navigation animations | AnimationPlugin — passes through native `animationType` / `animationDuration` on App (officially App-only), implements H5 with the Web Animations API, and back automatically maps to exit-style animations |
| uni API interception | InterceptorPlugin + `interceptUniApi` — external direct calls like `uni.navigateTo` also go through the guard chain (based on `uni.addInterceptor`, with automatic degradation when it is missing at runtime) |
| State sync | `syncRoute()` syncs `currentRoute` from the page stack; on H5, `install` automatically registers the onShow global mixin; `onRouteChange` listens for route changes |
| Error system | RouterError / NavigationFailure with 7 error codes (ABORTED / CANCELLED / DUPLICATED / ROUTE_NOT_FOUND / NAVIGATION_API_ERROR / SETUP_ERROR / PLUGIN_REQUIRED); navigation failures always reject; onError + isNavigationFailure |
| Plugin system | The RouterPlugin abstract class + PluginContext with 8 hooks; 4 built-in plugins — ParamsPlugin / EventsPlugin / AnimationPlugin / InterceptorPlugin, registered as instances |

> Install with `npm install @meng-xi/unix-router`

## Next Steps

- Want to get hands-on right away? Start with [Installation](./installation) or [Getting Started](./getting-started).
- Want to understand the design trade-offs? See [Differences from vue-router](./differences).
