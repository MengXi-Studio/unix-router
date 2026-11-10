# 功能实现工作流（文档驱动四步）

> 本规则定义「实现功能 / 修改行为」类任务的强制推进流程，与 `core-protocol.md`（管行为底线）、`unix-router.md`（管 UTS 写法红线）互补——本文件管怎么推进。
> 核心机制：**文档驱动**——需求分析、伪代码方案、疑问与回答、实施记录全部落盘到 `.trae/documents/` 的任务文档中，跨会话持久；对话只做通知与执行，**不在对话中回答疑问**。
> 触发条件：用户要求新增路由能力 / 插件 / 组合式 API / 构建期插件能力，或修改既有导航行为、类型签名、平台适配。纯问答、架构讨论、纯文档翻译不适用（见第四步「纯文档任务简化路径」）。

## 第零步：确认影响面（动手前必读）

本仓库三个包 + 两套分发形态 + 双语文档，改动前先定位落点，避免漏改：

| 落点 | 路径 | 说明 |
| --- | --- | --- |
| 运行时源码 | `packages/core/src/` | UTS 源码（router / guard / plugin / composables / types / utils / enums），**唯一真实源** |
| 构建期插件 | `packages/core/node/`（route-gen / pages-gen / routes-gen / shared） | 文件路由生成，Node 侧 TS，产物 `dist/plugin` |
| 包入口导出 | `packages/core/src/index.uts` | 新增对外 API 必须在此导出 |
| 演示 App | `packages/playground/`（`pages` 主包 / `pages-sub` 分包 / `router` / `store`） | 功能需可现场复现 |
| uni_modules 镜像 | `packages/playground/uni_modules/ux-router/{utssdk,plugins}` | **纯镜像，禁止手改**，由 `pnpm sync:uni-modules` 生成 |
| 文档站 | `packages/docs/src/`（中文）与 `packages/docs/src/en/`（英文镜像） | 双份都要改，`guide` / `api` / `changelog.md` |
| README × 4 | 根 `README.md` / `README-en.md`、`packages/core/README.md` / `README-en.md` | 新增特性、API 变更、平台限制 |
| 发布产物 | `packages/docs/src/changelog.md`（中英）、`packages/playground/uni_modules/ux-router/changelog.md`、`versions/<版本>.md` | 见第四步发布清单 |
| AI 规则 / 技能 | `.trae/rules/unix-router.md`、`.trae/skills/unix-router/` | 对外行为或红线变化时同步 |
| 经验教训 | `.trae/documents/LESSONS_LEARNED.md` | 仅 DoD 通过后写入 |

## 第一步：建立任务文档（需求分析与伪代码设计，写任何代码之前）

- 在 `.trae/documents/` 下按 **需求类型 / 日期** 两级目录归档：
  `.trae/documents/<需求类型>/<YYYY-MM-DD>/<模块>-<功能名>.md`
  - 第一层：`新增`（新能力）/ `优化`（既有行为改进）/ `修复`（缺陷）/ `文档`（纯文档与规则同步）；
  - 第二层：任务创建当日，隔离同名文件；
  - 文件名：`<模块>-<功能名>.md`（如 `新增/2026-11-10/interceptor-tabbar-拦截.md`）；跨模块的用 `<功能名>.md`。
- 文档固定结构（首次创建时生成，后续步骤逐节填充）：

```markdown
# <功能名> 实施方案

## 需求
<用户原话要点与任务目标>

## 需求分析
<影响面清单：core/src 哪些文件 · 是否涉及 node/ 构建期 · 对外导出与类型签名变化 ·
双发行版（npm / uni_modules）是否一致 · playground 复现页面 · docs 中英双份 · README ４份 ·
changelog 与 versions 博客 · 是否需要更新 .trae 规则与技能>

## 伪代码设计
<新增/变更的类型签名、Map 结构、插件 hook 时序、守卫返回值分支、条件编译分段——不写最终可运行代码>

## 疑问清单
### Q1 <问题原文>（影响：<范围>）
回答：

## 实施记录
（实现完成后回填）
```

- 建档后在对话中告知用户文档路径，请其**在文档「疑问清单」的「回答：」处作答**。

## 第二步：等待并读取回答（跨会话循环）

- 用户回答后再次发起会话时：**读取任务文档完整内容**（不是只读自己写的段落），逐条核对疑问清单：
  - 仍有未回答的疑问 → 在文档中补充新疑问 / 细化问题，通知用户继续作答，**回到本步等待**；
  - 全部澄清 → 进入第三步。
- 判定标准：回答只认文档中「回答：」处的内容；对话里口头回答的，须由 AI 转记入文档后方可生效（保证文档自包含）。
- 本步可循环多轮，直到疑问清单全部闭环。

## 第三步：实现与验证（DoD 硬性门槛）

