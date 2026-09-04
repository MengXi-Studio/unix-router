/**
 * uni-app x 构建配置
 *
 * 提示：uni-app x 项目的编译主要由 HBuilderX 驱动；本文件为基于
 * @dcloudio/vite-plugin-uni 的 CLI 方式构建预留（H5 / 微信小程序）。
 */
import { defineConfig } from 'vite'
import uni from '@dcloudio/vite-plugin-uni'

export default defineConfig({
	plugins: [uni()],
	optimizeDeps: {
		exclude: ['@meng-xi/unix-router']
	},
	build: {
		target: 'es2018'
	}
})
