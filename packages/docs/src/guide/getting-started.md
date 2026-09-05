# 快速开始

用 5 步让 unix-router 跑起来。

## 1. 定义路由配置

创建 `router.config.ts`（路径须与 `pages.json` 注册一致）：

```ts
import type { RouteConfig } from '@meng-xi/unix-router'

export const routes: RouteConfig[] = [
	{ path: 'pages/index/index', name: 'home', meta: { title: '首页', isTab: true } },
	{ path: 'pages/about/about', name: 'about', meta: { title: '关于' } },
	{ path: 'pages/guards/guards', name: 'guards', meta: { title: '守卫', requireAuth: true } }
]
```

## 2. 创建路由器

```ts
import { createRouter } from '@meng-xi/unix-router'
import { routes } from './router.config'

export const router = createRouter({ routes, strict: true })
```

## 3. 安装到 Vue 应用

```ts
// main.ts
import { createSSRApp } from 'vue'
import App from './App.uvue'
import { router } from './router'

export function createApp() {
	const app = createSSRApp(App)
	app.use(router) // 安装：provide + $router/$route + 全局 mixin
	return { app }
}
```

## 4. 在页面中使用

```vue
<script setup lang="uts">
import { useRouter, useRoute } from '@meng-xi/unix-router'

const router = useRouter()
const route = useRoute() // 响应式当前路由

const go = () => {
	router.push({ name: 'about', query: new Map([['from', 'home']]) })
}
</script>
```

## 5. 添加守卫（按需）

```ts
router.beforeEach((to, from) => {
	if (to.meta.requireAuth && !isLoggedIn()) return { name: 'login' }
	return true
})
```

完成。现在可以在任何 `.uvue` 页面中调用 `router.push('/pages/about/about')`。

## 完整示例

仓库内 `packages/playground` 是一个可直接运行的 uni-app x 测试工程，覆盖导航、守卫、参数、错误处理与 `pages/test` 功能自检页，可作为参考模板。