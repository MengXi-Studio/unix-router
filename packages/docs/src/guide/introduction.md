# 介绍

`@meng-xi/unix-router` 是一个为 **uni-app x** 提供的路由管理库，API 风格完整对标 **vue-router 4**，但采用 uni-app x 的**静态页面模型**（`.uvue` 页面 + `pages.json` 注册 + `uni.*` 原生导航）。

## 为什么需要它

uni-app x 原生提供了 `uni.navigateTo / redirectTo / reLaunch / navigateBack / switchTab`，但当页面增多后，直接使用这些 API 会有明显痛点：

- **无路由表**：页面路径散落各处，无集中管理；缺少命名路由与类型提示。
- **无守卫**：无法在跳转前统一做登录校验、埋点等逻辑。
- **无路由状态**：没有统一的 `currentRoute` 响应式对象，难以在组合式 API 中追踪当前页。
- **参数传递繁琐**：对象类型参数难以跨页传递，查询解析需要手写。

unix-router 用一套与 vue-router 一致的 API 抹平这些差异。

## 核心能力一览

| 能力 | 说明 |
| --- | --- |
| 路由匹配 | path / name 双索引，字符串 / 对象 / 命名三种解析方式，严格模式 |
| 导航 | push / replace / relaunch / back，自动识别 TabBar |
| 守卫 | beforeEach / beforeResolve / afterEach / beforeEnter / onBeforeRouteLeave 等 |
| 组合式 API | useRouter / useRoute / useLink |
| 组件 | RouterLink（基于 useLink 的声明式导航） |
| 错误体系 | RouterError / NavigationFailure / isNavigationFailure |
| 扩展 | guardRoute 冷启动守卫、重复导航拦截、守卫重定向 + 深度上限 |

## 与 uni-router 的关系

本项目 **monorepo 结构参考** [uni-router](https://github.com/MengXi-Studio/uni-router)，但面向平台改为 **uni-app x**，核心语言采用 **UTS**（`.uts`），并**只复刻 vue-router 4 的功能**，不包含 uni-router 自身的插件/拦截器等扩展。

## 下一步

- 想立即使用？前往[安装](./installation)或[快速开始](./getting-started)。
- 想了解设计取舍？查看[与 vue-router 的差异](./differences)。