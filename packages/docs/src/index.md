---
layout: home

title: '@meng-xi/unix-router'
titleTemplate: Unix Router 路由管理

hero:
  name: '@meng-xi/unix-router'
  text: uni-app x 路由管理
  tagline: 为 uni-app x 提供类似 vue-router 风格的路由管理系统（UTS 编写，双模式兼容）
  image:
    src: /logo.svg
    alt: Unix Router
  actions:
    - theme: brand
      text: 快速开始
      link: /guide/getting-started
    - theme: alt
      text: 了解更多
      link: /guide/introduction
    - theme: alt
      text: GitHub
      link: https://github.com/MengXi-Studio/unix-router

features:
  - icon: 🧭
    title: 路由导航
    details: push / replace / relaunch / back 四种导航，自动识别 TabBar（switchTab），useLink 声明式导航
  - icon: 🛡️
    title: 路由守卫
    details: 全局前置/解析/后置守卫 + 路由独享 beforeEnter + 组件内守卫，guardRoute 冷启动补执行，重定向与深度上限保护
  - icon: 📦
    title: 参数与查询增强
    details: params 对象参数经查询编码跨页传递，queryInt / queryNumber / queryBool 便捷解析
  - icon: 🔄
    title: 路由状态自动同步
    details: install 注入全局 Mixin 自动 syncRoute()，页面栈与非路由器导航自动对齐
  - icon: ⚠️
    title: 错误处理
    details: RouterError / NavigationFailure 完整体系，onError 全局捕获，isNavigationFailure() 精准判断，重复导航拦截
  - icon: 💪
    title: TypeScript / UTS 优先
    details: 完整类型与组合式 API，useRouter / useRoute / useLink / onBeforeRouteLeave，与 Vue 3 无缝集成
---
