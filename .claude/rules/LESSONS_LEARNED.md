# 经验教训记录（Lessons Learned）

> 仅收录经运行验证（DoD 通过）后确认的坑与特殊处理逻辑。

## 2026-09-22 路由生成插件（route-gen，0.8.0）

### 1. HBuilderX 项目自定义 vite.config.ts 必须自行引入 uni()
- HBuilderX 启动时，项目根存在 `vite.config.{js,ts,mjs,mts}` 会**整体替换**内置配置（`UNI_CLI_CONTEXT/vite.config.js`，其中含 `plugins: [uni()]`），导致 uni 插件链全失、`/main` 404、`transformIndexHtml` 缺失。
- 修复（两种，均经 H5 运行验证）：
  - 方式 A：项目依赖安装 `@dcloudio/vite-plugin-uni`，vite.config 直接 `import uni from '@dcloudio/vite-plugin-uni'`（官网写法）。**必须锁与 HBuilderX 内置一致的精确版本**（查 `<HBuilderX>/plugins/uniapp-cli-vite/node_modules/@dcloudio/vite-plugin-uni/package.json` 的 version，HBuilderX 5.26 为 `3.0.0-alpha-5020520260821001`），否则 vite.config 与编译链版本分裂。H5 dev server 由内置 vite v5.2.8 承载，config 内 import 解析到项目 node_modules，同版本无行为差异。
  - 方式 B（无项目依赖时）：`createRequire('<HBuilderX>/plugins/uniapp-cli-vite/package.json')` 从内置插件目录解析引入（源码依据：`@dcloudio/vite-plugin-uni/dist/cli/utils.js` 的 `addConfigFile/resolveConfigFile`）。

### 2. UTS 编译到 JS（H5）端可选字段缺省值是 undefined 而非 null
- Kotlin（App）端可选字段缺省为 `null`，但 H5/JS 端为 `undefined`；`if (x === null)` 后直接 `.length` / `.size` 在 H5 抛 `TypeError: Cannot read properties of undefined`。
- 修复模式：先归一化再判断——`const v: T | null = raw.field ?? null`。本次修复 `pickAnimation` / `toUniAnimation` / `pickEvents` 三处（已随 0.8.0 发布）。跨端空值判断必须先归一化。

### 3. tsup 打包 CJS 产物的 import.meta 垫片
- `import.meta` 在 CJS 产物中不合法：用 `define` 将 `import.meta.url` 替换为自定义标识符，并在 banner 注入 `var 标识符 = require('url').pathToFileURL(__filename).href` 垫片。
- unplugin 3 为纯 ESM，CJS 适配器依赖其 provide 方式，必须 bundle 进产物，不可留 external。

### 4. HBuilderX CLI 无设备也能验证 App 编译链
- `cli publish app-android --type appResource --project <name>` 走完整 Kotlin/UTS 编译链并本地导出资源包，无需 adb 设备/模拟器/SDK，是 App 端「编译级验证」的最低成本手段；运行时行为仍需真机。
- `cli launch web --project <name>` 启动 H5 dev server，端口自动 +1（如占用 5173 则用 5174）。

### 5. tsup onSuccess 时机早于 dts 产物写出，复制声明文件须用构建脚本串行
- 需要对 dts 产物做后处理（如补 `index.d.mts`：TS 解析显式 `import './x.mjs'` 时按 `.d.mts` 配对查找声明）时，`onSuccess` 在 ESM/CJS 构建完成即触发，此时 dts worker 尚未写出文件，`copyFileSync` 抛 ENOENT 且错误被 PowerShell CLIXXML 包装吞掉、进程退出码仍为 0，极易误判成功。
- 可靠做法：build script 串行——`"build": "tsup && node -e \"...copyFileSync...\""`，`&&` 保证 tsup（含 dts）完全结束后才执行后处理。

### 6. playground 的 uni_modules/ux-router 是拷贝而非链接
- `packages/playground/uni_modules/ux-router` 是 core `src/` 的**静态拷贝**；改 core 源码后必须手动 `Copy-Item` 同步，否则 playground 仍跑旧代码。

### 7. CDP 运行时验证 headless Chrome 的沙箱限制
- Chrome headless 需写 `C:\Windows\SystemTemp\scoped_dir*`，默认沙箱会拦截（`hit restricted`），需放行后运行；用 `Runtime.consoleAPICalled` / `exceptionThrown` / `Network.responseReceived` 捕获运行时证据，shadow DOM 需穿透查询。

### 8. 生成器输出对象属性名必须做标识符安全检查
- route-name.gen.d.ts 曾直接拼接 `d.name` 作属性名：显式声明含连字符的 name（如 'goods-detail'）生成非法 TS（连字符属性名须加引号）。渲染层对非 `/^[A-Za-z_$][\w$]*$/` 的 key 复用 `quote()` 加引号；值链路（routes.gen.uts 的 name、pages.json 的 JSON.stringify）天然为字符串值/带引号 key，不受影响。

