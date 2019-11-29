import Route from '@ember/routing/route';

export default Route.extend({
  async model({ series_id }) {
    const series = this.store.findRecord('series', series_id);
    if (!series.group) {
      await this.store.findAll('group');
    }
    return series;
  },

  async afterModel(series, { to }) {
    if (!series.canInteract) {
      this.transitionTo('group', series.get('group.id'));
      return;
    }

    await this.store.query('exercise', { seriesId: series.id });
    if (
      to.name === 'series.index' &&
      series.get('sortedExercises.firstObject')
    ) {
      this.transitionTo(
        'series.exercise.index',
        series.get('sortedExercises.firstObject.id'),
      );
    }
  },
});
