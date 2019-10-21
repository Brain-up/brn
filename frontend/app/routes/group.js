import Route from '@ember/routing/route';

export default Route.extend({
  model({ group_id }) {
    return this.store.findRecord('group', group_id, { include: 'series' });
  },

  async afterModel(series) {
    // in case if series was pre-loaded
    // asking for exercises again
    await series.hasMany('series').load();
  },
});
