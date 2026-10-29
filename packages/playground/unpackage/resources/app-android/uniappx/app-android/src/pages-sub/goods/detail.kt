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
import io.dcloud.uniapp.extapi.showToast as uni_showToast
open class GenPagesSubGoodsDetail : BasePage {
    constructor(__ins: ComponentInternalInstance, __renderer: String?) : super(__ins, __renderer) {}
    companion object {
        @Suppress("UNUSED_PARAMETER", "UNUSED_VARIABLE")
        var setup: (__props: GenPagesSubGoodsDetail) -> Any? = fun(__props): Any? {
            val __ins = getCurrentInstance()!!
            val _ctx = __ins.proxy as GenPagesSubGoodsDetail
            val _cache = __ins.renderCache
            val router = useRouter()
            val route = useRoute()
            val goods = ref(null as Goods?)
            val from = ref("")
            val evtBack = ref("")
            fun gen_formatPrice_fn(p: Number): String {
                return formatPrice(p)
            }
            val formatPrice__1 = ::gen_formatPrice_fn
            fun gen_tag_fn(): String {
                return if (goods.value != null) {
                    goods.value!!.tag
                } else {
                    ""
                }
            }
            val tag = ::gen_tag_fn
            fun gen_name_fn(): String {
                return if (goods.value != null) {
                    goods.value!!.name
                } else {
                    ""
                }
            }
            val name = ::gen_name_fn
            fun gen_desc_fn(): String {
                return if (goods.value != null) {
                    goods.value!!.desc
                } else {
                    ""
                }
            }
            val desc = ::gen_desc_fn
            fun gen_price_fn(): String {
                return if (goods.value != null) {
                    formatPrice__1(goods.value!!.price)
                } else {
                    ""
                }
            }
            val price = ::gen_price_fn
            onLoad(fun(options: UTSJSONObject){
                val idStr: String? = (options["id"] ?: null) as String?
                if (idStr != null && idStr.length > 0) {
                    goods.value = goodsById(parseInt(idStr, 10))
                } else {
                    goods.value = null
                }
            }
            )
            onShow(fun(){
                val channel = useOpenerEventChannel()
                if (channel != null) {
                    channel.emit("goodsDetailOpened", _uO("id" to if (goods.value != null) {
                        goods.value!!.id
                    } else {
                        0
                    }, "name" to if (goods.value != null) {
                        goods.value!!.name
                    } else {
                        ""
                    }))
                    evtBack.value = "已回传商品信息给打开页"
                } else {
                    evtBack.value = ""
                }
                val f = route.params.get("from")
                if (f != null) {
                    from.value = f
                } else {
                    from.value = ""
                }
            }
            )
            fun gen_add_fn(): Unit {
                if (goods.value == null) {
                    return
                }
                addToCart(goods.value!!)
                uni_showToast(ShowToastOptions(title = "已加入购物车", icon = "none"))
            }
            val add = ::gen_add_fn
            fun gen_buyNow_fn(): Unit {
                if (goods.value == null) {
                    return
                }
                router.push(RawLocation__1(name = "checkout", params = Map(_uA(
                    _uA(
                        "goodsId",
                        goods.value!!.id.toString(10)
                    )
                ))))
            }
            val buyNow = ::gen_buyNow_fn
            return fun(): Any? {
                return _cE("scroll-view", _uM("class" to "container"), _uA(
                    if (unref(goods) != null) {
                        _cE("view", _uM("key" to 0, "class" to "page"), _uA(
                            _cE("view", _uM("class" to "detail-thumb"), _uA(
                                _cE("text", _uM("class" to "thumb-tag"), _tD(tag()), 1)
                            )),
                            _cE("view", _uM("class" to "goods-card"), _uA(
                                _cE("text", _uM("class" to "goods-name"), _tD(name()), 1),
                                _cE("text", _uM("class" to "goods-price"), _tD(price()), 1),
                                _cE("text", _uM("class" to "goods-desc"), _tD(desc()), 1)
                            )),
                            if (unref(from) !== "") {
                                _cE("view", _uM("key" to 0, "class" to "tip-card"), _uA(
                                    _cE("text", _uM("class" to "tip-label"), "来源（params）："),
                                    _cE("text", _uM("class" to "tip-value"), _tD(unref(from)), 1)
                                ))
                            } else {
                                _cC("v-if", true)
                            },
                            if (unref(evtBack) !== "") {
                                _cE("view", _uM("key" to 1, "class" to "tip-card"), _uA(
                                    _cE("text", _uM("class" to "tip-label"), "EventsPlugin："),
                                    _cE("text", _uM("class" to "tip-value"), _tD(unref(evtBack)), 1)
                                ))
                            } else {
                                _cC("v-if", true)
                            },
                            _cE("view", _uM("class" to "action-row"), _uA(
                                _cE("button", _uM("class" to "action-btn plain", "onClick" to add), "加入购物车"),
                                _cE("button", _uM("class" to "action-btn primary", "onClick" to buyNow), "立即购买")
                            ))
                        ))
                    } else {
                        _cC("v-if", true)
                    }
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
                return _uM("container" to _pS(_uM("flexGrow" to 1, "flexShrink" to 1, "flexBasis" to "0%", "backgroundColor" to "#f5f6f8")), "page" to _pS(_uM("flexDirection" to "column", "paddingTop" to "24rpx", "paddingRight" to "24rpx", "paddingBottom" to "24rpx", "paddingLeft" to "24rpx")), "detail-thumb" to _pS(_uM("height" to "400rpx", "borderTopLeftRadius" to "20rpx", "borderTopRightRadius" to "20rpx", "borderBottomRightRadius" to "20rpx", "borderBottomLeftRadius" to "20rpx", "alignItems" to "center", "justifyContent" to "center", "backgroundColor" to "#fff1ec", "marginBottom" to "20rpx")), "thumb-tag" to _pS(_uM("fontSize" to "36rpx", "color" to "#ff6034")), "goods-card" to _pS(_uM("backgroundColor" to "#ffffff", "borderTopLeftRadius" to "20rpx", "borderTopRightRadius" to "20rpx", "borderBottomRightRadius" to "20rpx", "borderBottomLeftRadius" to "20rpx", "paddingTop" to "28rpx", "paddingRight" to "28rpx", "paddingBottom" to "28rpx", "paddingLeft" to "28rpx")), "goods-name" to _pS(_uM("fontSize" to "36rpx", "fontWeight" to "bold", "color" to "#1a1a1a")), "goods-price" to _pS(_uM("fontSize" to "44rpx", "fontWeight" to "bold", "color" to "#f2270c", "marginTop" to "16rpx")), "goods-desc" to _pS(_uM("fontSize" to "26rpx", "color" to "#666666", "marginTop" to "20rpx", "lineHeight" to 1.6)), "tip-card" to _pS(_uM("flexDirection" to "row", "backgroundColor" to "#ffffff", "borderTopLeftRadius" to "20rpx", "borderTopRightRadius" to "20rpx", "borderBottomRightRadius" to "20rpx", "borderBottomLeftRadius" to "20rpx", "paddingTop" to "24rpx", "paddingRight" to "28rpx", "paddingBottom" to "24rpx", "paddingLeft" to "28rpx", "marginTop" to "20rpx")), "tip-label" to _pS(_uM("fontSize" to "24rpx", "color" to "#9a9a9a")), "tip-value" to _pS(_uM("fontSize" to "24rpx", "color" to "#ff6034")), "action-row" to _pS(_uM("flexDirection" to "row", "marginTop" to "40rpx")), "action-btn" to _uM("" to _uM("flexGrow" to 1, "flexShrink" to 1, "flexBasis" to "0%", "borderTopLeftRadius" to "44rpx", "borderTopRightRadius" to "44rpx", "borderBottomRightRadius" to "44rpx", "borderBottomLeftRadius" to "44rpx", "alignItems" to "center", "paddingTop" to "24rpx", "paddingRight" to 0, "paddingBottom" to "24rpx", "paddingLeft" to 0, "fontSize" to "30rpx"), ".primary" to _uM("backgroundColor" to "#ff6034", "color" to "#ffffff", "marginLeft" to "20rpx"), ".plain" to _uM("backgroundColor" to "#ffffff", "color" to "#ff6034")))
            }
        var inheritAttrs = true
        var inject: Map<String, Map<String, Any?>> = _uM()
        var emits: Map<String, Any?> = _uM()
        var props = _nP(_uM())
        var propsNeedCastKeys: UTSArray<String> = _uA()
        var components: Map<String, CreateVueComponent> = _uM()
    }
}
