# Platform Compatibility

unix-router is written in **UTS** (`.uts`) and is compiled on the fly for each platform by the uni-app x build chain.

## Compiled Output

| Platform | Compiled to | Supported |
| --- | --- | --- |
| Web / H5 | JavaScript | ✅ |
| WeChat Mini Program | JavaScript | ✅ |
| Android (VDOM / vapor mode) | Kotlin / JS | ✅ |
| iOS (VDOM) | Swift / JS | ✅ |
| App-Android (VDOM native) | Kotlin | ✅ (via UTS compilation) |

> Per the official rules: when the target language is JS, ts/js can be referenced directly; when it is not JS (Android), only ts files can be referenced, treated as UTS. This library is therefore distributed as `.uts` source, ensuring it compiles on all endpoints.

## Navigation API Platform Differences

Under the hood, it only depends on `uni.navigateTo / redirectTo / reLaunch / navigateBack / switchTab`, which are equalized across platforms by uni-app x's native adaptation layer.

- `switchTab` (tabBar pages): consistent across endpoints.
- App back key / swipe-back: does not pass through the router; it is synced by `syncRoute()` on `onShow`.

## Runtime Modes

- **VDOM mode** (first generation): scripts compile to Kotlin/Swift and rely on strong UTS typing → this library is already adapted.
- **Vapor mode** (the new generation starting 2026): runs JS on all endpoints → this library runs here too.

Both modes can be used directly without changing your code.

## Environment Requirements (HBuilderX)

::: warning How versions are determined
This library is distributed as `.uts` source and is compiled on the fly for each target platform by the uni-app x build chain. **The core code uses no "cutting-edge" features** (it only relies on basic UTS syntax + the `uni.*` navigation APIs + `getCurrentPages`), so the "minimum HBuilderX version" is decided by the **target platform and rendering mode**, not by the library code itself.
:::

| Target platform / mode | Minimum requirement |
| --- | --- |
| uni-app x running to HarmonyOS | HBuilderX **4.61+** |
| App / Harmony - **VDOM mode** | HBuilderX **4.71+** (SDK minimum 4.71) |
| App / Harmony - **Vapor mode** | Vapor runtime `@dcloudio/uni-app-x-vapor-runtime` minimum **5.25** |
| Web / WeChat Mini Program (JS target) | HBuilderX first release (4.x) is enough, no extra requirement |

::: tip Vapor version note
The vapor mode **5.25 is the SDK / runtime module version**, not the HBuilderX version. The two use different versioning systems, so do not mix them up. For vapor mode, use the **latest stable HBuilderX**.
:::

**Practical advice**: installing the **latest stable HBuilderX** covers VDOM, vapor, and HarmonyOS all at once; there is no need to target the lowest version deliberately.

## Intercepting Native Navigation APIs (addInterceptor)

uni-app x provides `uni.addInterceptor(name, interceptor)` / `uni.removeInterceptor(name, interceptor?)` for intercepting native navigation APIs. **Minimum HBuilderX version required to enable interception (official compatibility table)**:

| Platform | Minimum HBuilderX version |
| --- | --- |
| Web | 4.0 |
| WeChat Mini Program | 4.41 |
| Android | 3.97 |
| iOS | 4.11 |
| HarmonyOS | 4.61 |

Interceptable navigation APIs: `navigateTo` / `redirectTo` / `reLaunch` / `switchTab` / `navigateBack` (plus `loadFontFace`, `pageScrollTo`, `setNavigationBarTitle`, etc.).

::: tip Version note
Earlier uni-app x docs for the `interceptor` API once marked iOS as unsupported (x) in its "system version" compatibility table; per the latest official docs (including the HBuilderX Alpha branch), iOS is now supported (≥ 4.11). Trust the actual behavior of the HBuilderX version you are using.
:::

::: warning What this means for the library
- This library provides it as a **plugin**: `createRouter({ routes, plugins: [InterceptorPlugin], interceptUniApi: true })`. Once enabled, external direct calls to `uni.navigateTo` etc. are handed to the router to run the full guard chain (avoiding bypassed guards).
- The interceptor only applies to "external direct calls"; the router's **own `router.*` calls are not intercepted twice**.
- If the platform's HBuilderX version is below the table above, `addInterceptor` cannot be registered at runtime; interception degrades gracefully with a warning.

### Conventions

- Prefer navigating via `router.push / replace / relaunch / back` or `<RouterLink>`.
- Calling `uni.navigateTo` directly bypasses the route guards; enable native navigation interception if you need those calls routed through the router.

## How to Verify

- Web / Mini Program: the `pages/test` self-check page in `packages/playground` outputs PASS/FAIL.
- App native: open the playground in HBuilderX and package it to verify.