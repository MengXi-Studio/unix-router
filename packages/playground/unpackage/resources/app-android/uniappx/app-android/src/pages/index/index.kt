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
open class GenPagesIndexIndex : BasePage {
    constructor(__ins: ComponentInternalInstance, __renderer: String?) : super(__ins, __renderer) {}
    companion object {
        @Suppress("UNUSED_PARAMETER", "UNUSED_VARIABLE")
        var setup: (__props: GenPagesIndexIndex) -> Any? = fun(__props): Any? {
            val __ins = getCurrentInstance()!!
            val _ctx = __ins.proxy as GenPagesIndexIndex
            val _cache = __ins.renderCache
            val router = useRouter()
            val route = useRoute()
            fun gen_formatPrice_fn(p: Number): String {
                return formatPrice(p)
            }
            val formatPrice__1 = ::gen_formatPrice_fn
            val recommend: UTSArray<Goods> = goodsList.slice(0, 4)
            val evtMsg = ref("")
            fun gen_goCategory_fn(key: String): Unit {
                router.push(RawLocation__1(path = "pages-sub/goods/list", query = Map(_uA(
                    _uA(
                        "category",
                        key
                    )
                ))))
            }
            val goCategory = ::gen_goCategory_fn
            fun gen_goDetail_fn(id: Number): Unit {
                val events: Map<String, (data: Any) -> Any> = Map<String, (data: Any) -> Any>()
                events.set("goodsDetailOpened", fun(data: Any): Any {
                    val d = data as UTSJSONObject
                    evtMsg.value = "详情页回传: " + (d["id"] as Number).toString(10) + " - " + (d["name"] as String)
                    return "" as Any
                }
                )
                router.push(RawLocation__1(path = "pages-sub/goods/detail", query = Map(_uA(
                    _uA(
                        "id",
                        id.toString(10)
                    )
                )), events = events))
            }
            val goDetail = ::gen_goDetail_fn
            return fun(): Any? {
                return _cE("scroll-view", _uM("class" to "container"), _uA(
                    _cE("view", _uM("class" to "page"), _uA(
                        _cE("view", _uM("class" to "banner"), _uA(
                            _cE("text", _uM("class" to "banner-title"), "梦曦商城"),
                            _cE("text", _uM("class" to "banner-sub"), "@meng-xi/unix-router 电商示例")
                        )),
                        _cE("text", _uM("class" to "section-title"), "商品分类"),
                        _cE("scroll-view", _uM("class" to "cat-scroll", "scroll-x" to "true", "show-scrollbar" to "false"), _uA(
                            _cE("view", _uM("class" to "cat-row"), _uA(
                                _cE(Fragment, null, RenderHelpers.renderList(unref(categories), fun(c, i, __index, _cached): Any {
                                    return _cE("view", _uM("class" to "cat-chip", "key" to i, "onClick" to fun(){
                                        goCategory(c.key)
                                    }
                                    ), _uA(
                                        _cE("text", _uM("class" to "cat-name"), _tD(c.name), 1)
                                    ), 8, _uA(
                                        "onClick"
                                    ))
                                }
                                ), 128)
                            ))
                        )),
                        _cE("text", _uM("class" to "section-title"), "推荐商品"),
                        _cE("view", _uM("class" to "goods-grid"), _uA(
                            _cE(Fragment, null, RenderHelpers.renderList(unref(recommend), fun(g, i, __index, _cached): Any {
                                return _cE("view", _uM("class" to "goods-card", "key" to i, "onClick" to fun(){
                                    goDetail(g.id)
                                }
                                ), _uA(
                                    _cE("view", _uM("class" to "goods-thumb"), _uA(
                                        _cE("text", _uM("class" to "thumb-tag"), _tD(g.tag), 1)
                                    )),
                                    _cE("text", _uM("class" to "goods-name"), _tD(g.name), 1),
                                    _cE("text", _uM("class" to "goods-price"), _tD(formatPrice__1(g.price)), 1)
                                ), 8, _uA(
                                    "onClick"
                                ))
                            }
                            ), 128)
                        )),
                        _cV(unref(GenUniModulesUxRouterUtssdkComponentsRouterLinkClass), _uM("to" to "pages/cart/cart", "class" to "cart-link"), _uM("default" to withSlotCtx(fun(): UTSArray<Any> {
                            return _uA(
                                _cE("text", _uM("class" to "cart-link-text"), "去购物车 →")
                            )
                        }
                        ), "_" to 1)),
                        if (unref(evtMsg).length > 0) {
                            _cE("view", _uM("key" to 0, "class" to "evt-box"), _uA(
                                _cE("text", _uM("class" to "evt-text"), _tD(unref(evtMsg)), 1)
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
                return _uM("container" to _pS(_uM("flexGrow" to 1, "flexShrink" to 1, "flexBasis" to "0%", "backgroundColor" to "#f5f6f8")), "page" to _pS(_uM("flexDirection" to "column", "paddingTop" to "24rpx", "paddingRight" to "24rpx", "paddingBottom" to "24rpx", "paddingLeft" to "24rpx")), "banner" to _pS(_uM("flexDirection" to "column", "alignItems" to "center", "backgroundColor" to "#ff6034", "borderTopLeftRadius" to "20rpx", "borderTopRightRadius" to "20rpx", "borderBottomRightRadius" to "20rpx", "borderBottomLeftRadius" to "20rpx", "paddingTop" to "56rpx", "paddingRight" to 0, "paddingBottom" to "56rpx", "paddingLeft" to 0, "marginBottom" to "32rpx")), "banner-title" to _pS(_uM("fontSize" to "44rpx", "fontWeight" to "bold", "color" to "#ffffff")), "banner-sub" to _pS(_uM("fontSize" to "24rpx", "color" to "#ffe3d9", "marginTop" to "12rpx")), "section-title" to _pS(_uM("fontSize" to "32rpx", "fontWeight" to "bold", "color" to "#1a1a1a", "marginBottom" to "20rpx")), "cat-scroll" to _pS(_uM("width" to "100%", "marginBottom" to "32rpx")), "cat-row" to _pS(_uM("flexDirection" to "row")), "cat-chip" to _pS(_uM("backgroundColor" to "#ffffff", "borderTopLeftRadius" to "32rpx", "borderTopRightRadius" to "32rpx", "borderBottomRightRadius" to "32rpx", "borderBottomLeftRadius" to "32rpx", "paddingTop" to "14rpx", "paddingRight" to "32rpx", "paddingBottom" to "14rpx", "paddingLeft" to "32rpx", "marginRight" to "20rpx")), "cat-name" to _pS(_uM("fontSize" to "26rpx", "color" to "#666666")), "goods-grid" to _pS(_uM("flexDirection" to "row", "flexWrap" to "wrap", "justifyContent" to "space-between")), "goods-card" to _pS(_uM("width" to "48%", "backgroundColor" to "#ffffff", "borderTopLeftRadius" to "20rpx", "borderTopRightRadius" to "20rpx", "borderBottomRightRadius" to "20rpx", "borderBottomLeftRadius" to "20rpx", "marginBottom" to "20rpx", "paddingTop" to "20rpx", "paddingRight" to "20rpx", "paddingBottom" to "20rpx", "paddingLeft" to "20rpx")), "goods-thumb" to _pS(_uM("height" to "200rpx", "borderTopLeftRadius" to "16rpx", "borderTopRightRadius" to "16rpx", "borderBottomRightRadius" to "16rpx", "borderBottomLeftRadius" to "16rpx", "alignItems" to "center", "justifyContent" to "center", "backgroundColor" to "#fff1ec", "marginBottom" to "16rpx")), "thumb-tag" to _pS(_uM("fontSize" to "24rpx", "color" to "#ff6034")), "goods-name" to _pS(_uM("fontSize" to "26rpx", "color" to "#1a1a1a", "marginBottom" to "8rpx")), "goods-price" to _pS(_uM("fontSize" to "30rpx", "fontWeight" to "bold", "color" to "#f2270c")), "cart-link" to _pS(_uM("marginTop" to "12rpx", "paddingTop" to "24rpx", "paddingRight" to "24rpx", "paddingBottom" to "24rpx", "paddingLeft" to "24rpx", "alignItems" to "center", "backgroundColor" to "#ffffff", "borderTopLeftRadius" to "44rpx", "borderTopRightRadius" to "44rpx", "borderBottomRightRadius" to "44rpx", "borderBottomLeftRadius" to "44rpx")), "cart-link-text" to _pS(_uM("fontSize" to "28rpx", "color" to "#ff6034")), "evt-box" to _pS(_uM("marginTop" to "20rpx", "paddingTop" to "24rpx", "paddingRight" to "24rpx", "paddingBottom" to "24rpx", "paddingLeft" to "24rpx", "alignItems" to "center", "backgroundColor" to "#e8f8ec", "borderTopLeftRadius" to "20rpx", "borderTopRightRadius" to "20rpx", "borderBottomRightRadius" to "20rpx", "borderBottomLeftRadius" to "20rpx")), "evt-text" to _pS(_uM("fontSize" to "26rpx", "color" to "#07c160")))
            }
        var inheritAttrs = true
        var inject: Map<String, Map<String, Any?>> = _uM()
        var emits: Map<String, Any?> = _uM()
        var props = _nP(_uM())
        var propsNeedCastKeys: UTSArray<String> = _uA()
        var components: Map<String, CreateVueComponent> = _uM()
    }
}
