import Route from '@ember/routing/route';

export default Route.extend({
  model({ series_id }) {
    return this.store.findRecord('series', series_id);
  },

  async afterModel(series, { to }) {
    await this.store.query('exercise', { seriesId: series.id });
    if (to.name === 'series.index' && series.exercises.firstObject) {
      this.transitionTo('series.exercise', series.exercises.firstObject);
    }
  },
});
