# 导航流程原理

理解 unix-router 的一次导航从触发到完成发生了什么。本章基于 [插件系统](./plugins) 的阶段划分，给出完整的内部时序。

## 前向导航完整时序（push / replace / relaunch）

以 `router.push(location)` 为例，`replace` / `relaunch` 仅换用不同的 uni API：

1. **并发排队**：若存在进行中的导航（`pendingNavigation`），先等待其完成（失败也被视为完成），保证同一时刻只有一条导航在执行。
2. **PLUGIN_REQUIRED 预检**：使用 `params` 但未注册 `ParamsPlugin`、或使用 `events` 但未注册 `EventsPlugin` 时，直接失败并抛 `PLUGIN_REQUIRED`，不进入后续任何阶段。
3. **enrichLocation hooks**：插件在 resolve 前**增强原始路由位置**（如 `ParamsPlugin` 把 params 存入管理器并注入 `__params__` 内部 key，`EventsPlugin` 注入 `__evt__` 通道 key）。
4. **`matcher.resolve`**：解析增强后的位置，产出完整 `RouteLocation`（即 `to`）。strict 模式（默认）下命名路由未匹配抛 `RouterError ROUTE_NOT_FOUND`。
5. **afterResolve hooks**：从增强后的位置中**提取插件数据**写入 `pluginData`（如按 `__params__` key 取回 params）。
6. **DUPLICATED 检测**：仅 `push` 检测——目标与当前路由的 path + query + params + hash 完全一致时抛 `DUPLICATED`（先执行 abort hooks 清理）。任一字段不同均视为新导航；`replace` / `relaunch` 不检测，可重入当前页。
7. **`beforeEach`** 全局前置守卫。
8. **`beforeEnter`** 路由独享守卫（若目标路由配置了）。
9. **`beforeResolve`** 全局解析守卫。
10. **重定向处理**：以上任一守卫返回重定向时，由 `handleGuardResult` 递归处理——对重定向目标**重建 enrichLocation / afterResolve**（插件数据可复用），再从头走守卫链；重定向模式缺省沿用原导航方式；深度上限 `MAX_REDIRECT_DEPTH`（10），超过则 `CANCELLED`。
11. **prepareNavigation hooks**：uni API 调用前，插件修改导航 URL 的 query 与选项（`NavigationPrepareContext.query` / `options` 可变）。
12. **beforeNavigation hooks**：真正调用 uni API 前执行，**可异步**且**串行**（如 H5 端 AnimationPlugin 在此等待退出动画播完）。
13. **调用 uni API**：按 `meta.isTab` 与导航模式分发——`push` → `navigateTo` / `switchTab`；`replace` → `redirectTo` / `switchTab`；`relaunch` → `reLaunch` / `switchTab`（tabBar 页不携带 query）。
14. **轮询页面栈顶确认**：以页面栈（`getCurrentPages`）为唯一真实来源，轮询等待目标页成为栈顶；超时（500ms）未确认 → `NAVIGATION_API_ERROR`。
15. **剥离内部 key**：从 `to.query` 中移除 `__params__` / `__evt__` 等插件内部 key 并重算 `fullPath`，不暴露给用户。
16. **更新状态**：`setCurrentRoute` + `setGlobalCurrentRoute`，`useRoute()` / `currentRoute` 响应式更新。
17. **completeNavigation hooks**：插件扩展导航结果（`ctx.result` 可变）。
18. **`afterEach(to, from, null)`** 后置守卫，`onRouteChange` 监听同步收到通知。
19. **返回目标**：Promise resolve 为目标 `RouteLocation`（`NavigationResult`，即剥离内部 key 后的 `to`）。

### 失败路径

第 3～14 步中**任一环节失败**（守卫中止 / 取消、uni API 报错、栈顶确认超时、重定向深度超限）：

```
onNavigationAbort（插件清理，异常被吞掉）
  → afterEach(to, from, failure)
  → onError(failure, to, from)
  → Promise reject（导航失败一律 reject，不会 resolve）
```

## 返回流程 back

`router.back(delta)` 更短，**不经过插件的 enrich / afterResolve**（pluginData 为空）：

