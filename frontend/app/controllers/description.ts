import Controller from '@ember/controller';
import { inject as service } from '@ember/service';
import PersonsService from 'brn/services/persons';

export default class DescriptionController extends Controller {
    @service('persons') personsService!: PersonsService;

    get persons() {
        return this.personsService.persons;
    }
}
