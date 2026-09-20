## 0.7.0（2026-09-20）

### 变更（破坏性）

- **插件注册改为实例化**：非蒸汽（原生 Kotlin/Swift）端不支持「对象字面量承载方法」，`RouterPlugin` 由 type 调整为 abstract class，内置插件改为 `class ParamsPlugin extends RouterPlugin` 实现，注册写法从
  `plugins: [ParamsPlugin]` 变更为 `plugins: [new ParamsPlugin()]`（Interceptor / Animation / Events 三个插件同理）

### 修复

- **非蒸汽（原生）编译全链路兼容**：
  - 含方法的对象字面量 class 化：`RouteState` / `GuardManager` / `RouteMatcher` / `ParamsManager` / `PluginContext` 均由「工厂返回对象字面量」重构为 class，消除 Kotlin 端被推断为 UTSJSONObject 导致的方法调用失效
  - 条件语句显式布尔化：全库移除 truthy 判断（`if (x)` → `if (x != null)`），符合 UTS 条件必须为布尔值的规范
  - uni.* 导航 options 跨端适配：App / 小程序使用无动画字段的对象字面量（匹配 `NavigateToOptions` 具名参数），H5 使用 UTSJSONObject 携带 `animationType`
  - 变参函数类型兼容：事件回调由 `(...args: any[]) => any` 调整为单参数 `(data: any) => any`（Kotlin 函数类型参数不支持 vararg / 修饰符）
  - 遍历与类型清理：移除 `for..in` + `Object.prototype` 遍历（改用 `UTSJSONObject.keys()`）、清除 `undefined` 标识符与 `Promise.reject` 返回类型问题、显式可空参数
  - 页面层：模板绑定顶层函数改为本地包装函数（非蒸汽下端顶层函数以属性对象暴露）；ucss 复合/后代选择器改为动态 class

## 0.6.0（2026-09-18）

### 新增

- **`EventsPlugin` 页面间事件通信插件**（opt-in，`plugins: [EventsPlugin]`）：
  - 对齐 uni-app 官方 `navigateTo` 的 `events` 语义：打开方 `push` 携带 `events` 监听表，被打开页通过 `useOpenerEventChannel()` 的 `EventChannel` `emit` 回传数据 / `on` 接收推送
  - 底层为自研 `eventBus`（`$on` / `$once` / `$off` / `$emit`，按 id 移除监听），不受官方 `uni.$on` 版本门槛限制
  - 通道 key 经内部 `__evt__` URL 查询串跨页桥接，状态同步时剔除（不暴露给用户）；`useOpenerEventChannel()` 不依赖路由同步时机，onShow 内即可使用
  - 未注册插件却携带 `events` 的导航抛 `PLUGIN_REQUIRED`，明确引导
- 新增导出：`EventsPlugin` / `eventBus` / `useOpenerEventChannel`
- 新增类型：`EventsMap`；`RawLocation` / `RouteLocationRaw` 支持 `events` 可选字段

## 0.5.1（2026-09-17）

### 修复

- **打包安卓基座编译错误（UTS110111101）**：`UniHistory.currentStack()` 的返回类型原为内联对象字面量
  `{ path: string; query: Map<string, string> }`，UTS 不允许直接声明对象字面量类型，导致打包安卓基座时编译失败；已提取为具名类型 `CurrentStackInfo` 并引用

## 0.5.0（2026-09-16）

### 新增

- **`AnimationPlugin` 导航窗口动画插件**（opt-in，`plugins: [AnimationPlugin]`）：
  - App / 小程序：透传 `animationType` / `animationDuration` 给 `uni.*` 原生导航 API（原生窗口动画）
  - H5：通过 Web Animations API（`element.animate`）播放进入 / 退出动画，无需 CSS `@keyframes`
  - 全局默认动画（`RouterOptions.animation`）+ 单次覆盖（`animationType` / `animationDuration`）
- **`RouterOptions.animation`**：全局默认导航动画配置 `{ type, duration }`
- **查询参数工具公开导出**：`queryInt()` / `queryNumber()` / `queryBool()` 由库入口直接导出，无需按相对路径导入
- 新增类型：`NavigationAnimation` / `AnimationType`，`RawLocation` 支持 `animationType` / `animationDuration` 可选字段

