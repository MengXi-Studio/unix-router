# 路由导航

unix-router 提供 vue-router 风格的编程式导航，底层映射到 uni-app x 原生 `uni.*` API。

## 四种导航方式

| 方法 | 对应原生 API | 说明 |
| --- | --- | --- |
| `router.push(location)` | `uni.navigateTo` / `uni.switchTab` | 保留当前页，跳转到新页 |
| `router.replace(location)` | `uni.redirectTo` / `uni.switchTab` | 替换当前页 |
| `router.relaunch(location)` | `uni.reLaunch` / `uni.switchTab` | 关闭所有页面，打开目标页 |
| `router.back(delta)` | `uni.navigateBack` | 返回上一页或多级 |

> 目标为 `meta.isTab` 的路由会自动使用 `uni.switchTab`。

## 位置形式（RouteLocationRaw）

导航目标支持 **字符串** 或 **对象** 两种写法：

```ts
// 字符串：可直接带查询
router.push('/pages/about/about?from=home')

// 对象：path / name + query + params
router.push({ name: 'about', query: new Map([['a', '1']]) })
router.push({ path: '/pages/about/about', params: new Map([['from', '首页']]) })
```

## params（对象参数）

uni-app x 静态页面模型不支持路径参数，unix-router 将 `params` 经**查询编码**（`__unixr_p_` 保留前缀）在页面 URL 间传递，目标页 `route.params` 可读取：

```ts
router.push({ path: '/pages/params/params', params: new Map([['id', '42']]) })
```

```ts
// 目标页 onLoad / useRoute
const id = route.params.get('id')
```

## 返回上一页

```ts
router.back() // 返回一页
router.back(2) // 返回两页
```

`back` 会执行完整守卫链（beforeEach → beforeResolve），可由守卫中止或重定向。

## 重复导航拦截

`router.push` 到当前相同地址时会抛出 `NAVIGATION_DUPLICATED` 失败（可用 `isNavigationFailure` 判断），避免无意义跳转。

```ts
try {
	await router.push('/pages/index/index')
} catch (e) {
	if (isNavigationFailure(e, RouterErrorCode.DUPLICATED)) {
		// 已在该页，忽略
	}
}
```

## 并发排队

当上一次导航尚未完成时，后续导航会自动**排队**，待前一次完成后顺序执行，避免状态错乱。