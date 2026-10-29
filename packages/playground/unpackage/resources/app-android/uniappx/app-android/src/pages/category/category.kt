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
open class GenPagesCategoryCategory : BasePage {
    constructor(__ins: ComponentInternalInstance, __renderer: String?) : super(__ins, __renderer) {}
    companion object {
        @Suppress("UNUSED_PARAMETER", "UNUSED_VARIABLE")
        var setup: (__props: GenPagesCategoryCategory) -> Any? = fun(__props): Any? {
            val __ins = getCurrentInstance()!!
            val _ctx = __ins.proxy as GenPagesCategoryCategory
            val _cache = __ins.renderCache
            val router = useRouter()
            val allGoods: UTSArray<Goods> = goodsList
            fun gen_formatPrice_fn(p: Number): String {
                return formatPrice(p)
            }
            val formatPrice__1 = ::gen_formatPrice_fn
            fun gen_countByKey_fn(key: String): Number {
                return goodsByCategory(key).length
            }
            val countByKey = ::gen_countByKey_fn
            fun gen_goList_fn(key: String): Unit {
                router.push(RawLocation__1(path = "pages-sub/goods/list", query = Map(_uA(
                    _uA(
                        "category",
                        key
                    )
                ))))
            }
            val goList = ::gen_goList_fn
            fun gen_goDetail_fn(id: Number): Unit {
                router.push(RawLocation__1(path = "pages-sub/goods/detail", query = Map(_uA(
                    _uA(
                        "id",
                        id.toString(10)
                    )
                ))))
            }
            val goDetail = ::gen_goDetail_fn
            return fun(): Any? {
                return _cE("scroll-view", _uM("class" to "container"), _uA(
                    _cE("view", _uM("class" to "page"), _uA(
                        _cE("text", _uM("class" to "section-title"), "全部分类"),
                        _cE("view", _uM("class" to "cat-grid"), _uA(
                            _cE(Fragment, null, RenderHelpers.renderList(unref(categories), fun(c, i, __index, _cached): Any {
                                return _cE("view", _uM("class" to "cat-card", "key" to i, "onClick" to fun(){
                                    goList(c.key)
                                }
                                ), _uA(
                                    _cE("view", _uM("class" to "cat-icon"), _uA(
                                        _cE("text", _uM("class" to "cat-icon-text"), _tD(c.name.substring(0, 1)), 1)
                                    )),
                                    _cE("text", _uM("class" to "cat-name"), _tD(c.name), 1),
                                    _cE("text", _uM("class" to "cat-count"), _tD(countByKey(c.key)) + " 件商品", 1)
                                ), 8, _uA(
                                    "onClick"
                                ))
                            }
                            ), 128)
                        )),
                        _cE("text", _uM("class" to "section-title"), "全部商品"),
                        _cE("view", _uM("class" to "goods-grid"), _uA(
                            _cE(Fragment, null, RenderHelpers.renderList(unref(allGoods), fun(g, i, __index, _cached): Any {
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
                        ))
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
                return _uM("container" to _pS(_uM("flexGrow" to 1, "flexShrink" to 1, "flexBasis" to "0%", "backgroundColor" to "#f5f6f8")), "page" to _pS(_uM("flexDirection" to "column", "paddingTop" to "24rpx", "paddingRight" to "24rpx", "paddingBottom" to "24rpx", "paddingLeft" to "24rpx")), "section-title" to _pS(_uM("fontSize" to "32rpx", "fontWeight" to "bold", "color" to "#1a1a1a", "marginBottom" to "20rpx")), "cat-grid" to _pS(_uM("flexDirection" to "row", "flexWrap" to "wrap", "justifyContent" to "space-between", "marginBottom" to "32rpx")), "cat-card" to _pS(_uM("width" to "31%", "backgroundColor" to "#ffffff", "borderTopLeftRadius" to "20rpx", "borderTopRightRadius" to "20rpx", "borderBottomRightRadius" to "20rpx", "borderBottomLeftRadius" to "20rpx", "alignItems" to "center", "paddingTop" to "24rpx", "paddingRight" to 0, "paddingBottom" to "24rpx", "paddingLeft" to 0, "marginBottom" to "20rpx")), "cat-icon" to _pS(_uM("width" to "88rpx", "height" to "88rpx", "borderTopLeftRadius" to "44rpx", "borderTopRightRadius" to "44rpx", "borderBottomRightRadius" to "44rpx", "borderBottomLeftRadius" to "44rpx", "alignItems" to "center", "justifyContent" to "center", "backgroundColor" to "#fff1ec", "marginBottom" to "12rpx")), "cat-icon-text" to _pS(_uM("fontSize" to "36rpx", "color" to "#ff6034", "fontWeight" to "bold")), "cat-name" to _pS(_uM("fontSize" to "26rpx", "color" to "#1a1a1a")), "cat-count" to _pS(_uM("fontSize" to "22rpx", "color" to "#9a9a9a", "marginTop" to "6rpx")), "goods-grid" to _pS(_uM("flexDirection" to "row", "flexWrap" to "wrap", "justifyContent" to "space-between")), "goods-card" to _pS(_uM("width" to "48%", "backgroundColor" to "#ffffff", "borderTopLeftRadius" to "20rpx", "borderTopRightRadius" to "20rpx", "borderBottomRightRadius" to "20rpx", "borderBottomLeftRadius" to "20rpx", "marginBottom" to "20rpx", "paddingTop" to "20rpx", "paddingRight" to "20rpx", "paddingBottom" to "20rpx", "paddingLeft" to "20rpx")), "goods-thumb" to _pS(_uM("height" to "200rpx", "borderTopLeftRadius" to "16rpx", "borderTopRightRadius" to "16rpx", "borderBottomRightRadius" to "16rpx", "borderBottomLeftRadius" to "16rpx", "alignItems" to "center", "justifyContent" to "center", "backgroundColor" to "#fff1ec", "marginBottom" to "16rpx")), "thumb-tag" to _pS(_uM("fontSize" to "24rpx", "color" to "#ff6034")), "goods-name" to _pS(_uM("fontSize" to "26rpx", "color" to "#1a1a1a", "marginBottom" to "8rpx")), "goods-price" to _pS(_uM("fontSize" to "30rpx", "fontWeight" to "bold", "color" to "#f2270c")))
            }
        var inheritAttrs = true
        var inject: Map<String, Map<String, Any?>> = _uM()
        var emits: Map<String, Any?> = _uM()
        var props = _nP(_uM())
        var propsNeedCastKeys: UTSArray<String> = _uA()
        var components: Map<String, CreateVueComponent> = _uM()
    }
}
