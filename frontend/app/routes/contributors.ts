import Route from '@ember/routing/route';
import { inject as service } from '@ember/service';
import Store from '@ember-data/store';


export default class SpecialistsRoute extends Route {
    @service('store') store!: Store;
    async model() {
        const request = await this.store.findAll('contributor');
        return request.filterBy('kind', 'DEVELOPER').filterBy('isActive', true).sortBy('contribution').reverse();
    }
}