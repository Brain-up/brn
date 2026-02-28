import Route from '@ember/routing/route';
// eslint-disable-next-line ember/no-mixins
import AuthenticatedRouteMixin from 'ember-simple-auth/mixins/authenticated-route-mixin';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { inject as service } from '@ember/service';
import type NetworkService from 'brn/services/network';
import type Store from 'brn/services/store';
import type GroupModel from 'brn/models/group';
import type UserDataService from 'brn/services/user-data';

export default class GroupsRoute extends Route.extend(AuthenticatedRouteMixin) {
  @service('network') network!: NetworkService;
  @service('store') store!: Store;
  @service('user-data') userData!: UserDataService;
  queryParams = {
    locale: {
      type: 'string',
      refreshModel: true,
    },
  };
  async model() {
    await this.network.loadCurrentUser();
    return await this.store.query<GroupModel>('group', {
      locale: this.userData.activeLocale,
    });
  }
}
