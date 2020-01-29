'use strict';

module.exports = {
  extends: 'octane',
  rules: {
    'self-closing-void-elements': false,
    'no-curly-component-invocation': { allow: ['group.name', 'not', 'html-safe', 'disablePlayButton'] },
    'no-implicit-this': { allow: ['data-test-logo', 'data-test-series-navigation-list-link', 'data-test-group-link', 'data-test-series-navigation-list-link'] }
  }
};
