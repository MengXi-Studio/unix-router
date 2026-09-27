---
layout: home

title: '@meng-xi/unix-router'
titleTemplate: Unix Router 路由管理

hero:
  name: '@meng-xi/unix-router'
  text: uni-app x 路由管理
  tagline: 为 uni-app x 提供类似 vue-router 风格的路由管理系统
  image:
    src: /logo.svg
    alt: Unix Router
  actions:
    - theme: brand
      text: 快速开始
      link: /guide/getting-started
    - theme: alt
      text: 学习路径
      link: /guide/introduction
    - theme: alt
      text: GitHub
      link: https://github.com/MengXi-Studio/unix-router

features:
  - icon: 🧭
    title: 路由导航
    details: push / replace / relaunch / back 四种导航，meta.isTab 自动 switchTab，useLink / RouterLink 声明式导航；并发导航自动排队串行执行，重复导航拦截（DUPLICATED）
  - icon: 🛡️
    title: 路由守卫
    details: 全局 beforeEach / beforeResolve / afterEach + 路由独享 beforeEnter + 组件内守卫，返回值风格重定向（深度上限 10），guardTimeout 超时保护，guardRoute 冷启动补执行守卫
  - icon: 📦
    title: 参数与查询增强
    details: query 直接进 URL；params 经 ParamsPlugin（__params__ 内部 key 通道）跨页传递、可持久化；queryInt / queryNumber / queryBool 类型化读取
  - icon: 📡
    title: 页面事件通信
    details: EventsPlugin 补齐 uni-app x 缺失的 events 能力：push 携带监听表、被打开页 useOpenerEventChannel 回传/接收，另附全局 eventBus
  - icon: 🎬
    title: 跨端导航动画
    details: AnimationPlugin 在 App 端透传原生 animationType / animationDuration（官方仅 App 支持），H5 用 Web Animations API 实现，back 自动映射退出型动画
  - icon: 🔄
    title: 路由状态自动同步
    details: install 在 H5 注册全局 mixin（onShow 自动 syncRoute），原生端建议页面 onShow 自行调用，onRouteChange 监听路由变化，页面栈与非路由器导航始终对齐
  - icon: ⚠️
    title: 可预测的错误体系
    details: RouterError / NavigationFailure 共 7 类错误码，导航失败一律 reject，onError 全局捕获 + isNavigationFailure 精准判断
  - icon: 🧩
    title: 核心精简 + 插件扩展
    details: 4 个内置插件（Params / Events / Animation / Interceptor）按需实例化注册，PluginContext 提供 8 个 hook 自定义扩展，未注册即抛 PLUGIN_REQUIRED 明确引导
  - icon: 💪
    title: UTS / TypeScript 优先
    details: 严格类型（Map 带泛型、无隐式转换），useRouter / useRoute / useLink / useOpenerEventChannel 全套组合式 API
---

## 快速上手路径

[介绍](/guide/introduction) → [快速开始](/guide/getting-started) → [路由配置](/guide/route-config) → [路由导航](/guide/navigation) → [路由守卫](/guide/guards) → [完整实战](/guide/recipes)
