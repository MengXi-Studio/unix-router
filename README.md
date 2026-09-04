# unix-router

为 **uni-app x** 提供的、**功能对标 vue-router 4**（基于 .uvue 与 .uts / UTS 源分发）的路由库。

- 技术根基：[uni-app x](https://doc.dcloud.net.cn/uni-app-x/)
- 架构参考：[uni-router](https://github.com/MengXi-Studio/uni-router)（仅参考其 monorepo 设计）
- 核心语言：**UTS**（`.uts`），VDOM 模式 → Kotlin/Swift 原生，蒸汽模式 / Web / 小程序 → JS，双模式兼容。

## monorepo 结构

| 包 | 说明 |
|---|---|
| `packages/core` | 路由库核心（`.uts` 源码分发） |
| `packages/playground` | uni-app x 测试/演示工程（HBuilderX 打开即可运行，含 `pages/test` 功能自检页） |

## 核心定位

- **功能基准 = vue-router 4**：`createRouter`、路由匹配（path/name/query/params）、全部守卫（`beforeEach`/`beforeResolve`/`afterEach`/`beforeEnter`/组件内 `onBeforeRouteLeave` 等）、编程式导航（`push`/`replace`/`relaunch`/`back`）、`useRouter`/`useRoute`/`useLink`、`RouterLink` 组件、错误体系（`NavigationFailure`/`isNavigationFailure`）、`currentRoute` 响应式、`isReady`/`onError`/`onRouteChange`、`resolve`/`getRoutes`/`hasRoute`、`guardRoute`（冷启动守卫）、`strict` 严格模式、守卫重定向 + 深度上限、重复导航拦截。

## 快速使用

```uts
// router.config.uts
import type { RouteConfig } from '@meng-xi/unix-router'
export const routes: RouteConfig[] = [
	{ path: 'pages/index/index', name: 'index', meta: { title: '首页', isTab: true } },
	{ path: 'pages/guards/guards', name: 'guards', meta: { title: '守卫', requireAuth: true } }
]
```

```uts
// router.ts
import { createRouter } from '@meng-xi/unix-router'
import { routes } from './router.config'
export const router = createRouter({ routes, strict: true })

router.beforeEach((to, from) => {
	if (to.meta.requireAuth && !loggedIn) return { name: 'login' }
	return true
})
```

```uts
// main.ts
import { createSSRApp } from 'vue'
import App from './App.uvue'
import { router } from './router'
export function createApp() {
	const app = createSSRApp(App)
	app.use(router)
	return { app }
}
```

```uts
// 页面内使用
import { useRouter, useRoute } from '@meng-xi/unix-router'
const router = useRouter()
const route = useRoute() // 响应式
router.push({ name: 'guards', query: new Map([['a','1']]), params: new Map([['from','首页']]) })
```

## 分发形态

`packages/core` 为 **UTS 源分发**（不做 tsup 通用打包）。依据 uni-app x 官方 UTS 编译器规则：

- Web / 小程序：可引用 ts/js，`uts2js` 只处理 `.uts`；
- Android（非 JS 目标）：**仅可引用 ts 文件且当作 UTS 处理**，纯 JS 产物无法被引用。

因此 core 以 `.uts` 源码随包分发，由 uni-app x 编译器按 `web/mp→JS、Android→Kotlin、iOS→Swift` 平台编译。App 原生推荐以 `uni_modules/<name>/utssdk` 形式分发；`node_modules` 下 `.uts` 的实际处理建议在目标工程先行验证。

## 运行与验证

1. 用 **HBuilderX** 打开 `packages/playground`，运行到 H5 / 微信小程序，或打包到 App 原生自测。
2. 首页的「功能自检」`pages/test/test.uvue` 会程序化断言核心逻辑并输出 PASS/FAIL。
3. CLI 方式（预留）：`pnpm dev:h5`、`pnpm build:mp-weixin`（需自行配置 uni-app x 编译链）。

## 与 vue-router 4 的差异（受限于 uni-app x 静态页面模型）

| 能力 | 说明 |
|---|---|
| `addRoute` / `removeRoute` 动态路由 | 不支持（未注册页面编译期被忽略），不在 API 暴露 |
| 嵌套路由 `children` | 不支持（扁平页面模型）；分包 `subPackages` 仅作资源分包 |
| 命名视图 / `RouterView` | 不支持（无页内渲染占位） |
| `scrollBehavior` | 不支持（滚动由 uni-app 原生管理） |
| `go(n)` / hash 路由 | 语义受限；`back(delta)` 对应 `uni.navigateBack` |
| `route.params` 对象参数 | 通过"查询编码"在页面 URL 间传递（`__p_` 前缀保留键） |
| `onBeforeRouteEnter/Update` | 静态页模型下触发受限（页面每次导航新建实例） |

## 脚本

- `pnpm install`：安装根依赖并链接 workspace
- `pnpm dev:playground:h5` / `pnpm build:playground:mp-weixin`：playground 构建（需编译链）

## License

MIT