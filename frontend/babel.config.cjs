const {
  babelCompatSupport,
  templateCompatSupport,
} = require('@embroider/compat/babel');
const plugins = [
  ['@babel/plugin-transform-typescript', { allowDeclareFields: true }],
  [
    'babel-plugin-ember-template-compilation',
    {
      compilerPath: 'ember-source/dist/ember-template-compiler.js',
      enableLegacyModules: [
        'ember-cli-htmlbars',
        'ember-cli-htmlbars-inline-precompile',
        'htmlbars-inline-precompile',
      ],
      transforms: [...templateCompatSupport()],
    },
  ],
  [
    'module:decorator-transforms',
    {
      runtime: {
        import: require.resolve('decorator-transforms/runtime-esm'),
      },
    },
  ],
  [
    '@babel/plugin-transform-runtime',
    {
      absoluteRuntime: __dirname,
      useESModules: true,
      regenerator: false,
    },
  ],
  ...babelCompatSupport(),
  ['ember-concurrency/async-arrow-task-transform'],
];

// When COVERAGE=true, add Istanbul instrumentation plugins for code coverage.
// This inserts babel-plugin-istanbul which populates window.__coverage__ at runtime.
if (process.env.COVERAGE === 'true') {
  const path = require('path');

  plugins.push(
    // Adds /* istanbul ignore next */ comments above template() eval calls
    // in .gjs/.gts files to avoid skewing coverage results.
    path.resolve(
      __dirname,
      'node_modules/ember-cli-code-coverage/lib/gjs-gts-istanbul-ignore-template-plugin'
    ),
    // Istanbul instrumentation plugin that tracks code coverage.
    [
      require.resolve('babel-plugin-istanbul'),
      {
        cwd: __dirname,
        include: ['app/**/*'],
        exclude: ['node_modules/**/*', 'tests/**/*', '*/mirage/**/*'],
        extension: ['.gjs', '.gts', '.js', '.ts'],
      },
    ]
  );
}

module.exports = {
  plugins,
  generatorOpts: {
    compact: false,
  },
};
