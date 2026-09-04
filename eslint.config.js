import eslint from '@eslint/js'
import tseslint from 'typescript-eslint'
import eslintConfigPrettier from 'eslint-config-prettier'
import eslintPluginPrettier from 'eslint-plugin-prettier'
import globals from 'globals'

export default tseslint.config(
	// 全局忽略配置
	{
		ignores: [
			// 依赖目录
			'**/node_modules/',
			// 构建输出目录
			'**/dist/',
			'**/unpackage/',
			// Git 目录
			'.git/',
			// 包管理锁文件
			'pnpm-lock.yaml',
			'package-lock.json',
			'yarn.lock',
			// 编辑器配置文件
			'.vscode/',
			'.idea/',
			'**/*.swp',
			'**/*.swo',
			'**/*~',
			// 环境变量文件
			'.env',
			'.env.local',
			'.env.*.local',
			// 日志文件
			'logs/',
			'**/*.log',
			'npm-debug.log*',
			'yarn-debug.log*',
			'yarn-error.log*',
			'pnpm-debug.log*',
			'lerna-debug.log*',
			// OS 生成的文件
			'.DS_Store',
			'Thumbs.db',
			// uni-app x 源码与声明：由 HBuilderX / uni-app x 编译链处理，不参与 ESLint
			'**/*.uts',
			'**/*.uvue',
			'**/*.d.ts',
			'**/*.d.uts',
			// 文档与其他
			'**/*.md',
			'**/*.json'
		]
	},
	// ESLint 推荐规则
	eslint.configs.recommended,
	// TypeScript ESLint 推荐规则
	...tseslint.configs.recommended,
	// Prettier 配置（禁用与 Prettier 冲突的规则）
	eslintConfigPrettier,
	// 自定义配置
	{
		languageOptions: {
			ecmaVersion: 2022,
			sourceType: 'module',
			globals: {
				...globals.node,
				...globals.browser,
				...globals.es2021
			}
		},
		plugins: {
			prettier: eslintPluginPrettier
		},
		rules: {
			// 基本规则
			'no-console': 'off',
			'no-unused-vars': 'off',
			'@typescript-eslint/no-unused-vars': ['warn', { argsIgnorePattern: '^_', varsIgnorePattern: '^_' }],
			'no-undef': 'off',
			'prettier/prettier': 'error',
			'@typescript-eslint/no-explicit-any': 'off'
		}
	}
)
