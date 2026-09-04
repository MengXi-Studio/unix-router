# 导航流程原理

理解 unix-router 的一次导航从触发到完成发生了什么。

## 流程图

```mermaid
flowchart TD
	A[router.push / replace / relaunch] --> B{前一次导航?.}
	B -- 是 --> WAIT[等待完成]
	WAIT --> C
	B -- 否 --> C[matcher.resolve 解析目标]
	C --> D{重复导航? push 相同地址}
	D -- 是 --> DUP[抛 NAVIGATION_DUPLICATED]
	D -- 否 --> E[runBeforeEach 全局前置守卫]
	E --> F{结果?}
	F -- 中止/取消 --> ABORT[触发 onError + afterEach(fail)]
	F -- 重定向 --> REDIRECT{深度超限?}
	REDIRECT -- 是 --> CANCEL[取消导航]
	REDIRECT -- 否 --> C
	F -- 放行 --> G[runBeforeEnter 路由独享守卫]
	G --> H[runBeforeResolve 全局解析守卫]
	H --> I[调用 uni.* 导航 API]
	I --> J{成功?}
	J -- 失败 --> APIERR[回退 + 触发保护]
	J -- 成功 --> K[更新 currentRoute + runAfterEach]
```

## 执行顺序（前向导航 push / replace / relaunch）

1. **并发排队**：等待上一条导航完成。
2. **解析目标**：`matcher.resolve(location)`，产出 `RouteLocation`。
3. **重复检测**：`push` 到与当前相同地址 → `NAVIGATION_DUPLICATED`。
4. **全局前置** `beforeEach`
5. **路由独享** `beforeEnter`（若配置）
6. **全局解析** `beforeResolve`
7. **原生导航**：按 `meta.isTab` 分发 `navigateTo/redirectTo/reLaunch/switchTab`
8. **成功后置** `afterEach`，并更新 `currentRoute`

任一守卫返回 `false`/`Error`/重定向都会中止或改道。重定向深度超过
`MAX_REDIRECT_DEPTH` 时取消导航。

## 返回流程 back

`router.back(delta)` 会更短：

1. 等待前一次导航完成
2. 从页面栈计算目标页
3. `beforeEach` → `beforeResolve`
4. `uni.navigateBack(delta)`
5. 同步 `currentRoute` 并触发 `afterEach`

## 状态同步 syncRoute

uni-app x 的物理返回键、tab 切换等**不经过路由器**。`install` 时注入全局 mixin
在页面 `onShow` 自动调用 `router.syncRoute()`，从页面栈重建 `currentRoute`，
确保 `useRoute()` 始终反映真实页面。

## 冷启动 guardRoute

用户直接进入页面（H5 URL / 场景值 / deeplink）时页面已加载，守卫未执行。
`guardRoute()` 对当前路由补跑守卫链：放行即返回；重定向则跳转；
中止则触发 `onAbort`，可转跳安全页。