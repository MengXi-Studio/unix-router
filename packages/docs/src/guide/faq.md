# 常见问题

汇总使用 unix-router 过程中的高频问题与排查思路。

## params 读不到

**排查 1：是否正确注册了 ParamsPlugin**

未注册 `ParamsPlugin` 时，带 `params` 的导航会直接 reject `PLUGIN_REQUIRED`；若导航正常但目标页 `route.params` 为空，先确认注册方式（插件必须实例化）：

```ts
// ❌ 旧写法：直接传 class（已废弃）
createRouter({ routes, plugins: [ParamsPlugin] })

// ✅ 实例化注册
createRouter({ routes, plugins: [new ParamsPlugin()] })
```

**排查 2：是否读错了字段**

params 与 query 是两个独立字段。params 经内部 key `__params__` 通道传递，**不会出现在用户可见的 URL query 中**；目标页在状态同步完成后才能读到重建后的 `route.params`。

**排查 3：值是否可序列化**

params 值为 `Map<string, string>`，内部经 JSON 序列化跨页，复杂对象请先转字符串（如 `JSON.stringify`），目标页再反序列化。

## onShow 中读到旧 query

**现象**：页面 `onShow` 里通过 `useRoute()` 读到的 `query` 是上一页 / 旧值。

**原因**：`onShow` 的触发时机**早于**路由器完成 `syncRoute`（按页面栈同步 `currentRoute`），此时路由状态尚未更新到本页。

**方案**：在页面 `onLoad(options)` 中直接读取 uni 传入的本页 query，不要依赖 `useRoute()`：

```uts
import { onLoad } from '@dcloudio/uni-app'

onLoad((options: UTSJSONObject) => {
	const id = options['id'] as string | null
	// options 即本页 URL query，与本页加载同步
})
```

## switchTab 丢 query

`meta.isTab === true` 的页面导航会自动转用 `uni.switchTab`，而 **switchTab API 不支持携带 query**——传给 tab 页的 query 会丢失。

需要向 tab 页传递数据时，改用：

- **全局状态 / storage**（query、params、events 通道均经 URL query 桥接，switchTab 下均不可达）
- **eventBus** 广播（全局总线不经导航 URL，目标页注册监听后可收到）

## 守卫不生效

**排查 1：是否通过路由器调用**

```ts
// ❌ 直接调用 uni API，守卫不生效
uni.navigateTo({ url: '/pages/about/about' })

// ✅ 通过路由器调用
await router.push({ name: 'about' })
```

unix-router **默认不拦截**原生导航 API，直接调用 `uni.navigateTo` 等会**绕过守卫**。要么统一使用 `router.*` 或 `<RouterLink>`，要么启用 InterceptorPlugin + `interceptUniApi: true` 拦截外部调用（详见[uni API 拦截](./interceptor)）。

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
	if (user == null) {
		return { name: 'login' }
	}
	return true
})
```

## 守卫超时中止

**现象**：控制台出现守卫超时警告，导航被中止。

**原因**：异步守卫在 `guardTimeout`（默认 **10000ms**，设为 0 禁用）内未返回，路由器超时后警告并中止本次导航。

**排查**：

1. 守卫内 Promise 是否永远不会 settle（如请求未配超时 / catch）。
2. 是否 `await` 了一个卡死的任务。
3. 守卫所有分支是否都有返回值（漏 return 会被一直挂起等待）。

确认逻辑无误但仍需更长时间时，调大 `guardTimeout`：

```ts
const router = createRouter({
	routes,
	guardTimeout: 30000 // 30s；0 表示禁用超时保护
})
```

## 重复导航报错（DUPLICATED）

`push` 到相同位置（path + query + params + hash 完全一致）会 reject `DUPLICATED`（仅 push 检测；与 vue-router 的 `resolve(false)` 不同，这里是真正的 reject）。

```ts
import { isNavigationFailure, RouterErrorCode } from '@meng-xi/unix-router'

