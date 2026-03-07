import Route from '@ember/routing/route';
import { service } from '@ember/service';
import type Session from 'ember-simple-auth/services/session';
import type Transition from '@ember/routing/transition';

export default class LoginRoute extends Route {
  @service('session') declare session: Session;

  beforeModel(_transition: Transition) {
    this.session.prohibitAuthentication('index');
  }
}
