import { defineConfig } from 'vitepress'

/** 网站部署基础路径（CI 可经 DOCS_BASE 注入，本地默认 /unix-router/） */
const base = process.env.DOCS_BASE || '/unix-router/'

/** 将根路径资源拼接到当前 base 下，避免 favicon 等资源 404 */
const asset = (p: string) => base + p.replace(/^\//, '')

export const sharedConfig = defineConfig({
	/** 网站标题 */
	title: 'Unix Router',

	lastUpdated: true,

	markdown: {
		/** 代码块高亮主题 */
		theme: {
			dark: 'one-dark-pro',
			light: 'github-light'
		},
		/** UTS 是 TypeScript 的超集，按 TypeScript 高亮 */
		languageAlias: { uts: 'typescript' }
	},

	/** 网站头标签 */
	head: [
		['link', { rel: 'icon', type: 'image/svg+xml', href: asset('logo.svg') }],
		['meta', { property: 'og:type', content: 'website' }],
		['meta', { property: 'og:title', content: 'Unix Router' }],
		['meta', { property: 'twitter:title', content: 'Unix Router' }],
		['meta', { property: 'twitter:card', content: 'summary_large_image' }],
		['meta', { property: 'twitter:description', content: '为 uni-app x 提供类似 vue-router 风格的路由管理系统' }]
	],

	/** 网站主题配置 */
	themeConfig: {
		/** 主题 logo（VitePress 自动拼 base） */
		logo: '/logo.svg',

		/** 本地搜索 */
		search: {
			provider: 'local',
			options: {
				translations: { button: { buttonText: '搜索文档', buttonAriaLabel: '搜索文档' } }
			}
		},

		/** 社交链接 */
		socialLinks: [
			{ icon: 'github', link: 'https://github.com/MengXi-Studio/unix-router' },
			{ icon: 'npm', link: 'https://www.npmjs.com/package/@meng-xi/unix-router' }
		],

		/** 页脚 */
		footer: {
			copyright: 'Copyright © 2026-present 梦曦工作室',
			message: 'Released under the MIT License.'
		}
	}
})