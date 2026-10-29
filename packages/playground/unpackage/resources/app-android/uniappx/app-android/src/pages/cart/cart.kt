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
open class GenPagesCartCart : BasePage {
    constructor(__ins: ComponentInternalInstance, __renderer: String?) : super(__ins, __renderer) {}
    companion object {
        @Suppress("UNUSED_PARAMETER", "UNUSED_VARIABLE")
        var setup: (__props: GenPagesCartCart) -> Any? = fun(__props): Any? {
            val __ins = getCurrentInstance()!!
            val _ctx = __ins.proxy as GenPagesCartCart
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
            fun gen_plus_fn(id: Number): Unit {
                changeCartCount(id, 1)
            }
            val plus = ::gen_plus_fn
            fun gen_minus_fn(id: Number): Unit {
                changeCartCount(id, -1)
            }
            val minus = ::gen_minus_fn
            fun gen_checkout_fn(): Unit {
                router.push(RawLocation__1(name = "checkout"))
            }
            val checkout = ::gen_checkout_fn
            fun gen_goHome_fn(): Unit {
                router.push(RawLocation__1(name = "home"))
            }
            val goHome = ::gen_goHome_fn
            return fun(): Any? {
                return _cE("scroll-view", _uM("class" to "container"), _uA(
                    _cE("view", _uM("class" to "page"), _uA(
                        if (unref(cartItems).length === 0) {
                            _cE("view", _uM("key" to 0, "class" to "empty"), _uA(
                                _cE("text", _uM("class" to "empty-text"), "购物车还是空的"),
                                _cE("button", _uM("class" to "go-btn", "onClick" to goHome), "去逛逛")
                            ))
                        } else {
                            _cC("v-if", true)
                        }
                        ,
                        _cE(Fragment, null, RenderHelpers.renderList(unref(cartItems), fun(item, i, __index, _cached): Any {
                            return _cE("view", _uM("class" to "cart-item", "key" to i), _uA(
                                _cE("view", _uM("class" to "item-thumb"), _uA(
                                    _cE("text", _uM("class" to "thumb-tag"), _tD(item.goods.tag), 1)
                                )),
                                _cE("view", _uM("class" to "item-info"), _uA(
                                    _cE("text", _uM("class" to "item-name"), _tD(item.goods.name), 1),
                                    _cE("text", _uM("class" to "item-price"), _tD(formatPrice__1(item.goods.price)), 1),
                                    _cE("view", _uM("class" to "stepper"), _uA(
                                        _cE("view", _uM("class" to "step-btn", "onClick" to fun(){
                                            minus(item.goods.id)
                                        }
                                        ), _uA(
                                            _cE("text", _uM("class" to "step-btn-text"), "−")
                                        ), 8, _uA(
                                            "onClick"
                                        )),
                                        _cE("text", _uM("class" to "step-num"), _tD(item.count), 1),
                                        _cE("view", _uM("class" to "step-btn", "onClick" to fun(){
                                            plus(item.goods.id)
                                        }
                                        ), _uA(
                                            _cE("text", _uM("class" to "step-btn-text"), "＋")
                                        ), 8, _uA(
                                            "onClick"
                                        ))
                                    ))
                                ))
                            ))
                        }
                        ), 128),
                        if (unref(cartItems).length > 0) {
                            _cE("view", _uM("key" to 1, "class" to "bottom-bar"), _uA(
                                _cE("view", _uM("class" to "total-box"), _uA(
                                    _cE("text", _uM("class" to "total-label"), "合计："),
                                    _cE("text", _uM("class" to "total-price"), _tD(formatPrice__1(cartTotalPrice__1())), 1)
                                )),
                                _cE("button", _uM("class" to "checkout-btn", "onClick" to checkout), "去结算")
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
                return _uM("container" to _pS(_uM("flexGrow" to 1, "flexShrink" to 1, "flexBasis" to "0%", "backgroundColor" to "#f5f6f8")), "page" to _pS(_uM("flexDirection" to "column", "paddingTop" to "24rpx", "paddingRight" to "24rpx", "paddingBottom" to "24rpx", "paddingLeft" to "24rpx")), "empty" to _pS(_uM("flexDirection" to "column", "alignItems" to "center", "paddingTop" to "120rpx", "paddingRight" to 0, "paddingBottom" to "120rpx", "paddingLeft" to 0)), "empty-text" to _pS(_uM("fontSize" to "28rpx", "color" to "#9a9a9a")), "go-btn" to _pS(_uM("marginTop" to "24rpx", "backgroundColor" to "#ff6034", "color" to "#ffffff", "borderTopLeftRadius" to "44rpx", "borderTopRightRadius" to "44rpx", "borderBottomRightRadius" to "44rpx", "borderBottomLeftRadius" to "44rpx", "paddingTop" to "16rpx", "paddingRight" to "48rpx", "paddingBottom" to "16rpx", "paddingLeft" to "48rpx", "fontSize" to "28rpx")), "cart-item" to _pS(_uM("flexDirection" to "row", "backgroundColor" to "#ffffff", "borderTopLeftRadius" to "20rpx", "borderTopRightRadius" to "20rpx", "borderBottomRightRadius" to "20rpx", "borderBottomLeftRadius" to "20rpx", "paddingTop" to "24rpx", "paddingRight" to "24rpx", "paddingBottom" to "24rpx", "paddingLeft" to "24rpx", "marginBottom" to "20rpx")), "item-thumb" to _pS(_uM("width" to "150rpx", "height" to "150rpx", "borderTopLeftRadius" to "16rpx", "borderTopRightRadius" to "16rpx", "borderBottomRightRadius" to "16rpx", "borderBottomLeftRadius" to "16rpx", "alignItems" to "center", "justifyContent" to "center", "backgroundColor" to "#fff1ec", "marginRight" to "24rpx")), "thumb-tag" to _pS(_uM("fontSize" to "22rpx", "color" to "#ff6034")), "item-info" to _pS(_uM("flexGrow" to 1, "flexShrink" to 1, "flexBasis" to "0%", "flexDirection" to "column")), "item-name" to _pS(_uM("fontSize" to "28rpx", "color" to "#1a1a1a")), "item-price" to _pS(_uM("fontSize" to "30rpx", "fontWeight" to "bold", "color" to "#f2270c", "marginTop" to "8rpx")), "stepper" to _pS(_uM("flexDirection" to "row", "alignItems" to "center", "alignSelf" to "flex-end", "marginTop" to "12rpx")), "step-btn" to _pS(_uM("width" to "52rpx", "height" to "52rpx", "alignItems" to "center", "justifyContent" to "center", "backgroundColor" to "#fff1ec", "borderTopLeftRadius" to "26rpx", "borderTopRightRadius" to "26rpx", "borderBottomRightRadius" to "26rpx", "borderBottomLeftRadius" to "26rpx")), "step-btn-text" to _pS(_uM("fontSize" to "30rpx", "color" to "#ff6034")), "step-num" to _pS(_uM("paddingTop" to 0, "paddingRight" to "28rpx", "paddingBottom" to 0, "paddingLeft" to "28rpx", "fontSize" to "28rpx", "color" to "#1a1a1a")), "bottom-bar" to _pS(_uM("flexDirection" to "row", "alignItems" to "center", "justifyContent" to "space-between", "marginTop" to "12rpx")), "total-box" to _pS(_uM("flexDirection" to "row", "alignItems" to "flex-start")), "total-label" to _pS(_uM("fontSize" to "26rpx", "color" to "#666666")), "total-price" to _pS(_uM("fontSize" to "36rpx", "fontWeight" to "bold", "color" to "#f2270c")), "checkout-btn" to _pS(_uM("backgroundColor" to "#ff6034", "color" to "#ffffff", "borderTopLeftRadius" to "44rpx", "borderTopRightRadius" to "44rpx", "borderBottomRightRadius" to "44rpx", "borderBottomLeftRadius" to "44rpx", "paddingTop" to "20rpx", "paddingRight" to "56rpx", "paddingBottom" to "20rpx", "paddingLeft" to "56rpx", "fontSize" to "28rpx")))
            }
        var inheritAttrs = true
        var inject: Map<String, Map<String, Any?>> = _uM()
        var emits: Map<String, Any?> = _uM()
        var props = _nP(_uM())
        var propsNeedCastKeys: UTSArray<String> = _uA()
        var components: Map<String, CreateVueComponent> = _uM()
    }
}
