# Platform Compatibility

unix-router is written in **UTS** (`.uts`) and compiled per platform by the uni-app x toolchain. This chapter covers the feature support matrix, version thresholds, and platform differences.

## Feature Support Matrix

| Capability | Web / H5 | WeChat Mini Program | App-Android | App-iOS | App-HarmonyOS |
| --- | --- | --- | --- | --- | --- |
| Core navigation (push / replace / relaunch / back) | ✅ | ✅ | ✅ | ✅ | ✅ |
| Route guards (beforeEach / beforeEnter / beforeResolve / afterEach) | ✅ | ✅ | ✅ | ✅ | ✅ |
| Composable APIs (useRouter / useRoute / useLink / useOpenerEventChannel) | ✅ | ✅ | ✅ | ✅ | ✅ |
| `<RouterLink>` component | ✅ | ✅ | ✅ | ✅ | ✅ |
| ParamsPlugin (parameter passing) | ✅ | ✅ | ✅ | ✅ | ✅ |
| EventsPlugin (page-to-page communication) | ✅ | ✅ | ✅ | ✅ | ✅ |
| AnimationPlugin (navigation animation) | ✅ (Web Animations API) | ⚠️ (animation fields not supported officially, no animation) | ✅ (native pass-through) | ✅ (native pass-through) | ✅ (native pass-through) |
| InterceptorPlugin (uni API interception) | ✅ (≥ 4.0) | ✅ (≥ 4.41) | ✅ (≥ 3.97) | ✅ (≥ 4.11) | ✅ (≥ 4.61) |

> The underlying dependencies are only `uni.navigateTo / redirectTo / reLaunch / navigateBack / switchTab` and `getCurrentPages`, smoothed over per platform by uni-app x's native adaptation layer.

## Build Artifacts and Runtime Modes

| Platform | Compiles to | Supported |
| --- | --- | --- |
| Web / H5 | JavaScript | ✅ |
| WeChat Mini Program | JavaScript | ✅ |
| App-Android | Kotlin | ✅ (via UTS compilation) |
| App-iOS | Swift | ✅ (via UTS compilation) |
| App-HarmonyOS | ArkTS | ✅ (via UTS compilation) |

- **VDOM mode** (first generation): scripts compile to Kotlin/Swift/ArkTS, relying on UTS strong typing → supported by this library.
- **Vapor mode** (new generation): runs JS on all platforms → this library runs there as well.

Both modes work out of the box; no code changes are needed.

## Environment Requirements (HBuilderX)

::: warning Basis for version judgments
This library ships as `.uts` sources, compiled per target platform by the uni-app x toolchain. **The core code uses no bleeding-edge features** (it only depends on basic UTS syntax + `uni.*` navigation APIs + `getCurrentPages`), so the "minimum HBuilderX version" is determined by the **target runtime platform and rendering mode**, not by the library's code itself.
:::

| Target platform / mode | Minimum requirement |
| --- | --- |
| uni-app x running on HarmonyOS | HBuilderX **4.61+** |
| App / HarmonyOS - **VDOM mode** | HBuilderX **4.71+** (SDK minimum 4.71) |
| App / HarmonyOS - **vapor mode** | vapor runtime `@dcloudio/uni-app-x-vapor-runtime` minimum **5.25** |
| Web / WeChat Mini Program (JS target) | any early HBuilderX (4.x) works, no extra requirements |

::: tip Note on vapor-mode versions
The **5.25 for vapor mode is an SDK / runtime module version number**, not an HBuilderX version number — the two numbering systems differ; do not mix them. For vapor mode, pair it with the **latest stable HBuilderX**.
:::

**Practical advice**: installing the **latest stable HBuilderX** covers VDOM + vapor + HarmonyOS at once; there is no need to deliberately pin to minimum versions.

## uni API Interception Version Threshold (addInterceptor)

[InterceptorPlugin](./interceptor) depends on `uni.addInterceptor(name, interceptor)` / `uni.removeInterceptor(name, interceptor?)`. **Minimum HBuilderX versions required for interception (official compatibility table)**:

| Platform | Minimum HBuilderX version |
| --- | --- |
| Web | 4.0 |
| WeChat Mini Program | 4.41 |
| Android | 3.97 |
| iOS | 4.11 |
| HarmonyOS | 4.61 |

::: warning Automatic downgrade
When the running platform lacks `uni.addInterceptor` (version below the table), interception **automatically downgrades** with a warning: navigation still proceeds normally, but external direct calls to `uni.navigateTo` are not handed to the router, and guards do not apply to those calls.
:::

::: tip Note on version criteria
Early uni-app x `interceptor` API docs once marked iOS as unsupported (x) in the "system version" compatibility table; according to the latest HBuilderX official documentation (including the Alpha branch), iOS is now supported (≥ 4.11). Rely on your HBuilderX's actual behavior.
:::

The interceptor only applies to **external direct calls**; the uni calls issued by the router itself via `router.*` are not re-intercepted (distinguished via an internal marker). On WeChat Mini Program, `<navigator>` component jumps and tabBar taps (which do not trigger `uni.switchTab` under the hood) cannot be intercepted; handle those scenarios with an `onShow` fallback guard in the page.

## H5 vs Native Differences

| Difference | H5 (Web) | Native (App / Mini Program) |
| --- | --- | --- |
| `router.install()` (`app.use(router)`) | Registers `provide` (for `useRouter` setup injection), mounts `$router` / `$route` global properties, registers the `onShow` global mixin (automatic `syncRoute()`) | Only registers the global active router (usable by `useRouter`'s non-setup fallback); recommended to call `router.syncRoute()` manually in each page's `onShow` |
| Navigation animation | AnimationPlugin plays enter / exit animations on the page container with the **Web Animations API** (`element.animate`) | App: passes native `animationType` / `animationDuration` through to the `uni.*` navigation APIs (officially App-only); Mini Programs: animation fields not supported officially, no animation |
| `RouteName` route-name type augmentation | Augmented via the `RouteNameMap` module, inferring literal hints (`keyof RouteNameMap & string`) | UTS does not support keyof composite types; degrades to `string` (with `strict` validation as the backstop) |
| `hash` | Always `''` (uni-app x does not support hash routing; field kept) | Always `''` |
| Physical back / edge swipe | Does not pass through the router | Does not pass through the router; `syncRoute()` re-aligns the state in `onShow` |

## Notes on UTS Strong Typing

The library is compiled for native platforms and its API design is constrained by UTS strong typing; you will encounter the same constraints:

- **query / params are `Map<string, string>`**, not plain objects: read with `.get(key)`, check with `.has(key)`, write with `.set(key, value)`; construct with the generic `new Map<string, string>([['id', '1']])`.
- **No `undefined`**: nullable values are uniformly `null` (e.g. `route.name` is `string | null`); test with `!= null` rather than `!= undefined`; conditional statements must be explicit boolean expressions (`if (redirect != null)`, not a truthy check `if (redirect)`).
- **Plugins / method-bearing configs must be classes**: on non-vapor (Kotlin/Swift) platforms, an object literal containing methods is inferred as `UTSJSONObject` — see [Plugin System - Platform Notes](./plugins#platform-notes-uts-strong-typing).

## How to Verify

- Web / Mini Program: the `pages/test` self-check page inside `packages/playground` prints PASS/FAIL.
- App native: open the playground in HBuilderX and verify via a custom build.

## Next Steps

- [Differences from vue-router](./differences) — the list of API-level semantic differences
- [uni API Interception](./interceptor) — InterceptorPlugin usage and downgrade behavior
- [Navigation Flow](./navigation-flow) — `syncRoute`'s trigger timing and design rationale
