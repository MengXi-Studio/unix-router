# 安装

## 要求

- **uni-app x** 工程（`.uvue` 页面）
- Vue 3：uni-app x 已内置 Vue 3，**无需额外安装**；包中的 `vue` 仅以可选 peer 依赖声明（`>=3.0.0`），详见[下文](#peerdependencies-说明)。

## npm 安装

```bash
npm install @meng-xi/unix-router
# 或
pnpm add @meng-xi/unix-router
```

### UTS 源分发

本包为 **UTS 源分发**：包内直接是 `.uts` 源码（`main` / `exports` 即指向源码），由 uni-app x 编译链在构建时按目标平台现场编译，无需预编译：

| 平台 | 编译产物 |
| --- | --- |
| Web / 小程序 | JavaScript |
| Android | Kotlin |
| iOS | Swift |

路由逻辑在各原生端直接编译为 Kotlin / Swift 代码执行，没有运行时 JS 桥接开销。

### peerDependencies 说明

`vue` 声明为**可选** peer 依赖（`peerDependenciesMeta.vue.optional: true`，版本 `>=3.0.0`）：

- **uni-app x 工程**：框架已内置 Vue 3，无需也不应单独安装 `vue`。
- **脱离 uni-app x 复用源码**：仅在纯 Vue 3 工程中复用本包时，才需自行提供 Vue 3 运行时。

## uni_modules 方式安装

在 HBuilderX 中通过 uni 插件市场导入（内含 UTS 源码 `utssdk`）：

**[https://ext.dcloud.net.cn/plugin?id=29561](https://ext.dcloud.net.cn/plugin?id=29561)**

- HBuilderX 菜单「插件市场」搜索 `ux-router`（插件 ID：`ux-router`），点击「下载插件并导入 HBuilderX」
- 插件以 **`uni_modules/ux-router`** 自包含形式分发（内含 UTS 源码 `utssdk`），导入后 App 原生端（Android / iOS）与 Web / 小程序端均可用，无需额外配置
- 代码中按 uni_modules 路径引入（与仓库 playground 写法一致）：

```uts
import { createRouter } from '@/uni_modules/ux-router/utssdk/index.uts'
```

## 验证安装

```uts
import { createRouter } from '@meng-xi/unix-router'

const router = createRouter({ routes: [] })
console.log(router.currentRoute.path) // '/'（初始占位，首次导航/同步后变为真实路径）
```

> 完整可运行示例见仓库根目录下的 `packages/playground`（用 HBuilderX 打开即可运行）。
