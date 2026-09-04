# RouterLink

`RouterLink` 是声明式导航组件，内部通过 [useLink()](../api/use-link) 实现。点击后执行导航，并响应式地提供激活状态。

## 引入

```ts
// 全局注册或按需引入
import { RouterLink } from '@meng-xi/unix-router'
```

## Props

| 名称 | 类型 | 默认值 | 说明 |
| --- | --- | --- | --- |
| `to` | `string` | `''` | 目标路由位置（路径或对象） |
| `replace` | `boolean` | `false` | 是否使用 `replace` 导航 |
| `relaunch` | `boolean` | `false` | 是否使用 `relaunch` 导航 |

## 示例

```vue
<template>
	<RouterLink to="pages/about/about">关于</RouterLink>
	<RouterLink :to="{ name: 'about', query: new Map([['from', 'home']]) }">关于（带参）</RouterLink>
	<RouterLink to="pages/profile/profile" replace>替换到个人中心</RouterLink>
</template>
```

::: tip to 支持字符串或对象
`to` 为 `RouteLocationRaw`（字符串路径或对象），与 `router.push` 的参数一致。
:::

## 自定义样式与激活状态

若需基于激活状态自定义样式，可使用底层 [useLink()](../api/use-link) 自行封装，其返回 `isActive` / `isExactActive` 响应式状态。

## 相关 API

- [useLink()](../api/use-link)
- [路由导航指南](../guide/navigation)