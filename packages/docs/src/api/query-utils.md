# 查询参数工具

从 `route.query`（`Map<string, string>`）原子读取强类型值的便捷工具，替代手写 `parseInt` + 边界判断。

```ts
import { queryInt, queryNumber, queryBool } from '@meng-xi/unix-router'
```

三个函数共享同一套取值规则：键不存在、值为空字符串时直接返回默认值；解析失败同样返回默认值，**永不抛错**。

## queryInt()

解析整型值（`parseInt`，十进制）：

```ts
function queryInt(query: Map<string, string>, key: string, defaultValue: number = 0): number
```

```ts
const route = useRoute()
const page = queryInt(route.query, 'page', 1) // ?page=3 → 3；缺失/非法 → 1
```

## queryNumber()

解析浮点数值（`parseFloat`）：

```ts
function queryNumber(query: Map<string, string>, key: string, defaultValue: number = 0): number
```

```ts
const price = queryNumber(route.query, 'price', 9.9) // ?price=19.5 → 19.5
```

## queryBool()

解析布尔值。`'0'` 与 `'false'` 视为 `false`，**其余任何非空值视为 `true`**（`'1'` / `'true'` / `'yes'` 等）：

```ts
function queryBool(query: Map<string, string>, key: string, defaultValue: boolean = false): boolean
```

```ts
const compact = queryBool(route.query, 'compact', false) // ?compact=1 → true；?compact=0 → false
```

::: tip 使用场景
典型位置是页面 `onShow`：此时路由状态已同步，`route.query` 反映当前页面真实 URL。示例见 playground 的 `queryNumber(route.query, 'num', 0)`。
:::

## 相关 API

- [useRoute()](./use-route) — 获取当前路由位置（`query` 来源）
- [路由导航](../guide/navigation) — query 参数的写入方式
