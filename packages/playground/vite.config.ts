// 路由生成插件接入（vite 适配器）
// 注意：HBuilderX 下项目根存在 vite.config.ts 时会【整体替换】内置配置（含 plugins:[uni()]），
// 因此必须自行引入 uni() 插件；@dcloudio/vite-plugin-uni 不在项目依赖中，从 HBuilderX 内置插件目录解析。
import { createRequire } from 'node:module'
// 构建期插件从 uni_modules 内的同步产物引入（scripts/sync-uni-modules.mjs 镜像自 packages/core/dist/plugin），playground 自包含、不依赖 npm 包
import { routeGen } from './uni_modules/ux-router/plugins/index.mjs'

const hbxRequire = createRequire('E:/HBuilderX/plugins/uniapp-cli-vite/package.json')
const uniMod = hbxRequire('@dcloudio/vite-plugin-uni')
const uni = uniMod.default ?? uniMod

export default {
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
}
