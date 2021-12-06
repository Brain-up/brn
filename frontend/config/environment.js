'use strict';

const firebaseProjectId = process.env.FIREBASE_PROJECT_ID || 'brainupspb';
const firebaseAuthDomain =
  process.env.FIREBASE_AUTH_DOMAIN || 'brainupspb.firebaseapp.com';
const firebaseAPIKey =
  process.env.FIREBASE_API_KEY || 'AIzaSyCxu7mVxd_waBDUn9VKblBl4zl8MX5WxWY';

module.exports = function (environment) {
  let ENV = {
    modulePrefix: 'brn',
    environment,
    rootURL: '/',
    locationType: 'auto',
    idleTimeout: 10000,
    EmberENV: {
      FEATURES: {
        // Here you can enable experimental features on an ember canary build
        // e.g. EMBER_NATIVE_DECORATOR_SUPPORT: true
      },
      EXTEND_PROTOTYPES: {
        // Prevent Ember Data from overriding Date.parse.
        Date: false,
      },
    },
    firebase: {
      apiKey: firebaseAPIKey,
      authDomain: firebaseAuthDomain,
      projectId: firebaseProjectId,
    },
    APP: {},
  };

  if (environment === 'development') {
    // ENV.APP.LOG_RESOLVER = true;
    // ENV.APP.LOG_ACTIVE_GENERATION = true;
    // ENV.APP.LOG_TRANSITIONS = true;
    // ENV.APP.LOG_TRANSITIONS_INTERNAL = true;
    // ENV.APP.LOG_VIEW_LOOKUPS = true;
    ENV.APP.RUNLOOP_DEBUG = true;
  }

  if (environment === 'test') {
    // Testem prefers this...
    ENV.locationType = 'none';

    // keep test console output quieter
    ENV.APP.LOG_ACTIVE_GENERATION = false;
    ENV.APP.LOG_VIEW_LOOKUPS = false;

    ENV.APP.rootElement = '#ember-testing';
    ENV.APP.autoboot = false;
  }

  if (environment === 'production') {
    // here you can enable a production-specific feature
  }

  return ENV;
};
