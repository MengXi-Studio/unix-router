/**
 * 产物渲染：pages.json、routes.gen.uts（preserveRouteChanges）、route-name.gen.d.ts、defineUniPage 宏 d.ts
 */
import fs from 'node:fs'
import { LiteralEntry, LiteralParseError, LiteralValue, parseUtsArrayLiteral, parseUtsObjectLiteral } from './literal'
import { ResolvedOptions } from './options'
import { PageEntry, ScanResult } from './types'

type Draft = {
	path: string
	rel: string
	pkgRoot: string | null
	title: string | null
	isTab: boolean
	order: number | null
	iconPath: string | null
	selectedIconPath: string | null
	tabText: string | null
	name: string
	metaExtra: Array<{ key: string; value: LiteralValue }>
	beforeEnter: string | null
	redirect: string | null
}

/* -------------------------------------------------------------------------- */
/* name 规范化（末段 camelCase → 冲突回退全路径 camelCase → errorStrategy）      */
/* -------------------------------------------------------------------------- */

const toCamelSegment = (seg: string): string => {
	const parts = seg.split(/[-_]+/).filter((p) => p !== '')
	if (parts.length === 0) return seg
	return (
		parts[0].charAt(0).toLowerCase() +
		parts[0].slice(1) +
		parts
			.slice(1)
			.map((p) => p.charAt(0).toUpperCase() + p.slice(1))
			.join('')
	)
}

const capitalize = (s: string): string => (s === '' ? s : s.charAt(0).toUpperCase() + s.slice(1))

/** 全路径 camelCase：剥离 pagesDir 前缀后逐段驼峰拼接（'pages/a/index' → 'aIndex'） */
function fullPathName(pagePath: string, pagesDirName: string): string {
	const segs = pagePath.split('/')
	if (segs.length > 1 && segs[0] === pagesDirName) segs.shift()
	return segs.map((s, i) => (i === 0 ? toCamelSegment(s) : capitalize(toCamelSegment(s)))).join('')
}

export function buildNames(drafts: Draft[], options: ResolvedOptions, fail: (msg: string) => void): void {
	const pagesDirName = options.pages.pagesDir.rel.split('/').pop() ?? 'pages'
	// 首轮：按策略出候选名（宏/块显式 name 已在 buildDrafts 写入，不被覆盖）
	for (const d of drafts) {
		if (d.name !== '') continue
		if (options.router.nameStrategy === 'fullPath') {
			d.name = fullPathName(d.path, pagesDirName)
		} else {
			const lastSeg = d.path.split('/').pop() ?? d.path
			d.name = toCamelSegment(lastSeg)
		}
	}
	// 冲突回退：候选名重复的组改为全路径 camelCase（显式 name 不参与回退改写）
	const countOf = (): Map<string, number> => {
		const count = new Map<string, number>()
		for (const d of drafts) count.set(d.name, (count.get(d.name) ?? 0) + 1)
		return count
	}
	for (const d of drafts) {
		if ((countOf().get(d.name) ?? 0) > 1 && d.name !== fullPathName(d.path, pagesDirName)) {
			d.name = fullPathName(d.path, pagesDirName)
		}
	}
	// 终检：仍冲突才按 errorStrategy 终止
	const finalCount = new Map<string, string[]>()
	for (const d of drafts) {
		const list = finalCount.get(d.name) ?? []
		list.push(d.path)
		finalCount.set(d.name, list)
	}
	const conflicts = [...finalCount.entries()].filter(([, paths]) => paths.length > 1)
	if (conflicts.length > 0) {
		fail(
			'路由 name 冲突（末段与全路径 camelCase 均无法消解）：\n' +
				conflicts.map(([name, paths]) => `  - ${name}: ${paths.join(', ')}`).join('\n')
		)
	}
}

/* -------------------------------------------------------------------------- */
/* 路由条目构建（宏 > 块 > 推导）                                              */
/* -------------------------------------------------------------------------- */

