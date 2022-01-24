import Route from '@ember/routing/route';
import Series from 'brn/models/series';
import { inject as service } from '@ember/service';
import type Store from '@ember-data/store';

export default class GroupSeriesRoute extends Route {
  @service('store') store!: Store;
  model({ series_id }: { series_id: string }) {
    const seria = this.store.peekRecord('series', series_id) as Series;
    return this.store.query('subgroup', { seriesId: seria.id });
  }

  // redirect(series, { to }) {
  // to-do fixit to `group.series.index`
  // if (
  //   to.name === 'series.index' &&
  //   series.get('sortedExercises.firstObject')
  // ) {
  //   this.transitionTo(
  //     'group.series.subgroup.exercise',
  //     series.id,
  //     series.get('sortedExercises.firstObject.id'),
  //   );
  // }
  // }
}