### 修复

- **H5 首次进入二级页面动画卡顿**：`onCompleteNavigation` 时 uni-app x H5 已将新页内容替换进
  `uni-page`，此时**同步应用动画起点样式 + 强制 reflow**，让新页渲染首帧即位于屏幕外，再于下一帧播放滑入动画——消除「内容原位闪现后再跳到屏幕外滑入」的割裂感；动画结束后清理内联起点样式，避免残留影响后续 back 退出动画
- **H5 返回动画不生效**：新增 `toExitType()` 映射（进入型 → 退出型动画），返回时先播完退出动画再真正 `navigateBack`

## 0.4.0（2026-09-13）

### 新增

- **`RouterLink` 组件公开导出**：基于 `useLink` 的声明式导航组件（`to` / `replace` / `relaunch`），从库入口直接导入
- **插件契约合规**：`RouterPlugin` / `PluginContext` 等类型由 `interface` 调整为 `type`，支持对象字面量直接赋值（规避 UTS 对象字面量不能赋给 interface 的编译约束）

### 变更（破坏性）

- **params 传递机制统一**：移除旧的 `__unixr_p_` 查询前缀编码（`encodeParamsToQuery` / `extractParamsFromQuery` 及相关常量），params 一律经 `ParamsPlugin`（`__params__`
  关联存储）跨页传递。旧格式 URL 不再恢复参数，携带 params 的导航须注册 `plugins: [ParamsPlugin]`

### 修复

- 全库消除 `undefined` 残留（统一 `== null` / `??` 窄化），修复 Web 端编译类型警告
- `RouterLink` 组件 css 合规（移除 `scoped` / `inline-block`，改用 flex 布局）

## 0.3.0（2026-09-11）

### 新增

- **`RouterOptions.paramsPersistent`**：配合 `ParamsPlugin` 将 params 默认持久化到 storage（跨刷新/重进保留），默认 `false`
- **插件体系完善**：`PluginContext` 提供完整导航 hook（`onEnrichLocation` / `onAfterResolve` / `onPrepareNavigation` / `onCompleteNavigation` / `onNavigationAbort` / `onRouteSync` / `onAppInstall`）与 `router` /
  `paramsManager` / `hasPlugin`，支持自定义插件
- **`Router.guardRoute()` / `onRouteChange`**：冷启动守卫补跑与路由变化监听能力补齐

### 修复

- 导航成功后写入 `currentRoute` 前剔除内部 `__params__` key（`stripInternalKeys`），避免内部 key 暴露给用户
- `ParamsPlugin` 的 `afterResolve` 在无 `__params__` 键时对 `Map.get` 返回值（`null`）做空值防护，修复可能中断导航链路的崩溃

## 0.2.0（2026-09-09）

### 新增

- **uni API 拦截（opt-in）**：新增 `RouterOptions.interceptUniApi` 配置，拦截 `uni.navigateTo` / `redirectTo` / `switchTab` / `reLaunch` / `navigateBack` 的直接调用，转由 `router.*` 走完整守卫链，守卫下沉到 uni
  API 层；内置调用去重（计数器），并针对 H5 平台 `switchTab` 做放行 + 状态同步的特殊处理

### 修复

- 重复导航判定优化：`path + query + params + hash` 全部一致才判 `DUPLICATED`，允许以不同参数重入当前页面（此前 params 差异会被忽略而误判重复）

## 0.1.0（2026-09-05）

首个可运行版本。

### 新增

- 核心：路由器创建、路由匹配（path / name 双索引）、严格模式
- 导航：`push` / `replace` / `relaunch` / `back`，自动识别 TabBar
- 守卫：`beforeEach` / `beforeResolve` / `afterEach` / `beforeEnter` / 组件内守卫（`onBeforeRouteLeave` / `onBeforeRouteUpdate` / `onBeforeRouteEnter`）
- 组合式 API：`useRouter` / `useRoute` / `useLink`
- 状态同步：`syncRoute`，基于 `getCurrentPages()` 的响应式 `currentRoute`
- 错误体系：`RouterError` / `NavigationFailure` / `isNavigationFailure` / `RouterErrorCode`
- 双模式：UTS 源分发，web / 小程序 → JS，Android → Kotlin，iOS → Swift
