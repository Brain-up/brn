import UserWeeklyStatisticsAdapter from './user-weekly-statistics';

export default class UserYearlyStatisticsAdapter extends UserWeeklyStatisticsAdapter {
  pathForType() {
    return 'statistics/study/year';
  }
}

declare module 'ember-data/types/registries/adapter' {
  export default interface AdapterRegistry {
    'statistics/study/year': UserYearlyStatisticsAdapter;
  }
}
