# 更新日志

## [0.3.0] - 2026-09-10

### 新增

- **`RouterOptions.paramsPersistent`**：配合 `ParamsPlugin` 将 params 默认持久化到 storage（跨刷新/重进保留），默认 `false`
- **插件体系完善**：`PluginContext` 提供完整导航 hook（`onEnrichLocation` / `onAfterResolve` / `onPrepareNavigation` / `onCompleteNavigation` / `onNavigationAbort` / `onRouteSync` / `onAppInstall`）与 `router` /
  `paramsManager` / `hasPlugin`，支持自定义插件
- **`Router.guardRoute()` / `onRouteChange`**：冷启动守卫补跑与路由变化监听能力补齐

### 修复

- 导航成功后写入 `currentRoute` 前剔除内部 `__params__` key（`stripInternalKeys`），避免内部 key 暴露给用户
- `ParamsPlugin` 的 `afterResolve` 在无 `__params__` 键时对 `Map.get` 返回值（`null`）做空值防护，修复可能中断导航链路的崩溃

## [0.2.0] - 2026-09-09

### 新增

- **uni API 拦截（opt-in）**：新增 `RouterOptions.interceptUniApi` 配置，拦截 `uni.navigateTo` / `redirectTo` / `switchTab` / `reLaunch` / `navigateBack` 的直接调用，转由 `router.*` 走完整守卫链，守卫下沉到 uni
  API 层；内置调用去重（计数器），并针对 H5 平台 `switchTab` 做放行 + 状态同步的特殊处理

### 修复

- 重复导航判定优化：`path + query + params + hash` 全部一致才判 `DUPLICATED`，允许以不同参数重入当前页面（此前 params 差异会被忽略而误判重复）

## [0.1.0] - 2026-09-05

首个可运行版本。

### 新增

- 核心：路由器创建、路由匹配（path / name 双索引）、严格模式
- 导航：`push` / `replace` / `relaunch` / `back`，自动识别 TabBar
- 守卫：`beforeEach` / `beforeResolve` / `afterEach` / `beforeEnter` / 组件内守卫（`onBeforeRouteLeave` / `onBeforeRouteUpdate` / `onBeforeRouteEnter`）
- 组合式 API：`useRouter` / `useRoute` / `useLink`
- 状态同步：`syncRoute`，基于 `getCurrentPages()` 的响应式 `currentRoute`
- 错误体系：`RouterError` / `NavigationFailure` / `isNavigationFailure` / `RouterErrorCode`
- 双模式：UTS 源分发，web / 小程序 → JS，Android → Kotlin，iOS → Swift
