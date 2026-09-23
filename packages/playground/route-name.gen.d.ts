/**
 * RouteNameMap 字面量类型增强（由 @meng-xi/unix-router/vite-plugin 生成）
 * 供 WEB 端 RouteName 类型推导；原生端 UTS 不支持 keyof 组合，仍为 string。
 */
import '@/uni_modules/ux-router/utssdk/index.uts'

declare module '@/uni_modules/ux-router/utssdk/index.uts' {
	interface RouteNameMap {
		/** 设置 */
		setting: 'pages-sub/setting/setting'
		/** 梦曦商城 */
		home: 'pages/index/index'
		/** 购物车 */
		cart: 'pages/cart/cart'
		/** 分类 */
		category: 'pages/category/category'
		/** 确认订单 */
		checkout: 'pages/checkout/checkout'
		/** 商品详情 */
		goods-detail: 'pages/goods/detail'
		/** 商品列表 */
		goods-list: 'pages/goods/list'
		/** pages/login/login */
		login: 'pages/login/login'
		/** 我的 */
		mine: 'pages/mine/mine'
		/** 我的订单 */
		orders: 'pages/orders/orders'
	}
}
