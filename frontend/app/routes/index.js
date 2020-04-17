import Route from '@ember/routing/route';
import { inject as service } from '@ember/service';
export default class IndexRoute extends Route {
  @service('network') network;
  model() {
    return this.network.loadCurrentUser();
  }
  redirect() {
    this.transitionTo('groups');
  }
}
