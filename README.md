**中文** | [English](./README-en.md)

<div align="center">
  <a href="https://github.com/MengXi-Studio/unix-router">
    <img alt="梦曦工作室 Logo" width="215" src="https://github.com/MengXi-Studio/unix-router/blob/master/packages/docs/src/public/logo.png">
  </a>
  <a href="https://github.com/MengXi-Studio/unix-router">
    <img alt="微信公众号 二维码" width="215" src="https://github.com/MengXi-Studio/unix-router/blob/master/packages/docs/src/public/QR_code.jpg">
  </a>

  <h1>@meng-xi/unix-router</h1>
  <p>为 uni-app x 提供类似 vue-router 风格的路由管理系统（UTS 编写，双模式兼容）</p>

[![license](https://img.shields.io/github/license/MengXi-Studio/unix-router.svg)](./LICENSE) [![npm](https://img.shields.io/npm/v/@meng-xi/unix-router?color=blue)](https://www.npmjs.com/package/@meng-xi/unix-router)
![npm](https://img.shields.io/npm/dt/@meng-xi/unix-router?color=green)
</div>

## 为什么是 unix-router

在 uni-app x 里手写路由，绕不开这些事：`uni.navigateTo` 直调不受业务规则约束、tabBar 页与普通页要手动区分导航 API、复杂参数只能拼 URL 字符串、跨页回传要自己搭 `uni.$on` 通道，H5 刷新、返回键、deeplink 冷启动这些边界场景还要逐个踩坑。unix-router 把这些收敛成一个 vue-router 心智的路由器：

- **用过 vue-router？** `createRouter` / `push` / `back`、全局与组件内守卫、`useRouter` / `useRoute` 组合式 API 一脉相承，心智零迁移，看一眼 API 就能上手；
- **多端发布？** 一套 UTS 源码编译到 App（Android / iOS / HarmonyOS）、Web、小程序；页面栈即历史、`meta.isTab` 自动切换 `switchTab`、跨端空值与 UTS 强类型差异由库内确定性消化，不用逐端排查；
- **在意业务规则？** 守卫链可拦截一切导航：`beforeEach` 重定向登录态，`interceptUniApi` 开启后连绕过路由器的 `uni.navigateTo` 直调也走守卫链，业务规则无旁路。

底层只依赖 `uni.*` 原生导航 API 与 `getCurrentPages()` 页面栈，不重造路由栈；UTS 源码分发（Web / 小程序 → JS，Android → Kotlin，iOS → Swift），由 uni-app x 编译链现场编译。

## 特性

- **vue-router 风格 API** - `createRouter`、`push` / `replace` / `relaunch` / `back`，按 `meta.isTab` 自动切换 `switchTab`，重复导航自动拒绝（`DUPLICATED`），并发导航自动排队；页面栈不足时返回自动兜底回入口页（冷启动 / H5 刷新场景）
- **完整守卫体系** - 全局 `beforeEach` / `beforeResolve` / `afterEach`，路由独享 `beforeEnter`，组件内 `onBeforeRouteLeave` / `onBeforeRouteUpdate` / `onBeforeRouteEnter`；支持可控重定向、守卫超时保护（`guardTimeout`），冷启动（H5 直达 / deeplink）经 `guardRoute()` 补跑守卫链
- **基于文件的路由生成（构建期 opt-in）** - 从 `@meng-xi/unix-router/vite-plugin` 引入 vite 插件，页面旁声明 `defineUniPage` 宏或 `<route-config>` 块，构建期自动生成 `pages.json` 与路由表（`routes.gen.uts`），消除 path / title / isTab 双份手工维护；`routeGen` / `pagesGen` / `routesGen` 三个独立插件可单独或组合注册
- **跨页数据传递** - `params`（`Map<string,string>`）经 **ParamsPlugin**（`__params__` 关联存储）跨页传递，不暴露明文键名，可选持久化到 storage；`events` 经 **EventsPlugin** 对齐官方事件通道语义；`query` 为 `Map` 并配 `queryInt()` / `queryNumber()` / `queryBool()` 便捷解析
- **uni API 拦截（opt-in）** - `interceptUniApi: true` 时，绕过路由器直调 `uni.navigateTo` / `switchTab` 等原生导航同样被拦截转入守卫链，业务规则下沉到 uni API 层
- **导航窗口动画（opt-in）** - **AnimationPlugin**：App 端透传原生 `animationType` / `animationDuration`，H5 端以 WAAPI 播放进入 / 退出动画，支持全局默认（`animation` 选项）与单次覆盖
- **开箱即用的配套** - `useLink()` 与 **`RouterLink`** 组件、`onShow` 自动状态同步（`currentRoute` 响应式，非路由器导航自动对齐）、`RouterError` / `isNavigationFailure()` 错误体系、命名路由 + 严格模式

## 安装

```bash
pnpm add @meng-xi/unix-router
```

> UTS 源分发（Web / 小程序 → JS，Android → Kotlin，iOS → Swift），由 uni-app x 编译链现场编译；App 原生推荐以 `uni_modules/<name>/utssdk` 形式分发。

## 快速开始

### 1. 创建并挂载路由器

```typescript
// src/router.ts
import { createRouter } from '@meng-xi/unix-router'
import type { RouteConfig } from '@meng-xi/unix-router'

export const routes: RouteConfig[] = [
  { path: 'pages/index/index', name: 'home', meta: { title: '首页', isTab: true } },
  { path: 'pages/about/about', name: 'about', meta: { title: '关于', requireAuth: true } }
]

export const router = createRouter({ routes, strict: true })

// main.ts
import { createSSRApp } from 'vue'
import App from './App.uvue'
import { router } from './router'

export function createApp() {
  const app = createSSRApp(App)
  app.use(router) // 注入全局 mixin，onShow 时自动 syncRoute()
  return { app }
}
```

### 2. 导航、守卫与响应式路由

```typescript
import { useRouter, useRoute } from '@meng-xi/unix-router'

const router = useRouter()

// 四种导航方式，均返回目标路由位置（NavigationResult = RouteLocation）
await router.push({ name: 'about', query: new Map([['a', '1']]) })
await router.replace({ path: '/pages/detail/detail', params: new Map([['from', '首页']]) })
await router.relaunch('/pages/index/index')
await router.back(1)  // 返回上一页；栈深不足时自动兜底回首个 tabBar 页

// 全局守卫：requireAuth 未登录 → 重定向登录页
router.beforeEach((to, from) => {
  if (to.meta.requireAuth && !isLoggedIn()) {
    return { name: 'login' } // 重定向
  }
  return true // null / true 表示放行
})

// 响应式当前路由，脚本 / 模板中可直接访问字段
const route = useRoute()
route.path            // /pages/about/about
route.query.get('id') // '1'
```

> **注意**：`back(delta)` 必须显式传参（如 `back(1)`）。UTS 接口方法签名不支持默认值，App 端（Kotlin）可选参数会生成必填形参，省略实参将导致 `No value passed for parameter 'delta'` 编译错误。

> 目标路由若在 `meta.isTab` 中声明为 TabBar 页面，`push` / `replace` / `relaunch` 会自动改用 `uni.switchTab`。

## 路由选项

`createRouter` 支持以下常用选项：

| 选项          | 类型            | 默认值   | 说明                                                       |
| ------------- | --------------- | -------- | ---------------------------------------------------------- |
| `routes`      | `RouteConfig[]` | -        | 路由配置，需与 `pages.json` 声明一致                       |
| `strict`      | `boolean`       | `true`   | 严格模式，未匹配的命名路由抛出 `RouterError`               |
| `guardTimeout`| `number`        | `10000`  | 守卫超时（ms），超时警告并自动中止导航，设 `0` 关闭        |
| `readyTimeout`| `number`        | `0`      | 就绪超时（ms），防止 `await router.isReady()` 挂起          |
| `plugins`     | `RouterPlugin[]`| -        | 可选插件列表，**传入实例**，如 `[new ParamsPlugin()]` / `[new InterceptorPlugin()]` / `[new AnimationPlugin()]` / `[new EventsPlugin()]` |
| `paramsPersistent` | `boolean`  | `false`  | 是否默认将 `params` 持久化到 storage（需配合 `ParamsPlugin`） |
| `interceptUniApi` | `boolean`  | `false`  | opt-in：拦截 `uni.*` 原生导航，使直调也走守卫链（受运行时版本支持：Web 4.0 / 微信 4.41 / Android 3.97 / iOS 4.11 / Harmony 4.61） |
| `animation`   | `NavigationAnimation` | - | 全局默认导航动画 `{ type, duration }`（需配合 `AnimationPlugin` 才生效） |

```typescript
// 开启后，业务中直接调用 uni.navigateTo('/pages/xxx') 等原生导航同样会触发守卫链
const router = createRouter({ routes, interceptUniApi: true })

// 可选：注册插件 + 全局默认动画
const router2 = createRouter({
  routes,
  plugins: [new ParamsPlugin(), new InterceptorPlugin(), new AnimationPlugin()],
  animation: { type: 'slide-in-right', duration: 300 }
})
```

## 体验 Demo

示例工程 [packages/playground](https://github.com/MengXi-Studio/unix-router/tree/master/packages/playground)（覆盖导航、守卫、参数传递、事件通信、RouterLink、导航动画等功能）。

**Android 真机体验**：[下载体验版 App（APK）](https://mp-b8b8347a-48e9-434d-8302-3e9d99c2cb01.cdn.bspapp.com/cloudstorage/app-build-pkg/1790413072682-__UNI__B6A50A8_1790413055611.apk)——云打包产物，覆盖真实原生渲染效果；安装包随版本更新重新打包，链接以最新发版说明为准。

## 交流与支持

想了解设计思路与实现原理（页面栈即历史的路由模型、UTS 强类型下的路由器实现、基于文件的路径生成等）？公众号「**梦曦工作室**」有系列解析，扫码关注：

<p align="center">
  <img alt="微信公众号 二维码" width="180" src="https://github.com/MengXi-Studio/unix-router/blob/master/packages/docs/src/public/QR_code.jpg">
</p>

- 🐛 [Issues](https://github.com/MengXi-Studio/unix-router/issues) / 💬 [Discussions](https://github.com/MengXi-Studio/unix-router/discussions)

## 文档

📖 从**入门到精通**的完整文档（🟢 入门 → 🟡 进阶 → 🔴 精通，中英双语）：

**[https://mengxi-studio.github.io/unix-router/](https://mengxi-studio.github.io/unix-router/)**

阅读建议：先看[介绍与学习路径](https://mengxi-studio.github.io/unix-router/guide/introduction.html)，再按[快速开始](https://mengxi-studio.github.io/unix-router/guide/getting-started.html) → [路由配置](https://mengxi-studio.github.io/unix-router/guide/route-config.html) → [路由导航](https://mengxi-studio.github.io/unix-router/guide/navigation.html) 的顺序上手，进阶看[守卫](https://mengxi-studio.github.io/unix-router/guide/guards.html)，最后用[完整实战](https://mengxi-studio.github.io/unix-router/guide/recipes.html)收尾。

## 更新日志

📝 **[https://github.com/MengXi-Studio/unix-router/blob/master/packages/docs/src/changelog.md](https://github.com/MengXi-Studio/unix-router/blob/master/packages/docs/src/changelog.md)**

## License

[MIT](./LICENSE)
