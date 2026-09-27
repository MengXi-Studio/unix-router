/**
 * routesGen 插件：pages.json → 仅路由数组（routes.gen.uts + 可选 dts）
 *
 * 以 pages.json 为唯一数据源推导基础路由（path / title=navigationBarTitleText / isTab=tabBar 成员），
 * 再与既有生成文件（preserveRouteChanges：保留自定义字段并对齐既有 name）、
 * 扩展声明文件 routes.ext.uts（meta 扩展 / beforeEnter / 显式 name）合并后渲染。
 * 不扫描页面文件，与 routeGen / pagesGen 互不影响，可独立注册。
 */
import { createUnplugin } from 'unplugin'
import { ROUTES_GEN_LOG_PREFIX, ROUTES_GEN_PLUGIN_NAME } from '../shared/common/constants'
import { buildNames, parseExistingRoutes, renderRouteNameDts, renderRoutesGen } from '../shared/rendering/generate'
import { parseJsonc } from '../shared/parsing/literal'
import { RoutesGenOptions, ResolvedRoutesGenOptions, resolveRoutesGenOptions } from '../shared/common/options'
import { Draft } from '../shared/common/types'
import { createSerialDebounced, readIfExists, writeFileIfChanged } from '../shared/common/utils'
import { mergeExtIntoDrafts, parseExtDeclarations } from './ext'

/** 从 pages.json 数据推导路由草稿（title=style.navigationBarTitleText，isTab=tabBar.list 成员） */
function buildDraftsFromPagesJson(data: unknown, warn: (msg: string) => void): { drafts: Draft[]; entryPath: string | null } {
	const root = typeof data === 'object' && data !== null ? (data as Record<string, unknown>) : {}
	const emptyDraft = (path: string, rel: string, pkgRoot: string | null): Draft => ({
		path,
		rel,
		pkgRoot,
		title: null,
		isTab: false,
		order: null,
		iconPath: null,
		selectedIconPath: null,
		tabText: null,
		name: '',
		metaExtra: [],
		beforeEnter: null,
		redirect: null
	})

	// tabBar 成员集合（isTab 推导源）
	const tabPaths = new Set<string>()
	const tabBar = typeof root.tabBar === 'object' && root.tabBar !== null ? (root.tabBar as Record<string, unknown>) : null

	if (tabBar !== null && Array.isArray(tabBar.list)) {
		for (const item of tabBar.list) {
			if (typeof item === 'object' && item !== null && typeof (item as Record<string, unknown>).pagePath === 'string') {
				tabPaths.add((item as Record<string, unknown>).pagePath as string)
			}
		}
	}

	const titleOf = (page: Record<string, unknown>): string | null => {
		const style = typeof page.style === 'object' && page.style !== null ? (page.style as Record<string, unknown>) : null
		const title = style !== null ? style.navigationBarTitleText : null

		return typeof title === 'string' ? title : null
	}

	const drafts: Draft[] = []
	let entryPath: string | null = null

	if (Array.isArray(root.pages)) {
		for (const page of root.pages) {
			if (typeof page !== 'object' || page === null) continue

			const p = page as Record<string, unknown>
			if (typeof p.path !== 'string' || p.path === '') {
				warn('pages.json pages 数组中存在缺少 path 的条目，已忽略')
				continue
			}

			// uni 约定 pages[0] 为启动页（与 routeGen 的 entryPage 语义对齐）
			if (entryPath === null) entryPath = p.path
			const draft = emptyDraft(p.path, p.path, null)
			draft.title = titleOf(p)
			draft.isTab = tabPaths.has(p.path)
			drafts.push(draft)
		}
	} else {
		warn('pages.json 缺少 pages 数组，未推导出任何路由')
	}

	// 分包（subPackages / subpackages 均兼容）
	const subs = Array.isArray(root.subPackages) ? root.subPackages : Array.isArray(root.subpackages) ? root.subpackages : []
	for (const sub of subs) {
		if (typeof sub !== 'object' || sub === null) continue

		const s = sub as Record<string, unknown>
		if (typeof s.root !== 'string' || s.root === '' || !Array.isArray(s.pages)) continue

		const pkgRoot = s.root.replace(/\/$/, '')
		for (const page of s.pages) {
			if (typeof page !== 'object' || page === null) continue

			const p = page as Record<string, unknown>
			if (typeof p.path !== 'string' || p.path === '') continue

			const draft = emptyDraft(pkgRoot + '/' + p.path, p.path, pkgRoot)
			draft.title = titleOf(p)
			draft.isTab = tabPaths.has(draft.path)
			drafts.push(draft)
		}
	}

	return { drafts, entryPath }
}

