import path from 'node:path'

/** tabBar 外观配置（pages.json tabBar 中除 list 外的字段） */
export type TabBarChrome = {
	color?: string
	selectedColor?: string
	backgroundColor?: string
	borderStyle?: string
}

/** 分包配置：root 为 pages.json 中的页面路径前缀，dir 为源码目录（相对项目根） */
export type SubPackageOptions = {
	root: string
	dir: string
}

/** 阶段一（页面生成）选项 */
export type RouteGenPagesOptions = {
	/** 主包页面源码目录（相对项目根），默认 'pages' */
	pagesDir?: string
	subPackages?: SubPackageOptions[]
	/** 启动页（移到 pages 数组首位），如 'pages/index/index' */
	entryPage?: string
	/** 宏/块未声明 title 时的兜底标题 */
	titleFallback?: string
	tabBar?: TabBarChrome
	/** 参与扫描的页面扩展名，默认 ['.uvue'] */
	includeExtensions?: string[]
	/** 排除规则（字符串为路径包含匹配，或正则） */
	excludePatterns?: Array<string | RegExp>
	/** defineUniPage 宏全局类型声明输出路径（便于 IDE 提示），false 关闭 */
	dts?: string | false
}

/** 阶段二（路由生成）选项 */
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

/** routeGen 插件选项 */
export type RouteGenOptions = {
	/** pages.json 路径（相对项目根），默认 'pages.json' */
	pagesJsonPath?: string
	/** 监听页面目录变更自动重跑流水线，默认 true */
	watch?: boolean
	enabled?: boolean
	verbose?: boolean
	/** 解析冲突策略：strict 抛错终止构建；warn 告警跳过 */
	errorStrategy?: 'strict' | 'warn'
	pages?: RouteGenPagesOptions
	router?: RouteGenRouterOptions
}

export type FileRef = { rel: string; abs: string }

export type ResolvedOptions = {
	root: string
	pagesJsonPath: FileRef
	watch: boolean
	enabled: boolean
	verbose: boolean
	errorStrategy: 'strict' | 'warn'
	pages: {
		pagesDir: { rel: string; abs: string }
		subPackages: Array<{ root: string; dir: FileRef }>
		entryPage: string | null
		titleFallback: string | null
		tabBar: TabBarChrome
		includeExtensions: string[]
		excludePatterns: Array<string | RegExp>
		dts: FileRef | null
	}
	router: {
		outputPath: FileRef
		exportName: string
		nameStrategy: 'camelCase' | 'fullPath'
		importFrom: string
		dts: FileRef | null
		preserveRouteChanges: boolean
	}
}

const toFileRef = (root: string, rel: string): FileRef => ({ rel, abs: path.resolve(root, rel) })

/** 解析并补全插件选项默认值（相对路径基于项目根；默认取 cwd，vite 环境由 configResolved 修正） */
export function resolveOptions(raw: RouteGenOptions, root: string = process.cwd()): ResolvedOptions {
	const pages = raw.pages ?? {}
	const router = raw.router ?? {}
	const pagesDts = typeof pages.dts === 'string' ? toFileRef(root, pages.dts) : null
	const routerDts =
		typeof router.dts === 'string' ? toFileRef(root, router.dts) : router.dts === true ? toFileRef(root, 'route-name.gen.d.ts') : null
	return {
		root,
		pagesJsonPath: toFileRef(root, raw.pagesJsonPath ?? 'pages.json'),
		watch: raw.watch ?? true,
		enabled: raw.enabled ?? true,
		verbose: raw.verbose ?? false,
		errorStrategy: raw.errorStrategy ?? 'strict',
		pages: {
			pagesDir: toFileRef(root, pages.pagesDir ?? 'pages'),
			subPackages: (pages.subPackages ?? []).map((s) => ({ root: s.root, dir: toFileRef(root, s.dir) })),
			entryPage: pages.entryPage ?? null,
			titleFallback: pages.titleFallback ?? null,
			tabBar: pages.tabBar ?? {},
			includeExtensions: pages.includeExtensions ?? ['.uvue'],
			excludePatterns: pages.excludePatterns ?? [],
			dts: pagesDts
		},
		router: {
			outputPath: toFileRef(root, router.outputPath ?? 'routes.gen.uts'),
			exportName: router.exportName ?? 'routes',
			nameStrategy: router.nameStrategy ?? 'camelCase',
			importFrom: router.importFrom ?? '@meng-xi/unix-router',
			dts: routerDts,
			preserveRouteChanges: router.preserveRouteChanges ?? true
		}
	}
}
