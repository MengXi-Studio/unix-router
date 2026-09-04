# useRouter()

`useRouter()` 返回当前组件上下文中的 [Router 实例](./router-instance)。必须在组件 `setup` 中调用（对应 `vue-router` 的同名 API）。

```ts
import { useRouter } from '@meng-xi/unix-router'

const router = useRouter()
```

## 返回值

返回 `Router` 实例（与 `createRouter()` 返回值相同）。可调用其全部导航、守卫注册与状态查询方法。

## 在组件中使用

```vue
<script setup lang="uts">
import { useRouter } from '@meng-xi/unix-router'

const router = useRouter()

const goAbout = () => {
	router.push({ name: 'about', query: new Map([['from', 'home']]) })
}
</script>
```

## 注意事项

- 仅可在组件 `setup`（或 `script setup`）中调用
- 依赖 `app.use(router)` 已安装路由器，否则抛出 `SETUP_ERROR`
- 与全局导出的 `router` 是**同一实例**，`useRouter()` 只是更符合组合式风格、且无需手动导入的路由访问方式

## 相关 API

- [Router 实例](./router-instance)
- [useRoute()](./use-route)
- [useLink()](./use-link)