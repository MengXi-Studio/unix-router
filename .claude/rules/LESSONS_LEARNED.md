# 经验教训记录（Lessons Learned）

> 仅收录经运行验证（DoD 通过）后确认的坑与特殊处理逻辑。

## 2026-09-22 路由生成插件（route-gen，0.8.0）

### 1. HBuilderX 项目自定义 vite.config.ts 必须自行引入 uni()
- HBuilderX 启动时，项目根存在 `vite.config.{js,ts,mjs,mts}` 会**整体替换**内置配置（`UNI_CLI_CONTEXT/vite.config.js`，其中含 `plugins: [uni()]`），导致 uni 插件链全失、`/main` 404、`transformIndexHtml` 缺失。
- 修复：在项目 `vite.config.ts` 中用 `createRequire('<HBuilderX>/plugins/uniapp-cli-vite/package.json')` 解析并引入 `@dcloudio/vite-plugin-uni`（源码依据：`@dcloudio/vite-plugin-uni/dist/cli/utils.js` 的 `addConfigFile/resolveConfigFile`）。

### 2. UTS 编译到 JS（H5）端可选字段缺省值是 undefined 而非 null
- Kotlin（App）端可选字段缺省为 `null`，但 H5/JS 端为 `undefined`；`if (x === null)` 后直接 `.length` / `.size` 在 H5 抛 `TypeError: Cannot read properties of undefined`。
- 修复模式：先归一化再判断——`const v: T | null = raw.field ?? null`。本次修复 `pickAnimation` / `toUniAnimation` / `pickEvents` 三处（已随 0.8.0 发布）。跨端空值判断必须先归一化。

### 3. tsup 打包 CJS 产物的 import.meta 垫片
- `import.meta` 在 CJS 产物中不合法：用 `define` 将 `import.meta.url` 替换为自定义标识符，并在 banner 注入 `var 标识符 = require('url').pathToFileURL(__filename).href` 垫片。
- unplugin 3 为纯 ESM，CJS 适配器依赖其 provide 方式，必须 bundle 进产物，不可留 external。

### 4. HBuilderX CLI 无设备也能验证 App 编译链
- `cli publish app-android --type appResource --project <name>` 走完整 Kotlin/UTS 编译链并本地导出资源包，无需 adb 设备/模拟器/SDK，是 App 端「编译级验证」的最低成本手段；运行时行为仍需真机。
- `cli launch web --project <name>` 启动 H5 dev server，端口自动 +1（如占用 5173 则用 5174）。

### 5. playground 的 uni_modules/ux-router 是拷贝而非链接
- `packages/playground/uni_modules/ux-router` 是 core `src/` 的**静态拷贝**；改 core 源码后必须手动 `Copy-Item` 同步，否则 playground 仍跑旧代码。

### 6. CDP 运行时验证 headless Chrome 的沙箱限制
- Chrome headless 需写 `C:\Windows\SystemTemp\scoped_dir*`，默认沙箱会拦截（`hit restricted`），需放行后运行；用 `Runtime.consoleAPICalled` / `exceptionThrown` / `Network.responseReceived` 捕获运行时证据，shadow DOM 需穿透查询。

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
