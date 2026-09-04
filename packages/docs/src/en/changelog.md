# Changelog

## 0.1.0 (2026-09-04)

First runnable release.

### Added

- Core: router creation, route matching (path / name dual index), strict mode
- Navigation: `push` / `replace` / `relaunch` / `back`, with automatic TabBar detection
- Guards: `beforeEach` / `beforeResolve` / `afterEach` / `beforeEnter` / in-component guards (`onBeforeRouteLeave` / `onBeforeRouteUpdate` / `onBeforeRouteEnter`)
- Composition API: `useRouter` / `useRoute` / `useLink`
- State sync: `syncRoute`, with a reactive `currentRoute` based on `getCurrentPages()`
- Error system: `RouterError` / `NavigationFailure` / `isNavigationFailure` / `RouterErrorCode`
- Component: `RouterLink`
- Dual mode: UTS source distribution — web / Mini Program → JS, Android → Kotlin, iOS → Swift