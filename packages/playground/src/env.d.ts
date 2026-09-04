/**
 * 环境类型声明：为 uni.* 全局与 getCurrentPages 提供最小类型（供编辑器/tsc 参考）。
 * 实际运行类型以 uni-app x 官方 typings 为准。
 */

declare const uni: {
	navigateTo(options: {
		url: string
		success?: () => void
		fail?: (err: { errMsg?: string }) => void
	}): void
	redirectTo(options: {
		url: string
		success?: () => void
		fail?: (err: { errMsg?: string }) => void
	}): void
	reLaunch(options: {
		url: string
		success?: () => void
		fail?: (err: { errMsg?: string }) => void
	}): void
	switchTab(options: {
		url: string
		success?: () => void
		fail?: (err: { errMsg?: string }) => void
	}): void
	navigateBack(options?: { delta?: number; success?: () => void; fail?: (err: { errMsg?: string }) => void }): void
	setStorageSync(key: string, data: any): void
	getStorageSync(key: string): any
	removeStorageSync(key: string): void
	getAppBaseInfo(): Record<string, any>
	showToast(options: { title: string; icon?: string }): void
}

declare function getCurrentPages(): Array<{
	route: string
	options?: Record<string, string>
}>

declare module '*.uvue' {
	import { type DefineComponent } from 'vue'
	const component: DefineComponent<{}, {}, any>
	export default component
}