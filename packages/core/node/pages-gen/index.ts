/**
 * pagesGen 插件：页面文件宏/块 → 仅 pages.json
 *
 * 与 routeGen 共用扫描与 pages.json 渲染层，但不产出路由数组文件；
 * 适合已有手写/独立维护路由表的工程，只想让 pages.json 随页面目录自动同步。
 * 同样负责 defineUniPage 宏剥离与 <route-config> 块拦截（独立注册时保证 uni 编译器看到干净源码）。
 */
import { createUnplugin } from 'unplugin'
import { BLOCK_VIRTUAL_PREFIX, PAGES_GEN_LOG_PREFIX, PAGES_GEN_PLUGIN_NAME, isRouteConfigBlockRequest } from '../shared/common/constants'
import { stripDefineUniPage } from '../shared/parsing/extract'
import { buildDrafts, renderMacroDts, renderPagesJson } from '../shared/rendering/generate'
import { PagesGenOptions, ResolvedPagesGenOptions, resolvePagesGenOptions } from '../shared/common/options'
import { scanPages } from '../shared/scanning/scan'
import { createSerialDebounced, writeFileIfChanged } from '../shared/common/utils'

/** 页面生成插件 */
export const pagesGenUnplugin = createUnplugin<PagesGenOptions | undefined>((raw = {}) => {
	// 默认基于 cwd 解析；vite 下由 configResolved 以真实项目根重新解析（HBuilderX 进程 cwd 非项目根）
	let options: ResolvedPagesGenOptions = resolvePagesGenOptions(raw)

	/** 页面生成插件日志 */
	const log = (...args: unknown[]): void => {
		if (options.verbose) console.log(PAGES_GEN_LOG_PREFIX, ...args)
	}

	/** 页面生成插件警告日志 */
	const warn = (...args: unknown[]): void => {
		console.warn(PAGES_GEN_LOG_PREFIX, ...args)
	}

	/** 页面生成插件失败日志 */
	const fail = (msg: string): void => {
		if (options.errorStrategy === 'warn') {
			warn(msg)
			return
		}

		throw new Error(`${PAGES_GEN_LOG_PREFIX} ${msg}`)
	}

	/** 单阶段流水线：扫描页面 → 写 pages.json（保留手写字段）+ 可选宏 dts */
	function regenerate(reason: string): void {
		if (!options.enabled) return

		const scan = scanPages(options)
		scan.warnings.forEach(w => warn(w))
		if (scan.errors.length > 0) {
			fail(`页面声明解析失败：\n${scan.errors.map(e => `  - ${e}`).join('\n')}`)
		}

		const drafts = buildDrafts(scan, options)
		const changedPagesJson = writeFileIfChanged(
			options.pagesJsonPath.abs,
			renderPagesJson(drafts, options, msg => warn(msg))
		)

		let changedDts = false
		if (options.pages.dts !== null) {
			changedDts = writeFileIfChanged(options.pages.dts.abs, renderMacroDts())
		}

		const outputs = [changedPagesJson ? options.pagesJsonPath.rel : null, changedDts ? 'dts' : null].filter(v => v !== null)

		/** 页面生成插件日志 */
		log(`regenerated(${reason}): ${scan.pages.length} pages（tab ${drafts.filter(d => d.isTab).length}/分包 ${scan.pages.filter(p => p.pkgRoot !== null).length}）→ ${outputs.length > 0 ? outputs.join(' / ') : '无变化'}`)
	}

	/** 页面生成插件错误日志 */
	const scheduleRegenerate = createSerialDebounced(regenerate, e => console.error(PAGES_GEN_LOG_PREFIX, e))

	/** 生成产物自身的变更不触发重跑（防自激循环） */
	const isGeneratedOutput = (file: string): boolean => file === options.pagesJsonPath.abs || file === options.pages.dts?.abs

	/** 页面目录 / 分包目录内的文件变更才关心 */
	const isWatchedPath = (file: string): boolean => {
		const watched = [options.pages.pagesDir.abs, ...options.pages.subPackages.map(s => s.dir.abs)]

		return watched.some(dir => file.startsWith(dir + '\\') || file.startsWith(dir + '/'))
	}

	return {
		name: PAGES_GEN_PLUGIN_NAME,

		/** 页面生成插件构建开始时触发 */
		buildStart() {
			regenerate('buildStart')
		},

		/** 页面生成插件监听 Vite 服务器文件变更时触发 */
		watchChange(id, change) {
			if (!options.watch || isGeneratedOutput(id) || !isWatchedPath(id)) return

			scheduleRegenerate(`watch ${change.event}: ${id}`)
		},

		/** 页面生成插件解析模块 ID时触发 */
		resolveId(id) {
			// <route-config> 自定义块的模块请求 → 虚拟空模块（防块内容被当作 JS 解析报错）
			if (isRouteConfigBlockRequest(id)) return BLOCK_VIRTUAL_PREFIX + id

			return null
		},

		/** 页面生成插件加载模块时触发 */
		load(id) {
			if (id.startsWith(BLOCK_VIRTUAL_PREFIX)) return ''

			return null
		},

		/** 页面生成插件转换模块时触发 */
		transform(code, id) {
			// 仅处理原始 uvue/vue 源（跳过带 query 的子请求）；等行数注释占位，行号不变
			const cleanId = id.split('?')[0]
			if (!/\.(uvue|vue)$/.test(cleanId)) return null

			if (!/\bdefineUniPage\s*\(/.test(code)) return null

			const result = stripDefineUniPage(code)
			if (result.error !== null) fail(result.error)

			return result.code !== code ? { code: result.code, map: null } : null
		},

		/** 页面生成插件 Vite 配置 */
		vite: {
			enforce: 'pre' as const,

			/** 页面生成插件 Vite 配置 */
			configResolved(config) {
				// 进程 cwd 可能不是项目根（如 HBuilderX），以 vite 解析出的项目根为准
				if (config.root !== options.root) options = resolvePagesGenOptions(raw, config.root)
			},

			/** 页面生成插件 Vite 配置 */
			configureServer(server) {
				if (!options.watch) return

				const onEvent = (file: string): void => {
					if (isGeneratedOutput(file) || !isWatchedPath(file)) return

					scheduleRegenerate(`hmr: ${file}`)
				}

				/** 页面生成插件 Vite 配置 */
				server.watcher.on('add', onEvent)
				server.watcher.on('change', onEvent)
				server.watcher.on('unlink', onEvent)
			}
		}
	}
})

/** vite 适配器：vite.config.ts 中 pagesGen(options) 直调 */
export const pagesGen = pagesGenUnplugin.vite
export type { PagesGenOptions }
