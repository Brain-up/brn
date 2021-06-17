import EmberRouter from '@ember/routing/router';
import config from 'brn/config/environment';

export default class Router extends EmberRouter {
  location = config.locationType;
  rootURL = config.rootURL;
}

Router.map(function () {
  this.route('groups', function () {});
  this.route('group', { path: 'groups/:group_id' }, function () {
    this.route('series', { path: 'series/:series_id' }, function () {
      this.route('subgroup', { path: 'subgroup/:subgroup_id' }, function () {
        this.route('exercise', { path: 'exercise/:exercise_id' }, function () {
          this.route('task', { path: 'task/:task_id' });
        });
      });
    });
  });
  this.route('not-accessable');
  this.route('login');
  this.route('registration');
  this.route('not-found', { path: '*wildcard_path' });
  this.route('password-recovery');
  this.route('user-agreement');
  this.route('description');
  this.route('profile');
});
