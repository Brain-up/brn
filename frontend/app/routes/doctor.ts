import Route from '@ember/routing/route';
import { service } from '@ember/service';
import Session from 'ember-simple-auth/services/session';
import type Router from '@ember/routing/router-service';
import type UserDataService from 'brn/services/user-data';

export default class DoctorRoute extends Route {
  @service('session') session!: Session;
  @service('router') declare router: Router;
  @service('user-data') declare userData: UserDataService;

  beforeModel() {
    if (!this.session.isAuthenticated) {
      this.router.replaceWith('login');
      return;
    }
    if (!this.userData.isSpecialist) {
      this.router.replaceWith('groups');
    }
  }
}