/** 路由生成插件 */
export const routesGenUnplugin = createUnplugin<RoutesGenOptions | undefined>((raw = {}) => {
	// 默认基于 cwd 解析；vite 下由 configResolved 以真实项目根重新解析（HBuilderX 进程 cwd 非项目根）
	let options: ResolvedRoutesGenOptions = resolveRoutesGenOptions(raw)

	const log = (...args: unknown[]): void => {
		if (options.verbose) console.log(ROUTES_GEN_LOG_PREFIX, ...args)
	}

	/** 路由生成插件告警 */
	const warn = (...args: unknown[]): void => {
		console.warn(ROUTES_GEN_LOG_PREFIX, ...args)
	}

	/** 路由生成插件失败 */
	const fail = (msg: string): void => {
		if (options.errorStrategy === 'warn') {
			warn(msg)
			return
		}

		throw new Error(`${ROUTES_GEN_LOG_PREFIX} ${msg}`)
	}

	/** 单阶段流水线：pages.json → 基础路由 + 既有 name 对齐 + 扩展声明合并 → routes.gen.uts + 可选 dts */
	function regenerate(reason: string): void {
		if (!options.enabled) return

		/** 读取 pages.json 内容 */
		const pagesJsonText = readIfExists(options.pagesJsonPath.abs)
		if (pagesJsonText === null) {
			fail(`未找到 ${options.pagesJsonPath.rel}（routesGen 以 pages.json 为数据源，页面生成请改用 routeGen / pagesGen）`)
			return
		}

		/** 解析 pages.json 内容 */
		let pagesJsonData: unknown
		try {
			pagesJsonData = parseJsonc(pagesJsonText)
		} catch (e) {
			fail(`pages.json 解析失败：${String(e)}`)
			return
		}

		/** 从 pages.json 内容推导路由 */
		const { drafts, entryPath } = buildDraftsFromPagesJson(pagesJsonData, msg => warn(msg))

		/** 与 routeGen 扫描序对齐：path 排序 + 启动页移至主包首位（保证两插件产物一致） */
		drafts.sort((a, b) => a.path.localeCompare(b.path))
		if (entryPath !== null) {
			const idx = drafts.findIndex(d => d.path === entryPath)

			if (idx >= 0) {
				const [d] = drafts.splice(idx, 1)
				const firstMain = drafts.findIndex(x => x.pkgRoot === null)
				drafts.splice(firstMain < 0 ? 0 : firstMain, 0, d)
			}
		}

		/** 既有生成文件中的 name 作为基准（与 routeGen 输出对齐，防重复生成时改名） */
		const existing = readIfExists(options.router.outputPath.abs)
		if (existing !== null && options.router.preserveRouteChanges) {
			for (const entry of parseExistingRoutes(existing, options.router.exportName, msg => warn(msg))) {
				const draft = drafts.find(d => d.path === entry.path)
				if (draft === undefined || draft.name !== '') continue

				/** 合并 name 字段 */
				const nameEntry = entry.entries.find(e => e.key === 'name')
				if (nameEntry !== undefined && nameEntry.value.kind === 'string') draft.name = nameEntry.value.value
			}
		}

		/** name 规范化：主包页面路径首段即 pagesDir 名（pages.json 主包路径相对 pagesDir 父目录） */
		const firstMain = drafts.find(d => d.pkgRoot === null)
		const pagesDirName = firstMain !== undefined ? (firstMain.path.split('/')[0] ?? 'pages') : 'pages'
		buildNames(drafts, { pagesDirName, nameStrategy: options.router.nameStrategy }, fail)

		/** 扩展声明合并（文件缺省视为未配置，静默跳过） */
		if (options.router.extensionsFile !== null) {
			const extText = readIfExists(options.router.extensionsFile.abs)
			if (extText !== null) {
				const ext = parseExtDeclarations(extText)
				ext.warnings.forEach(w => warn(w))

				if (ext.error !== null) {
					fail(`扩展声明解析失败（${options.router.extensionsFile.rel}）：${ext.error}`)
					return
				}

				mergeExtIntoDrafts(drafts, ext.entries, msg => warn(msg))
			} else {
				log(`扩展声明文件 ${options.router.extensionsFile.rel} 不存在，跳过扩展合并`)
			}
		}

		/** 渲染路由生成内容 */
		const routesContent = renderRoutesGen(drafts, options, existing, msg => warn(msg), ' * 扩展字段（name / meta / beforeEnter）请在 routes.ext.uts 扩展声明文件中配置。')
		const changedRoutes = writeFileIfChanged(options.router.outputPath.abs, routesContent)

		let changedDts = false
		if (options.router.dts !== null) {
			changedDts = writeFileIfChanged(options.router.dts.abs, renderRouteNameDts(drafts, options))
		}

		const outputs = [changedRoutes ? options.router.outputPath.rel : null, changedDts ? 'dts' : null].filter(v => v !== null)

		/** 记录生成结果 */
		log(`regenerated(${reason}): ${drafts.length} routes（tab ${drafts.filter(d => d.isTab).length}/分包 ${drafts.filter(d => d.pkgRoot !== null).length}）→ ${outputs.length > 0 ? outputs.join(' / ') : '无变化'}`)
	}

	/** 路由生成插件异步任务调度器 */
	const scheduleRegenerate = createSerialDebounced(regenerate, e => console.error(ROUTES_GEN_LOG_PREFIX, e))

	/** 生成产物自身的变更不触发重跑（防自激循环） */
	const isGeneratedOutput = (file: string): boolean => file === options.router.outputPath.abs || file === options.router.dts?.abs

	/** pages.json / 扩展声明文件的变更才关心 */
	const isWatchedPath = (file: string): boolean => file === options.pagesJsonPath.abs || (options.router.extensionsFile !== null && file === options.router.extensionsFile.abs)

	return {
		name: ROUTES_GEN_PLUGIN_NAME,

		/** 路由生成插件构建开始时触发 */
		buildStart() {
			regenerate('buildStart')
		},

		/** 路由生成插件监听文件变更时触发 */
		watchChange(id, change) {
			if (!options.watch || isGeneratedOutput(id) || !isWatchedPath(id)) return
			scheduleRegenerate(`watch ${change.event}: ${id}`)
		},

		/** 路由生成插件监听 HMR 事件时触发 */
		vite: {
			/** 路由生成插件监听 HMR 事件时触发 */
			configResolved(config) {
				// 进程 cwd 可能不是项目根（如 HBuilderX），以 vite 解析出的项目根为准
				if (config.root !== options.root) options = resolveRoutesGenOptions(raw, config.root)
			},

			/** 路由生成插件监听 HMR 事件时触发 */
			configureServer(server) {
				if (!options.watch) return

				const onEvent = (file: string): void => {
					if (isGeneratedOutput(file) || !isWatchedPath(file)) return
					scheduleRegenerate(`hmr: ${file}`)
				}

				server.watcher.on('add', onEvent)
				server.watcher.on('change', onEvent)
				server.watcher.on('unlink', onEvent)
			}
		}
	}
})

/** vite 适配器：vite.config.ts 中 routesGen(options) 直调 */
export const routesGen = routesGenUnplugin.vite
export type { RoutesGenOptions, RoutesGenRouterOptions } from '../shared/common/options'
