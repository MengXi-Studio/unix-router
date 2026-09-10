# 常见问题

汇总使用 unix-router 过程中的高频问题与排查思路。

## 守卫不生效

**排查 1：是否通过路由器调用**

```ts
// ❌ 直接调用 uni API，守卫不生效
uni.navigateTo({ url: '/pages/about/about' })

// ✅ 通过路由器调用
await router.push({ name: 'about' })
```

uni-app x 各端均支持 `uni.addInterceptor`（所需 HBuilderX 版本见[平台兼容性](./compatibility#原生导航-api-拦截addinterceptor)），但 unix-router **默认不拦截**原生导航 API，直接调用 `uni.navigateTo` 等会**绕过守卫**。请统一使用 `router.*` 或 `<RouterLink>`。

**排查 2：守卫是否正确返回**

```ts
// ❌ 分支漏 return
router.beforeEach((to, from) => {
	if (needAuth) {
		return { name: 'login' }
	}
	// 漏了放行分支
})

// ✅ 显式放行
router.beforeEach((to, from) => {
	if (needAuth) {
		return { name: 'login' }
	}
	return true
})
```

**排查 3：异步守卫是否正确 await**

```ts
// ✅ 用 async/await
router.beforeEach(async (to, from) => {
	const user = await fetchUser()
	if (!user) {
		return { name: 'login' }
	}
	return true
})
```

## query / params 丢失

`query` 与 `params` 均为 `Map<string, string>`，且以字符串在 URL 传递。

```ts
// ❌ 用点号访问
route.query.id

// ✅ 用 Map API
route.query.get('id')
route.query.has('id')
```

复杂对象数据需先序列化，或改由全局状态（如 Pinia）承载。

## 重复导航报错

`push` 到相同位置（path + name + query 一致）会抛出 `DUPLICATED`。

```ts
try {
	await router.push({ name: 'about' })
} catch (err) {
	if (err.code !== 16) throw err // RouterErrorCode.DUPLICATED
}
```

## 返回无法拦截

不同平台对返回的拦截能力不同：

- **App 端**：物理返回键 / 导航栏返回经返回守卫链，`onBeforeRouteLeave` 生效
- **H5 端**：浏览器后退经返回守卫链，`onBeforeRouteLeave` 生效
- **小程序端**：顶部返回箭头 / 滑动由宿主控制，**无法同步拦截**，用 `onRouteChange` 事后处理

详见[平台兼容性](./compatibility#返回拦截)。

## H5 刷新 404

uni-app x H5 端使用 hash 模式，请访问形如 `https://example.com/#/pages/index/index` 的地址；避免直接访问不带 hash 的深层 URL。

## 页面栈溢出

小程序页面栈有上限，接近上限时改用 `relaunch`。参考[实战指南 - 页面栈深度管理](./recipes#页面栈深度管理)。

## 守卫中触发导航死锁

不要在守卫内调用 `router.push`，改为 `return` 重定向。

```ts
// ✅ 重定向，不形成死锁
router.beforeEach((to, from) => {
	if (needRedirect) {
		return { name: 'other' }
	}
})
```

## 冷启动守卫校验

在 `App.vue` 的 `onLaunch` 中对真实入口页面补执行守卫，可传入 `options.path`：

```ts
onLaunch((options) => {
	router.isReady().then(() => {
		const launchPath = options?.path ? `/${options.path}` : undefined
		router.guardRoute(launchPath, {
			onAbort: (failure) => {
				router.relaunch({ name: 'home' })
			}
		})
	})
})
```

直接在启动时访问 `router.currentRoute` 得到的可能是初始值，页面级数据仍以本页 `onLoad` / `onShow` 为准。

## 路由跳转白屏

1. 路径是否正确（应为 `pages/xxx/xxx` 完整页面路径）
2. 页面是否已在 `pages.json` 注册
3. 目标页面的 `onLoad` / `setup` 是否有报错
4. 路由配置的 `path` 是否与 `pages.json` 完全一致

## 路由懒加载

uni-app x 的页面加载由 `pages.json` 决定，**不支持** vue-router 的 `() => import()` 懒加载。所有页面都会被打包，可使用分包（`subPackages`）控制。

## 仍无法解决？

1. 查看 [API 文档](../api/create-router) 确认用法
2. 查看 [导航流程原理](./navigation-flow) 理解内部机制
3. 查看 [平台兼容性](./compatibility) 确认是否平台限制
4. 在 [GitHub Issues](https://github.com/MengXi-Studio/unix-router/issues) 提交问题（附复现步骤、平台、uni-app x 与 unix-router 版本、完整报错）