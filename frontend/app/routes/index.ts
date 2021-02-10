import Route from '@ember/routing/route';
import { inject as service } from '@ember/service';
import NetworkService from 'brn/services/network';
export default class IndexRoute extends Route {
  @service('network') network!: NetworkService;
  model() {
    return this.network.loadCurrentUser();
  }
  redirect() {
    this.transitionTo('groups');
  }
}
