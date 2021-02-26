import Route from '@ember/routing/route';
import { inject as service } from '@ember/service';
import Session from 'ember-simple-auth/services/session';

export default class IndexRoute extends Route {
  @service('session') session!: Session;
  redirect() {
    if (this.session.isAuthenticated) {
      this.replaceWith('groups');
    }
  }
}
