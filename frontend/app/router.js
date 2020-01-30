import EmberRouter from '@ember/routing/router';
import config from './config/environment';

export default class Router extends EmberRouter {
  location = config.locationType;
  rootURL = config.rootURL;
}

Router.map(function() {
  this.route('groups', function() {});
  this.route('group', { path: 'groups/:group_id' }, function() {
    this.route('series', { path: 'series/:series_id' }, function() {
      this.route('exercise', { path: 'exercise/:exercise_id' }, function() {
        this.route('task', { path: 'task/:task_id' });
      });
    });
  });
  this.route('not-accessable');
});
