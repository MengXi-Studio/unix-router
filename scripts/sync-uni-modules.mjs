/**
 * uni_modules 镜像同步（两组镜像对，uni_modules 插件自包含分发）：
 *   1. packages/core/src         → playground/uni_modules/ux-router/utssdk（运行时 UTS 源码，纯镜像）
 *   2. packages/core/dist/plugin → playground/uni_modules/ux-router/plugins（构建期 vite 插件产物）
 *
 * 库有两个分发形态（npm 包 / uni_modules 插件）：运行时链路走 uni_modules，必须与 core/src
 * 逐文件一致；构建期链路（vite.config.ts 相对路径引入 plugins/index.mjs）也随插件分发，
 * 免去 HBuilderX 用户安装 npm 包。本脚本做全量镜像（含删除基准源已不存在的文件）。
 *
 * 用法：node scripts/sync-uni-modules.mjs [--check]
 *   --check  仅校验并输出漂移清单，有漂移退出码 1（供 CI / prebuild 使用；不触发构建）
 *   同步模式：先构建 @meng-xi/unix-router（tsup 产出 dist/plugin），再逐对镜像
 */
import fs from 'node:fs'
import path from 'node:path'
import { execSync } from 'node:child_process'
import { createHash } from 'node:crypto'

// 基于脚本位置推导仓库根，不依赖调用方 cwd（import.meta.dirname 为路径字符串，Node ≥ 20.11）
const repoRoot = path.resolve(import.meta.dirname, '..')
const check = process.argv.includes('--check')

/** 镜像对：src 为基准源，dest 为镜像目标 */
const pairs = [
	{
		label: 'utssdk（运行时源码）',
		src: path.join(repoRoot, 'packages/core/src'),
		dest: path.join(repoRoot, 'packages/playground/uni_modules/ux-router/utssdk')
	},
	{
		label: 'plugins（构建期插件产物）',
		src: path.join(repoRoot, 'packages/core/dist/plugin'),
		dest: path.join(repoRoot, 'packages/playground/uni_modules/ux-router/plugins')
	}
]

/** 递归收集目录下文件相对路径（posix 分隔；base 固定为遍历起点，防递归后基准漂移） */
function walk(dir, base = dir, out = []) {
	for (const item of fs.readdirSync(dir, { withFileTypes: true })) {
		const abs = path.join(dir, item.name)
		if (item.isDirectory()) walk(abs, base, out)
		else out.push(path.relative(base, abs).split(path.sep).join('/'))
	}
	return out
}

const sha256 = (abs) => createHash('sha256').update(fs.readFileSync(abs)).digest('hex')
const rel = (abs) => path.relative(repoRoot, abs)

/** 以 pair.src 为基准算出漂移清单（src / dest 均须存在，调用方保证） */
function diffPair(pair) {
	const srcFiles = walk(pair.src)
	const destFiles = walk(pair.dest)
	const srcSet = new Set(srcFiles)
	const destSet = new Set(destFiles)

	const added = []
	const updated = []
	const removed = []
	for (const f of srcFiles) {
		if (!destSet.has(f)) added.push(f)
		else if (sha256(path.join(pair.src, f)) !== sha256(path.join(pair.dest, f))) updated.push(f)
	}
	for (const f of destFiles) {
		if (!srcSet.has(f)) removed.push(f)
	}
	return { added, updated, removed }
}

const report = (diff) => {
	for (const f of diff.added) console.error(`  + ${f}（镜像缺失）`)
	for (const f of diff.updated) console.error(`  ~ ${f}（内容不一致）`)
	for (const f of diff.removed) console.error(`  - ${f}（基准源已删除）`)
}

if (check) {
	// 校验模式：不触发构建，逐对比对现有产物，任一对漂移即退出码 1
	let drifted = false
	for (const pair of pairs) {
		if (!fs.existsSync(pair.src) || !fs.existsSync(pair.dest)) {
			console.error(`镜像漂移（${pair.label}）：目录缺失 ${!fs.existsSync(pair.src) ? rel(pair.src) : rel(pair.dest)}`)
			drifted = true
			continue
		}
		const diff = diffPair(pair)
		if (diff.added.length + diff.updated.length + diff.removed.length > 0) {
			console.error(`镜像漂移（${pair.label}，以 ${rel(pair.src)} 为准）：`)
			report(diff)
			drifted = true
		} else {
			console.log(`镜像一致：${rel(pair.src)} ≡ ${rel(pair.dest)}`)
		}
	}
	process.exit(drifted ? 1 : 0)
}

// 同步模式：先构建 core（plugins 镜像对的产物源），失败则终止
console.log('[1/2] 构建 @meng-xi/unix-router（tsup → dist/plugin）...')
execSync('pnpm --filter @meng-xi/unix-router build', { cwd: repoRoot, stdio: 'inherit' })

console.log('[2/2] 镜像同步...')
for (const pair of pairs) {
	if (!fs.existsSync(pair.src)) {
		console.error(`基准源缺失：${rel(pair.src)}`)
		process.exit(1)
	}

	const diff = diffPair(pair)
	// Buffer 级复制，不做行尾转换
	for (const f of diff.added) {
		fs.mkdirSync(path.dirname(path.join(pair.dest, f)), { recursive: true })
		fs.copyFileSync(path.join(pair.src, f), path.join(pair.dest, f))
	}
	for (const f of diff.updated) {
		fs.copyFileSync(path.join(pair.src, f), path.join(pair.dest, f))
	}
	for (const f of diff.removed) {
		fs.rmSync(path.join(pair.dest, f))
	}

	// 自底向上清理删除后遗留的空目录
	function pruneEmptyDirs(dir) {
		for (const item of fs.readdirSync(dir, { withFileTypes: true })) {
			if (!item.isDirectory()) continue
			const abs = path.join(dir, item.name)
			pruneEmptyDirs(abs)
			if (fs.readdirSync(abs).length === 0) fs.rmdirSync(abs)
		}
	}
	if (fs.existsSync(pair.dest)) pruneEmptyDirs(pair.dest)

	const total = diff.added.length + diff.updated.length + diff.removed.length
	if (total > 0) {
		console.log(`${pair.label}：新增 ${diff.added.length}，更新 ${diff.updated.length}，删除 ${diff.removed.length}`)
		report(diff)
	} else {
		console.log(`${pair.label}：已是最新，无需写入`)
	}
}
