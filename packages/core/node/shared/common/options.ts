/**
 * 三插件选项定义与解析（原始选项 → 带默认值的解析结果）。
 *
 * @remarks
 * 相对路径统一基于项目根 `root` 解析为 {@link FileRef}；
 * 默认 root 取 `process.cwd()`，vite 环境由 configResolved 用 `config.root` 修正
 * （HBuilderX 下 cwd ≠ 项目根）。
 *
 * @packageDocumentation
 */
import path from 'node:path'

/** tabBar 外观配置（pages.json tabBar 中除 list 外的字段） */
export type TabBarChrome = {
	/** tabBar 文字颜色 */
	color?: string
	/** tabBar 选中文字颜色 */
	selectedColor?: string
	/** tabBar 背景颜色 */
	backgroundColor?: string
	/** tabBar 边框样式 */
	borderStyle?: string
}

/** 分包配置：root 为 pages.json 中的页面路径前缀，dir 为源码目录（相对项目根） */
export type SubPackageOptions = {
	/** 分包 root 路径（分包 pages.json 中的页面路径前缀），如 'pages-sub' */
	root: string
	/** 分包页面源码目录（相对项目根），如 'pages-sub' */
	dir: string
}

/** 阶段一（页面生成）选项：routeGen.pages 与 pagesGen.pages 共用 */
export type RouteGenPagesOptions = {
	/** 主包页面源码目录（相对项目根），默认 'pages' */
	pagesDir?: string
	/** 分包配置列表 */
	subPackages?: SubPackageOptions[]
	/** 启动页（移到 pages 数组首位），如 'pages/index/index' */
	entryPage?: string
	/** 宏/块未声明 title 时的兜底标题 */
	titleFallback?: string
	/** tabBar 外观配置 */
	tabBar?: TabBarChrome
	/** 参与扫描的页面扩展名，默认 ['.uvue'] */
	includeExtensions?: string[]
	/** 排除规则（字符串为路径包含匹配，或正则） */
	excludePatterns?: Array<string | RegExp>
	/** defineUniPage 宏全局类型声明输出路径（便于 IDE 提示），false 关闭 */
	dts?: string | false
}

/** 阶段二（路由生成）选项：routeGen.router 与 routesGen.router 共用基底 */
export type RouteGenRouterOptions = {
	/** 生成路由表文件路径（相对项目根），默认 'routes.gen.uts' */
	outputPath?: string
	/** 路由数组导出变量名，默认 'routes' */
	exportName?: string
	/** 命名策略：末段 camelCase（默认）或全路径 camelCase */
	nameStrategy?: 'camelCase' | 'fullPath'
	/**
	 * RouteConfig 类型导入来源，默认 '@meng-xi/unix-router'。
	 * uni_modules 用法可指向 utssdk 入口（如 '@/uni_modules/ux-router/utssdk/index.uts'）
	 */
	importFrom?: string
	/** RouteNameMap 字面量类型声明文件（WEB 端编辑器提示），false 关闭 */
	dts?: string | boolean | false
	/** 重新生成时保留用户对路由文件的修改，默认 true */
	preserveRouteChanges?: boolean
}

/** routesGen 专属：扩展声明选项 */
export type RoutesGenRouterOptions = RouteGenRouterOptions & {
	/** 扩展声明文件路径（相对项目根），默认 'routes.ext.uts'；设为 false 关闭扩展合并 */
	extensions?: string | false
}

/** routeGen 插件选项（页面 → pages.json + 路由数组，全量流水线） */
export type RouteGenOptions = {
	/** pages.json 路径（相对项目根），默认 'pages.json' */
	pagesJsonPath?: string
	/** 监听页面目录变更自动重跑流水线，默认 true */
	watch?: boolean
	/** 是否启用生成（false 时插件完全旁路） */
	enabled?: boolean
	/** 输出详细日志（扫描 / 写盘明细） */
	verbose?: boolean
	/** 解析冲突策略：strict 抛错终止构建；warn 告警跳过 */
	errorStrategy?: 'strict' | 'warn'
	/** 页面生成选项 */
	pages?: RouteGenPagesOptions
	/** 路由生成选项 */
	router?: RouteGenRouterOptions
}

