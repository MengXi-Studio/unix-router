# 安装

## 要求

- **uni-app x** 工程（`.uvue` 页面）
- Vue 3

## npm 安装

```bash
npm install @meng-xi/unix-router
# 或
pnpm add @meng-xi/unix-router
```

> 本包为 **UTS 源分发**：包内含 `.uts` 源码，由 uni-app x 编译链按平台现场编译
> （Web / 小程序 → JS，Android → Kotlin，iOS → Swift），无需预编译。

## uni-app x 中的使用

uni-app x 工程可直接 `import` 使用：

```uts
import { createRouter } from '@meng-xi/unix-router'
```

### 关于 App 原生分发

若需在 App 原生（VDOM）端使用，推荐以 **`uni_modules/<name>/utssdk`** 形式分发 UTS 源码（参考 uni-app x 插件生态）。Web / 小程序端直接由编译链消费 `node_modules` 中的 `.uts` 源码即可。

## 验证安装

```uts
import { createRouter } from '@meng-xi/unix-router'

const router = createRouter({ routes: [] })
console.log(router.currentRoute.path) // '/'（初始占位）
```

> 完整可运行示例见仓库根目录下的 `packages/playground`（用 HBuilderX 打开即可运行）。