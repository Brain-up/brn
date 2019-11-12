import Route from '@ember/routing/route';

export default Route.extend({
  model({ series_id }) {
    return this.store.findRecord('series', series_id);
  },

  async afterModel(series, { to }) {
    // TODO: remove userID:1 approx after Nov 20 2019
    await this.store.query('exercise', { seriesId: series.id, userId: 1 });
    // await this.store.query('exercise', { seriesId: series.id, });
    if (to.name === 'series.index' && series.exercises.firstObject) {
      this.transitionTo('series.exercise', series.exercises.firstObject);
    }
  },
});
