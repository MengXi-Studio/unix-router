# uni-app x 路由库实现计划（monorepo 仿 uni-router，功能仿 vue-router 4，core 用 UTS）

## Context（背景）

在空仓库 `/Users/queyupeng/github/MengXi-Studio/unix-router` 中，从零构建一个面向 **uni-app x** 的路由库，定位与两条约束对齐：

- **项目结构参考** `https://github.com/MengXi-Studio/uni-router` v2.7.1（已克隆至 `/tmp/uni-router-ref`）：仅参考其 **monorepo 设计**（`packages/core` + `packages/playground` 等），**不**照搬其功能。

- **功能对照 vue-router 4 最新版**，在 uni-app x 静态 pages.json 模型下"一比一复刻"其可落地能力。

- **技术根基** `https://doc.dcloud.net.cn/uni-app-x/`（`.uvue` 页面、pages.json 静态注册、uni.\* 原生导航、原生渲染）。

**核心差异**：uni-app x 用**静态 pages.json 页面模型**（未注册页面编译期被忽略），与 vue-router 依赖 URL/history、支持动态路由/嵌套路由/命名视图的模型有根本冲突。因此对 vue-router 功能做"完整复刻可行项 + 明确标注平台不支持/适配项"。

### UTS 与 TS 权威结论（已核实+确认）

uni-app x 两代运行模式：**VDOM 模式**（主语言 UTS，Android→Kotlin、iOS→Swift，Android 无 JS 引擎无法跑 JS）、**蒸汽模式**（普通 ts/js，全端 JS 引擎，2026 起逐渐替代 VDOM）。官方规则："非 JS 目标下引用 `.ts` 会当作 UTS 处理。"

**决策**：core 用 **`.uts`** 编写，实现 VDOM（→原生）与蒸汽模式（→JS）**双模式兼容**。代价：强类型约束，不用 `any`，用 `Map`/`UTSJSONObject`/`interface`。

### 其余已确认决策

1. 首期**核心完整实现优先**：createRouter、匹配、全部守卫、编程式导航、useRouter/useRoute、错误体系、isReady/onError；不建文档站。
2. **不用 vitest**；验证依托 **playground（真实 uni-app x 测试工程）** H5+微信小程序编译运行，内置自校验页输出 PASS/FAIL；App 原生由用户 HBuilderX 手动验证。
3. 中文注释；遵循"生成代码时添加标准级函数注释"。
4. monorepo 仿 uni-router：`packages/core`（`.uts`）+ `packages/playground`。

## vue-router 4 功能对照与落地（功能基准 = vue-router）

