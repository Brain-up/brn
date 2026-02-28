import Route from '@ember/routing/route';
import { inject as service } from '@ember/service';
import type Store from 'brn/services/store';
import type Contributor from 'brn/models/contributor';


export default class SpecialistsRoute extends Route {
    @service('store') store!: Store;
    async model() {
        const request = await this.store.findAll<Contributor>('contributor');
        return request
            .filter((e) => e.kind === 'SPECIALIST')
            .filter((e) => e.isActive)
            .sort((a, b) => b.contribution - a.contribution);
    }
}