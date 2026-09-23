import { defineConfig } from 'tsup'

/**
 * 路由生成插件构建：packages/core/node → dist/plugin
 * bundle unplugin 等依赖，用户侧只需安装 @meng-xi/unix-router 一个包；
 * node/ 源码位于 UTS 编译扫描范围外，不影响主入口 UTS 编译。
 */
export default defineConfig({
	// 三插件统一入口：node/index.ts re-export routeGen / pagesGen / routesGen
	entry: ['node/index.ts'],
	// 与 package.json exports 的 './vite-plugin' 子路径对齐：
	// dist 根归属包主入口（.uts 源码），Node 构建产物独立成目录，两条编译链物理隔离
	outDir: 'dist/plugin',
	format: ['esm', 'cjs'],
	// 显式双扩展：package.json 为 "type": "module"，.js 会被 Node 按 ESM 解析；
	// .mjs/.cjs 保证 import 与 require 各自精确命中 exports 的 import/require 条件
	outExtension: ctx => ({ js: ctx.format === 'esm' ? '.mjs' : '.cjs' }),
	// CJS 下 esbuild 会把 import.meta 垫成空对象；define 到标识符，再由 banner 注入真实值（define 仅接受实体名）
	esbuildOptions: (opts, ctx) => {
		if (ctx.format === 'cjs') {
			opts.define = {
				...(opts.define ?? {}),
				'import.meta.url': '__uniRouterImportMetaUrl',
				'import.meta.dirname': '__uniRouterImportMetaDirname'
			}
		}
	},
	// 与上方 define 配套：为 CJS 注入 import.meta.url / import.meta.dirname 的真实实现
	banner: ctx =>
		ctx.format === 'cjs'
			? {
					js: 'var __uniRouterImportMetaUrl = require("node:url").pathToFileURL(__filename).href;var __uniRouterImportMetaDirname = __dirname;'
				}
			: { js: '' },
	// 生成 index.d.ts / index.d.cts：exports './vite-plugin' 的 types 条件指向 index.d.ts；
	// index.d.mts 由 build script 在 tsup 结束后复制生成（TS 解析显式 `import './x.mjs'` 时按 .d.mts 配对
	// 查找声明，如 playground 从 uni_modules 内以 index.mjs 引入；tsup onSuccess 时机早于 dts 写出，不可用）
	dts: true,
	bundle: true,
	platform: 'node',
	// 插件运行于用户 Node 的 vite 进程，node18 为保守兼容基线
	target: 'node18',
	sourcemap: false,
	// 每次清空重建，防旧产物残留
	clean: true
})