## 2026-09-23 三插件拆分（route-gen / pages-gen / routes-gen）

### 1. Shell 工具 cwd 不持久，脚本依赖 process.cwd() 会静默写错目录
- Shell 调用间 cwd 会重置（如回到仓库根），插件以 `process.cwd()` 解析相对路径时，扫描 0 页面也会**静默生成空产物**（pages.json / routes.gen.uts / dts）——曾在仓库根生成过 4 个垃圾文件。
- 对策：运行脚本必须显式传 cwd 参数；冒烟前后用 `git status` 兜底确认无误写。

### 2. 生成文件 hash 比对前先归一化「设计内差异」
- `routes.gen.uts` 文件头的 declareHint 提示行随插件而异（routeGen 提示宏/块，routesGen 提示 routes.ext.uts），属设计内差异；hash 比对前用正则归一化为统一占位，其余内容才要求逐字节一致。

### 3. preserveRouteChanges 的 beforeEnter 缩进往返恒等
- 解析既有生成文件时 `dedentField` 去缩进、写回时 `reindent` 补缩进，往返恒等必须由 **dedent 单侧**保证（无条件剥前缀）；reindent 若加 `startsWith(indent)` 守卫，dedent 后仍带旧前缀的行不会再补齐，每次重生成缩进翻倍。

### 4. unplugin 工厂默认 root 取 cwd，vite 环境须在 configResolved 修正
- HBuilderX 的 cwd ≠ 项目根，相对路径会解析错位；`.vite()` 适配器必须在 `configResolved(config)` 中用 `config.root` 重解析所有 FileRef，再执行生成。

### 5. 多插件产物顺序对齐：排序 + 首页移位
- pages.json 顺序（主包在前、分包在后、页内保持声明序）与 routeGen 扫描序（path `localeCompare` 排序）不同；routesGen 从 pages.json 推导后需显式 `sort` 并把 entryPage 移至主包首位，两插件产物才能逐字节一致（含 name dts）。

## 2026-09-24 playground 重构（分包化 + 守卫/动画演示，H5 运行验证）

### 1. dev server 会漏掉外部进程的文件编辑，transform 用旧源码反复报错
- HBuilderX 启动的 vite dev server 对外部工具（如编辑器/脚本）写入的 `.uvue` 改动可能不触发模块失效：每次请求都重新执行 uts transform，但读到的仍是**旧源码**（错误体 pluginCode 中 import 还是改前路径）。诊断特征：curl script 子模块 500 且错误体内源码与磁盘文件不一致。对策：重启 dev server。

### 2. H5 冷启动（直连 URL）不执行路由守卫（core 设计行为）
- 冷启动走 `initRoute() → syncRoute()`（router/index.uts）仅同步路由状态；守卫链只在 `performNavigation`（router.push/replace、interceptUniApi 拦截的 uni.navigateTo）中执行。运行时验证守卫必须「先加载非守卫页 → 应用内触发导航」，直连守卫页 URL 断言重定向必假失败；对当前页重复 navigateTo 会命中 DUPLICATED（也是判别信号）。

### 3. onLoad options 缺 key 归一化是页面层必修项（非仅插件数据）
- H5 端 `options['k'] as string | null` 编译期断言不改运行时：缺 key 得 `undefined`，`undefined !== null` 判空穿透，随后 `.length` 抛 TypeError。必须 `(options['k'] ?? null) as string | null` 归一化——是 09-22 #2（跨端空值先归一化）在页面 onLoad 参数上的具体化。CDP 验证须覆盖「无 query 直连」形态才能暴露。

### 4. UTS 编译器对 H5 相对导入的两种输出形态可当解析成败信号
- 可解析的相对导入输出根绝对路径（`from "/data/goods.uts?import"`）；解析失败的保持相对形态仅去扩展名（`from "../store/index"`）交由 vite import-analysis 报错。看编译产物 import 形态即可判断解析成败，无需猜测。

### 5. CDP harness：资源错误走 Network 通道才能按 URL 过滤
- `Log.entryAdded` 的 "Failed to load resource" 文本不含 URL（favicon.ico 404 无法按 URL 过滤、会误报 FAIL）；资源错误改由 `Network.responseReceived` 承载（可按 URL 排除 favicon 噪声，favicon 404 属浏览器自动请求，非业务错误）。

### 6. WEB 分支动态构造 uni.* options 须集中 as any 桥接
- H5 端 `uni.navigateTo/switchTab/redirectTo/reLaunch` 的 options 含 required 字段 `url`，UTSJSONObject 下标动态赋值无法静态满足 → 每次编译刷 4 条类型警告（navigateBack 无 required 字段故不报）。收敛到单个 `#ifdef WEB` 桥接函数（注入回调 + 按需动画字段 + `return options as any`），差异字段（url/delta）留在调用处；运行时 JS 输出不变。验证断言点：动画字段桥接后 H5 进入动画仍播放（`H5 进入动画播放 -> slide-in-right 300ms` 日志）。
