# 参数传递

页面间传参有两种方式：**query**（URL 查询参数，路由器内置）与 **params**（经 `ParamsPlugin` 关联存储传递）。先选对方式，再写代码。

## query vs params 选型

| 维度 | query | params |
| --- | --- | --- |
| URL 可见性 | 可见（拼进 `fullPath`） | 用户不可见（内部 key 被剥离） |
| 刷新 / 收藏 / 分享后保留 | ✅ 仍在 | ❌ 与本次导航绑定 |
| 承载类型 | `Map<string, string>` | `Map<string, string>`（内容须 JSON 可序列化） |
| 是否需要插件 | 否 | 是（`ParamsPlugin`） |
| 典型场景 | 列表筛选、id、来源标记等短小参数 | 详情页数据包、结构化参数、不想暴露在地址栏的数据 |

## query：URL 查询参数

```ts
// 发起方：字符串内联或对象携带
await router.push('/pages/detail/detail?id=1024&from=home')
// 或
await router.push({
	name: 'detail',
	query: new Map<string, string>([['id', '1024'], ['from', 'home']])
})
```

query 会拼进 URL，目标页用 `route.query` 读取：

```ts
import { useRoute } from '@meng-xi/unix-router'

const route = useRoute()
route.query.get('id')     // '1024'
route.query.has('from')   // true
```

## params：关联存储传递（ParamsPlugin）

### 注册插件

params 依赖 `ParamsPlugin`，创建路由器时以**实例**注册：

```ts
import { createRouter, ParamsPlugin } from '@meng-xi/unix-router'

const router = createRouter({
	routes,
	plugins: [new ParamsPlugin()]
})
```

### 发起与读取

```ts
// 发起页
await router.push({
	name: 'detail',
	params: new Map<string, string>([['id', '1024'], ['info', 'hello']])
})
```

目标页 `pages/detail/detail.uvue` 读回：

```vue
<script setup lang="uts">
import { useRoute } from '@meng-xi/unix-router'

const route = useRoute()
const id = route.params.get('id')     // '1024'
const info = route.params.get('info') // 'hello'
</script>

<template>
	<view class="page">
		<text class="title">{{ id }}</text>
		<text class="desc">{{ info }}</text>
	</view>
</template>
```

### 示例：列表页传对象数组到详情页

params 只承载 `Map<string, string>`，对象 / 数组等结构化数据先 `JSON.stringify` 成字符串传递，目标页再 `JSON.parse<T>` 还原：

```vue
<!-- 列表页 pages/list/list.uvue -->
<script setup lang="uts">
import { useRouter } from '@meng-xi/unix-router'

type GoodsItem = {
	id: number
	name: string
}

const router = useRouter()

const openDetail = () => {
	const goods: GoodsItem[] = [{ id: 1, name: '机械键盘' }, { id: 2, name: '无线鼠标' }]
	const params = new Map<string, string>()
	// 结构化数据序列化为 JSON 字符串（params 内容须 JSON 可序列化）
	params.set('goods', JSON.stringify(goods))
	params.set('from', 'list')
	router.push({
		path: 'pages/goods/goods',
		params: params
	})
}
</script>

<template>
	<view class="page">
		<button @click="openDetail"><text>传对象数组打开商品页</text></button>
	</view>
</template>
```

```vue
<!-- 商品页 pages/goods/goods.uvue -->
<script setup lang="uts">
import { useRoute } from '@meng-xi/unix-router'

type GoodsItem = {
	id: number
	name: string
}

const route = useRoute()

// Map 取值可能为 null，先判空再解析
const raw = route.params.get('goods')
let goodsList: GoodsItem[] = []
if (raw !== null) {
	const parsed = JSON.parse<GoodsItem[]>(raw)
	if (parsed !== null) {
		goodsList = parsed
	}
}
const firstName = goodsList.length > 0 ? goodsList[0].name : '无数据'
</script>

<template>
	<view class="page">
		<text class="title">第一件商品：{{ firstName }}</text>
	</view>
</template>
```

