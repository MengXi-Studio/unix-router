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
open class GenPagesSubUserProfile : BasePage {
    constructor(__ins: ComponentInternalInstance, __renderer: String?) : super(__ins, __renderer) {}
    companion object {
        @Suppress("UNUSED_PARAMETER", "UNUSED_VARIABLE")
        var setup: (__props: GenPagesSubUserProfile) -> Any? = fun(__props): Any? {
            val __ins = getCurrentInstance()!!
            val _ctx = __ins.proxy as GenPagesSubUserProfile
            val _cache = __ins.renderCache
            fun gen_cartTotalCount_fn(): Number {
                return cartTotalCount()
            }
            val cartTotalCount__1 = ::gen_cartTotalCount_fn
            fun gen_broadcast_fn(): Unit {
                eventBus.emit("user:updated", _uO("nickname" to "梦曦同学", "savedAt" to Date.now().toString(10)))
                uni_showToast(ShowToastOptions(title = "已广播 user:updated", icon = "none"))
            }
            val broadcast = ::gen_broadcast_fn
            return fun(): Any? {
                return _cE("scroll-view", _uM("class" to "container"), _uA(
                    _cE("view", _uM("class" to "page"), _uA(
                        _cE("view", _uM("class" to "profile-card"), _uA(
                            _cE("view", _uM("class" to "avatar"), _uA(
                                _cE("text", _uM("class" to "avatar-text"), "梦")
                            )),
                            _cE("view", _uM("class" to "profile-info"), _uA(
                                _cE("text", _uM("class" to "nickname"), "梦曦同学"),
                                _cE("text", _uM("class" to "uid"), "UID：MX-0001")
                            ))
                        )),
                        _cE("view", _uM("class" to "stat-card"), _uA(
                            _cE("view", _uM("class" to "stat-item"), _uA(
                                _cE("text", _uM("class" to "stat-num"), _tD(unref(orders).length), 1),
                                _cE("text", _uM("class" to "stat-label"), "订单")
                            )),
                            _cE("view", _uM("class" to "stat-divider")),
                            _cE("view", _uM("class" to "stat-item"), _uA(
                                _cE("text", _uM("class" to "stat-num"), _tD(cartTotalCount__1()), 1),
                                _cE("text", _uM("class" to "stat-label"), "购物车")
                            ))
                        )),
                        _cE("view", _uM("class" to "card"), _uA(
                            _cE("text", _uM("class" to "card-title"), "eventBus 广播演示"),
                            _cE("text", _uM("class" to "card-desc"), "点击后通过 eventBus.emit('user:updated', ...) 全局广播，「路由能力演示」页监听并展示收到的数据（两页需同处页面栈中，可先从演示页进入本页再点击）。"),
                            _cE("button", _uM("class" to "primary-btn", "onClick" to broadcast), "保存资料并广播")
                        )),
                        _cE("view", _uM("class" to "card"), _uA(
                            _cE("text", _uM("class" to "card-title"), "全局守卫演示"),
                            _cE("text", _uM("class" to "card-desc"), "本页 meta 声明 requireAuth: true，未登录访问会被全局 beforeEach 重定向到登录页并携带 redirect 参数，登录后自动跳回本页。")
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
                return _uM("container" to _pS(_uM("flexGrow" to 1, "flexShrink" to 1, "flexBasis" to "0%", "backgroundColor" to "#f5f6f8")), "page" to _pS(_uM("flexDirection" to "column", "paddingTop" to "24rpx", "paddingRight" to "24rpx", "paddingBottom" to "24rpx", "paddingLeft" to "24rpx")), "profile-card" to _pS(_uM("flexDirection" to "row", "alignItems" to "center", "backgroundColor" to "#ff6034", "borderTopLeftRadius" to "20rpx", "borderTopRightRadius" to "20rpx", "borderBottomRightRadius" to "20rpx", "borderBottomLeftRadius" to "20rpx", "paddingTop" to "32rpx", "paddingRight" to "28rpx", "paddingBottom" to "32rpx", "paddingLeft" to "28rpx")), "avatar" to _pS(_uM("width" to "112rpx", "height" to "112rpx", "borderTopLeftRadius" to "56rpx", "borderTopRightRadius" to "56rpx", "borderBottomRightRadius" to "56rpx", "borderBottomLeftRadius" to "56rpx", "backgroundColor" to "#ffffff", "alignItems" to "center", "justifyContent" to "center", "marginRight" to "24rpx")), "avatar-text" to _pS(_uM("fontSize" to "48rpx", "fontWeight" to "bold", "color" to "#ff6034")), "profile-info" to _pS(_uM("flexDirection" to "column")), "nickname" to _pS(_uM("fontSize" to "34rpx", "fontWeight" to "bold", "color" to "#ffffff")), "uid" to _pS(_uM("fontSize" to "24rpx", "color" to "#ffe3d9", "marginTop" to "12rpx")), "stat-card" to _pS(_uM("flexDirection" to "row", "alignItems" to "center", "backgroundColor" to "#ffffff", "borderTopLeftRadius" to "20rpx", "borderTopRightRadius" to "20rpx", "borderBottomRightRadius" to "20rpx", "borderBottomLeftRadius" to "20rpx", "paddingTop" to "28rpx", "paddingRight" to 0, "paddingBottom" to "28rpx", "paddingLeft" to 0, "marginTop" to "20rpx")), "stat-item" to _pS(_uM("flexGrow" to 1, "flexShrink" to 1, "flexBasis" to "0%", "flexDirection" to "column", "alignItems" to "center")), "stat-divider" to _pS(_uM("width" to "1rpx", "height" to "48rpx", "backgroundColor" to "#f0f0f0")), "stat-num" to _pS(_uM("fontSize" to "36rpx", "fontWeight" to "bold", "color" to "#1a1a1a")), "stat-label" to _pS(_uM("fontSize" to "24rpx", "color" to "#9a9a9a", "marginTop" to "8rpx")), "card" to _pS(_uM("backgroundColor" to "#ffffff", "borderTopLeftRadius" to "20rpx", "borderTopRightRadius" to "20rpx", "borderBottomRightRadius" to "20rpx", "borderBottomLeftRadius" to "20rpx", "paddingTop" to "28rpx", "paddingRight" to "28rpx", "paddingBottom" to "28rpx", "paddingLeft" to "28rpx", "marginTop" to "20rpx")), "card-title" to _pS(_uM("fontSize" to "30rpx", "fontWeight" to "bold", "color" to "#1a1a1a")), "card-desc" to _pS(_uM("fontSize" to "24rpx", "color" to "#9a9a9a", "marginTop" to "12rpx", "lineHeight" to 1.6)), "primary-btn" to _pS(_uM("marginTop" to "24rpx", "backgroundColor" to "#ff6034", "color" to "#ffffff", "borderTopLeftRadius" to "44rpx", "borderTopRightRadius" to "44rpx", "borderBottomRightRadius" to "44rpx", "borderBottomLeftRadius" to "44rpx", "paddingTop" to "22rpx", "paddingRight" to 0, "paddingBottom" to "22rpx", "paddingLeft" to 0, "fontSize" to "28rpx")))
            }
        var inheritAttrs = true
        var inject: Map<String, Map<String, Any?>> = _uM()
        var emits: Map<String, Any?> = _uM()
        var props = _nP(_uM())
        var propsNeedCastKeys: UTSArray<String> = _uA()
        var components: Map<String, CreateVueComponent> = _uM()
    }
}
