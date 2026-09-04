# Installation

## Requirements

- A **uni-app x** project (`.uvue` pages)
- Vue 3

## Installing via npm

```bash
npm install @meng-xi/unix-router
# or
pnpm add @meng-xi/unix-router
```

> This package is distributed as **UTS source**: it ships the `.uts` source code, which is compiled on the fly for each platform by the uni-app x build chain
> (Web / Mini Program → JS, Android → Kotlin, iOS → Swift), with no pre-compilation required.

## Usage in uni-app x

A uni-app x project can `import` it directly:

```uts
import { createRouter } from '@meng-xi/unix-router'
```

### About App Native Distribution

If you need to use it on the App native (VDOM) side, it is recommended to distribute the UTS source in the **`uni_modules/<name>/utssdk`** form (refer to the uni-app x plugin ecosystem). For the Web / Mini Program sides, the build chain can consume the `.uts` source in `node_modules` directly.

## Verifying the Installation

```uts
import { createRouter } from '@meng-xi/unix-router'

const router = createRouter({ routes: [] })
console.log(router.currentRoute.path) // '/'（initial placeholder）
```

> For a complete runnable example, see `packages/playground` at the repository root (open it with HBuilderX to run).