import Route from '@ember/routing/route';
// eslint-disable-next-line ember/no-mixins
import AuthenticatedRouteMixin from 'ember-simple-auth/mixins/authenticated-route-mixin';
import type GroupModel from 'brn/models/group';
import type SeriesModel from 'brn/models/series';
import type Transition from '@ember/routing/-private/transition';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { inject as service } from '@ember/service';
import type NetworkService from 'brn/services/network';
import type Store from 'brn/services/store';
import type Router from '@ember/routing/router-service';
import { sortByKey } from 'brn/utils/sort-by-key';

export interface GroupRouteModel {
  group: GroupModel;
  series: SeriesModel[];
}

export default class GroupRoute extends Route.extend(AuthenticatedRouteMixin) {
  @service('network') network!: NetworkService;
  @service('store') store!: Store;
  @service('router') declare router: Router;

  async model({ group_id }: { group_id: string }): Promise<GroupRouteModel> {
    await this.network.loadCurrentUser();
    const [group, series] = await Promise.all([
      this.store.findRecord<GroupModel>('group', group_id),
      this.store.query<SeriesModel>('series', { groupId: group_id }),
    ]);
    // Sort by id (matching the group extension's sortChildrenBy: 'id')
    const sortedSeries = sortByKey(Array.from(series || []), 'id');
    return { group, series: sortedSeries };
  }

  redirect({ group, series }: GroupRouteModel, { to }: Transition) {
    if (!series.length) {
      this.router.transitionTo('groups');
      return;
    }
    if (to.name === 'group.index') {
      this.router.transitionTo(
        'group.series.index',
        // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
        group.id!,
        // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
        series[0]!.id!,
      );
    }
  }
}
