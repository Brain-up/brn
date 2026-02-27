import UserWeeklyStatisticsAdapter from './user-weekly-statistics';

export default class UserYearlyStatisticsAdapter extends UserWeeklyStatisticsAdapter {
  pathForType() {
    return 'v2/statistics/study/year';
  }
}

