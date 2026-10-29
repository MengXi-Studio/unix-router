@file:Suppress("UNCHECKED_CAST", "USELESS_CAST", "INAPPLICABLE_JVM_NAME", "UNUSED_ANONYMOUS_PARAMETER", "SENSELESS_COMPARISON", "NAME_SHADOWING", "UNNECESSARY_NOT_NULL_ASSERTION")
package uni.UNIB6A50A8
import io.dcloud.uniapp.*
import io.dcloud.uniapp.extapi.*
import io.dcloud.uniapp.framework.*
import io.dcloud.uniapp.runtime.*
import io.dcloud.uniapp.vue.*
import io.dcloud.uniapp.vue.shared.*
import io.dcloud.uts.*
import io.dcloud.uts.Map
import io.dcloud.uts.Set
import io.dcloud.uts.UTSAndroid
import kotlin.properties.Delegates
import io.dcloud.uniapp.extapi.showModal as uni_showModal
import io.dcloud.uniapp.extapi.showToast as uni_showToast
open class GenPagesSubCheckoutCheckout : BasePage {
    constructor(__ins: ComponentInternalInstance, __renderer: String?) : super(__ins, __renderer) {}
    companion object {
        @Suppress("UNUSED_PARAMETER", "UNUSED_VARIABLE")
        var setup: (__props: GenPagesSubCheckoutCheckout) -> Any? = fun(__props): Any? {
            val __ins = getCurrentInstance()!!
            val _ctx = __ins.proxy as GenPagesSubCheckoutCheckout
            val _cache = __ins.renderCache
            val router = useRouter()
            fun gen_cartTotalPrice_fn(): Number {
                return cartTotalPrice()
            }
            val cartTotalPrice__1 = ::gen_cartTotalPrice_fn
            fun gen_formatPrice_fn(p: Number): String {
                return formatPrice(p)
            }
            val formatPrice__1 = ::gen_formatPrice_fn
            val allowLeave = ref(false)
            onBeforeRouteLeave(fun(to, from): Any {
                if (allowLeave.value || cartItems.length === 0) {
                    return true
                }
                return UTSPromise<Boolean>(fun(resolve, _reject){
                    uni_showModal(ShowModalOptions(title = "提示", content = "还有商品未结算，确认离开吗？", success = fun(res){
                        resolve(res.confirm)
                    }
                    ))
                }
                )
            }
            )
            fun gen_currentTime_fn(): String {
                val now = Date()
                return now.getFullYear() + "-" + (now.getMonth() + 1) + "-" + now.getDate()
            }
            val currentTime = ::gen_currentTime_fn
            fun gen_submit_fn(): Unit {
                if (cartItems.length === 0) {
                    return
                }
                val order = Order(id = "NO" + Date.now().toString(10), items = cartItems.slice(0), total = cartTotalPrice__1(), createdAt = currentTime())
                orders.push(order)
                clearCart()
                allowLeave.value = true
                uni_showToast(ShowToastOptions(title = "下单成功", icon = "success"))
                router.replace(RawLocation__1(name = "orders"))
            }
            val submit = ::gen_submit_fn
            fun gen_goHome_fn(): Unit {
                router.push(RawLocation__1(name = "home"))
            }
            val goHome = ::gen_goHome_fn
            return fun(): Any? {
                return _cE("scroll-view", _uM("class" to "container"), _uA(
                    _cE("view", _uM("class" to "page"), _uA(
                        if (unref(cartItems).length === 0) {
                            _cE("view", _uM("key" to 0, "class" to "empty"), _uA(
                                _cE("text", _uM("class" to "empty-text"), "没有待结算的商品"),
                                _cE("button", _uM("class" to "go-btn", "onClick" to goHome), "去逛逛")
                            ))
                        } else {
                            _cC("v-if", true)
                        }
                        ,
                        if (unref(cartItems).length > 0) {
                            _cE("view", _uM("key" to 1), _uA(
                                _cE("view", _uM("class" to "address-card"), _uA(
                                    _cE("text", _uM("class" to "address-title"), "收货地址"),
                                    _cE("text", _uM("class" to "address-detail"), "张同学 138****8888 · 北京市朝阳区梦曦街 1 号")
                                )),
                                _cE("view", _uM("class" to "goods-card"), _uA(
                                    _cE(Fragment, null, RenderHelpers.renderList(unref(cartItems), fun(item, i, __index, _cached): Any {
                                        return _cE("view", _uM("class" to "item-row", "key" to i), _uA(
                                            _cE("text", _uM("class" to "item-name"), _tD(item.goods.name) + " ×" + _tD(item.count), 1),
                                            _cE("text", _uM("class" to "item-price"), _tD(formatPrice__1(item.goods.price * item.count)), 1)
                                        ))
                                    }), 128)
                                )),
                                _cE("view", _uM("class" to "total-card"), _uA(
                                    _cE("text", _uM("class" to "total-label"), "应付合计"),
                                    _cE("text", _uM("class" to "total-price"), _tD(formatPrice__1(cartTotalPrice__1())), 1)
                                )),
                                _cE("button", _uM("class" to "submit-btn", "onClick" to submit), "提交订单"),
                                _cE("view", _uM("class" to "demo-note"), _uA(
                                    _cE("text", _uM("class" to "demo-title"), "组件内离开守卫演示"),
                                    _cE("text", _uM("class" to "demo-desc"), "本页通过 onBeforeRouteLeave 注册离开守卫：未提交订单时返回会被弹窗拦截确认，提交后放行（守卫支持 Promise 异步结果）。")
                                ))
                            ))
                        } else {
                            _cC("v-if", true)
                        }
                    ))
                ))
            }
        }
        val styles: Map<String, Map<String, Map<String, Any>>> by lazy {
            _nCS(_uA(
                styles0
            ))
        }
        val styles0: Map<String, Map<String, Map<String, Any>>>
            get() {
                return _uM("container" to _pS(_uM("flexGrow" to 1, "flexShrink" to 1, "flexBasis" to "0%", "backgroundColor" to "#f5f6f8")), "page" to _pS(_uM("flexDirection" to "column", "paddingTop" to "24rpx", "paddingRight" to "24rpx", "paddingBottom" to "24rpx", "paddingLeft" to "24rpx")), "empty" to _pS(_uM("flexDirection" to "column", "alignItems" to "center", "paddingTop" to "120rpx", "paddingRight" to 0, "paddingBottom" to "120rpx", "paddingLeft" to 0)), "empty-text" to _pS(_uM("fontSize" to "28rpx", "color" to "#9a9a9a")), "go-btn" to _pS(_uM("marginTop" to "24rpx", "backgroundColor" to "#ff6034", "color" to "#ffffff", "borderTopLeftRadius" to "44rpx", "borderTopRightRadius" to "44rpx", "borderBottomRightRadius" to "44rpx", "borderBottomLeftRadius" to "44rpx", "paddingTop" to "16rpx", "paddingRight" to "48rpx", "paddingBottom" to "16rpx", "paddingLeft" to "48rpx", "fontSize" to "28rpx")), "address-card" to _pS(_uM("backgroundColor" to "#ffffff", "borderTopLeftRadius" to "20rpx", "borderTopRightRadius" to "20rpx", "borderBottomRightRadius" to "20rpx", "borderBottomLeftRadius" to "20rpx", "paddingTop" to "28rpx", "paddingRight" to "28rpx", "paddingBottom" to "28rpx", "paddingLeft" to "28rpx", "marginBottom" to "20rpx")), "address-title" to _pS(_uM("fontSize" to "30rpx", "fontWeight" to "bold", "color" to "#1a1a1a")), "address-detail" to _pS(_uM("fontSize" to "24rpx", "color" to "#666666", "marginTop" to "12rpx")), "goods-card" to _pS(_uM("backgroundColor" to "#ffffff", "borderTopLeftRadius" to "20rpx", "borderTopRightRadius" to "20rpx", "borderBottomRightRadius" to "20rpx", "borderBottomLeftRadius" to "20rpx", "paddingTop" to "8rpx", "paddingRight" to "28rpx", "paddingBottom" to "8rpx", "paddingLeft" to "28rpx")), "item-row" to _pS(_uM("flexDirection" to "row", "alignItems" to "center", "justifyContent" to "space-between", "paddingTop" to "24rpx", "paddingRight" to 0, "paddingBottom" to "24rpx", "paddingLeft" to 0, "borderBottomWidth" to "1rpx", "borderBottomColor" to "#f5f5f5")), "item-name" to _pS(_uM("fontSize" to "26rpx", "color" to "#1a1a1a", "flexGrow" to 1, "flexShrink" to 1, "flexBasis" to "0%")), "item-price" to _pS(_uM("fontSize" to "26rpx", "color" to "#f2270c")), "total-card" to _pS(_uM("flexDirection" to "row", "alignItems" to "center", "justifyContent" to "flex-end", "marginTop" to "24rpx")), "total-label" to _pS(_uM("fontSize" to "26rpx", "color" to "#666666", "marginRight" to "16rpx")), "total-price" to _pS(_uM("fontSize" to "40rpx", "fontWeight" to "bold", "color" to "#f2270c")), "submit-btn" to _pS(_uM("marginTop" to "32rpx", "backgroundColor" to "#ff6034", "color" to "#ffffff", "borderTopLeftRadius" to "44rpx", "borderTopRightRadius" to "44rpx", "borderBottomRightRadius" to "44rpx", "borderBottomLeftRadius" to "44rpx", "paddingTop" to "24rpx", "paddingRight" to 0, "paddingBottom" to "24rpx", "paddingLeft" to 0, "fontSize" to "30rpx")), "demo-note" to _pS(_uM("backgroundColor" to "#ffffff", "borderTopLeftRadius" to "20rpx", "borderTopRightRadius" to "20rpx", "borderBottomRightRadius" to "20rpx", "borderBottomLeftRadius" to "20rpx", "paddingTop" to "24rpx", "paddingRight" to "28rpx", "paddingBottom" to "24rpx", "paddingLeft" to "28rpx", "marginTop" to "40rpx")), "demo-title" to _pS(_uM("fontSize" to "26rpx", "fontWeight" to "bold", "color" to "#1a1a1a")), "demo-desc" to _pS(_uM("fontSize" to "22rpx", "color" to "#9a9a9a", "marginTop" to "12rpx", "lineHeight" to 1.6)))
            }
        var inheritAttrs = true
        var inject: Map<String, Map<String, Any?>> = _uM()
        var emits: Map<String, Any?> = _uM()
        var props = _nP(_uM())
        var propsNeedCastKeys: UTSArray<String> = _uA()
        var components: Map<String, CreateVueComponent> = _uM()
    }
}
