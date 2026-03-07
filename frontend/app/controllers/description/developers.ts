import Controller from '@ember/controller';
import { service } from '@ember/service';
import PersonsService from 'brn/services/persons';

export default class DescriptionDevelopersController extends Controller {
  @service('persons') personsService!: PersonsService;

  get persons() {
    return this.personsService.persons;
  }
}