/** pagesGen 插件选项（页面 → 仅 pages.json） */
export type PagesGenOptions = {
	/** pages.json 路径（相对项目根），默认 'pages.json' */
	pagesJsonPath?: string
	/** 监听页面目录变更自动重跑，默认 true */
	watch?: boolean
	/** 是否启用生成（false 时插件完全旁路） */
	enabled?: boolean
	/** 输出详细日志（扫描 / 写盘明细） */
	verbose?: boolean
	/** 解析冲突策略：strict 抛错终止构建；warn 告警跳过 */
	errorStrategy?: 'strict' | 'warn'
	/** 页面生成选项 */
	pages?: RouteGenPagesOptions
}

/** routesGen 插件选项（pages.json → 仅路由数组） */
export type RoutesGenOptions = {
	/** pages.json 路径（相对项目根），默认 'pages.json' */
	pagesJsonPath?: string
	/** 监听 pages.json / 扩展声明文件变更自动重跑，默认 true */
	watch?: boolean
	/** 是否启用生成（false 时插件完全旁路） */
	enabled?: boolean
	/** 输出详细日志（解析 / 写盘明细） */
	verbose?: boolean
	/** 解析冲突策略：strict 抛错终止构建；warn 告警跳过 */
	errorStrategy?: 'strict' | 'warn'
	/** 路由生成选项 */
	router?: RoutesGenRouterOptions
}

/** 相对路径解析结果：rel 为用户声明值，abs 为基于项目根的绝对路径 */
export type FileRef = {
	/** 相对路径 */
	rel: string
	/** 绝对路径 */
	abs: string
}

/** 公共解析段（三个插件共用） */
export type ResolvedCommonSection = {
	/** 项目根目录 */
	root: string
	/** pages.json 路径（绝对路径） */
	pagesJsonPath: FileRef
	/** 监听页面目录变更自动重跑流水线，默认 true */
	watch: boolean
	/** 是否启用生成（false 时插件完全旁路） */
	enabled: boolean
	/** 输出详细日志（扫描 / 写盘明细） */
	verbose: boolean
	/** 解析冲突策略：strict 抛错终止构建；warn 告警跳过 */
	errorStrategy: 'strict' | 'warn'
}

/** 页面生成段（routeGen / pagesGen） */
export type ResolvedPagesSection = {
	/** 主包页面源码目录（绝对路径） */
	pagesDir: FileRef
	/** 分包配置列表 */
	subPackages: Array<{
		/** 分包根目录（相对项目根） */
		root: string
		/** 分包页面源码目录（绝对路径） */
		dir: FileRef
	}>
	/** 启动页（移到 pages 数组首位），如 'pages/index/index' */
	entryPage: string | null
	/** 宏/块未声明 title 时的兜底标题 */
	titleFallback: string | null
	/** tabBar 外观配置 */
	tabBar: TabBarChrome
	/** 参与扫描的页面扩展名，默认 ['.uvue'] */
	includeExtensions: string[]
	/** 排除的页面路径模式，默认 [] */
	excludePatterns: Array<string | RegExp>
	/** RouteNameMap 字面量类型声明文件（WEB 端编辑器提示），false 关闭 */
	dts: FileRef | null
}

/** 路由生成段（routeGen / routesGen）；extensionsFile 仅 routesGen 消费，其余插件恒为 null */
export type ResolvedRouterSection = {
	/** 路由数组输出路径（绝对路径） */
	outputPath: FileRef
	/** 路由数组导出名称 */
	exportName: string
	/** 路由数组导出路径策略 */
	nameStrategy: 'camelCase' | 'fullPath'
	/** 路由数组导出路径策略 */
	importFrom: string
	/** RouteNameMap 字面量类型声明文件（WEB 端编辑器提示），false 关闭 */
	dts: FileRef | null
	/** 是否保留路由变化（false 时仅保留初始路由） */
	preserveRouteChanges: boolean
	/** 扩展声明文件（绝对路径） */
	extensionsFile: FileRef | null
}

/** routeGen 全量解析结果 */
export type ResolvedOptions = ResolvedCommonSection & {
	/** 页面生成段 */
	pages: ResolvedPagesSection
	/** 路由生成段 */
	router: ResolvedRouterSection
}

/** pagesGen 解析结果 */
export type ResolvedPagesGenOptions = ResolvedCommonSection & {
	/** 页面生成段 */
	pages: ResolvedPagesSection
}

