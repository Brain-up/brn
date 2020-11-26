import Route from '@ember/routing/route';
// eslint-disable-next-line ember/no-mixins
import AuthenticatedRouteMixin from 'ember-simple-auth/mixins/authenticated-route-mixin';
import { inject as service } from '@ember/service';

export default class GroupsRoute extends Route.extend(AuthenticatedRouteMixin) {
  @service('intl') intl;
  queryParams = {
    locale: {
      type: 'string',
      refreshModel: true
    }
  }
  model() {
    return this.store.query('group', {
      locale: this.intl.locale[0]
    });
  }
}
