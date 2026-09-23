/**
 * 跨模块共享的数据类型（对齐 src 的「共享类型收敛、模块私有类型留模块内」惯例）
 *
 * - PageSpec / ExtractResult：宏与块解析产物（extract 产出、scan 消费）
 * - PageEntry / ScanResult：页面扫描产物（scan 产出、generate / index 消费）
 * - 模块私有类型（Draft、ResolvedOptions 等）仍留在各自模块
 */
import { LiteralValue } from './literal'

/** 页面就近声明（宏或块）解析结果 */
export type PageSpec = {
	title: string | null
	name: string | null
	isTab: boolean
	order: number | null
	iconPath: string | null
	selectedIconPath: string | null
	tabText: string | null
	/** meta 扩展字段（数据字段；嵌套对象/数组按原文注入） */
	metaExtra: Array<{ key: string; value: LiteralValue }>
	/** beforeEnter 函数原文（自包含表达式） */
	beforeEnter: string | null
	/** 重定向目标（页面路径字符串） */
	redirect: string | null
	/** 不认识的字段（strict 时报错提示） */
	unknownFields: string[]
}

/** 宏剥离 / 块提取的统一返回（code 为处理后的源码，spec 为解析结果） */
export type ExtractResult = { code: string; spec: PageSpec | null; error: string | null }

/** 页面扫描条目 */
export type PageEntry = {
	/** pages.json 页面路径（分包含 root 前缀），如 'pages/index/index'、'pages-sub/setting/setting' */
	path: string
	/** 分包内相对路径（分包 pages.json 条目用），如 'setting/setting'；主包与 path 一致 */
	rel: string
	/** 源文件绝对路径 */
	file: string
	/** 所在分包 root；主包为 null */
	pkgRoot: string | null
	/** 扫描时提取的就近声明（macro 优先级高于 block） */
	spec: PageSpec | null
	macro: PageSpec | null
	block: PageSpec | null
}

/** 扫描结果（流水线阶段一输出） */
export type ScanResult = {
	pages: PageEntry[]
	warnings: string[]
	errors: string[]
}
