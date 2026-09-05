# 路由导航

unix-router 提供 vue-router 风格的**编程式导航**，底层映射到 uni-app x 原生 `uni.*` API。本篇先讲清每种导航的用途，再深入 `params / query` 传递与常见陷阱。

## 四种导航方式

| 方法 | 对应原生 API | 页签行为 | 适用场景 |
| --- | --- | --- | --- |
| `router.push(location)` | `uni.navigateTo` / `uni.switchTab` | **保留**当前页，推入新页 | 绝大多数前进跳转 |
| `router.replace(location)` | `uni.redirectTo` / `uni.switchTab` | **替换**当前页 | 登录等"不希望返回上一步"的场景 |
| `router.relaunch(location)` | `uni.reLaunch` / `uni.switchTab` | **关闭全部页**，打开目标 | 跳回首页 / 退出到根 |
| `router.back(delta)` | `uni.navigateBack` | 返回上一页或多级 | 返回 |

> 目标路由若在 `meta.isTab` 中标为 TabBar 页，`push / replace / relaunch` 会自动改用 `uni.switchTab`（此时不支持传 query/params）。

**怎么选？**
- 想要"返回"回到当前页 → `push`
- 不想让用户返回到当前页（如登录成功）→ `replace`
- 清空栈回到根页 / 从落地页进入主界面 → `relaunch`
- 返回 → `back(delta)`

## 位置形式（RouteLocationRaw）

目标支持**字符串**或**对象**。

```ts
// 字符串：可直接内联 query
router.push('/pages/about/about?from=home')

// 对象：name 导航（推荐，解耦路径）
router.push({ name: 'about', query: new Map([['a', '1']]) })

// 对象：path 导航 + params
router.push({ path: '/pages/detail/detail', params: new Map([['from', '首页']]) })

// 同时给 name 和 path 时：name 优先
router.push({ name: 'about', path: '/pages/index/index' }) // 命中 name='about'
```

## 参数传递：query 与 params

### query（查询串，URL 可见）

以 `Map<string,string>` 承载，序列化进 URL。适合**少量、简单、可分享**的数据：

```ts
router.push({ name: 'about', query: new Map([['id', '42'], ['utm', 'banner']]) })
// URL ≈ /pages/about/about?id=42&utm=banner

// 目标页读取（Map API，非点号）
const id = useRoute().query.get('id') ?? ''
```

### params（对象参数，编码进 URL 但键不暴露）

uni-app x 不支持路径参数，unix-router 将 `params` 经**查询编码**（`__unixr_p_` 保留前缀）跨页传递。**key 经过编码，不暴露明文键名**，与普通 query 隔离：

```ts
router.push({ path: '/pages/detail/detail', params: new Map([['from', '首页'], ['id', '42']]) })
// URL 中约为 ...?__unixr_p_%E4%BB%8E...=首页 的编码形态

// 目标页读回的是干净 key
const from = route.params.get('from')  // "首页"
const id = route.params.get('id')      // "42"
// params 的内部键不会出现在 route.query 里
```

### query 还是 params？

| 维度 | query | params |
| --- | --- | --- |
| URL 可见性 | 明文键名 + 值 | 键名被编码，值仍可见 |
| 适合数据 | 少量简单、可分享、埋点 | 命名参数、不想暴露键名 |
| 读取 | `route.query.get` | `route.params.get` |
| 类型 | 均为 `Map<string,string>` | 均为 `Map<string,string>` |

**注意**：两者都以字符串在 URL 传递。**复杂对象请先 `JSON.stringify` 或改用全局状态（如原生 `reactive` 模块）承载**；跨页传递复杂/敏感/大量数据首选后者。

## 返回上一页

```ts
router.back()  // 返回一页
router.back(2) // 返回两级
```

`back` 会执行**完整守卫链**（`beforeEach` → `beforeResolve`），可由守卫中止或重定向——所以"返回拦截"也是通过守卫实现的之一。

## 重复导航拦截

`push` 到与**当前相同地址**（path + query 一致）时，抛 `DUPLICATED` 失败，可用 `isNavigationFailure` 精准判断并忽略：

```ts
try {
	await router.push('/pages/about/about')
} catch (e) {
	if (isNavigationFailure(e, RouterErrorCode.DUPLICATED)) {
		// 已在该页，忽略
	} else {
		throw e
	}
}
```

> 想"刷新当前页"应改用 `router.replace`（或用不同 query/params 导航）。

## 并发排队

上一次导航未完成时，后续导航会**自动排队**，顺序执行，避免状态错乱。因此大部分场景无需手动防抖。

## TabBar 切换

TabBar 页面用 `switchTab` 导航，不会产生新的页面栈项。想让 TabBar 激活态与当前页一致，依赖 `syncRoute()`（见[组合式 API](./composables)）在 `onShow` 自动对齐。

## 常见坑

1. **用点号访问 `route.query.id`** → 应为 `route.query.get('id')`（query/params 都是 Map）。
2. **TabBar 页传 query/params 无效**：`switchTab` 不支持，请改用全局状态。
3. **push 到当前页没反应**：被 `DUPLICATED` 拦截；改用 `replace` 或携带不同参数。
4. **复杂对象传给 params 丢失**：params 只承载字符串，先序列化或用全局状态。

## 相关

- [参数传递深入 + 实践](./recipes#参数传递)
- [路由导航 API](../api/router-instance)