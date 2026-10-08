import Route from '@ember/routing/route';
import { service } from '@ember/service';
import type NetworkService from 'brn/services/network';
import type Store from 'brn/services/store';
import type { Headphone } from 'brn/schemas/headphone';

export default class AudiometryTestRoute extends Route {
  @service('network') declare network: NetworkService;
  @service('store') declare store: Store;

  async model(params: { audiometry_id: string }) {
    const [testResponse, headphones] = await Promise.all([
      this.network.request(`audiometrics/${params.audiometry_id}`).then((r) => r.json()),
      this.store.findAll<Headphone>('headphone'),
    ]);
    return { test: testResponse.data || {}, headphones };
  }
}