1. 并发排队：等待进行中的导航完成。
2. 校验 `delta`：默认 `1`（传 `null` 同样按 1 处理）；非正整数 → `ABORTED`；页面栈不足（栈长 < 2 或 `delta >= 栈长`）→ `CANCELLED`。
3. 从页面栈计算出目标页并解析为 `to`。
4. `beforeEach` → `beforeResolve`（守卫可重定向，重定向按真实导航处理）。
5. `prepareNavigation` → `beforeNavigation` hooks（`mode: 'back'`，如播放退出动画后）。
6. `uni.navigateBack(delta)` → `setCurrentRoute(to)` → `afterEach(to, from, null)` → resolve `to`。

## 状态同步 syncRoute

uni-app x 的物理返回键、侧滑、tab 切换等**不经过路由器**，路由状态会与页面栈脱节。`syncRoute()` 按页面栈重建状态：

1. 解析页面栈顶页（`history.resolveCurrent()`），得到 path / query / params / meta 等。
2. 执行 **routeSync hooks**：插件从 URL query 提取插件数据（如按 `__params__` key 重建 params）并剥离内部 key。
3. 重算 `fullPath`，构造新的 `RouteLocation`，经 `setCurrentRoute` 更新路由器内部 `currentRoute` 并触发 `onRouteChange` 监听（当前版本不回写 `useRoute()` 返回的全局对象，见[组合式 API](./composables#useroute)）。

同步触发时机：`createRouter` 初始化时（页面栈非空）、`router.install()` 注册的 `onShow` mixin（仅 H5）、以及手动调用（原生端建议在页面 `onShow` 自行调用）。

## 冷启动 guardRoute

用户直接进入某页面（H5 直达 URL、deeplink、扫码）时页面已加载，但守卫链从未执行。`guardRoute(location?, { onAbort? })` **只补跑 `beforeEach` 守卫链，不执行实际导航**：

1. 解析目标：传入 `location` 则解析之，否则取当前 `currentRoute`。
2. 执行 `beforeEach`：
   - **放行** → 返回目标 `RouteLocation`，流程结束。
   - **中止** → 触发 `onError` + `onAbort(failure)`，并 Promise reject。
   - **重定向** → 以重定向模式**真实导航**（缺省 `relaunch`，因为冷启动页面栈只有当前页，`push` / `replace` 语义不明）；重定向后进入完整导航流程（含守卫链）。

::: tip 与 guardRoute 配合的典型写法

```ts
router.isReady().then(() => {
	router.guardRoute(undefined, {
		onAbort: (failure) => {
			// 页面已加载无法阻止，跳转到安全页
			router.relaunch({ name: 'home' })
		}
	}).catch(() => {})
})
```

:::

## 设计说明：页面栈是唯一真实来源

- **uni API 的 success 回调不作为成功依据**：uni 导航 API 受理成功不代表目标页已入栈。路由器在调用 uni API 后**轮询页面栈**，确认目标页真实成为栈顶才记为成功，否则按 `NAVIGATION_API_ERROR` 失败处理。
- **`currentRoute` 始终从页面栈派生**：导航成功后写入的是栈顶确认过的目标；物理返回、tab 切换等系统行为由 `syncRoute()` 在 `onShow` 重新对齐。这保证 `router.currentRoute` 与 `onRouteChange` 监听不会与实际页面出现状态漂移（当前版本 `useRoute()` 返回的全局对象仅在路由器导航成功时回写，物理返回等场景需借助 `onRouteChange` 感知）。

## 你的代码分别在哪一步执行

| 你的代码 | 在哪一步 |
| --- | --- |
| `routes` 配置 | `matcher.resolve` 决定 `to` |
| 插件 `onEnrichLocation` / `onAfterResolve` | 守卫链之前 |
| `beforeEach` | 排队、预检、resolve、重复检测之后 |
| `beforeEnter` | 路由独享前置（`beforeEach` 之后） |
| `beforeResolve` | 真正导航前的最后一道闸 |
| 插件 `onPrepareNavigation` / `onBeforeNavigation` | uni API 调用前（后者可异步） |
| `uni.*` 原生导航 | 内部调度（按 `meta.isTab` 与模式分发） |
| 插件 `onCompleteNavigation` / `afterEach` / `onRouteChange` | 栈顶确认 + 状态更新后 |
| 插件 `onNavigationAbort` | 任一环节失败或中止时 |
| `syncRoute` | `onShow` / 初始化，从页面栈重建状态 |

## 下一步

- [插件系统](./plugins) — 各阶段 hook 的签名与注册方式
- [路由守卫](./guards) — 守卫返回值与重定向语义
- [错误处理](./error-handling) — 失败路径的错误码与处理
