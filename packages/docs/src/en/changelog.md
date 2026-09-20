# Changelog

## [0.7.0] - 2026-09-20

### Breaking

- **Plugins are now registered as instances**: on the native (Kotlin/Swift) end `RouterPlugin` becomes an abstract class (object literals cannot hold methods), and the built-in plugins extend it as
  `class ParamsPlugin extends RouterPlugin`; registration changes from `plugins: [ParamsPlugin]` to `plugins: [new ParamsPlugin()]` (same for Interceptor / Animation / Events).

### Fixed

- **Full native-compilation (non-steam mode) compatibility**:
  - Method-carrying object literals converted to classes: `RouteState` / `GuardManager` / `RouteMatcher` / `ParamsManager` / `PluginContext` are now classes instead of factory-returned object literals, eliminating the
    Kotlin UTSJSONObject inference that broke method calls
  - Explicit boolean conditions: removed all truthy checks (`if (x)` → `if (x != null)`) to satisfy the UTS rule that conditions must be boolean
  - uni.* navigation options adapted per platform: App / Mini Program use animation-free object literals (matching the `NavigateToOptions` named parameter type), H5 uses UTSJSONObject carrying `animationType`
  - Variadic function types made compatible: event callbacks changed from `(...args: any[]) => any` to single-arg `(data: any) => any` (Kotlin forbids vararg / modifiers on function-type parameters)
  - Iteration and type cleanup: replaced `for..in` + `Object.prototype` with `UTSJSONObject.keys()`, removed `undefined` identifiers and `Promise.reject` return-type issues, made nullable parameters explicit
  - Page layer: top-level functions used in templates are wrapped by local functions (exposed as properties on Kotlin-native); ucss compound/descendant selectors replaced by dynamic classes

## [0.6.0] - 2026-09-18

### Added

- **`EventsPlugin` page-to-page event communication plugin** (opt-in via `plugins: [EventsPlugin]`):
  - Aligns with the official `navigateTo` `events` semantics: the opener passes an `events` listener map in `push`, and the opened page emits data back / receives pushes through the `EventChannel` from
    `useOpenerEventChannel()`
  - Backed by the built-in `eventBus` (`$on` / `$once` / `$off` / `$emit`, listeners removed by id), free of the official `uni.$on` version gate
  - The channel key is bridged across pages via the internal `__evt__` URL query key and stripped during state sync (never exposed to users); `useOpenerEventChannel()` does not depend on route-sync timing, usable right
    in `onShow`
  - Navigating with `events` without registering the plugin throws `PLUGIN_REQUIRED` for clear guidance
- New exports: `EventsPlugin` / `eventBus` / `useOpenerEventChannel`
- New type: `EventsMap`; `RawLocation` / `RouteLocationRaw` accept the optional `events` field

## [0.5.1] - 2026-09-17

### Fixed

- **Android base compilation error (UTS110111101)**: the return type of `UniHistory.currentStack()` was an inline object literal `{ path: string; query: Map<string, string> }`, which UTS does not allow as a direct
  object-literal type declaration, breaking the Android base packaging compile; extracted it into the named type `CurrentStackInfo`

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
