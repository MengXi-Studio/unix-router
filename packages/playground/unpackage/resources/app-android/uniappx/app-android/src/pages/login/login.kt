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
import io.dcloud.uniapp.extapi.setStorageSync as uni_setStorageSync
import io.dcloud.uniapp.extapi.showToast as uni_showToast
open class GenPagesLoginLogin : BasePage {
    constructor(__ins: ComponentInternalInstance, __renderer: String?) : super(__ins, __renderer) {}
    companion object {
        @Suppress("UNUSED_PARAMETER", "UNUSED_VARIABLE")
        var setup: (__props: GenPagesLoginLogin) -> Any? = fun(__props): Any? {
            val __ins = getCurrentInstance()!!
            val _ctx = __ins.proxy as GenPagesLoginLogin
            val _cache = __ins.renderCache
            val router = useRouter()
            val route = useRoute()
            val account = ref("")
            val password = ref("")
            fun gen_doLogin_fn(): Unit {
                setLoggedIn(true)
                uni_setStorageSync("route-gen-logged", "1")
                uni_showToast(ShowToastOptions(title = "登录成功", icon = "success"))
                val redirect = route.query.get("redirect")
                if (redirect != null && redirect.length > 0) {
                    router.replace(redirect)
                } else {
                    router.replace(RawLocation__1(name = "home"))
                }
            }
            val doLogin = ::gen_doLogin_fn
            return fun(): Any? {
                return _cE("scroll-view", _uM("class" to "container"), _uA(
                    _cE("view", _uM("class" to "page"), _uA(
                        _cE("view", _uM("class" to "login-logo"), _uA(
                            _cE("text", _uM("class" to "logo-text"), "梦曦商城"),
                            _cE("text", _uM("class" to "logo-sub"), "登录后可使用购物车结算")
                        )),
                        _cE("view", _uM("class" to "input-row"), _uA(
                            _cE("text", _uM("class" to "input-label"), "账号"),
                            _cE("input", _uM("class" to "input", "modelValue" to unref(account), "onInput" to fun(`$event`: UniInputEvent){
                                trySetRefValue(account, `$event`.detail.value)
                            }
                            , "placeholder" to "演示账号：任意输入"), null, 40, _uA(
                                "modelValue"
                            ))
                        )),
                        _cE("view", _uM("class" to "input-row"), _uA(
                            _cE("text", _uM("class" to "input-label"), "密码"),
                            _cE("input", _uM("class" to "input", "modelValue" to unref(password), "onInput" to fun(`$event`: UniInputEvent){
                                trySetRefValue(password, `$event`.detail.value)
                            }
                            , "placeholder" to "演示密码：任意输入", "type" to "password"), null, 40, _uA(
                                "modelValue"
                            ))
                        )),
                        _cE("button", _uM("class" to "login-btn", "onClick" to doLogin), "登 录"),
                        _cE("text", _uM("class" to "login-tip"), "由守卫 beforeEach（requireAuth）重定向而来，登录后自动回跳")
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
                return _uM("container" to _pS(_uM("flexGrow" to 1, "flexShrink" to 1, "flexBasis" to "0%", "backgroundColor" to "#f5f6f8")), "page" to _pS(_uM("flexDirection" to "column", "paddingTop" to "48rpx", "paddingRight" to "32rpx", "paddingBottom" to "48rpx", "paddingLeft" to "32rpx")), "login-logo" to _pS(_uM("flexDirection" to "column", "alignItems" to "center", "marginBottom" to "64rpx")), "logo-text" to _pS(_uM("fontSize" to "48rpx", "fontWeight" to "bold", "color" to "#ff6034")), "logo-sub" to _pS(_uM("fontSize" to "24rpx", "color" to "#9a9a9a", "marginTop" to "12rpx")), "input-row" to _pS(_uM("flexDirection" to "row", "alignItems" to "center", "backgroundColor" to "#ffffff", "borderTopLeftRadius" to "20rpx", "borderTopRightRadius" to "20rpx", "borderBottomRightRadius" to "20rpx", "borderBottomLeftRadius" to "20rpx", "paddingTop" to "24rpx", "paddingRight" to "28rpx", "paddingBottom" to "24rpx", "paddingLeft" to "28rpx", "marginBottom" to "24rpx")), "input-label" to _pS(_uM("fontSize" to "28rpx", "color" to "#1a1a1a", "width" to "120rpx")), "input" to _pS(_uM("flexGrow" to 1, "flexShrink" to 1, "flexBasis" to "0%", "fontSize" to "28rpx")), "login-btn" to _pS(_uM("marginTop" to "24rpx", "backgroundColor" to "#ff6034", "color" to "#ffffff", "borderTopLeftRadius" to "44rpx", "borderTopRightRadius" to "44rpx", "borderBottomRightRadius" to "44rpx", "borderBottomLeftRadius" to "44rpx", "paddingTop" to "24rpx", "paddingRight" to 0, "paddingBottom" to "24rpx", "paddingLeft" to 0, "fontSize" to "30rpx")), "login-tip" to _pS(_uM("fontSize" to "22rpx", "color" to "#9a9a9a", "marginTop" to "24rpx", "textAlign" to "center")))
            }
        var inheritAttrs = true
        var inject: Map<String, Map<String, Any?>> = _uM()
        var emits: Map<String, Any?> = _uM()
        var props = _nP(_uM())
        var propsNeedCastKeys: UTSArray<String> = _uA()
        var components: Map<String, CreateVueComponent> = _uM()
    }
}
