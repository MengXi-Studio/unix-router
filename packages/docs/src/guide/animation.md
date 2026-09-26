# 导航动画

`AnimationPlugin` 为导航注入窗口过渡动画（对齐 uni-app x 原生 `animationType`），支持全局默认与单次覆盖。

## 注册插件

```ts
import { createRouter, AnimationPlugin } from '@meng-xi/unix-router'

const router = createRouter({
	routes,
	plugins: [new AnimationPlugin()], // 插件为 abstract class，注册时必须实例化
	animation: { type: 'slide-in-right', duration: 300 } // 全局默认动画（可选）
})
```

## 全局默认动画

`RouterOptions.animation`（`NavigationAnimation`：`{ type, duration? }`）为所有导航设置默认动画；`duration` 省略时默认 `300`ms。

## 单次导航覆盖

导航位置上携带 `animationType` / `animationDuration`，只对**本次**生效，优先于全局默认：

```ts
await router.push({
	path: 'pages/detail/detail',
	animationType: 'fade-in',
	animationDuration: 500
})
```

::: tip back 无单次覆盖
`back()` 不接收位置参数，使用全局默认动画（自动映射为退出型，见下文）。单次覆盖仅对 `push` / `replace` / `relaunch` 有效。
:::

### 示例：全局默认 + 详情页单次覆盖

```ts
// router/index.uts：全局默认 slide-in-right
import { createRouter, AnimationPlugin } from '@meng-xi/unix-router'

export const router = createRouter({
	routes,
	plugins: [new AnimationPlugin()],
	animation: { type: 'slide-in-right', duration: 300 }
})
```

```vue
<!-- 首页 pages/index/index.uvue：打开详情页时单次覆盖为 fade-in -->
<script setup lang="uts">
import { useRouter } from '@meng-xi/unix-router'

const router = useRouter()

const openDetail = () => {
	router.push({
		path: 'pages/detail/detail',
		animationType: 'fade-in',      // 单次覆盖全局默认
		animationDuration: 400
	})
}
</script>

<template>
	<view class="page">
		<button @click="openDetail"><text>打开详情（fade-in）</text></button>
	</view>
</template>
```

## AnimationType 全表

共 18 个值，分为通用、进入型、退出型三组：

| 分类 | 值 |
| --- | --- |
| 通用 | `auto`（平台默认）、`none`（无动画） |
| 进入型 | `slide-in-right`、`slide-in-left`、`slide-in-top`、`slide-in-bottom`、`fade-in`、`zoom-in`、`zoom-fade-in`、`pop-in` |
| 退出型 | `slide-out-right`、`slide-out-left`、`slide-out-top`、`slide-out-bottom`、`fade-out`、`zoom-out`、`zoom-fade-out`、`pop-out` |

## back 的退出动画映射

`back()` 会把全局默认动画自动映射为对应的**退出型**：已是退出型则原样使用。

| 全局默认（进入型） | back 实际播放 |
| --- | --- |
| `slide-in-right` | `slide-out-right` |
| `slide-in-left` | `slide-out-left` |
| `slide-in-top` | `slide-out-top` |
| `slide-in-bottom` | `slide-out-bottom` |
| `fade-in` | `fade-out` |
| `zoom-in` | `zoom-out` |
| `zoom-fade-in` | `zoom-fade-out` |
| `pop-in` | `pop-out` |
| `auto` / `none` | `none`（不播动画） |

### 示例：返回时自动播放退出型动画

`back()` 无单次动画参数，返回动画由全局默认自动映射（如 `slide-in-right` → `slide-out-right`），页面无需额外配置：

```vue
<!-- 详情页 pages/detail/detail.uvue（全局默认 slide-in-right 时） -->
<script setup lang="uts">
import { useRouter } from '@meng-xi/unix-router'

const router = useRouter()

const goBack = () => {
	// App：原生窗口播放 slide-out-right（小程序官方不支持动画字段）；
	// H5：先播完 300ms 退出动画，再真正执行 navigateBack
	router.back()
}
</script>

<template>
	<view class="page">
		<button @click="goBack"><text>返回（自动退出型动画）</text></button>
	</view>
</template>
```

## 平台实现差异

| 平台 | 实现方式 |
| --- | --- |
| App | 透传 `animationType` / `animationDuration` 给 `uni.*` 原生导航 API（原生窗口动画，官方仅 App 支持） |
| 小程序 | 官方不支持动画字段，无动画 |
| H5 | Web Animations API（`element.animate`）对页面容器播放进入 / 退出动画，无需 CSS `@keyframes` |

H5 端的两个时序细节：

