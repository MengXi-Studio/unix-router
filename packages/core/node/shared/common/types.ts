/**
 * 跨模块共享的数据类型
 *
 * @remarks
 * - PageSpec / ExtractResult：宏与块解析产物（extract 产出、scan 消费）
 * - PageEntry / ScanResult：页面扫描产物（scan 产出、generate / 各插件消费）
 * - Draft：路由条目草稿（routeGen 由扫描产出、routesGen 由 pages.json + 扩展声明产出）
 * - 模块私有类型（ResolvedOptions 等）仍留在各自模块
 *
 * @packageDocumentation
 */
import { LiteralValue } from '../parsing/literal'

/** 页面就近声明（宏或块）解析结果 */
export type PageSpec = {
	/** 页面标题 */
	title: string | null
	/** 页面名称 */
	name: string | null
	/** 是否为 tab 页面 */
	isTab: boolean
	/** 页面排序 */
	order: number | null
	/** 页面图标路径 */
	iconPath: string | null
	/** 页面选中图标路径 */
	selectedIconPath: string | null
	/** tab 页面文本 */
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
export type ExtractResult = {
	/** 处理后的源码 */
	code: string
	/** 解析后的 spec */
	spec: PageSpec | null
	/** 解析错误信息 */
	error: string | null
}

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
	/** 宏声明 */
	macro: PageSpec | null
	/** 块声明 */
	block: PageSpec | null
}

/** 扫描结果（流水线阶段一输出） */
export type ScanResult = {
	/** 页面扫描条目 */
	pages: PageEntry[]
	/** 警告信息 */
	warnings: string[]
	/** 错误信息 */
	errors: string[]
}

/**
 * 路由条目草稿（渲染 pages.json / routes.gen.uts 的中间态）。
 *
 * @remarks
 * routeGen 由页面扫描产出；routesGen 由 pages.json + 扩展声明合并产出。
 * `title` / `isTab` / `order` 等推导字段语义与 {@link PageEntry.spec} 对应字段一致。
 */
export type Draft = {
	/** pages.json 页面路径（分包含 root 前缀），如 'pages/index/index'、'pages-sub/setting/setting' */
	path: string
	/** 分包内相对路径（分包 pages.json 条目用），如 'setting/setting'；主包与 path 一致 */
	rel: string
	/** 所在分包 root；主包为 null */
	pkgRoot: string | null
	/** 页面标题 */
	title: string | null
	/** 是否为 tab 页面 */
	isTab: boolean
	/** 页面排序 */
	order: number | null
	/** 页面图标路径 */
	iconPath: string | null
	/** 页面选中图标路径 */
	selectedIconPath: string | null
	/** tab 页面文本 */
	tabText: string | null
	/** 空串表示未显式声明，由 buildNames 按策略推导 */
	name: string
	/** meta 扩展字段（数据字段；嵌套对象/数组按原文注入） */
	metaExtra: Array<{ key: string; value: LiteralValue }>
	/** beforeEnter 函数原文（自包含表达式） */
	beforeEnter: string | null
	/** 重定向目标（页面路径字符串） */
	redirect: string | null
}
