import ApplicationAdapter from './application';

export default class UserYearlyStatisticsAdapter extends ApplicationAdapter {
  pathForType() {
    return 'statistics/study/year';
  }
}

declare module 'ember-data/types/registries/adapter' {
  export default interface AdapterRegistry {
    'statistics/study/year': UserYearlyStatisticsAdapter;
  }
}
