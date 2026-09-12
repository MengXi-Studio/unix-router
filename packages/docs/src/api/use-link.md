# useLink()

`useLink()` 提供声明式导航的响应式状态与触发函数，适合自定义链接 / 菜单组件。

```ts
import { useLink } from '@meng-xi/unix-router'

const link = useLink({ to: 'pages/about/about' })
```

## 参数

`useLink(options: UseLinkOptions)`

| 选项 | 类型 | 默认值 | 说明 |
| --- | --- | --- | --- |
| `to` | `RouteLocationRaw` | — | 目标路由位置（字符串或对象） |
| `replace` | `boolean` | `false` | 是否使用 `replace` 导航 |
| `relaunch` | `boolean` | `false` | 是否使用 `relaunch` 导航 |

## 返回值

返回 `UseLinkReturn`：

| 属性 | 类型 | 说明 |
| --- | --- | --- |
| `route` | `ComputedRef<RouteLocation>` | 目标路由位置（响应式） |
| `isActive` | `ComputedRef<boolean>` | 是否为当前激活路由（路径前缀匹配） |
| `isExactActive` | `ComputedRef<boolean>` | 是否为当前精确激活路由（完整路径匹配） |
| `href` | `ComputedRef<string>` | 响应式目标 href（等于 `route.fullPath`） |
| `navigate` | `() => Promise<void \| RouteLocation>` | 执行导航（内部调用 `router.push` / `replace` / `relaunch`） |

## 自定义链接组件示例

```vue
<script setup lang="uts">
import { useLink } from '@meng-xi/unix-router'

const props = defineProps({
	to: { type: String, default: '' },
	replace: { type: Boolean, default: false }
})

const link = useLink({ to: props.to, replace: props.replace })

const onClick = () => {
	try {
		link.navigate()
	} catch (e) {
		// 导航失败由调用方或 router.onError 处理
	}
}
</script>

<template>
	<view :class="link.isActive ? 'active' : ''" @click="onClick">
		<slot />
	</view>
</template>
```

## 相关 API

- [RouterLink 组件](./router-link)
- [组合式 API 指南](../guide/composables)