| vue-router 4 功能                                | uni-app x 静态模型落地                                                                                           |
| ---------------------------------------------- | ---------------------------------------------------------------------------------------------------------- |
| `createRouter({ routes, history, ... })`       | `createRouter({ routes, ... })`；无人 web/hash/memory 的 URL 概念，`history` 由**原生页面栈**承担，不提供 history 工厂（文档注明差异化） |
| 匹配：路径/命名/参数/查询                                 | pathMap/nameMap 双 map；路径参数、查询 `Map`；严格模式；字符串/路径对象/命名对象 resolve                                             |
| `route.params` / `query` / `meta` / `fullPath` | params 借 storage 存 JSON（`UTSJSONObject`）；query=onLoad options；meta 复用；fullPath 由 path+query 拼装             |
| `router.currentRoute` 响应式 ref                  | state 层 `ref`，useRoute 返回                                                                                  |
| 编程式导航 push / replace / back                    | push→navigateTo/switchTab；replace→redirectTo/switchTab；back→navigateBack(delta)；concurrent 排队、重复导航检测       |
| `router.go(n)`                                 | 退化为 `back(delta)`；history 语义受限（文档注明）                                                                       |
| 全局守卫 beforeEach / beforeResolve / afterEach    | 完整；返回 false/对象/Error/重定向语义对齐 vue-router                                                                    |
| 路由独享守卫 beforeEnter                             | `RouteConfig.beforeEnter`                                                                                  |
| 组件内守卫 onBeforeRouteEnter / Leave / Update      | Enter/Leave 完整；uni-app x 页面每次导航新实例，Update 极少触发（注明局限）                                                       |
| `useRouter` / `useRoute` / `useLink`           | 完整（provide/inject + ref；link 提供 isActive/isExactActive/navigate）                                           |
| 组件 `RouterLink`                                | 提供适配式组件（useLink 封装）                                                                                        |
| `RouterView`（命名视图/嵌套）                          | **不支持**（扁平页面模型，无页内渲染占位）→ 文档标注                                                                              |
| 动态路由 `addRoute` / `removeRoute`                | **不支持**（编译期忽略未注册页）→ 不在 Router 接口暴露                                                                         |
| 嵌套路由 children                                  | **不支持**；分包 subPackages 可作资源分包（非嵌套渲染）                                                                       |
| 命名视图                                           | **不支持** → 文档标注                                                                                             |
| 懒加载                                            | 借分包 subPackages 演示                                                                                         |
| `scrollBehavior`                               | **适配/受限**：页面滚动由 uni-app 原生管理，不提供 scrollBehavior 回调 → 文档标注                                                  |
| 404 兜底                                         | 保留一个"未匹配"错误页路径（无 `*` 语法），未匹配时导航到错误处理                                                                       |
| 错误处理 NavigationFailure / isNavigationFailure   | 完整 error 体系（aborted/cancelled/duplicated/not-found）                                                        |
| `isReady` / `onError` / `onRouteChange`        | 完整（onRouteChange 为额外增强）                                                                                    |
| route props（`props:true` 传参）                   | 适配：页面以 onLoad options 接收 params/query（部分支持，文档注明）                                                           |
| Replay 语义/redirect                             | 守卫重定向 + 最大深度限制                                                                                             |
| 冷启动直达守卫                                        | `guardRoute()` 补执行守卫链（额外增强，适配 H5 URL/deeplink）                                                             |

> 注：uni-router 的 ParamsPlugin/AnimationPlugin/ChannelPlugin/InterceptorPlugin、onBeforeBack、返回守卫等为 uni-router **自有扩展**，不属于 vue-router，**不作为本期功能**；monorepo 结构与实现的组织风格才参考它。

## monorepo 初始化（S1）

- 根 `package.json`（`"type":"module"`、`private:false`、`packageManager:"pnpm@10.30.0"`、`workspaces:["packages/*"]`；scripts：dev:playground:h5、build:playground:mp-weixin、typecheck、release）、`pnpm-workspace.yaml`、`.npmrc`、根 `tsconfig.json`、`.gitignore`、`.editorconfig`、`.prettierrc`、`eslint.config.js`。（core 为 UTS 源分发，不设 tsup build 脚本。）

- 验证：`pnpm install` 通过。

## packages/core（UTS，S2–S6）

### 打包与发布策略（重要，已核实）

`packages/core` **不做 tsup/rollup 通用打包**（无法产出"通吃全端含 App 原生"的运行时），采用**UTS 源分发**，由 uni-app x 工具链按平台现场编译。

依据官方 UTS 编译器规则：

- 目标语言为 JS（web/小程序）时：可引用 ts/js 文件，`uts2js` 只处理 `.uts`。

- 目标语言非 JS（Android→Kotlin）时：**仅可引用 ts 文件且当作 uts 处理**；纯 `.js` 编译产物无法被 Android 端引用。

结论与玩法：

- **不能**用 tsup 把 core 编成通用 JS 包去全端跑；tsup 只能产出 JS，且 JS 无法在 Android VDOM 原生端被引用。

- core 以 **`.uts`** **源码**随包分发（npm files 含 `utssdk/` 或源码目录），web/mp→JS、Android→Kotlin、iOS→Swift，由 uni-app x 编译器处理。

- **推荐分发形态**：官方 uni\_modules 插件（`uni_modules/<name>/utssdk` 放 UTS 源码；可选附 `js_sdk` 预编译 JS 仅给 JS 端/蒸汽模式用），App 原生经 HBuilderX 验证。

