/**
 * 插件级常量（对齐 src 的 constants 惯例：魔法串收敛单点，防多处硬编码漂移）
 */

/** unplugin 插件名（vite 调试面板显示 [plugin:xxx]） */
export const PLUGIN_NAME = 'unix-router-route-gen'

/** 日志前缀 */
export const LOG_PREFIX = '[unix-router:route-gen]'

/** <route-config> 自定义块的源码标识（resolveId 匹配用） */
export const ROUTE_CONFIG_BLOCK_ID = 'route-config'

/** 虚拟模块前缀：<route-config> 块的编译请求被重定向到此 */
export const BLOCK_VIRTUAL_PREFIX = '\0unix-router-route-config:'

/** <route-config> 块的编译期模块请求是否应被拦截（宽松匹配自定义块查询） */
export function isRouteConfigBlockRequest(id: string): boolean {
	return id.includes(ROUTE_CONFIG_BLOCK_ID)
}