export function buildDrafts(scan: ScanResult, options: ResolvedOptions): Draft[] {
	const drafts: Draft[] = []
	for (const page of scan.pages) {
		const spec = page.spec
		const declaredName = spec?.name ?? null
		const draft: Draft = {
			path: page.path,
			rel: page.rel,
			pkgRoot: page.pkgRoot,
			title: spec?.title ?? options.pages.titleFallback,
			isTab: spec?.isTab ?? false,
			order: spec?.order ?? null,
			iconPath: spec?.iconPath ?? null,
			selectedIconPath: spec?.selectedIconPath ?? null,
			tabText: spec?.tabText ?? null,
			name: declaredName ?? '',
			metaExtra: spec?.metaExtra ?? [],
			beforeEnter: spec?.beforeEnter ?? null,
			redirect: spec?.redirect ?? null
		}
		drafts.push(draft)
	}
	// entryPage 移至主包首位（uni 启动页约定）
	const entry = options.pages.entryPage
	if (entry !== null) {
		const idx = drafts.findIndex((d) => d.path === entry)
		if (idx >= 0) {
			const [d] = drafts.splice(idx, 1)
			const firstMain = drafts.findIndex((x) => x.pkgRoot === null)
			drafts.splice(firstMain < 0 ? 0 : firstMain, 0, d)
		}
	}
	return drafts
}

/* -------------------------------------------------------------------------- */
/* 字面量 → UTS 文本渲染                                                       */
/* -------------------------------------------------------------------------- */

