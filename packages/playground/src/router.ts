/**
 * 路由器实例
 */
import { createRouter, type Router } from '@meng-xi/unix-router'
import { routes, authStore } from './router.config'

/**
 * 创建并配置路由器：
 * - 注册全局前置守卫：requireAuth 页面在未登录时重定向到登录页
 * - 注册 afterEach：打印每次导航结果便于调试
 */
export const router: Router = createRouter({
	routes,
	strict: true
})

// 全局前置守卫：未登录访问 requireAuth 页面时重定向到登录页
router.beforeEach((to, _from) => {
	if (to.meta && to.meta.requireAuth && !authStore.loggedIn) {
		return {
			name: 'login',
			query: new Map([['redirect', to.fullPath]])
		}
	}
	return true
})

// 全局后置守卫：打印导航日志
router.afterEach((to, _from) => {
	console.log(`[unix-router][afterEach] ${to.fullPath}`)
})

// 错误处理：重复导航等信息以警告形式输出，便于调试
router.onError(error => {
	console.warn(`[unix-router][onError] ${error.message}`)
})
