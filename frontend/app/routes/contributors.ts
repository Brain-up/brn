import Route from '@ember/routing/route';
import { inject as service } from '@ember/service';
import Store from '@ember-data/store';


export default class SpecialistsRoute extends Route {
    @service('store') store!: Store;
    async model() {
        const request = await this.store.findAll('contributor');
        const kinds = ['DEVELOPER', 'QA', 'DESIGNER', 'OTHER'];
        return request.filterBy('isActive', true).sortBy('contribution').reverse().filter(e => kinds.includes(e.kind)).toArray();
    }
}