- 本期 `packages/core` 作为 npm 子包同时发布 `.uts` 源码；playground 用 workspace + 别名直接消费本地 `.uts` 源码编译验证。`node_modules` 下 `.uts` 的实际处理机制在 S2 先行实测确认。

```
packages/core/utssdk（或 src）
├── index.uts        # 对外导出 createRouter/useRouter/useRoute/useLink/onBeforeRouteX/类型/错误/常量
├── router/          # Router 主类与 createRouter、导航执行链、插件无关
│   ├── index.uts    # 主类：push/replace/back/beforeEach/beforeResolve/afterEach/getRoutes/hasRoute/resolve/isReady/onError/onRouteChange/syncRoute/guardRoute/install；performNavigation/executeNavigation/failNavigation/handleGuardResult
│   ├── location.uts # resolveLocation、isSameRouteLocation、currentRoute 构造
│   └── type.uts
├── matcher/         # RouteMatcher（Map path/name、参数匹配、严格模式、resolve 三形式）+ type.uts
├── guard/           # createGuardManager（全局/独享/组件内守卫执行链）+ helpers/{resolve,run}.uts
├── navigation/      # navigate.uts（navigateTo/switchTab/redirectTo/navigateBack/reLaunch 分流）、context.uts（getCurrentPages 封装/onLoad options）、helpers/uni-api.uts（promisify）
├── history/         # uni 页面栈历史适配（记忆栈、当前/后退、非路由器切换同步）
├── state/           # createRouteState（currentRoute ref、setCurrentRoute、markReady/onReady、onRouteChange）+ type.uts
├── composables/     # router.uts/route.uts/link.uts/guard/route-x.uts；index.uts
├── components/      # RouterLink.uvue（适配式导航组件）
├── errors/          # router-error.uts / navigation-failure.uts / is-navigation-failure.uts；index.uts
├── enums/           # router-error-code.uts；index.uts
├── constants/       # router.uts(ROUTER_SYMBOL)、defaults.uts(守卫超时/MAX_REDIRECT_DEPTH)、keys.uts(内部 key)；index.uts
├── types/           # route.uts / router.uts / guard.uts / error.uts / composables.uts；index.uts
└── utils/           # path.uts / query.uts / platform.uts(getPlatform 含 isAppX) / route.uts / id.uts / general.uts / type.uts；index.uts
```

### UTS 强类型适配要点

参考 uni-router 大量用 `Record<string,any>`，UTS（→Kotlin/Swift）不允许任意动态类型，改造：

- `query`：`Map<string,string>`，RouteLocation 提供原子读取 queryInt/queryNumber/queryBool。

- `params`/`meta` 等动态载荷：用 `UTSJSONObject` 或显式 `interface`；不用 `any`。

- 守卫返回 `NavigationGuardReturn`：UTS 可表达的联合（boolean | RouteLocationRaw | NavigationRedirect | Error | null）。

- 集合：优先 `Array`；KV 用 `Map`；JSON 用 `UTSJSONObject`。

### 关键 API 签名（对齐 vue-router 4；实现按 UTS 约束）

```uts
interface RouterOptions {
  routes: RouteConfig[]                 // 须与 pages.json 静态注册一致
  strict?: boolean                      // 默认 true
  guardTimeout?: number                 // 默认 10000，0 禁用
  readyTimeout?: number                 // 默认 0 永不超时
}
interface RouteConfig {
  path: string; name?: string;
  meta?: RouteMeta;
  beforeEnter?: NavigationGuard | NavigationGuard[]
}
interface RouteLocation {
  path: string; name?: string;
  meta: RouteMeta; query: Map<string,string>; params: Readonly<ParamObject>;
  fullPath: string; _synced?: boolean;
}
interface Router {
  readonly currentRoute: RouteLocation
  push(location): Promise<void>; replace(location): Promise<void>; back(delta?): Promise<void>
  beforeEach(guard): () => void; beforeResolve(guard): () => void; afterEach(guard): () => void
  getRoutes(): RouteConfig[]; hasRoute(name): boolean; resolve(location): RouteLocation
  isReady(): Promise<void>; onError(handler): () => void; onRouteChange(listener): () => void
  syncRoute(): void; guardRoute(location?, options?): Promise<RouteLocation>
  install(app): void
}
// 不暴露：addRoute/removeRoute/go/hash/嵌套/命名视图/scrollBehavior
type NavigationGuardReturn = void | boolean | RouteLocationRaw | NavigationRedirect | Error | null
type NavigationGuard = (to: RouteLocation, from: RouteLocation) => NavigationGuardReturn | Promise<NavigationGuardReturn>
type PostNavigationGuard = (to, from) => void
```

