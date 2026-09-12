# RouterLink 组件

`<RouterLink>` 是基于 [useLink()](./use-link) 的声明式导航组件，提供点击跳转的可交互导航入口。

> 说明：uni-app x 为静态页面模型，不存在 vue-router 的页内渲染占位（`<router-view>`），组件仅承担「可交互导航入口」角色。

## 引入

从库入口直接导入使用：

```ts
import { RouterLink } from '@meng-xi/unix-router'
```

## 属性

| 属性 | 类型 | 默认值 | 说明 |
| --- | --- | --- | --- |
| `to` | `string` | `''` | 目标路由路径（字符串） |
| `replace` | `boolean` | `false` | 是否使用 `replace` 导航（当前页替换为目标页） |
| `relaunch` | `boolean` | `false` | 是否使用 `relaunch` 导航（关闭所有页面再打开目标） |

> `replace` 与 `relaunch` 互斥：同时为 `true` 时按 `relaunch` 优先处理（与 `useLink` 语义一致）。

## 用法

```vue
<template>
	<view class="page">
		<RouterLink to="pages/about/about">关于</RouterLink>
		<RouterLink to="pages/detail/detail" replace>替换导航</RouterLink>
		<RouterLink to="pages/index/index" relaunch>重开导航</RouterLink>
	</view>
</template>

<script setup lang="uts">
import { RouterLink } from '@meng-xi/unix-router'
</script>
```

点击组件触发导航；导航失败由路由器 `onError` / 调用方处理。

## 与 useLink 的关系

组件内部是 `useLink({ to, replace, relaunch })` 的薄封装（点击时调用 `link.navigate()`）。若需要自定义交互（如菜单激活态、更多样式控制），直接用 [useLink()](./use-link) 自行实现：

```vue
<script setup lang="uts">
import { useLink } from '@meng-xi/unix-router'

const link = useLink({ to: 'pages/about/about' })
const onClick = () => link.navigate()
</script>
```

## 相关 API

- [useLink()](./use-link)
- [组合式 API 指南](../guide/composables)