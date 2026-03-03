'use strict';

const path = require('path');

module.exports = {
  reporters: ['html', 'lcov', 'json-summary'],
  excludes: ['*/mirage/**/*'],
  coverageFolder: 'coverage',

  // With Embroider + Vite, babel-plugin-istanbul stores absolute filesystem
  // paths in window.__coverage__ (e.g. /path/to/frontend/app/components/foo.js).
  // The default adjustCoverageKey logic expects Ember module-style names
  // (e.g. brn/components/foo) and fails to map them correctly.
  // This hook returns the correct absolute path so coverage data is preserved.
  modifyAssetLocation(root, relativePath, filepath) {
    // relativePath is already correct (e.g. "app/components/foo.js"),
    // return the absolute path so the relative-to-root calculation works.
    return path.join(root, relativePath);
  },
};
