import Route from '@ember/routing/route';
import { inject as service } from '@ember/service';
import type Session from 'ember-simple-auth/services/session';
import type Transition from '@ember/routing/-private/transition';

export default class RegistrationRoute extends Route {
  @service('session') declare session: Session;

  beforeModel(transition: Transition) {
    this.session.prohibitAuthentication('index');
  }
}
