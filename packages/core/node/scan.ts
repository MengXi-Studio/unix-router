/**
 * 页面目录扫描（流水线阶段一数据源）
 *
 * 扫描时直接读取页面文件并提取宏/块声明（不依赖 transform 登记时序），
 * transform 剥离仅为让 uni 编译器看到干净源码，二者互为独立。
 */
import fs from 'node:fs'
import path from 'node:path'
import { extractRouteConfigBlock, mergeSpecs, stripDefineUniPage } from './extract'
import { ResolvedOptions } from './options'
import { PageEntry, ScanResult } from './types'

const isExcluded = (relPath: string, patterns: Array<string | RegExp>): boolean =>
	patterns.some((p) => (typeof p === 'string' ? relPath.includes(p) : p.test(relPath)))

/** 递归收集目录下符合扩展名的页面文件（返回不含扩展名的相对路径，posix 分隔） */
function walk(dirAbs: string, relBase: string, options: ResolvedOptions, out: string[]): void {
	if (!fs.existsSync(dirAbs)) return
	const items = fs.readdirSync(dirAbs, { withFileTypes: true }).sort((a, b) => a.name.localeCompare(b.name))
	for (const item of items) {
		const abs = path.join(dirAbs, item.name)
		if (item.isDirectory()) {
			if (item.name === 'node_modules' || item.name === 'unpackage' || item.name === '.git') continue
			const rel = relBase + item.name + '/'
			if (isExcluded(rel, options.pages.excludePatterns)) continue
			walk(abs, rel, options, out)
			continue
		}
		if (!item.isFile()) continue
		const ext = path.extname(item.name)
		if (!options.pages.includeExtensions.includes(ext)) continue
		// 带扩展名的相对路径（posix 分隔）；无扩展名页面路径由消费方剥离
		const relWithExt = relBase + item.name
		if (isExcluded(relWithExt, options.pages.excludePatterns)) continue
		out.push(relWithExt)
	}
}

/** 扫描主包与分包页面目录，并就地提取每页的宏/块声明 */
export function scanPages(options: ResolvedOptions): ScanResult {
	const warnings: string[] = []
	const errors: string[] = []
	const pages: PageEntry[] = []

	// 主包：页面路径 = 相对 pagesDir 父目录的 posix 路径（'src/pages' → 'pages/...'，'pages' → 'pages/...'）
	const mainBase = path.dirname(options.pages.pagesDir.abs)
	const mainRels: string[] = []
	walk(options.pages.pagesDir.abs, '', options, mainRels)
	for (const relWithExt of mainRels) {
		const file = path.join(options.pages.pagesDir.abs, relWithExt)
		const ext = path.extname(relWithExt)
		const rel = relWithExt.slice(0, relWithExt.length - ext.length)
		const pathWithExt = toPosix(path.relative(mainBase, file))
		pages.push({
			path: pathWithExt.slice(0, pathWithExt.length - ext.length),
			rel,
			file,
			pkgRoot: null,
			spec: null,
			macro: null,
			block: null
		})
	}

	// 分包：路径 = root + '/' + 包内相对路径
	for (const sub of options.pages.subPackages) {
		const subRels: string[] = []
		walk(sub.dir.abs, '', options, subRels)
		for (const relWithExt of subRels) {
			const ext = path.extname(relWithExt)
			const rel = relWithExt.slice(0, relWithExt.length - ext.length)
			pages.push({
				path: toPosix(sub.root + '/' + rel),
				rel,
				file: path.join(sub.dir.abs, relWithExt),
				pkgRoot: sub.root,
				spec: null,
				macro: null,
				block: null
			})
		}
	}

	// 就近声明提取（读文件，独立于 transform 时序）
	for (const page of pages) {
		let content: string
		try {
			content = fs.readFileSync(page.file, 'utf8')
		} catch (e) {
			errors.push(`读取页面失败 ${page.file}: ${String(e)}`)
			continue
		}
		const macro = stripDefineUniPage(content)
		const block = extractRouteConfigBlock(content)
		if (macro.error !== null) errors.push(`${path.basename(page.file)}: ${macro.error}`)
		if (block.error !== null) errors.push(`${path.basename(page.file)}: ${block.error}`)
		if (macro.spec !== null && macro.spec.unknownFields.length > 0) {
			warnings.push(`${path.basename(page.file)}: defineUniPage 存在未识别字段 ${macro.spec.unknownFields.join(', ')}（已忽略）`)
		}
		if (block.spec !== null && block.spec.unknownFields.length > 0) {
			warnings.push(`${path.basename(page.file)}: <route-config> 存在未识别字段 ${block.spec.unknownFields.join(', ')}（已忽略）`)
		}
		page.macro = macro.spec
		page.block = block.spec
		page.spec = mergeSpecs(macro.spec, block.spec)
	}

	pages.sort((a, b) => a.path.localeCompare(b.path))
	return { pages, warnings, errors }
}

const toPosix = (p: string): string => p.split(path.sep).join('/')
