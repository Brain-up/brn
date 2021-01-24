import Route from '@ember/routing/route';

export default class GroupSeriesRoute extends Route {
  model({series_id}) {
    const seria = this.store.peekRecord('series', series_id);
    return this.store.query('subgroup', { seriesId: seria.id });
  }

  setupController(controller, model, transition) {
    super.setupController(controller, model, transition);
    controller.exerciseAvailabilityCalculationTask.perform();
  }

  // redirect(series, { to }) {
    // to-do fixit to `group.series.index`
    // if (
    //   to.name === 'series.index' &&
    //   series.get('sortedExercises.firstObject')
    // ) {
    //   this.transitionTo(
    //     'group.series.exercise',
    //     series.id,
    //     series.get('sortedExercises.firstObject.id'),
    //   );
    // }
  // }
}
