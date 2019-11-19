import checkInteractionRoute from './check-interaction';

export default checkInteractionRoute.extend({
  model({ series_id }) {
    return this.store.findRecord('series', series_id);
  },

  async afterModel(series, { to }) {
    this._super(...arguments);
    await this.store.query('exercise', { seriesId: series.id });
    if (to.name === 'series.index' && series.exercises.firstObject) {
      this.transitionTo('series.exercise', series.exercises.firstObject);
    }
  },
});
