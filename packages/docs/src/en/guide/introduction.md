# Introduction

`@meng-xi/unix-router` is a routing management library for **uni-app x**. Its API style fully mirrors **vue-router 4**, but it builds on uni-app x's **static page model** (`.uvue` pages + `pages.json` registration + `uni.*` native navigation).

## Why Do You Need It

uni-app x natively provides `uni.navigateTo / redirectTo / reLaunch / navigateBack / switchTab`, but once your pages grow, using these APIs directly has clear pain points:

- **No route table**: page paths are scattered around with no centralized management; there are no named routes or type hints.
- **No guards**: you cannot run unified login checks, analytics, or other logic before navigation.
- **No route state**: there is no unified reactive `currentRoute` object, making it hard to track the current page in the Composition API.
- **Cumbersome parameter passing**: object-type parameters are hard to pass between pages, and query parsing has to be handwritten.

unix-router smooths over these differences with an API set that matches vue-router.

## Core Capabilities at a Glance

| Capability | Description |
| --- | --- |
| Route matching | `path` / `name` dual index, string / object / named three resolution modes, strict mode |
| Navigation | push / replace / relaunch / back, auto-detects TabBar |
| Guards | beforeEach / beforeResolve / afterEach / beforeEnter / onBeforeRouteLeave, etc. |
| Composition API | useRouter / useRoute / useLink |
| Error system | RouterError / NavigationFailure / isNavigationFailure |
| Extensions | guardRoute cold-start guard, duplicate-navigation interception, guard redirect + depth limit |

## Next Steps

- Want to use it right away? Go to [Installation](./installation) or [Getting Started](./getting-started).
- Want to understand the design trade-offs? See [Differences from vue-router](./differences).