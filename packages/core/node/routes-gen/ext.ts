/**
 * routesGen 扩展声明解析（routes.ext.uts）
 *
 * 声明形态：export const xxx = [ { path | name, name?, meta?, beforeEnter? }, ... ]
 * 构建期解析为结构化条目，与 pages.json 推导的基础路由按 path/name 匹配合并；
 * beforeEnter 等表达式按原文保留（与宏注入机制同构），routes.gen.uts 保持纯数据。
 */
import { LiteralEntry, LiteralParseError, LiteralValue, parseUtsArrayLiteral } from '../shared/parsing/literal'
import { Draft } from '../shared/common/types'

/** 单条扩展声明 */
export type ExtEntry = {
	/** 匹配键：页面路径（pages.json 完整页面路径） */
	path: string | null
	/** 匹配键：路由 name（与 nameStrategy 推导结果或既有生成文件中的 name 匹配） */
	name: string | null
	/** meta 扩展字段（数据字段按结构解析，函数/标识符按原文保留） */
	metaExtra: LiteralEntry[]
	/** beforeEnter 函数原文（自包含表达式） */
	beforeEnter: string | null
	/** 不认识的字段（strict 时报错提示） */
	unknownFields: string[]
}

/** 扩展声明解析结果 */
export type ExtParseResult = {
	/** 解析到的扩展声明条目 */
	entries: ExtEntry[]
	/** 解析警告（strict 时报错提示） */
	warnings: string[]
	/** 解析错误（strict 时报错提示） */
	error: string | null
}

/** 解析扩展声明文件文本，定位 export const xxx = [ ... ] 数组并逐条解析 */
export function parseExtDeclarations(text: string): ExtParseResult {
	const warnings: string[] = []
	const anchor = /export\s+(?:const|let)\s+[A-Za-z_$][\w$]*\s*=/.exec(text)

	if (anchor === null) {
		return { entries: [], warnings, error: "未找到 'export const xxx =' 声明，扩展声明文件须导出一个数组字面量" }
	}

	const arrStart = text.indexOf('[', anchor.index + anchor[0].length)
	if (arrStart < 0) {
		return { entries: [], warnings, error: '声明右侧须为数组字面量 [ ... ]' }
	}

	// 括号配平取数组全文
	let depth = 0
	let arrEnd = -1

	for (let i = arrStart; i < text.length; i++) {
		const c = text[i]
		if (c === '[') depth++

		if (c === ']') {
			depth--

			if (depth === 0) {
				arrEnd = i
				break
			}
		}
	}

	if (arrEnd < 0) return { entries: [], warnings, error: '数组字面量未闭合' }

	let parsed: LiteralValue
	try {
		parsed = parseUtsArrayLiteral(text.slice(arrStart, arrEnd + 1))
	} catch (e) {
		const msg = e instanceof LiteralParseError ? e.message : String(e)
		return { entries: [], warnings, error: `扩展声明数组解析失败：${msg}` }
	}

	if (parsed.kind !== 'array') return { entries: [], warnings, error: '声明右侧须为数组字面量 [ ... ]' }

	const entries: ExtEntry[] = []
	for (const item of parsed.items) {
		if (item.kind !== 'object') {
			warnings.push('扩展声明数组中存在非对象条目，已忽略')
			continue
		}

		const entry = extFromEntries(item.entries)
		if (entry.error !== null) {
			warnings.push(entry.error)
			continue
		}

		entries.push(entry.entry)
	}

	return { entries, warnings, error: null }
}

/** 解析扩展声明条目 */
function extFromEntries(entries: LiteralEntry[]): { entry: ExtEntry; error: string | null } {
	const ext: ExtEntry = { path: null, name: null, metaExtra: [], beforeEnter: null, unknownFields: [] }

	for (const { key, value } of entries) {
		switch (key) {
			case 'path':
				if (value.kind === 'string') ext.path = value.value
				break

			case 'name':
				if (value.kind === 'string') ext.name = value.value
				break

			case 'meta':
				if (value.kind === 'object') ext.metaExtra = [...value.entries]
				break

			case 'beforeEnter':
				// 函数/标识符引用：按原文保留，生成时原样注入
				if (value.kind === 'raw') ext.beforeEnter = value.value
				break

			default:
				ext.unknownFields.push(key)
		}
	}

	if (ext.path === null && ext.name === null) {
		return { entry: ext, error: '扩展声明条目缺少匹配键：须声明 path 或 name 之一' }
	}

	if (ext.unknownFields.length > 0) {
		return { entry: ext, error: `扩展声明条目存在未支持字段 ${ext.unknownFields.join(', ')}（可用：path / name / meta / beforeEnter）` }
	}

	return { entry: ext, error: null }
}

/**
 * 将扩展声明合并进路由草稿（在 buildNames 之后执行，name 匹配基于推导结果）
 * 规则：显式 name 可覆盖推导名（冲突告警）；meta.title/isTab 由 pages.json 推导时声明不生效；
 * beforeEnter 后声明覆盖先前值；未匹配到路由的声明告警忽略。
 */
export function mergeExtIntoDrafts(drafts: Draft[], exts: ExtEntry[], warn: (msg: string) => void): void {
	for (const ext of exts) {
		const draft = ext.path !== null ? drafts.find(d => d.path === ext.path) : drafts.find(d => d.name === ext.name)
		const keyDesc = ext.path !== null ? `path: '${ext.path}'` : `name: '${ext.name}'`

		if (draft === undefined) {
			warn(`扩展声明未匹配到路由（${keyDesc}），已忽略`)
			continue
		}

		if (ext.name !== null && ext.name !== draft.name) {
			if (drafts.some(d => d !== draft && d.name === ext.name)) {
				warn(`扩展声明 name '${ext.name}'（${keyDesc}）与其他路由冲突，已忽略改名`)
			} else {
				draft.name = ext.name
			}
		}

		for (const e of ext.metaExtra) {
			// title/isTab 为 pages.json 推导字段，扩展声明仅在推导值缺失时补位
			if (e.key === 'title' && draft.title !== null) {
				warn(`路由 '${draft.path}' 的 meta.title 已由 pages.json style 推导，扩展声明被忽略`)
				continue
			}

			if (e.key === 'isTab' && draft.isTab) {
				warn(`路由 '${draft.path}' 的 meta.isTab 已由 pages.json tabBar 推导，扩展声明被忽略`)
				continue
			}

			draft.metaExtra = draft.metaExtra.filter(x => x.key !== e.key)
			draft.metaExtra.push(e)
		}

		if (ext.beforeEnter !== null) {
			if (draft.beforeEnter !== null) warn(`路由 '${draft.path}' 的 beforeEnter 被扩展声明覆盖`)
			draft.beforeEnter = ext.beforeEnter
		}
	}
}
