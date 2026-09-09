# Changelog

## [0.2.0] - 2026-09-09

### Added

- **uni API interception (opt-in)**: new `RouterOptions.interceptUniApi` option. Intercepts direct calls to `uni.navigateTo` / `redirectTo` / `switchTab` / `reLaunch` / `navigateBack` and reroutes them through `router.*`
  so the full guard chain runs — guards are sunk down to the uni API layer. Includes internal call deduplication (counter-based) and an H5-specific `switchTab` passthrough + state-sync special case.

### Fixed

- Duplicate-navigation detection now compares `path + query + params + hash`; it only reports `DUPLICATED` when all four match, so re-entering the current page with different params is allowed (previously a params
  difference was ignored and misreported as a duplicate).

## [0.1.0] - 2026-09-05

First runnable release.

### Added

- Core: router creation, route matching (path / name dual index), strict mode
- Navigation: `push` / `replace` / `relaunch` / `back`, with automatic TabBar detection
- Guards: `beforeEach` / `beforeResolve` / `afterEach` / `beforeEnter` / in-component guards (`onBeforeRouteLeave` / `onBeforeRouteUpdate` / `onBeforeRouteEnter`)
- Composition API: `useRouter` / `useRoute` / `useLink`
- State sync: `syncRoute`, with a reactive `currentRoute` based on `getCurrentPages()`
- Error system: `RouterError` / `NavigationFailure` / `isNavigationFailure` / `RouterErrorCode`
- Dual mode: UTS source distribution — web / Mini Program → JS, Android → Kotlin, iOS → Swift
