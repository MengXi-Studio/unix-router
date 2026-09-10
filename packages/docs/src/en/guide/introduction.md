# Introduction

`@meng-xi/unix-router` is a routing library for **uni-app x**, with an API style that mirrors **vue-router 4**. It is built on uni-app x's **static page model** (`.uvue` pages + `pages.json` registration + `uni.*` native navigation) and written in **UTS** (`.uts`).

## Why You Need It

uni-app x natively provides `uni.navigateTo / redirectTo / reLaunch / navigateBack / switchTab`. As pages grow, using these directly has obvious pain points:

- **No route table**: page paths are scattered with no centralized management and no named routes or type hints.
- **No guards**: you cannot run login checks or analytics before navigation.
- **No route state**: there is no unified reactive `currentRoute`, making it hard to track the current page in the Composition API.
- **Cumbersome parameter passing**: object parameters are hard to pass across pages, and query parsing has to be handwritten.

unix-router smooths over these differences with an API set consistent with vue-router.

## Who Is It For

- **Migrating from uni-app (Vue 3) to uni-app x**: get familiar quickly with a routing style you already know.
- **Pages have grown and need centralized navigation & auth**: a route table plus guards make the flow clear and controllable.
- **You want consistent routing behavior across web / Mini Program / App**: one API spans all platforms.

## How This Guide Takes You from Beginner to Master

The docs are organized progressively. Read them in order:

| Stage | Topics | What you'll master |
| --- | --- | --- |
| 🟢 **Beginner** | [Quick Start](./getting-started) → [Route Configuration](./route-config) → [Navigation](./navigation) | Run your first navigation from scratch; understand the route table, four navigation modes, and parameter passing |
| 🟢 **Beginner** | [Reading the route](../api/use-route) / [Composables](./composables) | `useRouter` / `useRoute` / `useLink` and their lifecycle interaction |
| 🟡 **Intermediate** | [Route Guards](./guards) → [Route Meta](./meta) | Login auth, `beforeEnter`, cold-start guards, dynamic titles |
| 🟡 **Intermediate** | [Error Handling](./error-handling) → [Navigation Flow](./navigation-flow) | Failure detection, global error capture, understanding the internals of a navigation |
| 🔴 **Master** | [Recipes](./recipes) → [Platform Compatibility](./compatibility) | Build a complete login + TabBar + detail app with one pattern; master platform differences and common pitfalls |

> **Tip**: the [`packages/playground`](https://github.com/MengXi-Studio/unix-router/tree/master/packages/playground) in the repo is a runnable uni-app x project that includes a `pages/test` self-check page (programmatic PASS/FAIL output). Use it to verify alongside the docs.

## Capabilities at a Glance

| Capability | Description |
| --- | --- |
| Route matching | `path` / `name` dual index, string / object / named resolution, strict mode |
| Navigation | push / replace / relaunch / back, auto-detects TabBar |
| Guards | beforeEach / beforeResolve / afterEach / beforeEnter / onBeforeRouteLeave, etc. |
| Composition API | useRouter / useRoute / useLink |
| Error system | RouterError / NavigationFailure / isNavigationFailure |
| Extensions | guardRoute cold-start guard, duplicate-navigation interception, guard redirect + depth limit |
| Plugin system | RouterPlugin / PluginContext (8 hooks) / ParamsPlugin / InterceptorPlugin (opt-in) |

> Install: `npm install @meng-xi/unix-router`

## Next Steps

- Want to start right away? Go to [Installation](./installation) or [Quick Start](./getting-started).
- Want to understand the design trade-offs? See [Differences from vue-router](./differences).