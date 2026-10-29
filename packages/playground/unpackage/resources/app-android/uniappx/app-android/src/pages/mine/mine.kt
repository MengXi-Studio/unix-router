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
import io.dcloud.uniapp.extapi.removeStorageSync as uni_removeStorageSync
import io.dcloud.uniapp.extapi.showToast as uni_showToast
open class GenPagesMineMine : BasePage {
    constructor(__ins: ComponentInternalInstance, __renderer: String?) : super(__ins, __renderer) {}
    companion object {
        @Suppress("UNUSED_PARAMETER", "UNUSED_VARIABLE")
        var setup: (__props: GenPagesMineMine) -> Any? = fun(__props): Any? {
            val __ins = getCurrentInstance()!!
            val _ctx = __ins.proxy as GenPagesMineMine
            val _cache = __ins.renderCache
            val router = useRouter()
            fun gen_isLoggedIn_fn(): Boolean {
                return isLoggedIn()
            }
            val isLoggedIn__1 = ::gen_isLoggedIn_fn
            fun gen_cartTotalCount_fn(): Number {
                return cartTotalCount()
            }
            val cartTotalCount__1 = ::gen_cartTotalCount_fn
            fun gen_loginBtnClass_fn(): Map<String, Boolean> {
                val m = Map<String, Boolean>()
                m.set("logout", isLoggedIn__1())
                return m
            }
            val loginBtnClass = ::gen_loginBtnClass_fn
            fun gen_loginTextClass_fn(): Map<String, Boolean> {
                val m = Map<String, Boolean>()
                m.set("logout-text", isLoggedIn__1())
                return m
            }
            val loginTextClass = ::gen_loginTextClass_fn
            fun gen_goCart_fn(): Unit {
                router.push(RawLocation__1(name = "cart"))
            }
            val goCart = ::gen_goCart_fn
            fun gen_goOrders_fn(): Unit {
                router.push(RawLocation__1(name = "orders"))
            }
            val goOrders = ::gen_goOrders_fn
            fun gen_goProfile_fn(): Unit {
                router.push(RawLocation__1(name = "profile"))
            }
            val goProfile = ::gen_goProfile_fn
            fun gen_goDemo_fn(): Unit {
                router.push(RawLocation__1(name = "demo"))
            }
            val goDemo = ::gen_goDemo_fn
            fun gen_goSetting_fn(): Unit {
                router.push(RawLocation__1(name = "setting"))
            }
            val goSetting = ::gen_goSetting_fn
            fun gen_goHome_fn(): Unit {
                router.push(RawLocation__1(name = "home"))
            }
            val goHome = ::gen_goHome_fn
            fun gen_toggleLogin_fn(): Unit {
                if (isLoggedIn__1()) {
                    setLoggedIn(false)
                    uni_removeStorageSync("route-gen-logged")
                    uni_showToast(ShowToastOptions(title = "已退出登录", icon = "none"))
                } else {
                    setLoggedIn(true)
                    uni_showToast(ShowToastOptions(title = "已登录（演示）", icon = "none"))
                }
            }
            val toggleLogin = ::gen_toggleLogin_fn
            return fun(): Any? {
                return _cE("scroll-view", _uM("class" to "container"), _uA(
                    _cE("view", _uM("class" to "page"), _uA(
                        _cE("view", _uM("class" to "profile"), _uA(
                            _cE("view", _uM("class" to "avatar"), _uA(
                                _cE("text", _uM("class" to "avatar-text"), _tD(if (isLoggedIn__1()) {
                                    "曦"
                                } else {
                                    "客"
                                }
                                ), 1)
                            )),
                            _cE("text", _uM("class" to "profile-name"), _tD(if (isLoggedIn__1()) {
                                "梦曦商城会员"
                            } else {
                                "未登录"
                            }
                            ), 1),
                            _cE("text", _uM("class" to "profile-desc"), _tD(if (isLoggedIn__1()) {
                                "欢迎回来"
                            } else {
                                "登录后体验完整功能"
                            }
                            ), 1)
                        )),
                        _cE("view", _uM("class" to "order-bar"), _uA(
                            _cE("view", _uM("class" to "order-item", "onClick" to goOrders), _uA(
                                _cE("text", _uM("class" to "order-num"), _tD(unref(orders).length), 1),
                                _cE("text", _uM("class" to "order-label"), "我的订单")
                            )),
                            _cE("view", _uM("class" to "order-item", "onClick" to goCart), _uA(
                                _cE("text", _uM("class" to "order-num"), _tD(cartTotalCount__1()), 1),
                                _cE("text", _uM("class" to "order-label"), "购物车")
                            ))
                        )),
                        _cE("view", _uM("class" to "menu"), _uA(
                            _cE("view", _uM("class" to "menu-item", "onClick" to goCart), _uA(
                                _cE("text", _uM("class" to "menu-label"), "购物车"),
                                _cE("text", _uM("class" to "menu-arrow"), "›")
                            )),
                            _cE("view", _uM("class" to "menu-item", "onClick" to goOrders), _uA(
                                _cE("text", _uM("class" to "menu-label"), "我的订单"),
                                _cE("text", _uM("class" to "menu-arrow"), "›")
                            )),
                            _cE("view", _uM("class" to "menu-item", "onClick" to goProfile), _uA(
                                _cE("text", _uM("class" to "menu-label"), "个人资料（分包 · requireAuth）"),
                                _cE("text", _uM("class" to "menu-arrow"), "›")
                            )),
                            _cE("view", _uM("class" to "menu-item", "onClick" to goDemo), _uA(
                                _cE("text", _uM("class" to "menu-label"), "路由能力演示（分包）"),
                                _cE("text", _uM("class" to "menu-arrow"), "›")
                            )),
                            _cE("view", _uM("class" to "menu-item", "onClick" to goSetting), _uA(
                                _cE("text", _uM("class" to "menu-label"), "设置（分包 · beforeEnter）"),
                                _cE("text", _uM("class" to "menu-arrow"), "›")
                            )),
                            _cE("view", _uM("class" to "menu-item", "onClick" to goHome), _uA(
                                _cE("text", _uM("class" to "menu-label"), "回到首页"),
                                _cE("text", _uM("class" to "menu-arrow"), "›")
                            ))
                        )),
                        _cE("view", _uM("class" to "login-box"), _uA(
                            _cE("view", _uM("class" to _nC(_uA(
                                "login-btn",
                                loginBtnClass()
                            )), "onClick" to toggleLogin), _uA(
                                _cE("text", _uM("class" to _nC(_uA(
                                    "login-text",
                                    loginTextClass()
                                ))), _tD(if (isLoggedIn__1()) {
                                    "退出登录"
                                } else {
                                    "去登录"
                                }
                                ), 3)
                            ), 2)
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
                return _uM("container" to _pS(_uM("flexGrow" to 1, "flexShrink" to 1, "flexBasis" to "0%", "backgroundColor" to "#f5f6f8")), "page" to _pS(_uM("flexDirection" to "column", "paddingTop" to "24rpx", "paddingRight" to "24rpx", "paddingBottom" to "24rpx", "paddingLeft" to "24rpx")), "profile" to _pS(_uM("flexDirection" to "column", "alignItems" to "center", "paddingTop" to "48rpx", "paddingRight" to 0, "paddingBottom" to "32rpx", "paddingLeft" to 0)), "avatar" to _pS(_uM("width" to "120rpx", "height" to "120rpx", "borderTopLeftRadius" to "60rpx", "borderTopRightRadius" to "60rpx", "borderBottomRightRadius" to "60rpx", "borderBottomLeftRadius" to "60rpx", "alignItems" to "center", "justifyContent" to "center", "backgroundColor" to "#ff6034", "marginBottom" to "16rpx")), "avatar-text" to _pS(_uM("fontSize" to "48rpx", "color" to "#ffffff", "fontWeight" to "bold")), "profile-name" to _pS(_uM("fontSize" to "32rpx", "fontWeight" to "bold", "color" to "#1a1a1a")), "profile-desc" to _pS(_uM("fontSize" to "24rpx", "color" to "#9a9a9a", "marginTop" to "8rpx")), "order-bar" to _pS(_uM("flexDirection" to "row", "backgroundColor" to "#ffffff", "borderTopLeftRadius" to "20rpx", "borderTopRightRadius" to "20rpx", "borderBottomRightRadius" to "20rpx", "borderBottomLeftRadius" to "20rpx", "paddingTop" to "32rpx", "paddingRight" to 0, "paddingBottom" to "32rpx", "paddingLeft" to 0, "marginBottom" to "24rpx")), "order-item" to _pS(_uM("flexGrow" to 1, "flexShrink" to 1, "flexBasis" to "0%", "flexDirection" to "column", "alignItems" to "center")), "order-num" to _pS(_uM("fontSize" to "36rpx", "fontWeight" to "bold", "color" to "#1a1a1a")), "order-label" to _pS(_uM("fontSize" to "24rpx", "color" to "#9a9a9a", "marginTop" to "8rpx")), "menu" to _pS(_uM("backgroundColor" to "#ffffff", "borderTopLeftRadius" to "20rpx", "borderTopRightRadius" to "20rpx", "borderBottomRightRadius" to "20rpx", "borderBottomLeftRadius" to "20rpx")), "menu-item" to _pS(_uM("flexDirection" to "row", "alignItems" to "center", "justifyContent" to "space-between", "paddingTop" to "28rpx", "paddingRight" to "28rpx", "paddingBottom" to "28rpx", "paddingLeft" to "28rpx", "borderBottomWidth" to "1rpx", "borderBottomColor" to "#f5f5f5")), "menu-label" to _pS(_uM("fontSize" to "28rpx", "color" to "#1a1a1a")), "menu-arrow" to _pS(_uM("fontSize" to "36rpx", "color" to "#cccccc")), "login-box" to _pS(_uM("marginTop" to "40rpx")), "login-btn" to _pS(_uM("backgroundColor" to "#ff6034", "borderTopLeftRadius" to "44rpx", "borderTopRightRadius" to "44rpx", "borderBottomRightRadius" to "44rpx", "borderBottomLeftRadius" to "44rpx", "alignItems" to "center", "paddingTop" to "24rpx", "paddingRight" to 0, "paddingBottom" to "24rpx", "paddingLeft" to 0)), "logout" to _pS(_uM("backgroundColor" to "#f2f3f5")), "login-text" to _pS(_uM("fontSize" to "30rpx", "color" to "#ffffff")), "logout-text" to _pS(_uM("color" to "#333333")))
            }
        var inheritAttrs = true
        var inject: Map<String, Map<String, Any?>> = _uM()
        var emits: Map<String, Any?> = _uM()
        var props = _nP(_uM())
        var propsNeedCastKeys: UTSArray<String> = _uA()
        var components: Map<String, CreateVueComponent> = _uM()
    }
}
