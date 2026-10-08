import Route from '@ember/routing/route';
import { service } from '@ember/service';
import Session from 'ember-simple-auth/services/session';
import type Router from '@ember/routing/router-service';

export default class IndexRoute extends Route {
  @service('session') session!: Session;
  @service('router') declare router: Router;
  redirect() {
    if (this.session.isAuthenticated) {
      this.router.replaceWith('groups');
    }
  }
}
