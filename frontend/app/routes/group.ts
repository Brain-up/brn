import Route from '@ember/routing/route';
import type { Group as GroupModel } from 'brn/schemas/group';
import type { Series as SeriesModel } from 'brn/schemas/series';
import type Transition from '@ember/routing/-private/transition';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { inject as service } from '@ember/service';
import type NetworkService from 'brn/services/network';
import type Store from 'brn/services/store';
import type Router from '@ember/routing/router-service';
import type Session from 'ember-simple-auth/services/session';
import { sortByKey } from 'brn/utils/sort-by-key';

export interface GroupRouteModel {
  group: GroupModel;
  series: SeriesModel[];
}

export default class GroupRoute extends Route {
  @service('network') network!: NetworkService;
  @service('store') store!: Store;
  @service('router') declare router: Router;
  @service('session') declare session: Session;

  beforeModel(transition: Transition) {
    this.session.requireAuthentication(transition, 'login');
  }

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

  async redirect(model: GroupRouteModel | GroupModel, { to }: Transition) {
    // When navigating via <LinkTo @route="group" @model={{record}}>, Ember
    // passes the raw group record directly (bypassing model()), so we need
    // to handle both the composite { group, series } and a bare GroupModel.
    let group: GroupModel;
    let series: SeriesModel[];
    if ('group' in model && 'series' in model) {
      group = (model as GroupRouteModel).group;
      series = (model as GroupRouteModel).series;
    } else {
      group = model as GroupModel;
      series = await this.store.query<SeriesModel>('series', { groupId: group.id! });
    }

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
