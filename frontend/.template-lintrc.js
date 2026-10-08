'use strict';

module.exports = {
  extends: ['recommended'],
  rules: {
    'self-closing-void-elements': false,
    'no-curly-component-invocation': { allow: ['group.name', 'not', 'html-safe', 'disablePlayButton', 'link-to'] },
    'no-implicit-this': { allow: ['data-test-logo', 'data-test-series-navigation-list-link', 'data-test-group-link'] },
    'no-inline-styles': false,
    'style-concatenation': false,
    'no-unused-block-params': false,
    'no-heading-inside-button': false,
    'require-input-label': false,
    'no-duplicate-id': false,
    'no-negated-condition': false
  }
};