### 工作机制

- params 以 `Map<string, string>` 承载，内容须 **JSON 可序列化**（不可序列化的值存储时会丢失并输出警告）
- 导航时参数存入 ParamsManager 并生成关联 key，以内部 query key `__params__` 拼入导航 URL
- 目标页做路由状态同步（`syncRoute`）时按 key 读回 params，并**剥离 `__params__`** —— 用户在 URL 与 `route.query` 中看不到它

### 持久化 paramsPersistent

默认存内存。开启 `paramsPersistent: true` 后写入 uni storage（key 带专属前缀），storage 写入失败时自动回退内存并告警：

```ts
const router = createRouter({
	routes,
	plugins: [new ParamsPlugin()],
	paramsPersistent: true
})
```

### 示例：paramsPersistent 持久化后跨冷启动读取

开启 `paramsPersistent: true` 后 params 写入 uni storage（key 前缀 `unixr_params_`）：应用被系统杀死后冷启动，若恢复的栈顶页 URL 仍携带 `__params__` 内部 key，路由状态同步会自动从 storage 读回 params —— 内存模式下重启后则读不回。

```ts
// router/index.uts
import { createRouter, ParamsPlugin } from '@meng-xi/unix-router'

export const router = createRouter({
	routes,
	plugins: [new ParamsPlugin()],
	paramsPersistent: true
})
```

自定义插件或独立场景可直接使用 `createParamsManager(true)`，此时关联 key 由调用方自行保存（如写入 storage）：

```ts
import { createParamsManager } from '@meng-xi/unix-router'

// 创建持久化管理器（独立于路由器实例）
const manager = createParamsManager(true)

// 存储内容并取得关联 key
const params = new Map<string, string>()
params.set('draft', '未提交的表单草稿')
const key = manager.set(params)

// key 自行持久保存后，任意时刻（含冷启动后）都能按 key 读回；不再使用时 remove 清理
uni.setStorageSync('draft_key', key)
const restored = manager.peek(key)
```

::: warning 未注册插件就使用 params 会导航失败
使用 `params` 但未注册 `ParamsPlugin` 时，导航直接 reject `PLUGIN_REQUIRED`（错误码 256）。收到此错误请检查 `createRouter` 的 `plugins` 是否包含 `new ParamsPlugin()`。
:::

### 高级：ParamsManager

插件同时导出 `createParamsManager(defaultPersistent)` 与 `ParamsManager`（`set(params, persistent?) → key` / `peek(key)` / `remove(key)`），自定义插件可复用同一套「存储 + key 关联」协议，详见[插件体系](./plugins)。

## query 解析工具

query 值恒为字符串，在 UTS 强类型下手动转换容易踩坑。内置三个原子读取工具：

```ts
import { queryInt, queryNumber, queryBool } from '@meng-xi/unix-router'

const route = useRoute()

const page = queryInt(route.query, 'page', 1)      // 整型；缺省/空串/NaN → 1（defaultValue 默认 0）
const price = queryNumber(route.query, 'price')    // 数值；缺省/空串/NaN → 0
const enabled = queryBool(route.query, 'enabled')  // 布尔；'' / '0' / 'false' → false
```

| 函数 | 签名 | 行为 |
| --- | --- | --- |
| `queryInt` | `(query, key, defaultValue = 0)` | 解析失败（缺键、空串、NaN）返回默认值 |
| `queryNumber` | `(query, key, defaultValue = 0)` | 同上，浮点 |
| `queryBool` | `(query, key, defaultValue = false)` | `''` / `'0'` / `'false'` → `false`，其余非空值 → `true` |

## 注意事项

