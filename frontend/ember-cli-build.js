'use strict';

const EmberApp = require('ember-cli/lib/broccoli/ember-app');
const { compatBuild } = require('@embroider/compat');

module.exports = async function (defaults) {
  const { buildOnce } = await import('@embroider/vite');
  const { setConfig } = await import('@warp-drive/build-config');

  const app = new EmberApp(defaults, {
    'ember-cli-babel': {
      enableTypeScriptTransform: true,
    },
    'ember-test-selectors': {
      strip: false,
    },
  });

  setConfig(app, __dirname, {
    compatWith: '6.8',
  });

  return compatBuild(app, buildOnce);
};
