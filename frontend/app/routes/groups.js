import Route from '@ember/routing/route';
import AuthenticatedRouteMixin from 'ember-simple-auth/mixins/authenticated-route-mixin';

export default class GroupsRoute extends Route.extend(AuthenticatedRouteMixin) {
  model() {
    return this.store.findAll('group');
  }
  redirect() {
    document.body.classList.add('route-groups');
    document.body.classList.remove('route-group');
  }
}
