# route-gen 页面级 style 字段（issue #10）实施方案

## 需求

GitHub issue #10：ux-router 0.8.3 + HBuilderX 5.34（VDOM）+ Windows。希望在 `defineUniPage` 中像 `title` 一样声明页面级 `style`，直接写入 `pages.json` 的 `style` 字段，用于 `navigationStyle: 'custom'` 等页面配置。

- 实际行为：`style` 属未识别字段被忽略，产生 unknownFields 告警；生成的 `pages.json` 只有 `navigationBarTitleText`。
- 期望：`style` 中的键按字面量原样合并进 `pages.json` 对应页面的 `style`；`title` 仍生成 `navigationBarTitleText`；同名键需明确优先级。
- issue 建议：`defineUniPage` 增加 `style?: Record<string, any>`；`<route-config>` 块同步支持 `style`；宏类型声明补充 `style`。

## 需求分析

- 落点均在构建期插件 `packages/core/node/`（不涉及 UTS 运行时）：
  - `shared/common/types.ts`：`PageSpec` / `Draft` 增加 `styleExtra`（与 `metaExtra` 同构：`Array<{ key; value: LiteralValue }>`）。
  - `shared/parsing/extract.ts`：`emptySpec()` 补 `styleExtra: []`；`specFromEntries()` 增加 `case 'style'`（对象字面量 → 条目）；`mergeSpecs()` 按 metaExtra 同策略合并（宏键优先、块补充去重）。宏与 `<route-config>` 块共用 `specFromEntries`，故块自动获得 `style` 支持。
  - `shared/rendering/generate.ts`：`buildDrafts()` 透传 `styleExtra`；`renderPagesJson()` 的 `styleFor()` 合并 `title → navigationBarTitleText` 与 `styleExtra`（需字面量 → JSON 值转换，`raw` 表达式无法写入 JSON → 告警忽略）；`renderMacroDts()` 增加 `style`。
  - `packages/playground/types/define-uni-page.d.ts`：静态宏类型声明同步补 `style`（与 `renderMacroDts` 保持一致）。
- 优先级决策（issue 未强制，取 issue 首选并写入文档）：**`style` 显式键优先于 `title` 生成的 `navigationBarTitleText`**（同名键以 style 为准）。`title` 仍照常进入路由 `meta.title`。
- 取值约束：`style` 仅接受 JSON 可序列化字面量（string / number / boolean / null / 数组 / 对象，含平台细分如 `h5` / `app-plus` 嵌套对象）；函数 / 标识符 / 模板串（`raw`）无法表达为 JSON，告警并忽略该键。
- 双发行版：`node/` 改动经 `tsup` 出 `dist/plugin`，再由 `pnpm sync:uni-modules` 镜像到 `uni_modules/ux-router/plugins`；`utssdk` 不受影响。
- 文档：`docs/src/guide/file-conventions.md` 与 `en/...` 字段表补 `style` 行；三处 changelog 登记（docs 中/英 + uni_modules）；`For` core README 若有字段枚举一并补。
- playground 复现：在演示页 `defineUniPage` 增加 `style`（如 `navigationStyle: 'custom'`），由 HBuilderX dev 构建时重生成 `pages.json` 验证。
- 无 `node/` 单元测试基建，采用「临时 fixture 直接调用已构建插件的 `buildStart`」做运行时验证。

## 伪代码设计

```ts
// types.ts
type PageSpec = { /* ...既有字段 */; styleExtra: Array<{ key: string; value: LiteralValue }> }
type Draft   = { /* ...既有字段 */; styleExtra: Array<{ key: string; value: LiteralValue }> }

// extract.ts
const emptySpec = () => ({ /* ... */, styleExtra: [] })
case 'style': if (value.kind === 'object') spec.styleExtra = [...value.entries]; break
// mergeSpecs：
const styleKeys = new Set(macro.styleExtra.map(e => e.key))
merged.styleExtra = [...macro.styleExtra, ...block.styleExtra.filter(e => !styleKeys.has(e.key))]

// generate.ts —— 字面量 → JSON 值（raw 不可序列化）
type JsonResult = { ok: true; value: unknown } | { ok: false }
function literalToJson(v: LiteralValue): JsonResult
// styleFor：
const style = {}
if (d.title !== null) style.navigationBarTitleText = d.title
for (const e of d.styleExtra) { const r = literalToJson(e.value); if (!r.ok) { warn(...); continue } style[e.key] = r.value }
return Object.keys(style).length ? style : undefined

// renderMacroDts：在 tab 后追加 style?: Record<string, any>
```

