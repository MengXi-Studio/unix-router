/**
 * 归一化 tsup/esbuild 构建产物（packages/core/dist/plugin/index.{mjs,cjs}），
 * 消除其中内嵌的「文件系统相对路径」，使产物在不同 OS / Node 环境下字节一致（可复现构建）。
 *
 * 背景：esbuild 打包 CJS 互操作时，会把每个被打包依赖的 realpath 以两种形态写进 bundle：
 *   1. 行首注释： // ../../node_modules/.pnpm/<pkg>/.../x.js
 *   2. __commonJS 的对象键： "<同一路径>"(exports[, module]) { ... }
 * （ESM 内联模块只有形态 1。）这些路径随构建机的 realpath 解析差异（Windows  junction
 * vs Linux symlink、Node 版本）而变化，导致 Windows 提交的 uni_modules 镜像与
 * Linux(CI / npm 发版)构建产物字节漂移，sync:uni-modules:check 失败。
 *
 * 处理：把每种出现的路径按「首次出现顺序」映射为环境无关的固定 token（__mod_0、__mod_1…），
 * 注释与键中同一路径映射到同一 token。esbuild 输出顺序由模块图决定（frozen lockfile 下图
 * 一致），与 OS 无关，故编号跨环境稳定；__commonJS 取首键（__getOwnPropNames(cb)[0]），
 * 键名无语义，改名不影响运行。
 *
 * 用法：node scripts/normalize-plugin-bundle.mjs
 *   由 packages/core 的 build script 在 tsup 之后、sync 镜像之前调用；幂等。
 */
import fs from 'node:fs'
import path from 'node:path'

const repoRoot = path.resolve(import.meta.dirname, '..')
const outDir = path.join(repoRoot, 'packages/core/dist/plugin')
const targets = ['index.mjs', 'index.cjs']

/**
 * 匹配 esbuild 内嵌路径的两种形态（多行模式）：
 *   - 行首注释：^// <path>
 *   - __commonJS 键："<path>"(exports[2]...)
 * 捕获其中包含 node_modules/ 的路径串；不含 node_modules/ 的注释/键不是构建机路径，跳过。
 */
const pathRe = /(?:^\/\/[ \t]+([^\n\r]*node_modules\/[^\n\r]*)[ \t]*$)|(?:"([^"\n\r]*node_modules\/[^"\n\r]*)"\(exports\d*)/gm

/** 归一化：返回 { out, count }；count 为去重后的路径种数 */
function normalize(content) {
	const tokenMap = new Map()
	const tokenFor = p => {
		if (!tokenMap.has(p)) tokenMap.set(p, `__mod_${tokenMap.size}`)
		return tokenMap.get(p)
	}

	// 单次扫描，按出现顺序登记路径（保证跨环境编号一致）
	let m
	while ((m = pathRe.exec(content)) !== null) {
		tokenFor((m[1] ?? m[2]).trim())
	}

	let out = content
	for (const [p, tok] of tokenMap) {
		// 转义路径中的正则元字符后全局替换；同一路径在注释与键中出现多次，统一替换
		const escaped = p.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
		out = out.replace(new RegExp(escaped, 'g'), tok)
	}
	return { out, count: tokenMap.size }
}

let updated = 0
for (const name of targets) {
	const file = path.join(outDir, name)
	if (!fs.existsSync(file)) {
		console.warn(`[normalize] 跳过（不存在）：${path.relative(repoRoot, file)}`)
		continue
	}
	const src = fs.readFileSync(file, 'utf8')
	const { out, count } = normalize(src)
	if (out !== src) {
		fs.writeFileSync(file, out, 'utf8')
		updated++
		console.log(`[normalize] ${name}：归一化 ${count} 个内嵌路径`)
	} else {
		console.log(`[normalize] ${name}：已是归一化状态，跳过`)
	}
}

// 校验：两种内嵌路径形态必须全部消除（残留即说明有未覆盖的新形态，立即失败防带病发布）
const probeRe = /(?:^\/\/[ \t]+[^\n\r]*node_modules\/[^\n\r]*$)|(?:"[^"\n\r]*node_modules\/[^"\n\r]*"\(exports\d*)/m
for (const name of targets) {
	const file = path.join(outDir, name)
	if (!fs.existsSync(file)) continue
	const content = fs.readFileSync(file, 'utf8')
	if (probeRe.test(content)) {
		console.error(`[normalize] 校验失败：${name} 仍残留内嵌 node_modules 路径`)
		process.exit(1)
	}
}

console.log(`[normalize] 完成（${updated} 个文件已更新）`)
