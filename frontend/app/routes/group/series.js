import Route from '@ember/routing/route';

export default Route.extend({
  model({ series_id }) {
    return this.store.findRecord('series', series_id);
  },

  async afterModel(series, { to }) {
    await this.store.query('exercise', { seriesId: series.id });
  },

  redirect(series, { to }) {
    if (
      to.name === 'series.index' &&
      series.get('sortedExercises.firstObject')
    ) {
      this.transitionTo(
        'route.index',
        series.id,
        series.get('sortedExercises.firstObject.id'),
      );
    }
  }
});