- **大数据量走 params 而非 URL**：各端对 URL 长度有限制，长内容（整表单、大 JSON）用 ParamsPlugin 关联存储传递，不要塞 query。
- **读取时序**：目标页 `onShow` 可能早于路由状态同步，此时 `route.params` / `route.query` 尚未重建。建议在页面 `onLoad(options)` 里直接读启动 query，或在 `onShow` 里先 `router.syncRoute()` 再读（见[组合式 API](./composables#路由状态同步)）。
- **params 是一次性的**：它为"这一次导航"而生，不要当持久化状态用；需要跨会话的状态请用 storage 或全局状态。

## API 参考

### ParamsPlugin

`RouterPlugin` 子类，页面参数传递插件。

| 成员 | 签名 | 说明 |
| --- | --- | --- |
| `name` | 常量 `'params'` | 插件名，注册后可被 `hasPlugin('params')` 检测 |
| `constructor()` | 无参数 | — |
| `install` | `(context: PluginContext, options: RouterOptions): void` | 按 `options.paramsPersistent` 创建 ParamsManager，注册 enrichLocation / afterResolve / prepareNavigation / routeSync 四个 hook |

注册写法（必须实例化；非蒸汽端插件必须 class 实现，对象字面量含方法会被推断为 UTSJSONObject）：

```ts
plugins: [new ParamsPlugin()]
```

### createParamsManager

```ts
function createParamsManager(defaultPersistent: boolean): ParamsManager
```

| 参数 | 类型 | 说明 |
| --- | --- | --- |
| `defaultPersistent` | `boolean` | 全局默认持久化策略：`true` 写 uni storage，`false` 存内存 |

### ParamsManager

```ts
class ParamsManager {
	constructor(defaultPersistent: boolean)
	setDefaultPersistent(persistent: boolean): void
	set(params: ParamObject, persistent?: boolean | null): string
	peek(key: string): ParamObject | null
	remove(key: string): void
}
```

其中 `ParamObject = Map<string, string>`。

| 成员 | 参数 | 返回值 | 说明 |
| --- | --- | --- | --- |
| `constructor` | `defaultPersistent: boolean` | — | 设置默认持久化策略 |
| `setDefaultPersistent` | `persistent: boolean` | `void` | 修改全局默认持久化策略 |
| `set` | `params: ParamObject`；`persistent?: boolean \| null`（默认 `null`，沿用全局策略） | `string` | 存储并生成 `pk_` 前缀关联 key；内容须 JSON 可序列化，否则值丢失并警告 |
| `peek` | `key: string` | `ParamObject \| null` | 按 key 读回（内存优先，storage 兜底）；不清理，供导航解析阶段复用 |
| `remove` | `key: string` | `void` | 删除内存与 storage 中的该 key |

### RouterOptions.paramsPersistent

```ts
paramsPersistent?: boolean // 默认 false
```

开启后 ParamsPlugin 的 params 写入 uni storage；`true` 但未注册 ParamsPlugin 时安装阶段输出警告，选项被忽略。

### 错误行为

| 条件 | 行为 |
| --- | --- |
| 未注册 ParamsPlugin 时导航携带 `params` | 导航 reject `NavigationFailure`，错误码 `PLUGIN_REQUIRED`（`256`），提示「使用 params 需注册 ParamsPlugin」 |
| `paramsPersistent: true` 但未注册 ParamsPlugin | 安装时输出警告，选项被忽略 |
| params 含不可 JSON 序列化的值 | 存储时该值丢失，输出警告 |

### 平台注意

- 内部 query key 为 `__params__`（常量 `PARAMS_QUERY_KEY`），路由状态同步时被剥离，URL 与 `route.query` 中不可见；
- 持久化存储 key 带前缀 `unixr_params_`（常量 `PARAMS_STORAGE_PREFIX`）；storage 写入失败自动回退内存并告警；
- `peek` 读取顺序：内存 Map 优先，未命中再读 storage。

## 下一步

- [组合式 API](./composables) — useRoute 的读取时机与状态同步
- [页面间通信](./events) — events 双向通道
- [插件体系](./plugins) — ParamsPlugin 内部与自定义插件
