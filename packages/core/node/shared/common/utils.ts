/**
 * 插件共享的文件/调度工具（routeGen / pagesGen / routesGen 复用）。
 *
 * @packageDocumentation
 */
import fs from 'node:fs'

/**
 * 内容变化才写盘，避免 dev 下无谓的产物变更触发 uni 重载。
 *
 * @param abs - 目标文件绝对路径
 * @param content - 待写入内容
 * @returns 实际写盘时 `true`；文件已存在且内容一致时 `false`
 */
export function writeFileIfChanged(abs: string, content: string): boolean {
	try {
		if (fs.readFileSync(abs, 'utf8') === content) return false
	} catch {
		// 文件不存在 → 直接写入
	}

	fs.writeFileSync(abs, content, 'utf8')
	return true
}

/**
 * 读取文件内容，不存在时返回 `null`。
 *
 * @param abs - 文件绝对路径
 * @returns 文件内容；不存在或不可读时 `null`
 */
export function readIfExists(abs: string): string | null {
	try {
		return fs.readFileSync(abs, 'utf8')
	} catch {
		return null
	}
}

/**
 * watch 去抖 + 串行队列。
 *
 * @remarks
 * 连续触发只执行最后一次；执行期间的新触发在本轮结束后合并补跑一次，
 * 保证监听回调最终一致而非丢失。`run` 抛异常经 `onError` 上报后继续消费队列。
 *
 * @param run - 实际执行的重建回调
 * @param onError - `run` 抛异常时的上报通道
 * @returns 调度函数，`reason` 透传给触发时的 `run`
 */
export function createSerialDebounced(run: (reason: string) => void, onError: (e: unknown) => void): (reason: string) => void {
	let running = false
	let pending = false
	const schedule = (reason: string): void => {
		if (running) {
			pending = true
			return
		}

		running = true
		setTimeout(() => {
			try {
				run(reason)
			} catch (e) {
				onError(e)
			}

			running = false
			if (pending) {
				pending = false
				schedule(reason)
			}
		}, 200)
	}
	return schedule
}
