const defaultConfig = {
  "disableAnalytics": false
};

if (process.env.GITPOD_WORKSPACE_URL) {
  let workspaceURL = new URL(process.env.GITPOD_WORKSPACE_URL);
  let port = 4200;
  defaultConfig.liveReloadJsUrl = `https://${port}-${workspaceURL.host}/_lr/livereload.js`
  defaultConfig.liveReloadOptions = {
    "port": 443,
    "https": true,
    "host": process.env.GITPODURLHOST
  }
}

module.exports =  defaultConfig;
