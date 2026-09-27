import { defineConfig } from 'vite'
import uni from '@dcloudio/vite-plugin-uni'
import { routeGen } from './uni_modules/ux-router/plugins/index.mjs'

export default defineConfig({
	plugins: [
		uni(),
		routeGen({
			verbose: true,
			pages: {
				pagesDir: 'pages',
				subPackages: [{ root: 'pages-sub', dir: 'pages-sub' }],
				entryPage: 'pages/index/index',
				tabBar: {
					color: '#7A7E83',
					selectedColor: '#007AFF',
					backgroundColor: '#FFFFFF'
				},
				dts: 'define-uni-page.d.ts'
			},
			router: {
				outputPath: 'routes.gen.uts',
				importFrom: '@/uni_modules/ux-router/utssdk/index.uts',
				dts: 'route-name.gen.d.ts'
			}
		})
	]
})
