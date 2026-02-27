import Route from '@ember/routing/route';
import { inject as service } from '@ember/service';
import { type Store } from '@warp-drive-mirror/core';


export default class ContributorsRoute extends Route {
    @service('store') store!: Store;
    async model() {
        const request = await this.store.findAll('contributor');
        const kinds = ['DEVELOPER', 'QA', 'DESIGNER', 'OTHER', 'AUTOTESTER'];
        return request
            .filter((e) => e.isActive)
            .sort((a, b) => b.contribution - a.contribution)
            .filter((e) => kinds.includes(e.kind));
    }
}
