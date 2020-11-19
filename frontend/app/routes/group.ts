import Route from '@ember/routing/route';
// eslint-disable-next-line ember/no-mixins
import AuthenticatedRouteMixin from 'ember-simple-auth/mixins/authenticated-route-mixin';
import { inject as service } from '@ember/service';
import NetworkService from 'brn/services/network';
import GroupModel from 'brn/models/group';
export default class GroupRoute extends Route.extend(AuthenticatedRouteMixin) {
  @service('network') network!: NetworkService;
  model({ group_id }: { group_id: string }) {
    return this.store.findRecord('group', group_id);
  }

  async afterModel(group: GroupModel) {
    this.network.availableExercises(['12']);
    await this.store.query('series', { groupId: group.id });
  }

  redirect(group: GroupModel, { to } : { to: { name: string}}) {
    if (!group.get('sortedSeries.firstObject')) {
      this.transitionTo('groups');
    }
    if (to.name === 'group.index' && group.get('sortedSeries.firstObject')) {
      this.transitionTo(
        'group.series.index',
        group.id,
        group.get('sortedSeries.firstObject.id'),
      );
    }
  }
}
