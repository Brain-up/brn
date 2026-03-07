module.exports = {
  root: true,
  parser: '@typescript-eslint/parser',
  parserOptions: {
    ecmaVersion: 2020,
    sourceType: 'module',
    ecmaFeatures: {
      legacyDecorators: true
    }
  },
  plugins: [
    'ember'
  ],
  extends: [
    'plugin:@typescript-eslint/recommended',
    'plugin:ember/recommended',
    'prettier',
  ],
  env: {
    browser: true
  },
  rules: {
    'ember/no-jquery': 'error',
    'ember/require-tagless-components': 'off',
    'no-unused-vars': 'off',
    '@typescript-eslint/explicit-module-boundary-types': 'off',
    '@typescript-eslint/no-explicit-any': 'off',
    '@typescript-eslint/no-unused-vars': ['error', { argsIgnorePattern: '^_', varsIgnorePattern: '^_', caughtErrorsIgnorePattern: '^_' }],
    '@typescript-eslint/no-empty-object-type': 'off',
    '@typescript-eslint/no-require-imports': 'off',
    '@typescript-eslint/no-unused-expressions': 'off',
    'ember/no-at-ember-render-modifiers': 'off',
    'ember/no-runloop': 'off',
    'prefer-rest-params': 'off'
  },
  globals: {
    'server': 'readable'
  },
  overrides: [
    // gts/gjs files
    {
      files: ['**/*.gts'],
      parser: 'ember-eslint-parser',
      extends: [
        'plugin:@typescript-eslint/recommended',
        'plugin:ember/recommended',
        'prettier',
      ],
      rules: {
        '@typescript-eslint/no-explicit-any': 'off',
        '@typescript-eslint/no-unused-vars': ['error', { argsIgnorePattern: '^_', varsIgnorePattern: '^_', caughtErrorsIgnorePattern: '^_' }],
        '@typescript-eslint/no-empty-object-type': 'off',
        '@typescript-eslint/no-require-imports': 'off',
        '@typescript-eslint/no-unused-expressions': 'off',
        'ember/no-at-ember-render-modifiers': 'off',
        'ember/no-runloop': 'off',
      },
    },
    {
      files: ['**/*.gjs'],
      parser: 'ember-eslint-parser',
      extends: [
        'plugin:ember/recommended',
        'prettier',
      ],
      rules: {
        '@typescript-eslint/no-unused-vars': ['error', { argsIgnorePattern: '^_', varsIgnorePattern: '^_', caughtErrorsIgnorePattern: '^_' }],
        'ember/no-at-ember-render-modifiers': 'off',
        'ember/no-runloop': 'off',
      },
    },
    // test files
    {
      files: ['tests/**/*.{gjs,gts,js,ts}'],
      rules: {
        '@typescript-eslint/ban-ts-comment': 'off',
        // Ember .gjs tests use `const self = this` to pass test context into <template> tags
        '@typescript-eslint/no-this-alias': 'off',
      },
    },
    // node files
    {
      files: [
        '.eslintrc.js',
        '.ember-cli.js',
        '.template-lintrc.js',
        'testem.js',
        'blueprints/*/index.js',
        'config/**/*.js',
        'lib/*/index.js',
        'server/**/*.js'
      ],
      parserOptions: {
        sourceType: 'script'
      },
      env: {
        browser: false,
        node: true
      },
      plugins: ['node'],
      // eslint-disable-next-line @typescript-eslint/no-var-requires
      rules: Object.assign({}, require('eslint-plugin-node').configs.recommended.rules, {
        'node/no-unpublished-require': 'off'
      })
    },
    // ember-cli-build (CJS with dynamic import)
    {
      files: ['ember-cli-build.js'],
      parserOptions: {
        sourceType: 'script',
        ecmaVersion: 2020,
      },
      env: {
        browser: false,
        node: true
      },
      plugins: ['node'],
      rules: Object.assign({}, require('eslint-plugin-node').configs.recommended.rules, {
        'node/no-unpublished-require': 'off',
        'node/no-missing-require': 'off',
        'node/no-missing-import': 'off',
        'node/no-unpublished-import': 'off',
        'node/no-unsupported-features/es-syntax': 'off',
      })
    },
    // vite config (ESM)
    {
      files: ['vite.config.mjs'],
      env: {
        browser: false,
        node: true
      },
      rules: {
        '@typescript-eslint/no-unused-vars': ['error', { argsIgnorePattern: '^_', varsIgnorePattern: '^_', caughtErrorsIgnorePattern: '^_' }],
      },
    }
  ]
};
