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
open class GenPagesSubOrdersOrders : BasePage {
    constructor(__ins: ComponentInternalInstance, __renderer: String?) : super(__ins, __renderer) {}
    companion object {
        @Suppress("UNUSED_PARAMETER", "UNUSED_VARIABLE")
        var setup: (__props: GenPagesSubOrdersOrders) -> Any? = fun(__props): Any? {
            val __ins = getCurrentInstance()!!
            val _ctx = __ins.proxy as GenPagesSubOrdersOrders
            val _cache = __ins.renderCache
            val router = useRouter()
            fun gen_formatPrice_fn(p: Number): String {
                return formatPrice(p)
            }
            val formatPrice__1 = ::gen_formatPrice_fn
            fun gen_itemSummary_fn(o: Order): String {
                var buf = ""
                run {
                    var i: Number = 0
                    while(i < o.items.length){
                        if (i > 0) {
                            buf += "、"
                        }
                        buf += o.items[i].goods.name + " ×" + o.items[i].count
                        i++
                    }
                }
                return buf
            }
            val itemSummary = ::gen_itemSummary_fn
            fun gen_goHome_fn(): Unit {
                router.push(RawLocation__1(name = "home"))
            }
            val goHome = ::gen_goHome_fn
            return fun(): Any? {
                return _cE("scroll-view", _uM("class" to "container"), _uA(
                    _cE("view", _uM("class" to "page"), _uA(
                        if (unref(orders).length === 0) {
                            _cE("view", _uM("key" to 0, "class" to "empty"), _uA(
                                _cE("text", _uM("class" to "empty-text"), "暂无订单"),
                                _cE("button", _uM("class" to "go-btn", "onClick" to goHome), "去逛逛")
                            ))
                        } else {
                            _cC("v-if", true)
                        }
                        ,
                        _cE(Fragment, null, RenderHelpers.renderList(unref(orders), fun(o, i, __index, _cached): Any {
                            return _cE("view", _uM("class" to "order-card", "key" to i), _uA(
                                _cE("view", _uM("class" to "order-head"), _uA(
                                    _cE("text", _uM("class" to "order-no"), _tD(o.id), 1),
                                    _cE("text", _uM("class" to "order-status"), "已完成")
                                )),
                                _cE("text", _uM("class" to "order-items"), _tD(itemSummary(o)), 1),
                                _cE("view", _uM("class" to "order-foot"), _uA(
                                    _cE("text", _uM("class" to "order-date"), _tD(o.createdAt), 1),
                                    _cE("text", _uM("class" to "order-total"), _tD(formatPrice__1(o.total)), 1)
                                ))
                            ))
                        }
                        ), 128)
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
                return _uM("container" to _pS(_uM("flexGrow" to 1, "flexShrink" to 1, "flexBasis" to "0%", "backgroundColor" to "#f5f6f8")), "page" to _pS(_uM("flexDirection" to "column", "paddingTop" to "24rpx", "paddingRight" to "24rpx", "paddingBottom" to "24rpx", "paddingLeft" to "24rpx")), "empty" to _pS(_uM("flexDirection" to "column", "alignItems" to "center", "paddingTop" to "120rpx", "paddingRight" to 0, "paddingBottom" to "120rpx", "paddingLeft" to 0)), "empty-text" to _pS(_uM("fontSize" to "28rpx", "color" to "#9a9a9a")), "go-btn" to _pS(_uM("marginTop" to "24rpx", "backgroundColor" to "#ff6034", "color" to "#ffffff", "borderTopLeftRadius" to "44rpx", "borderTopRightRadius" to "44rpx", "borderBottomRightRadius" to "44rpx", "borderBottomLeftRadius" to "44rpx", "paddingTop" to "16rpx", "paddingRight" to "48rpx", "paddingBottom" to "16rpx", "paddingLeft" to "48rpx", "fontSize" to "28rpx")), "order-card" to _pS(_uM("backgroundColor" to "#ffffff", "borderTopLeftRadius" to "20rpx", "borderTopRightRadius" to "20rpx", "borderBottomRightRadius" to "20rpx", "borderBottomLeftRadius" to "20rpx", "paddingTop" to "28rpx", "paddingRight" to "28rpx", "paddingBottom" to "28rpx", "paddingLeft" to "28rpx", "marginBottom" to "20rpx")), "order-head" to _pS(_uM("flexDirection" to "row", "alignItems" to "center", "justifyContent" to "space-between")), "order-no" to _pS(_uM("fontSize" to "28rpx", "fontWeight" to "bold", "color" to "#1a1a1a")), "order-status" to _pS(_uM("fontSize" to "24rpx", "color" to "#07c160")), "order-items" to _pS(_uM("fontSize" to "24rpx", "color" to "#666666", "marginTop" to "16rpx")), "order-foot" to _pS(_uM("flexDirection" to "row", "alignItems" to "center", "justifyContent" to "space-between", "marginTop" to "16rpx")), "order-date" to _pS(_uM("fontSize" to "22rpx", "color" to "#9a9a9a")), "order-total" to _pS(_uM("fontSize" to "32rpx", "fontWeight" to "bold", "color" to "#f2270c")))
            }
        var inheritAttrs = true
        var inject: Map<String, Map<String, Any?>> = _uM()
        var emits: Map<String, Any?> = _uM()
        var props = _nP(_uM())
        var propsNeedCastKeys: UTSArray<String> = _uA()
        var components: Map<String, CreateVueComponent> = _uM()
    }
}