### 落码顺序

1. **类型先行**：`packages/core/src/types/` 定义或调整类型；UTS 约束见 `unix-router.md` 红线（`Map<string, string>`、无 `undefined`、对象字面量只用 `type`、类实现含方法的对象、条件必须布尔）。
2. **运行时实现**：`packages/core/src/` 内按 router / guard / plugin / composables 分层落码，跨端差异用条件编译（`#ifdef APP` / `#ifdef WEB`）。
3. **对外导出**：`packages/core/src/index.uts` 补齐导出（含类型导出）。
4. **构建期链路**（如涉及 `node/`）：`core/node` 改动需 `cd packages/core && pnpm build` 产出 `dist/plugin`。
5. **镜像同步**：`pnpm sync:uni-modules`（改 `core/src` 或 `core/node` 后必须执行；**两者是静态拷贝，不跑镜像 playground 仍跑旧代码**）。
6. **演示与复现**：在 `packages/playground` 增加或调整页面（主包 / 分包按需），确保功能可现场验证。
7. **文档双写**：`docs/src` 与 `docs/src/en` 同结构同步；README ４份按需。
8. **规则/技能同步**：对外行为或红线变化时更新 `.trae/rules/unix-router.md` 与 `.trae/skills/unix-router/`。

### 验证清单

必做：

1. `pnpm sync:uni-modules:check` —— 双发行版镜像无漂移（改 core 后必跑；有漂移退出码 1）。
2. `pnpm typecheck` 与 `pnpm lint` —— 静态检查通过。
3. `pnpm build:docs` —— VitePress 构建通过（含死链检查），确认中英文页面均能生成。
4. H5 实测：`cli launch web --project <项目名>` 启动 dev server（端口占用会自动 +1），在浏览器复现目标路径，确认无控制台报错；暖启动下的导航/守卫行为必须实际点击触发。
5. App 编译级验证：`cli publish app-android --type appResource --project <playground 绝对路径>` —— 走完整 UTS→Kotlin 编译链，无线设备即可暴露强类型问题（Kotlin 类型不匹配、接口默认值等只在云打包/原生编译中出现）。多仓库存在同名 playground 时**必须用绝对路径**消歧。
6. 清理临时调试代码（临时日志、断点、临时页面），保留必要业务日志。

按需：

7. 改动画 / 原生导航参数：核对官方平台门槛（App Android 4.18+ / iOS 4.25+ / HarmonyOS 4.61+，小程序不支持动画字段），并确认 `redirectTo` / `reLaunch` / `switchTab` 不透传动画。
8. 改守卫 / 拦截相关：覆盖「冷启动直达 URL 不执行守卫链」场景（需先进入非守卫页再应用内导航），以及 `<navigator>` 组件与 tabBar 点击不经 `uni.switchTab` 的盲区。
9. 改构建期插件：`core/node` 产物与 `uni_modules/ux-router/plugins` 镜像一致；生成物需一并提交 git。
10. 小程序端受影响：在目标小程序开发者工具中编译一次，确认条件编译分支正确。

## 第四步：回填与收尾（任务完成后，必须）

- **回填任务文档**的「实施记录」章节：
  - 方案落地形态（与伪代码设计的差异说明）· 变更文件清单 · 验证方式与结果（实际执行的命令与观察到的输出）· 遗留 TODO（含位置；无则写「无」）。
- 向 `.trae/documents/implementation-log.md` 追加一行索引：日期 · 任务文档相对链接 · 一句话结果（首次创建需先建该文件）。
- 用户可见变更登记（**三处，缺一不可**）：
  - `packages/docs/src/changelog.md` + `packages/docs/src/en/changelog.md`（文档站更新日志页）；
  - `packages/playground/uni_modules/ux-router/changelog.md`（uni_modules 分发版）；
  - 版本级发布另需 `versions/<x.y.z>.md` 发布博客（与本仓库既有博客同风格）。
- 版本级发布额外清单：版本号 4 处统一（根 `package.json`、`packages/core`、`packages/docs`、`packages/playground/uni_modules/ux-router`）→ 三处 changelog → `versions/` 博客 → README ４份复核。
- 达到 DoD 且**只有运行验证通过**后，才允许把「只有跑起来才知道」的坑写入 `.trae/documents/LESSONS_LEARNED.md`（格式与该文件既有条目一致：现象 / 根因 / 修复 / 验证手段）。
- 回填完成才算任务闭环；「代码写完但未回填」视为未完成。

### 纯文档 / 规则任务简化路径

仅改文档、changelog、`.trae` 规则或技能时，可跳过第一步的疑问清单与第二步等待，直接执行：影响面确认 → 改动 → 双份文档一致性核对（中英 / ４份 README / 规则与技能口径一致）→ 回填实施记录。
