import checkInteractionRoute from './check-interaction';

export default checkInteractionRoute.extend({
  async model({ series_id }) {
    const series = this.store.findRecord('series', series_id);
    if (!series.group) {
      await this.store.findAll('group');
    }
    return series;
  },

  async afterModel(series, { to }) {
    this._super(...arguments);
    await this.store.query('exercise', { seriesId: series.id });
    if (to.name === 'series.index' && series.sortedChildren.firstObject) {
      this.transitionTo('series.exercise', series.sortedChildren.firstObject);
    }
  },
});