const quote = (s: string): string => "'" + s.replace(/\\/g, '\\\\').replace(/'/g, "\\'") + "'"

function renderLiteral(v: LiteralValue): string {
	switch (v.kind) {
		case 'string':
			return quote(v.value)
		case 'number':
			return String(v.value)
		case 'boolean':
			return v.value ? 'true' : 'false'
		case 'null':
			return 'null'
		case 'raw':
			return v.value
		case 'array':
			return '[ ' + v.items.map(renderLiteral).join(', ') + ' ]'
		case 'object':
			return '{ ' + v.entries.map((e) => `${e.key}: ${renderLiteral(e.value)}`).join(', ') + ' }'
	}
}

function renderEntries(entries: LiteralEntry[], indent: string): string {
	return entries.map((e) => `${indent}${e.key}: ${renderLiteral(e.value)},`).join('\n')
}

/* -------------------------------------------------------------------------- */
/* preserveRouteChanges：解析既有生成文件中的条目                               */
/* -------------------------------------------------------------------------- */

type PreservedEntry = {
	path: string
	entries: LiteralEntry[]
}

function parseExistingRoutes(existingText: string, exportName: string): PreservedEntry[] {
	const anchor = new RegExp(`export\\s+const\\s+${exportName}\\s*:\\s*[\\w.$<>]+\\[\\]\\s*=`).exec(existingText)
	if (anchor === null) return []
	const arrStart = existingText.indexOf('[', anchor.index + anchor[0].length - 1)
	if (arrStart < 0) return []
	// 括号配平取数组全文
	let depth = 0
	let arrEnd = -1
	for (let i = arrStart; i < existingText.length; i++) {
		const c = existingText[i]
		if (c === '[') depth++
		if (c === ']') {
			depth--
			if (depth === 0) {
				arrEnd = i
				break
			}
		}
	}
	if (arrEnd < 0) return []
	try {
		const parsed = parseUtsArrayLiteral(existingText.slice(arrStart, arrEnd + 1))
		if (parsed.kind !== 'array') return []
		const out: PreservedEntry[] = []
		for (const item of parsed.items) {
			if (item.kind !== 'object') continue
			const pathEntry = item.entries.find((e) => e.key === 'path')
			if (pathEntry === undefined || pathEntry.value.kind !== 'string') continue
			out.push({ path: pathEntry.value.value, entries: item.entries })
		}
		return out
	} catch (e) {
		const msg = e instanceof LiteralParseError ? e.message : String(e)
		console.warn(`[unix-router:route-gen] 既有路由文件解析失败，本次跳过 preserveRouteChanges：${msg}`)
		return []
	}
}

/* -------------------------------------------------------------------------- */
/* pages.json 渲染                                                            */
/* -------------------------------------------------------------------------- */

export function renderPagesJson(scan: ScanResult, options: ResolvedOptions, drafts: Draft[]): string {
	// 既有 pages.json：仅保留非生成字段（globalStyle 等）
	let preserved: Record<string, unknown> = {}
	try {
		if (fs.existsSync(options.pagesJsonPath.abs)) {
			const raw = fs.readFileSync(options.pagesJsonPath.abs, 'utf8')
			const parsed = JSON.parse(raw) as Record<string, unknown>
			preserved = parsed
			delete preserved.pages
			delete preserved.tabBar
			delete preserved.subPackages
			delete preserved.subpackages
		}
	} catch (e) {
		console.warn(`[unix-router:route-gen] 既有 pages.json 解析失败，手写字段将丢失：${String(e)}`)
	}

	const styleFor = (d: Draft): Record<string, unknown> | undefined =>
		d.title !== null ? { navigationBarTitleText: d.title } : undefined

	const mainPages = drafts
		.filter((d) => d.pkgRoot === null)
		.map((d) => {
			const style = styleFor(d)
			return style !== undefined ? { path: d.path, style } : { path: d.path }
		})

	const out: Record<string, unknown> = { pages: mainPages }

	// 分包
	const subRoots = [...new Set(drafts.filter((d) => d.pkgRoot !== null).map((d) => d.pkgRoot as string))]
	if (subRoots.length > 0) {
		out.subPackages = subRoots.map((root) => ({
			root,
			pages: drafts
				.filter((d) => d.pkgRoot === root)
				.map((d) => {
					const style = styleFor(d)
					return style !== undefined ? { path: d.rel, style } : { path: d.rel }
				})
		}))
	}

	// tabBar：isTab 页面按 tab.order 排序（缺省排后，保持扫描序稳定）
	const tabPages = drafts
		.filter((d) => d.isTab && d.pkgRoot === null)
		.map((d, i) => ({ d, i }))
		.sort((a, b) => (a.d.order ?? Number.MAX_SAFE_INTEGER) - (b.d.order ?? Number.MAX_SAFE_INTEGER) || a.i - b.i)
		.map((x) => x.d)
	if (tabPages.length > 0) {
		const chrome: Record<string, unknown> = {}
		if (options.pages.tabBar.color !== undefined) chrome.color = options.pages.tabBar.color
		if (options.pages.tabBar.selectedColor !== undefined) chrome.selectedColor = options.pages.tabBar.selectedColor
		if (options.pages.tabBar.backgroundColor !== undefined) chrome.backgroundColor = options.pages.tabBar.backgroundColor
		if (options.pages.tabBar.borderStyle !== undefined) chrome.borderStyle = options.pages.tabBar.borderStyle
		chrome.list = tabPages.map((d) => {
			const item: Record<string, unknown> = {
				pagePath: d.path,
				text: d.tabText ?? d.title ?? d.path.split('/').pop() ?? d.path
			}
			if (d.iconPath !== null) item.iconPath = d.iconPath
			if (d.selectedIconPath !== null) item.selectedIconPath = d.selectedIconPath
			return item
		})
		out.tabBar = chrome
	}

	for (const key of Object.keys(preserved)) out[key] = preserved[key]
	return JSON.stringify(out, null, '\t') + '\n'
}

/* -------------------------------------------------------------------------- */
/* routes.gen.uts 渲染                                                        */
/* -------------------------------------------------------------------------- */

export function renderRoutesGen(scan: ScanResult, options: ResolvedOptions, drafts: Draft[], existingText: string | null): string {
	const preserved = new Map<string, LiteralEntry[]>()
	if (existingText !== null && options.router.preserveRouteChanges) {
		for (const entry of parseExistingRoutes(existingText, options.router.exportName)) {
			preserved.set(entry.path, entry.entries)
		}
	}

	const usedTypes = new Set<string>(['RouteConfig'])
	const lines: string[] = []

	for (const d of drafts) {
		const prev = preserved.get(d.path)
		const prevMap = new Map<string, LiteralValue>((prev ?? []).map((e) => [e.key, e.value]))

		// meta：生成字段用新值，用户追加的自定义字段保留
		const metaEntries: LiteralEntry[] = []
		const generatedMetaKeys = new Set<string>()
		if (d.title !== null) {
			metaEntries.push({ key: 'title', value: { kind: 'string', value: d.title } })
			generatedMetaKeys.add('title')
		}
		if (d.isTab) {
			metaEntries.push({ key: 'isTab', value: { kind: 'boolean', value: true } })
			generatedMetaKeys.add('isTab')
		}
		for (const extra of d.metaExtra) {
			metaEntries.push(extra)
			generatedMetaKeys.add(extra.key)
		}
		const prevMeta = prevMap.get('meta')
		if (prevMeta !== undefined && prevMeta.kind === 'object') {
			for (const e of prevMeta.entries) {
				if (!generatedMetaKeys.has(e.key)) metaEntries.push(e)
			}
		}
		if (metaEntries.length > 0) usedTypes.add('RouteMeta')

		const fields: string[] = []
		fields.push(`path: ${quote(d.path)}`)
		fields.push(`name: ${quote(d.name)}`)
		// redirect：声明优先；未声明则保留用户既有值
		const prevRedirect = prevMap.get('redirect')
		if (d.redirect !== null) fields.push(`redirect: ${quote(d.redirect)}`)
		else if (prevRedirect !== undefined) fields.push(`redirect: ${renderLiteral(prevRedirect)}`)
		if (metaEntries.length > 0) fields.push(`meta: { ${metaEntries.map((e) => `${e.key}: ${renderLiteral(e.value)}`).join(', ')} }`)
		// beforeEnter：宏/块声明优先；未声明则保留用户既有守卫
		const prevBeforeEnter = prevMap.get('beforeEnter')
		if (d.beforeEnter !== null) fields.push(`beforeEnter: ${reindent(d.beforeEnter, '\t\t')}`)
		else if (prevBeforeEnter !== undefined && prevBeforeEnter.kind === 'raw') fields.push(`beforeEnter: ${reindent(prevBeforeEnter.value, '\t\t')}`)
		// 其他用户自定义字段（如 redirect 对象形式、component 等）完整保留
		for (const [key, value] of prevMap) {
			if (['path', 'name', 'redirect', 'meta', 'beforeEnter'].includes(key)) continue
			fields.push(`${key}: ${renderLiteral(value)}`)
		}

		lines.push('\t{')
		lines.push(fields.map((f) => '\t\t' + f).join(',\n') + (fields.length > 0 ? ',' : ''))
		lines.push('\t},')
	}

	const importLine =
		usedTypes.size > 1
			? `import type { ${[...usedTypes].join(', ')} } from '${options.router.importFrom}'`
			: `import type { RouteConfig } from '${options.router.importFrom}'`

	const header = [
		'/**',
		' * 本文件由 @meng-xi/unix-router/vite-plugin 自动生成，请勿整体手改。',
		' * preserveRouteChanges 已开启：重新生成时保留你追加的自定义字段与整条自定义路由；',
		' * 页面级声明请使用页面内的 defineUniPage 宏或 <route-config> 块。',
		' */'
	]

	return (
		[...header, importLine, '', `export const ${options.router.exportName}: RouteConfig[] = [`].join('\n') +
		'\n' +
		lines.join('\n') +
		'\n]\n'
	)
}

/** 多行函数原文按目标缩进重排（首行已带字段缩进，后续行补齐） */
function reindent(raw: string, indent: string): string {
	const lines = raw.split('\n')
	if (lines.length === 1) return raw
	return lines.map((l, i) => (i === 0 || l.trim() === '' ? l : indent + l)).join('\n')
}

/* -------------------------------------------------------------------------- */
/* dts 渲染                                                                   */
/* -------------------------------------------------------------------------- */

/** RouteNameMap 字面量类型声明（WEB 端编辑器提示） */
export function renderRouteNameDts(drafts: Draft[], options: ResolvedOptions): string {
	const lines: string[] = [
		'/**',
		' * RouteNameMap 字面量类型增强（由 @meng-xi/unix-router/vite-plugin 生成）',
		' * 供 WEB 端 RouteName 类型推导；原生端 UTS 不支持 keyof 组合，仍为 string。',
		' */',
		`import '${options.router.importFrom}'`,
		'',
		`declare module '${options.router.importFrom}' {`,
		'\tinterface RouteNameMap {'
	]
	for (const d of drafts) {
		const title = d.title !== null ? d.title : d.path
		lines.push(`\t\t/** ${title} */`)
		lines.push(`\t\t${d.name}: ${quote(d.path)}`)
	}
	lines.push('\t}', '}', '')
	return lines.join('\n')
}

/** defineUniPage 宏全局类型声明（IDE 提示；宏编译期剥离不占运行时体积） */
export function renderMacroDts(): string {
	return [
		'/**',
		' * defineUniPage 页面配置宏类型声明（由 @meng-xi/unix-router/vite-plugin 生成）',
		' * 宏在编译期被剥离，运行时不占用体积。',
		' */',
		'declare function defineUniPage(config: {',
		"\t/** 页面标题：同步到 pages.json 的 navigationBarTitleText 与路由 meta.title */",
		'\ttitle?: string',
		"\t/** 路由名（缺省按 camelCase 规范化生成） */",
		'\tname?: string',
		'\t/** 是否 tabBar 页面 */',
		'\tisTab?: boolean',
		'\t/** tabBar 附属信息 */',
		'\ttab?: { order?: number; iconPath?: string; selectedIconPath?: string; text?: string }',
		'\t/** 路由 meta 扩展字段（需与 RouteMeta 定义匹配） */',
		'\tmeta?: Record<string, any>',
		'\t/** 路由重定向目标（页面路径） */',
		'\tredirect?: string',
		'\t/** 路由独享前置守卫（须自包含，生成文件内不引用页面作用域） */',
		'\tbeforeEnter?: (to: any, from: any) => any',
		'}): void',
		'',
		'export {}',
		''
	].join('\n')
}
