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

## How to Verify

- Web / Mini Program: the `pages/test` self-check page in `packages/playground` outputs PASS/FAIL.
- App native: open the playground in HBuilderX and package it to verify.