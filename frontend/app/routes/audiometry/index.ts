import Route from '@ember/routing/route';
import { service } from '@ember/service';
import type NetworkService from 'brn/services/network';
import type Store from 'brn/services/store';
import type UserDataService from 'brn/services/user-data';
import type { Headphone } from 'brn/schemas/headphone';

export default class AudiometryIndexRoute extends Route {
  @service('network') declare network: NetworkService;
  @service('store') declare store: Store;
  @service('user-data') declare userData: UserDataService;

  async model() {
    const locale = this.userData.activeLocale;
    const [testsResponse, headphones] = await Promise.all([
      this.network.request(`audiometrics?locale=${encodeURIComponent(locale)}`).then((r) => r.json()),
      this.store.findAll<Headphone>('headphone'),
    ]);
    return { tests: testsResponse.data || [], headphones };
  }
}
