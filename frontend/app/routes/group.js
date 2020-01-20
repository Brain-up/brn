import Route from '@ember/routing/route';

export default Route.extend({
  model({ group_id }) {
    return this.store.findRecord('group', group_id);
  },

  async afterModel(group, { to }) {
    await this.store.query('series', { groupId: group.id });
    if (to.name === 'group.index' && group.get('sortedSeries.firstObject')) {
      this.transitionTo(
        'group.series.index',
        group.id,
        group.get('sortedSeries.firstObject.id'),
      );
    }
  },
});
