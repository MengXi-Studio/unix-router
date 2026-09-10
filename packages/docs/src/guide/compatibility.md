# 平台兼容性

unix-router 以 **UTS**（`.uts`）编写，由 uni-app x 编译链按平台现场编译。

## 编译产物

| 平台 | 编译为 | 是否支持 |
| --- | --- | --- |
| Web / H5 | JavaScript | ✅ |
| 微信小程序 | JavaScript | ✅ |
| Android（VDOM / 蒸汽模式） | Kotlin / JS | ✅ |
| iOS（VDOM） | Swift / JS | ✅ |
| App-Android（VDOM 原生） | Kotlin | ✅（经 UTS 编译） |

> 依据官方规则：目标语言为 JS 时直接引用 ts/js；非 JS（Android）时仅可引用 ts 文件并当作 UTS 处理。因此本库以 `.uts` 源分发，确保全端可编译。

## 导航 API 平台差异

底层仅依赖 `uni.navigateTo / redirectTo / reLaunch / navigateBack / switchTab`，各平台由 uni-app x 原生适配层抹平。

- `switchTab`（tabBar 页）：各端一致。
- App 返回键 / 侧滑：不经过路由器，由 `syncRoute()` 在 `onShow` 同步。

## 运行模式

- **VDOM 模式**（第一代）：脚本编译为 Kotlin/Swift，依赖 UTS 强类型 → 本库已适配。
- **蒸汽模式**（2026 起新一代）：全端运行 JS → 本库同样可运行。

两种模式均可直接使用，无需改动代码。

## 环境要求（HBuilderX）

::: warning 版本判断依据
本库以 `.uts` 源分发，由 uni-app x 编译链按目标平台现场编译，**核心代码未使用任何"超新"特性**（仅依赖基础 UTS 语法 + `uni.*` 导航 API + `getCurrentPages`），因此"HBuilderX 最低版本"由**目标运行平台与渲染模式**决定，而非本库代码本身。
:::

| 目标平台 / 模式 | 最低要求 |
| --- | --- |
| uni-app x 支持运行到鸿蒙 (HarmonyOS) | HBuilderX **4.61+** |
| App / 鸿蒙 - **VDOM 模式** | HBuilderX **4.71+**（SDK 最低 4.71） |
| App / 鸿蒙 - **蒸汽模式 (vapor)** | 蒸汽运行时 `@dcloudio/uni-app-x-vapor-runtime` 最低 **5.25** |
| Web / 微信小程序（JS 目标） | HBuilderX 初版（4.x）即可，无额外要求 |

::: tip 蒸汽模式版本说明
蒸汽模式的 **5.25 是 SDK / 运行时模块版本号**，不是 HBuilderX 版本号，两者数字体系不同，请勿混用。使用蒸汽模式请配合 HBuilderX **最新稳定版**。
:::

**实操建议**：安装 HBuilderX **最新稳定版**即可覆盖 VDOM + 蒸汽 + 鸿蒙三个方向，无需刻意下探到最低版本。

## 原生导航 API 拦截（addInterceptor）

uni-app x 提供 `uni.addInterceptor(name, interceptor)` / `uni.removeInterceptor(name, interceptor?)` 拦截原生导航 API。**开启拦截所需的最低 HBuilderX 版本（官方兼容性表）**：

| 平台 | 最低 HBuilderX 版本 |
| --- | --- |
| Web | 4.0 |
| 微信小程序 | 4.41 |
| Android | 3.97 |
| iOS | 4.11 |
| HarmonyOS | 4.61 |

可拦截的导航相关 API：`navigateTo` / `redirectTo` / `reLaunch` / `switchTab` / `navigateBack`（另有 `loadFontFace`、`pageScrollTo`、`setNavigationBarTitle` 等）。

::: tip 版本口径说明
早期 uni-app x 的 `interceptor` API 文档曾在"系统版本"兼容表中标注 iOS 暂不支持（x）；据最新 HBuilderX（含 Alpha 分支）官方文档，iOS 端已支持（≥ 4.11）。请以你所用 HBuilderX 的实际表现为准。
:::

::: warning 对本库的意义
- 以**插件**形式提供：`createRouter({ routes, plugins: [InterceptorPlugin], interceptUniApi: true })`。开启后，外部直接调用 `uni.navigateTo` 等会被转交路由器执行完整守卫链（避免绕过守卫）。
- 拦截器仅针对"外部直接调用"生效；**路由器自身的 `router.*` 调用不会被二次拦截**。
- 若运行平台的 HBuilderX 版本低于上表，运行时无法注册 `addInterceptor`，拦截自动降级并输出警告。
:::

### 使用约定

- 推荐统一使用 `router.push / replace / relaunch / back` 或 `<RouterLink>` 进行导航。
- 直接调用 `uni.navigateTo` 等原生 API 会**绕过路由守卫**；需时可通过开启原生导航拦截将其转由路由器处理。

## 验证方式

- Web / 小程序：`packages/playground` 内 `pages/test` 自检页输出 PASS/FAIL。
- App 原生：HBuilderX 打开 playground 打包验证。