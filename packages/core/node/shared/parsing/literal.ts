/**
 * UTS 对象字面量解析器（构建期专用，供宏参数与既有生成文件解析）。
 *
 * @remarks
 * 纯数据字段解析为结构化值；函数/标识符等表达式按原文保留（`kind: 'raw'`），
 * 供生成 routes.gen.uts 时原样注入，类型安全由 UTS 编译链兜底。
 *
 * @packageDocumentation
 */

/**
 * 字面量解析结果（判别联合）。
 *
 * @remarks
 * `raw` 分支保留表达式原文（函数/标识符/模板串等），不做语义解析；
 * 其余分支为可序列化的纯数据值。
 */
export type LiteralValue =
	| { kind: 'string'; value: string }
	| { kind: 'number'; value: number }
	| { kind: 'boolean'; value: boolean }
	| { kind: 'null' }
	| { kind: 'raw'; value: string }
	| { kind: 'object'; entries: LiteralEntry[] }
	| { kind: 'array'; items: LiteralValue[] }

/** 对象字面量的键值对（key 为字面键名，value 为递归解析结果） */
export type LiteralEntry = { key: string; value: LiteralValue }

/** 字面量解析失败（语法错误 / 非预期起始字符等）时抛出 */
export class LiteralParseError extends Error {}

/** 标识符起始字符（字母、下划线、美元符号） */
const IDENT_START = /[A-Za-z_$]/

/**
 * 字面量解析器（构建期专用，供宏参数与既有生成文件解析）。
 *
 * @remarks
 * 纯数据字段解析为结构化值；函数/标识符等表达式按原文保留（`kind: 'raw'`），
 * 供生成 routes.gen.uts 时原样注入，类型安全由 UTS 编译链兜底。
 */
class Parser {
	private pos = 0

	constructor(private readonly src: string) {}

	/** 入口：解析一个对象字面量（期望首字符为 '{'） */
	parseObjectLiteral(): LiteralValue {
		this.skipWs()

		if (this.peek() !== '{') {
			throw new LiteralParseError(`期望对象字面量 '{'，实际 '${this.peek()}'（位置 ${this.pos}）`)
		}

		return this.parseValue()
	}

	/** 入口：解析一个数组字面量（期望首字符为 '['） */
	parseArrayLiteral(): LiteralValue {
		this.skipWs()

		if (this.peek() !== '[') {
			throw new LiteralParseError(`期望数组字面量 '['，实际 '${this.peek()}'（位置 ${this.pos}）`)
		}

		return this.parseArray()
	}

	private peek(): string {
		return this.src.charAt(this.pos) || '<eof>'
	}

	private skipWs(): void {
		while (this.pos < this.src.length) {
			const c = this.src[this.pos]

			if (c === ' ' || c === '\t' || c === '\n' || c === '\r') {
				this.pos++
				continue
			}

			if (c === '/' && this.src[this.pos + 1] === '/') {
				const end = this.src.indexOf('\n', this.pos)
				this.pos = end < 0 ? this.src.length : end
				continue
			}

			if (c === '/' && this.src[this.pos + 1] === '*') {
				const end = this.src.indexOf('*/', this.pos + 2)
				this.pos = end < 0 ? this.src.length : end + 2
				continue
			}

			break
		}
	}

	private parseValue(): LiteralValue {
		this.skipWs()

		const c = this.peek()
		if (c === '{') return this.parseObject()

		if (c === '[') return this.parseArray()

		if (c === "'" || c === '"') return this.parseQuotedString()

		if (c === '`') return this.parseTemplate()

		if (/[0-9]/.test(c) || (c === '-' && /[0-9]/.test(this.src.charAt(this.pos + 1)))) return this.parseNumber()

		if (IDENT_START.test(c)) return this.parseIdentLike()

		// '(' 开头的表达式（如箭头函数参数列表）：按原文保留
		if (c === '(') return { kind: 'raw', value: this.captureRaw() }

		throw new LiteralParseError(`无法解析的字符 '${c}'（位置 ${this.pos}）`)
	}

