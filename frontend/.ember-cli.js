const defaultConfig = {
  "disableAnalytics": false
};

if (process.env.GITPODURLHOST) {
  console.log(process.env.GITPOD_WORKSPACE_URL);
  console.log(process.env.GITPODURLHOST);
  defaultConfig.liveReloadJsUrl = `https://${process.env.GITPODURLHOST}/_lr/livereload.js`
  defaultConfig.liveReloadOptions = {
    "port": 443,
    "https": true,
    "host": process.env.GITPODURLHOST
  }
}

module.exports =  defaultConfig;
