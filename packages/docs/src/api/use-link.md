# useLink()

`useLink(options: UseLinkOptions): UseLinkReturn` 提供声明式导航的响应式状态与触发函数，适合自定义链接 / 菜单组件。[RouterLink 组件](./router-link)内部即基于它实现。

```ts
import { useLink } from '@meng-xi/unix-router'

const link = useLink({ to: 'pages/about/about' })
```

## 参数

`useLink(options: UseLinkOptions)`

| 选项 | 类型 | 默认值 | 说明 |
| --- | --- | --- | --- |
| `to` | `RouteLocationRaw` | — | 目标路由位置（字符串路径或对象） |
| `replace` | `boolean` | `false` | 是否使用 `replace` 导航 |
| `relaunch` | `boolean` | `false` | 是否使用 `relaunch` 导航 |

> `replace` 与 `relaunch` 同时为 `true` 时按 `relaunch` 优先处理。

## 返回值

返回 `UseLinkReturn`：

| 属性 | 类型 | 说明 |
| --- | --- | --- |
| `route` | `ComputedRef<RouteLocation>` | 解析后的目标路由位置（响应式） |
| `isActive` | `ComputedRef<boolean>` | 是否为当前激活路由（**路径前缀匹配**：相等或以 `目标path + '/'` 开头） |
| `isExactActive` | `ComputedRef<boolean>` | 是否为当前精确激活路由（页面路径相等） |
| `href` | `ComputedRef<string>` | 响应式目标 href（等于 `route.value.fullPath`） |
| `navigate` | `() => Promise<RouteLocation \| null>` | 执行导航（`relaunch` → `router.relaunch`，`replace` → `router.replace`，默认 `router.push`；`push` 返回目标位置，`replace` / `relaunch` 返回 `null`） |

## 自定义导航菜单组件示例

```vue
<script setup lang="uts">
import { useLink } from '@meng-xi/unix-router'

const props = defineProps({
	to: { type: String, default: '' },
	replace: { type: Boolean, default: false }
})

const link = useLink({ to: props.to, replace: props.replace })

const onClick = (): void => {
	link.navigate()
}
</script>

<template>
	<view :class="link.isActive.value ? 'menu-item active' : 'menu-item'" @click="onClick">
		<text>{{ link.href.value }}</text>
		<slot />
	</view>
</template>
```

::: tip
`isActive` 等是 `ComputedRef`，**在 `<script>` 脚本中访问需带 `.value`**（模板中可自动解包）。导航失败由 Promise reject 递出，可在 `navigate()` 调用处 `catch`，或统一交给 `router.onError` 处理。
:::

## 与 RouterLink 的关系

`<RouterLink>` 是 `useLink({ to, replace, relaunch })` 的组件化封装：点击时调用 `link.navigate()`。需要自定义交互（菜单激活态、复杂样式等）时，直接用 `useLink()` 自行实现。

## 相关 API

- [RouterLink 组件](./router-link)
- [组合式 API 指南](../guide/composables)
