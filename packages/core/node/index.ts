/**
 * @meng-xi/unix-router 构建期插件族（随路由库同包分发）
 *
 * 三个独立插件，互不影响，可单独或组合注册：
 *   routeGen  — 页面文件宏/块 → pages.json + 路由数组（全量流水线，默认选择）
 *   pagesGen  — 页面文件宏/块 → 仅 pages.json（路由表自行维护时使用）
 *   routesGen — pages.json → 仅路由数组（配合 routes.ext.uts 扩展声明补 name/meta/beforeEnter）
 */
export { routeGenUnplugin, routeGen } from './route-gen'
export { pagesGenUnplugin, pagesGen } from './pages-gen'
export { routesGenUnplugin, routesGen } from './routes-gen'

// 主插件默认导出（保持既有导入习惯：import routeGen from '@meng-xi/unix-router/vite-plugin'）
export { default } from './route-gen'
export type { RouteGenOptions, RouteGenPagesOptions, RouteGenRouterOptions, PagesGenOptions, RoutesGenOptions, RoutesGenRouterOptions, SubPackageOptions, TabBarChrome } from './shared/common/options'
