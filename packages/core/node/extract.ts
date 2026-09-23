/**
 * defineUniPage 宏剥离 与 <route-config> 块提取
 *
 * - 宏：等行数注释占位替换（行号不变），参数解析为 PageSpec（函数字段按原文保留）
 * - 块：仅提取内容供流水线消费，源码不动；编译期的块模块请求由虚拟模块拦截为空模块
 * - 优先级由消费方保证：macro 字段 > block 字段 > 插件推导
 */
import { LiteralEntry, LiteralParseError, LiteralValue, parseJsonc, parseUtsObjectLiteral } from './literal'
import { ExtractResult, PageSpec } from './types'

const emptySpec = (): PageSpec => ({
	title: null,
	name: null,
	isTab: false,
	order: null,
	iconPath: null,
	selectedIconPath: null,
	tabText: null,
	metaExtra: [],
	beforeEnter: null,
	redirect: null,
	unknownFields: []
})

/**
 * 剥离 defineUniPage(...) 宏调用（等行数注释占位）
 * 规范：每页至多一次，位于 script setup 顶层；重复按 errorStrategy 处理
 */
export function stripDefineUniPage(code: string): ExtractResult {
	const re = /\bdefineUniPage\s*\(/g
	const starts: number[] = []
	let m: RegExpExecArray | null
	while ((m = re.exec(code)) !== null) starts.push(m.index)
	if (starts.length === 0) return { code, spec: null, error: null }

	if (starts.length > 1) {
		return { code, spec: null, error: `defineUniPage 每页至多声明一次，发现 ${starts.length} 处` }
	}

	const start = starts[0]
	const parenStart = code.indexOf('(', start)
	let depth = 0
	let closeParen = -1
	for (let i = parenStart; i < code.length; i++) {
		const c = code[i]
		if (c === "'" || c === '"') {
			// 跳过字符串字面量
			const quote = c
			i++
			while (i < code.length) {
				if (code[i] === '\\') {
					i++
				} else if (code[i] === quote) {
					break
				}
				i++
			}
			continue
		}
		if (c === '(') depth++
		if (c === ')') {
			depth--
			if (depth === 0) {
				closeParen = i
				break
			}
		}
	}
	if (closeParen < 0) return { code, spec: null, error: 'defineUniPage(...) 括号未闭合' }

	const argsText = code.slice(parenStart + 1, closeParen)
	let spec: PageSpec
	try {
		const literal = parseUtsObjectLiteral(argsText.trim())
		if (literal.kind !== 'object') return { code, spec: null, error: 'defineUniPage 参数须为对象字面量' }
		spec = specFromEntries(literal.entries)
	} catch (e) {
		const msg = e instanceof LiteralParseError ? e.message : String(e)
		return { code, spec: null, error: `defineUniPage 参数解析失败：${msg}` }
	}

	// 等行数注释占位，保持行号稳定
	const matched = code.slice(start, closeParen + 1)
	const newlines = (matched.match(/\n/g) ?? []).length
	const placeholder = '/* defineUniPage 由 @meng-xi/unix-router/vite-plugin 剥离 */' + '\n'.repeat(newlines)
	return { code: code.slice(0, start) + placeholder + code.slice(closeParen + 1), spec, error: null }
}

/** 提取 <route-config> 块（lang="jsonc"（默认）| "uts"）；块本身不修改源码 */
export function extractRouteConfigBlock(code: string): ExtractResult {
	const re = /<route-config([^>]*)>([\s\S]*?)<\/route-config>/g
	const matches: RegExpExecArray[] = []
	let m: RegExpExecArray | null
	while ((m = re.exec(code)) !== null) matches.push(m)
	if (matches.length === 0) return { code, spec: null, error: null }
	if (matches.length > 1) {
		return { code, spec: null, error: `<route-config> 每页至多声明一个，发现 ${matches.length} 个` }
	}

	const block = matches[0]
	const langMatch = /lang\s*=\s*["']?([\w-]+)["']?/.exec(block[1])
	const lang = langMatch !== null ? langMatch[1] : 'jsonc'
	try {
		let entries: LiteralEntry[]
		if (lang === 'uts') {
			const literal = parseUtsObjectLiteral(block[2].trim())
			if (literal.kind !== 'object') return { code, spec: null, error: '<route-config lang="uts"> 内容须为对象字面量' }
			entries = literal.entries
		} else if (lang === 'jsonc' || lang === 'json') {
			const data = parseJsonc(block[2])
			if (data === null || typeof data !== 'object' || Array.isArray(data)) {
				return { code, spec: null, error: '<route-config> 内容须为 JSON 对象' }
			}
			entries = unknownToEntries(data as Record<string, unknown>)
		} else {
			return { code, spec: null, error: `不支持的 <route-config lang="${lang}">，可用：jsonc（默认）/ uts` }
		}
		return { code, spec: specFromEntries(entries), error: null }
	} catch (e) {
		const msg = e instanceof LiteralParseError ? e.message : String(e)
		return { code, spec: null, error: `<route-config> 内容解析失败：${msg}` }
	}
}

/** 宏 > 块 逐字段合并 */
export function mergeSpecs(macro: PageSpec | null, block: PageSpec | null): PageSpec {
	if (macro === null) return block ?? emptySpec()
	if (block === null) return macro
	// 字段级取高优先级：macro 有有效值（非 null / 非 false）则胜出
	const pick = <T>(mv: T, bv: T): T => ((mv !== null && mv !== false ? mv : bv) as T)
	const merged = emptySpec()
	merged.title = pick(macro.title, block.title)
	merged.name = pick(macro.name, block.name)
	merged.isTab = macro.isTab || block.isTab
	merged.order = pick(macro.order, block.order)
	merged.iconPath = pick(macro.iconPath, block.iconPath)
	merged.selectedIconPath = pick(macro.selectedIconPath, block.selectedIconPath)
	merged.tabText = pick(macro.tabText, block.tabText)
	merged.beforeEnter = pick(macro.beforeEnter, block.beforeEnter)
	merged.redirect = pick(macro.redirect, block.redirect)
	const metaKeys = new Set(macro.metaExtra.map((e) => e.key))
	merged.metaExtra = [...macro.metaExtra, ...block.metaExtra.filter((e) => !metaKeys.has(e.key))]
	merged.unknownFields = [...macro.unknownFields, ...block.unknownFields]
	return merged
}

function specFromEntries(entries: LiteralEntry[]): PageSpec {
	const spec = emptySpec()
	for (const { key, value } of entries) {
		switch (key) {
			case 'title':
				if (value.kind === 'string') spec.title = value.value
				break
			case 'name':
				if (value.kind === 'string') spec.name = value.value
				break
			case 'isTab':
				if (value.kind === 'boolean') spec.isTab = value.value
				break
			case 'redirect':
				if (value.kind === 'string') spec.redirect = value.value
				break
			case 'beforeEnter':
				// 函数/标识符引用：按原文保留，生成时原样注入
				if (value.kind === 'raw') spec.beforeEnter = value.value
				break
			case 'tab':
				if (value.kind === 'object') {
					for (const t of value.entries) {
						if (t.key === 'order' && t.value.kind === 'number') spec.order = t.value.value
						if (t.key === 'iconPath' && t.value.kind === 'string') spec.iconPath = t.value.value
						if (t.key === 'selectedIconPath' && t.value.kind === 'string') spec.selectedIconPath = t.value.value
						if (t.key === 'text' && t.value.kind === 'string') spec.tabText = t.value.value
					}
				}
				break
			case 'meta':
				if (value.kind === 'object') spec.metaExtra = [...value.entries]
				break
			default:
				spec.unknownFields.push(key)
		}
	}
	return spec
}

/** JSON 数据 → 字面量条目（jsonc 块使用；不支持函数字段） */
function unknownToEntries(data: Record<string, unknown>): LiteralEntry[] {
	const toLiteral = (v: unknown): LiteralValue => {
		if (typeof v === 'string') return { kind: 'string', value: v }
		if (typeof v === 'number') return { kind: 'number', value: v }
		if (typeof v === 'boolean') return { kind: 'boolean', value: v }
		if (v === null) return { kind: 'null' }
		if (Array.isArray(v)) return { kind: 'array', items: v.map(toLiteral) }
		if (typeof v === 'object') return { kind: 'object', entries: unknownToEntries(v as Record<string, unknown>) }
		return { kind: 'raw', value: JSON.stringify(v) ?? 'null' }
	}
	return Object.keys(data).map((key) => ({ key, value: toLiteral(data[key]) }))
}
