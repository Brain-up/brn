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
import type GroupController from 'brn/controllers/group';

export default class GroupRoute extends Route.extend(AuthenticatedRouteMixin) {
  @service('network') network!: NetworkService;
  @service('store') store!: Store;
  @service('router') declare router: Router;

  // Store the loaded series for reliable redirect logic.
  // group.sortedSeries depends on the hasMany being populated in the JSON:API
  // cache, which may not work if the cache doesn't auto-populate inverses.
  private _loadedSeries: SeriesModel[] = [];

  async model({ group_id }: { group_id: string }) {
    await this.network.loadCurrentUser();
    return await this.store.findRecord<GroupModel>('group', group_id);
  }

  async afterModel(group: GroupModel) {
    const series = await this.store.query<SeriesModel>('series', { groupId: group.id });
    // Sort by id (matching the group extension's sortChildrenBy: 'id')
    this._loadedSeries = Array.from(series || []).sort((a: any, b: any) => {
      const aId = String(a.id ?? '');
      const bId = String(b.id ?? '');
      if (aId < bId) return -1;
      if (aId > bId) return 1;
      return 0;
    });
  }

  setupController(controller: GroupController, model: GroupModel) {
    super.setupController(controller, model);
    // Pass loaded series to the controller so the GroupNavigation template
    // has a reliable data source even if group.series hasMany isn't resolved.
    controller.loadedSeries = this._loadedSeries;
  }

  redirect(group: GroupModel, { to }: Transition) {
    // Use the directly-loaded series rather than group.sortedSeries,
    // which depends on hasMany inverse population in the cache.
    const sortedSeries = this._loadedSeries;
    if (!sortedSeries.length) {
      this.router.transitionTo('groups');
      return;
    }
    if (to.name === 'group.index') {
      this.router.transitionTo(
        'group.series.index',
        // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
        group.id!,
        // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
        sortedSeries[0]!.id!,
      );
    }
  }
}
