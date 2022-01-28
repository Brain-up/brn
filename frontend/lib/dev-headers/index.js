'use strict';

module.exports = {
  // eslint-disable-next-line @typescript-eslint/no-var-requires
  name: require('./package').name,

  serverMiddleware(startOptions) {
    var app = startOptions.app;

    app.use(function(_, res, next) {
      res.set({
        'Cross-Origin-Opener-Policy': 'same-origin',
        'Cross-Origin-Embedder-Policy': 'require-corp'
      });
      next();
    });
  },

  isDevelopingAddon() {
    return true;
  }
};