- **进入动画**：在导航完成回调里**同步应用动画起点样式**（如 `translateX(100%)` 并强制 reflow），让新页首帧即位于屏幕外，再于下一帧播放滑入——避免「内容原位闪现后再跳到屏幕外滑入」的割裂感；动画结束后清理内联起点样式；
- **返回动画**：先播放当前页的**退出动画并等待其完成**，再真正执行 `navigateBack`。

::: warning switchTab 无动画
目标为 `meta.isTab` 页面时导航走 `uni.switchTab`，原生 `switchTab` 不接受动画参数——全局与单次动画配置对 TabBar 页导航均无效。
:::

::: tip 未注册插件时
未注册 `AnimationPlugin` 时，携带 `animationType` 的导航仍正常执行（动画被忽略），不影响功能。
:::

## API 参考

### AnimationPlugin

`RouterPlugin` 子类，导航动画插件。

| 成员 | 签名 | 说明 |
| --- | --- | --- |
| `name` | 常量 `'animation'` | 插件名 |
| `constructor()` | 无参数 | — |
| `install` | `(context: PluginContext, options: RouterOptions): void` | 读取 `options.animation` 与单次 `animationType` / `animationDuration`，在 prepare 阶段解析最终生效动画并注入导航选项；H5 端另挂接「返回先播退出动画」「完成后播进入动画」两个 hook |

注册写法（必须实例化；非蒸汽端插件必须 class 实现，对象字面量含方法会被推断为 UTSJSONObject）：

```ts
plugins: [new AnimationPlugin()]
```

### NavigationAnimation

```ts
type NavigationAnimation = {
	type: AnimationType
	duration?: number // ms，省略时默认 300
}
```

| 成员 | 类型 | 说明 |
| --- | --- | --- |
| `type` | `AnimationType` | 动画类型，18 个取值见上文「AnimationType 全表」 |
| `duration` | `number`（可选） | 动画时长，默认 `DEFAULT_ANIMATION_DURATION = 300` |

### 动画配置入口

| 入口 | 类型 | 作用域 |
| --- | --- | --- |
| `RouterOptions.animation` | `NavigationAnimation` | 全局默认，对所有导航生效 |
| 导航位置 `animationType` | `string`（`AnimationType` 取值） | 单次覆盖，仅对 `push` / `replace` / `relaunch` 生效；`back` 无此参数 |
| 导航位置 `animationDuration` | `number` | 单次覆盖时长 |

### 插件内部工具

以下工具由插件模块导出（包入口仅导出 `AnimationPlugin`，自定义插件可从模块级复用）：

| 名称 | 签名 | 说明 |
| --- | --- | --- |
| `DEFAULT_ANIMATION_DURATION` | 常量 `300` | 默认动画时长（ms） |
| `pickAnimation` | `(location: RouteLocationRaw \| null) => NavigationAnimation \| null` | 从导航位置提取单次动画（无 `animationType` 时返回 `null`） |
| `normalizeAnimation` | `(anim: NavigationAnimation) => NavigationAnimation` | 补齐默认时长 |
| `toExitType` | `(type: AnimationType) => AnimationType` | 进入型 → 退出型映射（见上文映射表）；已是退出型原样返回；`auto` / `none` 返回 `none` |
| `ANIM_DATA_KEY` | 常量 `'__animation__'` | pluginData 中单次动画的存储 key |
| `animatePageEnter` / `animatePageExit` | 仅 Web 条件导出 | H5 进入 / 退出动画实现（WAAPI），返回 `boolean` / `Promise<void>` |

### 错误行为

本插件不产生导航失败（无 `PLUGIN_REQUIRED` 等错误码）：

| 条件 | 行为 |
| --- | --- |
| 未注册 AnimationPlugin 时携带 `animationType` | 动画被忽略，导航正常执行 |
| H5 环境不支持 WAAPI | 静默降级：清理起点占位样式，正常导航 |
| 目标为 TabBar 页（switchTab） | 原生 `switchTab` 不接受动画参数，动画配置无效 |

### 平台注意

- **App**：解析后的 `animationType` / `animationDuration` 直接透传给 `uni.*` 原生导航 API，由原生窗口动画呈现（小程序官方不支持动画字段，无动画）；
- **H5**：通过 Web Animations API（`element.animate`）对页面容器（`uni-page` 元素）播放关键帧，不使用 CSS `@keyframes`；进入动画在导航完成回调中**同步应用起点样式并强制 reflow**，再延后一帧播放，避免「内容原位闪现后再滑入」；返回时先播退出动画（按时长等待完成）再真正 `navigateBack`；
- H5 关键帧命名与 App 端 `animationType` 一一对应（进入型 8 种、退出型 8 种），未匹配的类型不播放动画。

## 下一步

- [uni API 拦截](./interceptor) — 让原生直调的导航也走守卫链
- [插件系统](./plugins) — AnimationPlugin 在插件体系中的位置
- [RouterOptions](../api/type-router-options) — `animation` 选项类型定义
