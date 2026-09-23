import { defineConfig } from 'tsup'

/**
 * 路由生成插件构建：packages/core/node → dist/plugin
 * bundle unplugin 等依赖，用户侧只需安装 @meng-xi/unix-router 一个包；
 * node/ 源码位于 UTS 编译扫描范围外，不影响主入口 UTS 编译。
 */
export default defineConfig({
	entry: ['node/index.ts'],
	outDir: 'dist/plugin',
	format: ['esm', 'cjs'],
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
	banner: ctx =>
		ctx.format === 'cjs'
			? {
					js: 'var __uniRouterImportMetaUrl = require("node:url").pathToFileURL(__filename).href;var __uniRouterImportMetaDirname = __dirname;'
				}
			: { js: '' },
	dts: true,
	bundle: true,
	platform: 'node',
	target: 'node18',
	sourcemap: false,
	clean: true
})
