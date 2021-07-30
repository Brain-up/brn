import ApplicationAdapter from './application';

export default class UserWeeklyStatisticsAdapter extends ApplicationAdapter {
  pathForType() {
    return 'statistics/study/week';
  }
}

declare module 'ember-data/types/registries/adapter' {
  export default interface AdapterRegistry {
    'statistics/study/week': UserWeeklyStatisticsAdapter;
  }
}
