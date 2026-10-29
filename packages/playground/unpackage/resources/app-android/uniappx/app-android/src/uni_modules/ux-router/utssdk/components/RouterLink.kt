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
open class GenUniModulesUxRouterUtssdkComponentsRouterLink : VueComponent {
    constructor(__ins: ComponentInternalInstance) : super(__ins) {}
    open var to: String by `$props`
    open var replace: Boolean by `$props`
    open var relaunch: Boolean by `$props`
    companion object {
        @Suppress("UNUSED_PARAMETER", "UNUSED_VARIABLE")
        var setup: (__props: GenUniModulesUxRouterUtssdkComponentsRouterLink) -> Any? = fun(__props): Any? {
            val __ins = getCurrentInstance()!!
            val _ctx = __ins.proxy as GenUniModulesUxRouterUtssdkComponentsRouterLink
            val _cache = __ins.renderCache
            val props = __props
            val link = useLink(UseLinkOptions(to = props.to, replace = props.replace, relaunch = props.relaunch))
            val onClick = fun(){
                try {
                    link.navigate()
                }
                 catch (e: Throwable) {}
            }
            return fun(): Any? {
                return _cE("view", _uM("class" to "router-link", "onClick" to onClick), _uA(
                    renderSlot(_ctx.`$slots`, "default")
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
                return _uM("router-link" to _pS(_uM("flexDirection" to "column")))
            }
        var inheritAttrs = true
        var inject: Map<String, Map<String, Any?>> = _uM()
        var emits: Map<String, Any?> = _uM()
        var props = _nP(_uM("to" to _uM("type" to "String", "default" to ""), "replace" to _uM("type" to "Boolean", "default" to false), "relaunch" to _uM("type" to "Boolean", "default" to false)))
        var propsNeedCastKeys = _uA(
            "to",
            "replace",
            "relaunch"
        )
        var components: Map<String, CreateVueComponent> = _uM()
    }
}