	private parseObject(): LiteralValue {
		this.pos++ // '{'
		const entries: LiteralEntry[] = []

		while (true) {
			this.skipWs()

			if (this.peek() === '}') {
				this.pos++
				return { kind: 'object', entries }
			}

			if (this.peek() === ',') {
				this.pos++
				continue
			}

			// key：标识符或字符串
			let key: string
			const c = this.peek()

			if (c === "'" || c === '"') {
				key = this.parseQuotedString().value
			} else if (IDENT_START.test(c)) {
				const start = this.pos
				while (this.pos < this.src.length && /[A-Za-z0-9_$]/.test(this.src[this.pos])) this.pos++
				key = this.src.slice(start, this.pos)
			} else {
				throw new LiteralParseError(`非法属性名 '${c}'（位置 ${this.pos}）`)
			}

			this.skipWs()
			if (this.peek() !== ':') {
				throw new LiteralParseError(`属性 '${key}' 后期望 ':'，实际 '${this.peek()}'（位置 ${this.pos}）`)
			}

			this.pos++
			const value = this.parseValue()
			entries.push({ key, value })
		}
	}

	private parseArray(): LiteralValue {
		this.pos++ // '['
		const items: LiteralValue[] = []

		while (true) {
			this.skipWs()
			if (this.peek() === ']') {
				this.pos++
				return { kind: 'array', items }
			}

			if (this.peek() === ',') {
				this.pos++
				continue
			}
			items.push(this.parseValue())
		}
	}

	private parseQuotedString(): { kind: 'string'; value: string } {
		const quote = this.src[this.pos]
		this.pos++
		let out = ''

		while (this.pos < this.src.length) {
			const c = this.src[this.pos]
			if (c === '\\') {
				const next = this.src[this.pos + 1]
				const map: Record<string, string> = { n: '\n', t: '\t', r: '\r', "'": "'", '"': '"', '\\': '\\' }
				out += next !== undefined && next in map ? map[next] : (next ?? '')
				this.pos += 2
				continue
			}

			if (c === quote) {
				this.pos++
				return { kind: 'string', value: out }
			}

			out += c
			this.pos++
		}

		throw new LiteralParseError(`字符串未闭合（位置 ${this.pos}）`)
	}

	private parseTemplate(): LiteralValue {
		// 模板字符串无法静态求值，按原文保留
		const start = this.pos
		this.pos++ // '`'

		while (this.pos < this.src.length) {
			const c = this.src[this.pos]
			if (c === '\\') {
				this.pos += 2
				continue
			}

			if (c === '`') {
				this.pos++
				return { kind: 'raw', value: this.src.slice(start, this.pos) }
			}

			this.pos++
		}

		throw new LiteralParseError(`模板字符串未闭合（位置 ${this.pos}）`)
	}

	private parseNumber(): LiteralValue {
		const rest = this.src.slice(this.pos)
		const m = /^-?[0-9]+(\.[0-9]+)?([eE][+-]?[0-9]+)?/.exec(rest)
		if (m === null) throw new LiteralParseError(`非法数字（位置 ${this.pos}）`)

		this.pos += m[0].length
		return { kind: 'number', value: parseFloat(m[0]) }
	}

	private parseIdentLike(): LiteralValue {
		const start = this.pos
		while (this.pos < this.src.length && /[A-Za-z0-9_$]/.test(this.src[this.pos])) this.pos++

		const ident = this.src.slice(start, this.pos)
		if (ident === 'true') return { kind: 'boolean', value: true }
		if (ident === 'false') return { kind: 'boolean', value: false }
		if (ident === 'null') return { kind: 'null' }

		// 函数表达式 / 标识符引用 / 其他表达式：从起点做括号配平扫描到顶层分隔符，按原文保留
		this.pos = start
		return { kind: 'raw', value: this.captureRaw() }
	}