## packages/playground（uni-app x 测试/演示工程，S7）

**用户的验证载体，不用 vitest。** 真实 uni-app x 工程（`.uvue` + `<script lang="uts">`），消费本地 core `.uts` 源码，双端编译：

```
packages/playground/src/
├── App.uvue           # onLaunch: router.isReady().then(()=>guardRoute(...)) 冷启动守卫
├── main.ts            # createSSRApp + router.install
├── pages.json         # 静态注册全部页面（含分包）
├── manifest.json
├── router.config.uts  # 共享路由配置（path/name/meta/beforeEnter）
├── router.d.uts       # 模块增强 RouteNameMap（类型提示）
├── router.ts          # createRouter({ routes, ... })
├── pages/             # index / guards / navigation / params / about / not-found / test / tabbar 页等
└── components/RouterLink.uvue
```

**测试设计**：`pages/test/` 自校验页程序化遍历功能矩阵（匹配、各守卫触发顺序、push/replace/back 行为、参数/查询读写、错误分类、重复导航拒绝、守卫重定向+深度限制、isReady/onError），console+UI 输出 **PASS/FAIL**，作为无 vitest 回归载体。

## 实施顺序

| 步  | 内容                                                                                      | 产物        | 验证                            |
| -- | --------------------------------------------------------------------------------------- | --------- | ----------------------------- |
| S1 | monorepo 初始化                                                                            | 空壳工程      | `pnpm install`                |
| S2 | 搭 playground 最小 uni-app x 工程（先验证 `.uts` 消费/分发机制）                                        | 最小可编译工程   | `dev:h5`、`build:mp-weixin` 通过 |
| S3 | core 骨架（utils/errors/enums/constants/types，`.uts`）                                      | 类型全集+工具   | playground 编译集成               |
| S4 | matcher + guard                                                                         | 匹配+守卫执行   | test 页验证                      |
| S5 | state + navigation + history + router 主流程                                               | 导航+守卫链    | test 页验证导航五方式/守卫链/重复/重定向      |
| S6 | composables（useRouter/useRoute/useLink/onBeforeRouteX）+ components(RouterLink)+ install | Vue 集成    | test 页+手动                     |
| S7 | 补齐演示页面+RouterLink+自校验页完善                                                                | 完整测试/演示工程 | H5 手测全流程；mp 编译通过              |
| S8 | 回归 + README（含"不支持功能清单"）+ 分发包清单（npm 源码 + uni\_modules utssdk 布局说明）                       | 可交付       | H5/mp 全 PASS；App 打包说明         |

## 验证方式（端到端）

1. H5：`pnpm dev:playground:h5` + `pages/test` 自校验全部 PASS。
2. 小程序：`pnpm build:playground:mp-weixin` 编译通过；devtools 导入运行 test 页。
3. App 原生（VDOM→Kotlin/Swift）：交付后由用户在 HBuilderX 手动编译验证。

## 风险与提示

- **UTS 消费/分发形态**：playground 在 web/mp 经 workspace+别名消费 core `.uts`；App 分发按 uni\_modules `utssdk`（S2 先行验证）。

- **UTS 强类型约束**：`Record<string,any>`→`Map`/`UTSJSONObject`/typed；以 S2/S3 编译实测为准。

- **工具链**：`@dcloudio/*` 用支持 `.uvue`+`.uts` 与 H5/mp-weixin 的 AppX 版本。

- **isAppX 判定**：结合运行时标志，避免与 uni-app App 混淆。

- **getCurrentPages()/onLoad options**：uni-app x 逻辑层可用；类型面按 uni-app x typings 校验。

