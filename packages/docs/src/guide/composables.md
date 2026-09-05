# 组合式 API

unix-router 提供 Vue 3 组合式 API，与 `.uvue` 的 `<script setup>` 无缝集成。

## useRouter()

返回路由器实例：

```ts
import { useRouter } from '@meng-xi/unix-router'
const router = useRouter()

router.push('/pages/about/about')
router.back()
```

## useRoute()

返回**响应式**当前路由：

```ts
import { useRoute } from '@meng-xi/unix-router'
const route = useRoute()

console.log(route.path) // 当前路径
console.log(route.query.get('keyword')) // 查询
console.log(route.params.get('id')) // 参数
```

`useRoute` 在路由器导航与状态同步（`syncRoute`）时会自动更新。

## useLink()

返回响应式链接状态（声明式导航）：

```ts
import { useLink } from '@meng-xi/unix-router'
import { computed } from 'vue'

const link = useLink({ to: { name: 'about' }, replace: false })

link.href // ComputedRef<string> 完整路径
link.isActive // ComputedRef<boolean>
link.isExactActive // ComputedRef<boolean>
link.navigate() // 执行导航
```

## 组件内守卫

- `onBeforeRouteLeave` — 离开当前页面时触发
- `onBeforeRouteUpdate` — 更新时触发（静态页模型下极少）
- `onBeforeRouteEnter` — 进入时触发（效果有限）

详见[路由守卫](./guards)。

## 生命周期中的使用

在 `onLoad` / `onShow` 中如需读取路由，可调用 `router.syncRoute()` 手动同步后再读取：

```ts
onLoad(options => {
	router.syncRoute()
	const route = useRoute()
	// route.query 来自 onLoad options
})
```