	/** 从当前位置扫描一个完整表达式原文，直到顶层（深度 0）出现 ',' '}' ']' 或结尾 */
	private captureRaw(): string {
		const start = this.pos
		let depth = 0

		while (this.pos < this.src.length) {
			const c = this.src[this.pos]
			if (c === "'" || c === '"') {
				this.skipQuoted()
				continue
			}

			if (c === '`') {
				this.skipTemplate()
				continue
			}

			if (c === '/' && this.src[this.pos + 1] === '/') {
				const end = this.src.indexOf('\n', this.pos)
				this.pos = end < 0 ? this.src.length : end
				continue
			}

			if (c === '/' && this.src[this.pos + 1] === '*') {
				const end = this.src.indexOf('*/', this.pos + 2)
				this.pos = end < 0 ? this.src.length : end + 2
				continue
			}

			if (c === '(' || c === '[' || c === '{') depth++
			if (c === ')' || c === ']' || c === '}') {
				if (depth === 0) break
				depth--
			}
			if (depth === 0 && c === ',') break

			this.pos++
		}

		return this.src.slice(start, this.pos).trim()
	}

	private skipQuoted(): void {
		const quote = this.src[this.pos]
		this.pos++

		while (this.pos < this.src.length) {
			const c = this.src[this.pos]
			if (c === '\\') {
				this.pos += 2
				continue
			}

			if (c === quote) {
				this.pos++
				return
			}
			this.pos++
		}
	}

	private skipTemplate(): void {
		this.pos++

		while (this.pos < this.src.length) {
			const c = this.src[this.pos]
			if (c === '\\') {
				this.pos += 2
				continue
			}

			if (c === '`') {
				this.pos++
				return
			}
			this.pos++
		}
	}
}

/**
 * 解析 UTS 对象字面量文本。
 *
 * @param text - 源文本，首字符须为 `{`
 * @returns 解析结果（表达式按 `raw` 原文保留）
 * @throws {@link LiteralParseError} 文本不是合法对象字面量时
 */
export function parseUtsObjectLiteral(text: string): LiteralValue {
	return new Parser(text).parseObjectLiteral()
}

/**
 * 解析 UTS 数组字面量文本。
 *
 * @param text - 源文本，首字符须为 `[`
 * @returns 解析结果（表达式按 `raw` 原文保留）
 * @throws {@link LiteralParseError} 文本不是合法数组字面量时
 */
export function parseUtsArrayLiteral(text: string): LiteralValue {
	return new Parser(text).parseArrayLiteral()
}

/**
 * 解析 JSONC（容忍 `//` 与 `/* *​/` 注释、尾逗号），用于 pages.json 与 lang="jsonc" 块。
 *
 * @param text - JSONC 源文本
 * @returns 反序列化结果
 * @throws `SyntaxError` 剥离注释/尾逗号后仍非法 JSON 时
 */
export function parseJsonc(text: string): unknown {
	let out = ''
	let pos = 0

	while (pos < text.length) {
		const c = text[pos]
		if (c === "'" || c === '"') {
			const quote = c
			out += c
			pos++

			while (pos < text.length) {
				const ch = text[pos]
				out += ch

				if (ch === '\\') {
					out += text[pos + 1] ?? ''
					pos += 2
					continue
				}

				pos++
				if (ch === quote) break
			}

			continue
		}

		if (c === '/' && text[pos + 1] === '/') {
			const end = text.indexOf('\n', pos)
			if (end < 0) break

			out += ' '
			pos = end

			continue
		}

		if (c === '/' && text[pos + 1] === '*') {
			const end = text.indexOf('*/', pos + 2)
			pos = end < 0 ? text.length : end + 2
			out += ' '

			continue
		}

		// 尾逗号：跳过顶层/嵌套中位于 } 或 ] 之前的逗号
		if (c === ',') {
			let look = pos + 1
			while (look < text.length && /\s/.test(text[look])) look++

			if (text[look] === '}' || text[look] === ']') {
				out += ' '
				pos++

				continue
			}
		}

		out += c
		pos++
	}

	return JSON.parse(out)
}
