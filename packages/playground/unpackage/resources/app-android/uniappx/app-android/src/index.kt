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
import io.dcloud.uniapp.extapi.addInterceptor as uni_addInterceptor
import io.dcloud.uniapp.extapi.exit as uni_exit
import io.dcloud.uniapp.extapi.getAppBaseInfo as uni_getAppBaseInfo
import io.dcloud.uniapp.extapi.getStorageSync as uni_getStorageSync
import io.dcloud.uniapp.extapi.navigateBack as uni_navigateBack
import io.dcloud.uniapp.extapi.navigateTo as uni_navigateTo
import io.dcloud.uniapp.extapi.reLaunch as uni_reLaunch
import io.dcloud.uniapp.extapi.redirectTo as uni_redirectTo
import io.dcloud.uniapp.extapi.removeInterceptor as uni_removeInterceptor
import io.dcloud.uniapp.extapi.removeStorageSync as uni_removeStorageSync
import io.dcloud.uniapp.extapi.setStorageSync as uni_setStorageSync
import io.dcloud.uniapp.extapi.showToast as uni_showToast
import io.dcloud.uniapp.extapi.switchTab as uni_switchTab
val runBlock1 = run {
    __uniConfig.getAppStyles = fun(): Map<String, Map<String, Map<String, Any>>> {
        return GenApp.styles
    }
}
open class GenApp : BaseApp {
    constructor(__ins: ComponentInternalInstance) : super(__ins) {
        setCurrentInstance(__ins)
        __ins.proxy = this
        GenApp.setup(this)
    }
    companion object {
        @Suppress("UNUSED_PARAMETER", "UNUSED_VARIABLE")
        var setup: (__props: GenApp) -> Any? = fun(__props): Any? {
            val __ins = getCurrentInstance()!!
            val _ctx = __ins.proxy as GenApp
            val _cache = __ins.renderCache
            var firstBackTime: Number = 0
            onLaunch(fun(_options){
                console.log("App Launch")
            }
            )
            onAppShow(fun(_options){
                console.log("App Show")
            }
            )
            onAppHide(fun(){
                console.log("App Hide")
            }
            )
            onLastPageBackPress(fun(){
                console.log("App LastPageBackPress")
                if (firstBackTime == 0) {
                    uni_showToast(ShowToastOptions(title = "再按一次退出应用", position = "bottom"))
                    firstBackTime = Date.now()
                    setTimeout(fun(){
                        firstBackTime = 0
                    }, 2000)
                } else if (Date.now() - firstBackTime < 2000) {
                    firstBackTime = Date.now()
                    uni_exit(null)
                }
            }
            )
            onExit(fun(){
                console.log("App Exit")
            }
            )
            return fun(): Any? {
                return null
            }
        }
        val styles: Map<String, Map<String, Map<String, Any>>> by lazy {
            _nCS(_uA(
                styles0
            ))
        }
        val styles0: Map<String, Map<String, Map<String, Any>>>
            get() {
                return _uM("uni-row" to _pS(_uM("flexDirection" to "row")), "uni-column" to _pS(_uM("flexDirection" to "column")))
            }
    }
}
val GenAppClass = CreateVueAppComponent(GenApp::class.java, fun(): VueComponentOptions {
    return VueComponentOptions(type = "app", name = "", inheritAttrs = true, inject = Map(), props = Map(), propsNeedCastKeys = _uA(), emits = Map(), components = Map(), styles = GenApp.styles, setup = fun(props: ComponentPublicInstance): Any? {
        return GenApp.setup(props as GenApp)
    }
    )
}
, fun(instance): GenApp {
    return GenApp(instance)
}
)
open class RouteMeta (
    open var title: String? = null,
    open var isTab: Boolean? = null,
    open var requireAuth: Boolean? = null,
) : UTSReactiveObject() {
    override fun __v_create(__v_isReadonly: Boolean, __v_isShallow: Boolean, __v_skip: Boolean): UTSReactiveObject {
        return RouteMetaReactiveObject(this, __v_isReadonly, __v_isShallow, __v_skip)
    }
}
class RouteMetaReactiveObject : RouteMeta, IUTSReactive<RouteMeta> {
    override var __v_raw: RouteMeta
    override var __v_isReadonly: Boolean
    override var __v_isShallow: Boolean
    override var __v_skip: Boolean
    constructor(__v_raw: RouteMeta, __v_isReadonly: Boolean, __v_isShallow: Boolean, __v_skip: Boolean) : super(title = __v_raw.title, isTab = __v_raw.isTab, requireAuth = __v_raw.requireAuth) {
        this.__v_raw = __v_raw
        this.__v_isReadonly = __v_isReadonly
        this.__v_isShallow = __v_isShallow
        this.__v_skip = __v_skip
    }
    override fun __v_clone(__v_isReadonly: Boolean, __v_isShallow: Boolean, __v_skip: Boolean): RouteMetaReactiveObject {
        return RouteMetaReactiveObject(this.__v_raw, __v_isReadonly, __v_isShallow, __v_skip)
    }
    override var title: String?
        get() {
            return _tRG(__v_raw, "title", __v_raw.title, __v_isReadonly, __v_isShallow)
        }
        set(value) {
            if (!__v_canSet("title")) {
                return
            }
            val oldValue = __v_raw.title
            __v_raw.title = value
            _tRS(__v_raw, "title", oldValue, value)
        }
    override var isTab: Boolean?
        get() {
            return _tRG(__v_raw, "isTab", __v_raw.isTab, __v_isReadonly, __v_isShallow)
        }
        set(value) {
            if (!__v_canSet("isTab")) {
                return
            }
            val oldValue = __v_raw.isTab
            __v_raw.isTab = value
            _tRS(__v_raw, "isTab", oldValue, value)
        }
    override var requireAuth: Boolean?
        get() {
            return _tRG(__v_raw, "requireAuth", __v_raw.requireAuth, __v_isReadonly, __v_isShallow)
        }
        set(value) {
            if (!__v_canSet("requireAuth")) {
                return
            }
            val oldValue = __v_raw.requireAuth
            __v_raw.requireAuth = value
            _tRS(__v_raw, "requireAuth", oldValue, value)
        }
}
typealias ParamObject = Map<String, String>
typealias NavigationRedirectMode = String
open class RawLocation__1 (
    open var path: String? = null,
    open var name: String? = null,
    open var query: Map<String, String>? = null,
    open var params: ParamObject? = null,
    open var hash: String? = null,
    open var animationType: String? = null,
    open var animationDuration: Number? = null,
    open var events: Map<String, (data: Any) -> Any>? = null,
) : UTSObject()
typealias RouteLocationRaw = Any
open class RouteLocationNamedRaw (
    @JsonNotNull
    open var name: String,
    open var query: Map<String, String>? = null,
    open var params: ParamObject? = null,
    open var hash: String? = null,
    open var animationType: String? = null,
    open var animationDuration: Number? = null,
    open var events: Map<String, (data: Any) -> Any>? = null,
) : UTSObject()
open class RouteLocationPathRaw (
    @JsonNotNull
    open var path: String,
    open var query: Map<String, String>? = null,
    open var params: ParamObject? = null,
    open var hash: String? = null,
    open var animationType: String? = null,
    open var animationDuration: Number? = null,
    open var events: Map<String, (data: Any) -> Any>? = null,
) : UTSObject()
open class NavigationRedirect (
    @JsonNotNull
    open var location: RouteLocationRaw,
    open var mode: NavigationRedirectMode? = null,
) : UTSObject()
typealias AnimationType = String
open class NavigationAnimation (
    @JsonNotNull
    open var type: AnimationType,
    open var duration: Number? = null,
) : UTSObject()
open class RouteLocation (
    @JsonNotNull
    open var path: String,
    open var name: String? = null,
    @JsonNotNull
    open var meta: RouteMeta,
    @JsonNotNull
    open var query: Map<String, String>,
    @JsonNotNull
    open var params: Map<String, String>,
    @JsonNotNull
    open var fullPath: String,
    @JsonNotNull
    open var hash: String,
    @JsonNotNull
    open var matched: UTSArray<RouteConfig>,
) : UTSReactiveObject() {
    override fun __v_create(__v_isReadonly: Boolean, __v_isShallow: Boolean, __v_skip: Boolean): UTSReactiveObject {
        return RouteLocationReactiveObject(this, __v_isReadonly, __v_isShallow, __v_skip)
    }
}
class RouteLocationReactiveObject : RouteLocation, IUTSReactive<RouteLocation> {
    override var __v_raw: RouteLocation
    override var __v_isReadonly: Boolean
    override var __v_isShallow: Boolean
    override var __v_skip: Boolean
    constructor(__v_raw: RouteLocation, __v_isReadonly: Boolean, __v_isShallow: Boolean, __v_skip: Boolean) : super(path = __v_raw.path, name = __v_raw.name, meta = __v_raw.meta, query = __v_raw.query, params = __v_raw.params, fullPath = __v_raw.fullPath, hash = __v_raw.hash, matched = __v_raw.matched) {
        this.__v_raw = __v_raw
        this.__v_isReadonly = __v_isReadonly
        this.__v_isShallow = __v_isShallow
        this.__v_skip = __v_skip
    }
    override fun __v_clone(__v_isReadonly: Boolean, __v_isShallow: Boolean, __v_skip: Boolean): RouteLocationReactiveObject {
        return RouteLocationReactiveObject(this.__v_raw, __v_isReadonly, __v_isShallow, __v_skip)
    }
    override var path: String
        get() {
            return _tRG(__v_raw, "path", __v_raw.path, __v_isReadonly, __v_isShallow)
        }
        set(value) {
            if (!__v_canSet("path")) {
                return
            }
            val oldValue = __v_raw.path
            __v_raw.path = value
            _tRS(__v_raw, "path", oldValue, value)
        }
    override var name: String?
        get() {
            return _tRG(__v_raw, "name", __v_raw.name, __v_isReadonly, __v_isShallow)
        }
        set(value) {
            if (!__v_canSet("name")) {
                return
            }
            val oldValue = __v_raw.name
            __v_raw.name = value
            _tRS(__v_raw, "name", oldValue, value)
        }
    override var meta: RouteMeta
        get() {
            return _tRG(__v_raw, "meta", __v_raw.meta, __v_isReadonly, __v_isShallow)
        }
        set(value) {
            if (!__v_canSet("meta")) {
                return
            }
            val oldValue = __v_raw.meta
            __v_raw.meta = value
            _tRS(__v_raw, "meta", oldValue, value)
        }
    override var query: Map<String, String>
        get() {
            return _tRG(__v_raw, "query", __v_raw.query, __v_isReadonly, __v_isShallow)
        }
        set(value) {
            if (!__v_canSet("query")) {
                return
            }
            val oldValue = __v_raw.query
            __v_raw.query = value
            _tRS(__v_raw, "query", oldValue, value)
        }
    override var params: Map<String, String>
        get() {
            return _tRG(__v_raw, "params", __v_raw.params, __v_isReadonly, __v_isShallow)
        }
        set(value) {
            if (!__v_canSet("params")) {
                return
            }
            val oldValue = __v_raw.params
            __v_raw.params = value
            _tRS(__v_raw, "params", oldValue, value)
        }
    override var fullPath: String
        get() {
            return _tRG(__v_raw, "fullPath", __v_raw.fullPath, __v_isReadonly, __v_isShallow)
        }
        set(value) {
            if (!__v_canSet("fullPath")) {
                return
            }
            val oldValue = __v_raw.fullPath
            __v_raw.fullPath = value
            _tRS(__v_raw, "fullPath", oldValue, value)
        }
    override var hash: String
        get() {
            return _tRG(__v_raw, "hash", __v_raw.hash, __v_isReadonly, __v_isShallow)
        }
        set(value) {
            if (!__v_canSet("hash")) {
                return
            }
            val oldValue = __v_raw.hash
            __v_raw.hash = value
            _tRS(__v_raw, "hash", oldValue, value)
        }
    override var matched: UTSArray<RouteConfig>
        get() {
            return _tRG(__v_raw, "matched", __v_raw.matched, __v_isReadonly, __v_isShallow)
        }
        set(value) {
            if (!__v_canSet("matched")) {
                return
            }
            val oldValue = __v_raw.matched
            __v_raw.matched = value
            _tRS(__v_raw, "matched", oldValue, value)
        }
}
open class RouteConfig (
    @JsonNotNull
    open var path: String,
    open var name: String? = null,
    open var redirect: RouteLocationRaw? = null,
    open var meta: RouteMeta? = null,
    open var beforeEnter: Any? = null,
) : UTSReactiveObject() {
    override fun __v_create(__v_isReadonly: Boolean, __v_isShallow: Boolean, __v_skip: Boolean): UTSReactiveObject {
        return RouteConfigReactiveObject(this, __v_isReadonly, __v_isShallow, __v_skip)
    }
}
class RouteConfigReactiveObject : RouteConfig, IUTSReactive<RouteConfig> {
    override var __v_raw: RouteConfig
    override var __v_isReadonly: Boolean
    override var __v_isShallow: Boolean
    override var __v_skip: Boolean
    constructor(__v_raw: RouteConfig, __v_isReadonly: Boolean, __v_isShallow: Boolean, __v_skip: Boolean) : super(path = __v_raw.path, name = __v_raw.name, redirect = __v_raw.redirect, meta = __v_raw.meta, beforeEnter = __v_raw.beforeEnter) {
        this.__v_raw = __v_raw
        this.__v_isReadonly = __v_isReadonly
        this.__v_isShallow = __v_isShallow
        this.__v_skip = __v_skip
    }
    override fun __v_clone(__v_isReadonly: Boolean, __v_isShallow: Boolean, __v_skip: Boolean): RouteConfigReactiveObject {
        return RouteConfigReactiveObject(this.__v_raw, __v_isReadonly, __v_isShallow, __v_skip)
    }
    override var path: String
        get() {
            return _tRG(__v_raw, "path", __v_raw.path, __v_isReadonly, __v_isShallow)
        }
        set(value) {
            if (!__v_canSet("path")) {
                return
            }
            val oldValue = __v_raw.path
            __v_raw.path = value
            _tRS(__v_raw, "path", oldValue, value)
        }
    override var name: String?
        get() {
            return _tRG(__v_raw, "name", __v_raw.name, __v_isReadonly, __v_isShallow)
        }
        set(value) {
            if (!__v_canSet("name")) {
                return
            }
            val oldValue = __v_raw.name
            __v_raw.name = value
            _tRS(__v_raw, "name", oldValue, value)
        }
    override var redirect: RouteLocationRaw?
        get() {
            return _tRG(__v_raw, "redirect", __v_raw.redirect, __v_isReadonly, __v_isShallow)
        }
        set(value) {
            if (!__v_canSet("redirect")) {
                return
            }
            val oldValue = __v_raw.redirect
            __v_raw.redirect = value
            _tRS(__v_raw, "redirect", oldValue, value)
        }
    override var meta: RouteMeta?
        get() {
            return _tRG(__v_raw, "meta", __v_raw.meta, __v_isReadonly, __v_isShallow)
        }
        set(value) {
            if (!__v_canSet("meta")) {
                return
            }
            val oldValue = __v_raw.meta
            __v_raw.meta = value
            _tRS(__v_raw, "meta", oldValue, value)
        }
}
typealias NavigationResult = RouteLocation
open class RawLocationLike (
    open var path: String? = null,
    open var name: String? = null,
    open var query: Map<String, String>? = null,
    open var params: Map<String, String>? = null,
) : UTSObject()
typealias NavigationGuardReturn = Any?
typealias NavigationGuard = (to: RouteLocation, from: RouteLocation) -> Any
typealias PostNavigationGuard = (to: RouteLocation, from: RouteLocation, failure: UTSError?) -> Unit
typealias BeforeRouteLeaveGuard = (to: RouteLocation, from: RouteLocation) -> Any
enum class RouterErrorCode__1(override val value: Int) : UTSEnumInt {
    ABORTED(4),
    CANCELLED(8),
    DUPLICATED(16),
    ROUTE_NOT_FOUND(32),
    NAVIGATION_API_ERROR(64),
    SETUP_ERROR(128),
    PLUGIN_REQUIRED(256)
}
open class UseLinkOptions (
    @JsonNotNull
    open var to: RouteLocationRaw,
    open var replace: Boolean? = null,
    open var relaunch: Boolean? = null,
) : UTSObject()
open class UseLinkReturn (
    @JsonNotNull
    open var route: ComputedRef<RouteLocation>,
    @JsonNotNull
    open var isActive: ComputedRef<Boolean>,
    @JsonNotNull
    open var isExactActive: ComputedRef<Boolean>,
    @JsonNotNull
    open var href: ComputedRef<String>,
    open var navigate: () -> UTSPromise<RouteLocation?>,
) : UTSObject()
fun normalizePath(path: String): String {
    var p = path
    if (p.length > 0 && p.charCodeAt(0) !== 47) {
        p = "/" + p
    }
    while(p.length > 1 && p.charAt(p.length - 1) === "/"){
        p = p.substring(0, p.length - 1)
    }
    return p
}
open class ParsedUrl {
    open var path: String = ""
    open var query: Map<String, String> = Map()
}
fun parseQuery(queryString: String): Map<String, String> {
    val result = Map<String, String>()
    var raw = queryString
    if (raw.length > 0 && raw.charAt(0) === "?") {
        raw = raw.substring(1)
    }
    if (raw.length === 0) {
        return result
    }
    val pairs = raw.split("&")
    run {
        var i: Number = 0
        while(i < pairs.length){
            val pair = pairs[i]
            if (pair.length === 0) {
                i++
                continue
            }
            val eqIndex = pair.indexOf("=")
            if (eqIndex === -1) {
                result.set(decodeURIComponentSafe(pair), "")
            } else {
                val key = decodeURIComponentSafe(pair.substring(0, eqIndex))
                val value = decodeURIComponentSafe(pair.substring(eqIndex + 1))
                result.set(key, value)
            }
            i++
        }
    }
    return result
}
fun serializeQuery(query: Map<String, String>): String {
    val parts: UTSArray<String> = _uA()
    query.forEach(fun(value, key){
        parts.push(encodeURIComponent(key) + "=" + encodeURIComponent(value))
    }
    )
    return parts.join("&")
}
fun buildFullPath(path: String, query: Map<String, String>): String {
    if (query.size === 0) {
        return path
    }
    return path + "?" + serializeQuery(query)
}
fun parseUrl(full: String): ParsedUrl {
    val result = ParsedUrl()
    val qIndex = full.indexOf("?")
    if (qIndex === -1) {
        result.path = full
        return result
    }
    result.path = full.substring(0, qIndex)
    result.query = parseQuery(full.substring(qIndex + 1))
    return result
}
fun decodeURIComponentSafe(value: String): String {
    try {
        val decoded = decodeURIComponent(value)
        return if (decoded != null) {
            decoded
        } else {
            value
        }
    }
     catch (e: Throwable) {
        return value
    }
}
fun queryNumber(query: Map<String, String>, key: String, defaultValue: Number = 0): Number {
    val raw = query.get(key)
    if (raw == null || raw === "") {
        return defaultValue
    }
    val n = UTSNumber.parseFloat(raw)
    if (isNaN(n)) {
        return defaultValue
    }
    return n
}
var __DEV__: Boolean = true
fun warn(message: String): Unit {
    if (__DEV__) {
        console.warn("[unix-router] " + message)
    }
}
fun isObject(value: Any): Boolean {
    return value != null && UTSAndroid.`typeof`(value) === "object"
}
var idCounter: Number = 0
fun generateId(prefix: String = ""): String {
    idCounter = (idCounter + 1) % 100000
    val now = Date.now()
    return prefix + now.toString(36) + "_" + idCounter.toString(36)
}
open class PlatformInfo {
    open var isApp: Boolean = false
    open var isH5: Boolean = false
    open var isMpWeixin: Boolean = false
    open var isAppX: Boolean = false
    open var uniPlatform: String = "web"
}
var cached: PlatformInfo? = null
fun getPlatform(): PlatformInfo {
    val current = cached
    if (current != null) {
        return current
    }
    val info = PlatformInfo()
    try {
        val appBase = uni_getAppBaseInfo(null)
        val platform: String? = if (appBase.uniPlatform == null) {
            null
        } else {
            appBase.uniPlatform!!
        }
        info.uniPlatform = if (platform != null && platform.length > 0) {
            platform
        } else {
            "web"
        }
        info.isApp = platform === "app"
        info.isH5 = platform === "web"
        info.isMpWeixin = platform === "mp-weixin"
        info.isAppX = info.isApp || platform === "app"
    }
     catch (e: Throwable) {
        info.uniPlatform = "web"
        info.isH5 = true
    }
    cached = info
    return info
}
val ROUTER_SYMBOL: String = "__unix_router__"
val PARAMS_QUERY_KEY: String = "__params__"
val EVT_QUERY_KEY: String = "__evt__"
val MAX_REDIRECT_DEPTH: Number = 10
val DEFAULT_GUARD_TIMEOUT: Number = 10000
val DEFAULT_READY_TIMEOUT: Number = 0
val DEFAULT_NAV_VERIFY_TIMEOUT: Number = 500
val PARAMS_STORAGE_PREFIX: String = "unixr_params_"
fun mapToRecord(params: ParamObject): UTSJSONObject {
    val obj = UTSJSONObject()
    params.forEach(fun(value, key){
        obj[key] = value
    }
    )
    return obj
}
fun recordToMap(record: Any): ParamObject {
    val m = Map<String, String>()
    val obj = record as UTSJSONObject
    val keys = UTSJSONObject.keys(obj)
    run {
        var i: Number = 0
        while(i < keys.length){
            val k = keys[i]
            m.set(k, obj[k] as String)
            i++
        }
    }
    return m
}
fun storageKey(key: String): String {
    return PARAMS_STORAGE_PREFIX + key
}
open class ParamsManager {
    private var memoryMap: Map<String, ParamObject> = Map<String, ParamObject>()
    private var currentDefaultPersistent: Boolean
    constructor(defaultPersistent: Boolean){
        this.currentDefaultPersistent = defaultPersistent
    }
    open fun setDefaultPersistent(persistent: Boolean): Unit {
        this.currentDefaultPersistent = persistent
    }
    open fun set(params: ParamObject, persistent: Boolean? = null): String {
        val useStorage = if (persistent == null) {
            this.currentDefaultPersistent
        } else {
            persistent
        }
        val key = generateId("pk_")
        try {
            JSON.stringify(mapToRecord(params))
        }
         catch (e: Throwable) {
            warn("params must be JSON-serializable. Non-serializable values will be lost.")
        }
        if (useStorage) {
            try {
                uni_setStorageSync(storageKey(key), JSON.stringify(mapToRecord(params)))
            } catch (e: Throwable) {
                warn("Failed to write params to storage, falling back to memory storage.")
                this.memoryMap.set(key, Map(params))
            }
        } else {
            this.memoryMap.set(key, Map(params))
        }
        return key
    }
    open fun peek(key: String): ParamObject? {
        if (this.memoryMap.has(key)) {
            val stored = this.memoryMap.get(key)
            if (stored != null) {
                return Map(stored)
            }
        }
        try {
            val raw = uni_getStorageSync(storageKey(key))
            if (raw != null) {
                try {
                    return recordToMap(JSON.parse(raw as String)!!)
                }
                 catch (e: Throwable) {
                    return null
                }
            }
        }
         catch (e: Throwable) {}
        return null
    }
    open fun remove(key: String): Unit {
        this.memoryMap.`delete`(key)
        try {
            uni_removeStorageSync(storageKey(key))
        }
         catch (e: Throwable) {}
    }
}
typealias RouterOnError = (error: UTSError, to: RouteLocation, from: RouteLocation) -> Unit
typealias PluginNavigationMode = String
open class UniNavigationOptions (
    @JsonNotNull
    open var path: String,
    @JsonNotNull
    open var query: Map<String, String>,
    @JsonNotNull
    open var isTab: Boolean = false,
    @JsonNotNull
    open var meta: RouteMeta,
    open var animationType: String? = null,
    open var animationDuration: Number? = null,
) : UTSObject()
open class RouterError : UTSError {
    open lateinit var code: RouterErrorCode__1
    open lateinit var to: RouteLocation
    open lateinit var from: RouteLocation
    constructor(code: RouterErrorCode__1, message: String, to: RouteLocation, from: RouteLocation) : super(message) {
        this.code = code
        this.to = to
        this.from = from
        this.name = "RouterError"
    }
}
open class NavigationFailure : RouterError {
    constructor(code: RouterErrorCode__1, message: String, to: RouteLocation, from: RouteLocation) : super(code, message, to, from) {
        this.name = "NavigationFailure"
    }
}
fun isNavigationFailure(error: Any?, type: RouterErrorCode__1? = null): Boolean {
    if (!(error is NavigationFailure)) {
        return false
    }
    if (type == null) {
        return true
    }
    return (error as NavigationFailure).code === type
}
open class GuardRouteOptions (
    open var onAbort: ((failure: UTSError) -> Unit)? = null,
) : UTSObject()
open class RouterOptions (
    @JsonNotNull
    open var routes: UTSArray<RouteConfig>,
    open var strict: Boolean? = null,
    open var guardTimeout: Number? = null,
    open var readyTimeout: Number? = null,
    open var interceptUniApi: Boolean? = null,
    open var plugins: UTSArray<RouterPlugin>? = null,
    open var paramsPersistent: Boolean? = null,
    open var animation: NavigationAnimation? = null,
) : UTSObject()
interface Router {
    val currentRoute: RouteLocation
    fun push(location: RouteLocationRaw): UTSPromise<NavigationResult>
    fun replace(location: RouteLocationRaw): UTSPromise<NavigationResult>
    fun relaunch(location: RouteLocationRaw): UTSPromise<NavigationResult>
    fun back(delta: Number?): UTSPromise<NavigationResult>
    fun beforeEach(guard: NavigationGuard): () -> Unit
    fun beforeResolve(guard: NavigationGuard): () -> Unit
    fun afterEach(guard: PostNavigationGuard): () -> Unit
    fun getRoutes(): UTSArray<RouteConfig>
    fun hasRoute(name: String): Boolean
    fun resolve(location: RouteLocationRaw): RouteLocation
    fun isReady(): UTSPromise<Unit>
    fun onError(handler: RouterOnError): () -> Unit
    fun onRouteChange(listener: (to: RouteLocation, from: RouteLocation) -> Unit): () -> Unit
    fun syncRoute()
    fun guardRoute(location: RouteLocationRaw?, options: GuardRouteOptions?): UTSPromise<RouteLocation>
    fun install(app: Any)
}
typealias PluginData = Map<String, Any>
open class NavigationPrepareContext (
    @JsonNotNull
    open var to: RouteLocation,
    @JsonNotNull
    open var from: RouteLocation,
    @JsonNotNull
    open var mode: PluginNavigationMode,
    @JsonNotNull
    open var pluginData: PluginData,
    @JsonNotNull
    open var query: Map<String, String>,
    @JsonNotNull
    open var options: UniNavigationOptions,
) : UTSObject()
open class NavigationCompleteContext (
    @JsonNotNull
    open var to: RouteLocation,
    @JsonNotNull
    open var mode: PluginNavigationMode,
    @JsonNotNull
    open var pluginData: PluginData,
    @JsonNotNull
    open var result: RouteLocation,
) : UTSObject()
open class PluginContext {
    open lateinit var onEnrichLocation: (hook: (location: RouteLocationRaw) -> RouteLocationRaw) -> Unit
    open lateinit var onAfterResolve: (hook: (enrichedLocation: RouteLocationRaw, pluginData: PluginData) -> Unit) -> Unit
    open lateinit var onPrepareNavigation: (hook: (ctx: NavigationPrepareContext) -> Unit) -> Unit
    open lateinit var onBeforeNavigation: (hook: (ctx: NavigationPrepareContext) -> Any) -> Unit
    open lateinit var onCompleteNavigation: (hook: (ctx: NavigationCompleteContext) -> Unit) -> Unit
    open lateinit var onNavigationAbort: (hook: (pluginData: PluginData) -> Unit) -> Unit
    open lateinit var onRouteSync: (hook: (query: Map<String, String>, params: Map<String, String>) -> Unit) -> Unit
    open lateinit var onAppInstall: (hook: (app: Any) -> Unit) -> Unit
    open lateinit var currentRouteReader: () -> RouteLocation
    open lateinit var resolve: (location: RouteLocationRaw) -> RouteLocation
    open lateinit var router: Any
    open lateinit var paramsManager: Any
    open lateinit var hasPlugin: (name: String) -> Boolean
    constructor(){
        this.onEnrichLocation = fun(hook){}
        this.onAfterResolve = fun(hook){}
        this.onPrepareNavigation = fun(hook){}
        this.onBeforeNavigation = fun(hook){}
        this.onCompleteNavigation = fun(hook){}
        this.onNavigationAbort = fun(hook){}
        this.onRouteSync = fun(hook){}
        this.onAppInstall = fun(hook){}
        this.currentRouteReader = fun(): RouteLocation {
            return this.currentRoute
        }
        this.resolve = fun(location): RouteLocation {
            return this.currentRoute
        }
        this.router = null as Any
        this.paramsManager = null as Any
        this.hasPlugin = fun(name): Boolean {
            return false
        }
    }
    open val currentRoute: RouteLocation
        get(): RouteLocation {
            return this.currentRouteReader()
        }
}
abstract class RouterPlugin {
    open var name: String = ""
    abstract fun install(context: PluginContext, options: RouterOptions): Unit
}
open class UniAnimation (
    open var type: String? = null,
    open var duration: Number? = null,
) : UTSObject()
typealias GuardResultKind = String
fun resolveGuardReturn(value: Any?, to: RouteLocation): GuardResult {
    if (value == null || value === true) {
        return next()
    }
    if (value as Any === false) {
        return abort(RouterErrorCode__1.ABORTED)
    }
    if (value is UTSError) {
        return abort(RouterErrorCode__1.CANCELLED, value as UTSError)
    }
    if (UTSAndroid.`typeof`(value) === "string") {
        return redirect(value as String, null)
    }
    if (UTSAndroid.`typeof`(value) === "object" && isRedirectObject(value)) {
        val nav = value as NavigationRedirect
        return redirect(nav.location, nav.mode ?: null)
    }
    val raw = value as RawLocationLike
    return redirect(raw as RouteLocationRaw, null)
}
fun runGuard(guard: NavigationGuard, to: RouteLocation, from: RouteLocation, timeout: Number): UTSPromise<GuardResult> {
    return wrapUTSPromise(suspend w@{
            try {
                val raw = guard(to, from)
                var value = raw
                if (timeout > 0) {
                    value = await(withTimeout(UTSPromise.resolve(raw), timeout, to))
                } else {
                    value = await(UTSPromise.resolve(raw))
                }
                return@w resolveGuardReturn(value, to)
            }
             catch (e: Throwable) {
                val err = if (e is UTSError) {
                    e as UTSError
                } else {
                    UTSError("" + e)
                }
                return@w GuardResult(kind = "abort", code = RouterErrorCode__1.CANCELLED, error = err)
            }
    })
}
open class GuardResult (
    @JsonNotNull
    open var kind: GuardResultKind,
    open var code: RouterErrorCode__1? = null,
    open var redirect: RouteLocationRaw? = null,
    open var mode: NavigationRedirectMode? = null,
    open var error: UTSError? = null,
) : UTSObject()
fun runGuardQueue(guards: UTSArray<NavigationGuard>, to: RouteLocation, from: RouteLocation, timeout: Number): UTSPromise<GuardResult> {
    return wrapUTSPromise(suspend w@{
            run {
                var i: Number = 0
                while(i < guards.length){
                    val result = await(runGuard(guards[i], to, from, timeout))
                    if (result.kind !== "next") {
                        return@w result
                    }
                    i++
                }
            }
            return@w GuardResult(kind = "next")
    })
}
fun runBeforeEnterGuards(config: RouteConfig, to: RouteLocation, from: RouteLocation, timeout: Number): UTSPromise<GuardResult> {
    return wrapUTSPromise(suspend w@{
            val beforeEnter = config.beforeEnter
            if (beforeEnter == null) {
                return@w GuardResult(kind = "next")
            }
            val list: UTSArray<NavigationGuard> = if (UTSArray.isArray(beforeEnter)) {
                beforeEnter as UTSArray<NavigationGuard>
            } else {
                _uA(
                    beforeEnter as NavigationGuard
                )
            }
            return@w runGuardQueue(list, to, from, timeout)
    })
}
fun runAfterGuards(guards: UTSArray<PostNavigationGuard>, to: RouteLocation, from: RouteLocation, failure: UTSError? = null): Unit {
    run {
        var i: Number = 0
        while(i < guards.length){
            try {
                guards[i](to, from, failure)
            }
             catch (e: Throwable) {
                console.warn("[unix-router] afterEach 守卫执行异常: " + ("" + e))
            }
            i++
        }
    }
}
open class GuardManager {
    private var beforeEachGuards: UTSArray<NavigationGuard> = _uA()
    private var beforeResolveGuards: UTSArray<NavigationGuard> = _uA()
    private var afterGuards: UTSArray<PostNavigationGuard> = _uA()
    private fun register(list: UTSArray<NavigationGuard>, guard: NavigationGuard): () -> Unit {
        list.push(guard)
        return fun(){
            val index = list.indexOf(guard)
            if (index > -1) {
                list.splice(index, 1)
            }
        }
    }
    open fun beforeEach(guard: NavigationGuard): () -> Unit {
        return this.register(this.beforeEachGuards, guard)
    }
    open fun beforeResolve(guard: NavigationGuard): () -> Unit {
        return this.register(this.beforeResolveGuards, guard)
    }
    open fun afterEach(guard: PostNavigationGuard): () -> Unit {
        this.afterGuards.push(guard)
        return fun(){
            val index = this.afterGuards.indexOf(guard)
            if (index > -1) {
                this.afterGuards.splice(index, 1)
            }
        }
    }
    open fun runBeforeEach(to: RouteLocation, from: RouteLocation, timeout: Number): UTSPromise<GuardResult> {
        return runGuardQueue(this.beforeEachGuards, to, from, timeout)
    }
    open fun runBeforeResolve(to: RouteLocation, from: RouteLocation, timeout: Number): UTSPromise<GuardResult> {
        return runGuardQueue(this.beforeResolveGuards, to, from, timeout)
    }
    open fun runAfter(to: RouteLocation, from: RouteLocation, failure: UTSError? = null): Unit {
        runAfterGuards(this.afterGuards, to, from, failure)
    }
    open fun runBeforeEnter(config: RouteConfig, to: RouteLocation, from: RouteLocation, timeout: Number): UTSPromise<GuardResult> {
        return runBeforeEnterGuards(config, to, from, timeout)
    }
}
fun <T> withTimeout(promise: UTSPromise<T>, timeout: Number, to: RouteLocation): UTSPromise<T> {
    return UTSPromise<T>(fun(resolve, reject){
        val timer = setTimeout(fun(){
            warn("导航守卫执行超过 " + timeout + "ms，正在中止导航: " + to.fullPath)
            reject(UTSError("导航守卫超时: " + to.path))
        }
        , timeout)
        promise.then(fun(value){
            clearTimeout(timer)
            resolve(value)
        }
        , fun(err: Any){
            clearTimeout(timer)
            reject(err)
        }
        )
    }
    )
}
fun isRedirectObject(value: Any): Boolean {
    val obj = value as NavigationRedirect
    return obj != null && resolveInOperator(obj, "location")
}
fun next(): GuardResult {
    return GuardResult(kind = "next")
}
fun abort(code: RouterErrorCode__1, error: UTSError? = null): GuardResult {
    if (error != null) {
        return GuardResult(kind = "abort", code = code, error = error)
    }
    return GuardResult(kind = "abort", code = code)
}
fun redirect(location: Any, mode: NavigationRedirectMode?): GuardResult {
    if (mode != null) {
        return GuardResult(kind = "redirect", redirect = location, mode = mode)
    }
    return GuardResult(kind = "redirect", redirect = location)
}
fun createGuardManager(): GuardManager {
    return GuardManager()
}
interface UniFailure {
    var errMsg: String?
}
fun promisifyUniApi(api: String, executor: (resolve: () -> Unit, reject: (err: Any) -> Unit) -> Unit): UTSPromise<Unit> {
    return UTSPromise<Unit>(fun(resolve, reject){
        try {
            executor(fun(){
                return resolve(Unit)
            }
            , fun(err: Any){
                val fail = err as UniFailure
                val message = "" + api + " 调用失败: " + (if (fail != null && fail.errMsg != null) {
                    fail.errMsg!!
                } else {
                    ""
                }
                )
                reject(UTSError(message))
            }
            )
        }
         catch (e: Throwable) {
            reject(if (e is UTSError) {
                e
            } else {
                UTSError("" + api + " 调用异常")
            }
            )
        }
    }
    )
}
fun uniNavigateTo(path: String, query: Map<String, String>, anim: UniAnimation? = null): UTSPromise<Unit> {
    val url = buildFullPath(path, query)
    return promisifyUniApi("navigateTo", fun(resolve, reject){
        if (anim != null) {
            val animType = anim.type
            val duration = anim.duration
            if (animType != null && animType.length > 0) {
                var animDuration: Number = 300
                if (duration != null) {
                    animDuration = duration
                }
                uni_navigateTo(NavigateToOptions(url = url, animationType = animType, animationDuration = animDuration, success = fun(_){
                    return resolve()
                }, fail = fun(err: Any){
                    return reject(err)
                }))
            } else {
                uni_navigateTo(NavigateToOptions(url = url, success = fun(_){
                    return resolve()
                }, fail = fun(err: Any){
                    return reject(err)
                }))
            }
        } else {
            uni_navigateTo(NavigateToOptions(url = url, success = fun(_){
                return resolve()
            }
            , fail = fun(err: Any){
                return reject(err)
            }
            ))
        }
    }
    )
}
fun uniSwitchTab(path: String): UTSPromise<Unit> {
    return promisifyUniApi("switchTab", fun(resolve, reject){
        uni_switchTab(SwitchTabOptions(url = path, success = fun(_){
            return resolve()
        }
        , fail = fun(err: Any){
            return reject(err)
        }
        ))
    }
    )
}
fun uniRedirectTo(path: String, query: Map<String, String>, anim: UniAnimation? = null): UTSPromise<Unit> {
    val url = buildFullPath(path, query)
    return promisifyUniApi("redirectTo", fun(resolve, reject){
        uni_redirectTo(RedirectToOptions(url = url, success = fun(_){
            return resolve()
        }
        , fail = fun(err: Any){
            return reject(err)
        }
        ))
    }
    )
}
fun uniReLaunch(path: String, query: Map<String, String>, anim: UniAnimation? = null): UTSPromise<Unit> {
    val url = buildFullPath(path, query)
    return promisifyUniApi("reLaunch", fun(resolve, reject){
        uni_reLaunch(ReLaunchOptions(url = url, success = fun(_){
            return resolve()
        }
        , fail = fun(err: Any){
            return reject(err)
        }
        ))
    }
    )
}
fun uniNavigateBack(delta: Number, anim: UniAnimation? = null): UTSPromise<Unit> {
    return promisifyUniApi("navigateBack", fun(resolve, reject){
        if (anim != null) {
            val animType = anim.type
            val duration = anim.duration
            if (animType != null && animType.length > 0) {
                var animDuration: Number = 300
                if (duration != null) {
                    animDuration = duration
                }
                uni_navigateBack(NavigateBackOptions(delta = delta, animationType = animType, animationDuration = animDuration, success = fun(_){
                    return resolve()
                }, fail = fun(err: Any){
                    return reject(err)
                }))
            } else {
                uni_navigateBack(NavigateBackOptions(delta = delta, success = fun(_){
                    return resolve()
                }, fail = fun(err: Any){
                    return reject(err)
                }))
            }
        } else {
            uni_navigateBack(NavigateBackOptions(delta = delta, success = fun(_){
                return resolve()
            }
            , fail = fun(err: Any){
                return reject(err)
            }
            ))
        }
    }
    )
}
fun toUniAnimation(options: UniNavigationOptions): UniAnimation? {
    val animType: String? = options.animationType ?: null
    if (animType == null || animType.length === 0) {
        return null
    }
    return UniAnimation(type = animType, duration = options.animationDuration ?: null)
}
fun navigateTo(options: UniNavigationOptions): UTSPromise<Unit> {
    if (options.isTab) {
        return uniSwitchTab(options.path)
    }
    return uniNavigateTo(options.path, options.query, toUniAnimation(options))
}
fun replaceTo(options: UniNavigationOptions): UTSPromise<Unit> {
    if (options.isTab) {
        return uniSwitchTab(options.path)
    }
    return uniRedirectTo(options.path, options.query, toUniAnimation(options))
}
fun relaunchTo(options: UniNavigationOptions): UTSPromise<Unit> {
    if (options.isTab) {
        return uniSwitchTab(options.path)
    }
    return uniReLaunch(options.path, options.query, toUniAnimation(options))
}
fun goBack(delta: Number, anim: UniAnimation? = null): UTSPromise<Unit> {
    return uniNavigateBack(delta, anim)
}
typealias UniPageOptions = Any
open class UniPageLike (
    @JsonNotNull
    open var route: String,
    open var options: UniPageOptions? = null,
) : UTSObject()
fun getCurrentPagesSafe(): UTSArray<UniPageLike> {
    try {
        val pages = getCurrentPages()
        return pages as UTSArray<UniPageLike>
    }
     catch (e: Throwable) {
        return _uA()
    }
}
fun getPageStackLength(): Number {
    return getCurrentPagesSafe().length
}
fun getCurrentPagePath(): String {
    val pages = getCurrentPagesSafe()
    if (pages.length === 0) {
        return "/"
    }
    return "/" + pages[pages.length - 1].route
}
fun getCurrentPageQuery(): Map<String, String> {
    val result = Map<String, String>()
    val pages = getCurrentPagesSafe()
    if (pages.length === 0) {
        return result
    }
    val options = pages[pages.length - 1].options
    if (options != null) {
        val obj = options as UTSJSONObject
        val keys = UTSJSONObject.keys(obj)
        run {
            var i: Number = 0
            while(i < keys.length){
                val k = keys[i]
                result.set(k, obj[k] as String)
                i++
            }
        }
    }
    return result
}
fun sleep(ms: Number): UTSPromise<Unit> {
    return UTSPromise<Unit>(fun(resolve, _reject){
        setTimeout(fun(){
            return resolve(Unit)
        }
        , ms)
    }
    )
}
fun waitForTopPage(targetPath: String, timeout: Number, interval: Number = 50): UTSPromise<Boolean> {
    return wrapUTSPromise(suspend w@{
            val deadline = Date.now() + timeout
            while(true){
                if (getCurrentPagePath() === targetPath) {
                    return@w true
                }
                if (Date.now() >= deadline) {
                    return@w false
                }
                await(sleep(interval))
            }
    })
}
typealias RouteChangeListener = (to: RouteLocation, from: RouteLocation) -> Unit
open class RouteState {
    open lateinit var currentRoute: Ref<RouteLocation>
    private var ready: Boolean
    private var readyTimeout: Number
    private var listeners: UTSArray<RouteChangeListener>
    private var readyWaiters: UTSArray<(() -> Unit)>
    constructor(readyTimeout: Number = 0){
        this.readyTimeout = readyTimeout
        this.ready = false
        this.listeners = _uA()
        this.readyWaiters = _uA()
        val initial = RouteLocation(path = "/", name = null, meta = RouteMeta(), query = Map(), params = Map(), fullPath = "/", hash = "", matched = _uA())
        this.currentRoute = shallowRef<RouteLocation>(initial)
    }
    open fun setCurrentRoute(to: RouteLocation): Unit {
        val from = this.currentRoute.value
        this.currentRoute.value = to
        this.notify(to, from)
    }
    private fun notify(to: RouteLocation, from: RouteLocation): Unit {
        run {
            var i: Number = 0
            while(i < this.listeners.length){
                try {
                    this.listeners[i](to, from)
                }
                 catch (e: Throwable) {
                    warn("路由变化监听器执行异常: " + e)
                }
                i++
            }
        }
    }
    open fun markReady(): Unit {
        this.ready = true
        while(this.readyWaiters.length > 0){
            val waiter = this.readyWaiters.shift()
            if (waiter != null) {
                waiter()
            }
        }
    }
    open fun onReady(): UTSPromise<Unit> {
        if (this.ready) {
            return UTSPromise.resolve()
        }
        if (this.readyTimeout <= 0) {
            return UTSPromise<Unit>(fun(resolve, _reject){
                this.readyWaiters.push(fun(){
                    resolve(Unit)
                }
                )
            }
            )
        }
        return UTSPromise<Unit>(fun(resolve, reject){
            val timer = setTimeout(fun(){
                reject(UTSError("路由器就绪超时"))
            }
            , this.readyTimeout)
            this.readyWaiters.push(fun(){
                clearTimeout(timer)
                resolve(Unit)
            }
            )
        }
        )
    }
    open fun onRouteChange(listener: RouteChangeListener): () -> Unit {
        this.listeners.push(listener)
        return fun(){
            val index = this.listeners.indexOf(listener)
            if (index > -1) {
                this.listeners.splice(index, 1)
            }
        }
    }
}
fun createRouteState(readyTimeout: Number = 0): RouteState {
    return RouteState(readyTimeout)
}
open class ParsedPathUrl (
    @JsonNotNull
    open var path: String,
    @JsonNotNull
    open var query: Map<String, String>,
) : UTSObject()
open class RouteMatcher {
    private var pathMap: Map<String, RouteConfig> = Map<String, RouteConfig>()
    private var nameMap: Map<String, RouteConfig> = Map<String, RouteConfig>()
    private var routeList: UTSArray<RouteConfig> = _uA()
    private var strict: Boolean
    constructor(routes: UTSArray<RouteConfig>, strict: Boolean){
        this.strict = strict
        run {
            var i: Number = 0
            while(i < routes.length){
                val route = routes[i]
                val normalizedPath = normalizePath(route.path)
                val rname: String? = if (route.name != null && route.name!!.length > 0) {
                    route.name
                } else {
                    null
                }
                if (rname != null && this.nameMap.has(rname)) {
                    warn("检测到重复路由名称 \"" + rname + "\"，后者将覆盖前者。")
                }
                if (this.pathMap.has(normalizedPath)) {
                    warn("检测到重复路由路径 \"" + normalizedPath + "\"，后者将覆盖前者。")
                }
                this.pathMap.set(normalizedPath, route)
                if (rname != null) {
                    this.nameMap.set(rname, route)
                }
                this.routeList.push(route)
                i++
            }
        }
    }
    open fun getRoutes(): UTSArray<RouteConfig> {
        return this.routeList.slice()
    }
    open fun hasRoute(name: String): Boolean {
        return this.nameMap.has(name)
    }
    open fun getRouteConfig(path: String): RouteConfig? {
        val config = this.pathMap.get(normalizePath(path))
        return if (config == null) {
            null
        } else {
            config
        }
    }
    open fun resolve(location: Any): RouteLocation {
        if (UTSAndroid.`typeof`(location) === "string") {
            return this.resolveFromPathString(location as String)
        }
        if (isObject(location)) {
            val named = location as RouteLocationNamedRaw
            if (named.name != null && named.name.length > 0) {
                return this.resolveFromName(named)
            }
            val pathRaw = location as RouteLocationPathRaw
            if (pathRaw.path != null && pathRaw.path.length > 0) {
                return this.resolveFromPathRaw(pathRaw)
            }
        }
        throw RouterError(RouterErrorCode__1.ROUTE_NOT_FOUND, "非法的路由位置: " + JSON.stringify(location) + "。", emptyLocation(""), emptyLocation(""))
    }
    private fun createLocation(path: String, name: String?, meta: RouteMeta, query: Map<String, String>, params: Map<String, String>): RouteLocation {
        return RouteLocation(path = path, name = name, meta = meta, query = query, params = params, fullPath = buildFullPath(path, query), hash = "", matched = _uA<RouteConfig>())
    }
    private fun resolveFromPathString(path: String): RouteLocation {
        val parsed = parsePathUrl(path)
        val config = this.pathMap.get(normalizePath(parsed.path))
        val meta: RouteMeta = if (config != null && config.meta != null) {
            config.meta!!
        } else {
            RouteMeta()
        }
        val params = Map<String, String>()
        val location = this.createLocation(normalizePath(parsed.path), if (config != null) {
            if (config.name != null) {
                config.name
            } else {
                null
            }
        } else {
            null
        }
        , meta, parsed.query, params)
        location.matched = if (config != null) {
            _uA(
                config
            )
        } else {
            _uA()
        }
        return location
    }
    private fun resolveFromPathRaw(location: RouteLocationPathRaw): RouteLocation {
        val query: Map<String, String> = if (location.query != null) {
            Map(location.query!!)
        } else {
            Map()
        }
        val params: Map<String, String> = if (location.params != null) {
            Map(location.params!!)
        } else {
            Map()
        }
        val config = this.pathMap.get(normalizePath(location.path))
        val meta: RouteMeta = if (config != null && config.meta != null) {
            config.meta!!
        } else {
            RouteMeta()
        }
        val loc = this.createLocation(normalizePath(location.path), if (config != null) {
            if (config.name != null) {
                config.name
            } else {
                null
            }
        } else {
            null
        }
        , meta, query, params)
        loc.matched = if (config != null) {
            _uA(
                config
            )
        } else {
            _uA()
        }
        return loc
    }
    private fun resolveFromName(location: RouteLocationNamedRaw): RouteLocation {
        val config = this.nameMap.get(location.name)
        if (config == null) {
            if (this.strict) {
                throw RouterError(RouterErrorCode__1.ROUTE_NOT_FOUND, "未找到命名路由 \"" + location.name + "\"。", emptyLocation(""), emptyLocation(""))
            }
            warn("未找到命名路由 \"" + location.name + "\"，将按路径处理。")
            val query: Map<String, String> = if (location.query != null) {
                Map(location.query!!)
            } else {
                Map()
            }
            val params: Map<String, String> = if (location.params != null) {
                Map(location.params!!)
            } else {
                Map()
            }
            val loc = this.createLocation("/" + location.name, null, RouteMeta(), query, params)
            loc.matched = _uA()
            return loc
        }
        val query: Map<String, String> = if (location.query != null) {
            Map(location.query!!)
        } else {
            Map()
        }
        val params: Map<String, String> = if (location.params != null) {
            Map(location.params!!)
        } else {
            Map()
        }
        val meta: RouteMeta = if (config.meta != null) {
            config.meta!!
        } else {
            RouteMeta()
        }
        val loc = this.createLocation(normalizePath(config.path), if (config.name != null) {
            config.name
        } else {
            null
        }
        , meta, query, params)
        loc.matched = _uA(
            config
        )
        return loc
    }
}
fun parsePathUrl(full: String): ParsedPathUrl {
    val qIndex = full.indexOf("?")
    if (qIndex === -1) {
        return ParsedPathUrl(path = full, query = Map())
    }
    return ParsedPathUrl(path = full.substring(0, qIndex), query = parseQuery(full.substring(qIndex + 1)))
}
fun emptyLocation(path: String): RouteLocation {
    return RouteLocation(path = normalizePath(path), meta = RouteMeta(), name = null, query = Map(), params = Map(), fullPath = normalizePath(path), hash = "", matched = _uA<RouteConfig>())
}
fun createRouteMatcher(routes: UTSArray<RouteConfig>, strict: Boolean): RouteMatcher {
    return RouteMatcher(routes, strict)
}
typealias EnrichLocationHook = (location: RouteLocationRaw) -> RouteLocationRaw
typealias AfterResolveHook = (enrichedLocation: RouteLocationRaw, pluginData: PluginData) -> Unit
typealias PrepareNavigationHook = (ctx: NavigationPrepareContext) -> Unit
typealias BeforeNavigationHook = (ctx: NavigationPrepareContext) -> Any
typealias CompleteNavigationHook = (ctx: NavigationCompleteContext) -> Unit
typealias NavigationAbortHook = (pluginData: PluginData) -> Unit
typealias RouteSyncHook = (query: Map<String, String>, params: Map<String, String>) -> Unit
typealias AppInstallHook = (app: Any) -> Unit
open class PluginHookManager {
    private var router: Router by Delegates.notNull()
    private var paramsManager: ParamsManager by Delegates.notNull()
    private var enrichLocationHooks: UTSArray<EnrichLocationHook> = _uA()
    private var afterResolveHooks: UTSArray<AfterResolveHook> = _uA()
    private var prepareNavigationHooks: UTSArray<PrepareNavigationHook> = _uA()
    private var beforeNavigationHooks: UTSArray<BeforeNavigationHook> = _uA()
    private var completeNavigationHooks: UTSArray<CompleteNavigationHook> = _uA()
    private var navigationAbortHooks: UTSArray<NavigationAbortHook> = _uA()
    private var routeSyncHooks: UTSArray<RouteSyncHook> = _uA()
    private var appInstallHooks: UTSArray<AppInstallHook> = _uA()
    private var installedPlugins: Set<String> = Set()
    constructor(router: Router, paramsManager: ParamsManager){
        this.router = router
        this.paramsManager = paramsManager
    }
    open fun install(plugins: UTSArray<RouterPlugin>, options: RouterOptions): Unit {
        val deps = this
        val context = PluginContext()
        context.onEnrichLocation = fun(hook){
            this.enrichLocationHooks.push(hook)
        }
        context.onAfterResolve = fun(hook){
            this.afterResolveHooks.push(hook)
        }
        context.onPrepareNavigation = fun(hook){
            this.prepareNavigationHooks.push(hook)
        }
        context.onBeforeNavigation = fun(hook){
            this.beforeNavigationHooks.push(hook)
        }
        context.onCompleteNavigation = fun(hook){
            this.completeNavigationHooks.push(hook)
        }
        context.onNavigationAbort = fun(hook){
            this.navigationAbortHooks.push(hook)
        }
        context.onRouteSync = fun(hook){
            this.routeSyncHooks.push(hook)
        }
        context.onAppInstall = fun(hook){
            this.appInstallHooks.push(hook)
        }
        context.currentRouteReader = fun(): RouteLocation {
            return deps.router.currentRoute
        }
        context.resolve = fun(location): RouteLocation {
            return deps.router.resolve(location)
        }
        context.router = deps.router
        context.paramsManager = deps.paramsManager
        context.hasPlugin = fun(name): Boolean {
            return this.installedPlugins.has(name)
        }
        run {
            var i: Number = 0
            while(i < plugins.length){
                val plugin = plugins[i]
                this.installedPlugins.add(plugin.name)
                plugin.install(context, options)
                i++
            }
        }
        if (options.interceptUniApi === true && !this.installedPlugins.has("interceptor")) {
            warn("options.interceptUniApi is true but InterceptorPlugin is not registered. The option will be ignored.")
        }
        if (options.paramsPersistent === true && !this.installedPlugins.has("params")) {
            warn("options.paramsPersistent is true but ParamsPlugin is not registered. The option will be ignored.")
        }
    }
    open fun hasPlugin(name: String): Boolean {
        return this.installedPlugins.has(name)
    }
    open fun enrichLocation(location: Any): RouteLocationRaw {
        var result = location
        run {
            var i: Number = 0
            while(i < this.enrichLocationHooks.length){
                result = this.enrichLocationHooks[i](result)
                i++
            }
        }
        return result
    }
    open fun afterResolve(enrichedLocation: Any, pluginData: PluginData): Unit {
        run {
            var i: Number = 0
            while(i < this.afterResolveHooks.length){
                this.afterResolveHooks[i](enrichedLocation, pluginData)
                i++
            }
        }
    }
    open fun prepareNavigation(ctx: NavigationPrepareContext): Unit {
        run {
            var i: Number = 0
            while(i < this.prepareNavigationHooks.length){
                this.prepareNavigationHooks[i](ctx)
                i++
            }
        }
    }
    open fun beforeNavigation(ctx: NavigationPrepareContext): UTSPromise<Unit> {
        return wrapUTSPromise(suspend {
                run {
                    var i: Number = 0
                    while(i < this.beforeNavigationHooks.length){
                        await(this.beforeNavigationHooks[i](ctx))
                        i++
                    }
                }
        })
    }
    open fun completeNavigation(ctx: NavigationCompleteContext): Unit {
        run {
            var i: Number = 0
            while(i < this.completeNavigationHooks.length){
                this.completeNavigationHooks[i](ctx)
                i++
            }
        }
    }
    open fun runAbortHooks(pluginData: PluginData): Unit {
        run {
            var i: Number = 0
            while(i < this.navigationAbortHooks.length){
                try {
                    this.navigationAbortHooks[i](pluginData)
                }
                 catch (e: Throwable) {}
                i++
            }
        }
    }
    open fun runRouteSyncHooks(query: Map<String, String>, params: Map<String, String>): Unit {
        run {
            var i: Number = 0
            while(i < this.routeSyncHooks.length){
                this.routeSyncHooks[i](query, params)
                i++
            }
        }
    }
    open fun runAppInstallHooks(app: Any): Unit {
        run {
            var i: Number = 0
            while(i < this.appInstallHooks.length){
                this.appInstallHooks[i](app)
                i++
            }
        }
    }
}
open class CurrentStackInfo (
    @JsonNotNull
    open var path: String,
    @JsonNotNull
    open var query: Map<String, String>,
) : UTSObject()
open class UniHistory {
    open lateinit var matcher: RouteMatcher
    constructor(matcher: RouteMatcher){
        this.matcher = matcher
    }
    open fun currentStack(): CurrentStackInfo {
        val pages = getCurrentPagesSafe()
        if (pages.length === 0) {
            return CurrentStackInfo(path = "/", query = Map())
        }
        val page = pages[pages.length - 1]
        val path = "/" + page.route
        val query = Map<String, String>()
        val options = page.options
        if (options != null) {
            val obj = options as UTSJSONObject
            val keys = UTSJSONObject.keys(obj)
            run {
                var i: Number = 0
                while(i < keys.length){
                    val k = keys[i]
                    query.set(k, obj[k] as String)
                    i++
                }
            }
        }
        return CurrentStackInfo(path = path, query = query)
    }
    open fun resolveCurrent(): RouteLocation {
        val stack = this.currentStack()
        val q = stack.query
        var full = stack.path
        var first = true
        q.forEach(fun(value, key){
            full += (if (first) {
                "?"
            } else {
                "&"
            }
            ) + key + "=" + encodeURIComponent(value)
            first = false
        }
        )
        return this.matcher.resolve(full)
    }
}
fun emptyRoute(): RouteLocation {
    return RouteLocation(path = "/", name = null, meta = RouteMeta(), query = Map(), params = Map(), fullPath = "/", hash = "", matched = _uA())
}
val currentRoute: RouteLocation = reactive<RouteLocation>(emptyRoute())
var globalRouter: Router? = null
fun setGlobalCurrentRoute(to: RouteLocation): Unit {
    currentRoute.path = to.path
    currentRoute.name = to.name
    currentRoute.meta = to.meta
    currentRoute.query = to.query
    currentRoute.params = to.params
    currentRoute.fullPath = to.fullPath
    currentRoute.hash = to.hash
    currentRoute.matched = to.matched
}
fun setActiveGlobalRouter(router: Router?): Unit {
    globalRouter = router
}
fun activeGlobalRouter(): Router? {
    return globalRouter
}
fun findFirstTabRoutePath(routes: UTSArray<RouteConfig>): String? {
    run {
        var i: Number = 0
        while(i < routes.length){
            val meta: RouteMeta? = routes[i].meta
            if (meta != null && meta.isTab === true) {
                return routes[i].path
            }
            i++
        }
    }
    return null
}
fun isSameRouteLocation(a: RouteLocation, b: RouteLocation): Boolean {
    if (a.path !== b.path) {
        return false
    }
    if (!sameMap(a.query, b.query)) {
        return false
    }
    if (!sameMap(a.params, b.params)) {
        return false
    }
    return a.hash === b.hash
}
fun sameMap(a: Map<String, String>, b: Map<String, String>): Boolean {
    if (a.size !== b.size) {
        return false
    }
    var same = true
    a.forEach(fun(value, key){
        if (b.get(key) !== value) {
            same = false
        }
    }
    )
    return same
}
val INTERCEPTED_APIS = _uA(
    "navigateTo",
    "redirectTo",
    "switchTab",
    "reLaunch",
    "navigateBack"
) as UTSArray<String>
open class InterceptorManager {
    private var routerCallCount: Number = 0
    private var router: Router? = null
    open fun mark(): Unit {
        this.routerCallCount++
    }
    open fun isRouterCall(): Boolean {
        if (this.routerCallCount > 0) {
            this.routerCallCount--
            return true
        }
        return false
    }
    open fun getRouter(): Router? {
        return this.router
    }
    open fun setRouter(router: Router): Unit {
        this.router = router
    }
    open fun reset(): Unit {
        this.router = null
        this.routerCallCount = 0
    }
}
var activeManager: InterceptorManager? = null
fun markRouterCall(): Unit {
    val active = activeManager
    if (active != null) {
        active.mark()
    }
}
fun dispatch(api: String, url: String?, delta: Number?): Unit {
    val active = activeManager
    if (active == null) {
        return
    }
    val router = active.getRouter()
    if (router == null) {
        return
    }
    if (api === "navigateBack") {
        router.back(if (delta != null && delta > 0) {
            delta
        } else {
            1
        }
        )
        return
    }
    if (api === "switchTab") {
        val safeUrl = if (url == null) {
            ""
        } else {
            url
        }
        val parsed = parseUrl(safeUrl)
        if (parsed.path.length > 1) {
            router.push(parsed.path)
        }
        return
    }
    val safeUrl = if (url == null) {
        ""
    } else {
        url
    }
    val parsed = parseUrl(safeUrl)
    if (parsed.path.length <= 1) {
        if (api === "reLaunch") {
            val entryPath = findFirstTabRoutePath(router.getRoutes())
            if (entryPath != null) {
                router.relaunch("/" + entryPath)
            }
        }
        return
    }
    val q = parsed.query
    val hasQuery = q.size > 0
    if (api === "navigateTo") {
        if (hasQuery) {
            router.push(RawLocation__1(path = parsed.path, query = q))
        } else {
            router.push(parsed.path)
        }
    } else if (api === "redirectTo") {
        if (hasQuery) {
            router.replace(RawLocation__1(path = parsed.path, query = q))
        } else {
            router.replace(parsed.path)
        }
    } else if (api === "reLaunch") {
        if (hasQuery) {
            router.relaunch(RawLocation__1(path = parsed.path, query = q))
        } else {
            router.relaunch(parsed.path)
        }
    }
}
fun installApi(api: String, interceptor: AddInterceptorOptions): Unit {
    uni_addInterceptor(api, interceptor)
}
fun installInterceptors(router: Router): Unit {
    if (UTSAndroid.`typeof`(uni_addInterceptor) !== "function") {
        warn("uni.addInterceptor 不可用（HBuilderX 版本不足或平台未实现），原生导航拦截已禁用")
        return
    }
    if (activeManager != null) {
        warn("已有其他路由器实例注册了拦截器，将被新的实例替换（同一时刻仅支持一个）")
        removeInterceptors()
    }
    val manager = InterceptorManager()
    activeManager = manager
    manager.setRouter(router)
    val isWeb = getPlatform().isH5
    run {
        var i: Number = 0
        while(i < INTERCEPTED_APIS.length){
            val api = INTERCEPTED_APIS[i]
            val interceptor = AddInterceptorOptions(invoke = fun(args: Record<String, Any>): Any {
                val argObj = args as UTSJSONObject
                val active = activeManager
                if (active != null && active.isRouterCall()) {
                    return true
                }
                if (api === "switchTab" && isWeb) {
                    if (active != null) {
                        val r = active.getRouter()
                        if (r != null) {
                            val originalSuccess = argObj["success"]
                            argObj["success"] = fun(res: Any): Unit {
                                r.syncRoute()
                                if (originalSuccess != null && UTSAndroid.`typeof`(originalSuccess) === "function") {
                                    (originalSuccess as (res: Any) -> Unit)(res)
                                }
                            }
                        }
                    }
                    return true
                }
                val urlV = argObj["url"]
                val deltaV = argObj["delta"]
                dispatch(api, if (urlV == null) {
                    null
                } else {
                    urlV as String
                }
                , if (deltaV == null) {
                    null
                } else {
                    deltaV as Number
                }
                )
                if (argObj["url"] != null) {
                    argObj["url"] = ""
                }
                return false
            }
            )
            installApi(api, interceptor)
            i++
        }
    }
}
fun removeInterceptors(): Unit {
    if (UTSAndroid.`typeof`(uni_removeInterceptor) === "function") {
        run {
            var i: Number = 0
            while(i < INTERCEPTED_APIS.length){
                uni_removeInterceptor(INTERCEPTED_APIS[i], null)
                i++
            }
        }
    }
    val active = activeManager
    if (active != null) {
        active.reset()
    }
    activeManager = null
}
open class InterceptorPlugin : RouterPlugin {
    constructor() : super() {
        this.name = "interceptor"
    }
    override fun install(context: PluginContext, options: RouterOptions): Unit {
        val interceptUniApi = options.interceptUniApi === true
        if (!interceptUniApi) {
            return
        }
        installInterceptors(context.router as Router)
        context.onAppInstall(fun(app){})
    }
}
fun createParamsManager(defaultPersistent: Boolean): ParamsManager {
    return ParamsManager(defaultPersistent)
}
val PLUGIN_DATA_KEY = "params"
fun injectQueryKey(location: RawLocation__1, key: String, value: String): RawLocation__1 {
    val query = Map<String, String>(location.query ?: Map<String, String>())
    query.set(key, value)
    return UTSJSONObject.assign<RawLocation__1>(_uO(), location, _uO("query" to query)) as RawLocation__1
}
fun extractQueryKey(location: RawLocation__1, key: String): String? {
    val query = location.query
    if (query == null) {
        return null
    }
    val v = query.get(key)
    return if (v == null) {
        null
    } else {
        v
    }
}
open class ParamsPlugin : RouterPlugin {
    constructor() : super() {
        this.name = "params"
    }
    override fun install(context: PluginContext, options: RouterOptions): Unit {
        val persistent = options.paramsPersistent === true
        val paramsManager: ParamsManager = createParamsManager(persistent)
        context.onEnrichLocation(fun(location): Any {
            if (UTSAndroid.`typeof`(location) === "string") {
                return location as String
            }
            val loc = location as RawLocation__1
            if (loc.params == null || loc.params!!.size === 0) {
                return location as RawLocation__1
            }
            val key = paramsManager.set(loc.params!!)
            return injectQueryKey(loc, PARAMS_QUERY_KEY, key)
        }
        )
        context.onAfterResolve(fun(enrichedLocation, pluginData){
            if (UTSAndroid.`typeof`(enrichedLocation) !== "string") {
                val raw = enrichedLocation as RawLocation__1
                val key = extractQueryKey(raw, PARAMS_QUERY_KEY)
                if (key != null && key.length > 0) {
                    pluginData.set(PLUGIN_DATA_KEY, key)
                }
            }
        }
        )
        context.onPrepareNavigation(fun(ctx){
            val key = ctx.pluginData.get(PLUGIN_DATA_KEY)
            if (key != null) {
                ctx.query.set(PARAMS_QUERY_KEY, key as String)
            }
        }
        )
        context.onRouteSync(fun(query, params){
            val key = query.get(PARAMS_QUERY_KEY)
            if (key == null) {
                return
            }
            val resolved = paramsManager.peek(key)
            if (resolved != null) {
                resolved.forEach(fun(value, k){
                    params.set(k, value)
                }
                )
            }
            query.`delete`(PARAMS_QUERY_KEY)
        }
        )
    }
}
typealias NavigationMode = String
open class UniRouter : Router {
    private var routeState: RouteState
    private var guardManager: GuardManager
    private var matcher: RouteMatcher
    private var history: UniHistory
    private var pluginHooks: PluginHookManager
    private var pendingNavigation: UTSPromise<NavigationResult>? = null
    private var errorHandlers: UTSArray<RouterOnError> = _uA()
    private var guardTimeout: Number
    private var readyTimeout: Number
    private var strict: Boolean
    constructor(options: RouterOptions){
        this.strict = options.strict ?: true
        this.guardTimeout = options.guardTimeout ?: DEFAULT_GUARD_TIMEOUT
        this.readyTimeout = options.readyTimeout ?: DEFAULT_READY_TIMEOUT
        this.guardManager = createGuardManager()
        this.matcher = createRouteMatcher(options.routes, this.strict)
        this.routeState = createRouteState(this.readyTimeout)
        this.history = UniHistory(this.matcher)
        this.pluginHooks = PluginHookManager(this, createParamsManager(options.paramsPersistent === true))
        this.pendingNavigation = null
        this.errorHandlers = _uA()
        this.initPlugins(options)
        this.initRoute()
    }
    override val currentRoute: RouteLocation
        get(): RouteLocation {
            return this.routeState.currentRoute.value
        }
    override fun push(location: Any): UTSPromise<NavigationResult> {
        return this.performNavigation(location, "push")
    }
    override fun replace(location: Any): UTSPromise<NavigationResult> {
        return this.performNavigation(location, "replace")
    }
    override fun relaunch(location: Any): UTSPromise<NavigationResult> {
        return this.performNavigation(location, "relaunch")
    }
    override fun back(delta: Number?): UTSPromise<NavigationResult> {
        return wrapUTSPromise(suspend w@{
                if (this.pendingNavigation != null) {
                    await(this.pendingNavigation!!.`catch`(fun(){}))
                }
                val from = this.routeState.currentRoute.value
                val d = if (delta == null) {
                    1
                } else {
                    delta
                }
                if (d <= 0 || !UTSNumber.isInteger(d)) {
                    return@w this.failNavigation(from, from, NavigationFailure(RouterErrorCode__1.ABORTED, "back() 的 delta 必须为正整数", from, from))
                }
                val pages = getCurrentPagesSafe()
                if (pages.length < 2 || d >= pages.length) {
                    return@w this.backWithStackFallback(from)
                }
                val targetPage = pages[pages.length - 1 - d]
                val targetPath = "/" + targetPage.route
                val to = this.matcher.resolve(targetPath)
                val before = await(this.guardManager.runBeforeEach(to, from, this.guardTimeout))
                val handled = await(this.handleGuardResult(before, to, from, "push"))
                if (handled != null) {
                    return@w handled
                }
                val resolveResult = await(this.guardManager.runBeforeResolve(to, from, this.guardTimeout))
                val handledResolve = await(this.handleGuardResult(resolveResult, to, from, "push"))
                if (handledResolve != null) {
                    return@w handledResolve
                }
                val pluginData: PluginData = Map<String, Any>()
                val navOptions = this.buildNavOptions(to)
                val prepareCtx = NavigationPrepareContext(to = to, from = from, mode = "back", pluginData = pluginData, query = navOptions.query, options = navOptions)
                this.pluginHooks.prepareNavigation(prepareCtx)
                await(this.pluginHooks.beforeNavigation(prepareCtx))
                try {
                    markRouterCall()
                    await(goBack(d, UniAnimation(type = navOptions.animationType, duration = navOptions.animationDuration)))
                    this.routeState.setCurrentRoute(to)
                    this.guardManager.runAfter(to, from, null)
                    return@w to
                }
                 catch (e: Throwable) {
                    this.pluginHooks.runAbortHooks(pluginData)
                    val failure = NavigationFailure(RouterErrorCode__1.NAVIGATION_API_ERROR, "返回失败: " + e, to, from)
                    return@w this.failNavigation(to, from, failure)
                }
        })
    }
    override fun beforeEach(guard: NavigationGuard): () -> Unit {
        return this.guardManager.beforeEach(guard)
    }
    private fun backWithStackFallback(from: RouteLocation): UTSPromise<NavigationResult> {
        return wrapUTSPromise(suspend w@{
                val entryPath = findFirstTabRoutePath(this.matcher.getRoutes())
                if (entryPath != null) {
                    warn("back() 页面栈中无上一页（冷启动/刷新直连），兜底重载入口页: /" + entryPath)
                    return@w this.relaunch("/" + entryPath)
                }
                return@w this.failNavigation(from, from, NavigationFailure(RouterErrorCode__1.CANCELLED, "无法返回：页面栈中没有足够的上一页", from, from))
        })
    }
    override fun beforeResolve(guard: NavigationGuard): () -> Unit {
        return this.guardManager.beforeResolve(guard)
    }
    override fun afterEach(guard: PostNavigationGuard): () -> Unit {
        return this.guardManager.afterEach(guard)
    }
    override fun getRoutes(): UTSArray<RouteConfig> {
        return this.matcher.getRoutes()
    }
    override fun hasRoute(name: String): Boolean {
        return this.matcher.hasRoute(name)
    }
    override fun resolve(location: Any): RouteLocation {
        return this.matcher.resolve(location)
    }
    override fun isReady(): UTSPromise<Unit> {
        return this.routeState.onReady()
    }
    override fun onError(handler: RouterOnError): () -> Unit {
        this.errorHandlers.push(handler)
        return fun(){
            val index = this.errorHandlers.indexOf(handler)
            if (index > -1) {
                this.errorHandlers.splice(index, 1)
            }
        }
    }
    override fun onRouteChange(listener: (to: RouteLocation, from: RouteLocation) -> Unit): () -> Unit {
        return this.routeState.onRouteChange(listener)
    }
    override fun syncRoute(): Unit {
        try {
            val current = this.history.resolveCurrent()
            val query = Map<String, String>(current.query)
            val params = Map<String, String>(current.params)
            this.pluginHooks.runRouteSyncHooks(query, params)
            val next = RouteLocation(path = current.path, name = current.name, meta = current.meta, query = query, params = params, fullPath = buildFullPath(current.path, query), hash = current.hash, matched = current.matched)
            this.routeState.setCurrentRoute(next)
        }
         catch (e: Throwable) {
            warn("路由状态同步失败: " + e)
        }
    }
    override fun guardRoute(location: Any?, options: GuardRouteOptions?): UTSPromise<RouteLocation> {
        return wrapUTSPromise(suspend w@{
                val target = if (location != null) {
                    this.matcher.resolve(location)
                } else {
                    this.routeState.currentRoute.value
                }
                val from = this.routeState.currentRoute.value
                val before = await(this.guardManager.runBeforeEach(target, from, this.guardTimeout))
                if (before.kind === "abort") {
                    val failure = NavigationFailure(before.code ?: RouterErrorCode__1.CANCELLED, "守卫中止了冷启动导航", target, from)
                    this.triggerErrorHandlers(failure, target, from)
                    if (options != null && options.onAbort != null) {
                        options.onAbort!!(failure)
                    }
                    return@w UTSPromise<RouteLocation>(fun(resolve, reject){
                        reject(failure)
                    }
                    )
                }
                if (before.kind === "redirect" && before.redirect != null) {
                    val redirectMode = before.mode ?: "relaunch"
                    return@w this.performNavigation(before.redirect!!, if (redirectMode === "replace") {
                        "replace"
                    } else {
                        if (redirectMode === "push") {
                            "push"
                        } else {
                            "relaunch"
                        }
                    }
                    )
                }
                return@w target
        })
    }
    override fun install(app: Any): Unit {
        setActiveGlobalRouter(this)
        this.pluginHooks.runAppInstallHooks(app)
        this.routeState.markReady()
    }
    private fun initPlugins(options: RouterOptions): Unit {
        this.pluginHooks.install(options.plugins ?: _uA(), options)
    }
    private fun initRoute(): Unit {
        if (getPageStackLength() === 0) {
            return
        }
        this.syncRoute()
    }
    private fun stripInternalKeys(to: RouteLocation): RouteLocation {
        val query = Map<String, String>(to.query)
        var stripped = false
        if (query.has(PARAMS_QUERY_KEY)) {
            query.`delete`(PARAMS_QUERY_KEY)
            stripped = true
        }
        if (query.has(EVT_QUERY_KEY)) {
            query.`delete`(EVT_QUERY_KEY)
            stripped = true
        }
        if (!stripped) {
            return to
        }
        return UTSJSONObject.assign<RouteLocation>(_uO(), to, _uO("query" to query, "fullPath" to buildFullPath(to.path, query))) as RouteLocation
    }
    private fun buildNavOptions(to: RouteLocation): UniNavigationOptions {
        val query = Map<String, String>(to.query)
        return UniNavigationOptions(path = to.path, query = query, isTab = to.meta.isTab === true, meta = to.meta)
    }
    private fun performNavigation(location: Any, mode: NavigationMode): UTSPromise<NavigationResult> {
        return wrapUTSPromise(suspend w@{
                if (this.pendingNavigation != null) {
                    await(this.pendingNavigation!!.`catch`(fun(){}))
                }
                if (!this.pluginHooks.hasPlugin("params") && location != null && UTSAndroid.`typeof`(location) !== "string" && (location as RawLocation__1).params != null && (location as RawLocation__1).params!!.size > 0) {
                    val from = this.routeState.currentRoute.value
                    val failure = NavigationFailure(RouterErrorCode__1.PLUGIN_REQUIRED, "使用 params 需注册 ParamsPlugin：createRouter({ plugins: [ParamsPlugin] })", from, from)
                    return@w this.failNavigation(from, from, failure)
                }
                if (!this.pluginHooks.hasPlugin("events") && location != null && UTSAndroid.`typeof`(location) !== "string" && (location as RawLocation__1).events != null && (location as RawLocation__1).events!!.size > 0) {
                    val from = this.routeState.currentRoute.value
                    val failure = NavigationFailure(RouterErrorCode__1.PLUGIN_REQUIRED, "使用 events 需注册 EventsPlugin：createRouter({ plugins: [EventsPlugin] })", from, from)
                    return@w this.failNavigation(from, from, failure)
                }
                val enriched = this.pluginHooks.enrichLocation(location)
                val pluginData: PluginData = Map<String, Any>()
                var to: RouteLocation
                try {
                    to = this.matcher.resolve(enriched)
                }
                 catch (e: Throwable) {
                    val from = this.routeState.currentRoute.value
                    if (e is RouterError) {
                        return@w this.failNavigation(from, from, e as RouterError)
                    }
                    val failure = NavigationFailure(RouterErrorCode__1.ROUTE_NOT_FOUND, "路由解析失败: " + e, from, from)
                    return@w this.failNavigation(from, from, failure)
                }
                this.pluginHooks.afterResolve(enriched, pluginData)
                val from = this.routeState.currentRoute.value
                if (mode === "push" && isSameRouteLocation(to, from)) {
                    this.pluginHooks.runAbortHooks(pluginData)
                    val failure = NavigationFailure(RouterErrorCode__1.DUPLICATED, "避免重复导航到当前地址: \"" + to.fullPath + "\"", to, from)
                    return@w this.failNavigation(to, from, failure)
                }
                val promise = this.executeNavigation(to, from, mode, 0, pluginData)
                this.pendingNavigation = promise
                try {
                    return@w await(promise)
                }
                 finally {
                    if (this.pendingNavigation === promise) {
                        this.pendingNavigation = null
                    }
                }
        })
    }
    private fun executeNavigation(to: RouteLocation, from: RouteLocation, mode: NavigationMode, redirectDepth: Number, pluginData: PluginData): UTSPromise<NavigationResult> {
        return wrapUTSPromise(suspend w@{
                if (redirectDepth > MAX_REDIRECT_DEPTH) {
                    val failure = NavigationFailure(RouterErrorCode__1.CANCELLED, "重定向深度超过上限(" + MAX_REDIRECT_DEPTH + ")", to, from)
                    return@w this.failNavigation(to, from, failure)
                }
                val config = this.matcher.getRouteConfig(to.path)
                val before = await(this.guardManager.runBeforeEach(to, from, this.guardTimeout))
                val handled = await(this.handleGuardResult(before, to, from, mode, redirectDepth, pluginData))
                if (handled != null) {
                    return@w handled
                }
                if (config != null) {
                    val beforeEnter = await(this.guardManager.runBeforeEnter(config, to, from, this.guardTimeout))
                    val handledEnter = await(this.handleGuardResult(beforeEnter, to, from, mode, redirectDepth, pluginData))
                    if (handledEnter != null) {
                        return@w handledEnter
                    }
                }
                val beforeResolve = await(this.guardManager.runBeforeResolve(to, from, this.guardTimeout))
                val handledResolve = await(this.handleGuardResult(beforeResolve, to, from, mode, redirectDepth, pluginData))
                if (handledResolve != null) {
                    return@w handledResolve
                }
                val navOptions = this.buildNavOptions(to)
                val prepareCtx = NavigationPrepareContext(to = to, from = from, mode = mode, pluginData = pluginData, query = navOptions.query, options = navOptions)
                this.pluginHooks.prepareNavigation(prepareCtx)
                await(this.pluginHooks.beforeNavigation(prepareCtx))
                try {
                    markRouterCall()
                    if (mode === "push") {
                        await(navigateTo(navOptions))
                    } else if (mode === "replace") {
                        await(replaceTo(navOptions))
                    } else {
                        await(relaunchTo(navOptions))
                    }
                    val confirmed = await(this.confirmNavigation(to))
                    if (!confirmed) {
                        this.pluginHooks.runAbortHooks(pluginData)
                        val failure = NavigationFailure(RouterErrorCode__1.NAVIGATION_API_ERROR, "导航后未能在页面栈顶确认目标页: \"" + to.path + "\"", to, from)
                        return@w this.failNavigation(to, from, failure)
                    }
                    val clean = this.stripInternalKeys(to)
                    this.routeState.setCurrentRoute(clean)
                    setGlobalCurrentRoute(clean)
                    var result: RouteLocation = clean
                    val completeCtx = NavigationCompleteContext(to = clean, mode = mode, pluginData = pluginData, result = result)
                    this.pluginHooks.completeNavigation(completeCtx)
                    result = completeCtx.result
                    this.guardManager.runAfter(to, from, null)
                    return@w result
                }
                 catch (e: Throwable) {
                    this.pluginHooks.runAbortHooks(pluginData)
                    val failure = NavigationFailure(RouterErrorCode__1.NAVIGATION_API_ERROR, "uni 导航失败: " + e, to, from)
                    return@w this.failNavigation(to, from, failure)
                }
        })
    }
    private fun confirmNavigation(to: RouteLocation): UTSPromise<Boolean> {
        return waitForTopPage(normalizePath(to.path), DEFAULT_NAV_VERIFY_TIMEOUT)
    }
    private fun handleGuardResult(result: GuardResult, to: RouteLocation, from: RouteLocation, mode: NavigationMode, redirectDepth: Number = 0, pluginData: PluginData = Map<String, Any>()): UTSPromise<NavigationResult?> {
        return wrapUTSPromise(suspend w@{
                if (result.kind === "abort") {
                    this.pluginHooks.runAbortHooks(pluginData)
                    val message = if (result.code === RouterErrorCode__1.CANCELLED && result.error != null) {
                        "导航取消: " + result.error!!.message
                    } else {
                        "导航被中止"
                    }
                    val failure = NavigationFailure(result.code ?: RouterErrorCode__1.CANCELLED, message, to, from)
                    return@w this.failNavigation(to, from, failure)
                }
                if (result.kind === "redirect" && result.redirect != null) {
                    val redirectMode: NavigationMode = if (result.mode === "replace") {
                        "replace"
                    } else {
                        if (result.mode === "push") {
                            "push"
                        } else {
                            mode
                        }
                    }
                    val enrichedRedirect = this.pluginHooks.enrichLocation(result.redirect!!)
                    val redirectTarget = this.matcher.resolve(enrichedRedirect)
                    this.pluginHooks.afterResolve(enrichedRedirect, pluginData)
                    return@w this.executeNavigation(redirectTarget, from, redirectMode, redirectDepth + 1, pluginData)
                }
                return@w null
        })
    }
    private fun triggerErrorHandlers(error: UTSError, to: RouteLocation, from: RouteLocation): Unit {
        run {
            var i: Number = 0
            while(i < this.errorHandlers.length){
                try {
                    this.errorHandlers[i](error, to, from)
                }
                 catch (e: Throwable) {}
                i++
            }
        }
    }
    private fun failNavigation(to: RouteLocation, from: RouteLocation, failure: UTSError): UTSPromise<Any?> {
        this.guardManager.runAfter(to, from, failure)
        this.triggerErrorHandlers(failure, to, from)
        return UTSPromise<Any?>(fun(resolve, reject){
            reject(failure)
        }
        )
    }
}
fun createRouter(options: RouterOptions): Router {
    return UniRouter(options)
}
fun useRouter(): Router {
    val instance = getCurrentInstance()
    if (instance != null) {
        val injected = inject<Router?>(ROUTER_SYMBOL, null)
        if (injected != null) {
            return injected
        }
    }
    val global = activeGlobalRouter()
    if (global != null) {
        return global
    }
    throw UTSError("未找到路由器实例，请先 app.use(router) 后再调用 useRouter()")
}
fun useRoute(): RouteLocation {
    return currentRoute
}
fun useLink(options: UseLinkOptions): UseLinkReturn {
    val router = useRouter()
    val route: ComputedRef<RouteLocation> = computed(fun(): RouteLocation {
        return router.resolve(options.to)
    }
    )
    val href: ComputedRef<String> = computed(fun(): String {
        return route.value.fullPath
    }
    )
    val isExactActive: ComputedRef<Boolean> = computed(fun(): Boolean {
        return router.currentRoute.path === route.value.path
    }
    )
    val isActive: ComputedRef<Boolean> = computed(fun(): Boolean {
        val current = router.currentRoute.path
        val target = route.value.path
        return current === target || current.startsWith(target + "/")
    }
    )
    val navigate = fun(): UTSPromise<RouteLocation?> {
        return wrapUTSPromise(suspend w1@{
                if (options.relaunch === true) {
                    await(router.relaunch(options.to))
                    return@w1 null
                }
                if (options.replace === true) {
                    await(router.replace(options.to))
                    return@w1 null
                }
                return@w1 router.push(options.to)
        })
    }
    return UseLinkReturn(route = route, isActive = isActive, isExactActive = isExactActive, href = href, navigate = navigate)
}
open class BusListener (
    @JsonNotNull
    open var id: Number,
    @JsonNotNull
    open var once: Boolean = false,
    open var fn: (data: Any) -> Any,
) : UTSObject()
open class UniEventBus {
    private var listeners: Map<String, UTSArray<BusListener>> = Map<String, UTSArray<BusListener>>()
    private var nextId: Number = 1
    open fun `$on`(eventName: String, fn: (data: Any) -> Any): Number {
        val id = this.nextId++
        val item = BusListener(id = id, once = false, fn = fn)
        val list = this.listeners.get(eventName)
        if (list == null) {
            this.listeners.set(eventName, _uA(
                item
            ))
        } else {
            list.push(item)
        }
        return id
    }
    open fun `$once`(eventName: String, fn: (data: Any) -> Any): Number {
        val id = this.nextId++
        val item = BusListener(id = id, once = true, fn = fn)
        val list = this.listeners.get(eventName)
        if (list == null) {
            this.listeners.set(eventName, _uA(
                item
            ))
        } else {
            list.push(item)
        }
        return id
    }
    open fun `$off`(eventName: String, target: Number? = null): Unit {
        val list = this.listeners.get(eventName)
        if (list == null) {
            return
        }
        if (target == null) {
            this.listeners.`delete`(eventName)
            return
        }
        val keep: UTSArray<BusListener> = _uA()
        run {
            var i: Number = 0
            while(i < list.length){
                val item = list[i]
                if (item.id !== target) {
                    keep.push(item)
                }
                i++
            }
        }
        if (keep.length === 0) {
            this.listeners.`delete`(eventName)
        } else {
            this.listeners.set(eventName, keep)
        }
    }
    @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    open fun `$emit`(eventName: String, vararg spreadArgs: Any): Unit {
        var args = UTSArray(*spreadArgs)
        val list = this.listeners.get(eventName)
        if (list == null) {
            return
        }
        val pending: UTSArray<BusListener> = _uA()
        val remain: UTSArray<BusListener> = _uA()
        run {
            var i: Number = 0
            while(i < list.length){
                val item = list[i]
                pending.push(item)
                if (!item.once) {
                    remain.push(item)
                }
                i++
            }
        }
        if (remain.length === 0) {
            this.listeners.`delete`(eventName)
        } else {
            this.listeners.set(eventName, remain)
        }
        run {
            var j: Number = 0
            while(j < pending.length){
                try {
                    if (args.length > 0) {
                        pending[j].fn(args[0])
                    }
                }
                 catch (e: Throwable) {}
                j++
            }
        }
    }
    open fun on(eventName: String, fn: (data: Any) -> Any): Number {
        return this.`$on`(eventName, fn)
    }
    open fun once(eventName: String, fn: (data: Any) -> Any): Number {
        return this.`$once`(eventName, fn)
    }
    open fun off(eventName: String, target: Number? = null): Unit {
        this.`$off`(eventName, target)
    }
    @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    open fun emit(eventName: String, vararg spreadArgs: Any): Unit {
        var args = UTSArray(*spreadArgs)
        this.`$emit`(eventName, *args.toTypedArray())
    }
}
val eventBus: UniEventBus = UniEventBus()
val CHANNEL_NS = "evt:"
var nextChannelSeq: Number = 0
fun nsEvent(channelKey: String, eventName: String): String {
    return CHANNEL_NS + channelKey + ":" + eventName
}
open class EventChannel {
    open lateinit var key: String
    constructor(key: String){
        this.key = key
    }
    open fun on(eventName: String, fn: (data: Any) -> Any): Number {
        return eventBus.on(nsEvent(this.key, eventName), fn)
    }
    open fun once(eventName: String, fn: (data: Any) -> Any): Number {
        return eventBus.once(nsEvent(this.key, eventName), fn)
    }
    open fun off(eventName: String, target: Number? = null): Unit {
        eventBus.off(nsEvent(this.key, eventName), target)
    }
    @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    open fun emit(eventName: String, vararg spreadArgs: Any): Unit {
        var args = UTSArray(*spreadArgs)
        eventBus.emit(nsEvent(this.key, eventName), *args.toTypedArray())
    }
}
open class ChannelManager {
    private var channels: Map<String, EventChannel> = Map<String, EventChannel>()
    open fun create(): String {
        val key = "ch" + nextChannelSeq++ + "_" + Date.now().toString(10)
        if (!this.channels.has(key)) {
            this.channels.set(key, EventChannel(key))
        }
        return key
    }
    open fun get(key: String): EventChannel? {
        val ch = this.channels.get(key)
        return if (ch == null) {
            null
        } else {
            ch
        }
    }
    open fun remove(key: String): Unit {
        this.channels.`delete`(key)
    }
}
val channelManager: ChannelManager = ChannelManager()
typealias EventsMap = Map<String, (data: Any) -> Any>
val EVT_QUERY_KEY__1: String = "__evt__"
val CHANNEL_DATA_KEY: String = "events_channel"
fun pickEvents(location: Any?): EventsMap? {
    if (location == null || UTSAndroid.`typeof`(location) === "string") {
        return null
    }
    val raw = location as RawLocation__1
    val events: EventsMap? = raw.events ?: null
    if (events == null || events.size === 0) {
        return null
    }
    return events as EventsMap
}
@JvmField
var currentChannelKey: String? = null
fun setCurrentChannelKey(key: String?): Unit {
    currentChannelKey = key
}
fun getCurrentChannelKey(): String? {
    return currentChannelKey
}
fun getCurrentPageEventKey(): String? {
    val key = getCurrentPageQuery().get(EVT_QUERY_KEY__1)
    if (key == null || key.length === 0) {
        return null
    }
    return key
}
fun useOpenerEventChannel(): EventChannel? {
    var key = getCurrentChannelKey()
    if (key == null) {
        key = getCurrentPageEventKey()
    }
    if (key == null) {
        return null
    }
    return channelManager.get(key)
}
fun onBeforeRouteLeave(guard: BeforeRouteLeaveGuard): Unit {
    val router = useRouter()
    val currentPath = router.currentRoute.path
    router.beforeResolve(fun(to, from): Any {
        if (from.path !== currentPath) {
            return true
        }
        return guard(to, from)
    }
    )
}
val ANIM_DATA_KEY: String = "__animation__"
fun pickAnimation(location: Any?): NavigationAnimation? {
    if (location == null || UTSAndroid.`typeof`(location) === "string") {
        return null
    }
    val raw = location as RawLocation__1
    val animType: String? = raw.animationType ?: null
    if (animType == null || animType.length === 0) {
        return null
    }
    return NavigationAnimation(type = animType as AnimationType, duration = raw.animationDuration ?: null)
}
fun toExitType(type: AnimationType): AnimationType {
    when (type) {
        "slide-in-right" -> 
            return "slide-out-right"
        "slide-in-left" -> 
            return "slide-out-left"
        "slide-in-top" -> 
            return "slide-out-top"
        "slide-in-bottom" -> 
            return "slide-out-bottom"
        "fade-in" -> 
            return "fade-out"
        "zoom-in" -> 
            return "zoom-out"
        "zoom-fade-in" -> 
            return "zoom-fade-out"
        "pop-in" -> 
            return "pop-out"
        "auto" -> 
            return "none"
        else -> 
            return type
    }
}
val RESOLVED_KEY: String = "animation_resolved"
open class AnimationPlugin : RouterPlugin {
    constructor() : super() {
        this.name = "animation"
    }
    override fun install(context: PluginContext, options: RouterOptions): Unit {
        val isH5 = getPlatform().isH5
        context.onAfterResolve(fun(enrichedLocation: RouteLocationRaw, pluginData){
            val anim = pickAnimation(enrichedLocation)
            if (anim != null) {
                pluginData.set(ANIM_DATA_KEY, anim)
            }
        }
        )
        context.onPrepareNavigation(fun(ctx){
            val single = ctx.pluginData.get(ANIM_DATA_KEY) as NavigationAnimation?
            val anim: NavigationAnimation? = if (single != null) {
                single
            } else {
                if (options.animation != null) {
                    options.animation
                } else {
                    null
                }
            }
            if (anim != null) {
                val type = if (ctx.mode === "back") {
                    toExitType(anim.type)
                } else {
                    anim.type
                }
                ctx.options.animationType = type
                ctx.options.animationDuration = anim.duration
                ctx.pluginData.set(RESOLVED_KEY, anim)
            }
        }
        )
        val resolveForHook = fun(pluginData: Map<String, Any>): NavigationAnimation? {
            val resolved = pluginData.get(RESOLVED_KEY) as NavigationAnimation?
            if (resolved != null) {
                return resolved
            }
            return if (options.animation != null) {
                options.animation
            } else {
                null
            }
        }
        context.onBeforeNavigation(fun(ctx): UTSPromise<Unit> {
            return wrapUTSPromise(suspend w1@{
                    if (!isH5 || ctx.mode !== "back") {
                        return@w1
                    }
                    val anim = resolveForHook(ctx.pluginData)
                    if (anim == null) {
                        return@w1
                    }
            })
        }
        )
        if (isH5) {
            context.onCompleteNavigation(fun(ctx){
                val anim = resolveForHook(ctx.pluginData)
                if (anim == null) {
                    return
                }
            }
            )
        }
    }
}
open class EventsPlugin : RouterPlugin {
    constructor() : super() {
        this.name = "events"
    }
    override fun install(context: PluginContext, options: RouterOptions): Unit {
        context.onAfterResolve(fun(enrichedLocation: RouteLocationRaw, pluginData){
            val events: EventsMap? = pickEvents(enrichedLocation)
            if (events == null) {
                return
            }
            val key = channelManager.create()
            val channel = channelManager.get(key)
            if (channel != null) {
                events.forEach(fun(fn, eventName){
                    channel.on(eventName, fn)
                }
                )
            }
            pluginData.set(CHANNEL_DATA_KEY, key)
        }
        )
        context.onPrepareNavigation(fun(ctx){
            val key = ctx.pluginData.get(CHANNEL_DATA_KEY)
            if (key != null) {
                ctx.query.set(EVT_QUERY_KEY__1, key as String)
            }
        }
        )
        context.onRouteSync(fun(query, params){
            val key = query.get(EVT_QUERY_KEY__1)
            if (key != null && key.length > 0) {
                setCurrentChannelKey(key)
            } else {
                setCurrentChannelKey(null)
            }
            query.`delete`(EVT_QUERY_KEY__1)
        }
        )
        context.onNavigationAbort(fun(pluginData){
            val key = pluginData.get(CHANNEL_DATA_KEY)
            if (key != null) {
                channelManager.remove(key as String)
            }
        }
        )
    }
}
val GenUniModulesUxRouterUtssdkComponentsRouterLinkClass = CreateVueComponent(GenUniModulesUxRouterUtssdkComponentsRouterLink::class.java, fun(): VueComponentOptions {
    return VueComponentOptions(type = "component", name = "", inheritAttrs = GenUniModulesUxRouterUtssdkComponentsRouterLink.inheritAttrs, inject = GenUniModulesUxRouterUtssdkComponentsRouterLink.inject, props = GenUniModulesUxRouterUtssdkComponentsRouterLink.props, propsNeedCastKeys = GenUniModulesUxRouterUtssdkComponentsRouterLink.propsNeedCastKeys, emits = GenUniModulesUxRouterUtssdkComponentsRouterLink.emits, components = GenUniModulesUxRouterUtssdkComponentsRouterLink.components, styles = GenUniModulesUxRouterUtssdkComponentsRouterLink.styles, setup = fun(props: ComponentPublicInstance): Any? {
        return GenUniModulesUxRouterUtssdkComponentsRouterLink.setup(props as GenUniModulesUxRouterUtssdkComponentsRouterLink)
    }
    )
}
, fun(instance, renderer): GenUniModulesUxRouterUtssdkComponentsRouterLink {
    return GenUniModulesUxRouterUtssdkComponentsRouterLink(instance)
}
)
val routes = _uA(
    RouteConfig(path = "pages-sub/checkout/checkout", name = "checkout", meta = RouteMeta(title = "确认订单", requireAuth = true)),
    RouteConfig(path = "pages-sub/demo/demo", name = "demo", meta = RouteMeta(title = "路由能力演示")),
    RouteConfig(path = "pages-sub/goods/detail", name = "goods-detail", meta = RouteMeta(title = "商品详情")),
    RouteConfig(path = "pages-sub/goods/list", name = "goods-list", meta = RouteMeta(title = "商品列表")),
    RouteConfig(path = "pages-sub/orders/orders", name = "orders", meta = RouteMeta(title = "我的订单", requireAuth = true)),
    RouteConfig(path = "pages-sub/setting/setting", name = "setting", meta = RouteMeta(title = "设置"), beforeEnter = fun(to: Any, from: Any): Any {
        val logged = uni_getStorageSync("route-gen-logged")
        if (logged != "1") {
            console.log("[route-gen] beforeEnter setting -> 未登录，重定向 login")
            return _uO("name" to "login") as Any
        }
        console.log("[route-gen] beforeEnter setting -> 已登录，放行")
        return true
    }
    ),
    RouteConfig(path = "pages-sub/user/profile", name = "profile", meta = RouteMeta(title = "个人资料", requireAuth = true)),
    RouteConfig(path = "pages/index/index", name = "home", meta = RouteMeta(title = "梦曦商城", isTab = true)),
    RouteConfig(path = "pages/cart/cart", name = "cart", meta = RouteMeta(title = "购物车")),
    RouteConfig(path = "pages/category/category", name = "category", meta = RouteMeta(title = "分类", isTab = true)),
    RouteConfig(path = "pages/login/login", name = "login"),
    RouteConfig(path = "pages/mine/mine", name = "mine", meta = RouteMeta(title = "我的", isTab = true))
) as UTSArray<RouteConfig>
open class Goods (
    @JsonNotNull
    open var id: Number,
    @JsonNotNull
    open var name: String,
    @JsonNotNull
    open var price: Number,
    @JsonNotNull
    open var category: String,
    @JsonNotNull
    open var tag: String,
    @JsonNotNull
    open var desc: String,
) : UTSReactiveObject() {
    override fun __v_create(__v_isReadonly: Boolean, __v_isShallow: Boolean, __v_skip: Boolean): UTSReactiveObject {
        return GoodsReactiveObject(this, __v_isReadonly, __v_isShallow, __v_skip)
    }
}
class GoodsReactiveObject : Goods, IUTSReactive<Goods> {
    override var __v_raw: Goods
    override var __v_isReadonly: Boolean
    override var __v_isShallow: Boolean
    override var __v_skip: Boolean
    constructor(__v_raw: Goods, __v_isReadonly: Boolean, __v_isShallow: Boolean, __v_skip: Boolean) : super(id = __v_raw.id, name = __v_raw.name, price = __v_raw.price, category = __v_raw.category, tag = __v_raw.tag, desc = __v_raw.desc) {
        this.__v_raw = __v_raw
        this.__v_isReadonly = __v_isReadonly
        this.__v_isShallow = __v_isShallow
        this.__v_skip = __v_skip
    }
    override fun __v_clone(__v_isReadonly: Boolean, __v_isShallow: Boolean, __v_skip: Boolean): GoodsReactiveObject {
        return GoodsReactiveObject(this.__v_raw, __v_isReadonly, __v_isShallow, __v_skip)
    }
    override var id: Number
        get() {
            return _tRG(__v_raw, "id", __v_raw.id, __v_isReadonly, __v_isShallow)
        }
        set(value) {
            if (!__v_canSet("id")) {
                return
            }
            val oldValue = __v_raw.id
            __v_raw.id = value
            _tRS(__v_raw, "id", oldValue, value)
        }
    override var name: String
        get() {
            return _tRG(__v_raw, "name", __v_raw.name, __v_isReadonly, __v_isShallow)
        }
        set(value) {
            if (!__v_canSet("name")) {
                return
            }
            val oldValue = __v_raw.name
            __v_raw.name = value
            _tRS(__v_raw, "name", oldValue, value)
        }
    override var price: Number
        get() {
            return _tRG(__v_raw, "price", __v_raw.price, __v_isReadonly, __v_isShallow)
        }
        set(value) {
            if (!__v_canSet("price")) {
                return
            }
            val oldValue = __v_raw.price
            __v_raw.price = value
            _tRS(__v_raw, "price", oldValue, value)
        }
    override var category: String
        get() {
            return _tRG(__v_raw, "category", __v_raw.category, __v_isReadonly, __v_isShallow)
        }
        set(value) {
            if (!__v_canSet("category")) {
                return
            }
            val oldValue = __v_raw.category
            __v_raw.category = value
            _tRS(__v_raw, "category", oldValue, value)
        }
    override var tag: String
        get() {
            return _tRG(__v_raw, "tag", __v_raw.tag, __v_isReadonly, __v_isShallow)
        }
        set(value) {
            if (!__v_canSet("tag")) {
                return
            }
            val oldValue = __v_raw.tag
            __v_raw.tag = value
            _tRS(__v_raw, "tag", oldValue, value)
        }
    override var desc: String
        get() {
            return _tRG(__v_raw, "desc", __v_raw.desc, __v_isReadonly, __v_isShallow)
        }
        set(value) {
            if (!__v_canSet("desc")) {
                return
            }
            val oldValue = __v_raw.desc
            __v_raw.desc = value
            _tRS(__v_raw, "desc", oldValue, value)
        }
}
open class Category (
    @JsonNotNull
    open var key: String,
    @JsonNotNull
    open var name: String,
) : UTSObject()
val categories = _uA(
    Category(key = "phone", name = "手机"),
    Category(key = "pc", name = "电脑"),
    Category(key = "audio", name = "音频"),
    Category(key = "home", name = "家电"),
    Category(key = "wear", name = "穿戴")
) as UTSArray<Category>
val goodsList = _uA(
    Goods(id = 1001, name = "梦曦 X1 Pro 智能手机", price = 4999, category = "phone", tag = "骁龙旗舰", desc = "6.7 英寸 2K 屏，骁龙 8 Gen3，5000mAh 大电池"),
    Goods(id = 1002, name = "梦曦 X1 标准版", price = 3999, category = "phone", tag = "轻薄", desc = "6.4 英寸直屏，旗舰影像，轻薄机身"),
    Goods(id = 1003, name = "梦曦 Note 入门机", price = 1299, category = "phone", tag = "性价比", desc = "大电池长续航，流畅日常使用"),
    Goods(id = 2001, name = "梦曦 Book 16 轻薄本", price = 5999, category = "pc", tag = "办公", desc = "16 英寸 2.5K 屏，32G 内存，全金属机身"),
    Goods(id = 2002, name = "梦曦 Pad 平板", price = 2799, category = "pc", tag = "影音", desc = "11 英寸 120Hz 高刷，四扬声器，手写笔支持"),
    Goods(id = 3001, name = "梦曦 Buds 降噪耳机", price = 699, category = "audio", tag = "降噪", desc = "主动降噪，30 小时续航，低延迟"),
    Goods(id = 3002, name = "梦曦 Sound 音箱", price = 399, category = "audio", tag = "便携", desc = "360° 环绕声，IPX7 防水，支持串联"),
    Goods(id = 4001, name = "梦曦 Air 变频空调", price = 3299, category = "home", tag = "一级能效", desc = "1.5 匹新一级能效，智能除湿，静音运行"),
    Goods(id = 4002, name = "梦曦 Fresh 空气净化器", price = 1499, category = "home", tag = "除醛", desc = "CADR 500³/h，实时 PM2.5 显示，静音睡眠模式"),
    Goods(id = 5001, name = "梦曦 Watch S 智能手表", price = 1599, category = "wear", tag = "健康", desc = "血氧/心率/睡眠监测，14 天续航"),
    Goods(id = 5002, name = "梦曦 Band 手环", price = 249, category = "wear", tag = "轻量", desc = "轻至 20g，50 米防水，30 天超长续航")
) as UTSArray<Goods>
fun goodsByCategory(key: String): UTSArray<Goods> {
    val result: UTSArray<Goods> = _uA()
    run {
        var i: Number = 0
        while(i < goodsList.length){
            if (goodsList[i].category == key) {
                result.push(goodsList[i])
            }
            i++
        }
    }
    return result
}
fun goodsById(id: Number): Goods? {
    run {
        var i: Number = 0
        while(i < goodsList.length){
            if (goodsList[i].id == id) {
                return goodsList[i]
            }
            i++
        }
    }
    return null
}
fun formatPrice(price: Number): String {
    return "¥" + Math.floor(price) + ""
}
open class CartItem (
    @JsonNotNull
    open var goods: Goods,
    @JsonNotNull
    open var count: Number,
) : UTSReactiveObject() {
    override fun __v_create(__v_isReadonly: Boolean, __v_isShallow: Boolean, __v_skip: Boolean): UTSReactiveObject {
        return CartItemReactiveObject(this, __v_isReadonly, __v_isShallow, __v_skip)
    }
}
class CartItemReactiveObject : CartItem, IUTSReactive<CartItem> {
    override var __v_raw: CartItem
    override var __v_isReadonly: Boolean
    override var __v_isShallow: Boolean
    override var __v_skip: Boolean
    constructor(__v_raw: CartItem, __v_isReadonly: Boolean, __v_isShallow: Boolean, __v_skip: Boolean) : super(goods = __v_raw.goods, count = __v_raw.count) {
        this.__v_raw = __v_raw
        this.__v_isReadonly = __v_isReadonly
        this.__v_isShallow = __v_isShallow
        this.__v_skip = __v_skip
    }
    override fun __v_clone(__v_isReadonly: Boolean, __v_isShallow: Boolean, __v_skip: Boolean): CartItemReactiveObject {
        return CartItemReactiveObject(this.__v_raw, __v_isReadonly, __v_isShallow, __v_skip)
    }
    override var goods: Goods
        get() {
            return _tRG(__v_raw, "goods", __v_raw.goods, __v_isReadonly, __v_isShallow)
        }
        set(value) {
            if (!__v_canSet("goods")) {
                return
            }
            val oldValue = __v_raw.goods
            __v_raw.goods = value
            _tRS(__v_raw, "goods", oldValue, value)
        }
    override var count: Number
        get() {
            return _tRG(__v_raw, "count", __v_raw.count, __v_isReadonly, __v_isShallow)
        }
        set(value) {
            if (!__v_canSet("count")) {
                return
            }
            val oldValue = __v_raw.count
            __v_raw.count = value
            _tRS(__v_raw, "count", oldValue, value)
        }
}
open class Order (
    @JsonNotNull
    open var id: String,
    @JsonNotNull
    open var items: UTSArray<CartItem>,
    @JsonNotNull
    open var total: Number,
    @JsonNotNull
    open var createdAt: String,
) : UTSReactiveObject() {
    override fun __v_create(__v_isReadonly: Boolean, __v_isShallow: Boolean, __v_skip: Boolean): UTSReactiveObject {
        return OrderReactiveObject(this, __v_isReadonly, __v_isShallow, __v_skip)
    }
}
class OrderReactiveObject : Order, IUTSReactive<Order> {
    override var __v_raw: Order
    override var __v_isReadonly: Boolean
    override var __v_isShallow: Boolean
    override var __v_skip: Boolean
    constructor(__v_raw: Order, __v_isReadonly: Boolean, __v_isShallow: Boolean, __v_skip: Boolean) : super(id = __v_raw.id, items = __v_raw.items, total = __v_raw.total, createdAt = __v_raw.createdAt) {
        this.__v_raw = __v_raw
        this.__v_isReadonly = __v_isReadonly
        this.__v_isShallow = __v_isShallow
        this.__v_skip = __v_skip
    }
    override fun __v_clone(__v_isReadonly: Boolean, __v_isShallow: Boolean, __v_skip: Boolean): OrderReactiveObject {
        return OrderReactiveObject(this.__v_raw, __v_isReadonly, __v_isShallow, __v_skip)
    }
    override var id: String
        get() {
            return _tRG(__v_raw, "id", __v_raw.id, __v_isReadonly, __v_isShallow)
        }
        set(value) {
            if (!__v_canSet("id")) {
                return
            }
            val oldValue = __v_raw.id
            __v_raw.id = value
            _tRS(__v_raw, "id", oldValue, value)
        }
    override var items: UTSArray<CartItem>
        get() {
            return _tRG(__v_raw, "items", __v_raw.items, __v_isReadonly, __v_isShallow)
        }
        set(value) {
            if (!__v_canSet("items")) {
                return
            }
            val oldValue = __v_raw.items
            __v_raw.items = value
            _tRS(__v_raw, "items", oldValue, value)
        }
    override var total: Number
        get() {
            return _tRG(__v_raw, "total", __v_raw.total, __v_isReadonly, __v_isShallow)
        }
        set(value) {
            if (!__v_canSet("total")) {
                return
            }
            val oldValue = __v_raw.total
            __v_raw.total = value
            _tRS(__v_raw, "total", oldValue, value)
        }
    override var createdAt: String
        get() {
            return _tRG(__v_raw, "createdAt", __v_raw.createdAt, __v_isReadonly, __v_isShallow)
        }
        set(value) {
            if (!__v_canSet("createdAt")) {
                return
            }
            val oldValue = __v_raw.createdAt
            __v_raw.createdAt = value
            _tRS(__v_raw, "createdAt", oldValue, value)
        }
}
@JvmField
var loggedIn: Boolean = false
fun setLoggedIn(value: Boolean): Unit {
    loggedIn = value
}
fun isLoggedIn(): Boolean {
    return loggedIn
}
val cartItems = reactive(_uA<CartItem>())
fun cartTotalCount(): Number {
    var total: Number = 0
    run {
        var i: Number = 0
        while(i < cartItems.length){
            total += cartItems[i].count
            i++
        }
    }
    return total
}
fun cartTotalPrice(): Number {
    var total: Number = 0
    run {
        var i: Number = 0
        while(i < cartItems.length){
            total += cartItems[i].goods.price * cartItems[i].count
            i++
        }
    }
    return total
}
fun addToCart(goods: Goods): Unit {
    run {
        var i: Number = 0
        while(i < cartItems.length){
            if (cartItems[i].goods.id == goods.id) {
                cartItems[i].count += 1
                return
            }
            i++
        }
    }
    cartItems.push(CartItem(goods = goods, count = 1))
}
fun changeCartCount(goodsId: Number, delta: Number): Unit {
    run {
        var i: Number = 0
        while(i < cartItems.length){
            if (cartItems[i].goods.id == goodsId) {
                cartItems[i].count += delta
                if (cartItems[i].count <= 0) {
                    cartItems.splice(i, 1)
                }
                return
            }
            i++
        }
    }
}
fun clearCart(): Unit {
    cartItems.splice(0, cartItems.length)
}
val orders = reactive(_uA<Order>())
val router = createRouter(RouterOptions(routes = routes, strict = true, plugins = _uA(
    ParamsPlugin(),
    InterceptorPlugin(),
    AnimationPlugin(),
    EventsPlugin()
), interceptUniApi = true, animation = NavigationAnimation(type = "slide-in-right", duration = 300)))
val runBlock2 = run {
    router.beforeEach(fun(to, from): Any {
        if (to.meta.requireAuth === true && !isLoggedIn()) {
            return _uO("name" to "login", "query" to Map<String, String>(_uA(
                _uA(
                    "redirect",
                    to.fullPath
                )
            ))) as Any
        }
        return true
    }
    )
    router.beforeResolve(fun(to, from): Any {
        console.log("[unix-router] beforeResolve ->", to.fullPath)
        return true
    }
    )
    router.afterEach(fun(to, from, failure: UTSError?): Unit {
        console.log("[unix-router] afterEach ->", to.fullPath, "failure =", if (failure != null) {
            failure.message
        } else {
            "null"
        }
        )
    }
    )
    router.onError(fun(error: UTSError, to, from): Unit {
        console.log("[unix-router] onError ->", error.message)
    }
    )
}
val GenPagesIndexIndexClass = CreateVueComponent(GenPagesIndexIndex::class.java, fun(): VueComponentOptions {
    return VueComponentOptions(type = "page", name = "", inheritAttrs = GenPagesIndexIndex.inheritAttrs, inject = GenPagesIndexIndex.inject, props = GenPagesIndexIndex.props, propsNeedCastKeys = GenPagesIndexIndex.propsNeedCastKeys, emits = GenPagesIndexIndex.emits, components = GenPagesIndexIndex.components, styles = GenPagesIndexIndex.styles, setup = fun(props: ComponentPublicInstance): Any? {
        return GenPagesIndexIndex.setup(props as GenPagesIndexIndex)
    }
    )
}
, fun(instance, renderer): GenPagesIndexIndex {
    return GenPagesIndexIndex(instance, renderer)
}
)
val GenPagesCartCartClass = CreateVueComponent(GenPagesCartCart::class.java, fun(): VueComponentOptions {
    return VueComponentOptions(type = "page", name = "", inheritAttrs = GenPagesCartCart.inheritAttrs, inject = GenPagesCartCart.inject, props = GenPagesCartCart.props, propsNeedCastKeys = GenPagesCartCart.propsNeedCastKeys, emits = GenPagesCartCart.emits, components = GenPagesCartCart.components, styles = GenPagesCartCart.styles, setup = fun(props: ComponentPublicInstance): Any? {
        return GenPagesCartCart.setup(props as GenPagesCartCart)
    }
    )
}
, fun(instance, renderer): GenPagesCartCart {
    return GenPagesCartCart(instance, renderer)
}
)
val GenPagesCategoryCategoryClass = CreateVueComponent(GenPagesCategoryCategory::class.java, fun(): VueComponentOptions {
    return VueComponentOptions(type = "page", name = "", inheritAttrs = GenPagesCategoryCategory.inheritAttrs, inject = GenPagesCategoryCategory.inject, props = GenPagesCategoryCategory.props, propsNeedCastKeys = GenPagesCategoryCategory.propsNeedCastKeys, emits = GenPagesCategoryCategory.emits, components = GenPagesCategoryCategory.components, styles = GenPagesCategoryCategory.styles, setup = fun(props: ComponentPublicInstance): Any? {
        return GenPagesCategoryCategory.setup(props as GenPagesCategoryCategory)
    }
    )
}
, fun(instance, renderer): GenPagesCategoryCategory {
    return GenPagesCategoryCategory(instance, renderer)
}
)
val GenPagesLoginLoginClass = CreateVueComponent(GenPagesLoginLogin::class.java, fun(): VueComponentOptions {
    return VueComponentOptions(type = "page", name = "", inheritAttrs = GenPagesLoginLogin.inheritAttrs, inject = GenPagesLoginLogin.inject, props = GenPagesLoginLogin.props, propsNeedCastKeys = GenPagesLoginLogin.propsNeedCastKeys, emits = GenPagesLoginLogin.emits, components = GenPagesLoginLogin.components, styles = GenPagesLoginLogin.styles, setup = fun(props: ComponentPublicInstance): Any? {
        return GenPagesLoginLogin.setup(props as GenPagesLoginLogin)
    }
    )
}
, fun(instance, renderer): GenPagesLoginLogin {
    return GenPagesLoginLogin(instance, renderer)
}
)
val GenPagesMineMineClass = CreateVueComponent(GenPagesMineMine::class.java, fun(): VueComponentOptions {
    return VueComponentOptions(type = "page", name = "", inheritAttrs = GenPagesMineMine.inheritAttrs, inject = GenPagesMineMine.inject, props = GenPagesMineMine.props, propsNeedCastKeys = GenPagesMineMine.propsNeedCastKeys, emits = GenPagesMineMine.emits, components = GenPagesMineMine.components, styles = GenPagesMineMine.styles, setup = fun(props: ComponentPublicInstance): Any? {
        return GenPagesMineMine.setup(props as GenPagesMineMine)
    }
    )
}
, fun(instance, renderer): GenPagesMineMine {
    return GenPagesMineMine(instance, renderer)
}
)
val GenPagesSubCheckoutCheckoutClass = CreateVueComponent(GenPagesSubCheckoutCheckout::class.java, fun(): VueComponentOptions {
    return VueComponentOptions(type = "page", name = "", inheritAttrs = GenPagesSubCheckoutCheckout.inheritAttrs, inject = GenPagesSubCheckoutCheckout.inject, props = GenPagesSubCheckoutCheckout.props, propsNeedCastKeys = GenPagesSubCheckoutCheckout.propsNeedCastKeys, emits = GenPagesSubCheckoutCheckout.emits, components = GenPagesSubCheckoutCheckout.components, styles = GenPagesSubCheckoutCheckout.styles, setup = fun(props: ComponentPublicInstance): Any? {
        return GenPagesSubCheckoutCheckout.setup(props as GenPagesSubCheckoutCheckout)
    }
    )
}
, fun(instance, renderer): GenPagesSubCheckoutCheckout {
    return GenPagesSubCheckoutCheckout(instance, renderer)
}
)
val GenPagesSubDemoDemoClass = CreateVueComponent(GenPagesSubDemoDemo::class.java, fun(): VueComponentOptions {
    return VueComponentOptions(type = "page", name = "", inheritAttrs = GenPagesSubDemoDemo.inheritAttrs, inject = GenPagesSubDemoDemo.inject, props = GenPagesSubDemoDemo.props, propsNeedCastKeys = GenPagesSubDemoDemo.propsNeedCastKeys, emits = GenPagesSubDemoDemo.emits, components = GenPagesSubDemoDemo.components, styles = GenPagesSubDemoDemo.styles, setup = fun(props: ComponentPublicInstance): Any? {
        return GenPagesSubDemoDemo.setup(props as GenPagesSubDemoDemo)
    }
    )
}
, fun(instance, renderer): GenPagesSubDemoDemo {
    return GenPagesSubDemoDemo(instance, renderer)
}
)
val GenPagesSubGoodsDetailClass = CreateVueComponent(GenPagesSubGoodsDetail::class.java, fun(): VueComponentOptions {
    return VueComponentOptions(type = "page", name = "", inheritAttrs = GenPagesSubGoodsDetail.inheritAttrs, inject = GenPagesSubGoodsDetail.inject, props = GenPagesSubGoodsDetail.props, propsNeedCastKeys = GenPagesSubGoodsDetail.propsNeedCastKeys, emits = GenPagesSubGoodsDetail.emits, components = GenPagesSubGoodsDetail.components, styles = GenPagesSubGoodsDetail.styles, setup = fun(props: ComponentPublicInstance): Any? {
        return GenPagesSubGoodsDetail.setup(props as GenPagesSubGoodsDetail)
    }
    )
}
, fun(instance, renderer): GenPagesSubGoodsDetail {
    return GenPagesSubGoodsDetail(instance, renderer)
}
)
val GenPagesSubGoodsListClass = CreateVueComponent(GenPagesSubGoodsList::class.java, fun(): VueComponentOptions {
    return VueComponentOptions(type = "page", name = "", inheritAttrs = GenPagesSubGoodsList.inheritAttrs, inject = GenPagesSubGoodsList.inject, props = GenPagesSubGoodsList.props, propsNeedCastKeys = GenPagesSubGoodsList.propsNeedCastKeys, emits = GenPagesSubGoodsList.emits, components = GenPagesSubGoodsList.components, styles = GenPagesSubGoodsList.styles, setup = fun(props: ComponentPublicInstance): Any? {
        return GenPagesSubGoodsList.setup(props as GenPagesSubGoodsList)
    }
    )
}
, fun(instance, renderer): GenPagesSubGoodsList {
    return GenPagesSubGoodsList(instance, renderer)
}
)
val GenPagesSubOrdersOrdersClass = CreateVueComponent(GenPagesSubOrdersOrders::class.java, fun(): VueComponentOptions {
    return VueComponentOptions(type = "page", name = "", inheritAttrs = GenPagesSubOrdersOrders.inheritAttrs, inject = GenPagesSubOrdersOrders.inject, props = GenPagesSubOrdersOrders.props, propsNeedCastKeys = GenPagesSubOrdersOrders.propsNeedCastKeys, emits = GenPagesSubOrdersOrders.emits, components = GenPagesSubOrdersOrders.components, styles = GenPagesSubOrdersOrders.styles, setup = fun(props: ComponentPublicInstance): Any? {
        return GenPagesSubOrdersOrders.setup(props as GenPagesSubOrdersOrders)
    }
    )
}
, fun(instance, renderer): GenPagesSubOrdersOrders {
    return GenPagesSubOrdersOrders(instance, renderer)
}
)
val GenPagesSubSettingSettingClass = CreateVueComponent(GenPagesSubSettingSetting::class.java, fun(): VueComponentOptions {
    return VueComponentOptions(type = "page", name = "", inheritAttrs = GenPagesSubSettingSetting.inheritAttrs, inject = GenPagesSubSettingSetting.inject, props = GenPagesSubSettingSetting.props, propsNeedCastKeys = GenPagesSubSettingSetting.propsNeedCastKeys, emits = GenPagesSubSettingSetting.emits, components = GenPagesSubSettingSetting.components, styles = GenPagesSubSettingSetting.styles, setup = fun(props: ComponentPublicInstance): Any? {
        return GenPagesSubSettingSetting.setup(props as GenPagesSubSettingSetting)
    }
    )
}
, fun(instance, renderer): GenPagesSubSettingSetting {
    return GenPagesSubSettingSetting(instance, renderer)
}
)
val GenPagesSubUserProfileClass = CreateVueComponent(GenPagesSubUserProfile::class.java, fun(): VueComponentOptions {
    return VueComponentOptions(type = "page", name = "", inheritAttrs = GenPagesSubUserProfile.inheritAttrs, inject = GenPagesSubUserProfile.inject, props = GenPagesSubUserProfile.props, propsNeedCastKeys = GenPagesSubUserProfile.propsNeedCastKeys, emits = GenPagesSubUserProfile.emits, components = GenPagesSubUserProfile.components, styles = GenPagesSubUserProfile.styles, setup = fun(props: ComponentPublicInstance): Any? {
        return GenPagesSubUserProfile.setup(props as GenPagesSubUserProfile)
    }
    )
}
, fun(instance, renderer): GenPagesSubUserProfile {
    return GenPagesSubUserProfile(instance, renderer)
}
)
fun createApp(): UTSJSONObject {
    val app = createSSRApp(GenAppClass)
    app.use(router)
    return _uO("app" to app)
}
fun main(app: IApp) {
    enableStyleIsolation()
    definePageRoutes()
    defineAppConfig()
    (createApp()["app"] as VueApp).mount(app, GenUniApp())
}
open class UniAppConfig : io.dcloud.uniapp.appframe.AppConfig {
    override var name: String = "unix-router"
    override var appid: String = "__UNI__B6A50A8"
    override var versionName: String = "0.5.0"
    override var versionCode: String = "050"
    override var uniCompilerVersion: String = "5.26"
    constructor() : super() {}
}
fun definePageRoutes() {
    __uniRoutes.push(UniPageRoute(path = "pages/index/index", component = GenPagesIndexIndexClass, meta = UniPageMeta(isQuit = true), style = _uM("navigationBarTitleText" to "梦曦商城")))
    __uniRoutes.push(UniPageRoute(path = "pages/cart/cart", component = GenPagesCartCartClass, meta = UniPageMeta(isQuit = false), style = _uM("navigationBarTitleText" to "购物车")))
    __uniRoutes.push(UniPageRoute(path = "pages/category/category", component = GenPagesCategoryCategoryClass, meta = UniPageMeta(isQuit = false), style = _uM("navigationBarTitleText" to "分类")))
    __uniRoutes.push(UniPageRoute(path = "pages/login/login", component = GenPagesLoginLoginClass, meta = UniPageMeta(isQuit = false), style = _uM()))
    __uniRoutes.push(UniPageRoute(path = "pages/mine/mine", component = GenPagesMineMineClass, meta = UniPageMeta(isQuit = false), style = _uM("navigationBarTitleText" to "我的")))
    __uniRoutes.push(UniPageRoute(path = "pages-sub/checkout/checkout", component = GenPagesSubCheckoutCheckoutClass, meta = UniPageMeta(isQuit = false), style = _uM("navigationBarTitleText" to "确认订单")))
    __uniRoutes.push(UniPageRoute(path = "pages-sub/demo/demo", component = GenPagesSubDemoDemoClass, meta = UniPageMeta(isQuit = false), style = _uM("navigationBarTitleText" to "路由能力演示")))
    __uniRoutes.push(UniPageRoute(path = "pages-sub/goods/detail", component = GenPagesSubGoodsDetailClass, meta = UniPageMeta(isQuit = false), style = _uM("navigationBarTitleText" to "商品详情")))
    __uniRoutes.push(UniPageRoute(path = "pages-sub/goods/list", component = GenPagesSubGoodsListClass, meta = UniPageMeta(isQuit = false), style = _uM("navigationBarTitleText" to "商品列表")))
    __uniRoutes.push(UniPageRoute(path = "pages-sub/orders/orders", component = GenPagesSubOrdersOrdersClass, meta = UniPageMeta(isQuit = false), style = _uM("navigationBarTitleText" to "我的订单")))
    __uniRoutes.push(UniPageRoute(path = "pages-sub/setting/setting", component = GenPagesSubSettingSettingClass, meta = UniPageMeta(isQuit = false), style = _uM("navigationBarTitleText" to "设置")))
    __uniRoutes.push(UniPageRoute(path = "pages-sub/user/profile", component = GenPagesSubUserProfileClass, meta = UniPageMeta(isQuit = false), style = _uM("navigationBarTitleText" to "个人资料")))
}
val __uniTabBar: Map<String, Any?>? = _uM("color" to "#7A7E83", "selectedColor" to "#007AFF", "backgroundColor" to "#FFFFFF", "list" to _uA(
    _uM("pagePath" to "pages/index/index", "text" to "首页", "iconPath" to "static/tabbar/home.png", "selectedIconPath" to "static/tabbar/home-active.png"),
    _uM("pagePath" to "pages/category/category", "text" to "分类", "iconPath" to "static/tabbar/category.jpg", "selectedIconPath" to "static/tabbar/category-active.jpg"),
    _uM("pagePath" to "pages/mine/mine", "text" to "我的", "iconPath" to "static/tabbar/mine.png", "selectedIconPath" to "static/tabbar/mine-active.png")
))
val __uniLaunchPage: Map<String, Any?> = _uM("url" to "pages/index/index", "style" to _uM("navigationBarTitleText" to "梦曦商城"))
fun defineAppConfig() {
    __uniConfig.entryPagePath = "/pages/index/index"
    __uniConfig.globalStyle = _uM("navigationBarTextStyle" to "black", "navigationBarTitleText" to "梦曦商城", "navigationBarBackgroundColor" to "#F8F8F8", "backgroundColor" to "#F8F8F8")
    __uniConfig.getTabBarConfig = fun(): Map<String, Any>? {
        return _uM("color" to "#7A7E83", "selectedColor" to "#007AFF", "backgroundColor" to "#FFFFFF", "list" to _uA(
            _uM("pagePath" to "pages/index/index", "text" to "首页", "iconPath" to "static/tabbar/home.png", "selectedIconPath" to "static/tabbar/home-active.png"),
            _uM("pagePath" to "pages/category/category", "text" to "分类", "iconPath" to "static/tabbar/category.jpg", "selectedIconPath" to "static/tabbar/category-active.jpg"),
            _uM("pagePath" to "pages/mine/mine", "text" to "我的", "iconPath" to "static/tabbar/mine.png", "selectedIconPath" to "static/tabbar/mine-active.png")
        ))
    }
    __uniConfig.tabBar = __uniConfig.getTabBarConfig()
    __uniConfig.conditionUrl = ""
    __uniConfig.uniIdRouter = _uM()
    __uniConfig.ready = true
}
open class GenUniApp : UniAppImpl() {
    open val vm: GenApp?
        get() {
            return getAppVm() as GenApp?
        }
    open val `$vm`: GenApp?
        get() {
            return getAppVm() as GenApp?
        }
}
fun getApp(): GenUniApp {
    return getUniApp() as GenUniApp
}
