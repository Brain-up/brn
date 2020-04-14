import Route from '@ember/routing/route';

export default class GroupsRoute extends Route {
  model() {
    return this.store.findAll('group');
  }
}