/** routesGen 解析结果 */
export type ResolvedRoutesGenOptions = ResolvedCommonSection & {
	/** 路由生成段 */
	router: ResolvedRouterSection
}

/**
 * 解析相对路径为绝对路径
 * @param root 项目根目录
 * @param rel 相对路径
 * @returns 绝对路径
 */
const toFileRef = (root: string, rel: string): FileRef => ({ rel, abs: path.resolve(root, rel) })

/** 解析公共段默认值（相对路径基于项目根；默认取 cwd，vite 环境由 configResolved 修正） */
function resolveCommon(raw: Pick<RouteGenOptions, 'pagesJsonPath' | 'watch' | 'enabled' | 'verbose' | 'errorStrategy'>, root: string): ResolvedCommonSection {
	return {
		root,
		pagesJsonPath: toFileRef(root, raw.pagesJsonPath ?? 'pages.json'),
		watch: raw.watch ?? true,
		enabled: raw.enabled ?? true,
		verbose: raw.verbose ?? false,
		errorStrategy: raw.errorStrategy ?? 'strict'
	}
}

/** 解析页面生成段默认值 */
function resolvePagesSection(pages: RouteGenPagesOptions | undefined, root: string): ResolvedPagesSection {
	const p = pages ?? {}
	const pagesDts = typeof p.dts === 'string' ? toFileRef(root, p.dts) : null

	return {
		pagesDir: toFileRef(root, p.pagesDir ?? 'pages'),
		subPackages: (p.subPackages ?? []).map(s => ({ root: s.root, dir: toFileRef(root, s.dir) })),
		entryPage: p.entryPage ?? null,
		titleFallback: p.titleFallback ?? null,
		tabBar: p.tabBar ?? {},
		includeExtensions: p.includeExtensions ?? ['.uvue'],
		excludePatterns: p.excludePatterns ?? [],
		dts: pagesDts
	}
}

/** 解析路由生成段默认值 */
function resolveRouterSection(router: RouteGenRouterOptions | undefined, root: string): ResolvedRouterSection {
	const r = router ?? {}
	const routerDts = typeof r.dts === 'string' ? toFileRef(root, r.dts) : r.dts === true ? toFileRef(root, 'route-name.gen.d.ts') : null

	return {
		outputPath: toFileRef(root, r.outputPath ?? 'routes.gen.uts'),
		exportName: r.exportName ?? 'routes',
		nameStrategy: r.nameStrategy ?? 'camelCase',
		importFrom: r.importFrom ?? '@meng-xi/unix-router',
		dts: routerDts,
		preserveRouteChanges: r.preserveRouteChanges ?? true,
		extensionsFile: null
	}
}

/**
 * 解析 routeGen 选项：补全默认值并将相对路径解析为 {@link FileRef}。
 *
 * @param raw - 用户原始选项
 * @param root - 项目根（相对路径解析基准），默认 `process.cwd()`，vite 环境应传 `config.root`
 */
export function resolveOptions(raw: RouteGenOptions, root: string = process.cwd()): ResolvedOptions {
	return {
		...resolveCommon(raw, root),
		pages: resolvePagesSection(raw.pages, root),
		router: resolveRouterSection(raw.router, root)
	}
}

/**
 * 解析 pagesGen 选项：仅公共段 + 页面生成段，不解析路由段。
 *
 * @param raw - 用户原始选项
 * @param root - 项目根（相对路径解析基准），默认 `process.cwd()`，vite 环境应传 `config.root`
 */
export function resolvePagesGenOptions(raw: PagesGenOptions, root: string = process.cwd()): ResolvedPagesGenOptions {
	return {
		...resolveCommon(raw, root),
		pages: resolvePagesSection(raw.pages, root)
	}
}

/**
 * 解析 routesGen 选项：仅公共段 + 路由段，并额外解析 `extensions` 扩展声明文件。
 *
 * @param raw - 用户原始选项
 * @param root - 项目根（相对路径解析基准），默认 `process.cwd()`，vite 环境应传 `config.root`
 */
export function resolveRoutesGenOptions(raw: RoutesGenOptions, root: string = process.cwd()): ResolvedRoutesGenOptions {
	const router = raw.router ?? {}
	return {
		...resolveCommon(raw, root),
		router: {
			...resolveRouterSection(router, root),
			extensionsFile: router.extensions === false ? null : toFileRef(root, router.extensions ?? 'routes.ext.uts')
		}
	}
}
