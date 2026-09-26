# 平台兼容性

unix-router 以 **UTS**（`.uts`）编写，由 uni-app x 编译链按平台现场编译。本章说明各平台的功能支持矩阵、版本门槛与平台差异。

## 功能支持矩阵

| 能力 | Web / H5 | 微信小程序 | App-Android | App-iOS | App-HarmonyOS |
| --- | --- | --- | --- | --- | --- |
| 核心导航（push / replace / relaunch / back） | ✅ | ✅ | ✅ | ✅ | ✅ |
| 路由守卫（beforeEach / beforeEnter / beforeResolve / afterEach） | ✅ | ✅ | ✅ | ✅ | ✅ |
| 组合式 API（useRouter / useRoute / useLink / useOpenerEventChannel） | ✅ | ✅ | ✅ | ✅ | ✅ |
| `<RouterLink>` 组件 | ✅ | ✅ | ✅ | ✅ | ✅ |
| ParamsPlugin（参数传递） | ✅ | ✅ | ✅ | ✅ | ✅ |
| EventsPlugin（页面间通信） | ✅ | ✅ | ✅ | ✅ | ✅ |
| AnimationPlugin（导航动画） | ✅（Web Animations API） | ⚠️（官方不支持动画字段，无动画） | ✅（原生透传） | ✅（原生透传） | ✅（原生透传） |
| InterceptorPlugin（uni API 拦截） | ✅（≥ 4.0） | ✅（≥ 4.41） | ✅（≥ 3.97） | ✅（≥ 4.11） | ✅（≥ 4.61） |

> 底层仅依赖 `uni.navigateTo / redirectTo / reLaunch / navigateBack / switchTab` 与 `getCurrentPages`，各平台由 uni-app x 原生适配层抹平。

## 编译产物与运行模式

| 平台 | 编译为 | 是否支持 |
| --- | --- | --- |
| Web / H5 | JavaScript | ✅ |
| 微信小程序 | JavaScript | ✅ |
| App-Android | Kotlin | ✅（经 UTS 编译） |
| App-iOS | Swift | ✅（经 UTS 编译） |
| App-HarmonyOS | ArkTS | ✅（经 UTS 编译） |

- **VDOM 模式**（第一代）：脚本编译为 Kotlin/Swift/ArkTS，依赖 UTS 强类型 → 本库已适配。
- **蒸汽模式**（新一代）：全端运行 JS → 本库同样可运行。

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

## uni API 拦截版本门槛（addInterceptor）

[InterceptorPlugin](./interceptor) 依赖 `uni.addInterceptor(name, interceptor)` / `uni.removeInterceptor(name, interceptor?)`。**开启拦截所需的最低 HBuilderX 版本（官方兼容性表）**：

| 平台 | 最低 HBuilderX 版本 |
| --- | --- |
| Web | 4.0 |
| 微信小程序 | 4.41 |
| Android | 3.97 |
| iOS | 4.11 |
| HarmonyOS | 4.61 |

::: warning 自动降级
运行平台的 `uni.addInterceptor` 缺失（版本低于上表）时，拦截**自动降级**并输出警告：导航仍正常进行，但外部直接调用 `uni.navigateTo` 不会被转交路由器，守卫对这部分调用不生效。
:::

::: tip 版本口径说明
早期 uni-app x 的 `interceptor` API 文档曾在"系统版本"兼容表中标注 iOS 暂不支持（x）；据最新 HBuilderX（含 Alpha 分支）官方文档，iOS 端已支持（≥ 4.11）。请以你所用 HBuilderX 的实际表现为准。
:::

拦截器仅针对**外部直接调用**生效；路由器自身 `router.*` 发起的 uni 调用不会被二次拦截（内部标记区分）。微信小程序端 `<navigator>` 组件跳转与点击 tabBar（底层不触发 `uni.switchTab`）无法被拦截，此场景需在页面 `onShow` 兜底守卫。

## H5 与原生差异

| 差异点 | H5（Web） | 原生端（App / 小程序） |
| --- | --- | --- |
| `router.install()`（`app.use(router)`） | 注册 `provide`（`useRouter` setup 注入）、挂载 `$router` / `$route` 全局属性、注册 `onShow` 全局 mixin（自动 `syncRoute()`） | 仅注册全局活跃路由器（`useRouter` 非 setup 回退可用）；建议在页面 `onShow` 手动调用 `router.syncRoute()` |
| 导航动画 | AnimationPlugin 用 **Web Animations API**（`element.animate`）对页面容器播放进入 / 退出动画 | App：透传原生 `animationType` / `animationDuration` 给 `uni.*` 导航 API（官方仅 App 支持）；小程序：官方不支持动画字段，无动画 |
| `RouteName` 路由名类型增强 | 经 `RouteNameMap` 模块增强，推导字面量提示（`keyof RouteNameMap & string`） | UTS 不支持 keyof 组合类型，退化为 `string`（配合 `strict` 校验兜底） |
| `hash` | 恒为 `''`（uni-app x 不支持 hash 路由，保留字段） | 恒为 `''` |
| 物理返回 / 侧滑 | 不经过路由器 | 不经过路由器，由 `syncRoute()` 在 `onShow` 对齐状态 |

## UTS 强类型相关注意

本库面向原生端编译，API 设计受 UTS 强类型约束，使用者同样会遇到：

- **query / params 是 `Map<string, string>`**，不是普通对象：读取用 `.get(key)`、判断用 `.has(key)`、写入用 `.set(key, value)`；构造时带泛型 `new Map<string, string>([['id', '1']])`。
- **没有 `undefined`**：可空值统一为 `null`（如 `route.name` 为 `string | null`），判断用 `!= null` 而非 `!= undefined`；条件语句须为显式布尔表达式（`if (redirect != null)`，不能写 truthy 判断 `if (redirect)`）。
- **插件 / 含方法的配置必须用 class**：非蒸汽（Kotlin/Swift）端对象字面量含方法会被推断为 `UTSJSONObject`，见[插件系统 - 平台注意](./plugins#平台注意uts-强类型)。

## 验证方式

- Web / 小程序：`packages/playground` 内 `pages/test` 自检页输出 PASS/FAIL。
- App 原生：HBuilderX 打开 playground 打包验证。

## 下一步

- [与 vue-router 的差异](./differences) — API 语义层面的差异清单
- [uni API 拦截](./interceptor) — InterceptorPlugin 用法与降级行为
- [导航流程原理](./navigation-flow) — `syncRoute` 的触发时机与设计依据
