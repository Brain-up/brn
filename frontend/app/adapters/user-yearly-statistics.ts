import UserWeeklyStatisticsAdapter from './user-weekly-statistics';

export default class UserYearlyStatisticsAdapter extends UserWeeklyStatisticsAdapter {
  pathForType() {
    return 'v2/statistics/study/year';
  }
}

declare module 'ember-data/types/registries/adapter' {
  export default interface AdapterRegistry {
    'v2/statistics/study/year': UserYearlyStatisticsAdapter;
  }
}
