import Route from '@ember/routing/route';
import type { Subgroup } from 'brn/schemas/subgroup';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { inject as service } from '@ember/service';
import type Store from 'brn/services/store';

export default class GroupSeriesRoute extends Route {
  @service('store') store!: Store;
  async model({ series_id }: { series_id: string }) {
    // Use the series_id directly for the subgroup query.
    // peekRecord('series', series_id) is cache-only and returns null on hard
    // reload / deep link when the series hasn't been fetched yet.
    // The series_id from the URL is all we need for the query.
    return this.store.query<Subgroup>('subgroup', { seriesId: series_id });
  }
}
