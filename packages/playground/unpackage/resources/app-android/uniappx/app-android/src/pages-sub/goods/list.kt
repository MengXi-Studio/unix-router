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
open class GenPagesSubGoodsList : BasePage {
    constructor(__ins: ComponentInternalInstance, __renderer: String?) : super(__ins, __renderer) {}
    companion object {
        @Suppress("UNUSED_PARAMETER", "UNUSED_VARIABLE")
        var setup: (__props: GenPagesSubGoodsList) -> Any? = fun(__props): Any? {
            val __ins = getCurrentInstance()!!
            val _ctx = __ins.proxy as GenPagesSubGoodsList
            val _cache = __ins.renderCache
            val router = useRouter()
            val route = useRoute()
            fun gen_formatPrice_fn(p: Number): String {
                return formatPrice(p)
            }
            val formatPrice__1 = ::gen_formatPrice_fn
            val list = ref(_uA<Goods>())
            onLoad(fun(options: UTSJSONObject){
                val category: String? = (options["category"] ?: null) as String?
                if (category != null && category.length > 0) {
                    list.value = goodsByCategory(category)
                } else {
                    list.value = goodsList
                }
            }
            )
            onShow(fun(){
                val c = route.query.get("category")
                if (c != null && c.length > 0) {
                    list.value = goodsByCategory(c)
                }
            }
            )
            fun gen_goDetail_fn(id: Number): Unit {
                router.push(RawLocation__1(path = "pages-sub/goods/detail", query = Map(_uA(
                    _uA(
                        "id",
                        id.toString(10)
                    )
                ))))
            }
            val goDetail = ::gen_goDetail_fn
            fun gen_add_fn(g: Goods): Unit {
                addToCart(g)
                uni_showToast(ShowToastOptions(title = "已加入购物车", icon = "none"))
            }
            val add = ::gen_add_fn
            return fun(): Any? {
                return _cE("scroll-view", _uM("class" to "container"), _uA(
                    _cE("view", _uM("class" to "page"), _uA(
                        _cE("view", _uM("class" to "count-tip"), _uA(
                            _cE("text", _uM("class" to "count-text"), "共 " + _tD(unref(list).length) + " 件商品", 1)
                        )),
                        _cE(Fragment, null, RenderHelpers.renderList(unref(list), fun(g, i, __index, _cached): Any {
                            return _cE("view", _uM("class" to "goods-card", "key" to i, "onClick" to fun(){
                                goDetail(g.id)
                            }
                            ), _uA(
                                _cE("view", _uM("class" to "goods-thumb"), _uA(
                                    _cE("text", _uM("class" to "thumb-tag"), _tD(g.tag), 1)
                                )),
                                _cE("view", _uM("class" to "goods-info"), _uA(
                                    _cE("text", _uM("class" to "goods-name"), _tD(g.name), 1),
                                    _cE("text", _uM("class" to "goods-desc"), _tD(g.desc), 1),
                                    _cE("view", _uM("class" to "bottom-row"), _uA(
                                        _cE("text", _uM("class" to "goods-price"), _tD(formatPrice__1(g.price)), 1),
                                        _cE("view", _uM("class" to "add-btn", "onClick" to withModifiers(fun(){
                                            add(g)
                                        }
                                        , _uA(
                                            "stop"
                                        ))), _uA(
                                            _cE("text", _uM("class" to "add-btn-text"), "加入购物车")
                                        ), 8, _uA(
                                            "onClick"
                                        ))
                                    ))
                                ))
                            ), 8, _uA(
                                "onClick"
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
                return _uM("container" to _pS(_uM("flexGrow" to 1, "flexShrink" to 1, "flexBasis" to "0%", "backgroundColor" to "#f5f6f8")), "page" to _pS(_uM("flexDirection" to "column", "paddingTop" to "24rpx", "paddingRight" to "24rpx", "paddingBottom" to "24rpx", "paddingLeft" to "24rpx")), "count-tip" to _pS(_uM("marginBottom" to "20rpx")), "count-text" to _pS(_uM("fontSize" to "24rpx", "color" to "#9a9a9a")), "goods-card" to _pS(_uM("flexDirection" to "row", "backgroundColor" to "#ffffff", "borderTopLeftRadius" to "20rpx", "borderTopRightRadius" to "20rpx", "borderBottomRightRadius" to "20rpx", "borderBottomLeftRadius" to "20rpx", "paddingTop" to "24rpx", "paddingRight" to "24rpx", "paddingBottom" to "24rpx", "paddingLeft" to "24rpx", "marginBottom" to "20rpx")), "goods-thumb" to _pS(_uM("width" to "180rpx", "height" to "180rpx", "borderTopLeftRadius" to "16rpx", "borderTopRightRadius" to "16rpx", "borderBottomRightRadius" to "16rpx", "borderBottomLeftRadius" to "16rpx", "alignItems" to "center", "justifyContent" to "center", "backgroundColor" to "#fff1ec", "marginRight" to "24rpx")), "thumb-tag" to _pS(_uM("fontSize" to "24rpx", "color" to "#ff6034")), "goods-info" to _pS(_uM("flexGrow" to 1, "flexShrink" to 1, "flexBasis" to "0%", "flexDirection" to "column", "justifyContent" to "space-between")), "goods-name" to _pS(_uM("fontSize" to "30rpx", "fontWeight" to "bold", "color" to "#1a1a1a")), "goods-desc" to _pS(_uM("fontSize" to "24rpx", "color" to "#9a9a9a", "marginTop" to "8rpx")), "bottom-row" to _pS(_uM("flexDirection" to "row", "alignItems" to "center", "justifyContent" to "space-between", "marginTop" to "16rpx")), "goods-price" to _pS(_uM("fontSize" to "32rpx", "fontWeight" to "bold", "color" to "#f2270c")), "add-btn" to _pS(_uM("backgroundColor" to "#ff6034", "borderTopLeftRadius" to "28rpx", "borderTopRightRadius" to "28rpx", "borderBottomRightRadius" to "28rpx", "borderBottomLeftRadius" to "28rpx", "paddingTop" to "10rpx", "paddingRight" to "24rpx", "paddingBottom" to "10rpx", "paddingLeft" to "24rpx", "alignItems" to "center", "justifyContent" to "center")), "add-btn-text" to _pS(_uM("fontSize" to "24rpx", "color" to "#ffffff")))
            }
        var inheritAttrs = true
        var inject: Map<String, Map<String, Any?>> = _uM()
        var emits: Map<String, Any?> = _uM()
        var props = _nP(_uM())
        var propsNeedCastKeys: UTSArray<String> = _uA()
        var components: Map<String, CreateVueComponent> = _uM()
    }
}