## 疑问清单

无（issue #10 已给出字段签名、合并规则与三处落点；优先级取 issue 首选的「style 优先」，在文档中明确）。

## 实施记录

### 代码

- `packages/core/node/shared/common/types.ts`：`PageSpec` / `Draft` 增加 `styleExtra: Array<{ key; value: LiteralValue }>`。
- `packages/core/node/shared/parsing/extract.ts`：`emptySpec()` 补 `styleExtra: []`；`specFromEntries()` 增加 `case 'style'`（对象字面量逐键收集，宏与 `<route-config>` 块共用，块自动获得支持）；`mergeSpecs()` 按 metaExtra 同策略合并（宏键优先、块补充去重）。
- `packages/core/node/shared/rendering/generate.ts`：`buildDrafts()` 透传 `styleExtra`；新增 `literalToJson()`（字面量 → JSON 值，`raw` 返回 `{ ok: false }`）；`renderPagesJson()` 的 `styleFor()` 合并 `title → navigationBarTitleText` 与 `styleExtra`（style 显式键覆盖 title，raw 告警忽略），并保留 `globalStyle` 等手写字段；`renderMacroDts()` 增加 `style`。
- `packages/core/node/routes-gen/index.ts`：`emptyDraft()` 补 `styleExtra: []`（routesGen 由 pages.json 推导，style 仅进 pages.json，不进路由表）。
- `packages/playground/types/define-uni-page.d.ts`：静态宏类型声明补 `style`（与 `renderMacroDts` 逐字一致）。
- 镜像：`pnpm sync:uni-modules` 后 `node scripts/sync-uni-modules.mjs --check` 输出「镜像一致」（utssdk ≡，plugins ≡）。

### 演示与文档

- playground：`pages-sub/demo/demo.uvue` 的 `defineUniPage` 增加 `style: { navigationBarTextStyle: 'white', navigationBarBackgroundColor: '#007AFF' }`；用已构建插件对 playground 重生成，`pages.json` 仅该页新增两条 style（`routes.gen.uts` / `types/*` 无变化，证明生成一致性）。
- docs：`guide/file-conventions.md` 与 `en/...` 字段表新增 `style` 行（含平台细分与同名键优先级说明）。
- changelog 三处登记（`docs/src/changelog.md`、`docs/src/en/changelog.md`、`playground/uni_modules/ux-router/changelog.md`），版本 **0.9.0 / 2026-11-27**（MINOR：向后兼容的新增能力）。
- 版本级发布：4 处 `package.json` 版本号统一 bump 至 0.9.0（根 / core / docs / uni_modules）；发布博客 `versions/0.9.0.md` 已写（验证章节如实标注 HBuilderX 视觉复测待执行）。
- core README 未枚举宏字段，未改。

### 验证

| 项目 | 结果 |
| --- | --- |
| 临时 fixture 直调已构建插件 `buildStart`（真实扫描 + 渲染） | ✅ 7 项断言全过：title→navigationBarTitleText、style 标量/嵌套透传、style 覆盖 title、globalStyle 保留、raw 告警忽略且其余键保留 |
| `pnpm lint` | ✅ 通过 |
| `pnpm typecheck` | ✅ 通过 |
| `node scripts/sync-uni-modules.mjs --check` | ✅ utssdk / plugins 均一致 |
| `pnpm build:docs` | ✅ 通过（含死链检查） |
| HBuilderX H5 / App 真机运行验证 | ⏳ 未执行（本环境无法启动 HBuilderX；机制已由 fixture 真实插件运行覆盖，视觉表现建议在 playground 用 `navigationStyle: custom` 页面复测） |

**结论**：功能与类型、构建、镜像、文档静态与运行时（生成器级）均已验证通过；建议维护者在 HBuilderX 中以 `navigationStyle: 'custom'` 页面做一次视觉复测后合并。
