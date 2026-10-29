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
open class GenPagesSubSettingSetting : BasePage {
    constructor(__ins: ComponentInternalInstance, __renderer: String?) : super(__ins, __renderer) {}
    companion object {
        @Suppress("UNUSED_PARAMETER", "UNUSED_VARIABLE")
        var setup: (__props: GenPagesSubSettingSetting) -> Any? = fun(__props): Any? {
            val __ins = getCurrentInstance()!!
            val _ctx = __ins.proxy as GenPagesSubSettingSetting
            val _cache = __ins.renderCache
            return fun(): Any? {
                return _cE("scroll-view", _uM("class" to "container"), _uA(
                    _cE("view", _uM("class" to "page"), _uA(
                        _cE("view", _uM("class" to "card"), _uA(
                            _cE("text", _uM("class" to "title"), "设置"),
                            _cE("text", _uM("class" to "desc"), "本页位于分包 pages-sub，由路由生成插件扫描自动注册到 pages.json 与路由表。"),
                            _cE("text", _uM("class" to "desc"), "页面宏声明了 beforeEnter 独享守卫：未登录访问会被重定向到登录页，登录后可正常进入。")
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
                return _uM("container" to _pS(_uM("flexGrow" to 1, "flexShrink" to 1, "flexBasis" to "0%")), "page" to _pS(_uM("flexDirection" to "column", "paddingTop" to "24rpx", "paddingRight" to "24rpx", "paddingBottom" to "24rpx", "paddingLeft" to "24rpx")), "card" to _pS(_uM("backgroundColor" to "#ffffff", "borderTopLeftRadius" to "12rpx", "borderTopRightRadius" to "12rpx", "borderBottomRightRadius" to "12rpx", "borderBottomLeftRadius" to "12rpx", "borderTopWidth" to "1rpx", "borderRightWidth" to "1rpx", "borderBottomWidth" to "1rpx", "borderLeftWidth" to "1rpx", "borderTopColor" to "#eeeeee", "borderRightColor" to "#eeeeee", "borderBottomColor" to "#eeeeee", "borderLeftColor" to "#eeeeee", "paddingTop" to "32rpx", "paddingRight" to "32rpx", "paddingBottom" to "32rpx", "paddingLeft" to "32rpx")), "title" to _pS(_uM("fontSize" to "32rpx", "fontWeight" to "bold", "color" to "#333333", "marginBottom" to "16rpx")), "desc" to _pS(_uM("fontSize" to "26rpx", "color" to "#666666", "marginTop" to "8rpx")))
            }
        var inheritAttrs = true
        var inject: Map<String, Map<String, Any?>> = _uM()
        var emits: Map<String, Any?> = _uM()
        var props = _nP(_uM())
        var propsNeedCastKeys: UTSArray<String> = _uA()
        var components: Map<String, CreateVueComponent> = _uM()
    }
}
