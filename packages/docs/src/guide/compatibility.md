# 平台兼容性

unix-router 以 **UTS**（`.uts`）编写，由 uni-app x 编译链按平台现场编译。

## 编译产物

| 平台 | 编译为 | 是否支持 |
| --- | --- | --- |
| Web / H5 | JavaScript | ✅ |
| 微信小程序 | JavaScript | ✅ |
| Android（VDOM / 蒸汽模式） | Kotlin / JS | ✅ |
| iOS（VDOM） | Swift / JS | ✅ |
| App-Android（VDOM 原生） | Kotlin | ✅（经 UTS 编译） |

> 依据官方规则：目标语言为 JS 时直接引用 ts/js；非 JS（Android）时仅可引用 ts 文件并当作 UTS 处理。因此本库以 `.uts` 源分发，确保全端可编译。

## 导航 API 平台差异

底层仅依赖 `uni.navigateTo / redirectTo / reLaunch / navigateBack / switchTab`，各平台由 uni-app x 原生适配层抹平。

- `switchTab`（tabBar 页）：各端一致。
- App 返回键 / 侧滑：不经过路由器，由 `syncRoute()` 在 `onShow` 同步。

## 运行模式

- **VDOM 模式**（第一代）：脚本编译为 Kotlin/Swift，依赖 UTS 强类型 → 本库已适配。
- **蒸汽模式**（2026 起新一代）：全端运行 JS → 本库同样可运行。

两种模式均可直接使用，无需改动代码。

## 验证方式

- Web / 小程序：`packages/playground` 内 `pages/test` 自检页输出 PASS/FAIL。
- App 原生：HBuilderX 打开 playground 打包验证。