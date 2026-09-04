---
layout: home

title: '@meng-xi/unix-router'
titleTemplate: Unix Router Routing

hero:
  name: '@meng-xi/unix-router'
  text: uni-app x Routing
  tagline: A vue-router-style routing system for uni-app x (written in UTS, dual-mode compatible)
  image:
    src: /logo.svg
    alt: Unix Router
  actions:
    - theme: brand
      text: Get Started
      link: /guide/getting-started
    - theme: alt
      text: Learn More
      link: /guide/introduction
    - theme: alt
      text: GitHub
      link: https://github.com/MengXi-Studio/unix-router

features:
  - icon: 🧭
    title: Route Navigation
    details: Four navigation modes — push / replace / relaunch / back, automatic TabBar detection (switchTab), and declarative RouterLink
  - icon: 🛡️
    title: Route Guards
    details: Global before/resolve/after guards + route-local beforeEnter + in-component guards, guardRoute cold-start re-execution, redirect and depth-limit protection
  - icon: 📦
    title: Enhanced Params & Query
    details: Object params passed across pages via query encoding, with queryInt / queryNumber / queryBool handy parsing helpers
  - icon: 🔄
    title: Automatic Route State Sync
    details: install injects a global Mixin that auto-calls syncRoute(), keeping the page stack and non-router navigations in sync
  - icon: ⚠️
    title: Error Handling
    details: A complete RouterError / NavigationFailure system, global onError capture, precise isNavigationFailure() checks, and duplicate-navigation interception
  - icon: 💪
    title: TypeScript / UTS First
    details: Full types and a Composition API — useRouter / useRoute / useLink / onBeforeRouteLeave, seamlessly integrated with Vue 3
---

## Quick Start

```bash
# Install (UTS source distribution, compiled per platform by the uni-app x toolchain)
pnpm add @meng-xi/unix-router
```

```uts
import { createRouter } from '@meng-xi/unix-router'

const router = createRouter({
	routes: [
		{ path: 'pages/index/index', name: 'home', meta: { title: 'Home', isTab: true } },
		{ path: 'pages/guards/guards', name: 'guards', meta: { title: 'Guards', requireAuth: true } }
	],
	strict: true
})

router.beforeEach((to, from) => {
	if (to.meta.requireAuth && !loggedIn) return { name: 'login' }
	return true
})

app.use(router)
await router.push('/pages/index/index')
```