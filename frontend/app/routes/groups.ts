import Route from '@ember/routing/route';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { service } from '@ember/service';
import type NetworkService from 'brn/services/network';
import type Store from 'brn/services/store';
import type { Group as GroupModel } from 'brn/schemas/group';
import type UserDataService from 'brn/services/user-data';
import type Session from 'ember-simple-auth/services/session';
import type Transition from '@ember/routing/-private/transition';

export default class GroupsRoute extends Route {
  @service('network') network!: NetworkService;
  @service('store') store!: Store;
  @service('user-data') userData!: UserDataService;
  @service('session') declare session: Session;
  queryParams = {
    locale: {
      type: 'string',
      refreshModel: true,
    },
  };
  beforeModel(transition: Transition) {
    this.session.requireAuthentication(transition, 'login');
  }
  async model() {
    await this.network.loadCurrentUser();
    return await this.store.query<GroupModel>('group', {
      locale: this.userData.activeLocale,
    });
  }
}
