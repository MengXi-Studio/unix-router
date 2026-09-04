# 更新日志

## 0.1.0（2026-09-04）

首个可运行版本。

### 新增

- 核心：路由器创建、路由匹配（path / name 双索引）、严格模式
- 导航：`push` / `replace` / `relaunch` / `back`，自动识别 TabBar
- 守卫：`beforeEach` / `beforeResolve` / `afterEach` / `beforeEnter` / 组件内守卫（`onBeforeRouteLeave` / `onBeforeRouteUpdate` / `onBeforeRouteEnter`）
- 组合式 API：`useRouter` / `useRoute` / `useLink`
- 状态同步：`syncRoute`，基于 `getCurrentPages()` 的响应式 `currentRoute`
- 错误体系：`RouterError` / `NavigationFailure` / `isNavigationFailure` / `RouterErrorCode`
- 组件：`RouterLink`
- 双模式：UTS 源分发，web / 小程序 → JS，Android → Kotlin，iOS → Swift