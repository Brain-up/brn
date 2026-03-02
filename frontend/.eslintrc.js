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
    },
    {
      files: ['**/*.gjs'],
      parser: 'ember-eslint-parser',
      extends: [
        'plugin:ember/recommended',
        'prettier',
      ],
    },
    // node files
    {
      files: [
        '.eslintrc.js',
        '.ember-cli.js',
        '.template-lintrc.js',
        'ember-cli-build.js',
        'vite.config.mjs',
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
    }
  ]
};
