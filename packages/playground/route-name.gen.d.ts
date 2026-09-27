/**
 * RouteNameMap 字面量类型增强（由 @meng-xi/unix-router/vite-plugin 生成）
 * 供 WEB 端 RouteName 类型推导；原生端 UTS 不支持 keyof 组合，仍为 string。
 */
import '@/uni_modules/ux-router/utssdk/index.uts'

declare module '@/uni_modules/ux-router/utssdk/index.uts' {
	interface RouteNameMap {
		/** 确认订单 */
		checkout: 'pages-sub/checkout/checkout'
		/** 路由能力演示 */
		demo: 'pages-sub/demo/demo'
		/** 商品详情 */
		'goods-detail': 'pages-sub/goods/detail'
		/** 商品列表 */
		'goods-list': 'pages-sub/goods/list'
		/** 我的订单 */
		orders: 'pages-sub/orders/orders'
		/** 设置 */
		setting: 'pages-sub/setting/setting'
		/** 个人资料 */
		profile: 'pages-sub/user/profile'
		/** 梦曦商城 */
		home: 'pages/index/index'
		/** 购物车 */
		cart: 'pages/cart/cart'
		/** 分类 */
		category: 'pages/category/category'
		/** pages/login/login */
		login: 'pages/login/login'
		/** 我的 */
		mine: 'pages/mine/mine'
	}
}
