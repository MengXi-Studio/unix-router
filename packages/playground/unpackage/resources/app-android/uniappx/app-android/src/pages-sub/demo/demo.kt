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
open class GenPagesSubDemoDemo : BasePage {
    constructor(__ins: ComponentInternalInstance, __renderer: String?) : super(__ins, __renderer) {}
    companion object {
        @Suppress("UNUSED_PARAMETER", "UNUSED_VARIABLE")
        var setup: (__props: GenPagesSubDemoDemo) -> Any? = fun(__props): Any? {
            val __ins = getCurrentInstance()!!
            val _ctx = __ins.proxy as GenPagesSubDemoDemo
            val _cache = __ins.renderCache
            val router = useRouter()
            val route = useRoute()
            val numText = ref("0")
            val failText = ref("")
            val busText = ref("")
            var pingTarget: Number = 0
            var userTarget: Number = 0
            onLoad(fun(options: UTSJSONObject){
                pingTarget = eventBus.on("demo:ping", fun(data: Any): Any {
                    busText.value = "收到 demo:ping：" + (data as String)
                    return "" as Any
                }
                )
                userTarget = eventBus.on("user:updated", fun(data: Any): Any {
                    val obj = data as UTSJSONObject
                    val nick = obj["nickname"] as String?
                    busText.value = "收到 user:updated：" + (if (nick != null) {
                        nick
                    } else {
                        ""
                    }
                    )
                    return "" as Any
                }
                )
            }
            )
            onUnload(fun(){
                eventBus.off("demo:ping", pingTarget)
                eventBus.off("user:updated", userTarget)
            }
            )
            onShow(fun(){
                numText.value = queryNumber(route.query, "num", 0).toString(10)
            }
            )
            fun gen_openWithZoom_fn(): Unit {
                router.push(RawLocation__1(path = "pages-sub/goods/list", animationType = "zoom-in", animationDuration = 400))
            }
            val openWithZoom = ::gen_openWithZoom_fn
            fun gen_tryDuplicated_fn(): Unit {
                router.push(RawLocation__1(name = "demo")).`catch`(fun(e: Any?){
                    if (isNavigationFailure(e, RouterErrorCode__1.DUPLICATED)) {
                        failText.value = "捕获 DUPLICATED：" + (e as NavigationFailure).message
                    } else {
                        failText.value = "捕获未知错误"
                    }
                }
                )
            }
            val tryDuplicated = ::gen_tryDuplicated_fn
            fun gen_tryNotFound_fn(): Unit {
                router.push(RawLocation__1(name = "no-such-page")).`catch`(fun(e: Any?){
                    if (isNavigationFailure(e, RouterErrorCode__1.ROUTE_NOT_FOUND)) {
                        failText.value = "捕获 ROUTE_NOT_FOUND：" + (e as NavigationFailure).message
                    } else {
                        failText.value = "捕获未知错误"
                    }
                }
                )
            }
            val tryNotFound = ::gen_tryNotFound_fn
            fun gen_ping_fn(): Unit {
                eventBus.emit("demo:ping", "本地回环 " + Date.now().toString(10))
            }
            val ping = ::gen_ping_fn
            return fun(): Any? {
                return _cE("scroll-view", _uM("class" to "container"), _uA(
                    _cE("view", _uM("class" to "page"), _uA(
                        _cE("view", _uM("class" to "card"), _uA(
                            _cE("text", _uM("class" to "card-title"), "当前路由（useRoute）"),
                            _cE("text", _uM("class" to "route-line"), "fullPath：" + _tD(unref(route).fullPath), 1),
                            _cE("view", _uM("class" to "route-line"), _uA(
                                _cE("text", _uM("class" to "route-label"), "query.num（queryNumber 解析）："),
                                _cE("text", _uM("class" to "route-value"), _tD(unref(numText)), 1)
                            ))
                        )),
                        _cE("view", _uM("class" to "card"), _uA(
                            _cE("text", _uM("class" to "card-title"), "动画覆盖（AnimationPlugin）"),
                            _cE("text", _uM("class" to "card-desc"), "单次导航可覆盖全局默认动画：push 时携带 animationType + animationDuration（App 透传原生动画，H5 以 WAAPI 实现）。"),
                            _cE("button", _uM("class" to "primary-btn", "onClick" to openWithZoom), "缩放动画打开商品列表")
                        )),
                        _cE("view", _uM("class" to "card"), _uA(
                            _cE("text", _uM("class" to "card-title"), "导航失败捕获（isNavigationFailure）"),
                            _cE("text", _uM("class" to "card-desc"), "push 返回的 Promise 失败时可用 isNavigationFailure + RouterErrorCode 收窄判断错误类型。"),
                            _cE("view", _uM("class" to "btn-row"), _uA(
                                _cE("button", _uM("class" to "plain-btn", "onClick" to tryDuplicated), "重复导航"),
                                _cE("button", _uM("class" to "plain-btn", "onClick" to tryNotFound), "路由不存在")
                            )),
                            if (unref(failText) !== "") {
                                _cE("text", _uM("key" to 0, "class" to "result-text"), _tD(unref(failText)), 1)
                            } else {
                                _cC("v-if", true)
                            }
                        )),
                        _cE("view", _uM("class" to "card"), _uA(
                            _cE("text", _uM("class" to "card-title"), "全局事件（eventBus）"),
                            _cE("text", _uM("class" to "card-desc"), "监听 demo:ping（本页自发自收）与 user:updated（个人资料页保存资料时广播）。"),
                            _cE("button", _uM("class" to "plain-btn", "onClick" to ping), "发送 demo:ping"),
                            if (unref(busText) !== "") {
                                _cE("text", _uM("key" to 0, "class" to "result-text"), _tD(unref(busText)), 1)
                            } else {
                                _cC("v-if", true)
                            }
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
                return _uM("container" to _pS(_uM("flexGrow" to 1, "flexShrink" to 1, "flexBasis" to "0%", "backgroundColor" to "#f5f6f8")), "page" to _pS(_uM("flexDirection" to "column", "paddingTop" to "24rpx", "paddingRight" to "24rpx", "paddingBottom" to "24rpx", "paddingLeft" to "24rpx")), "card" to _pS(_uM("backgroundColor" to "#ffffff", "borderTopLeftRadius" to "20rpx", "borderTopRightRadius" to "20rpx", "borderBottomRightRadius" to "20rpx", "borderBottomLeftRadius" to "20rpx", "paddingTop" to "28rpx", "paddingRight" to "28rpx", "paddingBottom" to "28rpx", "paddingLeft" to "28rpx", "marginBottom" to "20rpx")), "card-title" to _pS(_uM("fontSize" to "30rpx", "fontWeight" to "bold", "color" to "#1a1a1a")), "card-desc" to _pS(_uM("fontSize" to "24rpx", "color" to "#9a9a9a", "marginTop" to "12rpx", "lineHeight" to 1.6)), "route-line" to _pS(_uM("flexDirection" to "row", "marginTop" to "16rpx")), "route-label" to _pS(_uM("fontSize" to "24rpx", "color" to "#666666")), "route-value" to _pS(_uM("fontSize" to "24rpx", "color" to "#ff6034")), "primary-btn" to _pS(_uM("marginTop" to "24rpx", "backgroundColor" to "#ff6034", "color" to "#ffffff", "borderTopLeftRadius" to "44rpx", "borderTopRightRadius" to "44rpx", "borderBottomRightRadius" to "44rpx", "borderBottomLeftRadius" to "44rpx", "paddingTop" to "22rpx", "paddingRight" to 0, "paddingBottom" to "22rpx", "paddingLeft" to 0, "fontSize" to "28rpx")), "plain-btn" to _pS(_uM("backgroundColor" to "#fff1ec", "color" to "#ff6034", "borderTopLeftRadius" to "44rpx", "borderTopRightRadius" to "44rpx", "borderBottomRightRadius" to "44rpx", "borderBottomLeftRadius" to "44rpx", "paddingTop" to "18rpx", "paddingRight" to "40rpx", "paddingBottom" to "18rpx", "paddingLeft" to "40rpx", "fontSize" to "26rpx")), "btn-row" to _pS(_uM("flexDirection" to "row", "marginTop" to "24rpx")), "result-text" to _pS(_uM("fontSize" to "24rpx", "color" to "#07c160", "marginTop" to "20rpx")))
            }
        var inheritAttrs = true
        var inject: Map<String, Map<String, Any?>> = _uM()
        var emits: Map<String, Any?> = _uM()
        var props = _nP(_uM())
        var propsNeedCastKeys: UTSArray<String> = _uA()
        var components: Map<String, CreateVueComponent> = _uM()
    }
}
