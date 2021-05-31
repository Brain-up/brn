import Route from '@ember/routing/route';
import { inject as service } from '@ember/service';
import PersonsService from 'brn/services/persons';

export default class DescriptionRoute extends Route {
    @service('persons') personsService!: PersonsService;

    queryParams = {
        locale: {
            type: 'string',
            refreshModel: true
        }
    }

    model() {
        return this.personsService.personsContent;
    }
}