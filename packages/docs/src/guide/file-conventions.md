# 文件约定

构建期插件把每个页面文件当作事实源：页面内**就近声明**一段配置，插件据此生成 `pages.json` 条目与路由表。本篇是全部声明语法的参考，接入方式见[基于文件的路由生成](./file-based-routing)。

## defineUniPage 宏

在每个页面的 `<script setup>` 顶部声明（无需 import，编译期剥离、等行数注释占位不影响行号）：

```vue
<!-- pages/mine/mine.uvue -->
<script setup>
	// 页面声明宏：编译期由 @meng-xi/unix-router/vite-plugin 剥离
	defineUniPage({
		title: '我的',
		name: 'mine',
		isTab: true,
		tab: { order: 2, text: '我的', iconPath: 'static/tabbar/mine.png', selectedIconPath: 'static/tabbar/mine-active.png' }
	})
	// ...页面逻辑
</script>
```

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `title` | `string?` | 页面标题 → `pages.json` 的 `navigationBarTitleText` 与路由 `meta.title` |
| `name` | `string?` | 命名路由；缺省按 camelCase 规范化自动生成 |
| `isTab` | `boolean?` | 是否 tabBar 页 → `meta.isTab` + `pages.json` `tabBar.list` |
| `tab` | `{ order?, text?, iconPath?, selectedIconPath? }` | tabBar 附属信息，`order` 决定 tab 顺序 |
| `meta` | `Record<string, any>?` | 路由 meta 扩展字段（如 `requireAuth`），与 `RouteMeta` 定义匹配 |
| `redirect` | `string?` | 路由重定向目标（页面路径） |
| `beforeEnter` | `(to, from) => any?` | 路由独享前置守卫，**须自包含**（生成文件内不引用页面作用域变量） |

分包页与独享守卫示例：

```vue
<!-- pages-sub/setting/setting.uvue —— 分包页 + 独享守卫 -->
<script setup>
	defineUniPage({
		title: '设置',
		name: 'setting',
		beforeEnter: (to: any, from: any): any => {
			// 注意：必须自包含，不能引用页面内变量（生成文件与页面文件是两个作用域）
			return uni.getStorageSync('logged') === '1' ? true : { name: 'login' }
		}
	})
</script>
```

**规范**：每页至多一次，位于 script 顶层。`beforeEnter` 等函数字段按原文注入生成文件（JSON 表达不了函数，UTS 表达式可以），类型安全由 UTS 编译链兜底。

## &lt;route-config&gt; 自定义块

不习惯宏或声明内容较长时，可用 SFC 自定义块（与宏同效，优先级低于宏）：

```vue
<route-config lang="jsonc">
{
	// jsonc：支持注释
	"title": "商品详情",
	"name": "goods-detail",
	"meta": { "requireAuth": true }
}
</route-config>

<route-config lang="uts">
{
	title: '确认订单',
	meta: { requireAuth: true },
	// lang="uts" 支持函数字段
	beforeEnter: (to, from) => true
}
</route-config>
```

块内容在编译期被虚拟模块拦截，不会进入页面 JS，对编译器透明。

## 优先级

同一字段多处声明时按优先级链取值：

```
defineUniPage 宏  >  <route-config> 块  >  插件推导（titleFallback / tabBar 配置兜底）
```

字段级合并：宏里写了 `title`、块里写了 `meta`，两者都生效。

## name 自动规范化

| 场景 | 结果 |
| --- | --- |
| 宏/块声明了 `name` | 使用声明值 |
| 缺省，末段 `mine` 不冲突 | 末段 camelCase：`pages/mine/mine` → `mine` |
| 末段撞名（如 `pages/a/index` 与 `pages/b/index`） | 全路径 camelCase 回退：`aIndex` / `bIndex` |
| 极端情况仍冲突 | 按 `errorStrategy` 处理：`strict`（默认）抛错终止并列出清单 / `warn` 告警跳过 |

生成 `route-name.gen.d.ts`（WEB 端 `RouteNameMap` 模块增强），按名导航获得字面量补全（见[命名路由类型](./route-config#命名路由类型-routename)）。

## 生成产物

| 文件 | 说明 |
| --- | --- |
| `pages.json` | 自动生成页面条目 + tabBar + subPackages；**手写的非页面字段（globalStyle、uniIdRouter 等）合并保留** |
| `routes.gen.uts` | 路由表 `RouteConfig[]`，在 `router.uts` 中 `import { routes } from './routes.gen.uts'` |
| `route-name.gen.d.ts` | `RouteNameMap` 字面量类型（WEB 端编辑器提示，可关） |
| `define-uni-page.d.ts` | 宏类型声明（编辑器提示，可关） |

生成文件**一并提交 git**：HBuilderX CLI 编译链不保证插件先行执行，watch/HMR 只是开发期辅助。

::: warning 常见坑
- **宏写了两次**：每页至多一次，`strict` 下直接终止构建。
- **`beforeEnter` 引用页面内变量**：宏函数被原样注入 `routes.gen.uts`，与页面是两个作用域，引用页面变量会编译失败。守卫逻辑须自包含（storage / 全局状态）。
:::

## 下一步

- [基于文件的路由生成](./file-based-routing) — 插件选择与接入设置
- [扩展路由](./extending-routes) — 修改生成产物 / `routes.ext.uts` 扩展声明
- [路由配置](./route-config) — 手写路由表的字段与规范（本插件的生成目标）
