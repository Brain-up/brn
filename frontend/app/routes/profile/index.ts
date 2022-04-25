import Route from '@ember/routing/route';
import { inject as service } from '@ember/service';
import NetworkService from 'brn/services/network';
export default class ProfileRoute extends Route {
  @service('network') declare network: NetworkService;
  async model() {
    await this.network.loadCurrentUser();
  }
}
