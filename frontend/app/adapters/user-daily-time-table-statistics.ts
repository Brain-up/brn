import {DateTime} from 'luxon';
import ApplicationAdapter from './application';

export default class UserDailyTimeTableStatisticsAdapter extends ApplicationAdapter {
  pathForType() {
    return 'v2/statistics/study/day';
  }

  sortQueryParams(query: { day: DateTime }): {
    day: string;
  } {
    return {
      day: query.day.toUTC().toFormat('yyyy-MM-dd\'T\'HH:mm:ss'),
    };
  }
}

declare module 'ember-data/types/registries/adapter' {
  export default interface AdapterRegistry {
    'v2/statistics/study/day': UserDailyTimeTableStatisticsAdapter;
  }
}
