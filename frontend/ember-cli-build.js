'use strict';

const EmberApp = require('ember-cli/lib/broccoli/ember-app');
const CssImport = require('postcss-import');
const tailwindcss = require('tailwindcss');

module.exports = function (defaults) {
  let app = new EmberApp(defaults, {
    'ember-test-selectors': {
      strip: false
    },
    fingerprint: {
      exclude: ['pictures/'],
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
          {
            module: tailwindcss
          }]
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

  app.import('node_modules/idle-js/dist/Idle.js');

  return app.toTree();
};
