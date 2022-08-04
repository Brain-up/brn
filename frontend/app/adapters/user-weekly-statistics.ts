import { DateTime } from 'luxon';
import ApplicationAdapter from './application';

export default class UserWeeklyStatisticsAdapter extends ApplicationAdapter {
  pathForType() {
    return 'v2/statistics/study/week';
  }

  sortQueryParams(query: { from: DateTime; to: DateTime }): {
    from: string;
    to: string;
  } {
    const newQuery = {
      from: query.from.toUTC().toFormat('yyyy-MM-dd\'T\'HH:mm:ss'),
      to: query.to.toUTC().toFormat('yyyy-MM-dd\'T\'HH:mm:ss'),
    };
    return newQuery;
  }
}

declare module 'ember-data/types/registries/adapter' {
  export default interface AdapterRegistry {
    'v2/statistics/study/week': UserWeeklyStatisticsAdapter;
  }
}
