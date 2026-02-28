import Route from '@ember/routing/route';
// eslint-disable-next-line ember/no-mixins
import AuthenticatedRouteMixin from 'ember-simple-auth/mixins/authenticated-route-mixin';
import type GroupModel from 'brn/models/group';
import type SeriesModel from 'brn/models/series';
import type Transition from '@ember/routing/-private/transition';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { inject as service } from '@ember/service';
import NetworkService from 'brn/services/network';
import type Store from 'brn/services/store';
import type Router from '@ember/routing/router-service';
export default class GroupRoute extends Route.extend(AuthenticatedRouteMixin) {
  @service('network') network!: NetworkService;
  @service('store') store!: Store;
  @service('router') declare router: Router;

  async model({ group_id }: { group_id: string }) {
    await this.network.loadCurrentUser();
    return await this.store.findRecord<GroupModel>('group', group_id);
  }

  async afterModel(group: GroupModel) {
    await this.store.query<SeriesModel>('series', { groupId: group.id });
  }

  redirect(group: GroupModel, { to }: Transition) {
    if (!group.sortedSeries?.length) {
      this.router.transitionTo('groups');
    }
    if (to.name === 'group.index' && group.sortedSeries?.length) {
      this.router.transitionTo(
        'group.series.index',
        // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
        group.id!,
        // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
        group.sortedSeries[0]!.id!,
      );
    }
  }
}
