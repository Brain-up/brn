import Route from '@ember/routing/route';
// eslint-disable-next-line ember/no-mixins
import AuthenticatedRouteMixin from 'ember-simple-auth/mixins/authenticated-route-mixin';

export default class GroupRoute extends Route.extend(AuthenticatedRouteMixin) {
  model({ group_id }) {
    return this.store.findRecord('group', group_id);
  }

  async afterModel(group) {
    await this.store.query('series', { groupId: group.id });
  }

  redirect(group, { to }) {
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
