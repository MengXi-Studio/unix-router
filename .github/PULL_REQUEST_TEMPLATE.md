## 背景
<!-- 为什么做这个变更（问题 / 需求来源） -->

## 变更内容
<!-- 做了什么（模块 / 关键决策）；若改动 packages/core/src，说明是否已同步 uni_modules 镜像 -->

## 关联 Issue
Closes #

## 自测清单
- [ ] pnpm lint 通过
- [ ] pnpm typecheck 通过
- [ ] pnpm sync:uni-modules:check 通过（双发行版镜像无漂移）
- [ ] pnpm build:docs 通过
- [ ] core 源码变更已执行 pnpm sync:uni-modules 同步镜像
- [ ] H5 运行时验证通过（dev server 复现目标路径，控制台无报错）
- [ ] App 编译级验证通过（涉及运行时 / 类型变更时，HBuilderX 云打包或本地编译无 Kotlin 类型错误）
