import type { DefaultTheme, LocaleSpecificConfig } from 'vitepress'

export const META_URL = 'https://mengxi-studio.github.io/unix-router/en/'
export const META_TITLE = 'Unix Router'
export const META_DESCRIPTION = 'Provide a routing management system for uni-app x that is similar to the style of vue-router'

export const enConfig: LocaleSpecificConfig<DefaultTheme.Config> = {
	/** 网站配置 描述 */
	description: META_DESCRIPTION,

	head: [
		['meta', { property: 'og:url', content: META_URL }],
		['meta', { property: 'og:description', content: META_DESCRIPTION }],
		['meta', { property: 'twitter:url', content: META_URL }],
		['meta', { property: 'twitter:title', content: META_TITLE }],
		['meta', { property: 'twitter:description', content: META_DESCRIPTION }]
	],

	/** 网站主题配置 */
	themeConfig: {
		/** 编辑链接 */
		editLink: {
			pattern: 'https://github.com/MengXi-Studio/unix-router/edit/main/packages/docs/:path',
			text: 'Suggest changes to this page'
		},

		/** 大纲标题 */
		outlineTitle: 'Contents of this page',

		/** 导航栏 */
		nav: [
			{ text: 'Guide', link: '/en/guide/getting-started' },
			{ text: 'Components', link: '/en/component/router-link' },
			{ text: 'API', link: '/en/api/create-router' },
			{ text: 'Changelog', link: '/en/changelog' },
			{
				text: 'Links',
				items: [
					{ text: 'Discussions', link: 'https://github.com/MengXi-Studio/unix-router/discussions' },
					{ text: 'Releases', link: 'https://github.com/MengXi-Studio/unix-router/releases' }
				]
			}
		],

		sidebar: {
			'/en/guide/': [
				{
					text: 'Getting Started',
					items: [
						{ text: 'Introduction', link: '/en/guide/introduction' },
						{ text: 'Installation', link: '/en/guide/installation' },
						{ text: 'Quick Start', link: '/en/guide/getting-started' }
					]
				},
				{
					text: 'Core Features',
					items: [
						{ text: 'Route Configuration', link: '/en/guide/route-config' },
						{ text: 'Navigation', link: '/en/guide/navigation' },
						{ text: 'Route Guards', link: '/en/guide/guards' },
						{ text: 'Route Meta', link: '/en/guide/meta' },
						{ text: 'Composables', link: '/en/guide/composables' }
					]
				},
				{
					text: 'Principles In Depth',
					items: [
						{ text: 'Navigation Flow', link: '/en/guide/navigation-flow' },
						{ text: 'Error Handling', link: '/en/guide/error-handling' }
					]
				},
				{
					text: 'Advanced',
					items: [
						{ text: 'Platform Compatibility', link: '/en/guide/compatibility' },
						{ text: 'Differences from vue-router', link: '/en/guide/differences' },
						{ text: 'Recipes', link: '/en/guide/recipes' },
						{ text: 'FAQ', link: '/en/guide/faq' }
					]
				}
			],
			'/en/api/': [
				{
					text: 'Core API',
					items: [
						{ text: 'createRouter()', link: '/en/api/create-router' },
						{ text: 'Router Instance', link: '/en/api/router-instance' },
						{ text: 'useRouter()', link: '/en/api/use-router' },
						{ text: 'useRoute()', link: '/en/api/use-route' },
						{ text: 'useLink()', link: '/en/api/use-link' }
					]
				},
				{
					text: 'Types',
					items: [
						{ text: 'RouterOptions', link: '/en/api/type-router-options' },
						{ text: 'RouteConfig', link: '/en/api/type-route-config' },
						{ text: 'RouteLocation', link: '/en/api/type-route-location' },
						{ text: 'RouteMeta', link: '/en/api/type-route-meta' },
						{ text: 'NavigationGuard', link: '/en/api/type-navigation-guard' },
						{ text: 'RouterErrorCode', link: '/en/api/type-router-error-code' }
					]
				}
			]
		}
	}
}
