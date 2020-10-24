'use strict';

const EmberApp = require('ember-cli/lib/broccoli/ember-app');
const CssImport = require('postcss-import');
const tailwindcss = require('tailwindcss');
const isProduction = EmberApp.env() === 'production';

const purgeCSS = {
  module: require('@fullhuman/postcss-purgecss'),
  options: {
    content: [
      // add extra paths here for components/controllers which include tailwind classes
      './app/index.html',
      './app/templates/**/*.hbs',
      './app/components/**/*.hbs',
      './app/components/**/*.css'
    ],
    defaultExtractor: content => content.match(/[A-Za-z0-9-_:/]+/g) || content.match(/[\w-/.:]+(?<!:)/g) || []
  }
}


module.exports = function (defaults) {
  let app = new EmberApp(defaults, {
    babel: {
      plugins: [ require.resolve('ember-auto-import/babel-plugin') ]
    },
    'ember-test-selectors': {
      strip: false
    },
    fingerprint: {
      exclude: ['pictures/'],
    },
    'ember-cli-babel': {
      enableTypeScriptTransform: true
    },
    postcssOptions: {
      compile: {
        enabled: true,
        plugins: [
          {
            module: CssImport,
            options: {
              path: ['node_modules']
            }
          },
          tailwindcss('./app/styles/tailwind.js'),
          ...isProduction ? [purgeCSS] : []
        ]
      }
    }
  });

  // Use `app.import` to add additional libraries to the generated
  // output files.
  //
  // If you need to use different assets in different
  // environments, specify an object as the first parameter. That
  // object's keys should be the environment name and the values
  // should be the asset to use in that environment.
  //
  // If the library that you are including contains AMD or ES6
  // modules that you would like to import into your application
  // please specify an object with the list of modules as keys
  // along with the exports of each module as its value.
  return app.toTree();
};
