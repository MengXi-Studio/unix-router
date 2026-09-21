# Installation

## Requirements

- A **uni-app x** project (`.uvue` pages)
- Vue 3 — uni-app x already bundles Vue 3, so **no extra installation is needed**; the `vue` in this package is only declared as an optional peer dependency (`>=3.0.0`). See [peerDependencies](#peerdependencies-explained) below.

## Installing via npm

```bash
npm install @meng-xi/unix-router
# or
pnpm add @meng-xi/unix-router
```

### UTS Source Distribution

This package ships as **UTS source**: what you get is the `.uts` source code itself (`main` / `exports` point directly to the sources). The uni-app x build chain compiles it on the fly for the target platform at build time — no pre-compilation required:

| Platform | Compiled output |
| --- | --- |
| Web / Mini Program | JavaScript |
| Android | Kotlin |
| iOS | Swift |

Route logic is compiled directly to Kotlin / Swift code on each native platform, with no runtime JS bridge overhead.

### peerDependencies Explained

`vue` is declared as an **optional** peer dependency (`peerDependenciesMeta.vue.optional: true`, version `>=3.0.0`):

- **uni-app x projects**: the framework already bundles Vue 3, so you neither need to nor should install `vue` separately.
- **Reusing the source outside uni-app x**: only when reusing this package in a pure Vue 3 project do you need to provide the Vue 3 runtime yourself.

## Installing via uni_modules

Import it from the uni plugin market in HBuilderX (includes the UTS source under `utssdk`):

**[https://ext.dcloud.net.cn/plugin?id=29561](https://ext.dcloud.net.cn/plugin?id=29561)**

- In HBuilderX, open the "Plugin Market" and search for `ux-router` (plugin ID: `ux-router`), then click "Download Plugin and Import into HBuilderX"
- The plugin is distributed as a self-contained **`uni_modules/ux-router`** (with the UTS source under `utssdk`); after importing, it works on App native (Android / iOS) as well as Web / Mini Program with no extra configuration
- Import it in code by the uni_modules path (matching the repo playground style):

```uts
import { createRouter } from '@/uni_modules/ux-router/utssdk/index.uts'
```

## Verifying the Installation

```uts
import { createRouter } from '@meng-xi/unix-router'

const router = createRouter({ routes: [] })
console.log(router.currentRoute.path) // '/' (initial placeholder; becomes the real path after the first navigation or sync)
```

> For a complete runnable example, see `packages/playground` at the repository root (open it with HBuilderX to run).

## Next Steps

- Ready to build something? Head to [Getting Started](./getting-started).
- Curious about the design trade-offs? See [Differences from vue-router](./differences).
