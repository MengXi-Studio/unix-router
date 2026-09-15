# Changelog

## [0.5.0] - 2026-09-16

### Added

- **`AnimationPlugin` navigation window animation plugin** (opt-in, `plugins: [AnimationPlugin]`):
  - App / Mini Program: passes `animationType` / `animationDuration` through to the `uni.*` native navigation APIs (native window animation)
  - H5: plays enter / exit animations with the Web Animations API (`element.animate`) — no CSS `@keyframes` needed
  - Global default animation (`RouterOptions.animation`) + per-navigation overrides (`animationType` / `animationDuration`)
- **`RouterOptions.animation`**: global default navigation animation config `{ type, duration }`
- **Query helper functions publicly exported**: `queryInt()` / `queryNumber()` / `queryBool()` are now exported from the library entry — no need to import via relative paths
- New types: `NavigationAnimation` / `AnimationType`; `RawLocation` supports optional `animationType` / `animationDuration` fields

### Fixed

- **H5 first-entry lag on secondary pages**: when `onCompleteNavigation` fires, uni-app x H5 has already swapped the new page's content into `uni-page`; the plugin now **synchronously applies the animation start style +
  forces a reflow** so the new page's first rendered frame is already off-screen, and the slide-in animation plays on the next frame — eliminating the jarring "content flashes in place, then jumps off-screen and slides
  in" effect. The inline start style is cleared after the animation ends so it cannot affect the exit animation of a later `back()`
- **H5 back animation not playing**: added `toExitType()` mapping (enter-type → exit-type animations); `back()` now plays the exit animation to completion before the real `navigateBack`

## [0.4.0] - 2026-09-13

### Added

- **`RouterLink` component publicly exported**: a declarative navigation component based on `useLink` (`to` / `replace` / `relaunch`), importable directly from the library entry
- **Plugin contract compliance**: `RouterPlugin` / `PluginContext` etc. changed from `interface` to `type`, allowing direct object-literal assignment (avoids the UTS constraint that object literals cannot be assigned to
  interfaces)

### Changed (breaking)

- **Params passing unified**: removed the legacy `__unixr_p_` query-prefix encoding (`encodeParamsToQuery` / `extractParamsFromQuery` and related constants); `params` are now always passed across pages via `ParamsPlugin`
  (the `__params__` keyed store). Old-format URLs no longer restore params; navigations carrying params must register `plugins: [ParamsPlugin]`

### Fixed

- Eliminated all `undefined` leftovers across the library (unified `== null` / `??` narrowing), fixing Web compilation type warnings
- `RouterLink` css compliance (removed `scoped` / `inline-block`, switched to flex layout)

## [0.3.0] - 2026-09-11

### Added

- **`RouterOptions.paramsPersistent`**: with `ParamsPlugin`, params are persisted to storage by default (kept across refreshes / re-entry); default `false`
- **Plugin system polish**: `PluginContext` exposes the full navigation hooks (`onEnrichLocation` / `onAfterResolve` / `onPrepareNavigation` / `onCompleteNavigation` / `onNavigationAbort` / `onRouteSync` /
  `onAppInstall`) plus `router` / `paramsManager` / `hasPlugin`, supporting custom plugins
- **`Router.guardRoute()` / `onRouteChange`**: completed cold-start guard re-check and route-change listening capabilities

### Fixed

- Internal `__params__` key is now stripped (`stripInternalKeys`) before writing into `currentRoute`, so it is not exposed to users
- `ParamsPlugin`'s `afterResolve` now null-guards the `Map.get` return value when no `__params__` key exists, fixing a crash that could interrupt the navigation chain

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
