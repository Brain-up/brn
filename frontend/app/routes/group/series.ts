import Route from '@ember/routing/route';
import type Series from 'brn/models/series';
import type Subgroup from 'brn/models/subgroup';
import { inject as service } from '@ember/service';
import type Store from 'brn/services/store';

export default class GroupSeriesRoute extends Route {
  @service('store') store!: Store;
  model({ series_id }: { series_id: string }) {
    const seria = this.store.peekRecord<Series>('series', series_id);
    return this.store.query<Subgroup>('subgroup', { seriesId: seria!.id });
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
