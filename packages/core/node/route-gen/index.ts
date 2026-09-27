/**
 * routeGen 插件：页面文件宏/块 → pages.json + routes.gen.uts 全量流水线
 *
 * 架构对齐 generateUni 两阶段流水线：
 *   Phase 1 扫描页面目录 + 宏/块 → 内存 pages → 写 pages.json（保留手写字段）
 *   Phase 2 pages → name 规范化 → routes.gen.uts（preserveRouteChanges）+ 可选 dts
 * defineUniPage 宏在 transform 剥离（等行数注释占位）；<route-config> 块经虚拟模块拦截为空模块。
 */
import { createUnplugin } from 'unplugin'
import { BLOCK_VIRTUAL_PREFIX, LOG_PREFIX, PLUGIN_NAME, isRouteConfigBlockRequest } from '../shared/common/constants'
import { stripDefineUniPage } from '../shared/parsing/extract'
import { buildDrafts, buildNames, renderMacroDts, renderPagesJson, renderRouteNameDts, renderRoutesGen } from '../shared/rendering/generate'
import { RouteGenOptions, ResolvedOptions, resolveOptions } from '../shared/common/options'
import { scanPages } from '../shared/scanning/scan'
import { createSerialDebounced, readIfExists, writeFileIfChanged } from '../shared/common/utils'

/** 路由生成插件 */
export const routeGenUnplugin = createUnplugin<RouteGenOptions | undefined>((raw = {}) => {
	// 默认基于 cwd 解析；vite 下由 configResolved 以真实项目根重新解析（HBuilderX 进程 cwd 非项目根）
	let options: ResolvedOptions = resolveOptions(raw)

	/** 路由生成插件日志 */
	const log = (...args: unknown[]): void => {
		if (options.verbose) console.log(LOG_PREFIX, ...args)
	}

	/** 路由生成插件警告日志 */
	const warn = (...args: unknown[]): void => {
		console.warn(LOG_PREFIX, ...args)
	}

	/** 路由生成插件错误日志 */
	const fail = (msg: string): void => {
		if (options.errorStrategy === 'warn') {
			warn(msg)
			return
		}

		throw new Error(`${LOG_PREFIX} ${msg}`)
	}

	/** 两阶段流水线（构建期同步执行，天然串行无竞态） */
	function regenerate(reason: string): void {
		if (!options.enabled) return

		const scan = scanPages(options)
		scan.warnings.forEach(w => warn(w))
		if (scan.errors.length > 0) {
			fail(`页面声明解析失败：\n${scan.errors.map(e => `  - ${e}`).join('\n')}`)
		}

		const drafts = buildDrafts(scan, options)
		buildNames(drafts, { pagesDirName: options.pages.pagesDir.rel.split('/').pop() ?? 'pages', nameStrategy: options.router.nameStrategy }, fail)

		const changedPagesJson = writeFileIfChanged(
			options.pagesJsonPath.abs,
			renderPagesJson(drafts, options, msg => warn(msg))
		)

		const routesContent = renderRoutesGen(drafts, options, readIfExists(options.router.outputPath.abs), msg => warn(msg), ' * 页面级声明请使用页面内的 defineUniPage 宏或 <route-config> 块。')
		const changedRoutes = writeFileIfChanged(options.router.outputPath.abs, routesContent)

		let changedDts = false
		if (options.pages.dts !== null) {
			changedDts = writeFileIfChanged(options.pages.dts.abs, renderMacroDts()) || changedDts
		}

		if (options.router.dts !== null) {
			changedDts = writeFileIfChanged(options.router.dts.abs, renderRouteNameDts(drafts, options)) || changedDts
		}

		const outputs = [changedPagesJson ? options.pagesJsonPath.rel : null, changedRoutes ? options.router.outputPath.rel : null, changedDts ? 'dts' : null].filter(v => v !== null)

		log(`regenerated(${reason}): ${scan.pages.length} pages（tab ${drafts.filter(d => d.isTab).length}/分包 ${scan.pages.filter(p => p.pkgRoot !== null).length}）→ ${outputs.length > 0 ? outputs.join(' / ') : '无变化'}`)
	}

	/** 路由生成插件监听文件变更时触发 */
	const scheduleRegenerate = createSerialDebounced(regenerate, e => console.error(LOG_PREFIX, e))

	/** 生成产物自身的变更不触发重跑（防自激循环） */
	const isGeneratedOutput = (file: string): boolean => file === options.pagesJsonPath.abs || file === options.router.outputPath.abs || file === options.router.dts?.abs || file === options.pages.dts?.abs

	/** 页面目录 / 分包目录内的文件变更才关心 */
	const isWatchedPath = (file: string): boolean => {
		const watched = [options.pages.pagesDir.abs, ...options.pages.subPackages.map(s => s.dir.abs)]
		return watched.some(dir => file.startsWith(dir + '\\') || file.startsWith(dir + '/'))
	}

	return {
		name: PLUGIN_NAME,

		/** 路由生成插件监听构建开始时触发 */
		buildStart() {
			regenerate('buildStart')
		},

		/** 路由生成插件监听文件变更时触发 */
		watchChange(id, change) {
			if (!options.watch || isGeneratedOutput(id) || !isWatchedPath(id)) return

			scheduleRegenerate(`watch ${change.event}: ${id}`)
		},

		/** 路由生成插件监听模块请求时触发 */
		resolveId(id) {
			// <route-config> 自定义块的模块请求 → 虚拟空模块（防块内容被当作 JS 解析报错）
			if (isRouteConfigBlockRequest(id)) return BLOCK_VIRTUAL_PREFIX + id

			return null
		},

		/** 路由生成插件监听模块加载时触发 */
		load(id) {
			if (id.startsWith(BLOCK_VIRTUAL_PREFIX)) return ''

			return null
		},

		/** 路由生成插件监听模块转换时触发 */
		transform(code, id) {
			// 仅处理原始 uvue/vue 源（跳过带 query 的子请求）；等行数注释占位，行号不变
			const cleanId = id.split('?')[0]
			if (!/\.(uvue|vue)$/.test(cleanId)) return null

			if (!/\bdefineUniPage\s*\(/.test(code)) return null

			const result = stripDefineUniPage(code)
			if (result.error !== null) fail(result.error)

			return result.code !== code ? { code: result.code, map: null } : null
		},

		/** 路由生成插件监听 Vite 配置解析时触发 */
		vite: {
			enforce: 'pre' as const,

			/** 路由生成插件监听 Vite 配置解析时触发 */
			configResolved(config) {
				// 进程 cwd 可能不是项目根（如 HBuilderX），以 vite 解析出的项目根为准
				if (config.root !== options.root) options = resolveOptions(raw, config.root)
			},

			/** 路由生成插件监听 Vite 服务器配置时触发 */
			configureServer(server) {
				if (!options.watch) return

				const onEvent = (file: string): void => {
					if (isGeneratedOutput(file) || !isWatchedPath(file)) return
					scheduleRegenerate(`hmr: ${file}`)
				}

				/** 路由生成插件监听 Vite 服务器文件变更时触发 */
				server.watcher.on('add', onEvent)
				server.watcher.on('change', onEvent)
				server.watcher.on('unlink', onEvent)
			}
		}
	}
})

/** vite 适配器：vite.config.ts 中 routeGen(options) 直调 */
export const routeGen = routeGenUnplugin.vite
export default routeGenUnplugin
export type { RouteGenOptions }
