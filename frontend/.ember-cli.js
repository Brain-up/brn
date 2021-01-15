const defaultConfig = {
  "disableAnalytics": false
};

if (process.env.GITPODURLHOST) {
  defaultConfig.liveReloadJsUrl = `https://${process.env.GITPODURLHOST}/_lr/livereload.js`
  defaultConfig.liveReloadOptions = {
    "port": 443,
    "https": true,
    "host": process.env.GITPODURLHOST
  }
}

module.exports =  defaultConfig;
