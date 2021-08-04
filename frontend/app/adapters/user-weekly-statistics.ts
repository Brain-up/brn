import { DateTime } from 'luxon';
import ApplicationAdapter from './application';

export default class UserWeeklyStatisticsAdapter extends ApplicationAdapter {
  pathForType() {
    return 'statistics/study/week';
  }

  sortQueryParams(query: { from: DateTime; to: DateTime }): {
    from: string;
    to: string;
  } {
    const newQuery = {
      from: query.from.toUTC().toFormat('yyyy-MM-dd'),
      to: query.to.toUTC().toFormat('yyyy-MM-dd'),
    };
    return newQuery;
  }
}

declare module 'ember-data/types/registries/adapter' {
  export default interface AdapterRegistry {
    'statistics/study/week': UserWeeklyStatisticsAdapter;
  }
}
