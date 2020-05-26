'use strict';

module.exports = {
  plugins: ['ember-template-lint-plugin-tailwindcss'],
  extends: ['octane', "ember-template-lint-plugin-tailwindcss:recommended"],
  rules: {
    'self-closing-void-elements': false,
    'no-curly-component-invocation': { allow: ['group.name', 'not', 'html-safe', 'disablePlayButton', 'style-namespace', 'link-to'] },
    'no-implicit-this': { allow: ['styleNamespace', 'data-test-logo', 'data-test-series-navigation-list-link', 'data-test-group-link', 'data-test-series-navigation-list-link'] }
  }
};
