---
layout: home

title: '@meng-xi/unix-router'
titleTemplate: 'Unix Router — Routing for uni-app x'

hero:
  name: '@meng-xi/unix-router'
  text: Routing for uni-app x
  tagline: A vue-router-style routing management system for uni-app x
  image:
    src: /logo.svg
    alt: Unix Router
  actions:
    - theme: brand
      text: Get Started
      link: /en/guide/getting-started
    - theme: alt
      text: Learning Path
      link: /en/guide/introduction
    - theme: alt
      text: GitHub
      link: https://github.com/MengXi-Studio/unix-router

features:
  - icon: 🧭
    title: Route Navigation
    details: Four navigation modes — push / replace / relaunch / back; meta.isTab automatically uses switchTab; useLink / RouterLink for declarative navigation; concurrent navigations are queued and executed serially, duplicate navigations are rejected (DUPLICATED)
  - icon: 🛡️
    title: Route Guards
    details: Global beforeEach / beforeResolve / afterEach + route-local beforeEnter + in-component guards; return-value-style redirects (depth limit 10); guardTimeout timeout protection; guardRoute re-runs the guard chain on cold start
  - icon: 📦
    title: Enhanced Params & Query
    details: query goes straight into the URL; params travel across pages via ParamsPlugin (the __params__ internal key channel) with optional persistence; queryInt / queryNumber / queryBool for typed reads
  - icon: 📡
    title: Page Event Communication
    details: EventsPlugin fills in the events capability missing from uni-app x — pass a listener map in push, the opened page uses useOpenerEventChannel to emit back / receive, plus a global eventBus
  - icon: 🎬
    title: Cross-Platform Navigation Animations
    details: AnimationPlugin passes through native animationType / animationDuration on App / Mini Program, implements H5 with the Web Animations API, and back automatically maps to exit-style animations
  - icon: 🔄
    title: Automatic Route State Sync
    details: install registers a global mixin on H5 (onShow auto-syncs via syncRoute); on native platforms pages are advised to call it themselves in onShow; onRouteChange listens for route changes, keeping the page stack and non-router navigations always aligned
  - icon: ⚠️
    title: Predictable Error System
    details: RouterError / NavigationFailure with 7 error codes; navigation failures always reject; onError for global capture + isNavigationFailure for precise checks
  - icon: 🧩
    title: Lean Core + Plugin Extensions
    details: 4 built-in plugins (Params / Events / Animation / Interceptor) instantiated and registered on demand; PluginContext provides 8 hooks for custom extensions; using params without the plugin rejects with PLUGIN_REQUIRED and clear guidance
  - icon: 💪
    title: UTS / TypeScript First
    details: Strict typing (generic Maps, no implicit conversions); the full composition API set — useRouter / useRoute / useLink / useOpenerEventChannel
---

## Quick Start Path

[Introduction](/en/guide/introduction) → [Getting Started](/en/guide/getting-started) → [Route Configuration](/en/guide/route-config) → [Navigation](/en/guide/navigation) → [Route Guards](/en/guide/guards) → [Recipes](/en/guide/recipes)