try {
	await router.push({ name: 'about' })
} catch (err) {
	// 忽略重复导航，其余错误继续抛出
	if (!isNavigationFailure(err, RouterErrorCode.DUPLICATED)) {
		throw err
	}
}
```

## 守卫中触发导航死锁

不要在守卫内调用 `router.push`，改为 `return` 重定向。

```ts
// ✅ 重定向，不形成死锁
router.beforeEach((to, from) => {
	if (needRedirect) {
		return { name: 'other' }
	}
	return true
})
```

重定向深度有上限（10 次），超限返回 `CANCELLED`，不会无限循环。

## 返回无法拦截

物理返回（按键 / 侧滑 / 导航栏返回）与浏览器后退**均不经过路由器**，任何平台的守卫链（含 `onBeforeRouteLeave`）都只对受控导航（`router.back()` / `push` 等）生效：

- **App 端**：物理返回键 / 导航栏返回 / 侧滑由系统直接出栈，不经守卫链；需要拦截时用页面 `onBackPress`
- **H5 端**：浏览器后退由 uni 框架直接出栈，不经守卫链
- **小程序端**：顶部返回箭头 / 滑动由宿主控制，无法同步拦截

以上场景用 `onRouteChange` + `syncRoute` 事后感知。详见[平台兼容性](./compatibility#h5-与原生差异)。

## H5 冷启动直达页守卫不执行

**现象**：用户通过 URL 直接打开深层页面（如分享链接进入详情页），全局守卫没有执行。

**原因**：守卫链挂在导航调用上，冷启动直达页没有发起 `router.push`，守卫自然不会跑。

**方案**：在 `App.uvue` 的 `onLaunch` 中，等路由器 ready 后用 `guardRoute` 对真实入口页**补执行 `beforeEach` 守卫链**（不实际导航；守卫返回 abort 时触发 `onAbort` 并 reject，返回 redirect 时默认以 `relaunch` 执行真实跳转）：

```uts
import { onLaunch } from '@dcloudio/uni-app'

onLaunch((options) => {
	router.isReady().then(() => {
		let launchPath: string | null = null
		if (options.path != null && options.path.length > 0) {
			launchPath = '/' + options.path
		}
		router.guardRoute(launchPath, {
			onAbort: (failure) => {
				// 冷启动被守卫拦截：重定向到首页
				router.relaunch({ name: 'home' })
			}
		})
	})
})
```

直接在启动时访问 `router.currentRoute` 得到的可能是初始值，页面级数据仍以本页 `onLoad` / `onShow` 为准。

## query / params 丢失

`query` 与 `params` 均为 `Map<string, string>`，且以字符串传递。

```ts
// ❌ 用点号访问
route.query.id

// ✅ 用 Map API
route.query.get('id')
route.query.has('id')
```

数字 / 布尔语义可用内置工具读取：`queryInt(query, 'id')` / `queryNumber(query, 'price')` / `queryBool(query, 'enabled')`（均带默认值兜底）。

复杂对象数据需先序列化；结构化数据跨页建议改用 params（ParamsPlugin）或 EventsPlugin / eventBus。

## switchTab 页面收不到参数

见上文「[switchTab 丢 query](#switchtab-丢-query)」：`switchTab` 不支持 query，改用 params 或事件通信。

## 页面栈溢出

小程序页面栈有上限，接近上限时改用 `relaunch`。参考[实战指南 - 页面栈深度管理](./recipes#页面栈深度管理)。

## H5 刷新 404

uni-app x H5 端使用 hash 模式，请访问形如 `https://example.com/#/pages/index/index` 的地址；避免直接访问不带 hash 的深层 URL。

## 路由跳转白屏

1. 路径是否正确（应为 `pages/xxx/xxx` 完整页面路径，不带前导 `/`）
2. 页面是否已在 `pages.json` 注册
3. 目标页面的 `onLoad` / `setup` 是否有报错
4. 路由配置的 `path` 是否与 `pages.json` 完全一致
5. 导航失败是否被 reject（`NAVIGATION_API_ERROR` 通常意味着栈顶校验未通过或目标页不存在）

## 路由懒加载

uni-app x 的页面加载由 `pages.json` 决定，**不支持** vue-router 的 `() => import()` 懒加载。所有页面都会被打包，可使用分包（`subPackages`）控制。

## 仍无法解决？

1. 查看 [API 文档](../api/create-router) 确认用法
2. 查看 [导航流程原理](./navigation-flow) 理解内部机制
3. 查看 [平台兼容性](./compatibility) 确认是否平台限制
4. 在 [GitHub Issues](https://github.com/MengXi-Studio/unix-router/issues) 提交问题（附复现步骤、平台、uni-app x 与 unix-router 版本、完整报错）
