import Route from '@ember/routing/route';
// eslint-disable-next-line ember/no-mixins
import AuthenticatedRouteMixin from 'ember-simple-auth/mixins/authenticated-route-mixin';
import GroupModel from 'brn/models/group';
import type Transition from '@ember/routing/-private/transition';
import { inject as service } from '@ember/service';
import NetworkService from 'brn/services/network';

// @ts-expect-error mixin
export default class GroupRoute extends Route.extend(AuthenticatedRouteMixin) {
  @service('network') network!: NetworkService;

  async model({ group_id }: { group_id: string }) {
    await this.network.loadCurrentUser();
    return await this.store.findRecord('group', group_id);
  }

  async afterModel(group: GroupModel) {
    await this.store.query('series', { groupId: group.id });
  }

  redirect(group: GroupModel, { to }: Transition) {
    if (!group.sortedSeries?.length) {
      this.transitionTo('groups');
    }
    if (to.name === 'group.index' && group.sortedSeries?.length) {
      this.transitionTo(
        'group.series.index',
        group.id,
        group.sortedSeries[0].id,
      );
    }
  }
}
