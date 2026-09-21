# RouterLink 组件

`<RouterLink>` 是基于 [useLink()](./use-link) 的声明式导航组件：点击即触发导航，提供可交互的导航入口。

> 说明：uni-app x 为静态页面模型，不存在 vue-router 的页内渲染占位（`<router-view>`），因此组件**仅承担「可交互导航入口」角色**，不渲染路由内容。

## 引入

从库入口直接导入使用：

```ts
import { RouterLink } from '@meng-xi/unix-router'
```

## 属性（props）

| 属性 | 类型 | 默认值 | 说明 |
| --- | --- | --- | --- |
| `to` | `String` | `''` | 目标路由路径（字符串） |
| `replace` | `Boolean` | `false` | 是否使用 `replace` 导航（当前页替换为目标页） |
| `relaunch` | `Boolean` | `false` | 是否使用 `relaunch` 导航（关闭所有页面再打开目标） |

::: warning 没有 active-class 等 vue-router 扩展 props
vue-router 的 `active-class`、`custom`、`exact-active-class` 等 props 在本组件中**不存在**。uni-app x 的静态页面模型无页内渲染占位，激活态等需求请基于 [useLink()](./use-link) 的 `isActive` / `isExactActive` 自行实现。
:::

`replace` 与 `relaunch` 同时为 `true` 时按 `relaunch` 优先处理（与 `useLink` 语义一致）。

## 基础用法

```vue
<script setup lang="uts">
import { RouterLink } from '@meng-xi/unix-router'
</script>

<template>
	<view class="page">
		<RouterLink to="pages/about/about">
			<text>关于</text>
		</RouterLink>
		<RouterLink to="pages/detail/detail" :replace="true">
			<text>替换导航</text>
		</RouterLink>
		<RouterLink to="pages/index/index" :relaunch="true">
			<text>重开导航</text>
		</RouterLink>
	</view>
</template>
```

点击组件触发内部 `useLink().navigate()` 执行导航；导航失败由 Promise reject 递出，统一交给 `router.onError` 或调用方处理。

## 与 useLink 的关系

组件内部是 `useLink({ to, replace, relaunch })` 的薄封装（点击时调用 `link.navigate()`）。若需要自定义交互（如菜单激活态、更多样式控制），直接用 [useLink()](./use-link) 自行实现：

```vue
<script setup lang="uts">
import { useLink } from '@meng-xi/unix-router'

const link = useLink({ to: 'pages/about/about' })
const onClick = (): void => {
	link.navigate()
}
</script>
```

## 相关 API

- [useLink()](./use-link)
- [组合式 API 指南](../guide/composables)
